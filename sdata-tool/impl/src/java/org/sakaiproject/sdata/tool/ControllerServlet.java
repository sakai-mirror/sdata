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

package org.sakaiproject.sdata.tool;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.tool.api.Tool;

/**
 * Loads
 * 
 * @author ieb
 */
public class ControllerServlet extends HttpServlet
{

	private static final Log log = LogFactory.getLog(ControllerServlet.class);

	private Map<String, Handler> handlerRegister;

	private Handler nullHandler = new Handler()
	{

		private Random r = new Random(System.currentTimeMillis());

		public void doDelete(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException
		{
		}

		public void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException
		{
			int size = 1024;
			try
			{
				size = Integer.parseInt(request.getHeader("x-testdata-size"));
			}
			catch (Exception ex)
			{

			}
			byte[] b = new byte[size];
			r.nextBytes(b);
			response.setContentType("application/octet-stream");
			response.setContentLength(b.length);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getOutputStream().write(b);
		}

		public void doHead(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException
		{
		}

		public void doPost(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException
		{
		}

		public void doPut(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException
		{
		}

		public void init(Map<String, String> config) throws ServletException
		{
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sakaiproject.sdata.tool.api.Handler#setHandlerHeaders(javax.servlet.http.HttpServletResponse)
		 */
		public void setHandlerHeaders(HttpServletResponse response)
		{
			response.setHeader("x-sdata-handler", this.getClass().getName());
		}

	};

	/**
	 * 
	 */
	public ControllerServlet()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		String handlerName = null;
		try
		{
			super.init(config);
			handlerRegister = new HashMap<String, Handler>();

			String configData = config.getInitParameter("config");
			Map<String, Map<String, String>> configMap = null;
			if (configData != null && configData.trim().length() > 0)
			{
				configMap = loadConfigMap(configData);
			}
			else
			{
				configMap = loadConfigMap(config);
			}
			for (String handler : configMap.keySet())
			{
				handlerName = handler;
				Map<String, String> handlerConfig = configMap.get(handler);
				Class c = Class.forName(handlerConfig.get("classname"));
				Handler h = (Handler) c.newInstance();
				h.init(handlerConfig);
				handlerRegister.put(handlerConfig.get("baseurl"), h);
			}
		}
		catch (ClassNotFoundException e)
		{
			throw new ServletException("Failed to instance handler " + handlerName, e);
		}
		catch (InstantiationException e)
		{
			throw new ServletException("Failed to instance handler " + handlerName, e);
		}
		catch (IllegalAccessException e)
		{
			throw new ServletException("Failed to instance handler " + handlerName, e);
		}
	}

	/**
	 * @param config
	 * @return
	 */

	private Map<String, Map<String, String>> loadConfigMap(ServletConfig config)
	{

		Map<String, Map<String, String>> configMap = new HashMap<String, Map<String, String>>();
		for (Enumeration<String> e = config.getInitParameterNames(); e.hasMoreElements();)
		{
			String name = e.nextElement();
			if (name.startsWith("handler."))
			{
				int handlerLength = "handler.".length();
				int endkey = name.indexOf(".", handlerLength + 1);

				String key = name.substring(handlerLength, endkey);
				String valuekey = name.substring(endkey + 1);
				String value = config.getInitParameter(name);
				if (log.isDebugEnabled())
				{
					log.debug("Adding Key[" + key + "] [" + handlerLength + "-" + endkey
							+ "] keyValue[" + valuekey + "] Value[" + value + "]");
				}
				Map<String, String> handlerConfig = configMap.get(key);
				if (handlerConfig == null)
				{
					handlerConfig = new HashMap<String, String>();
					configMap.put(key, handlerConfig);
				}
				handlerConfig.put(valuekey, value);
			}
		}
		return configMap;
	}

	private Map<String, Map<String, String>> loadConfigMap(String config)
	{

		Map<String, Map<String, String>> configMap = new HashMap<String, Map<String, String>>();
		String[] pairs = config.trim().split(";");
		for (String pair : pairs)
		{
			String[] nv = pair.trim().split("=", 2);
			String name = nv[0].trim();
			String value = nv[1].trim();
			int endkey = name.indexOf(".");

			String key = name.substring(0, endkey);
			String valuekey = name.substring(endkey + 1);
			if (log.isDebugEnabled())
			{
				log.debug("Config Data Adding Key[" + key + "] [" + endkey + "] keyValue["
						+ valuekey + "] Value[" + value + "]");
			}
			Map<String, String> handlerConfig = configMap.get(key);
			if (handlerConfig == null)
			{
				handlerConfig = new HashMap<String, String>();
				configMap.put(key, handlerConfig);
			}
			handlerConfig.put(valuekey, value);
		}
		return configMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		Handler h = getHandler(request);
		if (h != null)
		{
			h.setHandlerHeaders(response);
			h.doDelete(request, response);
		}
		else
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No Handler Found");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		Handler h = getHandler(request);
		if (h != null)
		{
			h.setHandlerHeaders(response);
			h.doGet(request, response);
		}
		else
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No Handler Found");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doHead(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		Handler h = getHandler(request);
		if (h != null)
		{
			h.setHandlerHeaders(response);
			h.doHead(request, response);
		}
		else
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No Handler Found");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		Handler h = getHandler(request);
		if (h != null)
		{
			h.setHandlerHeaders(response);
			h.doPost(request, response);
		}
		else
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No Handler Found");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		Handler h = getHandler(request);
		if (h != null)
		{
			h.setHandlerHeaders(response);
			h.doPut(request, response);
		}
		else
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "No Handler Found");
		}
	}

	/**
	 * @param request
	 * @return
	 */
	public Handler getHandler(HttpServletRequest request)
	{
		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
		String pathInfo = request.getPathInfo();
		if ( log.isDebugEnabled() ) {
			log.debug("Path is " + pathInfo);
		}
		if ("/checkRunning".equals(pathInfo))
		{
			return nullHandler;
		}
		if (pathInfo == null) return null;

		char[] path = request.getPathInfo().trim().toCharArray();
		if (path.length < 1) return null;
		int start = 0;
		if (path[0] == '/')
		{
			start = 1;
		}
		int end = start;
		for (; end < path.length && path[end] != '/'; end++);
		String key = new String(path, start, end - start);
		return handlerRegister.get(key);
	}

}
