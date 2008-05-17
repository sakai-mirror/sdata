/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Timefields Ltd
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

package org.sakaiproject.sdata.tool.test.http;


/**
 * @author ieb
 */

/**
 * @author ieb
 */
public class JsonJcrUserStorageHandlerUnitT extends JsonUserStorageHandlerUnitT
{

	private static final String BASE_URL = "http://localhost:8080/sdata/";

	private static final String BASE_DATA_URL = BASE_URL + "p/";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.test.http.JsonUserStorageServletUnitT#getBaseUrl()
	 */
	@Override
	protected String getBaseUrl()
	{
		return BASE_URL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.test.http.JsonUserStorageServletUnitT#getBaseDataUrl()
	 */
	@Override
	protected String getBaseDataUrl()
	{
		return BASE_DATA_URL;
	}

}
