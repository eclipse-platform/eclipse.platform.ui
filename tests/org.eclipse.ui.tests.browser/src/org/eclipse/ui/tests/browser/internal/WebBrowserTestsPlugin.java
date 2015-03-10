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
package org.eclipse.ui.tests.browser.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
/**
 * The web browser tests plugin class.
 */
public class WebBrowserTestsPlugin extends AbstractUIPlugin {
	// Web browser plugin id
	public static final String PLUGIN_ID = "org.eclipse.ui.browser.tests";

	// singleton instance of this class
	private static WebBrowserTestsPlugin singleton;

	/**
	 * Create the WebBrowserTestsPlugin
	 */
	public WebBrowserTestsPlugin() {
		super();
		singleton = this;
	}

	/**
	 * Returns the singleton instance of this plugin.
	 *
	 * @return org.eclipse.ui.internal.browser.WebBrowserPlugin
	 */
	public static WebBrowserTestsPlugin getInstance() {
		return singleton;
	}
}