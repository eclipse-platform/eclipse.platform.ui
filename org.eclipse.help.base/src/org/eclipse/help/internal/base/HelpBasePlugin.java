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
package org.eclipse.help.internal.base;
import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.datalocation.*;
import org.osgi.framework.*;
/**
 * Help Base plug-in.
 * 
 * @since 3.0
 */
public class HelpBasePlugin extends Plugin {

	public final static String PLUGIN_ID = "org.eclipse.help.base"; //$NON-NLS-1$
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_SEARCH = false;

	protected static HelpBasePlugin plugin;
	private static BundleContext bundleContext;

	private File configurationDirectory;

	private IHelpActivitySupport helpActivitySupport = new IHelpActivitySupport() {
		public boolean isEnabled(String href) {
			return true;
		}
		public boolean isEnabledTopic(String href, String locale) {
			return true;
		}
		public void enableActivities(String href) {
		}
		public boolean isFilteringEnabled() {
			return false;
		}
		public void setFilteringEnabled(boolean enabled) {
		}
		public boolean isUserCanToggleFiltering() {
			return false;
		}
	};
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
		HelpBasePlugin.getDefault().getLog().log(errorStatus);
	}
	/**
	 * Logs a Warning message with an exception. Note that the message should
	 * already be localized to proper local. ie: Resources.getString() should
	 * already have been called
	 */
	public static synchronized void logWarning(String message) {
		if (HelpBasePlugin.DEBUG) {
			if (message == null)
				message = ""; //$NON-NLS-1$
			Status warningStatus = new Status(IStatus.WARNING, PLUGIN_ID,
					IStatus.OK, message, null);
			HelpBasePlugin.getDefault().getLog().log(warningStatus);
		}
	}

	/**
	 * @return the singleton instance of the Help Base plugin
	 */
	public static HelpBasePlugin getDefault() {
		return plugin;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin.savePluginPreferences();
		BaseHelpSystem.shutdown();
		plugin = null;
		bundleContext = null;
		super.stop(context);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;
		// Setup debugging options
		DEBUG = isDebugging();
		if (DEBUG) {
			DEBUG_SEARCH = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/search")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// determine configuration location for this plug-in
		Location location = Platform.getConfigurationLocation();
		if (location != null) {
			URL configURL = location.getURL();
			if (configURL != null & configURL.getProtocol().startsWith("file")) { //$NON-NLS-1$
				configurationDirectory = new File(configURL.getFile(),
						PLUGIN_ID);
			}
		}
		if (configurationDirectory == null) {
			configurationDirectory = getStateLocation().toFile();
		}
		//
		BaseHelpSystem.startup();
	}

	/**
	 * Used to obtain directory where configuration (like help index) can be
	 * stored
	 */
	public static File getConfigurationDirectory() {
		return getDefault().configurationDirectory;
	}

	/**
	 * Used to obtain help activity support
	 * 
	 * @return instance of IHelpActivitySupport
	 */
	public static IHelpActivitySupport getActivitySupport() {
		return getDefault().helpActivitySupport;
	}

	/**
	 * Sets the activity support
	 * 
	 * @param activitySupport
	 */
	public static void setActivitySupport(IHelpActivitySupport activitySupport) {
		getDefault().helpActivitySupport = activitySupport;
	}
}
