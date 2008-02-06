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

import org.apache.commons.fileupload.sdata.FileItemIterator;
import org.apache.commons.fileupload.sdata.FileItemStream;
import org.apache.commons.fileupload.sdata.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.jcr.api.JCRConstants;
import org.sakaiproject.jcr.support.api.JCRNodeFactoryService;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.ResourceDefinitionFactory;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.util.ResourceDefinitionFactoryImpl;
import org.sakaiproject.tool.api.Tool;

/**
 * <p>
 * JCR Service is a servlet that gives access to the JCR returning the content
 * of files within the jcr or a map response (directories). The resource is
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
public abstract class JCRServlet extends HttpServlet
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

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
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

		resourceDefinitionFactory = getResourceDefinitionFactory();

	}

	/**
	 * Creates a resource definition factory suitable for controlling the
	 * storage of items
	 * 
	 * @return
	 */
	protected ResourceDefinitionFactory getResourceDefinitionFactory()
	{
		return new ResourceDefinitionFactoryImpl(basePath);
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

			snoopRequest(request);

			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
			if (n == null)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			NodeType nt = n.getPrimaryNodeType();

			long lastModifiedTime = -10;
			if (JCRConstants.NT_FILE.equals(nt.getName()))
			{

				Node resource = n.getNode(JCRConstants.JCR_CONTENT);
				Property lastModified = resource
						.getProperty(JCRConstants.JCR_LASTMODIFIED);
				lastModifiedTime = lastModified.getDate().getTimeInMillis();

				if (!checkPreconditions(request, response, lastModifiedTime, String
						.valueOf(lastModifiedTime)))
				{
					return;
				}
			}

			Session s = n.getSession();
			n.remove();
			s.save();
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);

		}
		catch (Exception e)
		{
			sendError(request, response, e);

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
		boolean snoop = "1".equals(request.getParameter("snoop"));
		if (snoop)
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
			log.info("Doing Head ");
			snoopRequest(request);

			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
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
			sendError(request, response, e);

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
			snoopRequest(request);

			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
			Node n = jcrNodeFactory.getNode(rp.getRepositoryPath());
			boolean created = false;
			if (n == null)
			{
				n = jcrNodeFactory.createFile(rp.getRepositoryPath());
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
			sendError(request, response, e);
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
	 * See section <a
	 * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec15.html#sec15.1.3">15.1.3</a>
	 * for security considerations when used for forms.
	 * </p>
	 * <p>
	 * Section <a
	 * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9"
	 * >14.9</a> specifies cache control headers.
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
			snoopRequest(request);

			String range = request.getHeader("range");
			boolean partialGet = (range != null && range.trim().length() != 0);

			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
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
				setGetCacheControl(response, rp.isPrivate());

				String currentEtag = String.valueOf(lastModified.getDate()
						.getTimeInMillis());
				response.setHeader("ETag", currentEtag);

				boolean sendContent = true;
				long lastModifiedTime = lastModified.getDate().getTimeInMillis();

				if (!checkPreconditions(request, response, lastModifiedTime, currentEtag))
				{
					return;
				}
				long totallength = content.getLength();
				long[] ranges = new long[2];
				ranges[0] = 0;
				ranges[1] = totallength;
				if (!checkRanges(request, response, lastModifiedTime, currentEtag, ranges))
				{
					return;
				}

				long length = ranges[1] - ranges[0];

				if (length > 1024 * 1024)
				{
					length = 1024 * 1024;
					ranges[1] = ranges[0] + length;
				}

				if (totallength != length)
				{
					response.setHeader("Content-Range", "bytes " + ranges[0] + "-"
							+ (ranges[1] - 1) + "/" + totallength);
					response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
				}
				else
				{
					response.setStatus(HttpServletResponse.SC_OK);
				}

				response.setContentLength((int) length);

				out = response.getOutputStream();

				in = content.getStream();
				in.skip(ranges[0]);
				byte[] b = new byte[10240];
				int nbytes = 0;
				while ((nbytes = in.read(b)) > 0 && length > 0)
				{
					if (nbytes < length)
					{
						out.write(b, 0, nbytes);
						length = length - nbytes;
					}
					else
					{
						out.write(b, 0, (int) length);
						length = 0;
					}
				}
			}
			else
			{
				setGetCacheControl(response, rp.isPrivate());

				// Property lastModified =
				// n.getProperty(JCRConstants.JCR_LASTMODIFIED);
				// response.setHeader("ETag",
				// String.valueOf(lastModified.getDate()
				// .getTimeInMillis()));

				Map<String, Object> outputMap = new HashMap<String, Object>();
				outputMap.put("path", rp.getExternalPath(n.getPath()));
				outputMap.put("type", nt.getName());
				List<Map> nodes = new ArrayList<Map>();
				NodeIterator ni = n.getNodes();
				int i = 0;
				while (ni.hasNext())
				{
					Node cn = ni.nextNode();
					Map<String, Object> cnm = new HashMap<String, Object>();
					cnm.put("path", rp.getExternalPath(cn.getName()));
					NodeType cnt = cn.getPrimaryNodeType();
					cnm.put("type", cnt.getName());
					cnm.put("position", String.valueOf(i));
					if (JCRConstants.NT_FILE.equals(nt.getName()))
					{
						Node resource = n.getNode(JCRConstants.JCR_CONTENT);
						Property nodeLastModified = resource
								.getProperty(JCRConstants.JCR_LASTMODIFIED);
						Property mimeType = resource
								.getProperty(JCRConstants.JCR_MIMETYPE);
						Property encoding = resource
								.getProperty(JCRConstants.JCR_ENCODING);
						Property content = resource.getProperty(JCRConstants.JCR_DATA);

						cnm.put("mime-type", mimeType.getString());
						cnm.put("encoding", encoding.getString());
						cnm.put("length", String.valueOf(content.getLength()));
						cnm.put("lastModified", nodeLastModified.getDate());

					}
					nodes.add(cnm);
					i++;
				}
				outputMap.put("nitems", nodes.size());
				outputMap.put("items", nodes);

				sendMap(request, response, outputMap);
			}

		}
		catch (Exception e)
		{
			sendError(request, response, e);

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

	/**
	 * @param request
	 * @param response
	 * @param lastModifiedTime
	 * @param currentEtag
	 * @param ranges
	 * @return
	 * @throws IOException
	 */
	private boolean checkRanges(HttpServletRequest request, HttpServletResponse response,
			long lastModifiedTime, String currentEtag, long[] ranges) throws IOException
	{

		String range = request.getHeader("range");
		long ifRangeDate = request.getDateHeader("if-range");
		String ifRangeEtag = request.getHeader("if-range");

		if (ifRangeDate != -1 && lastModifiedTime > ifRangeDate)
		{
			// the entity has been modified, ignore and send the whole lot
			return true;
		}
		if (ifRangeEtag != null && !currentEtag.equals(ifRangeEtag))
		{
			// the entity has been modified, ignore and send the whole lot
			return true;
		}

		if (range != null)
		{
			String[] s = range.split("=");
			if (!"bytes".equals(s[0]))
			{
				response
						.sendError(416,
								"System only supports single range responses, specified in bytes");
				return false;
			}
			range = s[1];
			String[] r = range.split(",");
			if (r.length > 1)
			{
				response.sendError(416, "System only supports single range responses");
				return false;
			}
			range = range.trim();
			if (range.startsWith("-"))
			{
				ranges[1] = Long.parseLong(range.substring(1));
			}
			else if (range.endsWith("-"))
			{
				ranges[0] = Long.parseLong(range.substring(0, range.length() - 1));
			}
			else
			{
				r = range.split("-");
				ranges[0] = Long.parseLong(r[0]);
				ranges[1] = Long.parseLong(r[1]);
			}
		}
		return true;
	}

	/**
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private boolean checkPreconditions(HttpServletRequest request,
			HttpServletResponse response, long lastModifiedTime, String currentEtag)
			throws IOException
	{
		lastModifiedTime = lastModifiedTime - (lastModifiedTime % 1000);
		long ifUnmodifiedSince = request.getDateHeader("if-unmodified-since");
		if (ifUnmodifiedSince > 0 && (lastModifiedTime > ifUnmodifiedSince))
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			return false;
		}

		String ifMatch = request.getHeader("if-match");
		if (ifMatch != null && ifMatch.indexOf(currentEtag) < 0)
		{
			// ifMatch was present, but the currentEtag didnt match
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			return false;
		}
		String ifNoneMatch = request.getHeader("if-none-match");
		if (ifNoneMatch != null && ifNoneMatch.indexOf(currentEtag) >= 0)
		{
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			return false;
		}
		long ifModifiedSince = request.getDateHeader("if-modified-since");
		if ((ifModifiedSince > 0) && (lastModifiedTime <= ifModifiedSince))
		{
			response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
			return false;
		}
		return true;
	}

	/**
	 * @param range
	 * @return
	 */
	private long[] parseRange(String range)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param response
	 */
	private void setGetCacheControl(HttpServletResponse response, boolean isprivate)
	{
		if (isprivate)
		{
			response.addHeader("Cache-Control", "must-revalidate");
			response.addHeader("Cache-Control", "private");
			response.addHeader("Cache-Control", "no-store");
		}
		else
		{
			// response.addHeader("Cache-Control", "must-revalidate");
			response.addHeader("Cache-Control", "public");
		}
		response.addHeader("Cache-Control", "max-age=600");
		response.addHeader("Cache-Control", "s-maxage=600");
		response.setDateHeader("Date", System.currentTimeMillis());
		response.setDateHeader("Expires", System.currentTimeMillis() + 600000);

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		snoopRequest(request);
		try
		{
			ResourceDefinition rp = resourceDefinitionFactory.getSpec(request);
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);

			// multiparts are always streamed uploads
			if (isMultipart)
			{
				doMumtipartUpload(request, response, rp);
			}
			else
			{
				log.info("Got Standard");

			}
		}
		catch (SDataException sde)
		{
			sendError(request, response, sde);

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
			HttpServletResponse response, ResourceDefinition rp) throws ServletException,
			IOException
	{
		try
		{
			try
			{
				Node n = jcrNodeFactory.createFolder(rp.getRepositoryPath());
				if (n == null)
				{
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"Unable to uplaod to location " + rp.getRepositoryPath());
					return;
				}
			}
			catch (Exception ex)
			{
				sendError(request, response, ex);
				snoopRequest(request);
				log.error("Failed  TO service Request ", ex);
				return;
			}

			// Check that we have a file upload request

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload();

			// Parse the request
			FileItemIterator iter = upload.getItemIterator(request);
			Map<String, Object> uploads = new HashMap<String, Object>();
			while (iter.hasNext())
			{
				FileItemStream item = iter.next();
				log.debug("Got Upload through Uploads");
				String name = item.getFieldName();
				log.debug("    Name is " + name);
				InputStream stream = item.openStream();
				if (!item.isFormField())
				{
					try
					{
						String mimeType = item.getContentType();
						Node target = jcrNodeFactory.createFile(rp
								.getRepositoryPath(name));
						GregorianCalendar lastModified = new GregorianCalendar();
						lastModified.setTime(new Date());
						long size = saveStream(target, stream, mimeType, "UTF-8",
								lastModified);
						Map<String, Object> uploadMap = new HashMap<String, Object>();
						if (size > Integer.MAX_VALUE)
						{
							uploadMap.put("contentLength", String.valueOf(size));
						}
						else
						{
							uploadMap.put("contentLength", (int) size);
						}
						uploadMap.put("mimeType", mimeType);
						uploadMap.put("lastModified", lastModified.getTime());
						uploadMap.put("status", "ok");
						uploads.put(name, uploadMap);
						uploadMap = new HashMap<String, Object>();
					}
					catch (Exception ex)
					{
						log.error(ex);
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

			sendMap(request, response, uploads);
		}
		catch (Throwable ex)
		{
			log.error("Failed  TO service Request ", ex);
			sendError(request, response, ex);
			return;
		}
	}

	/**
	 * @param ex
	 * @throws IOException
	 */
	protected abstract void sendError(HttpServletRequest request,
			HttpServletResponse response, Throwable ex) throws IOException;

	/**
	 * Serailize a Map strucutre to the output stream
	 * 
	 * @param uploads
	 * @throws IOException
	 */
	protected abstract void sendMap(HttpServletRequest request,
			HttpServletResponse response, Map<String, Object> contetMap)
			throws IOException;

	/**
	 * @return the basePath
	 */
	public String getBasePath()
	{
		return basePath;
	}

	/**
	 * @param basePath
	 *        the basePath to set
	 */
	public void setBasePath(String basePath)
	{
		this.basePath = basePath;
	}
}