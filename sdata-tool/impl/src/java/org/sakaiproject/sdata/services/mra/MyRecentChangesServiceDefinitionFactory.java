package org.sakaiproject.sdata.services.mra;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

public class MyRecentChangesServiceDefinitionFactory implements ServiceDefinitionFactory
{

	private SessionManager sessionManager;

	private SiteService siteService;

	private ComponentManager componentManager;

	private SqlService sqlService;

	private ContentHostingService contentHostingService;

	private AnnouncementService announcementService;

	private EntityManager entityManager;

	private SearchService searchService;

	public MyRecentChangesServiceDefinitionFactory()
	{
		componentManager = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		searchService = (SearchService) componentManager
				.get("org.sakaiproject.search.api.SearchService");
		contentHostingService = (ContentHostingService) componentManager
				.get("org.sakaiproject.content.api.ContentHostingService");
		announcementService = (AnnouncementService) componentManager
				.get("org.sakaiproject.announcement.api.AnnouncementService");
		entityManager = (EntityManager) componentManager
				.get("org.sakaiproject.entity.api.EntityManager");

		siteService = (SiteService) componentManager.get(SiteService.class.getName());
		sessionManager = (SessionManager) componentManager.get(SessionManager.class
				.getName());
		sqlService = (SqlService) componentManager.get(SqlService.class.getName());
	}

	public ServiceDefinition getSpec(HttpServletRequest request,
			HttpServletResponse response)
	{
		int paging = 1;
		if (request.getParameter("page") != null)
		{
			paging = Integer.parseInt(request.getParameter("page"));
		}
		return new MyRecentChangesBean(sessionManager, siteService, componentManager,
				sqlService, searchService,contentHostingService,announcementService,entityManager,paging);
	}

}
