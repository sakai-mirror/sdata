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

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sdata.tool.functions.CHSHideReleaseFunction;

/**
 * @author ieb
 */
public class CHSHideReleaseFuntionUnitT extends BaseHandlerUnitT
{

	private static final String BASE_URL = "http://localhost:8080/sdata/";

	private static final String BASE_DATA_URL = BASE_URL + "c/private/sdata";

	private static final Log log = LogFactory.getLog(CHSHideReleaseFuntionUnitT.class);

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

	protected String getBaseDataUrl()
	{
		return BASE_DATA_URL;
	}

	public void testShowHide() throws Exception
	{
		if (enabled)
		{
			String testDocument = getTestDocument();
			hideDocument(testDocument, true);
			checkHide(testDocument, true);
			hideDocument(testDocument, false);
			checkHide(testDocument, false);
		}
	}

	/**
	 * @param testDocument
	 * @param b
	 * @throws IOException
	 * @throws HttpException
	 */
	private void checkHide(String testDocument, boolean b) throws HttpException,
			IOException
	{
		GetMethod validate = new GetMethod(testDocument + "?f=m");
		client.executeMethod(validate);
		int code = validate.getStatusCode();

		assertEquals("Validate failed " + validate.getStatusLine(), 200, code);

		log.info("Got Result " + validate.getResponseBodyAsString());

		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(validate
				.getResponseBodyAsString());
		assertEquals("Item was not hidden", b, jsonObject.getBoolean("hidden"));

	}

	/**
	 * @param testDocument
	 * @param b
	 * @throws IOException
	 * @throws HttpException
	 */
	private void hideDocument(String testDocument, boolean b) throws HttpException,
			IOException
	{
		PostMethod method = new PostMethod(testDocument);
		method.setParameter("f", "hr");
		method.setParameter(CHSHideReleaseFunction.HIDDEN, String.valueOf(b));
		client.executeMethod(method);
		int code = method.getStatusCode();

		assertEquals("Hide Method failed " + method.getStatusLine(), 200, code);

	}

	public void testRelease() throws Exception
	{
		if (enabled)
		{
			String testDocument = getTestDocument();
			long now = System.currentTimeMillis();
			hideDocument(testDocument, true);
			checkHide(testDocument, true);
			setRelease(testDocument, now);
			checkHide(testDocument, false);
			checkRelease(testDocument, now);

			hideDocument(testDocument, false);
			checkHide(testDocument, false);
		}

	}

	/**
	 * @param testDocument
	 * @param now
	 * @throws IOException
	 * @throws HttpException
	 */
	private void checkRelease(String testDocument, long now) throws HttpException,
			IOException
	{
		GetMethod validate = new GetMethod(testDocument + "?f=m");
		client.executeMethod(validate);
		int code = validate.getStatusCode();

		assertEquals("Validate failed " + validate.getStatusLine(), 200, code);

		log.info("Got Result " + validate.getResponseBodyAsString());

		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(validate
				.getResponseBodyAsString());
		assertEquals("Item was hidden", false, jsonObject.getBoolean("hidden"));
		assertEquals("Item was not released", now, jsonObject.getLong("releaseDate"));
		assertFalse("Item was not retract", jsonObject.containsKey("retractDate"));

	}

	/**
	 * @param testDocument
	 * @param now
	 * @throws IOException
	 * @throws HttpException
	 */
	private void setRelease(String testDocument, long now) throws HttpException,
			IOException
	{
		PostMethod method = new PostMethod(testDocument);
		method.setParameter("f", "hr");
		method.setParameter(CHSHideReleaseFunction.HIDDEN, String.valueOf(false));
		method.setParameter(CHSHideReleaseFunction.RELEASE_DATE, String.valueOf(now));
		method.setParameter(CHSHideReleaseFunction.RETRACT_DATE, "");

		client.executeMethod(method);
		int code = method.getStatusCode();

		assertEquals("Hide Method failed " + method.getStatusLine(), 200, code);

	}

	public void testRetract() throws Exception
	{
		if (enabled)
		{
			String testDocument = getTestDocument();
			long now = System.currentTimeMillis();
			hideDocument(testDocument, true);
			checkHide(testDocument, true);
			setRetract(testDocument, now);
			checkHide(testDocument, false);
			checkRetract(testDocument, now);

			hideDocument(testDocument, false);
			checkHide(testDocument, false);
		}

	}

	/**
	 * @param testDocument
	 * @param now
	 * @throws IOException
	 * @throws HttpException
	 */
	private void checkRetract(String testDocument, long now) throws HttpException,
			IOException
	{
		GetMethod validate = new GetMethod(testDocument + "?f=m");
		client.executeMethod(validate);
		int code = validate.getStatusCode();

		assertEquals("Validate failed " + validate.getStatusLine(), 200, code);

		log.info("Got Result " + validate.getResponseBodyAsString());

		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(validate
				.getResponseBodyAsString());
		assertEquals("Item was hidden", false, jsonObject.getBoolean("hidden"));
		assertEquals("Item was not retracted", now, jsonObject.getLong("retractDate"));
		assertFalse("Item was released", jsonObject.containsKey("releaseDate"));

	}

	/**
	 * @param testDocument
	 * @param now
	 * @throws IOException
	 * @throws HttpException
	 */
	private void setRetract(String testDocument, long now) throws HttpException,
			IOException
	{
		PostMethod method = new PostMethod(testDocument);
		method.setParameter("f", "hr");
		method.setParameter(CHSHideReleaseFunction.HIDDEN, String.valueOf(false));
		method.setParameter(CHSHideReleaseFunction.RELEASE_DATE, "");
		method.setParameter(CHSHideReleaseFunction.RETRACT_DATE, String.valueOf(now));

		client.executeMethod(method);
		int code = method.getStatusCode();

		assertEquals("Hide Method failed " + method.getStatusLine(), 200, code);
	}

	public void testReleaesAndRetract() throws Exception
	{
		if (enabled)
		{
			String testDocument = getTestDocument();
			long now = System.currentTimeMillis();
			hideDocument(testDocument, true);
			checkHide(testDocument, true);
			setReleaseAndRetract(testDocument, now, now + 100);
			checkHide(testDocument, false);
			checkReleaseAndRetract(testDocument, now, now + 100);

			hideDocument(testDocument, false);
			checkHide(testDocument, false);
		}

	}

	/**
	 * @param testDocument
	 * @param now
	 * @param l
	 * @throws IOException
	 * @throws HttpException
	 */
	private void checkReleaseAndRetract(String testDocument, long now, long then)
			throws HttpException, IOException
	{
		GetMethod validate = new GetMethod(testDocument + "?f=m");
		client.executeMethod(validate);
		int code = validate.getStatusCode();

		assertEquals("Validate failed " + validate.getStatusLine(), 200, code);

		log.info("Got Result " + validate.getResponseBodyAsString());

		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(validate
				.getResponseBodyAsString());
		assertEquals("Item was hidden", false, jsonObject.getBoolean("hidden"));
		assertEquals("Item was not releaseed", now, jsonObject.getLong("releaseDate"));
		assertEquals("Item was not retracted", then, jsonObject.getLong("retractDate"));
	}

	/**
	 * @param testDocument
	 * @param now
	 * @param l
	 * @throws IOException
	 * @throws HttpException
	 */
	private void setReleaseAndRetract(String testDocument, long now, long then)
			throws HttpException, IOException
	{
		PostMethod method = new PostMethod(testDocument);
		method.setParameter("f", "hr");
		method.setParameter(CHSHideReleaseFunction.HIDDEN, String.valueOf(false));
		method.setParameter(CHSHideReleaseFunction.RELEASE_DATE, String.valueOf(now));
		method.setParameter(CHSHideReleaseFunction.RETRACT_DATE, String.valueOf(then));

		client.executeMethod(method);
		int code = method.getStatusCode();

		assertEquals("Hide Method failed " + method.getStatusLine(), 200, code);
	}

}
