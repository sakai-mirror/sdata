package org.sakaiproject.sdata.services.mra;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.sdata.tool.api.ServiceDefinition;
import org.sakaiproject.search.api.SearchList;
import org.sakaiproject.search.api.SearchResult;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

//import net.sf.json.JSONObject;
//import net.sf.json.JSONSerializer;

public class MyRecentChangesBean implements ServiceDefinition
{

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

	private MessageChannel messageChannel;

	private AnnouncementMessage announcementMessage;

	private List<MyRecentChangesResult> results = new ArrayList<MyRecentChangesResult>();

	private static final Log log = LogFactory.getLog(MyRecentChangesBean.class);

	public MyRecentChangesBean(SessionManager sessionManager, SiteService siteService,
			ComponentManager componentManager, SqlService sqlService,
			SearchService searchService, ContentHostingService contentHostingService,
			AnnouncementService announcementService, EntityManager entityManager,
			int paging)
	{
		this.setAnnouncementService(announcementService);
		this.setEntityManager(entityManager);
		this.contentHostingService = contentHostingService;
		this.setSearchService(searchService);
		this.setSqlService(sqlService);
		this.setComponentManager(componentManager);
		this.setSiteService(siteService);
		this.setSessionManager(sessionManager);
		this.setCurrentSession(getSessionManager().getCurrentSession());
		this.setCurrentUser(getCurrentSession().getUserId());

		log.info("Sessie = " + currentSession);
		log.info("User = " + currentUser);

		try
		{
			search(paging);
		}
		catch (PermissionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IdUnusedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (TypeException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	} // END CONSTRUCTOR

	public void search(int paging) throws PermissionException, IdUnusedException,
			TypeException, ParseException
	{

		try
		{

			currentSession = getSessionManager().getCurrentSession();
			currentUser = currentSession.getUserId();

			// log.error(currentSession);
			// log.error(currentUser);

			// setEventTrackingService(org.sakaiproject.event.cover.EventTrackingService.getInstance());
			// setEvent(getEventTrackingService().newEvent("octopus.recent.activity",
			// null, true));

			// post the event
			// getEventTrackingService().post(getEvent());

			// this.setLastLogin(null);
			lstLastLogin = sqlService
					.dbRead("select userdate from sdata_lastlogin where userid='"
							+ currentSession.getUserId() + "'");

			if (lstLastLogin.size() != 0)
			{

				lastLogin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(lstLastLogin.get(0).substring(0, 19));

			}

			mySites = siteService.getSites(SelectionType.ACCESS, null, null, null,
					SortType.TITLE_ASC, null);

			for (int i = 0; i < mySites.size(); i++)
			{
				arlSiteId.add(mySites.get(i).getId());
			}

			arlSiteId.add(siteService.getUserSiteId(currentSession.getUserId())
					.substring(1));

			// searchService = (SearchService) getComponentManager().get(
			// "org.sakaiproject.search.api.SearchService");
			// contentHostingService = (ContentHostingService) componentManager
			// .get("org.sakaiproject.content.api.ContentHostingService");
			// announcementService = (AnnouncementService) componentManager
			// .get("org.sakaiproject.announcement.api.AnnouncementService");
			// entityManager = (EntityManager) componentManager
			// .get("org.sakaiproject.entity.api.EntityManager");

		}
		catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
		/*
		 * log.warn("blah " + this.getSearchService().search("tool:content
		 * tool:announcement", this.getArlSiteId(), 0, 50, null,
		 * "dateRelevanceSort"));
		 */

		// ////////
		// //////// DELETE INDEXED FILES FROM INDEX QUEUE
		// ////////
		searchList = searchService.search("tool:content tool:announcement", arlSiteId, 0,
				50, null, "dateRelevanceSort");
		// log.warn(getSearchService().toString());
		// log.warn(searchList);

		// this.setSearchResult( null );
		int ii = -1, iii = 0;
		do
		{
			ii += 1;
			if (ii < searchList.size())
			{
				if (searchList.get(ii) == null)
				{
					break;
				}
				else
				{
					searchResult = ((SearchResult) searchList.get(ii));
					if (searchResult.getId() != null && !searchResult.getId().equals(""))
					{

						for (int r = 0; r < searchResult.getFieldNames().length; r++)
						{
							String s = "";
							if (searchResult.getFieldNames()[r].equals("indexdate"))
							{
								for (int y = 0; y < searchResult.getValues(searchResult
										.getFieldNames()[r]).length; y++)
								{
									s += searchResult.getValues(searchResult
											.getFieldNames()[r])[y];
								}
								long l = Long.parseLong(s);
								s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
										.format(new java.util.Date(l));
								sqlService
										.dbWrite("delete from sdata_indexqueue where version<'"
												+ s + "'");
							}
						}

						break;
					}
				}
			}
			else
			{

				break;

			}
		}

		while ((searchResult.getId() == null || searchResult.getId().equals(""))
				&& ii < 50);

		// ////////
		// //////// GET INDEXED RESULTS FROM INDEXQUEUE
		// ////////

		String finalResult = "";

		int totalrecordsshown = 0;
		String sites = "";
		for (int i = 0; i < arlSiteId.size(); i++)
		{
			if (i == 0)
			{
				sites += "context='" + arlSiteId.get(i) + "' ";
			}
			else
			{
				sites += "OR context='" + arlSiteId.get(i) + "' ";
			}
		}

		List<MyRecentChangesSqlresult> lst = getSqlService()
				.dbRead(
						"select * from sdata_indexqueue where " + sites
								+ "order by version desc", null,
						new MyRecentChangesSqlreader());

		ArrayList<String> arlUsed = new ArrayList<String>();

		// BEGIN NEW STUFF

		for (int i = 0; i < lst.size(); i++)
		{

			MyRecentChangesSqlresult mres = lst.get(i);

			if (mres.getTool().equals("content"))
			{
				String eid = mres.getName().substring(8);
				

				log.info("eid is " + eid);
				

				log.info("contenthostingservice is " + contentHostingService.toString());
				if (!contentHostingService.isCollection(eid))
				{

					if (contentHostingService.allowGetResource(eid)
							&& !arlUsed.contains(mres.getName()))
					{

						if (totalrecordsshown >= (paging - 1) * 10
								&& totalrecordsshown < (paging) * 10)
						{

							ContentResource cres = contentHostingService.getResource(eid);

							Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.parse(mres.getVersion().substring(0, 19));

							MyRecentChangesResult mrcs = new MyRecentChangesResult();

							for (Site s : mySites)
							{
								//TODO
								if (mres.getContext().equals(s.getId()))
								{

									mrcs.setSitename(s.getTitle());

								}
							}

							mrcs.setTool(mres.getTool());
							mrcs.setVersion(mres.getVersion());
							mrcs.setContext(mres.getContext());
							mrcs.setName(cres.getUrl());
							mrcs.setReference(cres.getReference());
							results.add(mrcs);

						}

						totalrecordsshown += 1;
						arlUsed.add(mres.getName());
						if (totalrecordsshown == 50)
						{
							break;
						}

					}
				}
			}
			else if (mres.getTool().equals("announcement"))
			{

				try
				{

					if (!arlUsed.contains(mres.getName()))
					{

						messageChannel = announcementService
								.getChannel("/announcement/channel/" + mres.getContext()
										+ "/main");
						announcementMessage = (AnnouncementMessage) messageChannel
								.getMessage(mres.getName().substring(
										mres.getName().lastIndexOf('/') + 1));

						if (!announcementMessage.getAnnouncementHeader().getDraft())
						{

							if (totalrecordsshown >= (paging - 1) * 10
									&& totalrecordsshown < (paging) * 10)
							{

								Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
										.parse(mres.getVersion().substring(0, 19));

								MyRecentChangesResult mrcs = new MyRecentChangesResult();

								for (Site s : mySites)
								{
									//TODO
									if (mres.getContext().equals(s.getId()))
									{

										mrcs.setSitename(s.getTitle());

									}
								}

								mrcs.setTool(mres.getTool());
								mrcs.setVersion(mres.getVersion());
								mrcs.setContext(mres.getContext());
								mrcs.setName(announcementMessage.getUrl());
								mrcs.setReference(announcementMessage.getReference());
								results.add(mrcs);

							}

							totalrecordsshown += 1;
							arlUsed.add(mres.getName());
							if (totalrecordsshown == 50)
							{
								break;
							}

						}

					}

				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}

			}

		}

		// ////////
		// //////// FILL WITH INDEXED RESULTS
		// ////////

		if (totalrecordsshown < 50)
		{

			ii = -1;
			iii = 0;
			do
			{
				ii += 1;

				if (ii < searchList.size())
				{
					if (searchList.get(ii) == null)
					{
						break;
					}
					else
					{
						SearchResult srl = (SearchResult) searchList.get(ii);

						if (srl.getId() != null && !srl.getId().equals("")
								&& !arlUsed.contains(srl.getReference()))
						{
							log.error("+++++ " + srl.getTool());
							if (srl.getTool().equals("content"))
							{

								if (totalrecordsshown >= (paging - 1) * 10
										&& totalrecordsshown < (paging) * 10)
								{

									long l = 0;
									String s = "";
									String s2 = "";
									for (int r = 0; r < srl.getFieldNames().length; r++)
									{
										if (srl.getFieldNames()[r].equals("siteid"))
										{
											for (int y = 0; y < srl.getValues(srl
													.getFieldNames()[r]).length; y++)
											{
												s += srl
														.getValues(srl.getFieldNames()[r])[y];
											}
										}
										else if (srl.getFieldNames()[r]
												.equals("indexdate"))
										{
											for (int y = 0; y < srl.getValues(srl
													.getFieldNames()[r]).length; y++)
											{
												s2 += srl
														.getValues(srl.getFieldNames()[r])[y];
											}
											l = Long.parseLong(s2);
											s2 = new SimpleDateFormat(
													"yyyy-MM-dd HH:mm:ss")
													.format(new java.util.Date(l));
										}
									}

									Date d = new java.util.Date(l);

									MyRecentChangesResult mrcs = new MyRecentChangesResult();

									mrcs.setName(srl.getTitle());

									for (Site ss : mySites)
									{
										// log.error("BIER :: " + s);
										if (s.equals(ss.getId()))
										{

											mrcs.setSitename(ss.getTitle());
											mrcs.setContext(ss.getId());
										}
									}

									mrcs.setTool(srl.getTool());
									mrcs.setVersion(s2);
									mrcs.setReference(srl.getReference());
									results.add(mrcs);

								}

								totalrecordsshown += 1;
								arlUsed.add(srl.getReference());

							}
							else if (srl.getTool().equals("announcement"))
							{

								if (totalrecordsshown >= (paging - 1) * 10
										&& totalrecordsshown < (paging) * 10)
								{

									long l = 0;
									String s = "";
									String s2 = "";
									for (int r = 0; r < srl.getFieldNames().length; r++)
									{
										if (srl.getFieldNames()[r].equals("siteid"))
										{
											for (int y = 0; y < srl.getValues(srl
													.getFieldNames()[r]).length; y++)
											{
												s += srl
														.getValues(srl.getFieldNames()[r])[y];
											}
										}
										else if (srl.getFieldNames()[r]
												.equals("indexdate"))
										{
											for (int y = 0; y < srl.getValues(srl
													.getFieldNames()[r]).length; y++)
											{
												s2 += srl
														.getValues(srl.getFieldNames()[r])[y];
											}
											l = Long.parseLong(s2);
											s2 = new SimpleDateFormat(
													"yyyy-MM-dd HH:mm:ss")
													.format(new java.util.Date(l));
										}
									}

									Date d = new java.util.Date(l);

									String[] arr = srl.getTitle().substring(9).split(
											" From ");
									String arrResult = "";
									for (int i = 0; i < arr.length - 1; i++)
									{
										arrResult += arr[i];
									}

									MyRecentChangesResult mrcs = new MyRecentChangesResult();
									mrcs.setContext(s);
									mrcs.setName(srl.getTitle());

									for (Site ss : mySites)
									{
										if (mrcs.getContext().equals(ss.getId()))
										{

											mrcs.setSitename(ss.getTitle());

										}
									}

									mrcs.setTool(srl.getTool());
									mrcs.setVersion(s2);
									mrcs.setReference(srl.getReference());
									results.add(mrcs);

								}

								totalrecordsshown += 1;
								arlUsed.add(searchResult.getReference());

							}

						}
					}
				}
				else
				{

					break;

				}
			}
			while (totalrecordsshown < 50 && ii < 50);

		}

		// END NEW STUFF

		for (MyRecentChangesResult mrcsr : results)
		{

			Map map = new HashMap<String, Object>();
			map.put("siteName", mrcsr.getSitename());
			map.put("context", mrcsr.getContext());
			map.put("name", mrcsr.getName());
			map.put("tool", mrcsr.getTool());
			map.put("version", mrcsr.getVersion());
			map.put("reference", mrcsr.getReference());
			myRecentResults.add(map);

		}
		
		
		if(lastLogin == null){
			//TODO
			map2.put("lastLogin", "NO USER LOGGED IN");
			
		}else{
			
			map2.put("lastLogin", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
			.format(lastLogin));
		}
		
		map2.put("total", totalrecordsshown);

		map2.put("items", myRecentResults);

		// JSONArray jsonArray = JSONArray.fromObject(lst);

		// return jsonArray.toString();
	}

	public Map<String, Object> getResponseMap()
	{
		// TODO Auto-generated method stub
		return map2;
	}

	// GETTERS AND SETTERS
	public void setCurrentSession(Session currentSession)
	{
		this.currentSession = currentSession;
	}

	public Session getCurrentSession()
	{
		return currentSession;
	}

	public void setCurrentUser(String currentUser)
	{
		this.currentUser = currentUser;
	}

	public String getCurrentUser()
	{
		return currentUser;
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService)
	{
		this.eventTrackingService = eventTrackingService;
	}

	public EventTrackingService getEventTrackingService()
	{
		return eventTrackingService;
	}

	public void setEvent(Event event)
	{
		this.event = event;
	}

	public Event getEvent()
	{
		return event;
	}

	public void setLastLogin(Date lastLogin)
	{
		this.lastLogin = lastLogin;
	}

	public Date getLastLogin()
	{
		return lastLogin;
	}

	public void setLstLastLogin(List<String> lstLastLogin)
	{
		this.lstLastLogin = lstLastLogin;
	}

	public List<String> getLstLastLogin()
	{
		return lstLastLogin;
	}

	public void setMySites(List<Site> mySites)
	{
		this.mySites = mySites;
	}

	public List<Site> getMySites()
	{
		return mySites;
	}

	public void setArlSiteId(ArrayList<String> arlSiteId)
	{
		this.arlSiteId = arlSiteId;
	}

	public ArrayList<String> getArlSiteId()
	{
		return arlSiteId;
	}

	public void setEntityManager(EntityManager entityManager)
	{
		entityManager = entityManager;
	}

	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	public void setAnnouncementService(AnnouncementService announcementService)
	{
		announcementService = announcementService;
	}

	public AnnouncementService getAnnouncementService()
	{
		return announcementService;
	}

	public void setContentHostingService(ContentHostingService contentHostingService)
	{
		contentHostingService = contentHostingService;
	}

	public ContentHostingService getContentHostingService()
	{
		return contentHostingService;
	}

	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	public SearchService getSearchService()
	{
		return searchService;
	}

	public void setSearchList(SearchList searchList)
	{
		searchList = searchList;
	}

	public SearchList getSearchList()
	{
		return searchList;
	}

	public void setSearchResult(SearchResult searchResult)
	{
		this.searchResult = searchResult;
	}

	public SearchResult getSearchResult()
	{
		return searchResult;
	}

	public void setCurrentpage(String currentpage)
	{
		this.currentpage = currentpage;
	}

	public String getCurrentpage()
	{
		return currentpage;
	}

	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public SiteService getSiteService()
	{
		return siteService;
	}

	public void setComponentManager(ComponentManager componentManager)
	{
		this.componentManager = componentManager;
	}

	public ComponentManager getComponentManager()
	{
		return componentManager;
	}

	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}

	public SqlService getSqlService()
	{
		return sqlService;
	}

	public void setMyRecentResults(List<Map> myRecentResults)
	{
		this.myRecentResults = myRecentResults;
	}

	public List<Map> getMyRecentResults()
	{
		return myRecentResults;
	}

	public void setMessageChannel(MessageChannel messageChannel)
	{
		this.messageChannel = messageChannel;
	}

	public MessageChannel getMessageChannel()
	{
		return messageChannel;
	}

	public void setAnnouncementMessage(AnnouncementMessage announcementMessage)
	{
		this.announcementMessage = announcementMessage;
	}

	public AnnouncementMessage getAnnouncementMessage()
	{
		return announcementMessage;
	}

	public void setResults(List<MyRecentChangesResult> results)
	{
		this.results = results;
	}

	public List<MyRecentChangesResult> getResults()
	{
		return results;
	}

}
