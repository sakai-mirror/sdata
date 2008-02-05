/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/sdata-tool/impl/src/java/org/sakaiproject/sdata/tool/StreamRequestFilter.java $
 * $Id: StreamRequestFilter.java 45207 2008-02-01 19:01:06Z ian@caret.cam.ac.uk $
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

package org.sakaiproject.sdata.tool;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;

public abstract class ServiceServlet extends HttpServlet
{

	private ServiceDefinitionFactory serviceDefinitionFactory = null;

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		serviceDefinitionFactory = getServiceDefinitionFactory(config);
	}

	/**
	 * @return
	 * @throws ServletException
	 */
	protected abstract ServiceDefinitionFactory getServiceDefinitionFactory()
			throws ServletException;

	/**
	 * @param config
	 * @return
	 * @throws ServletException
	 */
	@SuppressWarnings("unchecked")
	protected ServiceDefinitionFactory getServiceDefinitionFactory(ServletConfig config)
			throws ServletException
	{
		try
		{
			String factoryName = config.getInitParameter("factory-name");

			Class<ServiceDefinitionFactory> c = (Class<ServiceDefinitionFactory>) this
					.getClass().getClassLoader().loadClass(factoryName);
			ServiceDefinitionFactory o = c.newInstance();
			return o;
		}
		catch (InstantiationException e)
		{
			throw new ServletException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new ServletException(e);
		}
		catch (ClassNotFoundException e)
		{
			throw new ServletException(e);
		}
	}

	/**
	 * Respond to an HTTP GET request.
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		try
		{
			ServiceDefinition serviceDefinition = serviceDefinitionFactory.getSpec(
					request, response);

			Map<String, Object> responseMap = serviceDefinition.getResponseMap();

			sendMap(request, response, responseMap);
		}
		catch (Exception ex)
		{
			sendError(request, response, ex);
		}
	}

	/**
	 * @param ex
	 * @throws IOException
	 */
	protected abstract void sendError(HttpServletRequest request,
			HttpServletResponse response, Throwable ex) throws IOException;

	/**
	 * Serailize a Map strucutre to the output stream
	 * 
	 * @param uploads
	 * @throws IOException
	 */
	protected abstract void sendMap(HttpServletRequest request,
			HttpServletResponse response, Map<String, Object> contetMap)
			throws IOException;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// process(request,response);
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

}
