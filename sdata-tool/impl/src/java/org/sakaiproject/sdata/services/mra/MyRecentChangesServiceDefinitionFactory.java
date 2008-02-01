package org.sakaiproject.sdata.services.mra;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.db.api.SqlService;

public class MyRecentChangesServiceDefinitionFactory implements
		ServiceDefinitionFactory {

	private SessionManager sessionManager;
	private SiteService siteService;
	private ComponentManager componentManager;
	private SqlService sqlService;

	public MyRecentChangesServiceDefinitionFactory() {
		componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();
		
		siteService = (SiteService) componentManager.get(SiteService.class.getName());
		sessionManager = (SessionManager) componentManager.get(SessionManager.class.getName());
		sqlService = (SqlService) componentManager.get(SqlService.class.getName());
	}

	public ServiceDefinition getSpec(HttpServletRequest request,
			HttpServletResponse response) {
		return new MyRecentChangesBean(sessionManager, siteService, componentManager, sqlService);
	}

}
