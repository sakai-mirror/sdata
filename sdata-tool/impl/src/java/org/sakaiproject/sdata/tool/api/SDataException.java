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
 * TODO javadoc
 * 
 * @author ieb
 */
public class SDataException extends Exception
{

	private int code;

	/**
	 * TODO javadoc
	 */
	public SDataException()
	{
	}

	/**
	 * TODO javadoc
	 * 
	 * @param arg0
	 */
	public SDataException(String arg0)
	{
		super(arg0);
	}

	/**
	 * TODO javadoc
	 * 
	 * @param arg0
	 */
	public SDataException(Throwable arg0)
	{
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public SDataException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	/**
	 * TODO javadoc
	 * 
	 * @param sc_unauthorized
	 * @param string
	 */
	public SDataException(int code, String string)
	{
		super(string);
		this.code = code;
	}

	public int getCode()
	{
		return code;
	}

}
