package org.sakaiproject.sdata.services.site;

import java.io.IOException;
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

public class SiteRemovePageHandler extends JSONServiceHandler
{
	private SiteService siteService;

	@Override
	public void init(Map<String, String> config) throws ServletException
	{
		siteService = Kernel.siteService();
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
		// harvest the inputs
		String siteId = request.getParameter("siteid");
		String pageId = request.getParameter("pageId");
		try
		{
			// get the site requested
			Site site = siteService.getSite(siteId);

			// get the page requested
			SitePage page = site.getPage(pageId);

			// remove the page and save the site
			site.removePage(page);
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
