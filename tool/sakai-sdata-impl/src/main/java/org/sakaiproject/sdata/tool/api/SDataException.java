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

package org.sakaiproject.sdata.tool.api;

/**
 * Represents an exception with the SData server, that should be propagated back
 * to the client application
 * 
 * @author ieb
 */
public class SDataException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5097176656485907953L;
	private int code;

	/**
	 * Construct an exception with code and message
	 * 
	 * @param sc_unauthorized
	 * @param string
	 */
	public SDataException(int code, String string)
	{
		super(string);
		this.code = code;
	}

	/**
	 * The status code of the exception (http)
	 * 
	 * @return
	 */
	public int getCode()
	{
		return code;
	}

}
