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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

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
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.sdata.tool.api.ResourceDefinition;
import org.sakaiproject.sdata.tool.api.ResourceDefinitionFactory;
import org.sakaiproject.sdata.tool.api.SDataException;
import org.sakaiproject.sdata.tool.util.ResourceDefinitionFactoryImpl;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.Validator;

/**
 * <p>
 * CHSServlet is a servlet that gives access to the CHS returning the content of
 * files within the chs or a map response (directories). The resource is pointed
 * to using the URI/URL requested (the path info part), and the standard Http
 * methods do what they are expected to in the http standard. GET gets the
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
public abstract class CHSServlet extends HttpServlet
{
	private static final Log log = LogFactory.getLog(CHSServlet.class);

	/**
	 * Required for serialization... also to stop eclipse from giving me a
	 * warning!
	 */
	private static final long serialVersionUID = 676743152200357708L;

	private static final String BASE_PATH_INIT = "basepath";

	private static final String DEFAULT_BASE_PATH = "/private/sdata";

	private static final String LAST_MODIFIED = "Last-Modified";

	private String basePath;

	private ComponentManager componentManager;

	private ContentHostingService contentHostingService;

	private ResourceDefinitionFactory resourceDefinitionFactory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);

		ServletContext sc = servletConfig.getServletContext();

		componentManager = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		contentHostingService = (ContentHostingService) componentManager
				.get(ContentHostingService.class.getName());

		basePath = servletConfig.getInitParameter(BASE_PATH_INIT);
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
			ContentEntity e = getEntity(rp.getRepositoryPath());
			if (e == null)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			if (e instanceof ContentCollection)
			{
				contentHostingService.removeCollection(e.getReference());
			}
			else if (e instanceof ContentResource)
			{

				long lastModifiedTime = -10;
				ContentResource cr = (ContentResource) e;

				Date lastModified = getLastModified(cr);
				lastModifiedTime = lastModified.getTime();

				if (!checkPreconditions(request, response, lastModifiedTime, String
						.valueOf(lastModifiedTime)))
				{
					return;
				}
				contentHostingService.removeResource(e.getReference());
			}
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
	 * @param repositoryPath
	 * @return
	 * @throws PermissionException
	 */
	private ContentEntity getEntity(String repositoryPath) throws PermissionException
	{
		ContentEntity ce = null;
		try
		{
			ce = contentHostingService.getResource(repositoryPath);
		}
		catch (IdUnusedException e)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Resource Not Found " + repositoryPath + " " + e);
			}
		}
		catch (TypeException e)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Resource Not Found " + repositoryPath + " " + e);
			}
		}
		if (ce == null)
		{
			if (!repositoryPath.endsWith("/"))
			{
				repositoryPath = repositoryPath + "/";
			}
			try
			{
				ce = contentHostingService.getCollection(repositoryPath);
			}
			catch (IdUnusedException e)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Collection Not Found " + repositoryPath + " " + e);
				}
			}
			catch (TypeException e)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Collection Not Found " + repositoryPath + " " + e);
				}
			}
		}
		return ce;
	}

	/**
	 * @param cr
	 * @return
	 */
	private Date getLastModified(ContentResource cr)
	{
		String lastModified = cr.getProperties().getProperty(
				ResourceProperties.PROP_MODIFIED_DATE);
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		try
		{
			return df.parse(lastModified);
		}
		catch (ParseException e)
		{
			return new Date();
		}
	}

	/**
	 * @param cre
	 * @param lastModified
	 */
	private void setLastModified(ContentResourceEdit cre, Date lastModified)
	{
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String lastModifiedString = df.format(lastModified);
		cre.getPropertiesEdit().addProperty(ResourceProperties.PROP_MODIFIED_DATE,
				lastModifiedString);
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
			ContentEntity e = getEntity(rp.getRepositoryPath());
			if (e == null)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			if (e instanceof ContentResource)
			{
				ContentResource cr = (ContentResource) e;
				response.setContentType(cr.getContentType());
				response.setDateHeader(LAST_MODIFIED, getLastModified(cr).getTime());
				// we need to do something about huge files
				response.setContentLength((int) cr.getContentLength());
				response.setStatus(HttpServletResponse.SC_OK);
			}
			else
			{
				ContentResource cr = (ContentResource) e;
				response.setDateHeader(LAST_MODIFIED, new Date().getTime());
				response.setStatus(HttpServletResponse.SC_OK);
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
			ContentEntity e = getEntity(rp.getRepositoryPath());
			ContentResourceEdit cre = null;
			boolean created = false;
			if (e == null)
			{
				String s = rp.getRepositoryPath();
				s = s.substring(0, s.lastIndexOf("/"));
				getFolder(s);
				cre = addFile(rp.getRepositoryPath());

				created = true;
				if (cre == null)
				{
					throw new RuntimeException("Failed to create node at "
							+ rp.getRepositoryPath() + " type ContentResource ");
				}
			}
			else if (e instanceof ContentResource)
			{
				cre = contentHostingService.editResource(rp.getRepositoryPath());
				if (cre == null)
				{
					throw new RuntimeException("Failed to create node at "
							+ rp.getRepositoryPath() + " type ContentResource ");
				}

			}
			else
			{
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Content Can only be put to a file ");
				return;
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

			saveStream(cre, in, mimeType, charEncoding, gc);

			in.close();
			if (created)
			{
				response.setStatus(HttpServletResponse.SC_CREATED);
			}
			else
			{
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
			log.info("PUT Saved " + request.getContentLength() + " bytes to "
					+ rp.getRepositoryPath());
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
	 * @param repositoryPath
	 * @return
	 * @throws ServerOverloadException
	 * @throws InconsistentException
	 * @throws IdInvalidException
	 * @throws IdUsedException
	 * @throws PermissionException
	 */
	private ContentResourceEdit addFile(String repositoryPath)
			throws PermissionException, IdUsedException, IdInvalidException,
			InconsistentException, ServerOverloadException
	{
		String name = repositoryPath.substring(repositoryPath.lastIndexOf("/") + 1);
		ContentResourceEdit cre = contentHostingService.addResource(repositoryPath);
		cre.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME,
				Validator.escapeResourceName(name));
		return cre;
	}

	/**
	 * @param s
	 */
	private ContentCollection getFolder(String path)
	{
		try
		{
			String[] parts = path.split("/");
			StringBuilder sb = new StringBuilder();
			ContentCollection cc = null;

			for (String part : parts)
			{
				sb.append(part);
				sb.append("/");
				try
				{
					cc = contentHostingService.getCollection(sb.toString());
				}
				catch (IdUnusedException idu)
				{
					cc = null;
				}
				if (cc == null)
				{

					ContentCollectionEdit cce = contentHostingService.addCollection(sb
							.toString());
					cce.getPropertiesEdit().addProperty(
							ResourceProperties.PROP_DISPLAY_NAME,
							Validator.escapeResourceName(part));
					contentHostingService.commitCollection(cce);
					cc = contentHostingService.getCollection(sb.toString());
					if (cc != null)
					{
						if (log.isDebugEnabled())
						{
							log.debug("Created " + sb.toString());
						}
					}
				}
				else
				{
					if (log.isDebugEnabled())
					{
						log.debug("Found " + cc + ":" + cc.getReference());
					}

				}
				if (cc == null)
				{
					log.error("Failed to create " + sb.toString());
					return null;
				}
			}
			return cc;
		}
		catch (Exception inc)
		{
			log
					.warn(
							"Failed Creating folder " + path + " cause:"
									+ inc.getMessage(), inc);
		}
		return null;
	}

	/**
	 * @param n
	 * @param in
	 * @param mimeType
	 * @param charEncoding
	 * @param gc
	 * @throws ServerOverloadException
	 * @throws OverQuotaException
	 * @throws
	 * @throws RepositoryException
	 */
	private long saveStream(ContentResourceEdit cre, InputStream in, String mimeType,
			String charEncoding, Calendar lastModified) throws OverQuotaException,
			ServerOverloadException
	{

		cre.setContentType(mimeType);
		setLastModified(cre, lastModified.getTime());
		cre.setContent(in);
		contentHostingService.commitResource(cre);

		return cre.getContentLength();
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
			ContentEntity e = getEntity(rp.getRepositoryPath());
			if (e == null)
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			if (e instanceof ContentResource)
			{
				ContentResource cr = (ContentResource) e;
				Date lastModified = getLastModified(cr);

				response.setContentType(cr.getContentType());
				response.setDateHeader(LAST_MODIFIED, lastModified.getTime());
				setGetCacheControl(response, rp.isPrivate());

				String currentEtag = String.valueOf(lastModified.getTime());
				response.setHeader("ETag", currentEtag);

				boolean sendContent = true;
				long lastModifiedTime = lastModified.getTime();

				if (!checkPreconditions(request, response, lastModifiedTime, currentEtag))
				{
					return;
				}
				long totallength = cr.getContentLength();
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

				in = cr.streamContent();
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
			else if (e instanceof ContentCollection)
			{
				ContentCollection cc = (ContentCollection) e;
				setGetCacheControl(response, rp.isPrivate());

				// Property lastModified =
				// n.getProperty(JCRConstants.JCR_LASTMODIFIED);
				// response.setHeader("ETag",
				// String.valueOf(lastModified.getDate()
				// .getTimeInMillis()));

				Map<String, Object> outputMap = new HashMap<String, Object>();
				outputMap.put("path", rp.getExternalPath(e.getReference()));
				outputMap.put("type", "collection");
				List<Map> nodes = new ArrayList<Map>();
				Iterator members = cc.getMembers().iterator();

				int i = 0;
				while (members.hasNext())
				{
					String memberId = (String) members.next();
					ContentEntity ce = cc.getMember(memberId);
					Map<String, Object> cnm = new HashMap<String, Object>();
					cnm.put("path", rp.getExternalPath(ce.getReference()));
					cnm.put("position", String.valueOf(i));
					if (ce instanceof ContentCollection)
					{
						cnm.put("type", "folder");
					}
					else if (ce instanceof ContentResource)
					{
						ContentResource cr = (ContentResource) ce;
						cnm.put("type", "file");

						cnm.put("mime-type", cr.getContentType());
						cnm.put("length", cr.getContentLength());
						cnm.put("lastModified", getLastModified(cr));

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
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

				ContentCollection e = getFolder(rp.getRepositoryPath());
				if (e == null)
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
						String resourceName = rp.getRepositoryPath() + "/" + name;
						ContentEntity ce = getEntity(resourceName);
						ContentResourceEdit target = null;
						if (ce != null)
						{
							target = contentHostingService.editResource(resourceName);
						}
						if (target == null)
						{
							target = addFile(resourceName);

						}
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
						log.error("Failed to Add Item to " + ex);
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
