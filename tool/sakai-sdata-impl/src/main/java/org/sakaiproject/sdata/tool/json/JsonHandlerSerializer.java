/**
 * 
 */
package org.sakaiproject.sdata.tool.json;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.sakaiproject.sdata.tool.api.Handler;
import org.sakaiproject.sdata.tool.api.HandlerSerialzer;
import org.sakaiproject.sdata.tool.api.SDataException;

/**
 * @author ieb
 *
 */
public class JsonHandlerSerializer implements HandlerSerialzer {

	public void sendError(Handler handler, HttpServletRequest request,
			HttpServletResponse response, Throwable ex) throws IOException {
		if (ex instanceof SDataException)
		{
			SDataException sde = (SDataException) ex;
			response.reset();
			handler.setHandlerHeaders(request, response);
			response.sendError(sde.getCode(), sde.getMessage());
		}
		else
		{
			response.reset();
			handler.setHandlerHeaders(request, response);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed with " + ex.getMessage());
		}
		
	}

	public void sendMap(HttpServletRequest request,
			HttpServletResponse response, Map<String, Object> contetMap)
			throws IOException {
		JSONObject jsonObject = JSONObject.fromObject(contetMap);
		byte[] b = jsonObject.toString().getBytes("UTF-8");
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.setContentLength(b.length);
		response.getOutputStream().write(b);
		
	}



}
