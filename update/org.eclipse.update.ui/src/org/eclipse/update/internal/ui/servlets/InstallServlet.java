package org.eclipse.update.internal.ui.servlets;

import java.io.*;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class InstallServlet extends HttpServlet {
	private ServletConfig servletConfig;
	public static final String SERVLET_NAME = "/InstallServlet";
	
	public void init(ServletConfig config) throws ServletException {
		this.servletConfig = config;
	}
	
	public void destroy() {
	}
	
	public ServletConfig getServletConfig() {
		return servletConfig;
	}
	
	public String getServletInfo() {
		return "Eclipse Install servlet";
	}
	
	public void service(HttpServletRequest servletRequest,
				HttpServletResponse servletResponse) throws ServletException, IOException {
		String serverInfo = ServletsUtil.getServerInfo(servletRequest);
		PrintWriter writer = ServletsUtil.createResponsePrologue(servletResponse);
		createResponseBody(writer, serverInfo);
		ServletsUtil.createResponseEpilogue(servletResponse, writer);
	}
	
	private void createResponseBody(PrintWriter writer, String serverInfo) {
		writer.println("<H2>Response from the InstallServlet</H2>");
		writer.println("<P>Server Info: "+serverInfo+"</P>");
	}
}
