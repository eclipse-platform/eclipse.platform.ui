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
package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IProgressProvider;
import org.eclipse.core.runtime.jobs.Job;

/**
 * JobProgressManager provides the progress monitor to the 
 * job manager and informs any ProgressContentProviders of changes.
 */
public class JobProgressManager implements IProgressProvider {

	private ArrayList providers = new ArrayList();
	private static JobProgressManager singleton;

	/**
	 * Get the progress manager currently in use.
	 * @return JobProgressManager
	 */
	public static JobProgressManager getInstance() {
		if (singleton == null)
			singleton = new JobProgressManager();
		return singleton;
	}

	/**
	 * The JobMonitor is the inner class that handles the IProgressMonitor 
	 * integration with the ProgressMonitor.
	 */
	private class JobMonitor implements IProgressMonitor {
		Job job;
		boolean cancelled = false;

		/**
		 * Create a monitor on the supplied job.
		 * @param newJob
		 */
		JobMonitor(Job newJob) {
			job = newJob;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
		 */
		public void beginTask(String name, int totalWork) {
			Iterator iterator = providers.iterator();
			while (iterator.hasNext()) {
				ProgressContentProvider provider =
					(ProgressContentProvider) iterator.next();
				provider.beginTask(job, name, totalWork);
			}
		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#done()
		 */
		public void done() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
		 */
		public void internalWorked(double work) {
			Iterator iterator = providers.iterator();
			while (iterator.hasNext()) {
				ProgressContentProvider provider =
					(ProgressContentProvider) iterator.next();
				provider.worked(job, work);
			}

		}
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
		 */
		public boolean isCanceled() {
			return cancelled;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
		 */
		public void setCanceled(boolean value) {
			cancelled = value;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
		 */
		public void setTaskName(String name) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
		 */
		public void subTask(String name) {
				Iterator iterator = providers.iterator();
			while (iterator.hasNext()) {
				ProgressContentProvider provider =
					(ProgressContentProvider) iterator.next();
				provider.subTask(job, name);
			}

		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
		 */
		public void worked(int work) {
			Iterator iterator = providers.iterator();
			while (iterator.hasNext()) {
				ProgressContentProvider provider =
					(ProgressContentProvider) iterator.next();
				provider.worked(job, work);
			}
		}
	};

	/**
	 * Create a new instance of the receiver.
	 */
	JobProgressManager() {
		Platform.getJobManager().setProgressProvider(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.IProgressProvider#createMonitor(org.eclipse.core.runtime.jobs.Job)
	 */
	public IProgressMonitor createMonitor(Job job) {
		return new JobMonitor(job);
	}

	/**
	 * Add a progress content provider to listen to the changes.
	 * @param provider
	 */
	void addProvider(ProgressContentProvider provider) {
		providers.add(provider);
	}

	/**
	 * Remove the supplied provider from the list of providers.
	 * @param provider
	 */
	void removeProvider(ProgressContentProvider provider) {
		providers.remove(provider);
	}

}
