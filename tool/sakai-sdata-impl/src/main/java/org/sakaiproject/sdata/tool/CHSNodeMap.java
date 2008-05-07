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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.time.api.Time;

/**
 * @author ieb
 */
public class CHSNodeMap extends HashMap<String, Object>
{
	private ContentHostingService contentHostingService;

	/**
	 * @throws RepositoryException
	 */
	public CHSNodeMap(ContentEntity n, int depth, ResourceDefinition rp,
			ContentHostingService contentHostingService)
	{
		this.contentHostingService = contentHostingService;
		depth--;
		put("mixinNodeType", getMixinTypes(n));
		put("properties", getProperties(n));
		put("name", getName(n));
		if (rp != null)
		{
			put("path", rp.getExternalPath(n.getId()));
		}

		if (n instanceof ContentResource)
		{
			put("primaryNodeType", "nt:file");
			addFile((ContentResource) n);
		}
		else
		{
			put("primaryNodeType", "nt:folder");
			ContentCollection cc = (ContentCollection) n;
			if (depth >= 0)
			{

				Map<String, Object> nodes = new HashMap<String, Object>();
				// list of IDs
				List<String> l = cc.getMembers();

				int i = 0;
				for (String memberID : l)
				{

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
							if ( !collectionPath.endsWith("/") ) {
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
						Map<String, Object> m = new CHSNodeMap(cn, depth, rp, contentHostingService);
						m.put("position", String.valueOf(i));
						nodes.put(getName(cn), m);
					}
					i++;
				}
				put("nitems", nodes.size());
				put("items", nodes);
			}
		}
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
			Time lastModifiedTime = rp.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE);
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
		for (Iterator pi = rp.getPropertyNames(); pi.hasNext();)
		{
			String name = (String) pi.next();
			List l = rp.getPropertyList(name);
			if (l.size() > 1)
			{
				Object[] o = l.toArray(new String[0]);
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