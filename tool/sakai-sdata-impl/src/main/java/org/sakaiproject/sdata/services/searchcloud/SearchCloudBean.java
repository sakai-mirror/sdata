package org.sakaiproject.sdata.services.searchcloud;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.Kernel;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.email.cover.EmailService;
import org.sakaiproject.sdata.services.connections.ConnectionSqlreader;
import org.sakaiproject.sdata.services.connections.ConnectionSqlresult;
import org.sakaiproject.sdata.services.profile.ProfileSqlreader2;
import org.sakaiproject.sdata.services.profile.ProfileSqlresult2;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * A service definition bean for recent changes that the current user can see.
 * 
 * @author
 */
public class SearchCloudBean implements ServiceDefinition
{

	private static final Log log = LogFactory.getLog(SearchCloudBean.class);

	private SqlService sqlService = Kernel.sqlService();

	Map<String, Object> resultMap = new HashMap<String, Object>();

	private SessionManager sessionManager = Kernel.sessionManager();
	
	private UserDirectoryService userDirectoryService = Kernel.userDirectoryService();

	/**
	 * Create a recent changes bean with the number of pages.
	 * 
	 * @param paging
	 */
	public SearchCloudBean(HttpServletRequest request, HttpServletResponse response)
	{
		try {
			if (request.getMethod().equalsIgnoreCase("get")){
				getTagCloud(request, response);
			} else if (request.getMethod().equalsIgnoreCase("post")){
				addSearchTerm(request, response);
			} 
		} catch (Exception ex){
			try {
				ex.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}  

	private void addSearchTerm(HttpServletRequest request, HttpServletResponse response) {
		String searchquery = request.getParameter("query");
		
		Object[] params = new Object[1];
		params[0] = searchquery.toLowerCase();
		String sql = "SELECT * FROM sdata_searchcloud WHERE searchquery = ?";
		
		List <SearchCloudSqlresult> lst = sqlService.dbRead(sql, params, new SearchCloudSqlreader());
		
		if (lst.size() == 0){
			
			// Add it fresh to the list with number=1 
			Object[] params2 = new Object[2];
			params2[0] = searchquery.toLowerCase();
			params2[1] = 1;
			String sql2 = "INSERT INTO sdata_searchcloud (searchquery, number) VALUES (?,?)";
			
			sqlService.dbWrite(sql2, params2);
			
		} else {
			
			// Update
			Object[] params2 = new Object[2];
			params2[0] = lst.get(0).getNumber() + 1;
			params2[1] = searchquery.toLowerCase();
			String sql2 = "UPDATE sdata_searchcloud SET number=? WHERE searchquery=?";
			
			sqlService.dbWrite(sql2, params2);
			
		}
		
		resultMap.put("status","success");
		
	}

	private void getTagCloud(HttpServletRequest request, HttpServletResponse response) {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.api.ServiceDefinition#getResponseMap()
	 */
	public Map<String, Object> getResponseMap()
	{
		return resultMap;
	}

}
