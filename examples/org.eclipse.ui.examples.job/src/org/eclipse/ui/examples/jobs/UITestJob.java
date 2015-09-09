/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.jobs;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.progress.UIJob;
/**
 * Base class for a simple test UI job with configurable parameters
 */
public class UITestJob extends UIJob {
	private long duration;
	private boolean failure;
	private boolean unknown;
	public UITestJob(long duration, boolean lock, boolean failure, boolean indeterminate) {
		super("Test job"); //$NON-NLS-1$
		this.duration = duration;
		this.failure = failure;
		this.unknown = indeterminate;

		if (lock)
			setRule(ResourcesPlugin.getWorkspace().getRoot());
	}
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		if (failure)
			throw new RuntimeException();
		final long sleep = 10;
		int ticks = (int) (duration / sleep);
		if(unknown)
			monitor.beginTask(toString(), IProgressMonitor.UNKNOWN);
		else
			monitor.beginTask(toString(), ticks);
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
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}
}