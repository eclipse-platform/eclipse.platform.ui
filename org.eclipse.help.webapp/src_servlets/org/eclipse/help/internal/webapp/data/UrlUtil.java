/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import javax.servlet.http.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.base.util.*;

public class UrlUtil {
	// XML escaped characters mapping
	private static final String invalidXML[] = {"&", ">", "<", "\""}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String escapedXML[] = {
			"&amp;", "&gt;", "&lt;", "&quot;"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	// for Safari build 125.1 finds version 125
	static final Pattern safariPatern = Pattern.compile(
			"Safari/(\\d+)(?:\\.|\\s|$)", Pattern.CASE_INSENSITIVE); //$NON-NLS-1$

	// Default locale to use for serving requests to help
	private static String defaultLocale;
	// Locales that infocenter can serve in addition to the default locale.
	// null indicates that infocenter can serve every possible client locale.
	private static Collection locales;
	
	private static final int INFOCENTER_DIRECTION_BY_LOCALE = 1;
	private static final int INFOCENTER_DIRECTION_LTR = 2;
	private static final int INFOCENTER_DIRECTION_RTL = 3;
	private static int infocenterDirection = INFOCENTER_DIRECTION_BY_LOCALE;

	/**
	 * Encodes string for embedding in JavaScript source
	 */
	public static String JavaScriptEncode(String str) {
		char[] wordChars = new char[str.length()];
		str.getChars(0, str.length(), wordChars, 0);
		StringBuffer jsEncoded = new StringBuffer();
		for (int j = 0; j < wordChars.length; j++) {
			int unicode = wordChars[j];
			// to enhance readability, do not encode A-Z,a-z
			if (('A' <= unicode && unicode <= 'Z')
					|| ('a' <= unicode && unicode <= 'z')) {
				jsEncoded.append(wordChars[j]);
				continue;
			}
			// encode the character
			String charInHex = Integer.toString(unicode, 16).toUpperCase();
			switch (charInHex.length()) {
				case 1 :
					jsEncoded.append("\\u000").append(charInHex); //$NON-NLS-1$
					break;
				case 2 :
					jsEncoded.append("\\u00").append(charInHex); //$NON-NLS-1$
					break;
				case 3 :
					jsEncoded.append("\\u0").append(charInHex); //$NON-NLS-1$
					break;
				default :
					jsEncoded.append("\\u").append(charInHex); //$NON-NLS-1$
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
		if ("127.0.0.1".equals(reqIP)) { //$NON-NLS-1$
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
	 * Returns a URL that can be loaded from a browser. This method is used for
	 * all url's except those from the webapp plugin.
	 * 
	 * @param url
	 * @return String
	 */
	public static String getHelpURL(String url) {
		if (url == null || url.length() == 0)
			url = "about:blank"; //$NON-NLS-1$
		else if (url.startsWith("http:/") || url.startsWith("https:/")); //$NON-NLS-1$ //$NON-NLS-2$
		else if (url.startsWith("file:/") || url.startsWith("jar:file:/")) //$NON-NLS-1$ //$NON-NLS-2$
			url = "../topic/" + url; //$NON-NLS-1$
		else
			url = "../topic" + url; //$NON-NLS-1$
		return url;
	}

	public static boolean isBot(HttpServletRequest request) {
        String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
        if (agent==null)
		    return false;
        agent=agent.toLowerCase(Locale.ENGLISH);
		// sample substring Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)
		return agent.indexOf("bot") >= 0 || agent.indexOf("crawl") >= 0//$NON-NLS-1$ //$NON-NLS-2$
                || request.getParameter("bot") != null;//$NON-NLS-1$
    }

	public static boolean isGecko(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		if (agent==null)
		    return false;
		agent=agent.toLowerCase(Locale.ENGLISH);
		// sample substring Gecko/20020508
		// search for "gecko/" not to react to "like Gecko"
		return agent.indexOf("gecko/") >= 0; //$NON-NLS-1$
	}

	public static boolean isIE(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		if (agent==null)
		    return false;
		agent=agent.toLowerCase(Locale.ENGLISH);

		// When accessing with Bobby identified Bobby return 5.5 to allow
		// testing advanced UI as bobby cannot identifiy as IE >=5.5
		if (agent.startsWith("bobby/")) { //$NON-NLS-1$
			return true;
		}
		//

		return (agent.indexOf("msie") >= 0); //$NON-NLS-1$
	}

	public static String getIEVersion(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		if (agent==null)
		    return "0"; //$NON-NLS-1$

		agent=agent.toLowerCase(Locale.ENGLISH);
		// When accessing with Bobby identified Bobby return 5.5 to allow
		// testing advanced UI as bobby cannot identifiy as IE >=5.5
		if (agent.startsWith("bobby/")) { //$NON-NLS-1$
			return "5.5"; //$NON-NLS-1$
		}
		//

		int start = agent.indexOf("msie ") + "msie ".length(); //$NON-NLS-1$ //$NON-NLS-2$
		if (start < "msie ".length() || start >= agent.length()) //$NON-NLS-1$
			return "0"; //$NON-NLS-1$
		int end = agent.indexOf(";", start); //$NON-NLS-1$
		if (end <= start)
			return "0"; //$NON-NLS-1$
		return agent.substring(start, end);
	}

	public static boolean isKonqueror(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		if (agent==null)
		    return false;
		agent=agent.toLowerCase(Locale.ENGLISH); 
		return agent.indexOf("konqueror") >= 0; //$NON-NLS-1$
	}

	public static boolean isMozilla(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		if (agent==null)
		    return false;
		agent=agent.toLowerCase(Locale.ENGLISH);
		return agent.indexOf("mozilla/5") >= 0; //$NON-NLS-1$
	}

	public static String getMozillaVersion(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		if (agent==null)
		    return "0"; //$NON-NLS-1$
		agent=agent.toLowerCase(Locale.ENGLISH);
		if (agent.indexOf("mozilla/5") < 0) //$NON-NLS-1$
			return "0"; //$NON-NLS-1$
		int start = agent.indexOf("rv:") + "rv:".length(); //$NON-NLS-1$ //$NON-NLS-2$
		if (start < "rv:".length() || start >= agent.length()) //$NON-NLS-1$
			return "0"; //$NON-NLS-1$
		int end = agent.indexOf(")", start); //$NON-NLS-1$
		if (end <= start)
			return "0"; //$NON-NLS-1$
		return agent.substring(start, end);
	}

	public static boolean isOpera(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		if (agent==null)
		    return false;
		agent=agent.toLowerCase(Locale.ENGLISH);
		return agent.indexOf("opera") >= 0; //$NON-NLS-1$
	}

	public static boolean isSafari(HttpServletRequest request) {
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		if (agent==null)
		    return false;
		agent=agent.toLowerCase(Locale.ENGLISH);
		return agent.indexOf("safari/") >= 0; //$NON-NLS-1$
	}

	public static String getSafariVersion(HttpServletRequest request) {
		String version = "0"; //$NON-NLS-1$
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		if (agent==null)
		    return version;
		agent=agent.toLowerCase(Locale.ENGLISH);
		Matcher m = safariPatern.matcher(agent);
		boolean matched = m.find();
		if (matched) {
			version = m.group(1);
			while (version.length() < 3) {
				version = "0" + version; //$NON-NLS-1$
			}
		}
		return version;
	}
	/**
	 * 
	 * @param request
	 * @param response
	 *            HttpServletResponse or null (locale will not be persisted in
	 *            session cookie)
	 * @return
	 */
	public static Locale getLocaleObj(HttpServletRequest request,
			HttpServletResponse response) {
		String localeStr = getLocale(request, response);
		return getLocale(localeStr);
	}
	
	/**
	 * Returns the locale object from the provided string.
	 * @param localeStr the encoded locale string
	 * @return the Locale object
	 * 
	 * @since 3.1
	 */
	public static Locale getLocale(String localeStr) {
		if (localeStr.length() >= 5) {
			return new Locale(localeStr.substring(0, 2), localeStr.substring(3,
					5));
		} else if (localeStr.length() >= 2) {
			return new Locale(localeStr.substring(0, 2), ""); //$NON-NLS-1$
		} else {
			return Locale.getDefault();
		}
	}
	/**
	 * 
	 * @param request
	 * @param response
	 *            HttpServletResponse or null (locale will not be persisted in
	 *            session cookie)
	 * @return
	 */
	public static String getLocale(HttpServletRequest request,
			HttpServletResponse response) {
		if (defaultLocale == null) {
			initializeNL();
		}
		if ((BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER)
				|| request == null) {
			return defaultLocale;
		}

		// use locale passed in a request in current user session
		String forcedLocale = getForcedLocale(request, response);
		if (forcedLocale != null) {
			if (locales == null) {
				// infocenter set up to serve any locale
				return forcedLocale;
			}
			// match forced locale with one of infocenter locales
			if (locales.contains(forcedLocale)) {
				return forcedLocale;
			}
			// match language of forced locale with one of infocenter locales
			if (forcedLocale.length() > 2) {
				String ll = forcedLocale.substring(0, 2);
				if (locales.contains(ll)) {
					return ll;
				}
			}
		}

		// use one of the browser locales
		if (locales == null) {
			// infocenter set up to serve any locale
			return request.getLocale().toString();
		}
		// match client browser locales with one of infocenter locales
		for (Enumeration e = request.getLocales(); e.hasMoreElements();) {
			String locale = ((Locale) e.nextElement()).toString();
			if (locale.length() >= 5) {
				String ll_CC = locale.substring(0, 5);
				if (locales.contains(ll_CC)) {
					// client locale available
					return ll_CC;
				}
			}
			if (locale.length() >= 2) {
				String ll = locale.substring(0, 2);
				if (locales.contains(ll)) {
					// client language available
					return ll;
				}
			}
		}
		// no match
		return defaultLocale;
	}

	/**
	 * Obtains locale passed as lang parameter with a request during user
	 * session
	 * 
	 * @param request
	 * @param response
	 *            response or null; if null, locale will not be persisted (in
	 *            session cookie)
	 * @return ll_CC or ll or null
	 */
	private static String getForcedLocale(HttpServletRequest request,
			HttpServletResponse response) {
		// get locale passed in this request
		String forcedLocale = request.getParameter("lang"); //$NON-NLS-1$
		if (forcedLocale != null) {
			// save locale (in session cookie) for later use in a user session
			if (response != null) {
				Cookie cookieTest = new Cookie("lang", forcedLocale); //$NON-NLS-1$
				response.addCookie(cookieTest);
			}
		} else {
			// check if locale was passed earlier in this session
			Cookie[] cookies = request.getCookies();
			for (int c = 0; cookies != null && c < cookies.length; c++) {
				if ("lang".equals(cookies[c].getName())) { //$NON-NLS-1$
					forcedLocale = cookies[c].getValue();
					break;
				}
			}
		}

		// format forced locale
		if (forcedLocale != null) {
			if (forcedLocale.length() >= 5) {
				forcedLocale = forcedLocale.substring(0, 2) + "_" //$NON-NLS-1$
						+ forcedLocale.substring(3, 5);
			} else if (forcedLocale.length() >= 2) {
				forcedLocale = forcedLocale.substring(0, 2);
			}
		}
		return forcedLocale;
	}
	/**
	 * If locales for infocenter specified in prefernces or as command line
	 * parameters, this methods stores these locales in locales local variable
	 * for later access.
	 */
	private static synchronized void initializeNL() {
		if (defaultLocale != null) {
			// already initialized
			return;
		}
		initializeLocales();
		if ((BaseHelpSystem.getMode() == BaseHelpSystem.MODE_INFOCENTER)) {
			initializeIcDirection();
		}

	}
	/**
	 *  
	 */
	private static void initializeLocales() {
		// initialize default locale
		defaultLocale = Platform.getNL();
		if (defaultLocale == null) {
			defaultLocale = Locale.getDefault().toString();
		}
		if (BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER) {
			return;
		}

		// locale strings as passed in command line or in preferences
		List infocenterLocales = null;

		// first check if locales passed as command line arguments
		String[] args = Platform.getCommandLineArgs();
		boolean localeOption = false;
		for (int i = 0; i < args.length; i++) {
			if ("-locales".equalsIgnoreCase(args[i])) { //$NON-NLS-1$
				localeOption = true;
				infocenterLocales = new ArrayList();
				continue;
			} else if (args[i].startsWith("-")) { //$NON-NLS-1$
				localeOption = false;
				continue;
			}
			if (localeOption) {
				infocenterLocales.add(args[i]);
			}
		}
		// if no locales from command line, get them from preferences
		if (infocenterLocales == null) {
			StringTokenizer tokenizer = new StringTokenizer(HelpBasePlugin
					.getDefault().getPluginPreferences().getString("locales"), //$NON-NLS-1$
					" ,\t"); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				if (infocenterLocales == null) {
					infocenterLocales = new ArrayList();
				}
				infocenterLocales.add(tokenizer.nextToken());
			}
		}

		// format locales and collect in a set for lookup
		if (infocenterLocales != null) {
			locales = new HashSet(10, 0.4f);
			for (Iterator it = infocenterLocales.iterator(); it.hasNext();) {
				String locale = (String) it.next();
				if (locale.length() >= 5) {
					locales.add(locale.substring(0, 2).toLowerCase(Locale.ENGLISH) + "_" //$NON-NLS-1$
							+ locale.substring(3, 5).toUpperCase(Locale.ENGLISH));

				} else if (locale.length() >= 2) {
					locales.add(locale.substring(0, 2).toLowerCase(Locale.ENGLISH));
				}
			}
		}
	}
	
	private static void initializeIcDirection() {
		// from property
		String orientation = System.getProperty("eclipse.orientation"); //$NON-NLS-1$
		if ("rtl".equals(orientation)) { //$NON-NLS-1$
			infocenterDirection = INFOCENTER_DIRECTION_RTL;
			return;
		} else if ("ltr".equals(orientation)) { //$NON-NLS-1$
			infocenterDirection = INFOCENTER_DIRECTION_LTR;
			return;
		}
		// from command line
		String[] args = Platform.getCommandLineArgs();
		for (int i = 0; i < args.length; i++) {
			if ("-dir".equalsIgnoreCase(args[i])) { //$NON-NLS-1$
				if ((i + 1) < args.length
						&& "rtl".equalsIgnoreCase(args[i + 1])) { //$NON-NLS-1$
					infocenterDirection = INFOCENTER_DIRECTION_RTL;
					return;
				}
				infocenterDirection = INFOCENTER_DIRECTION_LTR;
				return;
			}
		}
		// by client locale
	}
	
	public static boolean isRTL(HttpServletRequest request,
			HttpServletResponse response) {
		if (BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER) {
			return BaseHelpSystem.isRTL();
		}
		{
			if (infocenterDirection == INFOCENTER_DIRECTION_RTL) {
				return true;
			} else if (infocenterDirection == INFOCENTER_DIRECTION_LTR) {
				return false;
			}
			String locale = getLocale(request, response);
			if (locale.startsWith("ar") || locale.startsWith("fa") //$NON-NLS-1$ //$NON-NLS-2$
					|| locale.startsWith("he") || locale.startsWith("iw") //$NON-NLS-1$ //$NON-NLS-2$
					| locale.startsWith("ur")) { //$NON-NLS-1$
				return true;
			}
			return false;
		}
	}
}
