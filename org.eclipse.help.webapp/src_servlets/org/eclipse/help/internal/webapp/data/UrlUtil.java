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
package org.eclipse.help.internal.webapp.data;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.*;

import org.eclipse.core.boot.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;

public class UrlUtil {
	// XML escaped characters mapping
	private static final String invalidXML[] = { "&", ">", "<", "\"" };
	private static final String escapedXML[] =
		{ "&amp;", "&gt;", "&lt;", "&quot;" };

	/**
	 * Encodes string for embedding in JavaScript source
	 */
	public static String JavaScriptEncode(String str) {
		char[] wordChars = new char[str.length()];
		str.getChars(0, str.length(), wordChars, 0);
		StringBuffer jsEncoded = new StringBuffer();
		for (int j = 0; j < wordChars.length; j++) {
			int unicode = (int) wordChars[j];
			// to enhance readability, do not encode A-Z,a-z
			if (((int) 'A' <= unicode && unicode <= (int) 'Z')
				|| ((int) 'a' <= unicode && unicode <= (int) 'z')) {
				jsEncoded.append(wordChars[j]);
				continue;
			}
			// encode the character
			String charInHex = Integer.toString(unicode, 16).toUpperCase();
			switch (charInHex.length()) {
				case 1 :
					jsEncoded.append("\\u000").append(charInHex);
					break;
				case 2 :
					jsEncoded.append("\\u00").append(charInHex);
					break;
				case 3 :
					jsEncoded.append("\\u0").append(charInHex);
					break;
				default :
					jsEncoded.append("\\u").append(charInHex);
					break;
			}
		}
		return jsEncoded.toString();
	}

	/**
	 * Encodes string for embedding in html source.
	 */
	public static String htmlEncode(String str) {

		for (int i = 0; i < invalidXML.length; i++)
			str = TString.change(str, invalidXML[i], escapedXML[i]);
		return str;
	}

	public static boolean isLocalRequest(HttpServletRequest request) {
		String reqIP = request.getRemoteAddr();
		if ("127.0.0.1".equals(reqIP)) {
			return true;
		}

		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			InetAddress[] addr = InetAddress.getAllByName(hostname);
			for (int i = 0; i < addr.length; i++) {
				// test all addresses retrieved from the local machine
				if (addr[i].getHostAddress().equals(reqIP))
					return true;
			}
		} catch (IOException ioe) {
		}
		return false;
	}

	/**
	 * Returns a URL that can be loaded from a browser.
	 * This method is used for all url's except those from the webapp plugin.
	 * @param url
	 * @return String
	 */
	public static String getHelpURL(String url) {
		if (url == null || url.length() == 0)
			url = "about:blank";
		else if (url.startsWith("http:/"));
		else if (url.startsWith("file:/") || url.startsWith("jar:file:/"))
			url = "../topic/" + url;
		else
			url = "../topic" + url;
		return url;
	}

	public static boolean isGecko(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent").toLowerCase();
		return agent.indexOf("gecko") >= 0;
	}

	public static boolean isIE(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent").toLowerCase();
		return (agent.indexOf("msie") >= 0);
	}

	public static String getIEVersion(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent").toLowerCase();
		int start = agent.indexOf("msie ") + "msie ".length();
		if (start < "msie ".length() || start >= agent.length())
			return "0";
		int end = agent.indexOf(";", start);
		if (end <= start)
			return "0";
		return agent.substring(start, end);
	}

	public static boolean isKonqueror(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent").toLowerCase();
		return agent.indexOf("konqueror") >= 0;
	}

	public static boolean isMozilla(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent").toLowerCase();
		return agent.indexOf("mozilla/5") >= 0;
	}

	public static String getMozillaVersion(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent").toLowerCase();
		if (agent.indexOf("mozilla/5") < 0)
			return "0";
		int start = agent.indexOf("rv:") + "rv:".length();
		if (start < "rv:".length() || start >= agent.length())
			return "0";
		int end = agent.indexOf(")", start);
		if (end <= start)
			return "0";
		return agent.substring(start, end);
	}

	public static boolean isOpera(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent").toLowerCase();
		return agent.indexOf("opera") >= 0;
	}

	public static String getLocale(HttpServletRequest request) {
		String locale = null;
		if ((HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER)
			&& request != null)
			locale = request.getLocale().toString();
		else
			locale = BootLoader.getNL();

		if (locale == null)
			locale = Locale.getDefault().toString();

		return locale;
	}
}
