/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.scheduler;

import java.lang.reflect.*;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.*;

/**
 * This plug-in is loaded on startup to fork a job that
 * searches for new plug-ins.
 */
public class UpdateSchedulerPlugin extends AbstractUIPlugin{
	// Preferences
	public static final String P_ENABLED = "enabled"; //$NON-NLS-1$
	public static final String P_SCHEDULE = "schedule"; //$NON-NLS-1$
	public static final String VALUE_ON_STARTUP = "on-startup"; //$NON-NLS-1$
	public static final String VALUE_ON_SCHEDULE = "on-schedule"; //$NON-NLS-1$
	public static final String P_DOWNLOAD = "download"; // value is true or false, default is false //$NON-NLS-1$

	//The shared instance.
	private static UpdateSchedulerPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	// singleton
	private static SchedulerStartup scheduler;

	/**
	 * The constructor.
	 */
	public UpdateSchedulerPlugin() {
		plugin = this;
	}


	public ResourceBundle getResourceBundle() {
		if (resourceBundle == null)
			try {
				resourceBundle = ResourceBundle.getBundle("org.eclipse.update.internal.scheduler.UpdateSchedulerResources"); //$NON-NLS-1$
			} catch (MissingResourceException x) {
				resourceBundle = null;
			}
		return resourceBundle;
	}

	/**
	 * Returns the shared instance.
	 */
	public static UpdateSchedulerPlugin getDefault() {
		return plugin;
	}

	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}

	public static void logException(Throwable e) {
		logException(e, true);
	}

	public static void logException(Throwable e, boolean showErrorDialog) {
		if (e instanceof InvocationTargetException) {
			e = ((InvocationTargetException) e).getTargetException();
		}

		IStatus status = null;
		if (e instanceof CoreException) {
			status = ((CoreException) e).getStatus();
		} else {
			String message = e.getMessage();
			if (message == null)
				message = e.toString();
			status =
				new Status(
					IStatus.ERROR,
					getPluginId(),
					IStatus.OK,
					message,
					e);
		}
		log(status, showErrorDialog);
	}

	public static void log(IStatus status, boolean showErrorDialog) {
		if (status.getSeverity() != IStatus.INFO) {
			if (showErrorDialog)
				ErrorDialog.openError(
					getActiveWorkbenchShell(),
					null,
					null,
					status);
//			 Should log on the update plugin's log
//			Platform.getPlugin("org.eclipse.core.runtime").getLog().log(status); //$NON-NLS-1$
			Bundle bundle = Platform.getBundle("org.eclipse.update.scheduler");  //$NON-NLS-1$
			Platform.getLog(bundle).log(status);
		} else {
			MessageDialog.openInformation(
				getActiveWorkbenchShell(),
				null,
				status.getMessage());
		}
	}

	public static IWorkbenchPage getActivePage() {
		UpdateSchedulerPlugin plugin = getDefault();
		IWorkbenchWindow window =
			plugin.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
			return window.getActivePage();
		return null;
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		return window != null ? window.getShell() : null;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}
	
	public static SchedulerStartup getScheduler() {
		// If the scheduler was disabled, it does not get initialized
		if (scheduler == null)
			scheduler = new SchedulerStartup();
		return scheduler;
	}
	
	static void setScheduler(SchedulerStartup scheduler) {
		UpdateSchedulerPlugin.scheduler = scheduler;
	}
}
