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
package org.eclipse.help.ui.internal;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.ui.internal.util.*;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.*;

/**
 * This class is Help UI plugin.
 */
public class HelpUIPlugin extends AbstractUIPlugin {
	public final static String PLUGIN_ID = "org.eclipse.help.ui"; //$NON-NLS-1$
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_INFOPOP = false;

	private static HelpUIPlugin plugin;
	private static BundleContext bundleContext;
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
		HelpPlugin.getDefault().getLog().log(errorStatus);
	}
	/**
	 * Logs a Warning message with an exception. Note that the message should
	 * already be localized to proper local. ie: Resources.getString() should
	 * already have been called
	 */
	public static synchronized void logWarning(String message) {
		if (HelpPlugin.DEBUG) {
			if (message == null)
				message = ""; //$NON-NLS-1$
			Status warningStatus = new Status(IStatus.WARNING, PLUGIN_ID,
					IStatus.OK, message, null);
			HelpPlugin.getDefault().getLog().log(warningStatus);
		}
	}

	/**
	 * Provides access to singleton
	 * 
	 * @return HelpUIPlugin
	 */
	public static HelpUIPlugin getDefault() {
		return plugin;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
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
			DEBUG_INFOPOP = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/infopop")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_WORKBENCH)
			// UI may get activated during standalone
			BaseHelpSystem.setDefaultErrorUtil(new ErrorUtil());

		if (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_WORKBENCH) {
			// This is workbench scenario. Set activity support of base help to
			// use workbench activity support
			IWorkbench workbench = PlatformUI.getWorkbench();
			if (workbench != null) {
				HelpBasePlugin.setActivitySupport(new HelpActivitySupport(
						workbench));
			}
		}
	}
}
