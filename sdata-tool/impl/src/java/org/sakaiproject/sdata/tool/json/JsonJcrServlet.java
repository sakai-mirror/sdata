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

package org.sakaiproject.sdata.tool.json;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.sakaiproject.sdata.tool.JCRServlet;
import org.sakaiproject.sdata.tool.api.SDataException;

/**
 * A JCRServlet that serializes responses using JSON
 * 
 * @author ieb
 */
public class JsonJcrServlet extends JCRServlet
{

	/**
	 * 
	 */
	public JsonJcrServlet()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.JCRServlet#sendMap(java.util.Map)
	 */
	@Override
	protected void sendMap(HttpServletRequest request, HttpServletResponse response,
			Map<String, Object> contetMap) throws IOException
	{
		JSONObject jsonObject = JSONObject.fromObject(contetMap);
		PrintWriter w = response.getWriter();
		w.write(jsonObject.toString());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.JCRServlet#sendError(java.lang.Throwable)
	 */
	@Override
	protected void sendError(HttpServletRequest request, HttpServletResponse response,
			Throwable ex) throws IOException
	{
		if (ex instanceof SDataException)
		{
			SDataException sde = (SDataException) ex;
			response.reset();
			response.sendError(sde.getCode(), sde.getMessage());
		}
		else
		{
			response.reset();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed with " + ex.getMessage());
		}
	}

}
