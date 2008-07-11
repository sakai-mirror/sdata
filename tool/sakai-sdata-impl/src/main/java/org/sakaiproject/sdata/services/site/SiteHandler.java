/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/sdata-tool/impl/src/java/org/sakaiproject/sdata/tool/JCRDumper.java $
 * $Id: JCRDumper.java 45207 2008-02-01 19:01:06Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
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

package org.sakaiproject.sdata.services.site;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.sdata.tool.ResourceFunctionFactoryImpl;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.ResourceDefinitionFactory;
import org.sakaiproject.sdata.tool.api.ResourceFunctionFactory;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.SDataFunction;
import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.sakaiproject.sdata.tool.json.JSONServiceHandler;
import org.sakaiproject.sdata.tool.util.ResourceDefinitionFactoryImpl;

/**
 * Handles calls for site related data
 */
public class SiteHandler extends JSONServiceHandler
{
	private static final long serialVersionUID = 1L;

	private static final String BASE_PATH_INIT = "basepath";
	private static final String BASE_URL_INIT = "baseurl";

	private static final String DEFAULT_BASE_PATH = "/sakai/sdata";
	private static final String DEFAULT_BASE_URL = "site";

	private String basePath;
	private String baseUrl;

	private ResourceDefinitionFactory resourceDefinitionFactory;
	private ResourceFunctionFactory resourceFunctionFactory;

	@Override
	public void init(Map<String, String> config) throws ServletException
	{
		super.init(config);
		basePath = config.get(BASE_PATH_INIT);
		if (basePath == null)
		{
			this.basePath = DEFAULT_BASE_PATH;
		}

		baseUrl = config.get(BASE_URL_INIT);
		if (baseUrl == null)
		{
			this.baseUrl = DEFAULT_BASE_URL;
		}
		resourceDefinitionFactory = getResourceDefinitionFactory(config);
		resourceFunctionFactory = getResourceFunctionFactory(config);
	}

	public void destroy()
	{
		resourceDefinitionFactory.destroy();
		resourceFunctionFactory.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.json.JSONServiceServlet#getServiceDefinitionFactory()
	 */
	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory() throws ServletException
	{
		return new SiteServiceDefinitionFactory();
	}

	/**
	 * Creates a resource definition factory suitable for controlling the storage of items
	 * 
	 * @param config
	 * @return
	 */
	protected ResourceDefinitionFactory getResourceDefinitionFactory(Map<String, String> config)
	{
		return new ResourceDefinitionFactoryImpl(config, baseUrl, basePath);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		try
		{
			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
			SDataFunction m = resourceFunctionFactory.getFunction(rp.getFunctionDefinition());
			if (m != null)
				m.call(this, request, response, null, rp);
			else
				super.doGet(request, response);
		}
		catch (SDataException sde)
		{
			sendError(request, response, sde);
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// TODO Auto-generated method stub
		super.doPost(request, response);
	}

	/**
	 * @param config
	 * @return
	 */
	private ResourceFunctionFactory getResourceFunctionFactory(Map<String, String> config)
	{
		return new ResourceFunctionFactoryImpl(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sakaiproject.sdata.tool.ServiceServlet#getServiceDefinitionFactory(javax.servlet.
	 * ServletConfig)
	 */
	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory(Map<String, String> config)
			throws ServletException
	{
		return new SiteServiceDefinitionFactory();
	}

}
