package org.sakaiproject.sdata.tool.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.serializer.DefaultXMLWriterFactory;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.apache.xmlrpc.serializer.XmlWriterFactory;
import org.sakaiproject.sdata.tool.ServiceServlet;
import org.sakaiproject.sdata.tool.api.ServiceDefinitionFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class XmlRPCServiceServlet extends ServiceServlet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(XmlRPCServiceServlet.class);

	private XmlWriterFactory writerFactory = new DefaultXMLWriterFactory();

	private XmlRpcStreamRequestConfig pConfig = new XmlRpcStreamRequestConfigImpl();

	private TypeFactory typeFactory = new XmlRpcTypeFactory(pConfig);

	@Override
	protected ServiceDefinitionFactory getServiceDefinitionFactory()
			throws ServletException
	{
		throw new ServletException("No Default ServiceDefinitionFactory");
	}

	@Override
	protected void sendError(HttpServletRequest request, HttpServletResponse response,
			Throwable ex) throws IOException
	{
		/*
		 * if (ex instanceof SDataException) { SDataException sde =
		 * (SDataException) ex; response.reset();
		 * response.sendError(sde.getCode(), sde.getMessage()); } else {
		 * response.reset();
		 * response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		 * "Failed with " + ex.getMessage()); }
		 */

		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	@Override
	protected void sendMap(HttpServletRequest request, HttpServletResponse response,
			Map<String, Object> contentMap) throws IOException
	{
		XmlRpcWriter xw;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			xw = getXmlRpcWriter(pConfig, baos);
		}
		catch (XmlRpcException e)
		{
			log.error("Failed to get RpcWriter ", e);
			throw new IOException("Failed to get RpcWriter  " + e.getMessage());
		}
		try
		{
			xw.write(pConfig, contentMap);
			baos.flush();
			byte[] out = baos.toByteArray();
			baos.close();

			response.setContentLength(out.length);
			response.setContentType("text/xml");
			response.setCharacterEncoding("UTF-8");
			response.getOutputStream().write(out);
		}
		catch (SAXException e)
		{
			log.error("Failed to write response ", e);
			throw new IOException("Failed to write response " + e.getMessage());
		}

	}

	protected XmlRpcWriter getXmlRpcWriter(XmlRpcStreamRequestConfig pConfig,
			OutputStream pStream) throws XmlRpcException
	{
		ContentHandler w = getXMLWriterFactory().getXmlWriter(pConfig, pStream);
		return new XmlRpcWriter(pConfig, w, getTypeFactory());
	}

	protected XmlWriterFactory getXMLWriterFactory()
	{
		return writerFactory;
	}

	protected TypeFactory getTypeFactory()
	{
		return typeFactory;
	}

}
