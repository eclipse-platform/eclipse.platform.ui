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
package org.eclipse.update.internal.scheduler;

import java.lang.reflect.Constructor;
import java.util.Calendar;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;

/**
 * This plug-in is loaded on startup to fork a job that searches for new
 * plug-ins.
 */
public class SchedulerStartup implements IStartup {
	// Preferences
	public static final String P_ENABLED = "enabled"; //$NON-NLS-1$

	public static final String P_SCHEDULE = "schedule"; //$NON-NLS-1$

	public static final String VALUE_ON_STARTUP = "on-startup"; //$NON-NLS-1$

	public static final String VALUE_ON_SCHEDULE = "on-schedule"; //$NON-NLS-1$

	public static final String P_DOWNLOAD = "download"; // value is true or false, default isfalse //$NON-NLS-1$

	// values are to be picked up from the arryas DAYS and HOURS
	public static final String P_DAY = "day"; //$NON-NLS-1$

	public static final String P_HOUR = "hour"; //$NON-NLS-1$

	// Keeps track of the running job
	private Job job;

	static final Object automaticJobFamily = new Object();

	// Listener for job changes
	private IJobChangeListener jobListener;

	public static final String[] DAYS = {
			UpdateSchedulerMessages.SchedulerStartup_day,
			UpdateSchedulerMessages.SchedulerStartup_Monday,
			UpdateSchedulerMessages.SchedulerStartup_Tuesday,
			UpdateSchedulerMessages.SchedulerStartup_Wednesday,
			UpdateSchedulerMessages.SchedulerStartup_Thursday,
			UpdateSchedulerMessages.SchedulerStartup_Friday,
			UpdateSchedulerMessages.SchedulerStartup_Saturday,
			UpdateSchedulerMessages.SchedulerStartup_Sunday };

	public static final String[] HOURS = {
			UpdateSchedulerMessages.SchedulerStartup_1AM,
			UpdateSchedulerMessages.SchedulerStartup_2AM,
			UpdateSchedulerMessages.SchedulerStartup_3AM,
			UpdateSchedulerMessages.SchedulerStartup_4AM,
			UpdateSchedulerMessages.SchedulerStartup_5AM,
			UpdateSchedulerMessages.SchedulerStartup_6AM,
			UpdateSchedulerMessages.SchedulerStartup_7AM,
			UpdateSchedulerMessages.SchedulerStartup_8AM,
			UpdateSchedulerMessages.SchedulerStartup_9AM,
			UpdateSchedulerMessages.SchedulerStartup_10AM,
			UpdateSchedulerMessages.SchedulerStartup_11AM,
			UpdateSchedulerMessages.SchedulerStartup_12PM,
			UpdateSchedulerMessages.SchedulerStartup_1PM,
			UpdateSchedulerMessages.SchedulerStartup_2PM,
			UpdateSchedulerMessages.SchedulerStartup_3PM,
			UpdateSchedulerMessages.SchedulerStartup_4PM,
			UpdateSchedulerMessages.SchedulerStartup_5PM,
			UpdateSchedulerMessages.SchedulerStartup_6PM,
			UpdateSchedulerMessages.SchedulerStartup_7PM,
			UpdateSchedulerMessages.SchedulerStartup_8PM,
			UpdateSchedulerMessages.SchedulerStartup_9PM,
			UpdateSchedulerMessages.SchedulerStartup_10PM,
			UpdateSchedulerMessages.SchedulerStartup_11PM,
			UpdateSchedulerMessages.SchedulerStartup_12AM, };

	/**
	 * The constructor.
	 */
	public SchedulerStartup() {
		UpdateSchedulerPlugin.setScheduler(this);
	}

	public void earlyStartup() {
		scheduleUpdateJob();
	}

	public void scheduleUpdateJob() {
		Preferences pref = UpdateSchedulerPlugin.getDefault()
				.getPluginPreferences();
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
				case 0:
					return -1;
				case 1:
					return Calendar.MONDAY;
				case 2:
					return Calendar.TUESDAY;
				case 3:
					return Calendar.WEDNESDAY;
				case 4:
					return Calendar.THURSDAY;
				case 5:
					return Calendar.FRIDAY;
				case 6:
					return Calendar.SATURDAY;
				case 7:
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
	 * Computes the number of milliseconds from this moment to the next
	 * scheduled search. If that moment has already passed, returns 0L (start
	 * immediately).
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
			delay = ((delta_h * 60 - current_m) * 60 - current_s) * 1000
					- current_ms;
			return delay;
		} else {
			// Compute the delay for "every Xday at x o'clock"
			// Is it now ?
			if (target_d == current_d && target_h == current_h
					&& current_m == 0 && current_s == 0)
				return delay;

			int delta_d = target_d - current_d;
			if (target_d < current_d
					|| target_d == current_d
					&& (target_h < current_h || target_h == current_h
							&& current_m > 0))
				delta_d += 7;

			delay = (((delta_d * 24 + target_h - current_h) * 60 - current_m) * 60 - current_s)
					* 1000 - current_ms;

			return delay;
		}
		// return -1L;
	}

	private void startSearch(long delay) {
		if (job != null) {
			// cancel old job.
			// We need to deregister the listener first,so we won't
			// automatically start another job
			if (jobListener != null)
				Job.getJobManager().removeJobChangeListener(jobListener);
			Job.getJobManager().cancel(automaticJobFamily);
		}
		if (jobListener == null) {
			// using reflection to avoid premature class loading
			jobListener = createJobChangeAdapter();
			if (jobListener == null)
				return;
		}
		Job.getJobManager().addJobChangeListener(jobListener);
		String jobName = UpdateSchedulerMessages.AutomaticUpdatesJob_AutomaticUpdateSearch; //);
		boolean download = UpdateSchedulerPlugin.getDefault()
				.getPluginPreferences().getBoolean(
						UpdateSchedulerPlugin.P_DOWNLOAD);
		job = createUpdateJob(jobName, download);
		if (job != null)
			job.schedule(delay);

	}

	/*
	 * Loads the update job using reflection to avoid premature startup of the
	 * Update UI plug-in.
	 */

	private Job createUpdateJob(String name, boolean download) {
		try {
			Class theClass = Class
					.forName("org.eclipse.update.internal.scheduler.AutomaticUpdateJob"); //$NON-NLS-1$
			Constructor constructor = theClass.getConstructor(new Class[] {
					String.class, Boolean.TYPE, Boolean.TYPE });
			return (Job) constructor.newInstance(new Object[] { name,
					Boolean.TRUE, new Boolean(download) });
		} catch (Exception e) {
			UpdateSchedulerPlugin.logException(e, false);
			return null;
		}
	}

	/*
	 * Loads the job listener using reflection to avoid premature startup of the
	 * Update UI plug-in.
	 */
	private IJobChangeListener createJobChangeAdapter() {
		try {
			Class theClass = Class
					.forName("org.eclipse.update.internal.scheduler.UpdateJobChangeAdapter"); //$NON-NLS-1$
			Constructor constructor = theClass
					.getConstructor(new Class[] { SchedulerStartup.class });
			return (IJobChangeListener) constructor
					.newInstance(new Object[] { this });
		} catch (Exception e) {
			UpdateSchedulerPlugin.logException(e, false);
			return null;
		}
	}

	Job getJob() {
		return job;
	}
}
