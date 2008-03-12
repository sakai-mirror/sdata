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

package org.sakaiproject.sdata.tool.functions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.api.SDataFunction;

/**
 * @author ieb
 */
public class JCRNodeMetadata implements SDataFunction
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.sdata.tool.api.SDataFunction#call(org.sakaiproject.sdata.tool.api.Handler,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object)
	 */
	public void call(Handler handler, HttpServletRequest request,
			HttpServletResponse response, Object target) throws SDataException
	{
		try
		{
			Node n = (Node) target;
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("primaryNodeType", n.getPrimaryNodeType().getName());
			m.put("mixinNodeType", getMixinTypes(n));
			m.put("properties", getProperties(n));
			handler.sendMap(request, response, m);
		}
		catch (RepositoryException rex)
		{
			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, rex
					.getMessage());
		}
		catch (IOException e)
		{
			throw new SDataException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
					.getMessage());
		}

	}

	/**
	 * @param n
	 * @return
	 * @throws RepositoryException
	 */
	private Map<String, Object> getProperties(Node n) throws RepositoryException
	{
		Map<String, Object> m = new HashMap<String, Object>();
		for (PropertyIterator pi = n.getProperties(); pi.hasNext();)
		{
			Property p = pi.nextProperty();
			String name = p.getName();
			boolean multiValue = p.getDefinition().isMultiple();
			if (multiValue)
			{
				Value[] v = p.getValues();
				Object[] o = new String[v.length];
				for (int i = 0; i < o.length; i++)
				{
					o[i] = formatType(v[i]);
				}
				m.put(name,o);
			}
			else
			{
				Value v = p.getValue();
				m.put(name,formatType(v));

			}
		}
		return m;
	}

	/**
	 * @param value
	 * @return
	 * @throws RepositoryException 
	 */
	private Object formatType(Value value) throws RepositoryException
	{
		switch (value.getType())
		{
			case PropertyType.BOOLEAN:
				return String.valueOf(value.getBoolean());
			case PropertyType.BINARY:
				return "--binary--";
			case PropertyType.DATE:
				return value.getDate().getTime();
			case PropertyType.DOUBLE:
				return String.valueOf(value.getDouble());
			case PropertyType.LONG:
				return String.valueOf(value.getLong());
			case PropertyType.NAME:
			case PropertyType.PATH:
			case PropertyType.REFERENCE:
			case PropertyType.STRING:
				return value.getString();
			default:
				return "--undefined--";
		}
	}

	/**
	 * @param n
	 * @return
	 * @throws RepositoryException
	 */
	private String[] getMixinTypes(Node n) throws RepositoryException
	{
		List<String> mixins = new ArrayList<String>();
		for (NodeType nt : n.getMixinNodeTypes())
		{
			mixins.add(nt.getName());
		}
		return mixins.toArray(new String[0]);
	}

}
