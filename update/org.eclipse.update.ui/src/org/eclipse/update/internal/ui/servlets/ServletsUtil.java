package org.eclipse.update.internal.ui.servlets;

import javax.servlet.http.*;
import java.io.*;
import java.util.Enumeration;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ServletsUtil {
	
	public static String getServerInfo(HttpServletRequest servletRequest) {
		String host = servletRequest.getServerName();
		int port = servletRequest.getServerPort();
		String context = servletRequest.getContextPath();
		
		Enumeration atts = servletRequest.getParameterNames();
		StringBuffer buff = new StringBuffer();
		for (;atts.hasMoreElements();) {
			String attName = (String)atts.nextElement();
			Object attValue = servletRequest.getParameter(attName);
			buff.append(attName+"="+"\""+attValue+"\"+");
		}
		return host + ":"+ port + context+"?"+buff.toString();
	}
	
	public static PrintWriter createResponsePrologue(HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		writer.println("" +			"<!DOCTYPE HTML PUBLIC \" -//W3C//DTD HTML 4.0 Transitional//EN\">"
			+ "\n <HTML> \n <HEAD> \n <TITLE> "
			+ "\n Eclipse Install </TITLE>\n </HEAD>\n");
		writer.println(" <BODY>");
		return writer;
	}
	
	public static void createResponseEpilogue(HttpServletResponse response, PrintWriter writer) {
		writer.println(" </BODY> ");
		writer.println("</HTML>");
		writer.close();
	}
}