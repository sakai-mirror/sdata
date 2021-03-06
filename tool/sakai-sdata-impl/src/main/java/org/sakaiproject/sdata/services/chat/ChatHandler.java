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

package org.sakaiproject.sdata.services.chat;

import java.util.Map;

import javax.servlet.ServletException;

import org.sakaiproject.Kernel;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.sakaiproject.sdata.tool.json.JSONServiceHandler;

/**
 * The Handler for recent changes.
 * 
 * @author
 */
public class ChatHandler extends JSONServiceHandler
{

	private static final long serialVersionUID = 1L;
	private EventTrackingService eventTrackingService;
	private ChatObserver observer;

    public ChatHandler() {
    	observer = new ChatObserver();
    	eventTrackingService = Kernel.eventTrackingService();
    	eventTrackingService.addLocalObserver(observer);
    }	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.json.JSONServiceServlet#getServiceDefinitionFactory()
	 */
	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory()
			throws ServletException
	{
		return new ChatServiceDefinitionFactory();
	}
	
	@Override
	public void init(Map<String, String> config) throws ServletException {
		super.init(config);
	}
	@Override
	public void destroy() {
		eventTrackingService.deleteObserver(observer);
		super.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.ServiceServlet#getServiceDefinitionFactory(javax.servlet.ServletConfig)
	 */
	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory(
			Map<String, String> config) throws ServletException
	{
		return new ChatServiceDefinitionFactory();
	}

}
