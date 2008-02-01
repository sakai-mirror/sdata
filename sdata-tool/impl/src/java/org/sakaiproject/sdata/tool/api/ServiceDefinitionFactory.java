package org.sakaiproject.sdata.tool.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ServiceDefinitionFactory {

	ServiceDefinition getSpec(HttpServletRequest request,
			HttpServletResponse response);

}
