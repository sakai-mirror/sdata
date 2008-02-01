package org.sakaiproject.sdata.services.mcp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.sakaiproject.sdata.tool.json.JSONServiceServlet;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;

public class MyCoursesAndProjectsServlet extends JSONServiceServlet {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory()
			throws ServletException {
		return new MyCoursesAndProjectsServiceDefinitionFactory();
	}
	
	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory(
			ServletConfig config) throws ServletException {
		return new MyCoursesAndProjectsServiceDefinitionFactory();
	}

}
