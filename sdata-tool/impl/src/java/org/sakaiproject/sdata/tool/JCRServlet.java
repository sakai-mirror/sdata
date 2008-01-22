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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.sdata.FileItemIterator;
import org.apache.commons.fileupload.sdata.FileItemStream;
import org.apache.commons.fileupload.sdata.servlet.ServletFileUpload;
import org.apache.commons.fileupload.sdata.util.Streams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.jcr.api.JCRConstants;
import org.sakaiproject.jcr.support.api.JCRNodeFactoryService;
import org.sakaiproject.tool.api.Tool;

/**
 * <p>
 * JCR Service is a servlet that gives access to the JCR returning the content
 * of files within the jcr or a json response (directories). The resource is
 * pointed to using the URI/URL requested (the path info part), and the standard
 * Http methods do what they are expected to in the http standard. GET gets the
 * content of the file, PUT put puts a new file, the content comming from the
 * stream of the PUT. DELETE deleted the file. HEAD gets the headers that would
 * come from a full GET.
 * </p>
 * <p>
 * The content type and content encodign headers are honored for GET,HEAD and
 * PUT, but other headers are not honored completely at the moment (range-*)
 * etc,
 * </p>
 * <p>
 * POST takes multipart uploads of content, the URL pointing to a folder and
 * each upload being the name of the file being uploaded to that folder. The
 * upload uses a streaming api, and expects that form fields are ordered, such
 * that a field starting with mimetype before the uplaod stream will specify the
 * mimetype associated with the stream.
 * </p>
 * 
 * @author ieb
 */
public class JCRServlet extends HttpServlet
{
	private static final Log log = LogFactory.getLog(JCRServlet.class);

	/**
	 * Required for serialization... also to stop eclipse from giving me a
	 * warning!
	 */
	private static final long serialVersionUID = 676743152200357708L;

	private static final String BASE_PATH_INIT = "basepath";

	private static final String DEFAULT_BASE_PATH = "/sakai/sdata";

	private static final String LAST_MODIFIED = "Last-Modified";

	private String basePath;

	private ComponentManager componentManager;

	private JCRNodeFactoryService jcrNodeFactory;

	private ResourceDefinitionFactory resourceDefinitionFactory;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);

		ServletContext sc = servletConfig.getServletContext();

		componentManager = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		jcrNodeFactory = (JCRNodeFactoryService) componentManager
				.get(JCRNodeFactoryService.class.getName());

		String basePath = servletConfig.getInitParameter(BASE_PATH_INIT);
		if (basePath == null || basePath.trim().length() == 0)
		{
			basePath = DEFAULT_BASE_PATH;
		}

		resourceDefinitionFactory = new ResourceDefinitionFactory(basePath);

	}

	/**
	 * <p>
	 * The http DELETE method delete the resource at pointed to by the request.
	 * If sucessfull, it will 204 (no content), if not found 404, if error 500.
	 * Extract from the RFC on delete follows.
	 * </p>
	 * <p>
	 * The DELETE method requests that the origin server delete the resource
	 * identified by the Request-URI. This method MAY be overridden by human
	 * intervention (or other means) on the origin server. The client cannot be
	 * guaranteed that the operation has been carried out, even if the status
	 * code returned from the origin server indicates that the action has been
	 * completed successfully. However, the server SHOULD NOT indicate success
	 * unless, at the time the response is given, it intends to delete the
	 * resource or move it to an inaccessible location.
	 * </p>
	 * <p>
	 * A successful response SHOULD be 200 (OK) if the response includes an
	 * entity describing the status, 202 (Accepted) if the action has not yet
	 * been enacted, or 204 (No Content) if the action has been enacted but the
	 * response does not include an entity.
	 * </p>
	 * <p>
	 * If the request passes through a cache and the Request-URI identifies one
	 * or more currently cached entities, those entries SHOULD be treated as
	 * stale. Responses to this method are not cacheable.
	 * </p>
	 */
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		try
		{
			request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

			String path = request.getPathInfo();

			snoopRequest(request);

			ResourceDefinition rp = resourceDefinitionFactory.getSpec(path);
			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
			if (n == null)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			Session s = n.getSession();
			n.remove();
			s.save();
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);

		}
		catch (Exception e)
		{
			response.reset();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed with " + e.getMessage());
			snoopRequest(request);
			log.error("Failed  TO service Request ", e);
		}
		finally
		{
			request.removeAttribute(Tool.NATIVE_URL);
		}
	}

	/**
	 * @param request
	 */
	@SuppressWarnings("unchecked")
	private void snoopRequest(HttpServletRequest request)
	{
		StringBuilder sb = new StringBuilder("SData Request :");
		sb.append("\n\tRequest Path :").append(request.getPathInfo());
		sb.append("\n\tMethod :").append(request.getMethod());
		for (Enumeration<String> hnames = request.getHeaderNames(); hnames
				.hasMoreElements();)
		{
			String name = hnames.nextElement();
			sb.append("\n\tHeader :").append(name).append("=[").append(
					request.getHeader(name)).append("]");
		}
		if (request.getCookies() != null)
		{
			for (Cookie c : request.getCookies())
			{
				sb.append("\n\tCookie:");
				sb.append("name[").append(c.getName());
				sb.append("]path[").append(c.getPath());
				sb.append("]value[").append(c.getValue());
			}
		}
		sb.append("]");
		log.info(sb.toString());
	}

	/**
	 * <a id="sec9.4">9.4</a> HEAD</h3>
	 * <p>
	 * The HEAD method is identical to GET except that the server MUST NOT
	 * return a message-body in the response. The metainformation contained in
	 * the HTTP headers in response to a HEAD request SHOULD be identical to the
	 * information sent in response to a GET request. This method can be used
	 * for obtaining metainformation about the entity implied by the request
	 * without transferring the entity-body itself. This method is often used
	 * for testing hypertext links for validity, accessibility, and recent
	 * modification.
	 * </p>
	 * <p>
	 * The response to a HEAD request MAY be cacheable in the sense that the
	 * information contained in the response MAY be used to update a previously
	 * cached entity from that resource. If the new field values indicate that
	 * the cached entity differs from the current entity (as would be indicated
	 * by a change in Content-Length, Content-MD5, ETag or Last-Modified), then
	 * the cache MUST treat the cache entry as stale.
	 * </p>
	 */
	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		try
		{
			request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

			String path = request.getPathInfo();
			snoopRequest(request);

			ResourceDefinition rp = resourceDefinitionFactory.getSpec(path);
			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
			if (n == null)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			Node resource = n.getNode(JCRConstants.JCR_CONTENT);
			Property lastModified = resource.getProperty(JCRConstants.JCR_LASTMODIFIED);
			Property mimeType = resource.getProperty(JCRConstants.JCR_MIMETYPE);
			Property encoding = resource.getProperty(JCRConstants.JCR_ENCODING);
			Property content = resource.getProperty(JCRConstants.JCR_DATA);

			response.setContentType(mimeType.getString());
			response.setCharacterEncoding(encoding.getString());
			response.setDateHeader(LAST_MODIFIED, lastModified.getDate()
					.getTimeInMillis());
			// we need to do something about huge files
			response.setContentLength((int) content.getLength());
			response.setStatus(HttpServletResponse.SC_OK);

		}
		catch (Exception e)
		{
			response.reset();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed with " + e.getMessage());
			snoopRequest(request);
			log.error("Failed  TO service Request ", e);
		}
		finally
		{
			request.removeAttribute(Tool.NATIVE_URL);
		}
	}

	/**
	 * <p>
	 * The PUT method requests that the enclosed entity be stored under the
	 * supplied Request-URI. If the Request-URI refers to an already existing
	 * resource, the enclosed entity SHOULD be considered as a modified version
	 * of the one residing on the origin server. If the Request-URI does not
	 * point to an existing resource, and that URI is capable of being defined
	 * as a new resource by the requesting user agent, the origin server can
	 * create the resource with that URI. If a new resource is created, the
	 * origin server MUST inform the user agent via the 201 (Created) response.
	 * If an existing resource is modified, either the 200 (OK) or 204 (No
	 * Content) response codes SHOULD be sent to indicate successful completion
	 * of the request. If the resource could not be created or modified with the
	 * Request-URI, an appropriate error response SHOULD be given that reflects
	 * the nature of the problem. The recipient of the entity MUST NOT ignore
	 * any Content-* (e.g. Content-Range) headers that it does not understand or
	 * implement and MUST return a 501 (Not Implemented) response in such cases.
	 * </p>
	 * <p>
	 * If the request passes through a cache and the Request-URI identifies one
	 * or more currently cached entities, those entries SHOULD be treated as
	 * stale. Responses to this method are not cacheable.
	 * </p>
	 * <p>
	 * The fundamental difference between the POST and PUT requests is reflected
	 * in the different meaning of the Request-URI. The URI in a POST request
	 * identifies the resource that will handle the enclosed entity. That
	 * resource might be a data-accepting process, a gateway to some other
	 * protocol, or a separate entity that accepts annotations. In contrast, the
	 * URI in a PUT request identifies the entity enclosed with the request --
	 * the user agent knows what URI is intended and the server MUST NOT attempt
	 * to apply the request to some other resource. If the server desires that
	 * the request be applied to a different URI,
	 * </p>
	 * <p>
	 * it MUST send a 301 (Moved Permanently) response; the user agent MAY then
	 * make its own decision regarding whether or not to redirect the request.
	 * </p>
	 * <p>
	 * A single resource MAY be identified by many different URIs. For example,
	 * an article might have a URI for identifying "the current version" which
	 * is separate from the URI identifying each particular version. In this
	 * case, a PUT request on a general URI might result in several other URIs
	 * being defined by the origin server.
	 * </p>
	 * <p>
	 * HTTP/1.1 does not define how a PUT method affects the state of an origin
	 * server.
	 * </p>
	 * <p>
	 * PUT requests MUST obey the message transmission requirements set out in
	 * section 8.2.
	 * </p>
	 * <p>
	 * Unless otherwise specified for a particular entity-header, the
	 * entity-headers in the PUT request SHOULD be applied to the resource
	 * created or modified by the PUT.
	 * </p>
	 */
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		OutputStream out = null;
		try
		{
			request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

			String path = request.getPathInfo();
			snoopRequest(request);

			ResourceDefinition rp = resourceDefinitionFactory.getSpec(path);
			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
			boolean created = false;
			if (n == null)
			{
				n = jcrNodeFactory.createNode(rp.getRepositoryPath(),
						JCRConstants.NT_FILE);
				created = true;
				if (n == null)
				{
					throw new RuntimeException("Failed to create node at "
							+ rp.getRepositoryPath() + " type " + JCRConstants.NT_FILE);
				}
			}
			else
			{
				NodeType nt = n.getPrimaryNodeType();
				if (!JCRConstants.NT_FILE.equals(nt.getName()))
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"Content Can only be put to a file, resource type is "
									+ nt.getName());
					return;
				}
			}

			GregorianCalendar gc = new GregorianCalendar();
			long lastMod = request.getDateHeader(LAST_MODIFIED);
			if (lastMod > 0)
			{
				gc.setTimeInMillis(lastMod);
			}
			else
			{
				gc.setTime(new Date());
			}
			String mimeType = request.getContentType();
			String charEncoding = request.getCharacterEncoding();

			InputStream in = request.getInputStream();
			saveStream(n, in, mimeType, charEncoding, gc);

			in.close();
			if (created)
			{
				response.setStatus(HttpServletResponse.SC_CREATED);
			}
			else
			{
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
		}
		catch (Exception e)
		{
			response.reset();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed with " + e.getMessage());
			snoopRequest(request);
			log.error("Failed  TO service Request ", e);
		}
		finally
		{
			request.removeAttribute(Tool.NATIVE_URL);

			try
			{
				out.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	/**
	 * @param n
	 * @param in
	 * @param mimeType
	 * @param charEncoding
	 * @param gc
	 * @throws
	 * @throws RepositoryException
	 */
	private long saveStream(Node n, InputStream in, String mimeType, String charEncoding,
			Calendar lastModified) throws RepositoryException
	{
		Node resource = n.getNode(JCRConstants.JCR_CONTENT);
		resource.setProperty(JCRConstants.JCR_LASTMODIFIED, lastModified);
		resource.setProperty(JCRConstants.JCR_MIMETYPE, mimeType);
		resource.setProperty(JCRConstants.JCR_ENCODING, charEncoding);

		Property content = resource.getProperty(JCRConstants.JCR_DATA);
		content.setValue(in);

		n.save();

		return content.getLength();
	}

	/**
	 * <p>
	 * The GET method means retrieve whatever information (in the form of an
	 * entity) is identified by the Request-URI. If the Request-URI refers to a
	 * data-producing process, it is the produced data which shall be returned
	 * as the entity in the response and not the source text of the process,
	 * unless that text happens to be the output of the process.
	 * </p>
	 * <p>
	 * The semantics of the GET method change to a "conditional GET" if the
	 * request message includes an If-Modified-Since, If-Unmodified-Since,
	 * If-Match, If-None-Match, or If-Range header field. A conditional GET
	 * method requests that the entity be transferred only under the
	 * circumstances described by the conditional header field(s). The
	 * conditional GET method is intended to reduce unnecessary network usage by
	 * allowing cached entities to be refreshed without requiring multiple
	 * requests or transferring data already held by the client.
	 * </p>
	 * <p>
	 * The semantics of the GET method change to a "partial GET" if the request
	 * message includes a Range header field. A partial GET requests that only
	 * part of the entity be transferred, as described in section <a rel="xref"
	 * href="rfc2616-sec14.html#sec14.35">14.35</a>. The partial GET method is
	 * intended to reduce unnecessary network usage by allowing
	 * partially-retrieved entities to be completed without transferring data
	 * already held by the client.
	 * </p>
	 * <p>
	 * The response to a GET request is cacheable if and only if it meets the
	 * requirements for HTTP caching described in section 13.
	 * </p>
	 * <p>
	 * See section <a rel="xref" href="rfc2616-sec15.html#sec15.1.3">15.1.3</a>
	 * for security considerations when used for forms.
	 * </p>
	 */
	@Override
	protected void doGet(final HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		OutputStream out = null;
		InputStream in = null;
		try
		{
			request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

			String path = request.getPathInfo();
			snoopRequest(request);

			ResourceDefinition rp = resourceDefinitionFactory.getSpec(path);
			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
			if (n == null)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			NodeType nt = n.getPrimaryNodeType();

			if (JCRConstants.NT_FILE.equals(nt.getName()))
			{

				Node resource = n.getNode(JCRConstants.JCR_CONTENT);
				Property lastModified = resource
						.getProperty(JCRConstants.JCR_LASTMODIFIED);
				Property mimeType = resource.getProperty(JCRConstants.JCR_MIMETYPE);
				Property encoding = resource.getProperty(JCRConstants.JCR_ENCODING);
				Property content = resource.getProperty(JCRConstants.JCR_DATA);

				response.setContentType(mimeType.getString());
				response.setCharacterEncoding(encoding.getString());
				response.setDateHeader(LAST_MODIFIED, lastModified.getDate()
						.getTimeInMillis());
				// we need to do something about huge files
				response.setContentLength((int) content.getLength());

				out = response.getOutputStream();

				in = content.getStream();
				byte[] b = new byte[10240];
				int nbytes = 0;
				while ((nbytes = in.read(b)) > 0)
				{
					out.write(b, 0, nbytes);
				}
			}
			else
			{
				Map<String, Object> jsonmap = new HashMap<String, Object>();
				jsonmap.put("path", rp.getExternalPath(n.getPath()));
				jsonmap.put("type", nt.getName());
				List<Map> nodes = new ArrayList<Map>();
				NodeIterator ni = n.getNodes();
				int i = 0;
				while (ni.hasNext())
				{
					Node cn = ni.nextNode();
					Map<String, String> cnm = new HashMap<String, String>();
					cnm.put("path", rp.getExternalPath(cn.getName()));
					NodeType cnt = cn.getPrimaryNodeType();
					cnm.put("type", cnt.getName());
					cnm.put("position", String.valueOf(i));
					if (JCRConstants.NT_FILE.equals(nt.getName()))
					{
						Node resource = n.getNode(JCRConstants.JCR_CONTENT);
						Property lastModified = resource
								.getProperty(JCRConstants.JCR_LASTMODIFIED);
						Property mimeType = resource
								.getProperty(JCRConstants.JCR_MIMETYPE);
						Property encoding = resource
								.getProperty(JCRConstants.JCR_ENCODING);
						Property content = resource.getProperty(JCRConstants.JCR_DATA);

						cnm.put("mime-type", mimeType.getString());
						cnm.put("encoding", encoding.getString());
						cnm.put("length", String.valueOf(content.getLength()));

					}
					nodes.add(cnm);
					i++;
				}
				jsonmap.put("nitems", nodes.size());
				jsonmap.put("items", nodes);

				JSONObject jsonObject = JSONObject.fromObject(jsonmap);
				PrintWriter w = response.getWriter();
				w.write(jsonObject.toString());

			}
		}
		catch (Exception e)
		{
			response.reset();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed with " + e.getMessage());
			snoopRequest(request);
			log.error("Failed  TO service Request ", e);
		}
		finally
		{
			request.removeAttribute(Tool.NATIVE_URL);

			try
			{
				out.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				in.close();
			}
			catch (Exception ex)
			{
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

		String path = request.getPathInfo();
		snoopRequest(request);

		ResourceDefinition rp = resourceDefinitionFactory.getSpec(path);
		log.info("Checking for Multipart");
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		// multiparts are always streamed uploads
		if (isMultipart)
		{
			log.info("Got Multipart");
			doMumtipartUpload(request, response, path, rp);
		}
		else
		{
			log.info("Got Standard");

		}

	}

	/**
	 * @param request
	 * @param response
	 * @param path
	 * @param rp
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doMumtipartUpload(HttpServletRequest request,
			HttpServletResponse response, String path, ResourceDefinition rp)
			throws ServletException, IOException
	{
		try
		{
			try
			{
				Node n = jcrNodeFactory.createNode(path, JCRConstants.NT_FOLDER);
				if (n == null)
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"Unable to uplaod to location " + path);
					return;
				}
			}
			catch (Exception ex)
			{
				response.reset();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Failed with " + ex.getMessage());
				snoopRequest(request);
				log.error("Failed  TO service Request ", ex);
				return;
			}

			// Check that we have a file upload request

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload();

			// Parse the request
			FileItemIterator iter = upload.getItemIterator(request);
			Map<String, Map> uploads = new HashMap<String, Map>();
			while (iter.hasNext())
			{
				FileItemStream item = iter.next();
				log.info("Got Upload through Uploads");
				String name = item.getFieldName();
				log.info("    Name is "+name);
				InputStream stream = item.openStream();
				if (!item.isFormField())
				{
					try
					{
						String mimeType = item.getContentType();
						Node target = jcrNodeFactory.createNode(rp
								.getRepositoryPath(name), JCRConstants.NT_FILE);
						GregorianCalendar lastModified = new GregorianCalendar();
						lastModified.setTime(new Date());
						long size = saveStream(target, stream, mimeType, "UTF-8",
								lastModified);
						Map<String, Object> uploadMap = new HashMap<String, Object>();
						uploadMap.put("mimeType", mimeType);
						uploadMap.put("contentLength", size);
						uploadMap.put("lastModified", lastModified.getTime());
						uploadMap.put("status", "ok");
						uploads.put(name, uploadMap);
						uploadMap = new HashMap<String, Object>();
					}
					catch (Exception ex)
					{
						Map<String, Object> uploadMap = new HashMap<String, Object>();
						uploadMap.put("mimeType", "text/plain");
						uploadMap.put("encoding", "UTF-8");
						uploadMap.put("contentLength", -1);
						uploadMap.put("lastModified", 0);
						uploadMap.put("status", "Failed");
						uploadMap.put("cause", ex.getMessage());
						List<String> stackTrace = new ArrayList<String>();
						for (StackTraceElement ste : ex.getStackTrace())
						{
							stackTrace.add(ste.toString());
						}
						uploadMap.put("stacktrace", stackTrace);
						uploads.put(name, uploadMap);
						uploadMap = new HashMap<String, Object>();

					}

				}
			}

			JSONObject jsonobject = JSONObject.fromObject(uploads);
			PrintWriter pw = response.getWriter();
			log.info("Multipart Uplaod Complete "+jsonobject.toString());
			pw.write(jsonobject.toString());
		}
		catch (Throwable ex)
		{
			log.error("Failed  TO service Request ", ex);
			response.reset();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed with " + ex.getMessage());
			return;
		}
	}
}
