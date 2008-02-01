package org.sakaiproject.sdata.tool.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.sakaiproject.sdata.tool.ServiceServlet;
import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.xml.sax.SAXException;

import net.sf.json.JSONObject;

public class JSONServiceServlet extends ServiceServlet {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory()
			throws ServletException {
		throw new ServletException("No Default ServiceDefinitionFactory");
	}

	@Override
	protected void sendError(HttpServletRequest request,
			HttpServletResponse response, Throwable ex) throws IOException {
	/*	if (ex instanceof SDataException)
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
		}		*/
		ex.printStackTrace();
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"BOE ++++++===+++ " + ex.getMessage());
	}

	@Override
	protected void sendMap(HttpServletRequest request,
			HttpServletResponse response, Map<String, Object> contentMap)
			throws IOException {
		JSONObject jsonObject = JSONObject.fromObject(contentMap);
		PrintWriter w = response.getWriter();
		w.write(jsonObject.toString());	

	}
	

}
