/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
/**
 * Preferences for the Web browser.
 */
public class WebBrowserPreference {
	protected static final String PREF_BROWSER_HISTORY = "webBrowserHistory";
	protected static final String PREF_INTERNAL_WEB_BROWSER_HISTORY = "internalWebBrowserHistory";
   protected static final String PREF_USE_INTERNAL_BROWSER = "use-internal-browser";

	/**
	 * WebBrowserPreference constructor comment.
	 */
	private WebBrowserPreference() {
		super();
	}

	/**
	 * Returns the URL to the homepage.
	 * 
	 * @return java.lang.String
	 */
	public static String getHomePageURL() {
		try {
			// get the default home page
			URL url = WebBrowserUIPlugin.getInstance().getBundle().getEntry("home/home.html");
			url = Platform.resolve(url);
			return url.toExternalForm();
		} catch (Exception e) {
			return "http://www.eclipse.org";
		}
	}

	/**
	 * Returns the preference store.
	 *
	 * @return the preference store
	 */
	protected static IPreferenceStore getPreferenceStore() {
		return WebBrowserUIPlugin.getInstance().getPreferenceStore();
	}

	/**
	 * Returns the Web browser history list.
	 * 
	 * @return java.util.List
	 */
	public static List getInternalWebBrowserHistory() {
		String temp = getPreferenceStore().getString(PREF_INTERNAL_WEB_BROWSER_HISTORY);
		StringTokenizer st = new StringTokenizer(temp, "|*|");
		List l = new ArrayList();
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			l.add(s);
		}
		return l;
	}
	
	/**
	 * Sets the Web browser history.
	 *
	 * @param list the history
	 */
	public static void setInternalWebBrowserHistory(List list) {
		StringBuffer sb = new StringBuffer();
		if (list != null) {
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				String s = (String) iterator.next();
				sb.append(s);
				sb.append("|*|");
			}
		}
		getPreferenceStore().setValue(PREF_INTERNAL_WEB_BROWSER_HISTORY, sb.toString());
		WebBrowserUIPlugin.getInstance().savePluginPreferences();
	}

	/**
	 * Returns whether the internal browser is used by default
	 * 
	 * @return true if the internal browser is used by default
	 */
	public static boolean isDefaultUseInternalBrowser() {
		return WebBrowserUtil.canUseInternalWebBrowser();
	}

   /**
	 * Returns whether the internal browser is being used
	 * 
	 * @return true if the internal browser is being used
	 */
	public static boolean isUseInternalBrowser() {
		return getPreferenceStore().getBoolean(PREF_USE_INTERNAL_BROWSER);
	}

	/**
	 * Sets whether the internal browser is used
	 *
	 * @param b true to use the internal web browser
	 */
	public static void setUseInternalBrowser(boolean b) {
		getPreferenceStore().setValue(PREF_USE_INTERNAL_BROWSER, b);
		WebBrowserUIPlugin.getInstance().savePluginPreferences();
	}
}