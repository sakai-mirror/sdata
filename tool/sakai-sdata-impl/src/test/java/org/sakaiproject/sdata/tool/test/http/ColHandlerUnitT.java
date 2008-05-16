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

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 * @author ieb
 */
public class ColHandlerUnitT extends TestCase
{

	/**
	 * @param arg0
	 */
	public ColHandlerUnitT(String arg0)
	{
		super(arg0);
	}

	private static final Log log = LogFactory.getLog(JsonHandlerUnitT.class);

	private static final String LOGIN_BASE_URL = "http://localhost:8080/portal/relogin";

	private static final String USERNAME = "admin";

	private static final String PASSWORD = "admin";

	private static final String BASE_URL = "http://localhost:8080/sdata/";

	private static final String BASE_DATA_URL = BASE_URL + "col";

	protected HttpClient client;

	protected boolean enabled = true;

	private byte[] buffer;

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
	 * @see org.sakaiproject.sdata.tool.test.http.JsonUserStorageServletUnitT#getBaseUrl()
	 */
	protected String getBaseUrl()
	{
		return BASE_URL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.test.http.JsonUserStorageServletUnitT#getBaseDataUrl()
	 */
	protected String getBaseDataUrl()
	{
		return BASE_DATA_URL;
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
		client.executeMethod(postMethod);
		postMethod.getStatusCode();

		if (postMethod.getStatusCode() == 401)
		{
			log.info("Login " + postMethod.getURI() + " to Said "
					+ postMethod.getStatusCode() + " " + postMethod.getStatusText() + " "
					+ postMethod.getResponseBodyAsString());
			fail("Failed to login ");
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGet() throws Exception
	{
		if (enabled)
		{
			login();
			doUpload();
			PostMethod method = new PostMethod(getBaseDataUrl());
			method.addParameter("uri", "");
			client.executeMethod(method);

			checkHandler(method);
			int code = method.getStatusCode();
			assertTrue("Should have been a 200  ", (code == 200));
			String response = method.getResponseBodyAsString();
			log.info("Got " + response);
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	public void doUpload() throws Exception
	{
		if (enabled)
		{

			PostMethod method = new PostMethod(getBaseUrl() + "f/dirlist");

			log.info("Uloading to " + getBaseUrl() + "f/dirlist");

			Part[] parts = new Part[20];
			for (int i = 0; i < parts.length; i++)
			{
				parts[i] = new FilePart("multifile" + i, new ByteArrayPartSource(
						"multifile" + i, buffer));
			}
			method
					.setRequestEntity(new MultipartRequestEntity(parts, method
							.getParams()));

			client.executeMethod(method);

			int code = method.getStatusCode();

			log.info(method.getResponseBodyAsString());

			assertEquals("Should have been a 200 ", 200, code);
			// checkHandler(method);

			String content = method.getResponseBodyAsString();
			log.info("Content\n" + content);

			for (int i = 0; i < 20; i++)
			{
				GetMethod gmethod = new GetMethod(getBaseUrl() + "f/dirlist/multifile"
						+ i);
				log.info("Trying " + "dirlist/multifile" + i);
				client.executeMethod(gmethod);
				// checkHandler(gmethod);

				int rcode = gmethod.getStatusCode();
				assertEquals("Expected a 200 response ", 200, rcode);
				assertEquals("Content Lenght does not match  ", buffer.length, gmethod
						.getResponseContentLength());
				int contentL = (int) gmethod.getResponseContentLength();
				log.info("Got " + contentL + " bytes ");
				DataInputStream in = new DataInputStream(gmethod
						.getResponseBodyAsStream());
				byte[] buffer2 = new byte[contentL];
				in.readFully(buffer2);
				assertEquals("Upload Size is not the same as download size ",
						buffer.length, buffer2.length);
				for (int j = 0; j < buffer2.length; j++)
				{
					assertEquals("Byte at Offset " + j + " corrupted ", buffer[j],
							buffer2[j]);
				}
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}

	}

	/**
	 * @param resp
	 */
	protected void checkHandler(HttpMethod resp)
	{
		String className = this.getClass().getName();
		className = className.substring(className.lastIndexOf('.'));
		className = className.substring(0, className.length() - "UnitT".length());
		Header h = resp.getResponseHeader("x-sdata-handler");
		if (h == null)
		{
			try
			{
				log.info("Request was " + resp.getURI());
				log.info("Failed Content was " + resp.getResponseBodyAsString());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		assertNotNull("Handler Not found ", h);
		String handler = h.getValue();
		assertTrue("Handler Not found (no value)", handler.trim().length() > 0);
		handler = handler.substring(handler.lastIndexOf('.'));
		assertEquals("Not the expected Handler Class", className, handler);
	}

}
