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

package org.sakaiproject.sdata.services.motd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageService;
import org.sakaiproject.sdata.services.mra.MyRecentChangesResult;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import com.sun.java_cup.internal.parse_action;

/**
 * @author
 */
public class MessageOfTheDayBean implements ServiceDefinition
{
	private List<Map> searchList = new ArrayList<Map>();

	private Map<String, Object> map2 = new HashMap<String, Object>();;

	List<String> arl = new ArrayList<String>();

	ArrayList<Message> Mes = new ArrayList<Message>();

	private Map<String, Object> map = new HashMap<String, Object>();

	private static final Log log = LogFactory.getLog(MessageOfTheDayBean.class);
	
	private List<Map> MyMotds = new ArrayList<Map>();

	/**
	 * @param sessionManager
	 * @param siteService
	 */
	public MessageOfTheDayBean(SessionManager sessionManager,
			MessageService messageservice, TimeService timeService,
			SiteService siteService, HttpServletResponse response)
	{

		try
		{
			// log.error(messageservice.toString());

			// hardcoded because there does not seem to be a good way to do it
			String ref = "/announcement/channel/!site/motd"; // messageservice.channelReference("!site",
																// SiteService.MAIN_CONTAINER);
			// making up a date that is wayyy in the past
			Time reallyLongTimeAgo = timeService.newTime(0);
			List<Message> messages = messageservice.getMessages(ref, reallyLongTimeAgo,
					1, false, false, false);
			if (messages.size() <= 0)
			{
				map.put("motdBody", "No Message of the day set");
				map.put("motdUrl", "#");
				MyMotds.add(map);
				map2.put("items", MyMotds);
			}
			else
			{
				Message motd = messages.get(0);
				map.put("motdBody", motd.getBody());
				map.put("motdUrl", motd.getUrl());
				MyMotds.add(map);
				map2.put("items", MyMotds);

			}
		}
		catch (PermissionException e)
		{
			throw new RuntimeException("He's dead Jim! : " + e.getMessage(), e);
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
