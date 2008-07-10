package org.sakaiproject.sdata.services.site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.Kernel;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sdata.tool.json.JSONServiceHandler;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

public class SiteAddToolHandler extends JSONServiceHandler
{
	private SiteService siteService;
	private ToolManager toolManager;

	@Override
	public void init(Map<String, String> config) throws ServletException
	{
		siteService = Kernel.siteService();
		toolManager = (ToolManager) Kernel.componentManager().get(ToolManager.class.getName());
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		response.reset();
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		// harvest inputs
		String siteId = request.getParameter("siteid");
		String ati = request.getParameter("addToolIds");
		String rti = request.getParameter("removeToolIds");
		String[] addToolIds = ati.split(",");
		String[] removeToolIds = rti.split(",");

		try
		{
			// get the site to work with
			Site site = siteService.getSite(siteId);

			for (String toolId : addToolIds)
			{
				// get the requested tool
				Tool tool = toolManager.getTool(toolId);

				// create a page with the same name as the tool and add the tool
				SitePage page = site.addPage();
				page.setTitle(tool.getTitle());
				page.addTool(tool);
			}

			for (String toolId : removeToolIds)
			{
				// site.getPage();
			}

			// save the new page
			siteService.save(site);
		}
		catch (IdUnusedException iue)
		{
			sendError(request, response, iue);
		}
		catch (PermissionException pe)
		{
			sendError(request, response, pe);
		}
	}
}
