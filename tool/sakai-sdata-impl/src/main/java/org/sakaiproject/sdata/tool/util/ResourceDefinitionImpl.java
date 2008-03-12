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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.SecurityAssertion;

/**
 * TODO Javadoc
 * 
 * @author ieb
 */
public class ResourceDefinitionImpl implements ResourceDefinition
{

	private static final Log log = LogFactory.getLog(ResourceDefinitionImpl.class);

	private String path;

	private int version;

	private String basePath;

	private String repoPath;

	private String function;

	/**
	 * TODO Javadoc
	 * @param request 
	 * 
	 * @param inbasePath the base path of the resource in the repository
	 * @param inpath the path reference in the request
	 * @param method the method bein applied
	 * @param version the version being requested.
	 * @throws SDataException 
	 */
	public ResourceDefinitionImpl(String method, String f, String inbasePath, String inpath, int inversion, SecurityAssertion assertion ) throws SDataException
	{
		if (log.isDebugEnabled())
		{
			log.debug("ResourceDef: Base:" + inbasePath + ": path:" + inpath
					+ ": version:" + inversion);
		}
		path = inpath;
		version = inversion;
		basePath = String.valueOf(inbasePath);
		
		function = f;

		repoPath = basePath + path;
		repoPath = cleanPath(repoPath);
		repoPath = repoPath.replaceAll("//", "/");
		if (repoPath.length() > 1 && repoPath.endsWith("/"))
		{
			repoPath = repoPath.substring(0, repoPath.length() - 1);
		}
		if (!repoPath.startsWith("/"))
		{
			repoPath = "/" + repoPath;
		}
		
		assertion.check(method,repoPath);
	}


	/**
	 * TODO Javadoc
	 * 
	 * @param repoPath2
	 * @return
	 */
	private String cleanPath(String p)
	{
		p = p.replaceAll("//", "/");
		if (p.length() > 1 && p.endsWith("/"))
		{
			p = repoPath.substring(0, p.length() - 1);
		}
		if (!p.startsWith("/"))
		{
			p = "/" + p;
		}
		return p;

	}

	/**
	 * TODO Javadoc
	 * 
	 * @return
	 */
	public String getRepositoryPath()
	{
		return repoPath;
	}

	/**
	 * TODO Javadoc
	 * 
	 * @param path2
	 * @return
	 */
	public String getExternalPath(String path)
	{
		if (path == null)
		{
			return null;
		}
		if (path.startsWith(basePath))
		{
			return cleanPath(path.substring(basePath.length()));
		}
		return path;
	}

	/**
	 * TODO Javadoc
	 * 
	 * @param name
	 * @return
	 */
	public String getRepositoryPath(String name)
	{
		return cleanPath(repoPath + "/" + name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.api.ResourceDefinition#isPrivate()
	 */
	public boolean isPrivate()
	{
		return false;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.sdata.tool.api.ResourceDefinition#getFunctionDefinition()
	 */
	public String getFunctionDefinition()
	{
		return function;
	}

}
