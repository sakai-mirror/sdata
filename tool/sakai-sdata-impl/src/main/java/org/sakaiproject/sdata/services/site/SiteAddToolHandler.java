package org.sakaiproject.sdata.services.site;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.Kernel;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.SDataFunction;
import org.sakaiproject.sdata.tool.functions.SDataFunctionUtil;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

public class SiteAddToolHandler implements SDataFunction
{
	private SiteService siteService;
	private ToolManager toolManager;

	public SiteAddToolHandler() throws ServletException
	{
		siteService = Kernel.siteService();
		toolManager = (ToolManager) Kernel.componentManager().get(ToolManager.class.getName());
	}

	public void call(Handler handler, HttpServletRequest request, HttpServletResponse response,
			Object target, ResourceDefinition rp) throws SDataException
	{
		SDataFunctionUtil.checkMethod(request.getMethod(), "POST");
		// harvest inputs
		String siteId = request.getParameter("siteid");
		String tools = request.getParameter("tools");
		String[] toolIds = tools.split(",");

		try
		{
			// get the site to work with
			Site site = siteService.getSite(siteId);

			for (String toolId : toolIds)
			{
				// get the requested tool
				Tool tool = toolManager.getTool(toolId);

				// create a page with the same name as the tool and add the tool
				SitePage page = site.addPage();
				page.setTitle(tool.getTitle());
				page.addTool(tool);
			}

			// save the new page
			siteService.save(site);
		}
		catch (IdUnusedException iue)
		{
			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, iue.getMessage());
		}
		catch (PermissionException pe)
		{
			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, pe.getMessage());
		}
	}

	public void destroy()
	{
	}
}
