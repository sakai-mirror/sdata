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

package org.sakaiproject.sdata.tool.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author ieb
 */
public class TestAll extends TestCase
{

	/**
	 * @return
	 */
	public static Test suite()
	{
		TestSuite suite = new TestSuite("Test for org.sakaiproject.sdata.tool.test");
		// $JUnit-BEGIN$
		suite.addTestSuite(PathPrefixUnitT.class);
		suite.addTestSuite(ResourceDefinitionFactoryUnitT.class);
		suite.addTestSuite(UserResourceDefinitionFactoryUnitT.class);
		suite.addTestSuite(PathSecurityAssertionUnitT.class);
		suite.addTestSuite(ControllerServletUnitT.class);
		suite.addTestSuite(RFC1123DateUnitT.class);
		suite.addTestSuite(CHSHandlerUnitT.class);
		// $JUnit-END$
		return suite;
	}

}
