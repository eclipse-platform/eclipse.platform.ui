/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.HelpEvaluationContext;
import org.eclipse.help.internal.dynamic.FilterResolver;
import org.eclipse.help.internal.search.federated.IndexerJob;
import org.eclipse.help.ui.internal.dynamic.FilterResolverExtension;
import org.eclipse.help.ui.internal.util.ErrorUtil;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * This class is Help UI plugin.
 */
public class HelpUIPlugin extends AbstractUIPlugin {

	public final static String PLUGIN_ID = "org.eclipse.help.ui"; //$NON-NLS-1$
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_INFOPOP = false;

	private static HelpUIPlugin plugin;

	// private static BundleContext bundleContext;
	/**
	 * Logs an Error message with an exception. Note that the message should already be localized to
	 * proper locale. ie: Resources.getString() should already have been called
	 */
	public static synchronized void logError(String message, Throwable ex) {
		logError(message, ex, true, false);
	}

	public static synchronized void logError(String message, Throwable ex, boolean log, boolean openDialog) {
		if (message == null)
			message = ""; //$NON-NLS-1$
		Status errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex);
		HelpPlugin.getDefault().getLog().log(errorStatus);
		if (openDialog)
			ErrorDialog.openError(null, null, null, errorStatus);
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
		// Make sure we cancel indexer if it is currently running
		Job.getJobManager().cancel(IndexerJob.FAMILY);
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

		FilterResolver.setExtension(new FilterResolverExtension());
		HelpEvaluationContext.setContext(HelpUIEvaluationContext.getContext());

		// bundleContext = context;
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
				HelpBasePlugin.setActivitySupport(new HelpActivitySupport(workbench));
			}
		}
	}
}
