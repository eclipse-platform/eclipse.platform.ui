/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 548799
 *******************************************************************************/
package org.eclipse.ui.examples.jobs;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * Base class for a simple test job with configurable parameters
 */
public class TestJob extends Job {
	/**
	 * A family identifier for all test jobs
	 */
	public static final Object FAMILY_TEST_JOB = new Object();
	/**
	 * Total duration that the test job should sleep, in milliseconds.
	 */
	private final long duration;
	/**
	 * Whether the test job should fail.
	 */
	private final boolean failure;
	/**
	 * Whether the job should report unknown progress.
	 */
	private final boolean unknown;
	private final boolean reschedule;
	private final long rescheduleWait;

	/**
	 * Creates a new test job
	 *
	 * @param duration       Total time that the test job should sleep, in
	 *                       milliseconds.
	 * @param lock           Whether the job should use a workspace scheduling rule
	 * @param failure        Whether the job should fail
	 * @param indeterminate  Whether the job should report indeterminate progress
	 * @param rescheduleWait
	 * @param reschedule
	 */

	public TestJob(long duration, boolean lock, boolean failure, boolean indeterminate, boolean reschedule,
			long rescheduleWait) {
		super("Test job"); //$NON-NLS-1$
		this.duration = duration;
		this.failure = failure;
		this.unknown = indeterminate;
		this.reschedule = reschedule;
		this.rescheduleWait = rescheduleWait;
		setProperty(IProgressConstants.ICON_PROPERTY,
				ResourceLocator.imageDescriptorFromBundle(TestJob.class, "icons/sample.gif")); //$NON-NLS-1$
		if (lock) {
			setRule(ResourcesPlugin.getWorkspace().getRoot());
		}
	}

	@Override
	public boolean belongsTo(Object family) {
		if (family instanceof TestJob) {
			return true;
		}
		return family == FAMILY_TEST_JOB;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		if (failure) {
			MultiStatus result = new MultiStatus("org.eclipse.ui.examples.jobs", 1, "This is the MultiStatus message", //$NON-NLS-1$ //$NON-NLS-2$
					new RuntimeException("This is the MultiStatus exception")); //$NON-NLS-1$
			result.add(new Status(IStatus.ERROR, "org.eclipse.ui.examples.jobs", 1, "This is the child status message", //$NON-NLS-1$ //$NON-NLS-2$
					new RuntimeException("This is the child exception"))); //$NON-NLS-1$
			return result;
		}
		final long sleep = 10;
		int ticks = (int) (duration / sleep);
		if (this.unknown) {
			monitor.beginTask(toString(), IProgressMonitor.UNKNOWN);
		} else {
			monitor.beginTask(toString(), ticks);
		}
		try {
			for (int i = 0; i < ticks; i++) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				monitor.subTask("Processing tick #" + i); //$NON-NLS-1$
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					return Status.CANCEL_STATUS;
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
		if (reschedule) {
			schedule(rescheduleWait);
		}
		return Status.OK_STATUS;
	}
}