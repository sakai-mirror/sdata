/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.sdata.services.prfc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A Preferences definition bean
 * 
 */
public class PreferencesBean implements ServiceDefinition
{
	private static final Log log = LogFactory.getLog(PreferencesBean.class);
	
	private Session currentSession;

	private Map<String, Object> map = new HashMap<String, Object>();;

	public PreferencesBean(HttpServletRequest request, HttpServletResponse response, PreferencesService ps, 
			SessionManager sessionManager, UserDirectoryService userDirectoryService)
	{
		User user = null;
		currentSession = sessionManager.getCurrentSession();

		try
		{
			user = userDirectoryService.getUser(currentSession.getUserId());
		}
		catch (UserNotDefinedException e)
		{
			log.error(e);
		}

		map.put("userid", user.getId());
		map.put("firstname", user.getFirstName());
		map.put("lastname", user.getLastName());
		map.put("displayId", user.getDisplayId());
		map.put("email", user.getEmail());

		Preferences prefs = (PreferencesEdit) ps.getPreferences(user.getId());
		ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
		String timeZone = props.getProperty(TimeService.TIMEZONE_KEY);
		if(timeZone != null)
			map.put("timezone", timeZone);
		else
		{
			map.put("timezone", TimeService.getLocalTimeZone().getID());
		}
		props = prefs.getProperties(ResourceLoader.APPLICATION_ID);
		String locale = props.getProperty(ResourceLoader.LOCALE_KEY);
		if(locale != null)
			map.put("locale", locale);
		else
			map.put("locale", Locale.getDefault().getDisplayName());

		String[] timeZoneArray = TimeZone.getAvailableIDs();
		Arrays.sort(timeZoneArray);
		List timeZones = new ArrayList();
		for (int i = 0; i < timeZoneArray.length; i++)
		{
			timeZones.add(timeZoneArray[i]);
		}

		map.put("timezonelist", timeZones);
		
		Locale[] localeArray = Locale.getAvailableLocales();
		String [] languageArray = new String [localeArray.length];
		for (int i = 0; i < localeArray.length; i++)
		{
			languageArray [i] = localeArray[i].getDisplayName();
		}
		Arrays.sort(languageArray);
		List languages = new ArrayList();
		for (int i = 0; i < languageArray.length; i++)
		{
			languages.add(languageArray[i]);
		}
		
		map.put("languages", languages);
		
	}

	public Map<String, Object> getResponseMap() 
	{
		return map;
	}

}
