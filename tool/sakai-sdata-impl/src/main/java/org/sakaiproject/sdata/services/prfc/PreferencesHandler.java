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

import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.sakaiproject.sdata.tool.json.JSONServiceHandler;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;


/**
 * Handler for Preferences, in JSON form
 * 
 */
public class PreferencesHandler extends JSONServiceHandler
{
	private static final Log log = LogFactory.getLog(PreferencesHandler.class);
	
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.json.JSONServiceServlet#getServiceDefinitionFactory()
	 */
	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory()
			throws ServletException
	{
		return new PreferencesServiceDefinitionFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.ServiceServlet#getServiceDefinitionFactory(javax.servlet.ServletConfig)
	 */
	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory(
			Map<String, String> config) throws ServletException
	{
		return new PreferencesServiceDefinitionFactory();
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException 
	{
		String saveMode = request.getParameter("savemode");
		if(saveMode != null)
		{
			if(saveMode.equals("savedetail"))
			{
				saveDetail(request);
			}
			else if(saveMode.equals("savenoti"))
			{
			}
		}
	}
	
	private void saveDetail(HttpServletRequest request)
	{
		UserEdit user = null;
		Session currentSession = SessionManager.getCurrentSession();

		try
		{
			User currentUser = UserDirectoryService.getUser(currentSession.getUserId());
			user = UserDirectoryService.editUser(currentUser.getId());
		}
		catch (UserNotDefinedException unde)
		{
			log.error(unde);
		} 
		catch (UserPermissionException upe)
		{
			log.error(upe);
		} 
		catch (UserLockedException ule)
		{
			log.error(ule);
		}
		
		String firstname = request.getParameter("firstname");
		String lastname = request.getParameter("lastname");
		String emailAddress = request.getParameter("email");
		String oldPw = request.getParameter("currentpw");
		String newPw = request.getParameter("newpw");
		String retypePw = request.getParameter("retypepw");
		String selectedZone = request.getParameter("selected_zone");
		String selectedLanguage = request.getParameter("seleted_language");
		
		user.setFirstName(firstname);
		user.setLastName(lastname);
		user.setEmail(emailAddress);

		try
		{
			if(user.checkPassword(oldPw) && !StringUtil.different(newPw, retypePw))
			{
				user.setPassword(newPw);
			}
			UserDirectoryService.commitEdit(user);
		} 
		catch (UserAlreadyDefinedException uade)
		{
			log.error(uade);
		}

		PreferencesEdit prefEdit = null;
		try
		{
			prefEdit = (PreferencesEdit) PreferencesService.edit(user.getId());
			ResourcePropertiesEdit props = prefEdit.getPropertiesEdit(TimeService.APPLICATION_ID);
			if(props.getProperty(TimeService.TIMEZONE_KEY) != null && 
					!((String)props.getProperty(TimeService.TIMEZONE_KEY)).equals(selectedZone))
			{
				props.removeProperty(TimeService.TIMEZONE_KEY);
				props.addProperty(TimeService.TIMEZONE_KEY, selectedZone);
				PreferencesService.commit(prefEdit);				
			}
			else if (props.getProperty(TimeService.TIMEZONE_KEY) == null)
			{
				props.addProperty(TimeService.TIMEZONE_KEY, selectedZone);
				PreferencesService.commit(prefEdit);
			}
			props = (ResourcePropertiesEdit) prefEdit.getPropertiesEdit(ResourceLoader.APPLICATION_ID);
			if(props.getProperty(ResourceLoader.LOCALE_KEY) != null && 
					!((String)props.getProperty(ResourceLoader.LOCALE_KEY)).equals(selectedLanguage))
			{
				props.removeProperty(ResourceLoader.LOCALE_KEY);
				props.addProperty(ResourceLoader.LOCALE_KEY, selectedLanguage);
				PreferencesService.commit(prefEdit);				
			}
			else if (props.getProperty(ResourceLoader.LOCALE_KEY) == null)
			{
				props.addProperty(ResourceLoader.LOCALE_KEY, selectedLanguage);
				PreferencesService.commit(prefEdit);				
			}
		}
		catch (IdUnusedException iue)
		{
			try
			{
				prefEdit = PreferencesService.add(user.getId());
				ResourcePropertiesEdit props = prefEdit.getPropertiesEdit(TimeService.APPLICATION_ID);
				props.addProperty(TimeService.TIMEZONE_KEY, selectedZone);
				props.addProperty(ResourceLoader.LOCALE_KEY, selectedLanguage);
				PreferencesService.commit(prefEdit);
			}
			catch (PermissionException pe1)
			{
				log.error(pe1);
			}
			catch (IdUsedException ie1)
			{
				log.error(ie1);
			}
		}
		catch (PermissionException pe)
		{
			log.error(pe);
		} 
		catch (InUseException ie)
		{
			log.error(ie);
		}
	}
}