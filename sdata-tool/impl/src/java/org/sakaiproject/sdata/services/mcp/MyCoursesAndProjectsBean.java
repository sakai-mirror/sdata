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

package org.sakaiproject.sdata.services.mcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * @author 
 *
 */
public class MyCoursesAndProjectsBean implements ServiceDefinition
{

	private List<Site> mysites;

	private Session currentSession;

	private List<Map> MyMappedSites = new ArrayList<Map>();

	private Map<String, Object> map2 = new HashMap<String, Object>();;

	

	/**
	 * @param sessionManager
	 * @param siteService
	 */
	public MyCoursesAndProjectsBean(SessionManager sessionManager, SiteService siteService)
	{
		setCurrentSession(sessionManager.getCurrentSession());
		setMysites((List<Site>) siteService.getSites(SelectionType.ACCESS,
				null, null, null, SortType.TITLE_ASC, null));

		try
		{
			mysites.add(0, (siteService.getSite(siteService.getUserSiteId(currentSession
					.getUserId()))));

		}
		catch (IdUnusedException e)
		{
			// e.printStackTrace();
		}

		for (Site site : mysites)
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", site.getTitle());
			map.put("id", site.getId());
			map.put("url", site.getUrl());
			getMyMappedSites().add(map);
		}

		map2.put("items", getMyMappedSites());

		// jsonObject = JSONObject.fromObject(map2);

	}

	/**
	 * @param mysites
	 */
	public void setMysites(List<Site> mysites)
	{
		this.mysites = mysites;
	}

	/**
	 * @return
	 */
	public List<Site> getMysites()
	{
		return mysites;
	}

	/**
	 * @param currentSession
	 */
	public void setCurrentSession(Session currentSession)
	{
		this.currentSession = currentSession;
	}

	/**
	 * @return
	 */
	public Session getCurrentSession()
	{
		return currentSession;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
	 */
	public Map<String, Object> getResponseMap()
	{

		return map2;
	}

	/**
	 * @param myMappedSites
	 */
	public void setMyMappedSites(List<Map> myMappedSites)
	{
		MyMappedSites = myMappedSites;
	}

	/**
	 * @return
	 */
	public List<Map> getMyMappedSites()
	{
		return MyMappedSites;
	}

}
