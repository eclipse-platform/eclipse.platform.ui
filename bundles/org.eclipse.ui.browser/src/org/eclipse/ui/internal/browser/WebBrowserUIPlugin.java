/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
/**
 * The main web browser plugin class.
 */
public class WebBrowserUIPlugin extends AbstractUIPlugin {
	// Web browser plugin id
	public static final String PLUGIN_ID = "org.eclipse.ui.browser"; //$NON-NLS-1$

	// singleton instance of this class
	private static WebBrowserUIPlugin singleton;
	
	// cached copy of all browsers
	private static List<BrowserExt> browsers;

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
	 * @return org.eclipse.ui.internal.browser.WebBrowserPlugin
	 */
	public static WebBrowserUIPlugin getInstance() {
		return singleton;
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
	
	/**
	 * Returns an array of all known browers.
	 * <p>
	 * A new array is returned on each call, so clients may store or modify the result.
	 * </p>
	 * 
	 * @return a possibly-empty array of browser instances {@link IClient}
	 */
	public static IBrowserExt[] getBrowsers() {
		if (browsers == null)
			loadBrowsers();
		IBrowserExt[] c = new IBrowserExt[browsers.size()];
		browsers.toArray(c);
		return c;
	}
	
	public static IBrowserExt findBrowsers(String executable) {
		IBrowserExt[] browsers2 = getBrowsers();
		if (browsers2 == null || executable == null)
			return null;
		
		int ind1 = executable.lastIndexOf("/"); //$NON-NLS-1$
		int ind2 = executable.lastIndexOf("\\"); //$NON-NLS-1$
		if (ind2 > ind1)
			ind1 = ind2;
		executable = executable.substring(ind1 + 1);
		
		String os = Platform.getOS();
		int size = browsers2.length;
		for (int i = 0; i < size; i++) {
			if (browsers2[i].getOS().toLowerCase().indexOf(os) != -1) {
				if (browsers2[i].isAvailable()) {
					String executable2 = browsers2[i].getExecutable();
					if (executable.startsWith(executable2))
						return browsers2[i];
				}
			}
		}
		return null;
	}
	
	/**
	 * Load the browsers extension point.
	 */
	private static synchronized void loadBrowsers() {
		if (browsers != null)
			return;
		Trace.trace(Trace.CONFIG, "->- Loading .browsers extension point ->-"); //$NON-NLS-1$
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] cf = registry.getConfigurationElementsFor(PLUGIN_ID, "browsers"); //$NON-NLS-1$

		int size = cf.length;
		browsers = new ArrayList<BrowserExt>(size);
		for (int i = 0; i < size; i++) {
			try {
				browsers.add(new BrowserExt(cf[i]));
				Trace.trace(Trace.CONFIG, "  Loaded browser: " + cf[i].getAttribute("id")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Throwable t) {
				Trace.trace(Trace.SEVERE, "  Could not load browser: " + cf[i].getAttribute("id"), t); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		Trace.trace(Trace.CONFIG, "-<- Done loading .browsers extension point -<-"); //$NON-NLS-1$
	}
	
	/**
	 * Logs an Error message with an exception. Note that the message should
	 * already be localized to proper locale. ie: Resources.getString() should
	 * already have been called
	 */
	public static synchronized void logError(String message, Throwable ex) {
		if (message == null)
			message = ""; //$NON-NLS-1$
		Status errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK,
				message, ex);
		WebBrowserUIPlugin.getInstance().getLog().log(errorStatus);
	}

	/**
	 * Logs a Warning message with an exception. Note that the message should
	 * already be localized to proper local. ie: Resources.getString() should
	 * already have been called
	 */
	/*public static synchronized void logWarning(String message) {
		if (WebBrowserUIPlugin.DEBUG) {
			if (message == null)
				message = ""; //$NON-NLS-1$
			Status warningStatus = new Status(IStatus.WARNING, PLUGIN_ID,
					IStatus.OK, message, null);
			WebBrowserUIPlugin.getInstance().getLog().log(warningStatus);
		}
	}*/
}