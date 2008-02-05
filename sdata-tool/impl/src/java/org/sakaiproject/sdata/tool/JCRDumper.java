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

import javax.jcr.RepositoryException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.jcr.api.JCRService;
import org.sakaiproject.tool.api.Tool;

/**
 * @author ieb
 */
public class JCRDumper extends HttpServlet
{

	private ComponentManager componentManager;

	private JCRService jcrService;

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);

		ServletContext sc = servletConfig.getServletContext();

		componentManager = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		jcrService = (JCRService) componentManager.get(JCRService.class.getName());

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
		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
		String path = request.getPathInfo();
		if (path == null || path.length() == 0)
		{
			path = "/";
		}

		response.setContentType("text/xml");
		try
		{
			if (path.startsWith("/sys"))
			{
				path = path.substring("/sys".length());
				jcrService.getSession().exportSystemView(path,
						response.getOutputStream(), true, false);
			}
			else
			{
				jcrService.getSession().exportDocumentView(path,
						response.getOutputStream(), true, false);

			}
		}
		catch (RepositoryException e)
		{
			log("Failed", e);
		}
	}
}
