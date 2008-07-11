package org.sakaiproject.sdata.services.site;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.Kernel;
import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.SDataFunction;
import org.sakaiproject.sdata.tool.functions.SDataFunctionUtil;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

public class SiteAddToolFunction implements SDataFunction
{
	private ToolManager toolManager;

	public SiteAddToolFunction() throws ServletException
	{
		toolManager = (ToolManager) Kernel.componentManager().get(ToolManager.class.getName());
	}

	public void call(Handler handler, HttpServletRequest request, HttpServletResponse response,
			Object target, ResourceDefinition rp) throws SDataException
	{
		SDataFunctionUtil.checkMethod(request.getMethod(), "POST");

		Site site = (Site) target;

		// harvest inputs
		String tools = request.getParameter("tools");
		String[] toolIds = tools.split(",");

		for (String toolId : toolIds)
		{
			// get the requested tool
			Tool tool = toolManager.getTool(toolId);

			// create a page with the same name as the tool and add the tool
			SitePage page = site.addPage();
			page.setTitle(tool.getTitle());
			page.addTool(tool);
		}
	}

	public void destroy()
	{
	}
}
