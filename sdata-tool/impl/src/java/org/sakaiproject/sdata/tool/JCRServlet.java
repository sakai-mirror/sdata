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

package org.sakaiproject.sdata.tool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.jcr.support.api.JCRNodeFactoryService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author ieb
 */
public class JCRServlet extends HttpServlet
{
	private static final Log log = LogFactory.getLog(JCRServlet.class);

	private static final String MACROS = "/WEB-INF/vm/macros.vm";

	private static final String BUNDLE_NAME = "org.sakaiproject.search.tool.bundle.Messages"; //$NON-NLS-1$

	private static final ResourceLoader rlb = new ResourceLoader(BUNDLE_NAME);

	/**
	 * Required for serialization... also to stop eclipse from giving me a
	 * warning!
	 */
	private static final long serialVersionUID = 676743152200357708L;

	public static final String SAVED_REQUEST_URL = "org.sakaiproject.search.api.last-request-url";

	private static final String PANEL = "panel";

	private static final Object TITLE_PANEL = "Title";

	private SessionManager sessionManager;

	private Map<String, String> contentTypes = new HashMap<String, String>();

	private Map<String, String> characterEncodings = new HashMap<String, String>();

	private VelocityEngine vengine;

	private String inlineMacros;

	private String basePath;

	private ComponentManager componentManager;

	private JCRNodeFactoryService jcrNodeFactory;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);

		ServletContext sc = servletConfig.getServletContext();
		
		componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();

		jcrNodeFactory = (JCRNodeFactoryService) componentManager.get(JCRNodeFactoryService.class.getName());
		
		inlineMacros = MACROS;
		try
		{
			vengine = new VelocityEngine();

			vengine.setApplicationAttribute(ServletContext.class.getName(), sc);

			Properties p = new Properties();
			p.load(this.getClass().getResourceAsStream("sdata.config"));
			vengine.init(p);
			vengine.getTemplate(inlineMacros);

		}
		catch (Exception ex)
		{
			throw new ServletException(ex);
		}
		contentTypes.put("opensearch", "application/opensearchdescription+xml");
		contentTypes.put("sakai.src", "application/opensearchdescription+xml" );
		contentTypes.put("rss20", "text/xml" );
	}

	protected void doGet(final HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		execute(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		execute(request, response);
	}

	public void execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		VelocityContext vc = new VelocityContext();

		String sakaiHeader = (String) request.getAttribute("sakai.html.head");
		String toolPlacement = (String) request.getAttribute("sakai.tool.placement.id");
		String toolPlacementJs = toolPlacement.toString().replace('-','x');
		String skin = "default/"; // this could be changed in the future to
		// make search skin awaire

		vc.put("skin", skin);
		vc.put("sakaiheader", sakaiHeader);
		vc.put("rlb",rlb);
		vc.put("sakai_tool_placement_id", toolPlacement);
		vc.put("sakai_tool_placement_id_js", toolPlacementJs);

			
			
			
			String path = request.getPathInfo();
			if (path == null || path.length() == 0)
			{
				path = "index";
			}
			if (path.startsWith("/"))
			{
				path = path.substring(1);
			}
		String template = path;
		log.debug("Path is "+template+" for "+request.getPathInfo());
		try
		{
			

			
			String filePath = "/WEB-INF/vm/" + template + ".vm";
			String contentType = contentTypes.get(template);
			if (contentType == null)
			{
				contentType = "text/html";
			}
			String characterEncoding = characterEncodings.get(template);
			if (characterEncoding == null)
			{
				characterEncoding = "UTF-8";
			}

			response.setContentType(contentType);
			response.setCharacterEncoding(characterEncoding);
			vengine.mergeTemplate(filePath, vc, response.getWriter());

			request.removeAttribute(Tool.NATIVE_URL);
		}
		catch (Exception e)
		{
			throw new ServletException("Search Failed ", e);
		}
	}

	public void addLocalHeaders(HttpServletRequest request)
	{
	}




}
