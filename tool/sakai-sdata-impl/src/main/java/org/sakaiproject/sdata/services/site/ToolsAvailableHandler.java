package org.sakaiproject.sdata.services.site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.Kernel;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sdata.tool.json.JSONServiceHandler;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

public class ToolsAvailableHandler extends JSONServiceHandler
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
		// harvest inputs
		String siteId = request.getParameter("siteid");
		try
		{
			// get the requested site
			Site site = siteService.getSite(siteId);

			// set the category to the site type if site type isn't null
			HashSet<String> cats = null;
			if (site.getType() != null)
			{
				cats = new HashSet<String>();
				cats.add(site.getType());
			}

			// look up tools based on category (site type)
			Set<Tool> tools = toolManager.findTools(null, null);

			// create a list of maps that hold tool info
			ArrayList<HashMap<String, Object>> toolsOut = new ArrayList<HashMap<String, Object>>();
			for (Tool tool : tools)
			{
				// put certain tool attributes into the outbound map
				HashMap<String, Object> toolOut = new HashMap<String, Object>();
				toolOut.put("id", tool.getId());
				toolOut.put("title", tool.getTitle());
				toolOut.put("description", tool.getDescription());
				toolOut.put("iconclass", "icon-" + tool.getId().replaceAll("[.]", "-"));
				toolsOut.add(toolOut);
			}
			// add the tool list to a base element for access
			HashMap<String, Object> out = new HashMap<String, Object>();
			out.put("tools", toolsOut);

			// send the map to the client
			sendMap(request, response, out);
		}
		catch (IdUnusedException iue)
		{
			sendError(request, response, iue);
		}
	}
}
