/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.BundleContext;

/**
 * Help Base plug-in.
 */
public class HelpBasePlugin extends Plugin {

	public final static String PLUGIN_ID = "org.eclipse.help.base"; //$NON-NLS-1$
	private static HelpBasePlugin plugin;
	private File configurationDirectory;
	private BundleContext context;

	private IHelpActivitySupport helpActivitySupport = new IHelpActivitySupport() {
		public boolean isEnabled(String href) {
			return true;
		}
		public boolean isRoleEnabled(String href) {
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
		public String getShowAllMessage() {
			return null;
		}
		public String getDocumentMessage(boolean embedded) {
			return null;
		}
		public boolean getDocumentMessageUsesLiveHelp(boolean embedded) {
			return false;
		}
		public String getLocalScopeCheckboxLabel() {
			return null;
		}
	};

	public static synchronized void logError(String message, Throwable ex) {
		if (message == null) {
			message = ""; //$NON-NLS-1$
		}
		Status errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex);
		HelpBasePlugin.getDefault().getLog().log(errorStatus);
	}

	public static synchronized void logStatus(IStatus errorStatus) {
		HelpBasePlugin.getDefault().getLog().log(errorStatus);
	}

	public static HelpBasePlugin getDefault() {
		return plugin;
	}

	public void stop(BundleContext context) throws Exception {
		BaseHelpSystem.shutdown();
		this.context = null;
		plugin = null;
		super.stop(context);
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;

		// determine configuration location for this plug-in
		Location location = Platform.getConfigurationLocation();
		if (location != null) {
			URL configURL = location.getURL();
			if (configURL != null && configURL.getProtocol().startsWith("file")) { //$NON-NLS-1$
				configurationDirectory = new File(configURL.getFile(), PLUGIN_ID);
			}
		}
		if (configurationDirectory == null) {
			configurationDirectory = getStateLocation().toFile();
		}
		BaseHelpSystem.startup();
	}

	public static File getConfigurationDirectory() {
		return getDefault().configurationDirectory;
	}

	public static IHelpActivitySupport getActivitySupport() {
		return getDefault().helpActivitySupport;
	}

	public static void setActivitySupport(IHelpActivitySupport activitySupport) {
		getDefault().helpActivitySupport = activitySupport;
	}
	
	public static BundleContext getBundleContext() {
		return getDefault().context;
	}
}
