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
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpInternalErrorException;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletUnitClient;

/**
 * @author ieb
 */
public class HttpRangeUnitT extends TestCase
{

	private static final Log log = LogFactory.getLog(JsonJcrHandlerUnitT.class);

	private static final String LOGIN_BASE_URL = "http://localhost:8080/portal/relogin";

	private static final String USERNAME = "admin";

	private static final String PASSWORD = "admin";

	private static final String BASE_URL = "http://localhost:8080/sdata/";

	private static final String BASE_JCR_URL = BASE_URL + "f/";

	ServletUnitClient client = null;

	private WebConversation wc;

	private boolean enabled = true;

	private byte[] buffer;

	/**
	 * @param arg0
	 */
	public HttpRangeUnitT(String arg0)
	{
		super(arg0);
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
		assertEquals("Failed to Login", 200, resp.getResponseCode());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		try
		{
			wc = new WebConversation();
			WebRequest req = new GetMethodWebRequest(BASE_URL + "checkRunning");
			WebResponse resp = wc.getResponse(req);
			DataInputStream inputStream = new DataInputStream(resp.getInputStream());
			buffer = new byte[resp.getContentLength()];
			inputStream.readFully(buffer);
		}
		catch (HttpNotFoundException notfound)
		{
			enabled = false;
		}
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
	 * @param target
	 * @param size
	 * @return
	 */
	public byte[] uploadTestData(String target, final int size)
	{
		if (enabled)
		{
			try
			{
				login();

				byte[] content = new byte[size];
				Random r = new Random();
				r.nextBytes(content);
				ByteArrayInputStream bais = new ByteArrayInputStream(content);
				long start = System.currentTimeMillis();
				WebRequest req = new PutMethodWebRequest(target, bais, null);
				req.setHeaderField("Content-Type", "application/octet-stream");

				WebResponse resp = wc.getResponse(req);
				int code = resp.getResponseCode();
				assertTrue("Upload of data 201 or 204 ", (code == 201) || (code == 204));
				long t = System.currentTimeMillis() - start;
				long rate = (size * 1000) / (t + 1);
				log.info("Uplaoded at " + rate + " b/s in " + t + " ms");
				return content;
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
			catch (MalformedURLException e)
			{
				e.printStackTrace();
				fail("Failed with ");
			}
			catch (IOException e)
			{
				e.printStackTrace();
				fail("Failed with ");
			}
			catch (SAXException e)
			{
				e.printStackTrace();
				fail("Failed with ");
			}
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
		return null;
	}

	/**
	 * @throws IOException
	 */
	public void testRangeDownload() throws IOException
	{
		if (enabled)
		{
			String target = BASE_JCR_URL + "testRangeUplaod";
			byte[] content = uploadTestData(BASE_JCR_URL + "testRangeUplaod",
					2 * 1024 * 1024);
			try
			{
				WebRequest request = new GetMethodWebRequest(target);
				for (;;)
				{
					WebResponse response = wc.getResource(request);
					int code = response.getResponseCode();
					dumpHeaders(response);
					log.info("Code " + code);
					if (code == 206)
					{
						// partial response, get the range header and read the
						// content
						// for the range.
						int contentL = response.getContentLength();
						String rangeHeader = response.getHeaderField("content-range");
						assertNotNull("A 206 must respond with a range header ",
								rangeHeader);
						long[] range = parseRangeHeader(rangeHeader);
						log.info("Got " + contentL + " bytes from " + range[0] + " to "
								+ range[1] + " of " + range[2]);
						DataInputStream in = new DataInputStream(response
								.getInputStream());
						byte[] buffer2 = new byte[contentL];
						in.readFully(buffer2);
						for (int i = 0; i < buffer2.length; i++)
						{
							assertEquals("Content Does not Match at " + (range[0] + i),
									content[(int) range[0] + i], buffer2[i]);
						}
						range[1]++;
						if (range[1] == range[2])
						{
							break;
						}
						request = new GetMethodWebRequest(target);
						request.setHeaderField("range", "bytes="
								+ String.valueOf(range[1]) + "-");
					}
					else if (code == 200)
					{
						int contentL = response.getContentLength();
						log.info("Got Whole " + contentL + " bytes ");
						DataInputStream in = new DataInputStream(response
								.getInputStream());
						byte[] buffer2 = new byte[contentL];
						in.readFully(buffer2);
						for (int i = 0; i < buffer2.length; i++)
						{
							assertEquals("Content Does not Match at " + (i), content[i],
									buffer2[i]);
						}

						break;
					}
					else
					{
						fail("Unxepected Code " + response.getResponseCode() + " "
								+ response.getResponseMessage());
					}
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
	 * @param rangeHeader
	 * @return
	 */
	private long[] parseRangeHeader(String rangeHeader)
	{
		String[] parts = rangeHeader.split(" ");
		String[] rangesLength = parts[1].split("/");
		String[] startEnd = rangesLength[0].split("-");
		long[] ranges = new long[3];
		ranges[0] = Long.parseLong(startEnd[0]);
		ranges[1] = Long.parseLong(startEnd[1]);
		ranges[2] = Long.parseLong(rangesLength[1]);
		return ranges;
	}

	/**
	 * @param resp
	 */
	private void dumpHeaders(WebResponse resp)
	{
		StringBuilder sb = new StringBuilder();
		for (String headerName : resp.getHeaderFieldNames())
		{
			for (String header : resp.getHeaderFields(headerName))
			{
				sb.append("\n\t").append(headerName).append(": ").append(header);
			}
		}
		log.info("Headers " + sb.toString());
	}

}
