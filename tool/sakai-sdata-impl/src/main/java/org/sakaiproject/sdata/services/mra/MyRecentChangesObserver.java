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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.Kernel;
import org.sakaiproject.db.api.SqlReader;
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

			Object[] params = new Object[4];
			// log.info("MySakai Received : " + event.getEvent());
			params[0] = new java.text.SimpleDateFormat(
			           "yyyy-MM-dd HH:mm:ss")
			        .format(new java.util.Date(System
					     .currentTimeMillis()));
			params[1] = event.getResource();
			
			if (event.getEvent().equals("user.login"))
			{

				if (sqlService.dbRead(
						"select * from sdata_lastlogin where userid= ? ",new Object[] { user }, new SqlReader() {

							public Object readSqlResultRecord(ResultSet result) {
								try {
									return result.getObject(1);
								} catch (SQLException e) {
									return null;
								}
							}
							
						})
						.size() == 0)
				{
					sqlService
							.dbWrite("insert into sdata_lastlogin values( ?,?,? )"
									,new Object[] {
										user,
										euser,
										new java.text.SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss")
										.format(new java.util.Date(System
												.currentTimeMillis()))
									});
				}
				else
				{
					sqlService
							.dbWrite("update sdata_lastlogin set userdate= ? where userid= ?"
									, new Object[] {
											new java.text.SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss")
											.format(new java.util.Date(System
													.currentTimeMillis())),
													user
									});
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
						params[2] = getEventContext(event.getResource(),"/content/group/");
						params[3] = "content";

					}
					else if (event.getResource().startsWith("/content/user/"))
					{

						params[2] = getEventContext(event.getResource(),"/content/user/");
						params[3] = "content";


					}

				}

			}
			else if (event.getEvent().startsWith("annc"))
			{

				if (event.getResource().startsWith("/announcement/msg/"))
				{

					params[2] = getEventContext(event.getResource(),"/announcement/msg/");
					params[3] = "announcement";
					
				}

			}
			if ( params[3] != null ) 
			{
			  sqlService
			    .dbWrite("insert into sdata_indexqueue (version, name, context, tool) " +
					"values( ?,?,?,?)", params);
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

	private String getEventContext(String resource, String type) {
		return resource.replace(type,
		"").substring(
				0,
				resource.replace(
						type, "").indexOf(
						"/"));
	}
}
