/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Class that maintains a list of recently activated activities. Note that if
 * an activity is activated, quickly deactivated and activated again (before
 * the initial activation cleanup occurs) then the activity will time out after
 * the first job, not the second. This is such a rare (and impossible?)
 * occurance that it's probably acceptable.
 */
class RecentActivityManager implements IActivityManagerListener {

	/**
	 * Job that prunes records that are now 'stale'. */
	private class CleanupJob extends Job {

		/**
		 * The items to remove when this job runs */
		private Collection fItemsToCleanUp;

		/**
		 * Create a new job to clean up with the supplied items. */
		protected CleanupJob(Collection itemsToCleanUp) {
			super(RoleMessages.getString("RecentActivityManager.CleanupJobName")); //$NON-NLS-1$
			fItemsToCleanUp = itemsToCleanUp;
		}

		/*
		 * (non-Javadoc) @see
		 * org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor monitor) {
			monitor.beginTask(RoleMessages.getString("RecentActivityManager.CleanupTaskName"), 2); //$NON-NLS-1$
			synchronized (fRecent) {
				// remove all of the jobs
				fRecent.removeAll(fItemsToCleanUp);
			}
			monitor.worked(1);
			synchronized (jobs) {
				// remove this job from the job list
				jobs.remove(this);
			}
			monitor.worked(1);

			IStatus status = new Status(IStatus.OK, WorkbenchPlugin.PI_WORKBENCH, IStatus.OK, "", null); //$NON-NLS-1$
			return status;
		}
	}

	/**
	 * A list of the cleanup jobs */
	protected List jobs = new LinkedList();

	/**
	 * How long a given activity remains 'recent' */
	protected long lifetime;

	/**
	 * The recent records */
	protected Set fRecent = new HashSet(17);

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param lifetime
	 *           how long a given activated task remains 'recent'
	 */
	RecentActivityManager(long lifetime) {
		RoleManager.getInstance().addActivityManagerListener(this);
		setLifetime(lifetime);
	}

	/*
	 * (non-Javadoc) @see
	 * org.eclipse.ui.internal.roles.IActivityManagerListener#activityManagerChanged(org.eclipse.ui.internal.roles.ActivityManagerEvent)
	 */
	public void activityManagerChanged(ActivityManagerEvent event) {
		Activity[] enabled = event.getEnabled();

		if (enabled.length > 0) {
			List newItems = new ArrayList(enabled.length);
			for (int i = 0; i < enabled.length; i++) {
				newItems.add(enabled[i].getId());
			}

			synchronized (fRecent) {
				fRecent.addAll(newItems);
			}

			// schedule a cleanup job
			Job job = new CleanupJob(newItems);
			jobs.add(job);
			job.schedule(lifetime);
		}
	}

	/**
	 * Return how long a given activity remains 'recent'.
	 * @return long 
	 */
	protected long getLifetime() {
		return lifetime;
	}

	/**
	 * Get the recently enabled Activity ids.
	 * 
	 * @return String[]
	 */
	String[] getRecentActivityIds() {
		String[] activityIds;
		synchronized (fRecent) {
			activityIds = new String[fRecent.size()];
			int idx = 0;
			for (Iterator i = fRecent.iterator(); i.hasNext();) {
				activityIds[idx++] = (String) i.next();
			}
		}
		return activityIds;
	}

	/**
	 * Set the lifetime of new active objects. Note that this does not effect
	 * activities already added to this container.
	 * 
	 * @param lifetime: how long a given activity remains 'recent'
	 */
	protected void setLifetime(long newLifetime) {
		lifetime = newLifetime;
	}

	/**
	 * Clean up listeners and shut down the cleanup job. 
	 */
	void shutdown() {
		RoleManager.getInstance().removeActivityManagerListener(this);
		// cancel any scheduled jobs from running
		synchronized (jobs) {
			for (Iterator i = jobs.iterator(); i.hasNext();) {
				Job job = (Job) i.next();
				// running jobs will end shortly anyway
				// don't try and cancel, let them go naturally.
				if (job.getState() != Job.RUNNING) {
					job.cancel();
					i.remove();
				}
			}
		}
	}
}
