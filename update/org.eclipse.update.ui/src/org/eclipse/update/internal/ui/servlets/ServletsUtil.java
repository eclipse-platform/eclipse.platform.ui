package org.eclipse.update.internal.ui.servlets;

import java.io.*;
import java.util.Enumeration;

import javax.servlet.http.*;

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
		for (; atts.hasMoreElements();) {
			String attName = (String) atts.nextElement();
			Object attValue = servletRequest.getParameter(attName);
			buff.append(attName + "=" + "\"" + attValue + "\"+");
		}
		return host + ":" + port + context + "?" + buff.toString();
	}

	public static PrintWriter createResponsePrologue(HttpServletResponse response)
		throws IOException {
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		writer.println(
			""
				+ "<!DOCTYPE HTML PUBLIC \" -//W3C//DTD HTML 4.0 Transitional//EN\">"
				+ "\n <HTML> \n <HEAD> \n <TITLE> "
				+ "\n Eclipse Install </TITLE>\n </HEAD>\n");
		writer.println(" <BODY topmargin=\"0\" leftmargin=\"0\">");
		return writer;
	}

	public static void createError(
		PrintWriter writer,
		String problem,
		String resolution) {
		createHeading(writer, "Web update failed");
		startTextArea(writer);
		createParagraph(writer, "Problem", problem);
		if (resolution != null) {
			writer.println("<p>");
			createParagraph(writer, "What you can do", resolution);
		}
		endTextArea(writer);
	}

	public static void createParagraph(
		PrintWriter writer,
		String heading,
		String text) {
		writer.print("<b><font color=\"#5B78AC\">");
		writer.print(heading);
		writer.println("</b></font><br>");
		writer.println(text);
	}

	public static void createInfo(PrintWriter writer) {
		createHeading(writer, "Web Update in progress");
		startTextArea(writer);
		writer.println(
			"Web update has been initiated. You should see install wizard being open from the running Eclipse window.");
		endTextArea(writer);
	}

	private static void startTextArea(PrintWriter writer) {
		writer.println(
			"<table border=\"0\" width=\"100%\" cellspacing=\"5\" cellpadding=\"5\">");
		writer.println("<tr>");
		writer.println(
			"<td width=\"100%\"><font face=\"MS Sans Serif\" size=\"1\">");
	}
	private static void endTextArea(PrintWriter writer) {
		writer.println("</td>");
		writer.println("</tr>");
		writer.println("</table>");
	}

	public static void createHeading(PrintWriter writer, String title) {
		writer.println(
			"<table cols=1 width=\"588\" cellspacing=\"0\" cellpadding=\"0\">");
		writer.println("<tr>");
		writer.println(
			"<td background=\"images/form_banner.jpg\" width=\"580\" height=\"30\">");
		writer.print("<p><b><font size=\"3\" face=\"Tahoma\">&nbsp;");
		writer.print(title);
		writer.println("</font></b></p>");
		writer.println("</td>");
		writer.println("</tr>");

		writer.println("<tr>");
		writer.println(
			"<td width=\"580\"><img border=\"0\" src=\"images/form_underline.jpg\" width=\"600\" height=\"15\"></td>");
		writer.println("</td>");
		writer.println("</tr>");
		writer.println("</table>");
	}

	public static void createResponseEpilogue(
		HttpServletRequest request,
		HttpServletResponse response,
		PrintWriter writer) {

		String backURL = getOriginatingURL(request);
		if (backURL != null) {
			startTextArea(writer);
			writer.print("<img border=\"0\" src=\"images/backward_nav.gif\"/><a href=\"");
			writer.print(getOriginatingURL(request));
			writer.print("\">Back</a>");
			endTextArea(writer);
		}
		writer.println(" </BODY> ");
		writer.println("</HTML>");
		writer.close();
	}
	private static String getOriginatingURL(HttpServletRequest request) {
		return request.getParameter("backURL");
	}
}