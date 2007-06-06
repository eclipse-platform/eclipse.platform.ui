/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.eclipse.core.boot.*;
import org.eclipse.update.internal.ui.*;

/**
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
			buff.append(attName + "=" + "\"" + attValue + "\"+"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return host + ":" + port + context + "?" + buff.toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static PrintWriter createResponsePrologue(HttpServletResponse response)
		throws IOException {
		response.setContentType("text/html"); //$NON-NLS-1$
		PrintWriter writer = response.getWriter();
		String title = UpdateUI.getString("ServletsUtil.responseTitle"); //$NON-NLS-1$
		writer.println(
			"" //$NON-NLS-1$
				+ "<!DOCTYPE HTML PUBLIC \" -//W3C//DTD HTML 4.0 Transitional//EN\">" //$NON-NLS-1$
				+ "\n <HTML> \n <HEAD> \n" //$NON-NLS-1$
				+ "<TITLE>"+title+"</TITLE>\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" //$NON-NLS-1$
				+ "</HEAD>"); //$NON-NLS-1$
		writer.println(" <BODY topmargin=\"0\" leftmargin=\"0\">"); //$NON-NLS-1$
		return writer;
	}

	public static void createError(
		PrintWriter writer,
		String problem,
		String resolution) {
		createHeading(writer, UpdateUI.getString("ServletsUtil.updateFailed")); //$NON-NLS-1$
		startTextArea(writer);
		createParagraph(writer, UpdateUI.getString("ServletsUtil.problem"), problem); //$NON-NLS-1$
		if (resolution != null) {
			writer.println("<p>"); //$NON-NLS-1$
			createParagraph(writer, UpdateUI.getString("ServletsUtil.whatToDo"), resolution); //$NON-NLS-1$
		}
		endTextArea(writer);
	}

	public static void createParagraph(
		PrintWriter writer,
		String heading,
		String text) {
		writer.print("<b><font color=\"#5B78AC\">"); //$NON-NLS-1$
		writer.print(heading);
		writer.println("</b></font><br>"); //$NON-NLS-1$
		writer.println(text);
	}

	public static void createInfo(PrintWriter writer) {
		createHeading(writer, UpdateUI.getString("ServletsUtil.updateInProgress")); //$NON-NLS-1$
		startTextArea(writer);
		writer.println(
			UpdateUI.getString("ServletsUtil.updateInitiated")); //$NON-NLS-1$
		endTextArea(writer);
	}
	
	private static boolean isWin32() {
		return BootLoader.getWS().equals("win32"); //$NON-NLS-1$
	}

	private static void startTextArea(PrintWriter writer) {
		writer.println(
			"<table border=\"0\" width=\"100%\" cellspacing=\"5\" cellpadding=\"5\">"); //$NON-NLS-1$
		writer.println("<tr>"); //$NON-NLS-1$
		writer.print("<td width=\"100%\">"); //$NON-NLS-1$
		if (isWin32())
			writer.println("<font face=\"MS Sans Serif\" size=\"1\">"); //$NON-NLS-1$
		else
			writer.println(""); //$NON-NLS-1$
	}
	private static void endTextArea(PrintWriter writer) {
		if (isWin32())
			writer.println("</font>"); //$NON-NLS-1$
		writer.println("</td>"); //$NON-NLS-1$
		writer.println("</tr>"); //$NON-NLS-1$
		writer.println("</table>"); //$NON-NLS-1$
	}

	public static void createHeading(PrintWriter writer, String title) {
		writer.println(
			"<table cols=1 width=\"588\" cellspacing=\"0\" cellpadding=\"0\">"); //$NON-NLS-1$
		writer.println("<tr>"); //$NON-NLS-1$
		writer.println(
			"<td background=\"images/form_banner.jpg\" width=\"580\" height=\"30\">"); //$NON-NLS-1$
		writer.print("<p><b><font size=\"3\" face=\"Tahoma\">&nbsp;"); //$NON-NLS-1$
		writer.print(title);
		writer.println("</font></b></p>"); //$NON-NLS-1$
		writer.println("</td>"); //$NON-NLS-1$
		writer.println("</tr>"); //$NON-NLS-1$

		writer.println("<tr>"); //$NON-NLS-1$
		writer.println(
			"<td width=\"580\"><img border=\"0\" src=\"images/form_underline.jpg\" width=\"600\" height=\"15\"></td>"); //$NON-NLS-1$
		writer.println("</td>"); //$NON-NLS-1$
		writer.println("</tr>"); //$NON-NLS-1$
		writer.println("</table>"); //$NON-NLS-1$
	}

	public static void createResponseEpilogue(
		HttpServletRequest request,
		HttpServletResponse response,
		PrintWriter writer) {

		String backURL = getOriginatingURL(request);
		if (backURL != null) {
			startTextArea(writer);
			String backText = UpdateUI.getString("ServletsUtil.back"); //$NON-NLS-1$
			writer.print("<img border=\"0\" src=\"images/backward_nav.gif\"/><a href=\""); //$NON-NLS-1$
			writer.print(getOriginatingURL(request));
			writer.print("\">"+backText+"</a>"); //$NON-NLS-1$ //$NON-NLS-2$
			endTextArea(writer);
		}
		writer.println(" </BODY> "); //$NON-NLS-1$
		writer.println("</HTML>"); //$NON-NLS-1$
		writer.close();
	}
	private static String getOriginatingURL(HttpServletRequest request) {
		return request.getParameter("backURL"); //$NON-NLS-1$
	}
}
