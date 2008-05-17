/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.sdata.tool.test.http;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sdata.tool.functions.CHSMoveFunction;

/**
 * @author ieb
 */
public class CHSMoveFuntionUnitT extends BaseHandlerUnitT
{

	private static final String BASE_URL = "http://localhost:8080/sdata/";

	private static final String BASE_DATA_URL = BASE_URL + "c/private/sdata";

	private static final Log log = LogFactory.getLog(CHSMoveFuntionUnitT.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.test.http.BaseHandlerUnitT#getBaseUrl()
	 */
	@Override
	protected String getBaseUrl()
	{
		return BASE_URL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.test.http.BaseHandlerUnitT#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		createDocument();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.test.http.BaseHandlerUnitT#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		deleteDocument();
		super.tearDown();
	}

	/**
	 * @return
	 */
	@Override
	protected String getTestDocument()
	{
		return getBaseDataUrl() + this.getClass().getName();
	}

	protected String getTargetDocument()
	{
		return getTargetFolder() + this.getClass().getName();
	}

	protected String getTargetFolder()
	{
		return getBaseDataUrl() + "/destination/";
	}

	protected String getTargetPath()
	{
		return "/private/sdata/destination/";
	}

	protected String getSourcePath()
	{
		return "/private/sdata/";
	}

	protected String getBaseDataUrl()
	{
		return BASE_DATA_URL;
	}

	public void testMove() throws Exception
	{
		if (enabled)
		{
			String testDocument = getTestDocument();
			String targetPath = getTargetPath();
			String targetDocument = getTargetDocument();

			createDocument(targetDocument + "-foldergen");

			deleteDocument(targetDocument, false);

			moveDocument(testDocument, targetPath);
			checkDocumentExists(testDocument, false);

			deleteDocument(targetDocument, false);

		}
	}


	/**
	 * @param testDocument
	 * @param b
	 * @throws IOException
	 * @throws HttpException
	 */
	private void checkDocumentExists(String testDocument, boolean exists)
			throws HttpException, IOException
	{
		GetMethod validate = new GetMethod(testDocument + "?f=m");
		client.executeMethod(validate);
		int code = validate.getStatusCode();

		if (exists)
		{
			assertEquals("File does not exist and should " + validate.getStatusLine(),
					200, code);
		}
		else
		{
			assertEquals("File exists, should not " + validate.getStatusLine(), 404, code);

		}

	}

	/**
	 * @param testDocument
	 * @param b
	 * @throws IOException
	 * @throws HttpException
	 */
	private void moveDocument(String sourceDocument, String targetFolder)
			throws HttpException, IOException
	{
		log.info("Moving " + sourceDocument + " to " + targetFolder);
		PostMethod method = new PostMethod(sourceDocument);
		method.setParameter("f", "mv");
		method.setParameter(CHSMoveFunction.TO, targetFolder);
		client.executeMethod(method);
		int code = method.getStatusCode();

		assertEquals("Move Failed " + method.getStatusLine(), 200, code);

	}

}
