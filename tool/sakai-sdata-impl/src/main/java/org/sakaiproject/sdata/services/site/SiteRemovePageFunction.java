package org.sakaiproject.sdata.services.site;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.SDataFunction;
import org.sakaiproject.sdata.tool.functions.SDataFunctionUtil;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;

public class SiteRemovePageFunction implements SDataFunction
{
	public void call(Handler handler, HttpServletRequest request, HttpServletResponse response,
			Object target, ResourceDefinition rp) throws SDataException
	{
		SDataFunctionUtil.checkMethod(request.getMethod(), "POST");

		Site site = (Site) target;
		// harvest the inputs
		String pageId = request.getParameter("pageId");
		// get the page requested
		SitePage page = site.getPage(pageId);

		// remove the page and save the site
		site.removePage(page);
	}

	public void destroy()
	{
	}
}
