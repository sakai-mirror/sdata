package org.sakaiproject.sdata.tool;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.sdata.tool.api.HandlerSerialzer;
import org.sakaiproject.sdata.tool.api.SDataException;

public abstract class AbstractHandler implements Handler {

	public void sendMap(HttpServletRequest request,
			HttpServletResponse response, Map<String, Object> contetMap)
			throws IOException {
		getSerializer().sendMap(request, response, contetMap);
	}

	public void sendError(HttpServletRequest request,
			HttpServletResponse response, Throwable ex) throws IOException {
		getSerializer().sendError(this, request, response, ex);
	}

	public void doDelete(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		sendError(request, response, new SDataException(
				HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"Method Not Implemented "));
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		sendError(request, response, new SDataException(
				HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"Method Not Implemented "));
	}

	public void doHead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		sendError(request, response, new SDataException(
				HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"Method Not Implemented "));
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		sendError(request, response, new SDataException(
				HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"Method Not Implemented "));
	}

	public void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		sendError(request, response, new SDataException(
				HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"Method Not Implemented "));
	}

	public abstract HandlerSerialzer getSerializer();

}
