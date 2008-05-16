/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/sdata-tool/impl/src/java/org/sakaiproject/sdata/tool/JCRDumper.java $
 * $Id: JCRDumper.java 45207 2008-02-01 19:01:06Z ian@caret.cam.ac.uk $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
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

package org.sakaiproject.sdata.services.mra;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.Kernel;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.UsageSession;

/**
 * TODO Javadoc
 * 
 * @author
 */
public class MyRecentChangesObserver implements Observer
{

	private static final Log log = LogFactory.getLog(MyRecentChangesObserver.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable arg0, Object arg1)
	{
		// TODO Auto-generated method stub

		try
		{

			Event event = (org.sakaiproject.event.api.Event) arg1;
			UsageSession session = Kernel.usageSessionService().getSession(
					event.getSessionId());
			SqlService sqlService = Kernel.sqlService();
			String user = session.getUserId();
			String euser = session.getUserEid();

			// log.info("MySakai Received : " + event.getEvent());
			if (event.getEvent().equals("user.login"))
			{

				if (sqlService.dbRead(
						"select * from sdata_lastlogin where userid='" + user + "'")
						.size() == 0)
				{
					sqlService
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
					sqlService
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

						sqlService
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

						sqlService
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

					sqlService
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
			if (log.isDebugEnabled())
			{
				log.error(ex.getMessage());
			}

		}

	}
}
