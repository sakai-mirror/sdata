/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.sdata.tool;

/**
 * @author ieb
 */
public class ResourceDefinition
{

	private String path;

	private int version;

	private String basePath;

	private String repoPath;

	/**
	 * @param path
	 * @param path
	 * @param version
	 */
	public ResourceDefinition(String inbasePath, String inpath, int inversion)
	{
		path = inpath;
		version = inversion;
		basePath = String.valueOf(inbasePath);

		repoPath = basePath + path;
		repoPath = cleanPath(repoPath);
		repoPath = repoPath.replaceAll("//", "/");
		if ( repoPath.length() > 1 && repoPath.endsWith("/") ) {
			repoPath = repoPath.substring(0,repoPath.length()-1);
		}
		if ( !repoPath.startsWith("/") ) {
			repoPath = "/" + repoPath;
		}
	}

	/**
	 * @param repoPath2
	 * @return
	 */
	private String cleanPath(String p)
	{
		p = p.replaceAll("//", "/");
		if ( p.length() > 1 && p.endsWith("/") ) {
			p = repoPath.substring(0,p.length()-1);
		}
		if ( !p.startsWith("/") ) {
			p = "/" + p;
		}
		return p;
		
	}

	/**
	 * @return
	 */
	public String getRepositoryPath()
	{
		return repoPath;
	}

	/**
	 * @param path2
	 * @return
	 */
	public String getExternalPath(String path)
	{
		if ( path == null ) {
			return null;
		}
		if (path.startsWith(basePath))
		{
			return cleanPath(path.substring(basePath.length()));
		}
		return path;
	}

	/**
	 * @param name
	 * @return
	 */
	public String getRepositoryPath(String name)
	{
		return cleanPath(repoPath + "/" + name);
	}

}
