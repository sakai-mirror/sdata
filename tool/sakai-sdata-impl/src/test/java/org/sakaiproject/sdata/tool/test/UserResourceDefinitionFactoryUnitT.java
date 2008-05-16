/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Timefields Ltd
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.sdata.tool.test;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.ResourceDefinitionFactory;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.util.UserResourceDefinitionFactory;

/**
 * @author ieb
 */
public class UserResourceDefinitionFactoryUnitT extends TestCase
{

	private static final String[] users = { null, "", "test", "~test" };

	private static final Log log = LogFactory
			.getLog(UserResourceDefinitionFactoryUnitT.class);

	private String[] basePaths = { "/", "/sakai", "/sakai/", null, "" };

	private String[] testPaths = { "sdfsdfsdf", "sdfsdf/", "/",
			"/sdfsdf/sdfsdf/sdfsdf/sdfssdf/12321", "sdfsdfs/sdfsd/sdfsdf/sdfsdf/sdf/", "" };

	/**
	 * @param arg0
	 */
	public UserResourceDefinitionFactoryUnitT(String arg0)
	{
		super(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

	/**
	 * 
	 */
	public void testCreation()
	{
		for (String basePath : basePaths)
		{
			ResourceDefinitionFactory rdf = new UserResourceDefinitionFactory(basePath);
			for (String testPath : testPaths)
			{
				MockResourceDefinitionRequest request = new MockResourceDefinitionRequest(
						testPath);
				for (String user : users)
				{
					request.setRemoteUser(user);
					try
					{
						ResourceDefinition rd = rdf.getSpec(request);
						/*
						 * System.err.println(basePath + ":" + testPath +
						 * ":getExternalPath():" +
						 * rd.getExternalPath(testPath));
						 * System.err.println(basePath + ":" + testPath +
						 * ":getRepositoryPath():" + rd.getRepositoryPath());
						 * System.err.println(basePath + ":" + testPath +
						 * ":getRepositoryPath(extra):" +
						 * rd.getRepositoryPath("extra"));
						 */
						String rp = rd.getRepositoryPath();

						assertTrue("Repository Paths must not be null ", rp != null);
						assertTrue("Repository Paths must be absolute ", rp
								.startsWith("/"));
						assertTrue(
								"Repository Paths must not end in /, except when root ",
								rp.equals("/") || !rp.endsWith("/"));
						assertTrue(
								"Repository Paths must not have white space at either end ",
								rp.length() == rp.trim().length());
						log.info("Path :" + rp);
						assertTrue("Repository Paths must no have // ",
								rp.indexOf("//") < 0);
						String[] elements = rp.split("/");
						if (elements.length != 0)
						{
							char c = elements[elements.length - 1].charAt(0);
							assertTrue(
									"Last Element of a repository paths cant start with a number ",
									!Character.isDigit(c));
						}
						rp = rd.getExternalPath(testPath);
						assertTrue("External Paths must not be null ", rp != null);
						// assertTrue("External Paths must not end in /, except
						// when
						// root ",rp.equals("/") || !rp.endsWith("/"));
						assertTrue(
								"External Paths must not have white space at either end ",
								rp.length() == rp.trim().length());
						assertTrue("External Patsh must no have // ",
								rp.indexOf("//") < 0);
						rp = rd.getRepositoryPath("extra");
						assertTrue("Extra Repository Paths must not be null ", rp != null);
						assertTrue("Extra Repository Paths must be absolute ", rp
								.startsWith("/"));
						assertTrue(
								"Extra Repository Paths must not end in /, except when root ",
								rp.equals("/") || !rp.endsWith("/"));
						assertTrue(
								"Extra Repository Paths must not have white space at either end ",
								rp.length() == rp.trim().length());
						assertTrue("Extra Repository Patsh must no have // ", rp
								.indexOf("//") < 0);
					}
					catch (SDataException sde)
					{
						if (user != null && user.trim().length() != 0)
						{
							fail("Problem with dispatcher " + sde.getMessage());
						}
					}
				}

			}
		}

	}

}
