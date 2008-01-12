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

package org.sakaiproject.sdata.tool;

/**
 * @author ieb
 */
public class ResourceDefinitionFactory
{

	private String basePath;

	/**
	 * @param basePath
	 */
	public ResourceDefinitionFactory(String basePath)
	{
		this.basePath = basePath;
	}

	/**
	 * @param path
	 * @return
	 */
	public ResourceDefinition getSpec(String path)
	{
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

		return new ResourceDefinition(basePath, path, version);
	}

}
