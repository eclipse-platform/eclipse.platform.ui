/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.scheduler;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This plug-in is loaded on startup to fork a job that
 * searches for new plug-ins.
 */
public class UpdateScheduler extends AbstractUIPlugin implements IStartup {
	// Preferences
	public static final String P_ENABLED = "enabled";
	public static final String P_SCHEDULE = "schedule";
	public static final String VALUE_ON_STARTUP = "on-startup";
	public static final String VALUE_ON_SCHEDULE = "on-schedule";
	//The shared instance.
	private static UpdateScheduler plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public UpdateScheduler(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.update.internal.scheduler.UpdateSchedulerResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Returns the shared instance.
	 */
	public static UpdateScheduler getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getString(String key) {
		ResourceBundle bundle =
			UpdateScheduler.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public static String getFormattedMessage(String key, String[] args) {
		String text = getString(key);
		return java.text.MessageFormat.format(text, args);
	}

	public static String getFormattedMessage(String key, String arg) {
		String text = getString(key);
		return java.text.MessageFormat.format(text, new String[] { arg });
	}

	public static String getPluginId() {
		return getDefault().getDescriptor().getUniqueIdentifier();
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
			Platform.getPlugin("org.eclipse.core.runtime").getLog().log(status); //$NON-NLS-1$
		} else {
			MessageDialog.openInformation(
				getActiveWorkbenchShell(),
				null,
				status.getMessage());
		}
	}

	public static IWorkbenchPage getActivePage() {
		UpdateScheduler plugin = getDefault();
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

	public void startup() throws CoreException {
		super.startup();
		initializeDefaultPreferences();
	}

	private void initializeDefaultPreferences() {
		Preferences pref = getPluginPreferences();
		pref.setDefault(P_ENABLED, false);
		pref.setDefault(P_SCHEDULE, VALUE_ON_STARTUP);
	}

	public void shutdown() throws CoreException {
		super.shutdown();
	}

	public void earlyStartup() {
		Preferences pref = getPluginPreferences();
		// See if automatic search is enabled at all
		if (pref.getBoolean(P_ENABLED)==false) return;

		String schedule = pref.getString(P_SCHEDULE);
		if (schedule.equals(VALUE_ON_STARTUP))
			startSearch();
		else {
			long delay = computeDelay(pref);
			if (delay == -1) return;
			startSearch(delay);
		}
	}
	
	private long computeDelay(Preferences pref) {
		return -1;
	}
	
	private void startSearch() {
		startSearch(0);
	}

	private void startSearch(long delay) {
	}
}