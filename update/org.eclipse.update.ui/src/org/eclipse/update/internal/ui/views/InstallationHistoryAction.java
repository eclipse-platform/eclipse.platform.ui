/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import java.io.*;
import java.util.*;

import org.eclipse.core.boot.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.update.internal.ui.*;

public class InstallationHistoryAction extends Action {

	private BufferedReader buffRead;
	private PrintWriter htmlLog;
	private File tempFile;
	private String rowType;
	private IPath path;
	private static final String lightBlue = "#BBCCFF";
	private static final String white = "#FFFFFF";
	private static final String darkBlue = "#0080C0";

	public InstallationHistoryAction(String text, ImageDescriptor desc) {
		super(text, desc);
		String location =
			BootLoader
				.getCurrentPlatformConfiguration()
				.getConfigurationLocation()
				.getFile();
		path = new Path(location).removeLastSegments(1).append(".install-log");
		rowType = "light-row";
	}

	public void run() {
		try {
			openLog();
			parseLog();
			UpdateUI.showURL("file:" + getTempFile().getPath().toString());
		} catch (CoreException e) {
			UpdateUI.logException(e);
		} finally {
			closeLog();
		}
	}

	private void openLog() throws CoreException {
		try {
			buffRead = new BufferedReader(new FileReader(path.toOSString()));
			htmlLog = new PrintWriter(new FileOutputStream(getTempFile()));
		} catch (FileNotFoundException e) {
			throwCoreException(e);
		}

	}

	private File getTempFile() throws CoreException {
		if (tempFile == null) {
			try {
				tempFile = File.createTempFile("install-log", ".html");
				tempFile.deleteOnExit();
			} catch (IOException e) {
				throwCoreException(e);
			}
		}
		return tempFile;
	}

	private void parseLog() throws CoreException {
		//		!CONFIGURATION <configuration-date>
		//		!ACTIVITY <date> <target> <action> <status>

		try {
			String type, date, status, target, action;
			StringTokenizer htmlCode;

			htmlLog.println("<html>");
			htmlLog.println("<head>");
			htmlLog.println("<title>Install-Log</title>");
			addCSS();
			htmlLog.println("</head>");
			htmlLog.println("<body>");
			String title = "Installation History";
			String desc = "The list below represents the history of your update activities from the product installation to date. Each operation carries a date it commenced, as well as activities performed. Operations are sorted by date in ascending order.";
			htmlLog.println("<h1 class=title>"+title+"</h1>");
			htmlLog.println("<p class=bodyText>"+desc+"</p>");
			
			htmlLog.println("<center>");

			htmlLog.println("<table width =100% border=0 cellspacing=1 cellpadding=2>");

			while (buffRead.ready()) {

				htmlCode = new StringTokenizer(buffRead.readLine());
				while (!(htmlCode.hasMoreElements())) {
					if (!buffRead.ready())
						return;
					htmlCode = new StringTokenizer(buffRead.readLine());
				}

				type = htmlCode.nextToken();
				type = type.substring(type.indexOf("!") + 1, type.length());

				if (type.equals("ACTIVITY")) {
					target = "";
					date = htmlCode.nextToken(".");
					htmlCode.nextToken(" ");
					while (htmlCode.countTokens() > 2)
						target = target + " " + htmlCode.nextToken();

					action = htmlCode.nextToken();
					status = htmlCode.nextToken();

					addActivity(action, date, status, target);

				} else {
					htmlLog.println();
					htmlLog.println(
						"<tr id=separator><td colspan=4></td></tr>");
					htmlLog.println();
					date = "";
					while (htmlCode.countTokens() > 0)
						date = date + " " + htmlCode.nextToken();
					addConfigurationHeader(date);
					addActivityHeader();
				}

			}
			htmlLog.println("</table>");
			htmlLog.println("</body>");
			htmlLog.println("</html>");

		} catch (Exception e) {
			throwCoreException(e);
		}

	}

	private void addActivity(
		String type,
		String date,
		String status,
		String target) {
		htmlLog.print("<tr class=" + rowType + ">");
		htmlLog.print("<td class=log-text width=30%>");
		htmlLog.print(date);
		htmlLog.println("</td>");
		htmlLog.print("<td class=log-text width=40%>");
		htmlLog.print(target);
		htmlLog.println("</td>");
		htmlLog.print("<td class=log-text width=20%>");
		htmlLog.print(type);
		htmlLog.println("</td>");
		htmlLog.print("<td class=log-text width=10%>");
		htmlLog.print(status);
		htmlLog.println("</td></tr>");
		toggleRowColor();
	}

	private void addCSS() {
		htmlLog.println("<STYLE type=\"text/css\">");
		htmlLog.println("H1.title { font-family: sans-serif; color: #0080C0 }");
		htmlLog.println("P.bodyText { font-family: sans-serif; font-size: 9pt; }");
		htmlLog.println(
			"TD.log-header { font-family: sans-serif; font-style: normal; font-weight: bold; font-size: 9pt; color: white}");
		htmlLog.println(
			"TD.log-text { font-family: sans-serif; font-style: normal; font-weight: lighter; font-size: 8pt; color:black}");
		htmlLog.println(
		//	"TD.config-log-header { font-family: sans-serif; font-style: normal; font-weight: bold; font-size: 9pt; color: white; ;text-align: right; border-top:10px solid white}");
		"TD.config-log-header { font-family: sans-serif; font-style: normal; font-weight: bold; font-size: 9pt; color: white; border-top:10px solid white}");
		htmlLog.println("TR.light-row {background:" + white + "}");
		htmlLog.println("TR.dark-row {background:" + lightBlue + "}");
		htmlLog.println("TR.header {background:" + darkBlue + "}");
		htmlLog.println("</STYLE>");
	}

	private void addActivityHeader() {
		htmlLog.print("<tr class=header>");
		htmlLog.print("<td class=log-header>");
		htmlLog.print("Date / Time");
		htmlLog.print("</td>");
		htmlLog.print("<td class=log-header>");
		htmlLog.print("Target");
		htmlLog.print("</td>");
		htmlLog.print("<td class=log-header>");
		htmlLog.print("Action");
		htmlLog.print("</td>");
		htmlLog.print("<td class=log-header>");
		htmlLog.print("Status");
		htmlLog.println("</td></tr>");
	}

	private void addConfigurationHeader(String date) {
		if (date == null)
			return;

		htmlLog.print("<tr class=header>");
		htmlLog.print("<td class=config-log-header colspan=4>");
		htmlLog.print(date);
		htmlLog.println("</td></tr>");
	}

	private void toggleRowColor() {
		if (rowType.equals("light-row"))
			rowType = "dark-row";
		else
			rowType = "light-row";
	}

	private void throwCoreException(Throwable e) throws CoreException {
		throw new CoreException(
			new Status(
				IStatus.ERROR,
				UpdateUI.getPluginId(),
				IStatus.ERROR,
				"Errors while creating installation history file.",
				e));
	}

	private void closeLog() {
		try {
			if (buffRead != null)
				buffRead.close();
			if (htmlLog != null)
				htmlLog.close();
		} catch (IOException e) {
		} finally {
			buffRead = null;
			htmlLog = null;
		}
	}

}
