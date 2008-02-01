package org.sakaiproject.sdata.services.mra;

import java.util.Observable;
import java.util.Observer;

public class MyRecentChangesObserver implements Observer
{

	public void update(Observable arg0, Object arg1)
	{
		// TODO Auto-generated method stub

		try
		{

			org.sakaiproject.event.api.Event event = (org.sakaiproject.event.api.Event) arg1;
			org.sakaiproject.event.api.UsageSession session = org.sakaiproject.event.cover.UsageSessionService
					.getSession(event.getSessionId());
			String user = session.getUserId();
			String euser = session.getUserEid();

			// log.info("MySakai Received : " + event.getEvent());
			if (event.getEvent().equals("user.login"))
			{

				if (org.sakaiproject.db.cover.SqlService.dbRead(
						"select * from sdata_lastlogin where userid='" + user + "'")
						.size() == 0)
				{
					org.sakaiproject.db.cover.SqlService
							.dbWrite("insert into sdata_lastlogin values('"
									+ user
									+ "','"
									+ euser
									+ "','"
									+ new java.text.SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss")
											.format(new java.util.Date(System
													.currentTimeMillis())) + "') ");
				}
				else
				{
					org.sakaiproject.db.cover.SqlService
							.dbWrite("update sdata_lastlogin set userdate='"
									+ new java.text.SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss")
											.format(new java.util.Date(System
													.currentTimeMillis()))
									+ "' where userid='" + user + "'");
				}
				// log.info("MySakai Last Login Updated for " + user);

			}
			else if (event.getEvent().startsWith("content")
					&& !event.getEvent().equals("content.delete")
					&& !event.getEvent().equals("content.read"))
			{

				if (!event.getResource().endsWith("/"))
				{

					if (event.getResource().startsWith("/content/group/"))
					{

						org.sakaiproject.db.cover.SqlService
								.dbWrite("insert into sdata_indexqueue (version, name, context, tool) values('"
										+ new java.text.SimpleDateFormat(
												"yyyy-MM-dd HH:mm:ss")
												.format(new java.util.Date(System
														.currentTimeMillis()))
										+ "','"
										+ event.getResource()
										+ "','"
										+ event.getResource().replace("/content/group/",
												"").substring(
												0,
												event.getResource().replace(
														"/content/group/", "").indexOf(
														"/")) + "','content')");

					}
					else if (event.getResource().startsWith("/content/user/"))
					{

						org.sakaiproject.db.cover.SqlService
								.dbWrite("insert into sdata_indexqueue (version, name, context, tool) values('"
										+ new java.text.SimpleDateFormat(
												"yyyy-MM-dd HH:mm:ss")
												.format(new java.util.Date(System
														.currentTimeMillis()))
										+ "','"
										+ event.getResource()
										+ "','"
										+ event.getResource().replace("/content/user/",
												"").substring(
												0,
												event.getResource().replace(
														"/content/user/", "")
														.indexOf("/")) + "','content')");

					}

				}

			}
			else if (event.getEvent().startsWith("annc"))
			{

				if (event.getResource().startsWith("/announcement/msg/"))
				{

					org.sakaiproject.db.cover.SqlService
							.dbWrite("insert into sdata_indexqueue (version, name, context, tool) values('"
									+ new java.text.SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss")
											.format(new java.util.Date(System
													.currentTimeMillis()))
									+ "','"
									+ event.getResource()
									+ "','"
									+ event.getResource().replace("/announcement/msg/",
											"").substring(
											0,
											event.getResource().replace(
													"/announcement/msg/", "")
													.indexOf("/")) + "','announcement')");

				}

			}

		}
		catch (Exception ex)
		{

			// log.error(ex.getMessage());

		}

	}
}
