/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;

import javax.servlet.http.*;

import org.eclipse.help.internal.webapp.*;
import org.eclipse.help.internal.webapp.data.*;

/**
 * Utilities for working with cookies
 * 
 * @since 3.0
 */
public class CookieUtil {
	private static final int COOKIE_LIFE = 5 * 365 * 24 * 60 * 60;
	private static final int MAX_COOKIE_PAYLOAD = 4096
			- "wset01=".length() - "81920<".length() - 1; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * @return null or String
	 */
	public static String getCookieValue(String name, HttpServletRequest request) {
		String ret = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				if (name.equals(cookies[i].getName())) {
					ret = cookies[i].getValue();
					break;
				}
			}
		}
		if (HelpWebappPlugin.DEBUG_WORKINGSETS) {
			System.out.println("CookieUtil.getCookieValue(" //$NON-NLS-1$
					+ name + ", " //$NON-NLS-1$
					+ request.getRequestURI() + ") returning " //$NON-NLS-1$
					+ ret);
		}
		return ret;
	}
	public static void setCookieValue(String name, String value,
			HttpServletResponse response) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(COOKIE_LIFE);
		response.addCookie(cookie);
		if (HelpWebappPlugin.DEBUG_WORKINGSETS) {
			System.out
					.println("CookieUtil.setCookieValue(" + name + ", " + value + ",...)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	public static void deleteCookie(String name, HttpServletResponse response) {
		Cookie cookie = new Cookie(name, ""); //$NON-NLS-1$
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
	/**
	 * Saves string in multiple browser cookies. Cookies can store limited
	 * length string. This method will attemt to split string among multiple
	 * cookies. The following cookies will be set name1=length <substing1
	 * name2=substrging2 ... namen=substringn
	 * 
	 * @param data
	 *            a string containing legal characters for cookie value
	 * @throws IOException
	 *             when data is too long.
	 */
	public static void saveString(String name, String data, int maxCookies,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		int len = data.length();
		int n = len / MAX_COOKIE_PAYLOAD;
		if (n > maxCookies) {
			throw new IOException(WebappResources.getString(
					"CookieUtil.tooManyCookiesNeeded", UrlUtil.getLocaleObj( //$NON-NLS-1$
							request, response)));
		}
		for (int i = 1; i <= n; i++) {
			if (i == 1) {
				setCookieValue(name + "1", //$NON-NLS-1$
						len + "<" + data.substring(0, MAX_COOKIE_PAYLOAD), //$NON-NLS-1$
						response);
			} else {
				setCookieValue(name + i, data.substring(MAX_COOKIE_PAYLOAD
						* (i - 1), MAX_COOKIE_PAYLOAD * i), response);
			}
		}
		if (len % MAX_COOKIE_PAYLOAD > 0) {
			if (n == 0) {
				setCookieValue(name + "1", //$NON-NLS-1$
						len + "<" + data.substring(0, len), //$NON-NLS-1$
						response);
			} else {
				setCookieValue(name + (n + 1), data.substring(
						MAX_COOKIE_PAYLOAD * n, len), response);
			}
		}

		// if using less cookies than maximum, delete not needed cookies from
		// last time
		for (int i = n + 1; i <= maxCookies; i++) {
			if (i == n + 1 && len % MAX_COOKIE_PAYLOAD > 0) {
				continue;
			}
			if (getCookieValue(name + i, request) != null) {
				deleteCookie(name + i, response);
			} else {
				break;
			}
		}
	}
	/**
	 * @return null or String
	 */
	public static String restoreString(String name, HttpServletRequest request) {
		String value1 = CookieUtil.getCookieValue(name + "1", request); //$NON-NLS-1$
		if (value1 == null) {
			// no cookie
			return null;
		}
		String lengthAndSubstring1[] = value1.split("<"); //$NON-NLS-1$
		if (lengthAndSubstring1.length < 2) {
			return null;
		}
		int len = 0;
		try {
			len = Integer.parseInt(lengthAndSubstring1[0]);
		} catch (NumberFormatException nfe) {
			return null;
		}
		if (len <= 0) {
			return null;
		}
		StringBuffer data = new StringBuffer(len);
		data.append(lengthAndSubstring1[1]);
		int n = len / MAX_COOKIE_PAYLOAD;
		for (int i = 2; i <= n; i++) {
			String substring = CookieUtil.getCookieValue(name + i, request);
			if (substring == null) {
				return null;
			}
			data.append(substring);
		}
		if (len % MAX_COOKIE_PAYLOAD > 0 && n > 0) {
			String substring = CookieUtil.getCookieValue(name + (n + 1),
					request);
			if (substring == null) {
				return null;
			}
			data.append(substring);
		}

		if (data.length() != len) {
			return null;
		}

		return data.toString();
	}
}
