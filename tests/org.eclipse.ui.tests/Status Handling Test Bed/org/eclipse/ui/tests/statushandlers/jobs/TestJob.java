/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.statushandlers.jobs;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Base class for a simple test job with configurable parameters.
 */
public class TestJob extends Job {
	/**
	 * A family identifier for all test jobs
	 */
	public static final Object FAMILY_TEST_JOB = new Object();
	/**
	 * Total duration that the test job should sleep, in milliseconds.
	 */
	private long duration;

	private boolean reschedule;

	private long rescheduleWait;

	private boolean returnError;

	private Throwable toBeThrown;

	private long throwAfter;

	/**
	 * Creates a new test job
	 * 
	 * @param duration
	 *            total time that the test job should sleep, in milliseconds
	 * @param lock
	 *            whether the job should use a workspace scheduling rule
	 * @param rescheduleWait
	 * @param reschedule
	 * 
	 * @param throwAfter
	 * @param toBeThrown
	 * @param returnError
	 */
	public TestJob(long duration, boolean lock, boolean reschedule,
			long rescheduleWait, long throwAfter, Throwable toBeThrown,
			boolean returnError) {
		super("Test job"); //$NON-NLS-1$
		this.duration = duration;
		this.reschedule = reschedule;
		this.rescheduleWait = rescheduleWait;
		this.throwAfter = throwAfter;
		this.toBeThrown = toBeThrown;
		this.returnError = returnError;

		if (lock)
			setRule(ResourcesPlugin.getWorkspace().getRoot());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.jobs.InternalJob#belongsTo(java.lang.Object)
	 */
	public boolean belongsTo(Object family) {
		if (family instanceof TestJob) {
			return true;
		}
		return family == FAMILY_TEST_JOB;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {

		final long sleep = 10;
		int ticks = (int) (duration / sleep);
		int ticksToThrow = (int) (throwAfter / sleep);

		monitor.beginTask("UI Task", ticks);
		monitor.setTaskName("UI Task");
		try {
			for (int i = 0; i < ticks; i++) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				monitor.subTask("Processing tick #" + i); //$NON-NLS-1$
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				if (i == ticksToThrow) {
					if (returnError) {
						MultiStatus result = new MultiStatus(
								"org.eclipse.ui.examples.statushandling.jobs", 1, "This is the MultiStatus message", new RuntimeException("This is the MultiStatus exception")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						result
								.add(new Status(
										IStatus.ERROR,
										"org.eclipse.ui.examples.statushandling.jobs", 1, "This is the child status message", toBeThrown)); //$NON-NLS-1$ //$NON-NLS-2$ 
						return result;
					}
					throwException(toBeThrown);
					// toBeThrown is neither a runtime exception nor an
					// error
					return Status.CANCEL_STATUS;

				}

				monitor.worked(1);
			}
		} finally {
			if (reschedule)
				schedule(rescheduleWait);
			monitor.done();
		}

		return Status.OK_STATUS;
	}

	private void throwException(Throwable th) {
		if (th == null)
			return;

		if (th instanceof RuntimeException)
			throw (RuntimeException) th;

		if (th instanceof Error) {
			throw (Error) th;
		}

		// TODO do something !! the exception cannot be thrown - it's neither a
		// RuntimeException nor an Error
	}
}
