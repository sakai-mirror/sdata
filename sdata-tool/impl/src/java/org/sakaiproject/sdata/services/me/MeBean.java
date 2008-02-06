/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/sdata-tool/impl/src/java/org/sakaiproject/sdata/tool/JCRDumper.java $
 * $Id: JCRDumper.java 45207 2008-02-01 19:01:06Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
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

package org.sakaiproject.sdata.services.me;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * @author
 */
public class MeBean implements ServiceDefinition
{

	private Session currentSession;

	private Map<String, Object> map2 = new HashMap<String, Object>();;

	private Map<String, Object> map = new HashMap<String, Object>();

	/**
	 * @param sessionManager
	 * @param siteService
	 */
	public MeBean(SessionManager sessionManager, SiteService siteService,
			UserDirectoryService userDirectoryService, HttpServletResponse response)
	{
		User user = null;
		currentSession = sessionManager.getCurrentSession();

		try
		{

			user = userDirectoryService.getUser(currentSession.getUserId());
		}
		catch (UserNotDefinedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// serialize user object

		if (user == null)
		{

			try
			{
				response.sendError(401);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else
		{

			map.put("userid", user.getId());
			map.put("firstname", user.getFirstName());
			map.put("lastname", user.getLastName());
			map.put("displayId", user.getDisplayId());
			map.put("email", user.getEmail());
			map.put("createdBy", user.getCreatedBy().getDisplayName());
			map.put("createdTime", user.getCreatedTime().toStringLocalFull());
			map.put("userEid", user.getEid());

			// map2.put("items", user);
			map2.put("items", map);

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
	 */
	public Map<String, Object> getResponseMap()
	{

		return map2;
	}

	/**
	 * @param myMappedSites
	 */

}
