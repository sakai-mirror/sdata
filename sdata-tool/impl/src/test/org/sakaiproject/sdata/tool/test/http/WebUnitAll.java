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

package org.sakaiproject.sdata.tool.test.http;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author ieb
 */
public class WebUnitAll extends TestCase
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.sakaiproject.sdata.tool");
		// $JUnit-BEGIN$
		suite.addTestSuite(JsonJcrServletUnitT.class);
		suite.addTestSuite(JsonUserStorageServletUnitT.class);
		suite.addTestSuite(XmlRpcJcrServletUnitT.class);
		suite.addTestSuite(XmlRpcUserStorageServletUnitT.class);
		suite.addTestSuite(HttpRangeUnitT.class);
		// $JUnit-END$
		return suite;
	}

}
