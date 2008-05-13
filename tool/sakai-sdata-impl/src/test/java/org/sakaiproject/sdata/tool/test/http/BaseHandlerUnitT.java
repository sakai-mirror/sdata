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
import java.net.MalformedURLException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * @author ieb
 *
 */
public abstract class BaseHandlerUnitT extends TestCase
{
	private static final Log log = LogFactory.getLog(BaseHandlerUnitT.class);

	// use the container login url
	private static final String LOGIN_BASE_URL = "http://localhost:8080/portal/relogin";

	private static final String USERNAME = "admin";

	private static final String PASSWORD = "admin";

	protected HttpClient client;

	protected boolean enabled = true;
	
	protected byte[] buffer;


	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		try
		{
			client = new HttpClient();
			client.getState().setCredentials(
					new AuthScope("localhost", 8080, "LocalSakaiName"),
					new UsernamePasswordCredentials("admin", "admin"));
			GetMethod method = new GetMethod(getBaseUrl() + "checkRunning");
			method.setDoAuthentication(false);
			method.setFollowRedirects(true);
			method.setRequestHeader("x-testdata-size", "2048");
			client.executeMethod(method);
			if (method.getStatusCode() == 200)
			{
				buffer = method.getResponseBody();
				enabled = true;
			}
			else
			{
				enabled = false;
			}
		}
		catch (HttpException he)
		{
			enabled = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 */
	protected void login() throws MalformedURLException, IOException
	{
		PostMethod postMethod = new PostMethod(LOGIN_BASE_URL);
		postMethod.setParameter("eid", USERNAME);
		postMethod.setParameter("pw", PASSWORD);
		postMethod.setParameter("submit", "Login");
		postMethod.setDoAuthentication(true);
		
		log.info("Performing Login "+LOGIN_BASE_URL);
		client.executeMethod(postMethod);
		log.info("Done Performing Login");
		postMethod.getStatusCode();

		if (postMethod.getStatusCode() == 401)
		{
			log.info("Login " + postMethod.getURI() + " to Said "
					+ postMethod.getStatusCode() + " " + postMethod.getStatusText() + " "
					+ postMethod.getResponseBodyAsString());
			fail("Failed to login ");
		}
		log.info("Login " + postMethod.getURI() + " to Said "
				+ postMethod.getStatusCode() + " " + postMethod.getStatusText() + " "
				+ postMethod.getResponseBodyAsString());
	}
	/**
	 * @return
	 */
	protected abstract String getBaseUrl();

}
