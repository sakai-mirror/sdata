package org.sakaiproject.sdata.services.mcp;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.sakaiproject.sdata.tool.json.JSONServiceServlet;

public class MyCoursesAndProjectsServlet extends JSONServiceServlet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory()
			throws ServletException
	{
		return new MyCoursesAndProjectsServiceDefinitionFactory();
	}

	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory(ServletConfig config)
			throws ServletException
	{
		return new MyCoursesAndProjectsServiceDefinitionFactory();
	}

}
