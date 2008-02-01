package org.sakaiproject.sdata.services.mcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class MyCoursesAndProjectsBean implements ServiceDefinition {

	private List<Site> mysites;
	private Session currentSession;
	private JSONObject jsonObject;
	private JSONArray jsonArray;
	private List<Map> MyMappedSites = new ArrayList<Map>();
	private Map<String, Object> map2 = new HashMap<String, Object>();;
	private Map<String, Object> map  = new HashMap<String, Object>();
	public MyCoursesAndProjectsBean(SessionManager sessionManager,
			SiteService siteService) {
		setCurrentSession(sessionManager.getCurrentSession());
		setMysites((java.util.List<Site>) siteService.getSites(
				SelectionType.ACCESS, null, null, null, SortType.TITLE_ASC,
				null));

		try {
			mysites.add(0, (siteService.getSite(siteService
					.getUserSiteId(currentSession.getUserId()))));

		} catch (IdUnusedException e) {
			// e.printStackTrace();
		}

		
		for (Site site : mysites) {

			map.put("title", site.getTitle());
			map.put("id", site.getId());
			map.put("url", site.getUrl());
			getMyMappedSites().add(map);
		}

		map2.put("items", getMyMappedSites());

		//jsonObject = JSONObject.fromObject(map2);

	}

	public void setMysites(List<Site> mysites) {
		this.mysites = mysites;
	}

	public List<Site> getMysites() {
		return mysites;
	}

	public void setCurrentSession(Session currentSession) {
		this.currentSession = currentSession;
	}

	public Session getCurrentSession() {
		return currentSession;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public String getJsonObjectString() {
		return jsonObject.toString();
	}

	public void setJsonArray(JSONArray jsonArray) {
		this.jsonArray = jsonArray;
	}

	public JSONArray getJsonArray() {
		return jsonArray;
	}

	public String getJsonArrayString() {

		return jsonArray.toString();

	}

	public Map<String, Object> getResponseMap() {

		return map2;
	}

	public void setMyMappedSites(List<Map> myMappedSites) {
		MyMappedSites = myMappedSites;
	}

	public List<Map> getMyMappedSites() {
		return MyMappedSites;
	}

}
