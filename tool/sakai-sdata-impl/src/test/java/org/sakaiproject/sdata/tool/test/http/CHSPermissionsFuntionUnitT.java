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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sdata.tool.functions.CHSPropertiesFunction;

/**
 * @author ieb
 */
public class CHSPermissionsFuntionUnitT extends BaseHandlerUnitT
{

	private static final String BASE_URL = "http://localhost:8080/sdata/";

	private static final String BASE_DATA_URL = BASE_URL + "c/private/sdata";

	private static final Log log = LogFactory.getLog(CHSPermissionsFuntionUnitT.class);

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

	public void testSetProperties1() throws Exception
	{
		if (enabled)
		{
			String testDocument = getTestDocument();
			String[] names = { "xxx:yyyy", "xxx:yyyy", "xxx:yyyy", "xxx:yyyy",

			};
			String[] values = { "1", "2", "3", "4"

			};
			String[] actions = { CHSPropertiesFunction.ADD, CHSPropertiesFunction.ADD,
					CHSPropertiesFunction.ADD, CHSPropertiesFunction.ADD };
			setProperties(testDocument, names, values, actions);

			checkProperties(testDocument, names, values, actions);
		}
	}

	public void testSetProperties2() throws Exception
	{
		if (enabled)
		{
			String testDocument = getTestDocument();
			String[] names = { "1xxx:yyyy", "2xxx:yyyy", "3xxx:yyyy", "4xxx:yyyy",

			};
			String[] values = { "1", "2", "3", "4"

			};
			String[] actions = { CHSPropertiesFunction.ADD, CHSPropertiesFunction.ADD,
					CHSPropertiesFunction.ADD, CHSPropertiesFunction.ADD };
			setProperties(testDocument, names, values, actions);

			checkProperties(testDocument, names, values, actions);
		}
	}

	public void testSetProperties3() throws Exception
	{
		if (enabled)
		{
			String testDocument = getTestDocument();
			String[] names = { "xxx:yyyy", "xxx:yyyy", "xxx:yyyy", "xxx:yyyy",

			};
			String[] values = { "1", "2", "3", "4"

			};
			String[] actions = { CHSPropertiesFunction.REMOVE, CHSPropertiesFunction.ADD,
					CHSPropertiesFunction.REPLACE, CHSPropertiesFunction.ADD };
			setProperties(testDocument, names, values, actions);

			checkProperties(testDocument, names, values, actions);
		}
	}

	public void testSetProperties4() throws Exception
	{
		if (enabled)
		{
			String testDocument = getTestDocument();
			String[] names = { "1xxx:yyyy", "xxx:yyyy", "1xxx:yyyy", "xxx:yyyy",

			};
			String[] values = { "1", "2", "3", "4"

			};
			String[] actions = { CHSPropertiesFunction.REMOVE, CHSPropertiesFunction.ADD,
					CHSPropertiesFunction.REPLACE, CHSPropertiesFunction.ADD };
			setProperties(testDocument, names, values, actions);

			checkProperties(testDocument, names, values, actions);
		}
	}

	/**
	 * @param testDocument
	 * @param names
	 * @param values
	 * @param actions
	 * @throws IOException
	 * @throws HttpException
	 */
	private void checkProperties(String testDocument, String[] names, String[] values,
			String[] actions) throws HttpException, IOException
	{
		GetMethod validate = new GetMethod(testDocument + "?f=m");
		client.executeMethod(validate);
		int code = validate.getStatusCode();

		assertEquals("Validate failed " + validate.getStatusLine(), 200, code);

		log.info("Got Result " + validate.getResponseBodyAsString());

		Map<String, List<String>> properties = new HashMap<String, List<String>>();
		for (int i = 0; i < names.length; i++)
		{
			if (CHSPropertiesFunction.ADD.equals(actions[i]))
			{
				List<String> o = properties.get(names[i]);
				if (o == null)
				{
					o = new ArrayList<String>();
					properties.put(names[i], o);
				}
				o.add(values[i]);
			}
			else if (CHSPropertiesFunction.REMOVE.equals(actions[i]))
			{
				properties.remove(names[i]);
			}
			else if (CHSPropertiesFunction.REPLACE.equals(actions[i]))
			{
				List<String> o = new ArrayList<String>();
				o.add(values[i]);
				properties.put(names[i], o);
			}
		}

		String body = validate.getResponseBodyAsString();

		log.info("Got response " + body);

		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(body);

		JSONObject jsonProperties = jsonObject.getJSONObject("properties");
		for (String key : properties.keySet())
		{
			List<String> a = properties.get(key);
			if (a.size() == 1)
			{
				assertEquals("Property Not equals " + key, a.get(0), jsonProperties
						.getString(key));
			}
			else
			{
				JSONArray pa = jsonProperties.getJSONArray(key);
				assertNotNull(key + " Should not be null", pa);
				assertTrue(key + " Should have been an array " + pa, pa.isArray());
				assertEquals("Array Size of " + key + " does not match ", a.size(), pa
						.size());
				for (int i = 0; i < a.size(); i++)
				{
					assertEquals("Mismatch in Array ", a.get(i), pa.get(i));
				}
			}
		}

	}

	/**
	 * @param testDocument
	 * @param names
	 * @param values
	 * @param actions
	 * @throws IOException
	 * @throws HttpException
	 */
	private void setProperties(String testDocument, String[] names, String[] values,
			String[] actions) throws HttpException, IOException
	{
		PostMethod method = new PostMethod(testDocument);
		method.setParameter("f", "pr");
		for (String name : names)
		{
			method.addParameter(CHSPropertiesFunction.NAME, name);
		}
		for (String value : values)
		{
			method.addParameter(CHSPropertiesFunction.VALUE, value);
		}
		for (String action : actions)
		{
			method.addParameter(CHSPropertiesFunction.ACTION, action);
		}

		client.executeMethod(method);
		int code = method.getStatusCode();

		assertEquals("Hide Method failed " + method.getStatusLine(), 200, code);
	}

}
