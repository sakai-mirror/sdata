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

package org.sakaiproject.sdata.services.site;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;

/**
 * TODO Javadoc
 * 
 * @author
 */
public class SiteBean implements ServiceDefinition
{

	private List<Site> mysites;

	private Session currentSession;

	private List<Map> MyMappedSites = new ArrayList<Map>();

	private Map<String, Object> map2 = new HashMap<String, Object>();;

	private static final Log log = LogFactory.getLog(SiteBean.class);

	/**
	 * TODO Javadoc
	 * 
	 * @param sessionManager
	 * @param siteService
	 */
	public SiteBean(SessionManager sessionManager, SiteService siteService, String siteId)
	{
		boolean siteExists = true;
		String status = "900";
		ArrayList<HashMap<String, Object>> arlpages = new ArrayList<HashMap<String, Object>>();

		/*
		 * Determine the sites the current user is a member of
		 */

		setCurrentSession(sessionManager.getCurrentSession());
		setMysites((List<Site>) siteService.getSites(SelectionType.ACCESS, null, null,
				null, SortType.TITLE_ASC, null));

		try
		{
			mysites.add(0, (siteService.getSite(siteService.getUserSiteId(currentSession
					.getUserId()))));

		}
		catch (IdUnusedException e)
		{
			e.printStackTrace();
		}

		for (Site site : mysites)
		{
			log.error(site.getTitle() + " - " + site.getId());
		}

		/*
		 * See whether the user is allowed to see this page
		 */

		try
		{

			Site theSite = siteService.getSite(siteId);
			boolean member = false;

			map2.put("title", theSite.getTitle());

			if (!theSite.isPublished())
			{
				status = "903";
			}

			for (Site site : mysites)
			{
				if (site.getId().equals(siteId))
				{
					member = true;
				}
			}

			if (theSite.isPubView())
			{
				status = "904";
				member = true;
			}

			if (member == false)
			{
				status = "902";
			}

			int number = 0;

			if (member)
			{

				List<SitePage> pages = (List<SitePage>) theSite.getOrderedPages();

				for (SitePage page : pages)
				{

					number++;

					HashMap<String, Object> mpages = new HashMap<String, Object>();

					mpages.put("name", page.getTitle());
					mpages.put("layout", page.getLayoutTitle());
					mpages.put("number", number);

					ArrayList<HashMap<String, Object>> arltools = new ArrayList<HashMap<String, Object>>();
					List<ToolConfiguration> lst = (List<ToolConfiguration>) page
							.getTools();

					mpages.put("iconclass", "icon-"
							+ lst.get(0).getToolId().replaceAll("[.]", "-"));

					for (ToolConfiguration conf : lst)
					{
						HashMap<String, Object> tool = new HashMap<String, Object>();
						tool.put("url", conf.getId());
						Tool t = conf.getTool();

						if (t != null && t.getId() != null)
						{
							tool.put("title", conf.getTool().getTitle());
						}
						else
						{
							tool.put("title", page.getTitle());
						}
						arltools.add(tool);
					}

					mpages.put("tools", arltools);

					arlpages.add(mpages);

				}

			}

		}
		catch (IdUnusedException e)
		{

			status = "901";
			e.printStackTrace();

		}

		map2.put("status", status);
		map2.put("pages", arlpages);

	}

	/**
	 * TODO Javadoc
	 * 
	 * @param mysites
	 */
	public void setMysites(List<Site> mysites)
	{
		this.mysites = mysites;
	}

	/**
	 * TODO Javadoc
	 * 
	 * @return
	 */
	public List<Site> getMysites()
	{
		return mysites;
	}

	/**
	 * TODO Javadoc
	 * 
	 * @param currentSession
	 */
	public void setCurrentSession(Session currentSession)
	{
		this.currentSession = currentSession;
	}

	/**
	 * TODO Javadoc
	 * 
	 * @return
	 */
	public Session getCurrentSession()
	{
		return currentSession;
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
	 * TODO Javadoc
	 * 
	 * @param myMappedSites
	 */
	public void setMyMappedSites(List<Map> myMappedSites)
	{
		MyMappedSites = myMappedSites;
	}

	/**
	 * TODO Javadoc
	 * 
	 * @return
	 */
	public List<Map> getMyMappedSites()
	{
		return MyMappedSites;
	}

}