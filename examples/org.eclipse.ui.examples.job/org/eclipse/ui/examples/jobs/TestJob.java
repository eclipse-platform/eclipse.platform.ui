/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.examples.jobs;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
/**
 * Base class for a simple test job with configurable parameters
 */
public class TestJob extends Job {
	private long duration;
	private boolean failure;
	private boolean unknown;
	
	public TestJob(long duration, boolean lock, boolean failure, boolean indeterminate) {
		super("Test job");
		this.duration = duration;
		this.failure = failure;
		this.unknown = indeterminate;
		if (lock)
			setRule(ResourcesPlugin.getWorkspace().getRoot());
	}
	public IStatus run(IProgressMonitor monitor) {
		if (failure) {
			MultiStatus result = new MultiStatus("org.eclipse.ui.examples.jobs", 1, "This is the MultiStatus message", new RuntimeException("This is the MultiStatus exception"));
			result.add(new Status(IStatus.ERROR, "org.eclipse.ui.examples.jobs", 1, "This is the child status message", new RuntimeException("This is the child exception")));
			return result;
		}
		final long sleep = 10;
		int ticks = (int) (duration / sleep);
		if(this.unknown)
			monitor.beginTask(toString(), IProgressMonitor.UNKNOWN);
		else
			monitor.beginTask(toString(), ticks);
		try {
			for (int i = 0; i < ticks; i++) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				monitor.subTask("Processing tick #" + i);
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}
}