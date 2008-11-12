package org.sakaiproject.sdata.services.site;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.Kernel;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.SDataFunction;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

public class SiteRoleFunction implements SDataFunction {
	private SiteService siteService;

	public SiteRoleFunction() throws ServletException {
		siteService = Kernel.siteService();
	}

	public void call(Handler handler, HttpServletRequest request,
			HttpServletResponse response, Object target, ResourceDefinition rp)
			throws SDataException {
		
		String siteId = request.getParameter("siteid");
    	String roleId = request.getParameter("roleId");
    	if (roleId == null){
    		throw new IllegalArgumentException("No role id specified");
    	}
    	Site site = null;
    	try {
    		site = siteService.getSite(siteId);
    	} catch (IdUnusedException ex){
    		throw new IllegalArgumentException("Site with id " + siteId + " not found");
    	}
    	if (request.getMethod().equalsIgnoreCase("post")){
	    	try {
				site.addRole(roleId);
				Role role = site.getRole(roleId);
				role.allowFunction("site.visit");
			} catch (RoleAlreadyDefinedException e) {
				// Ignore
			} 
    	} else if (request.getMethod().equalsIgnoreCase("delete")) {
    		site.removeRole(roleId);
    	} else {
    		throw new IllegalArgumentException("Method " + request.getMethod() + " not supported");
    	}
    	try {
			siteService.save(site);
		} catch (IdUnusedException e) {
			// Ignore
		} catch (PermissionException e) {
			throw new SecurityException("User not allowed to update role " + roleId + " in site " + siteId);
		}

	}

	public void destroy() {
	}
}
