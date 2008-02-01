package org.sakaiproject.sdata.services.mra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService; //import org.sakaiproject.tool.cover.ToolManager;

import java.util.ArrayList; //import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.text.SimpleDateFormat;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.SiteService.SelectionType;
import net.sf.json.JSONArray;

//import net.sf.json.JSONObject;
//import net.sf.json.JSONSerializer;

public class MyRecentChangesBean implements ServiceDefinition {

	private Session currentSession;
	private String currentUser;
	private EventTrackingService eventTrackingService;
	private Event event;

	private Date lastLogin = null;
	private List<String> lstLastLogin;
	private List<Site> mySites;
	private ArrayList<String> arlSiteId = new ArrayList<String>();

	private SearchService searchService;
	private ContentHostingService contentHostingService;
	private AnnouncementService announcementService;
	private EntityManager entityManager;

	private SearchList searchList;
	private SearchResult searchResult = null;
	private SiteService siteService;
	private ComponentManager componentManager;
	private SqlService sqlService;

	private String currentpage;
	private List<Map> myRecentResults = new ArrayList<Map>();

	private Map<String, Object> map2 = new HashMap<String, Object>();
	private Map<String, Object> map = new HashMap<String, Object>();

	private SessionManager sessionManager;

	private static final Log log = LogFactory.getLog(MyRecentChangesBean.class);

	public MyRecentChangesBean(SessionManager sessionManager,
			SiteService siteService, ComponentManager componentManager,
			SqlService sqlService) {

		this.setSqlService(sqlService);
		this.setComponentManager(componentManager);
		this.setSiteService(siteService);
		this.setSessionManager(sessionManager);// .getCurrentSession());
		this.setCurrentSession(this.getSessionManager().getCurrentSession());
		this.setCurrentUser(this.getCurrentSession().getUserId());

		log.info("Sessie = " + currentSession);
		log.info("User = " + currentUser);
		
		search();
		
	} // END CONSTRUCTOR

	public void search() {

		// this.setArlSiteId(new ArrayList<String>());

		try {

			currentSession = getSessionManager().getCurrentSession();
			currentUser = currentSession.getUserId();

			log.error(currentSession);
			log.error(currentUser);

			// setEventTrackingService(org.sakaiproject.event.cover.EventTrackingService.getInstance());
			// setEvent(getEventTrackingService().newEvent("octopus.recent.activity",
			// null, true));

			// post the event
			// getEventTrackingService().post(getEvent());

			// this.setLastLogin(null);
			this.setLstLastLogin(this.getSqlService().dbRead(
					"select userdate from sdata_lastlogin where userid='"
							+ this.getCurrentSession().getUserId() + "'"));

			if (getLstLastLogin().size() != 0) {

				setLastLogin(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(getLstLastLogin().get(0).substring(0, 19)));

			}

			this.setMySites(this.getSiteService().getSites(
					SelectionType.ACCESS, null, null, null, SortType.TITLE_ASC,
					null));

			for (int i = 0; i < mySites.size(); i++) {
				getArlSiteId().add(mySites.get(i).getId());
			}

			getArlSiteId().add(
					this.getSiteService().getUserSiteId(
							this.getCurrentSession().getUserId()).substring(1));

			this.setSearchService((SearchService) this.getComponentManager()
					.get("org.sakaiproject.search.api.SearchService"));
			this.setContentHostingService((ContentHostingService) this
					.getComponentManager()
					.get("org.sakaiproject.content.api.ContentHostingService"));
			this
					.setAnnouncementService((AnnouncementService) this
							.getComponentManager()
							.get(
									"org.sakaiproject.announcement.api.AnnouncementService"));
			this.setEntityManager((EntityManager) this.getComponentManager()
					.get("org.sakaiproject.entity.api.EntityManager"));

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		log.warn("blah "
				+ this.getSearchService().search(
						"tool:content tool:announcement", this.getArlSiteId(),
						0, 50, null, "dateRelevanceSort"));

		// ////////
		// //////// DELETE INDEXED FILES FROM INDEX QUEUE
		// ////////

		searchList = this.getSearchService().search(
				"tool:content tool:announcement", this.getArlSiteId(), 0, 50,
				null, "dateRelevanceSort");
		log.warn(getSearchService().toString());
		log.warn(searchList);
		
		// this.setSearchResult( null );
		int ii = -1, iii = 0;
		do {
			ii += 1;
			if (ii < this.getSearchList().size()) {
				if (this.getSearchList().get(ii) == null) {
					break;
				} else {
					this.setSearchResult((SearchResult) this.getSearchList()
							.get(ii));
					if (this.getSearchResult().getId() != null
							&& !this.getSearchResult().getId().equals("")) {

						for (int r = 0; r < this.getSearchResult()
								.getFieldNames().length; r++) {
							String s = "";
							if (this.getSearchResult().getFieldNames()[r]
									.equals("indexdate")) {
								for (int y = 0; y < this.getSearchResult()
										.getValues(
												this.getSearchResult()
														.getFieldNames()[r]).length; y++) {
									s += this.getSearchResult().getValues(
											this.getSearchResult()
													.getFieldNames()[r])[y];
								}
								long l = Long.parseLong(s);
								s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
										.format(new java.util.Date(l));
								this.getSqlService().dbWrite(
										"delete from sdata_indexqueue where version<'"
												+ s + "'");
							}
						}

						break;
					}
				}
			} else {

				break;

			}
		} while ((this.getSearchResult().getId() == null || this
				.getSearchResult().getId().equals(""))
				&& ii < 50);
		
		// ////////
		// //////// GET INDEXED RESULTS FROM INDEXQUEUE
		// ////////

		String finalResult = "";

		int paging = 1;
		if (this.getCurrentpage() != null) {
			paging = Integer.parseInt(this.getCurrentpage());
		}

		int totalrecordsshown = 0;
		String sites = "";
		for (int i = 0; i < this.getArlSiteId().size(); i++) {
			if (i == 0) {
				sites += "context='" + this.getArlSiteId().get(i) + "' ";
			} else {
				sites += "OR context='" + this.getArlSiteId().get(i) + "' ";
			}
		}

		List<MyRecentChangesSqlresult> lst = this.getSqlService().dbRead(
				"select * from sdata_indexqueue where " + sites
						+ "order by version desc", null,
				new MyRecentChangesSqlreader());

		ArrayList<String> arlUsed = new ArrayList<String>();

		try {
			this.getMySites().add(
					this.getSiteService().getSite(
							this.getSiteService().getUserSiteId(
									currentSession.getUserId())));
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// BUILD JSON AND RETURN HERE

		// JSONSerializer jsl=new JSONSerializer();

		for (MyRecentChangesSqlresult mrcsr : lst) {
log.error("den for lus of doom");
			map.put("context", mrcsr.getContext());
			map.put("name", mrcsr.getName());
			map.put("tool", mrcsr.getTool());
			map.put("version", mrcsr.getVersion());
			myRecentResults.add(map);
		}

		map2.put("items", myRecentResults);

		// JSONArray jsonArray = JSONArray.fromObject(lst);

		// return jsonArray.toString();
	}

	public Map<String, Object> getResponseMap() {
		// TODO Auto-generated method stub
		return map2;
	}

	// GETTERS AND SETTERS
	public void setCurrentSession(Session currentSession) {
		this.currentSession = currentSession;
	}

	public Session getCurrentSession() {
		return currentSession;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setEventTrackingService(
			EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public EventTrackingService getEventTrackingService() {
		return eventTrackingService;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Event getEvent() {
		return event;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLstLastLogin(List<String> lstLastLogin) {
		this.lstLastLogin = lstLastLogin;
	}

	public List<String> getLstLastLogin() {
		return lstLastLogin;
	}

	public void setMySites(List<Site> mySites) {
		this.mySites = mySites;
	}

	public List<Site> getMySites() {
		return mySites;
	}

	public void setArlSiteId(ArrayList<String> arlSiteId) {
		this.arlSiteId = arlSiteId;
	}

	public ArrayList<String> getArlSiteId() {
		return arlSiteId;
	}

	public void setEntityManager(EntityManager entityManager) {
		entityManager = entityManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setAnnouncementService(AnnouncementService announcementService) {
		announcementService = announcementService;
	}

	public AnnouncementService getAnnouncementService() {
		return announcementService;
	}

	public void setContentHostingService(
			ContentHostingService contentHostingService) {
		contentHostingService = contentHostingService;
	}

	public ContentHostingService getContentHostingService() {
		return contentHostingService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchList(SearchList searchList) {
		searchList = searchList;
	}

	public SearchList getSearchList() {
		return searchList;
	}

	public void setSearchResult(SearchResult searchResult) {
		this.searchResult = searchResult;
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}

	public void setCurrentpage(String currentpage) {
		this.currentpage = currentpage;
	}

	public String getCurrentpage() {
		return currentpage;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setComponentManager(ComponentManager componentManager) {
		this.componentManager = componentManager;
	}

	public ComponentManager getComponentManager() {
		return componentManager;
	}

	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	public SqlService getSqlService() {
		return sqlService;
	}

	public void setMyRecentResults(List<Map> myRecentResults) {
		this.myRecentResults = myRecentResults;
	}

	public List<Map> getMyRecentResults() {
		return myRecentResults;
	}

}
