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

import java.lang.reflect.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.*;

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
	
	public static final String P_DOWNLOAD = "download"; // value is true or false, default is false

	// values are to be picked up from the arryas DAYS and HOURS 
	public static final String P_DAY = "day";
	public static final String P_HOUR = "hour";

	//The shared instance.
	private static UpdateScheduler plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	// Keeps track of running job
	private Job job;
	// Listener for job changes
	private UpdateJobChangeAdapter jobListener;

	public static final String[] DAYS =
		{
			"Every day",
			"Every Monday",
			"Every Tuesday",
			"Every Wednesday",
			"Every Thursday",
			"Every Friday",
			"Every Saturday",
			"Every Sunday" };

	public static final String[] HOURS =
		{
			"1:00 AM",
			"2:00 AM",
			"3:00 AM",
			"4:00 AM",
			"5:00 AM",
			"6:00 AM",
			"7:00 AM",
			"8:00 AM",
			"9:00 AM",
			"10:00 AM",
			"11:00 AM",
			"12:00 PM",
			"1:00 PM",
			"2:00 PM",
			"3:00 PM",
			"4:00 PM",
			"5:00 PM",
			"6:00 PM",
			"7:00 PM",
			"8:00 PM",
			"9:00 PM",
			"10:00 PM",
			"11:00 PM",
			"12:00 AM",
			};

	private class UpdateJobChangeAdapter extends JobChangeAdapter {
		public void done(Job job, IStatus result) {
			if (job == UpdateScheduler.this.job) {
				scheduleUpdateJob();
			}
		}
	}

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

		jobListener = new UpdateJobChangeAdapter();
		Platform.getJobManager().addJobChangeListener(jobListener);
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
		pref.setDefault(P_DOWNLOAD, false);
	}

	public void shutdown() throws CoreException {
		super.shutdown();
	}

	public void earlyStartup() {
		scheduleUpdateJob();
	}

	public void scheduleUpdateJob() {
		Preferences pref = getPluginPreferences();
		// See if automatic search is enabled at all
		if (pref.getBoolean(P_ENABLED) == false)
			return;

		String schedule = pref.getString(P_SCHEDULE);
		long delay = -1L;
		if (schedule.equals(VALUE_ON_STARTUP))
			// have we already started a job ?
			if (job == null)
				delay = 0L;
			else
				delay = -1L;
		else
			delay = computeDelay(pref);
		if (delay == -1L)
			return;
		startSearch(delay);
	}

	private int getDay(Preferences pref) {
		String day = pref.getString(P_DAY);
		for (int d = 0; d < DAYS.length; d++)
			if (DAYS[d].equals(day))
				switch (d) {
					case 0 :
						return -1;
					case 1 :
						return Calendar.MONDAY;
					case 2 :
						return Calendar.TUESDAY;
					case 3 :
						return Calendar.WEDNESDAY;
					case 4 :
						return Calendar.THURSDAY;
					case 5 :
						return Calendar.FRIDAY;
					case 6 :
						return Calendar.SATURDAY;
					case 7 :
						return Calendar.SUNDAY;
				}
		return -1;
	}

	private int getHour(Preferences pref) {
		String hour = pref.getString(P_HOUR);
		for (int h = 0; h < HOURS.length; h++)
			if (HOURS[h].equals(hour))
				return h + 1;
		return 1;
	}
	/*
	 * Computes the number of milliseconds from this moment
	 * to the next scheduled search. If that moment has
	 * already passed, returns 0L (start immediately).
	 */
	private long computeDelay(Preferences pref) {

		int target_d = getDay(pref);
		int target_h = getHour(pref);

		Calendar calendar = Calendar.getInstance();
		// may need to use the BootLoader locale
		int current_d = calendar.get(Calendar.DAY_OF_WEEK);
		// starts with SUNDAY
		int current_h = calendar.get(Calendar.HOUR_OF_DAY);
		int current_m = calendar.get(Calendar.MINUTE);
		int current_s = calendar.get(Calendar.SECOND);
		int current_ms = calendar.get(Calendar.MILLISECOND);

		long delay = 0L; // milliseconds

		if (target_d == -1) {
			// Compute the delay for "every day at x o'clock"
			// Is it now ?
			if (target_h == current_h && current_m == 0 && current_s == 0)
				return delay;

			int delta_h = target_h - current_h;
			if (target_h <= current_h)
				delta_h += 24;
			delay =
				((delta_h * 60 - current_m) * 60 - current_s) * 1000 - current_ms;
			return delay;
		} else {
			// Compute the delay for "every Xday at x o'clock"
			// Is it now ?
			if (target_d == current_d
				&& target_h == current_h
				&& current_m == 0
				&& current_s == 0)
				return delay;

			int delta_d = target_d - current_d;
			if (target_d < current_d
				|| target_d == current_d
				&& (target_h < current_h
					|| target_h == current_h
					&& current_m > 0))
				delta_d += 7;
			int delta_h = target_h - current_h;

			delay =
				(((delta_d * 24 + target_h - current_h) * 60 - current_m) * 60 - current_s)* 1000 - current_ms;

			return delay;
		}
		//return -1L;
	}

	private void startSearch(long delay) {
		if (job != null) {
			// cancel old job.
			// We need to deregister the listener first,so we won't automatically start another job
			Platform.getJobManager().removeJobChangeListener(jobListener);
			Platform.getJobManager().cancel(AutomaticUpdatesJob.family);
			Platform.getJobManager().addJobChangeListener(jobListener);
		}
		job = new AutomaticUpdatesJob();
		job.schedule(delay);
	}
}