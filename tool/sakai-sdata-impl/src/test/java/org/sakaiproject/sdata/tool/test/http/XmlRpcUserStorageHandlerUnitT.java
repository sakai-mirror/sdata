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

/**
 * @author ieb
 */

public abstract class XmlRpcUserStorageHandlerUnitT extends TestCase
{
	private static final Log log = LogFactory.getLog(XmlRpcUserStorageHandlerUnitT.class);

	private static final String LOGIN_BASE_URL = "http://localhost:8080/portal/login";

	private static final String USERNAME = "admin";

	private static final String PASSWORD = "admin";

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
			WebRequest req = new GetMethodWebRequest(getBaseUrl() + "checkRunning");
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

	/**
	 * @return
	 */
	protected abstract String getBaseDataUrl();

	/**
	 * @return
	 */
	protected abstract String getBaseUrl();

	/**
	 * 
	 */
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

	/**
	 * @throws Exception
	 */
	public void testGet401() throws Exception
	{
		if (enabled)
		{
			WebResponse resp = null;
			try
			{
				WebRequest req = new GetMethodWebRequest(getBaseDataUrl() + "testpage");

				resp = wc.getResponse(req);
				checkHandler(resp);

				fail("Should have been a 401, got:" + resp.getResponseCode());
			}
			catch (HttpInternalErrorException iex)
			{
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
			catch (HttpException hex)
			{
				assertEquals("Should have been Unauthorized ", 401, hex.getResponseCode());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGet404() throws Exception
	{
		if (enabled)
		{
			WebResponse resp = null;
			try
			{
				login();
				WebRequest req = new GetMethodWebRequest(getBaseDataUrl() + "testpage");
				resp = wc.getResponse(req);
				checkHandler(resp);

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
			catch (HttpException hex)
			{
				fail("Authorization Failed");
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	/**
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 */
	private void login() throws MalformedURLException, IOException, SAXException
	{
		PostMethodWebRequest postMethod = new PostMethodWebRequest(LOGIN_BASE_URL);
		postMethod.setParameter("eid", USERNAME);
		postMethod.setParameter("pw", PASSWORD);
		postMethod.setParameter("submit", "Login");
		WebResponse resp = wc.getResponse(postMethod);
		assertNotNull(resp);
	}

	/**
	 * @throws Exception
	 */
	public void testUpload() throws Exception
	{
		if (enabled)
		{
			try
			{

				login();
				ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
				WebRequest req = new PutMethodWebRequest(getBaseDataUrl() + "putUpload",
						bais, "UTF-8");
				req.setParameter("snoop", "1");
				req.setHeaderField("Content-Type", "text/html");
				req.setHeaderField("Content-Encoding", "UTF-8");
				wc.setAuthorization(USERNAME, PASSWORD);
				WebResponse resp = wc.getResponse(req);
				checkHandler(resp);

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

	/**
	 * @throws Exception
	 */
	public void testUploadDownload() throws Exception
	{
		if (enabled)
		{
			try
			{
				login();
				ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
				WebRequest req = new PutMethodWebRequest(getBaseDataUrl() + "putUpload",
						bais, "UTF-8");
				req.setParameter("snoop", "1");
				req.setHeaderField("Content-Type", "text/html");
				req.setHeaderField("Content-Encoding", "UTF-8");
				WebResponse resp = wc.getResponse(req);
				checkHandler(resp);

				int code = resp.getResponseCode();
				assertTrue("Should have been a 201 or 204 ", (code == 201)
						|| (code == 204));

				req = new GetMethodWebRequest(getBaseDataUrl() + "putUpload");
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

	/**
	 * @throws Exception
	 */
	public void testUploadDownloadCache() throws Exception
	{
		if (enabled)
		{
			try
			{
				login();
				ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
				WebRequest req = new PutMethodWebRequest(getBaseDataUrl() + "putUpload",
						bais, "UTF-8");
				req.setParameter("snoop", "1");
				req.setHeaderField("Content-Type", "text/html");
				req.setHeaderField("Content-Encoding", "UTF-8");
				WebResponse resp = wc.getResponse(req);
				checkHandler(resp);

				int code = resp.getResponseCode();
				assertTrue("Should have been a 201 or 204 ", (code == 201)
						|| (code == 204));

				req = new GetMethodWebRequest(getBaseDataUrl() + "putUpload");
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

				String dateheader = resp.getHeaderField("last-modified");
				// Date date =
				// RFC1123Date.parseDate(resp.getHeaderField("date"));

				// now test the 304 response
				req = new GetMethodWebRequest(getBaseDataUrl() + "putUpload");
				req.setHeaderField("if-modified-since", dateheader);

				resp = wc.getResource(req);
				code = resp.getResponseCode();
				assertEquals("Should have been a 304 ", 304, code);

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

	/**
	 * @throws Exception
	 */
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
					WebRequest req = new PutMethodWebRequest(getBaseDataUrl()
							+ "dirlist/file" + i, bais, "UTF-8");
					req.setParameter("snoop", "1");
					req.setHeaderField("Content-Type", "text/html");
					req.setHeaderField("Content-Encoding", "UTF-8");
					WebResponse resp = wc.getResponse(req);
					checkHandler(resp);

					int code = resp.getResponseCode();
					assertTrue("Should have been a 201 or 204 ", (code == 201)
							|| (code == 204));
				}
				long start = System.currentTimeMillis();
				WebRequest req = new GetMethodWebRequest(getBaseDataUrl() + "dirlist");
				WebResponse resp = wc.getResource(req);
				int code = resp.getResponseCode();
				log.info("Dir Method took:" + (System.currentTimeMillis() - start));

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

	/**
	 * @throws Exception
	 */
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
					WebRequest req = new DeleteMethodWebRequest(getBaseDataUrl()
							+ "dirlist/file" + i);
					WebResponse resp = wc.getResponse(req);
					checkHandler(resp);

					int code = resp.getResponseCode();
					assertEquals("Should have been a 204 ", 204, code);
				}
				{
					WebRequest req = new DeleteMethodWebRequest(getBaseDataUrl()
							+ "dirlist");
					WebResponse resp = wc.getResponse(req);
					checkHandler(resp);

					int code = resp.getResponseCode();
					assertEquals("Should have been a 204 ", 204, code);
				}
				try
				{
					WebRequest req = new GetMethodWebRequest(getBaseDataUrl() + "dirlist");
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
				log.error("Failed ", nfex);
				fail("Failed with " + nfex.getResponseCode() + " Cause: "
						+ nfex.getResponseMessage());
			}
			catch (HttpInternalErrorException iex)
			{
				log.error("Failed ", iex);
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	/**
	 * @throws Exception
	 */
	public void testDeleteAllAtOnce() throws Exception
	{
		if (enabled)
		{
			login();
			testDirectory();
			try
			{
				{
					WebRequest req = new DeleteMethodWebRequest(getBaseDataUrl()
							+ "dirlist");
					WebResponse resp = wc.getResponse(req);
					checkHandler(resp);

					int code = resp.getResponseCode();
					assertEquals("Should have been a 204 ", 204, code);
				}
				try
				{
					WebRequest req = new GetMethodWebRequest(getBaseDataUrl() + "dirlist");
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
				log.error("Failed ", nfex);
				fail("Failed with " + nfex.getResponseCode() + " Cause: "
						+ nfex.getResponseMessage());
			}
			catch (HttpInternalErrorException iex)
			{
				log.error("Failed ", iex);
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	/**
	 * @throws Exception
	 */
	public void testMultipartUpload() throws Exception
	{
		if (enabled)
		{
			login();
			try
			{
				PostMethodWebRequest mreq = new PostMethodWebRequest(getBaseDataUrl()
						+ "dirlist");
				mreq.setMimeEncoded(true);
				for (int i = 0; i < 20; i++)
				{
					ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
					mreq.setParameter("multifile" + i,
							new UploadFileSpec[] { new UploadFileSpec("multifile" + i,
									bais, "text/html") });
				}
				WebResponse resp = wc.getResponse(mreq);
				checkHandler(resp);

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
				for (int i = 0; i < 20; i++)
				{
					WebRequest req = new GetMethodWebRequest(getBaseDataUrl()
							+ "dirlist/multifile" + i);
					log.info("Trying " + "dirlist/multifile" + i);
					resp = wc.getResponse(req);
					checkHandler(resp);

					assertEquals("Expected a 200 response ", 200, resp.getResponseCode());
					assertEquals("Content Lenght does not match  ", buffer.length, resp
							.getContentLength());
					assertEquals("Content Type not correct  ", "text/html", resp
							.getContentType());
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
				log.error("Failed ", nfex);
				fail("Failed with " + nfex.getResponseCode() + " Cause: "
						+ nfex.getResponseMessage());
			}
			catch (HttpInternalErrorException iex)
			{
				log.error("Failed ", iex);
				fail("Failed with " + iex.getResponseCode() + " Cause: "
						+ iex.getResponseMessage());
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
	private void checkHandler(WebResponse resp)
	{
		String className = this.getClass().getName();
		className = className.substring(className.lastIndexOf('.'));
		className = className.substring(0, className.length() - "UnitT".length());
		String handler = resp.getHeaderField("x-sdata-handler");
		assertNotNull("Handler Not found ", handler);
		assertTrue("Handler Not found (no value)", handler.trim().length() > 0);
		handler = handler.substring(handler.lastIndexOf('.'));
		assertEquals("Not the expected Handler Class", className, handler);
	}
}
