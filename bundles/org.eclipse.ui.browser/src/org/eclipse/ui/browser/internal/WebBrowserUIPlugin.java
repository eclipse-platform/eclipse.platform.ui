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
package org.eclipse.ui.browser.internal;

import java.text.MessageFormat;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
/**
 * The main web browser plugin class.
 */
public class WebBrowserUIPlugin extends AbstractUIPlugin {
	// Web browser plugin id
	public static final String PLUGIN_ID = "org.eclipse.ui.browser";

	// singleton instance of this class
	private static WebBrowserUIPlugin singleton;

	/**
	 * Create the WebBrowserUIPlugin
	 */
	public WebBrowserUIPlugin() {
		super();
		singleton = this;
	}

	/**
	 * Returns the singleton instance of this plugin.
	 *
	 * @return org.eclipse.ui.browser.internal.WebBrowserPlugin
	 */
	public static WebBrowserUIPlugin getInstance() {
		return singleton;
	}

	/**
	 * Returns the translated String found with the given key.
	 *
	 * @param key java.lang.String
	 * @return java.lang.String
	 */
	public static String getResource(String key) {
		try {
			return Platform.getResourceString(getInstance().getBundle(), key);
		} catch (Exception e) {
			return key;
		}
	}

	/**
	 * Returns the translated String found with the given key,
	 * and formatted with the given arguments using java.text.MessageFormat.
	 *
	 * @param key java.lang.String
	 * @param arg java.lang.String
	 * @return java.lang.String
	 */
	public static String getResource(String key, String arg) {
		try {
			String text = getResource(key);
			return MessageFormat.format(text, new String[] { arg });
		} catch (Exception e) {
			return key;
		}
	}

	/**
	 * Shuts down this plug-in and saves all plug-in state.
	 *
	 * @exception Exception
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		BrowserManager.safeDispose();
	}
}