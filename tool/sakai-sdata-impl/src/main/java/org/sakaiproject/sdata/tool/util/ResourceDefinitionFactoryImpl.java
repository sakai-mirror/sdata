/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Timefields Ltd
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.sdata.tool.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.ResourceDefinitionFactory;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.SecurityAssertion;
import org.sakaiproject.tool.api.Tool;

/**
 * TODO Javadoc
 * 
 * @author ieb
 */
public class ResourceDefinitionFactoryImpl implements ResourceDefinitionFactory
{

	private static final Log log = LogFactory.getLog(ResourceDefinitionFactoryImpl.class);

	private String basePath;

	private String baseUrl;

	private SecurityAssertion pathSecurityAssertion;

	/**
	 * construct a resource definition factory with a base URL and a base Path.
	 * @param config 
	 * 
	 * @param basePath
	 * @param basePath2
	 */
	public ResourceDefinitionFactoryImpl(Map<String, String> config, String baseUrl, String basePath)
	{
		this.basePath = basePath;
		this.baseUrl = baseUrl;
		pathSecurityAssertion = new PathSecurityAssertion(config);
		log.info("Definition Factory Created with base path as " + basePath);
	}


	/**
	 * TODO Javadoc
	 * 
	 * @param path
	 * @return
	 * @throws SDataException 
	 */
	public ResourceDefinition getSpec(final HttpServletRequest request) throws SDataException
	{

		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		String path = request.getPathInfo();
		path = path.substring(baseUrl.length());

		if (path.endsWith("/"))
		{
			path = path.substring(0, path.length() - 1);
		}
		int lastSlash = path.lastIndexOf("/");
		int leng = path.length();
		String lastElement = path.substring(lastSlash + 1);

		int version = -1;
		if (lastElement.length() > 0)
		{
			char c = lastElement.charAt(0);
			if (Character.isDigit(c))
			{
				version = Integer.parseInt(lastElement);
				path = path.substring(0, lastSlash);
			}
		}

		return new ResourceDefinitionImpl(request.getMethod(), basePath, path, version, pathSecurityAssertion);
	}

}
