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

package org.sakaiproject.sdata.tool.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.SecurityAssertion;

/**
 * An implementaiton of the Security Assertion that uses the http method, the
 * path and the sakai security service for perform the assertion. On check it
 * will throw SDataExceptions indicating forbidden if the path is outside its
 * configured range, or it is denied by the the Sakai security service.
 * 
 * @author ieb
 */
public class PathSecurityAssertion implements SecurityAssertion
{

	/**
	 * the init parameter name for baseLocation
	 */
	private static final String BASE_LOCATION_INIT = "locationbase";

	/**
	 * The default setting for the baseLocation
	 */
	private static final String DEFAULT_BASE_LOCATION = "";

	/**
	 * the init parameter for baseResource
	 */
	private static final String BASE_RESOURCE_LOCATION_INIT = "resourcebase";

	/**
	 * the default value for base resource
	 */
	private static final String DEFAULT_BASE_RESOURCE = "";

	/**
	 * the init parameter name for the lock map
	 */
	private static final String LOCK_MAP_INIT = "locks";

	/**
	 * the default lock map
	 */
	private static final String DEFAULT_LOCK_MAP = "GET:content.read,PUT:content.write,HEAD:content.read,POST:content.write,DELETE:content.delete";

	private static final Log log = LogFactory.getLog(PathSecurityAssertion.class);

	/**
	 * The base location that is Security Advisor applies to. Only paths that
	 * start with this are allowed all others are denied regardless of the
	 * method. If the path starts with baseLocation, baseLocation is removed
	 * from the path and baseResource is prepended to the patch to generate a
	 * full resource location suitable for using with the security service.
	 */
	private String baseLocation;

	/**
	 * The sakai security service
	 */
	private SecurityService securityService;

	/**
	 * A map mapping http methods to locks
	 */
	private Map<String, String> locks;

	/**
	 * this is prepended to the resource path, after normalizing (ie removing
	 * baseLocation) and before sending to the Sakai security service.
	 */
	private String baseResource;

	private boolean inTest = false;

	/**
	 * Construct a PathSecurityAssertion class based on the standard
	 * configuration map. The Map may have init parameters as defined by
	 * BASE_LOCATION_INIT, BASE_RESOURCE_LOCATION_INIT, LOCK_MAP_INIT
	 * 
	 * @param config
	 */
	public PathSecurityAssertion(Map<String, String> config)
	{
		baseLocation = config.get(BASE_LOCATION_INIT);
		if (baseLocation == null)
		{
			baseLocation = DEFAULT_BASE_LOCATION;
		}
		else
		{
			log.info("Set Base Location to " + baseLocation);
		}
		baseResource = config.get(BASE_RESOURCE_LOCATION_INIT);
		if (baseResource == null)
		{
			baseResource = DEFAULT_BASE_RESOURCE;
		}
		else
		{
			log.info("Set Base Location to " + baseResource);
		}
		String lockMapSpec = config.get(LOCK_MAP_INIT);
		if (lockMapSpec == null)
		{
			lockMapSpec = DEFAULT_LOCK_MAP;
		}
		else
		{
			log.info("Set lockMapSpec to " + lockMapSpec);
		}
		locks = new HashMap<String, String>();
		String[] specList = lockMapSpec.split(",");
		for (String spec : specList)
		{
			String[] kv = spec.split(":", 2);
			locks.put(kv[0], kv[1]);
		}
		String testMode = config.get("testmode");
		if (testMode == null)
		{
			inTest = false;
			ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager
					.getInstance();

			securityService = (SecurityService) componentManager
					.get(SecurityService.class.getName());
		}
		else
		{
			inTest = true;
		}

	}

	/**
	 * Performs the security assertion based on the resourceLocation, from the
	 * orriginal request and the method beign attempted. Will throw a
	 * SDataException with Forbidden if the resoruce location is outside the
	 * configured range, or if permission is denied.
	 * 
	 * @see org.sakaiproject.sdata.tool.api.SecurityAssertion#check(java.lang.String,
	 *      java.lang.String)
	 */
	public void check(String method, String resourceLocation) throws SDataException
	{
		if (inTest && securityService == null)
		{
			return;
		}
		if (resourceLocation == null || !resourceLocation.startsWith(baseLocation))
		{
			log.info("Denied " + method + " on " + resourceLocation + " base "
					+ baseLocation);
			throw new SDataException(HttpServletResponse.SC_FORBIDDEN, "Access Forbidden");
		}
		String resourceReference = resourceLocation.substring(baseLocation.length());
		String resourceLock = getResourceLock(method);
		try
		{
			if (!securityService.unlock(resourceLock,resourceReference))
			{
				log.info("Denied " + method + ":" + resourceLock + " on "
						+ resourceLocation + " ref  " + resourceReference);
				throw new SDataException(HttpServletResponse.SC_FORBIDDEN,
						"Access denied for operation " + method);
			}
			log.info("Granted " + method + ":" + resourceLock + " on " + resourceLocation
					+ " ref  " + resourceReference);
		}
		catch (Exception pex)
		{
			log.info("Denied " + method + ":" + resourceLock + " on " + resourceLocation
					+ " ref  " + resourceReference);
			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Access denied for operation " + method + " cause:"
							+ pex.getMessage());
		}
	}

	/**
	 * Convert the HTTP Method into a lock.
	 * 
	 * @param method
	 * @return
	 */
	private String getResourceLock(String method)
	{
		return locks.get(method);
	}

	/**
	 * @return the securityService
	 */
	public SecurityService getSecurityService()
	{
		return securityService;
	}

	/**
	 * @param securityService
	 *        the securityService to set
	 */
	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}

}
