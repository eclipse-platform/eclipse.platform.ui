/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet;
import java.io.*;
import java.net.InetAddress;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.TString;

public class UrlUtil {
	// XML escaped characters mapping
	private static final String invalidXML[] = { "&", ">", "<", "\"" };
	private static final String escapedXML[] =
		{ "&amp;", "&gt;", "&lt;", "&quot;" };

	/**
	 * Decodes strings encoded with Javascript 1.3 escape
	 * Handles DBCS charactes that escape encoded as %uHHLL.
	 */
	public static String unescape(String encodedURL) {
		int len = encodedURL.length();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < len;) {
			ByteArrayOutputStream tempOs;
			switch (encodedURL.charAt(i)) {
				case '%' :
					if ((len > i + 1) && encodedURL.charAt(i + 1) != 'u') {
						// byte encoded as %XX
						if (len >= i + 3) {
							tempOs = new ByteArrayOutputStream(1);
							tempOs.write(
								Integer.parseInt(
									encodedURL.substring(i + 1, i + 3),
									16));
							try {
								buf.append(
									new String(
										tempOs.toByteArray(),
										"ISO8859_1"));
							} catch (UnsupportedEncodingException uee) {
								return null;
							}
						}
						i += 3;

					} else {
						// char escaped to the form %uHHLL
						if (len >= i + 6) {
							tempOs = new ByteArrayOutputStream(2);
							tempOs.write(
								Integer.parseInt(
									encodedURL.substring(i + 2, i + 4),
									16));
							tempOs.write(
								Integer.parseInt(
									encodedURL.substring(i + 4, i + 6),
									16));
							try {
								buf.append(
									new String(
										tempOs.toByteArray(),
										"UnicodeBigUnmarked"));
							} catch (UnsupportedEncodingException uee) {
								return null;
							}
						}
						i += 6;

					}

					break;
				case '+' : //exception from standard
					buf.append(' ');
					i++;
					break;
				default :
					tempOs = new ByteArrayOutputStream(1);
					tempOs.write(encodedURL.charAt(i++));
					try {
						buf.append(new String(tempOs.toByteArray(), "UTF8"));
					} catch (UnsupportedEncodingException uee) {
						return null;
					}

					break;
			}

		}
		return buf.toString();
	}
	/**
	 * Obtains parameter from request without decoding it
	 */
	public static String getRawRequestParameter(
		HttpServletRequest request,
		String parameterName) {
		String[] values = getRawRequestParameters(request, parameterName);
		if (values.length > 0) {
			return values[0];
		}
		return null;
	}
	/**
	 * Obtains values of a parameter from request query string
	 * withoud decoding them
	 * @return String[]
	 */
	public static String[] getRawRequestParameters(
		HttpServletRequest request,
		String parameterName) {
		String query = request.getQueryString();
		if (query == null || "".equals(query)) {
			return new String[0];
		}
		List values = new ArrayList();
		StringTokenizer stok = new StringTokenizer(query, "&");
		while (stok.hasMoreTokens()) {
			String nameEqValue = stok.nextToken();
			int equalsPosition = nameEqValue.indexOf("=");
			if (equalsPosition >= 0
				&& parameterName.equals(
					nameEqValue.substring(0, equalsPosition))) {
				String val = nameEqValue.substring(equalsPosition + 1);
				values.add(val);
			}
		}
		return (String[]) values.toArray(new String[values.size()]);
	}
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

	/**
	 * Validates a file:// URL by ensuring the file is only accessed 
	 * from a local installation.
	 */
	public static boolean validate(
		String fileURL,
		HttpServletRequest req,
		ServletContext context) {
		// first check if we are running outside the workbench
		if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER) {
			return false;
		}

		// check that the request IP is a local IP
		String reqIP = req.getRemoteAddr();
		if ("127.0.0.1".equals(reqIP))
			return true;

		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			InetAddress[] addr = InetAddress.getAllByName(hostname);
			for (int i = 0; i < addr.length; i++) {
				// test all addresses retrieved from the local machine
				if (addr[i].getHostAddress().equals(reqIP))
					return true;
			}
		} catch (IOException ioe) {
			return false;
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
		else if (url.startsWith("file:/"))
			url = "../content/" + url;
		else
			url = "../content/help:" + url;
		return url;
	}
	public static boolean isIE(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent").toLowerCase();
		boolean opera = (agent.indexOf("opera") >= 0);
		return (agent.indexOf("msie") >= 0) && !opera;
	}

	public static boolean isMozilla(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent").toLowerCase();
		boolean opera = (agent.toLowerCase().indexOf("opera") >= 0);
		return !isIE(request) && (agent.indexOf("mozilla/5") >= 0) && !opera;
	}
}