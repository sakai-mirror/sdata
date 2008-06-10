/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.sdata.services.col;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.sakaiproject.sdata.tool.json.JSONServiceHandler;

/**
 * A collections handler for CHS that will generate a listing of a collection in JSON form.
 * @author ieb
 */
public class ColCHSHandler extends JSONServiceHandler
{

	/**
	 * Seialization version number
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.json.JSONServiceServlet#getServiceDefinitionFactory()
	 */
	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory()
			throws ServletException
	{
		ColCHSServiceDefinitionFactory col = new ColCHSServiceDefinitionFactory();
		col.init(new HashMap<String, String>());
		return col;
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
		ColCHSServiceDefinitionFactory col = new ColCHSServiceDefinitionFactory();
		col.init(config);
		return col;
	}

}
