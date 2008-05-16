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

import java.io.DataInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 */

public abstract class JsonHandlerUnitT extends BaseHandlerUnitT
{
	private static final Log log = LogFactory.getLog(JsonHandlerUnitT.class);

	/**
	 * @return
	 */
	protected abstract String getBaseDataUrl();

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
	public void testGet404() throws Exception
	{
		if (enabled)
		{
			login();
			GetMethod method = new GetMethod(getBaseDataUrl() + "testpage");
			client.executeMethod(method);
			// anything other than 404 or 403 is Ok.
			if (method.getStatusCode() != 404)
			{
				fail("Failed " + method.getStatusLine());
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
	public void testUpload() throws Exception
	{
		if (enabled)
		{
			login();
			PutMethod method = new PutMethod(getBaseDataUrl() + "putUpload");
			method.setRequestHeader("Content-Type", "text/html");
			method.setRequestHeader("Content-Encoding", "UTF-8");
			method.setRequestEntity(new ByteArrayRequestEntity(buffer, "text/html"));
			client.executeMethod(method);
			checkHandler(method);
			int code = method.getStatusCode();

			assertTrue("Should have been a 201 or 204, response was  "
					+ method.getStatusLine(), (code == 201) || (code == 204));
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
			login();
			PutMethod method = new PutMethod(getBaseDataUrl() + "putUpload");
			method.setRequestHeader("Content-Type", "text/html");
			method.setRequestHeader("Content-Encoding", "UTF-8");
			method.setRequestEntity(new ByteArrayRequestEntity(buffer, "text/html"));
			client.executeMethod(method);
			checkHandler(method);
			int code = method.getStatusCode();
			assertTrue("Should have been a 201 or 204 " + method.getStatusLine(),
					(code == 201) || (code == 204));

			GetMethod gmethod = new GetMethod(getBaseDataUrl() + "putUpload");
			client.executeMethod(gmethod);
			checkHandler(gmethod);
			code = gmethod.getStatusCode();
			assertTrue("Should have been a 201 or 204 " + method.getStatusLine(),
					(code == 200));
			int contentL = (int) gmethod.getResponseContentLength();
			log.info("Got " + contentL + " bytes ");
			DataInputStream in = new DataInputStream(gmethod.getResponseBodyAsStream());
			byte[] buffer2 = new byte[contentL];
			in.readFully(buffer2);
			assertEquals("Upload Size is not the same as download size ", buffer.length,
					buffer2.length);
			for (int i = 0; i < buffer2.length; i++)
			{
				assertEquals("Byte at Offset " + i + " corrupted ", buffer[i], buffer2[i]);
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
			login();
			PutMethod method = new PutMethod(getBaseDataUrl() + "putUpload");
			method.setRequestHeader("Content-Type", "text/html");
			method.setRequestHeader("Content-Encoding", "UTF-8");
			method.setRequestEntity(new ByteArrayRequestEntity(buffer, "text/html"));
			client.executeMethod(method);
			checkHandler(method);
			int code = method.getStatusCode();
			assertTrue("Should have been a 201 or 204 ", (code == 201) || (code == 204));

			GetMethod gmethod = new GetMethod(getBaseDataUrl() + "putUpload");
			client.executeMethod(gmethod);
			checkHandler(gmethod);

			dumpHeaders(gmethod);
			code = gmethod.getStatusCode();
			assertEquals("Should have been a 200 ", 200, code);
			int contentL = (int) gmethod.getResponseContentLength();
			log.info("Got " + contentL + " bytes ");
			DataInputStream in = new DataInputStream(gmethod.getResponseBodyAsStream());
			byte[] buffer2 = new byte[contentL];
			in.readFully(buffer2);
			assertEquals("Upload Size is not the same as download size ", buffer.length,
					buffer2.length);
			for (int i = 0; i < buffer2.length; i++)
			{
				assertEquals("Byte at Offset " + i + " corrupted ", buffer[i], buffer2[i]);
			}

			String dateheader = gmethod.getResponseHeader("last-modified").getValue();
			// Date date =
			// RFC1123Date.parseDate(resp.getHeaderField("date"));

			// now test the 304 response
			gmethod = new GetMethod(getBaseDataUrl() + "putUpload");
			gmethod.setRequestHeader("if-modified-since", dateheader);
			client.executeMethod(gmethod);
			checkHandler(gmethod);

			code = gmethod.getStatusCode();
			dumpHeaders(gmethod);
			assertEquals("Should have been a 304 ", 304, code);

		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	/**
	 * @param resp
	 */
	private void dumpHeaders(HttpMethod resp)
	{
		StringBuilder sb = new StringBuilder();
		for (Header header : resp.getResponseHeaders())
		{
			for (HeaderElement h : header.getElements())
			{
				sb.append("\n\t").append(h.getName()).append(": ").append(h.getValue());
			}
		}
		log.info("Headers " + sb.toString());
	}

	/**
	 * @throws Exception
	 */
	public void testDirectory() throws Exception
	{
		if (enabled)
		{
			login();
			for (int i = 0; i < 20; i++)
			{
				PutMethod method = new PutMethod(getBaseDataUrl() + "dirlist/file" + i);
				method.setRequestHeader("Content-Type", "text/html");
				method.setRequestHeader("Content-Encoding", "UTF-8");
				method.setRequestEntity(new ByteArrayRequestEntity(buffer, "text/html"));
				client.executeMethod(method);
				checkHandler(method);
				int code = method.getStatusCode();
				assertTrue("Should have been a 201 or 204 ", (code == 201)
						|| (code == 204));

			}
			long start = System.currentTimeMillis();
			GetMethod gmethod = new GetMethod(getBaseDataUrl() + "dirlist");
			client.executeMethod(gmethod);
			checkHandler(gmethod);

			int code = gmethod.getStatusCode();
			log.info("Dir Method took:" + (System.currentTimeMillis() - start));

			assertEquals("Should have been a 200 ", 200, code);
			int contentL = (int) gmethod.getResponseContentLength();
			log.info("Got " + contentL + " bytes ");
			DataInputStream in = new DataInputStream(gmethod.getResponseBodyAsStream());
			byte[] buffer2 = new byte[contentL];
			in.readFully(buffer2);
			String contentEncoding = gmethod.getResponseCharSet();
			String content = new String(buffer2, contentEncoding);
			log.info("Content\n" + content);
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
			login();
			testDirectory();
			for (int i = 0; i < 20; i++)
			{
				DeleteMethod method = new DeleteMethod(getBaseDataUrl() + "dirlist/file"
						+ i);
				client.executeMethod(method);
				checkHandler(method);

				int code = method.getStatusCode();
				assertEquals("Should have been a 204 ", 204, code);
			}
			{
				DeleteMethod method = new DeleteMethod(getBaseDataUrl() + "dirlist");
				client.executeMethod(method);
				checkHandler(method);

				int code = method.getStatusCode();
				assertEquals("Should have been a 204 ", 204, code);
			}
			GetMethod method = new GetMethod(getBaseDataUrl() + "dirlist");
			client.executeMethod(method);
			checkHandler(method);

			int code = method.getStatusCode();
			assertEquals("Should have been a 404 ", 404, code);

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
			{
				DeleteMethod method = new DeleteMethod(getBaseDataUrl() + "dirlist");
				client.executeMethod(method);
				checkHandler(method);

				int code = method.getStatusCode();
				assertEquals("Should have been a 204 ", 204, code);
			}
			GetMethod method = new GetMethod(getBaseDataUrl() + "dirlist");
			client.executeMethod(method);
			checkHandler(method);

			int code = method.getStatusCode();
			assertEquals("Should have been a 404 ", 404, code);
		}
		else
		{
			log.info("Tests Disabled, please start tomcat with sdata installed");
		}
	}

	public void testMultipartUpload() throws Exception
	{
		if (enabled)
		{
			login();

			PostMethod method = new PostMethod(getBaseDataUrl() + "dirlist");

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
			log.info("Got " + method.getURI() + " " + method.getResponseBodyAsString());
			checkHandler(method);

			int code = method.getStatusCode();

			assertTrue("Should have been a 200 ", (code == 200));

			String content = method.getResponseBodyAsString();
			log.info("Content\n" + content);

			for (int i = 0; i < 20; i++)
			{
				GetMethod gmethod = new GetMethod(getBaseDataUrl() + "dirlist/multifile"
						+ i);
				log.info("Trying " + "dirlist/multifile" + i);
				client.executeMethod(gmethod);
				checkHandler(gmethod);

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

}
