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

package org.sakaiproject.sdata.tool.model;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.Kernel;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.sdata.tool.SDataAccessException;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.SessionManager;

/**
 * @author ieb
 */
public class CHSNodeMap extends HashMap<String, Object>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8641164912506510404L;

	private static final Log log = LogFactory.getLog(CHSNodeMap.class);

	private ContentHostingService contentHostingService;

	private AuthzGroupService authZGroupService;

	private EntityManager entityManager;

	private SessionManager sessionManager;

	/**
	 * @throws SDataException 
	 * @throws RepositoryException
	 */
	public CHSNodeMap(ContentEntity n, int depth, ResourceDefinition rp)
			throws SDataException
	{
		String lock = ContentHostingService.AUTH_RESOURCE_HIDDEN;
		sessionManager = Kernel.sessionManager();
		entityManager = Kernel.entityManager();
		String userId = sessionManager.getCurrentSessionUserId();
		String reference = n.getReference();
		Reference referenceObj = entityManager.newReference(reference);
		Collection<?> groups = referenceObj.getAuthzGroups();
		
		boolean canSeeHidden = Kernel.securityService().unlock(userId, lock, reference, groups);
		
		if (!canSeeHidden && !n.isAvailable())
		{
			throw new SDataAccessException(403, "Permission denied on item");
		}
		contentHostingService = Kernel.contentHostingService();
		authZGroupService = Kernel.authzGroupService();
		depth--;
		put("mixinNodeType", getMixinTypes(n));
		put("properties", getProperties(n));
		put("name", getName(n));
		if (rp != null)
		{
			put("path", rp.getExternalPath(n.getId()));
		}
		put("permissions", getPermissions(n));

		if (n instanceof ContentResource)
		{
			put("primaryNodeType", "nt:file");
			addFile((ContentResource) n);
		}
		else
		{
			put("primaryNodeType", "nt:folder");
			addFolder((ContentCollection)n, rp, depth);
		}
	}

	private void addFolder(ContentCollection n, ResourceDefinition rp,  int depth) throws SDataException {
		put("available", n.isAvailable());
		put("hidden", n.isHidden());
		if (!n.isHidden())
		{
			Time releaseDate = n.getReleaseDate();
			if (releaseDate != null)
			{
				put("releaseDate", releaseDate.getTime());
			}
			Time retractDate = n.getRetractDate();
			if (retractDate != null)
			{
				put("retractDate", retractDate.getTime());
			}
		}

		ContentCollection cc = (ContentCollection) n;
		put("members",cc.getMemberCount());
		if (depth >= 0)
		{

			Map<String, Object> nodes = new HashMap<String, Object>();
			// list of IDs
			List<?> l = cc.getMembers();

			int i = 0;
			for (int k = 0; k < l.size(); k++)
			{
				String memberID = (String) l.get(k);	
				ContentEntity cn = null;
				try
				{
					cn = contentHostingService.getResource(memberID);
				}
				catch (Exception idex)
				{
					try
					{
						String collectionPath = memberID;
						if (!collectionPath.endsWith("/"))
						{
							collectionPath = collectionPath + "/";
						}
						cn = contentHostingService.getCollection(collectionPath);
					}
					catch (Exception ex)
					{

					}
				}
				if (cn != null)
				{

					try
					{
						Map<String, Object> m = new CHSNodeMap(cn, depth, rp);
						m.put("position", String.valueOf(i));
						nodes.put(getName(cn), m);
					}
					catch (SDataAccessException sdae)
					{
						// hide the item from the list
						continue;
					}
				}
				i++;
			}
			put("nitems", nodes.size());
			put("items", nodes);
		}
	}

	private Map<String, String> getPermissions(ContentEntity n) throws SDataException
	{
		Map<String, String> map = new HashMap<String, String>();
		
		
		
		
		
		if (n instanceof ContentCollection)
		{
			map.put("read", String.valueOf(contentHostingService.allowGetCollection(n
					.getId())));
			map.put("remove", String.valueOf(contentHostingService.allowRemoveCollection(n
					.getId())));
			map.put("write", String.valueOf(contentHostingService.allowUpdateCollection(n
					.getId())));
			
			String ref = n.getReference();
			
			
			Reference reference = entityManager.newReference(n.getReference());
			if ( log.isDebugEnabled() )
			{
				log.debug("Got Reference "+reference+" for "+n.getReference());
			}
			
			Collection<?> groups = reference.getAuthzGroups();			
			String user = sessionManager.getCurrentSessionUserId();
			map.put("admin", String.valueOf(authZGroupService.isAllowed(sessionManager.getCurrentSessionUserId(), AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP, groups)));
		}
		else
		{
			map.put("read", String.valueOf(contentHostingService.allowGetResource(n
					.getId())));
			map.put("remove", String.valueOf(contentHostingService
					.allowRemoveResource(n.getId())));
			map.put("write", String.valueOf(contentHostingService.allowUpdateResource(n
					.getId())));
		}
		return map;
	}


	/**
	 * @param n
	 * @throws RepositoryException
	 */
	private void addFile(ContentResource n)
	{
		ResourceProperties rp = n.getProperties();
		Calendar lastModified = new GregorianCalendar();
		try
		{
			Time lastModifiedTime = rp
					.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
			lastModified.setTimeInMillis(lastModifiedTime.getTime());
		}
		catch (EntityPropertyNotDefinedException e)
		{
			// default to now
		}
		catch (EntityPropertyTypeException e)
		{
			// default to now
		}
		long contentLength = n.getContentLength();
		String mimeType = n.getContentType();
		put("lastModified", lastModified.getTime());
		put("mimeType", mimeType);
		put("length", String.valueOf(contentLength));

		put("available", n.isAvailable());
		put("hidden", n.isHidden());
		if (!n.isHidden())
		{
			Time releaseDate = n.getReleaseDate();
			if (releaseDate != null)
			{
				put("releaseDate", releaseDate.getTime());
			}
			Time retractDate = n.getRetractDate();
			if (retractDate != null)
			{
				put("retractDate", retractDate.getTime());
			}
		}
	}

	/**
	 * @param n
	 * @return
	 * @throws RepositoryException
	 */
	private Map<String, Object> getProperties(ContentEntity n)
	{
		Map<String, Object> m = new HashMap<String, Object>();
		ResourceProperties rp = n.getProperties();
		for (Iterator<?> pi = rp.getPropertyNames(); pi.hasNext();)
		{
			String name = (String) pi.next();
			List<?> l = rp.getPropertyList(name);
			if (l.size() > 1)
			{
				String[] o = l.toArray(new String[0]);
				m.put(name, o);
			}
			else if (l.size() == 1)
			{
				m.put(name, l.get(0));

			}
		}
		return m;
	}

	/**
	 * @param n
	 * @return
	 * @throws RepositoryException
	 */
	private String[] getMixinTypes(ContentEntity n)
	{
		return new String[0];
	}

	/**
	 * @param cre
	 * @return
	 */
	private String getName(ContentEntity cre)
	{
		String id = cre.getId();
		if (id == null) return null;
		if (id.length() == 0) return null;

		// take after the last resource path separator, not counting one at the
		// very end if there
		boolean lastIsSeparator = id.charAt(id.length() - 1) == '/';
		return id.substring(id.lastIndexOf('/', id.length() - 2) + 1,
				(lastIsSeparator ? id.length() - 1 : id.length()));
	}

}
