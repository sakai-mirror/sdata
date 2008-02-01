package org.sakaiproject.sdata.services.mcp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

public class MyCoursesAndProjectsServiceDefinitionFactory implements
		ServiceDefinitionFactory
{

	private SessionManager sessionManager;

	private SiteService siteService;

	private ComponentManager componentManager;

	public MyCoursesAndProjectsServiceDefinitionFactory()
	{
		componentManager = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		siteService = (SiteService) componentManager.get(SiteService.class.getName());
		sessionManager = (SessionManager) componentManager.get(SessionManager.class
				.getName());
	}

	public ServiceDefinition getSpec(HttpServletRequest request,
			HttpServletResponse response)
	{
		return new MyCoursesAndProjectsBean(sessionManager, siteService);
	}

}
