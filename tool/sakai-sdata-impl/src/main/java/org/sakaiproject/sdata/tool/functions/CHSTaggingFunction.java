/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/tool/sakai-sdata-impl/src/main/java/org/sakaiproject/sdata/tool/functions/CHSPropertiesFunction.java $
 * $Id: CHSPropertiesFunction.java 49164 2008-05-17 20:21:09Z ian@caret.cam.ac.uk $
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

package org.sakaiproject.sdata.tool.functions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.Kernel;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.SDataException;

/**
 * <p>
 * Get the list of tags on a site context.
 * </p>
 * <p>
 * <b>f=t</b>: Path  specifies the context.
 * </p>
 * <p>
 * <b>n</b>: The Name of the property
 * @author ieb
 */
public class CHSTaggingFunction extends CHSSDataFunction
{


	private static final String PROPERTY_NAME = "n";
	private CHSTagging chsTagging;

	
	public CHSTaggingFunction() {
		chsTagging = new CHSTagging();
		chsTagging.init();
	}
	
	@Override
	public void destroy() {
		chsTagging.destroy();
		super.destroy();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.api.SDataFunction#call(org.sakaiproject.sdata.tool.api.Handler,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.sakaiproject.sdata.tool.api.ResourceDefinition)
	 */
	public void call(Handler handler, HttpServletRequest request,
			HttpServletResponse response, Object target, ResourceDefinition rp)
			throws SDataException
	{
		SDataFunctionUtil.checkMethod(request.getMethod(), "GET");
		
		String propertyName = request.getParameter(PROPERTY_NAME);
		
		String path = rp.getRepositoryPath();
		if  ( !path.endsWith("/") ) {
			path = path+"/";
		}
		String[] parts = path.split("/");
		String context = parts[2];
				
		Map<String, Integer> distribution = chsTagging.getPropertyVector(context, propertyName);
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("context", context);
		result.put("name", propertyName);
		result.put("distribution", distribution);
		try
		{
			handler.sendMap(request, response, result);
		}
		catch (IOException e)
		{
			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"IO Error " + e.getMessage());
		}

	}


}
