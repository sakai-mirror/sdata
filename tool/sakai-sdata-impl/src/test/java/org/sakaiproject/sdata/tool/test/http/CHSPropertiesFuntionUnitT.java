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
import org.sakaiproject.sdata.tool.functions.CHSPermissionsFunction;
import org.sakaiproject.sdata.tool.functions.CHSPropertiesFunction;

/**
 * @author ieb
 */
public class CHSPropertiesFuntionUnitT extends BaseHandlerUnitT
{

	private static final String BASE_URL = "http://localhost:8080/sdata/";

	private static final String BASE_DATA_URL = BASE_URL + "c/private/sdata/";

	private static final Log log = LogFactory.getLog(CHSPropertiesFuntionUnitT.class);

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
			String[] roles = { "access", "access", "maintain", "maintain",

			};
			String[] permissions = { "read", "write", "remove", "admin"

			};
			String[] set = { CHSPermissionsFunction.SETVALUE, CHSPermissionsFunction.SETVALUE,
					CHSPermissionsFunction.SETVALUE, CHSPermissionsFunction.SETVALUE };
			setPermissions(testDocument, roles, permissions, set);

			checkPermissions(testDocument, roles, permissions, set);
		}
	}

	public void testSetPropertiesCollection() throws Exception
	{
		if (enabled)
		{
			String testDocument = getBaseDataUrl();
			String[] roles = { "access", "access", "maintain", "maintain",

			};
			String[] permissions = { "read", "write", "remove", "admin"

			};
			String[] set = { CHSPermissionsFunction.SETVALUE, CHSPermissionsFunction.SETVALUE,
					CHSPermissionsFunction.SETVALUE, CHSPermissionsFunction.SETVALUE };
			setPermissions(testDocument, roles, permissions, set);

			checkPermissions(testDocument, roles, permissions, set);
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
	private void checkPermissions(String testDocument, String[] roles, String[] permissions,
			String[] sets) throws HttpException, IOException
	{
		log.info("Validating Document "+testDocument);
		GetMethod validate = new GetMethod(testDocument + "?f=pm");
		client.executeMethod(validate);
		int code = validate.getStatusCode();

		assertEquals("Validate failed " + validate.getStatusLine(), 200, code);

		log.info("Got Result " + validate.getResponseBodyAsString());
		
		


		String body = validate.getResponseBodyAsString();

		log.info("Got response " + body);

		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(body);
		Map<String, Boolean> permSet = new HashMap<String, Boolean>(); 
		for ( int i = 0; i < roles.length; i++ ) {
			String name = roles[i]+":"+permissions[i];
			permSet.put(name, CHSPermissionsFunction.SETVALUE.equals(sets[i]));
		}

		JSONObject rolesSet = jsonObject.getJSONObject("roles");
		for ( int i = 0; i < roles.length; i++ ) {
			String role = roles[i];
			String permission = permissions[i];
			String name = role+":"+permission;
			
			JSONObject roleObj = rolesSet.getJSONObject(role);
			assertNotNull("Didnt find role "+role,roleObj);
			String p = roleObj.getString(permission);
			assertNotNull("Didnt find permission "+permission+" "+role,p);
			assertEquals("Permission Not set correctly ",String.valueOf(permSet.get(name)).toLowerCase(),p.toLowerCase());
			log.info("Permission Set Ok "+name+" "+p);
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
	private void setPermissions(String testDocument, String[] roles, String[] permissions,
			String[] sets) throws HttpException, IOException
	{
		PostMethod method = new PostMethod(testDocument);
		method.setParameter("f", "pm");
		for (String role : roles)
		{
			method.addParameter(CHSPermissionsFunction.ROLE, role);
		}
		for (String permission : permissions)
		{
			method.addParameter(CHSPermissionsFunction.PERM, permission);
		}
		for (String set : sets)
		{
			method.addParameter(CHSPermissionsFunction.SET, set);
		}

		client.executeMethod(method);
		int code = method.getStatusCode();

		assertEquals("Permissions Method failed " + method.getStatusLine(), 200, code);
		
		log.info("Got setPermissions result as "+method.getResponseBodyAsString());
	}

}
