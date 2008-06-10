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

package org.sakaiproject.sdata.tool.functions;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.Kernel;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupAlreadyDefinedException;
import org.sakaiproject.authz.api.GroupIdInvalidException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.model.CHSGroupMap;

/**
 * <h3>Overview</h3>
 * <p>
 * This function provides a mechanism to set permissions on a CHS item. It will
 * only do this on a folder at this stage to avoid exponential increase in the
 * number of locations where permissions are created.
 * </p>
 * <h3>Request Parameters</h3>
 * <ul>
 * <li>Method: POST</li>
 * <li>Parameter Name: Description</li>
 * <li>f: <b>pm</b> Identifies the function</li>
 * <li>role: <b>access</b> The role to set the parameter on</li>
 * <li>perm: <b>read|write|delete|admin</b> The permission to set</li>
 * <li>set: <b>1|0</b> Set or Unset</li>
 * </ul>
 * <h3>Description</h3>
 * <p>
 * The Post is made with a function select parameter f=pm, and a three equal
 * length arrays of parameter values for each of role, perm, and set. Elements
 * are processed in sequence specifying the role , the permission (perm) to set
 * or unset on that role and if (set) to set or unset the permission.
 * </p>
 * <h3>Response</h3>
 * <p>
 * The current user must have admin permission on the folder,if they do and the
 * operation is successful in its entity a 200 will be returned. If the user is
 * not allowed to set, a 403 will be returned, if there is a problem with the
 * request (eg the role does not exist or the permission is not understood, then
 * a 500 will be returned
 * </p>
 * <p>
 * On a 200 the body will contain an updated set of metadata on the folder and
 * the metadata of the files and folders contained in the folder down to a depth
 * of 1
 * </p>
 * 
 * @author ieb
 */
public class CHSPermissionsFunction extends CHSSDataFunction
{

	private static final Log log = LogFactory.getLog(CHSPermissionsFunction.class);

	public static final String ROLE = "role";

	public static final String PERM = "perm";

	public static final String SET = "set";

	public static final String READ = "read";

	public static final String WRITE = "write";

	public static final String DELETE = "delete";

	public static final String ADMIN = "admin";

	public static final String SETVALUE = "1";

	public static final String UNSETVALUE = "0";

	private HashMap<String, String[]> permissionMap;

	private AuthzGroupService authzGroupService;


	public CHSPermissionsFunction()
	{
		permissionMap = new HashMap<String, String[]>();
		permissionMap
				.put(READ, new String[] { ContentHostingService.AUTH_RESOURCE_READ });
		permissionMap.put(WRITE, new String[] {
				ContentHostingService.AUTH_RESOURCE_WRITE_ANY,
				ContentHostingService.AUTH_RESOURCE_ADD });
		permissionMap.put(DELETE,
				new String[] { ContentHostingService.AUTH_RESOURCE_REMOVE_ANY });
		permissionMap.put(ADMIN,
				new String[] { AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP });
		authzGroupService = Kernel.authzGroupService();
	}

	@SuppressWarnings("unchecked")
	public void call(Handler handler, HttpServletRequest request,
			HttpServletResponse response, Object target, ResourceDefinition rp)
			throws SDataException
	{
		SDataFunctionUtil.checkMethod(request.getMethod(), "GET|POST");
		if ("GET".equals(request.getMethod()))
		{
			
			try
			{
				// check we can read the entity, but dont keep it
				getEntity(handler, rp.getRepositoryPath());
			}
			catch (PermissionException e1)
			{
				throw new SDataException(HttpServletResponse.SC_FORBIDDEN,
						"User is not allowed to edit permissions on "
								+ rp.getRepositoryPath());
			}
			
			String ref = contentHostingService.getReference(rp.getRepositoryPath());			
			AuthzGroup authZGroup;
			try
			{
				authZGroup = authzGroupService.getAuthzGroup(ref);
			}
			catch (GroupNotDefinedException e1)
			{
				throw new SDataException(HttpServletResponse.SC_NOT_FOUND,
						"Realm "+ref+" does not exist for "
								+ rp.getRepositoryPath());
			}
			
			
			
			
			try
			{
				handler.sendMap(request, response, new CHSGroupMap(authZGroup,permissionMap));
			}
			catch (IOException e)
			{
				throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"IO Error " + e.getMessage());
			}

		}
		else
		{
			
			try {
				// check we can read the entity, but dont keep it
				getEntity(handler, rp.getRepositoryPath());
			} catch ( PermissionException ex1 ) {
				throw new SDataException(HttpServletResponse.SC_FORBIDDEN,
						"User is not allowed to edit permissions on "
								+ rp.getRepositoryPath());				
			}
			String ref = contentHostingService.getReference(rp.getRepositoryPath());
			
			// get the roles
			AuthzGroup authZGroup = null;
			try
			{
				authZGroup = authzGroupService.getAuthzGroup(ref);
			}
			catch (GroupNotDefinedException e1)
			{
				try
				{
					authZGroup = authzGroupService.addAuthzGroup(ref);
				}
				catch (GroupIdInvalidException e)
				{
					throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Denied create Permissions group  "+ref+" id is invalid "
									+ rp.getRepositoryPath());
				}
				catch (GroupAlreadyDefinedException e)
				{
					throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Denied create Permissions group  "+ref+" already exists for "
									+ rp.getRepositoryPath());
				}
				catch (AuthzPermissionException e)
				{
					throw new SDataException(HttpServletResponse.SC_FORBIDDEN,
							"Denied create Permissions group  "+ref+" "
									+ rp.getRepositoryPath());
				}
				
				
			}

			// validate the request
			String[] roles = request.getParameterValues(ROLE);
			String[] permissions = request.getParameterValues(PERM);
			String[] sets = request.getParameterValues(SET);

			if (roles == null || permissions == null || sets == null
					|| roles.length != permissions.length || roles.length != sets.length)
			{
				throw new SDataException(HttpServletResponse.SC_BAD_REQUEST,
						"Request must contain the same number of name, value, and action parameters ");
			}

			for (String perm : permissions)
			{
				if (!permissionMap.containsKey(perm))
				{
					throw new SDataException(HttpServletResponse.SC_BAD_REQUEST,
							"The Permission " + perm
									+ " does not exist as a permission on the folder ");

				}
			}
			for (String set : sets)
			{
				if ((SETVALUE.equals(set) && UNSETVALUE.equals(set)))
				{
					throw new SDataException(HttpServletResponse.SC_BAD_REQUEST,
							"The request can only be set(1) or unset(0) ");

				}
			}

			// set the permissions
			for (int i = 0; i < roles.length; i++)
			{						
				Role r = authZGroup.getRole(roles[i]);
				if ( r == null ) {
					try
					{
						r = authZGroup.addRole(roles[i]);
					}
					catch (RoleAlreadyDefinedException e)
					{
						log.warn("Internal Error, adding role twice "+roles[i]);
					}

				}
				String[] functions = permissionMap.get(permissions[i]);
				if (SETVALUE.equals(sets[i]))
				{
					for (String function : functions)
					{
						log.info("Allow "+function+" on "+r.getId());
						r.allowFunction(function);
					}
				}
				else
				{
					for (String function : functions)
					{
						log.info("Dissalow "+function+" on "+r.getId());
						r.disallowFunction(function);
					}
				}
			}
			try
			{
				authzGroupService.save(authZGroup);
			}
			catch (GroupNotDefinedException e1)
			{
				throw new SDataException(HttpServletResponse.SC_NOT_FOUND,
						"Auth Group Does not exist " + rp.getRepositoryPath());
			}
			catch (AuthzPermissionException e1)
			{
				throw new SDataException(HttpServletResponse.SC_FORBIDDEN,
						"User is not allowed to edit permissions on "
								+ rp.getRepositoryPath());
			}
			

			try
			{
				handler.sendMap(request, response, new CHSGroupMap(authZGroup,permissionMap));
			}
			catch (IOException e)
			{
				throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"IO Error " + e.getMessage());
			}
		}

	}

}