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

import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.Kernel;

/**
 * TODO Javadoc
 * 
 * @author
 */
public class MyRecentChangesDbBean implements ServletContextListener
{

	public static final MyRecentChangesObserver obs = new MyRecentChangesObserver();

	private static final Log log = LogFactory.getLog(MyRecentChangesDbBean.class);

	/**
	 * TODO Javadoc
	 */
	public void init()
	{

		log.info("sData Widget Data Service Initializing ...");

		try
		{
			boolean exist = false;
			boolean exist2 = false;
			List lsts = null;
			lsts = Kernel.sqlService().dbRead("SHOW TABLES");

			for (int i = 0; i < lsts.size(); i++)
			{

				if (lsts.get(i).toString().equals("sdata_lastlogin"))
				{

					exist = true;
					// log.error("tables exist");
				}
				else if (lsts.get(i).toString().equals("sdata_indexqueue"))
				{
					exist2 = true;
					// log.error("tables exist");
				}

			}

			if (exist == false)
			{
				Kernel
						.sqlService()
						.dbWrite(
								"create table sdata_lastlogin (userid varchar(255) not null, usereid varchar(255) not null, userdate timestamp not null, primary key(userid));");
				// log.info("MySakai Login Table Added");
				// log.info("MySakai Index Queue Table Added");

			}
			if (exist2 == false)
			{
				Kernel
						.sqlService()
						.dbWrite(
								"create table sdata_indexqueue (id int not null AUTO_INCREMENT, version timestamp not null, name varchar(255) not null, context varchar(255) not null, tool varchar(255) not null, primary key  (id));");

			}

		}
		catch (Exception ex)
		{

			if (log.isDebugEnabled())
			{
				log.debug("alrdy created");
			}

		}

		// log.info("MySakai Observer added");
		Kernel.eventTrackingService().addObserver(obs);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0)
	{
		Kernel.eventTrackingService().deleteObserver(obs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent arg0)
	{
		init();

	}

}
