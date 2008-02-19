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

package org.sakaiproject.sdata.services.mff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.SessionManager;

/**
 * TODO Javadoc
 * 
 * @author
 */
public class MyFileFinderBean implements ServiceDefinition
{
	private List<Map> searchList = new ArrayList<Map>();

	private Map<String, Object> map2 = new HashMap<String, Object>();

	private List<String> arl = new ArrayList<String>();

	private Map<String, Object> map = new HashMap<String, Object>();

	private static final Log log = LogFactory.getLog(MyFileFinderBean.class);

	/**
	 * TODO Javadoc
	 * 
	 * @param sessionManager
	 * @param siteService
	 */
	public MyFileFinderBean(SessionManager sessionManager, SiteService siteService,
			ContentHostingService contentHostingService, HttpServletResponse response,
			String searchParam, Boolean empty)
	{
		List<Site> sites = (List<Site>) siteService.getSites(SelectionType.ACCESS, null,
				null, null, SortType.TITLE_ASC, null);

		try
		{
			sites.add(0, (siteService.getSite(siteService.getUserSiteId(sessionManager
					.getCurrentSession().getUserId()))));
		}
		catch (IdUnusedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (Site s : sites)
		{

			arl.add(s.getId());

		}
		log.warn("is empty: " + empty);
		if (!empty)
		{

			try
			{

				SearchService search = (SearchService) ComponentManager
						.get("org.sakaiproject.search.api.SearchService");
				SearchList res = search.search(searchParam + " && tool:content", arl, 0,
						50, null, null);
				List<SearchResult> resBis = new ArrayList<SearchResult>();

				int totalfilesshown = 0;
				for (int ii = 0; ii < res.size(); ii++)
				{

					SearchResult bis = (SearchResult) res.get(ii);

					if (bis.getId() != null && !bis.getId().equals(""))
					{
						totalfilesshown += 1;
						Map<String, String> search_result = new HashMap<String, String>();
						search_result.put("title", bis.getTitle());
						search_result.put("reference", bis.getReference());
						search_result.put("url", bis.getUrl());
						// search_result.put("searchResult",
						// bis.getSearchResult());
						search_result.put("score", String.valueOf(bis.getScore()));
						searchList.add(search_result);

						resBis.add((SearchResult) res.get(ii));
					}
				}

				if (resBis.size() <= 0)
				{

					map2.put("total", totalfilesshown);
					map2.put("items", searchList);

				}
				else
				{
					map2.put("searchString", searchParam);
					map2.put("total", totalfilesshown);
					map2.put("items", searchList);
					map2.put("status", "succes");

				}

			}
			catch (Exception ex)
			{
				map2.put("status", "failed");
			}

		}
		else
		{

			map2 = new HashMap<String, Object>();
			map2.put("status", "failed");
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

}
