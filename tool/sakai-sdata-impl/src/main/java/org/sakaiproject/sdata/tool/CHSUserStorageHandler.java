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

package org.sakaiproject.sdata.tool;

import java.util.Map;

import org.sakaiproject.sdata.tool.api.ResourceDefinitionFactory;
import org.sakaiproject.sdata.tool.util.UserResourceDefinitionFactory;

/**
 * A user storage servlet performs storage based on the logged in user, as
 * defined by the Sakai session. It uses the UserResourceDefinitionFactory to
 * locate the location of the users storage within the underlying chs
 * repository. This servlet extends the CHSServlet and uses its methods and
 * handling to respond to the content.
 * 
 * @author ieb
 */
public abstract class CHSUserStorageHandler extends CHSHandler
{

	/**
	 * Create a User storage handler
	 */
	public CHSUserStorageHandler()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.JCRServlet#getResourceDefinitionFactory()
	 */
	@Override
	protected ResourceDefinitionFactory getResourceDefinitionFactory(
			Map<String, String> config)
	{
		return new UserResourceDefinitionFactory(getBasePath());
	}

}
