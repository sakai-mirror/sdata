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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.HttpInternalErrorException;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.UploadFileSpec;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;

/**
 * @author ieb
 */

public class XmlRpcUserStorageServletUnitT extends TestCase
{
	private static final Log log = LogFactory.getLog(XmlRpcUserStorageServletUnitT.class);

	private static final String LOGIN_BASE_URL = "http://localhost:8080/portal/relogin";

	private static final String BASE_URL = "http://localhost:8080/sdata/";

	private static final String BASE_JCR_URL = BASE_URL + "xp/";

	private static final String USERNAME = "admin";

	private static final String PASSWORD = "admin";

	ServletUnitClient client = null;

	private WebConversation wc;

	private boolean enabled = true;

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
			wc = new WebConversation();
			WebRequest req = new GetMethodWebRequest(BASE_URL + "testpage.html");
			WebResponse resp = wc.getResponse(req);
			DataInputStream inputStream = new DataInputStream(resp.getInputStream());
			buffer = new byte[resp.getContentLength()];
			inputStream.readFully(buffer);
		}
		catch (HttpNotFoundException notfound)
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

	public void testSinglePage()
	{
		if (enabled)
		{
			log.info("Single Page Test Enabled");
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	public void testGet401() throws Exception
	{
		if (enabled)
		{
			WebResponse resp = null;
			try
			{
				WebRequest req = new GetMethodWebRequest(BASE_JCR_URL + "testpage");
				
				resp = wc.getResponse(req);
				fail("Should have been a 401, got:" + resp.getResponseCode());
			}
			catch (HttpInternalErrorException iex)
			{
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
			catch ( HttpException hex ) {
				assertEquals("Should have been Unauthorized ",401, hex.getResponseCode());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}
	public void testGet404() throws Exception
	{
		if (enabled)
		{
			WebResponse resp = null;
			try
			{
				login();
				WebRequest req = new GetMethodWebRequest(BASE_JCR_URL + "testpage");
				resp = wc.getResponse(req);
				fail("Should have been a 404, got:" + resp.getResponseCode());
			}
			catch (HttpNotFoundException nfex)
			{
				// this is Ok
			}
			catch (HttpInternalErrorException iex)
			{
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
			catch ( HttpException hex ) {
				fail("Authorization Failed");
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}
	private void login() throws MalformedURLException, IOException, SAXException {
		PostMethodWebRequest postMethod = new PostMethodWebRequest(LOGIN_BASE_URL);
		postMethod.setParameter("eid", USERNAME);
		postMethod.setParameter("pw",PASSWORD);
		postMethod.setParameter("submit","Login");
		WebResponse resp = wc.getResponse(postMethod);
	}

	public void testUpload() throws Exception
	{
		if (enabled)
		{
			try
			{

				login();
				ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
				WebRequest req = new PutMethodWebRequest(BASE_JCR_URL + "putUpload",
						bais, "UTF-8");
				req.setParameter("snoop", "1");
				req.setHeaderField("Content-Type", "text/html");
				req.setHeaderField("Content-Encoding", "UTF-8");
				wc.setAuthorization(USERNAME, PASSWORD);
				WebResponse resp = wc.getResponse(req);
				int code = resp.getResponseCode();
				assertTrue("Should have been a 201 or 204 ", (code == 201)
						|| (code == 204));
			}
			catch (HttpNotFoundException nfex)
			{
				fail("Failed with " + nfex.getResponseCode() + " Cause: "
						+ nfex.getResponseMessage());
			}
			catch (HttpInternalErrorException iex)
			{
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	public void testUploadDownload() throws Exception
	{
		if (enabled)
		{
			try
			{
				login();
				ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
				WebRequest req = new PutMethodWebRequest(BASE_JCR_URL + "putUpload",
						bais, "UTF-8");
				req.setParameter("snoop", "1");
				req.setHeaderField("Content-Type", "text/html");
				req.setHeaderField("Content-Encoding", "UTF-8");
				WebResponse resp = wc.getResponse(req);
				int code = resp.getResponseCode();
				assertTrue("Should have been a 201 or 204 ", (code == 201)
						|| (code == 204));

				req = new GetMethodWebRequest(BASE_JCR_URL + "putUpload");
				resp = wc.getResource(req);
				code = resp.getResponseCode();
				assertTrue("Should have been a 201 or 204 ", (code == 200));
				int contentL = resp.getContentLength();
				log.info("Got " + contentL + " bytes ");
				DataInputStream in = new DataInputStream(resp.getInputStream());
				byte[] buffer2 = new byte[contentL];
				in.readFully(buffer2);
				assertEquals("Upload Size is not the same as download size ",
						buffer.length, buffer2.length);
				for (int i = 0; i < buffer2.length; i++)
				{
					assertEquals("Byte at Offset " + i + " corrupted ", buffer[i],
							buffer2[i]);
				}
			}
			catch (HttpNotFoundException nfex)
			{
				fail("Failed with " + nfex.getResponseCode() + " Cause: "
						+ nfex.getResponseMessage());
			}
			catch (HttpInternalErrorException iex)
			{
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	public void testDirectory() throws Exception
	{
		if (enabled)
		{
			try
			{
				login();
				for (int i = 0; i < 20; i++)
				{
					ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
					WebRequest req = new PutMethodWebRequest(BASE_JCR_URL
							+ "dirlist/file" + i, bais, "UTF-8");
					req.setParameter("snoop", "1");
					req.setHeaderField("Content-Type", "text/html");
					req.setHeaderField("Content-Encoding", "UTF-8");
					WebResponse resp = wc.getResponse(req);
					int code = resp.getResponseCode();
					assertTrue("Should have been a 201 or 204 ", (code == 201)
							|| (code == 204));
				}
				WebRequest req = new GetMethodWebRequest(BASE_JCR_URL + "dirlist");
				WebResponse resp = wc.getResource(req);
				int code = resp.getResponseCode();
				assertTrue("Should have been a 200 ", (code == 200));
				int contentL = resp.getContentLength();
				log.info("Got " + contentL + " bytes ");
				DataInputStream in = new DataInputStream(resp.getInputStream());
				byte[] buffer2 = new byte[contentL];
				in.readFully(buffer2);
				String contentEncoding = resp.getCharacterSet();
				String contentType = resp.getContentType();
				log.info("Got ContentType:" + contentType + " ContentEncoding:"
						+ contentEncoding);
				String content = new String(buffer2, contentEncoding);
				log.info("Content\n" + content);
			}
			catch (HttpNotFoundException nfex)
			{
				fail("Failed with " + nfex.getResponseCode() + " Cause: "
						+ nfex.getResponseMessage());
			}
			catch (HttpInternalErrorException iex)
			{
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	public void testDeleteOneByOne() throws Exception
	{
		if (enabled)
		{
			testDirectory();
			try
			{
				login();

				for (int i = 0; i < 20; i++)
				{
					WebRequest req = new DeleteMethodWebRequest(BASE_JCR_URL
							+ "dirlist/file" + i);
					WebResponse resp = wc.getResponse(req);
					int code = resp.getResponseCode();
					assertEquals("Should have been a 204 ",204,code);
				}
				{
					WebRequest req = new DeleteMethodWebRequest(BASE_JCR_URL + "dirlist");
					WebResponse resp = wc.getResponse(req);
					int code = resp.getResponseCode();
					assertEquals("Should have been a 204 ", 204,code);
				}
				try
				{
					WebRequest req = new GetMethodWebRequest(BASE_JCR_URL + "dirlist");
					WebResponse resp = wc.getResource(req);
					int code = resp.getResponseCode();
					assertEquals("Should have been a 404 ", 404, code);
				}
				catch (HttpNotFoundException nfex)
				{
					assertEquals("Should have generated a 404", 404, nfex
							.getResponseCode());
				}

			}
			catch (HttpNotFoundException nfex)
			{
				log.error("Failed ",nfex);
				fail("Failed with " + nfex.getResponseCode() + " Cause: "
						+ nfex.getResponseMessage());
			}
			catch (HttpInternalErrorException iex)
			{
				log.error("Failed ",iex);
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	public void testDeleteAllAtOnce() throws Exception
	{
		if (enabled)
		{
			login();
			testDirectory();
			try
			{
				{
					WebRequest req = new DeleteMethodWebRequest(BASE_JCR_URL + "dirlist");
					WebResponse resp = wc.getResponse(req);
					int code = resp.getResponseCode();
					assertEquals("Should have been a 204 ", 204, resp.getResponseCode());
				}
				try
				{
					WebRequest req = new GetMethodWebRequest(BASE_JCR_URL + "dirlist");
					WebResponse resp = wc.getResource(req);
					
					assertEquals("Should have been a 404 ", 404, resp.getResponseCode());
				}
				catch (HttpNotFoundException nfex)
				{
					assertEquals("Should have generated a 404", 404, nfex
							.getResponseCode());
				}
			}
			catch (HttpNotFoundException nfex)
			{
				log.error("Failed ",nfex);
				fail("Failed with " + nfex.getResponseCode() + " Cause: "
						+ nfex.getResponseMessage());
			}
			catch (HttpInternalErrorException iex)
			{
				log.error("Failed ",iex);
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}
	
	public void testMultipartUpload() throws Exception {
		if (enabled)
		{
			login();
			try
			{
					PostMethodWebRequest mreq = new PostMethodWebRequest(BASE_JCR_URL + "dirlist");
					mreq.setMimeEncoded(true);
					for ( int i = 0; i < 20; i++ ) {
						ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
						mreq.setParameter("multifile"+i, new UploadFileSpec[] {
								new UploadFileSpec("OriginalFileName", bais, "text/html")
						});
					}
					WebResponse resp = wc.getResponse(mreq);
					int code = resp.getResponseCode();
					assertTrue("Should have been a 200 ", (code == 200));
					int contentL = resp.getContentLength();
					log.info("Got " + contentL + " bytes ");
					DataInputStream in = new DataInputStream(resp.getInputStream());
					byte[] buffer2 = new byte[contentL];
					in.readFully(buffer2);
					String contentEncoding = resp.getCharacterSet();
					String contentType = resp.getContentType();
					log.info("Got ContentType:" + contentType + " ContentEncoding:"
							+ contentEncoding);
					String content = new String(buffer2, contentEncoding);
					log.info("Content\n" + content);
					for ( int i = 0; i < 20; i++ ) {
						WebRequest req = new GetMethodWebRequest(BASE_JCR_URL + "dirlist/multifile"+i);
						log.info("Trying " + "dirlist/multifile"+i);
						resp = wc.getResponse(req);
						assertEquals("Expected a 200 response ",200, resp.getResponseCode());
						assertEquals("Content Lenght does not match  ",buffer.length, resp.getContentLength());
						assertEquals("Content Type not correct  ","text/html", resp.getContentType());
						contentL = resp.getContentLength();
						log.info("Got " + contentL + " bytes ");
						in = new DataInputStream(resp.getInputStream());
						buffer2 = new byte[contentL];
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
			catch (HttpNotFoundException nfex)
			{
				log.error("Failed ",nfex);
				fail("Failed with " + nfex.getResponseCode() + " Cause: "
						+ nfex.getResponseMessage());
			}
			catch (HttpInternalErrorException iex)
			{
				log.error("Failed ",iex);
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
		
	}

}
