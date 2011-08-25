/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.work.ISchedulingExecutor;
import org.eclipse.equinox.concurrent.future.AbstractExecutor;
import org.eclipse.equinox.concurrent.future.AbstractFuture;
import org.eclipse.equinox.concurrent.future.FutureProgressMonitor;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.SingleOperationFuture;

/**
 * An implementation of {@link ISchedulingExecutor} that uses the {@link Job}
 * API.
 */
public class JobExecutor extends AbstractExecutor implements
		ISchedulingExecutor {

	public IFuture schedule(final IProgressRunnable runnable, String name,
			long delay) {
		final SingleOperationFuture future = new SingleOperationFuture();
		new Job(name) {
			protected IStatus run(IProgressMonitor monitor) {
				if (future.isCanceled())
					return future.getStatus();
				// Now add progress monitor as child of future monitor
				((FutureProgressMonitor) future.getProgressMonitor())
						.setChildProgressMonitor(monitor);
				// Now run safely
				future.runWithProgress(runnable);
				return future.getStatus();
			}
		}.schedule(delay);
		return future;
	}

	@Override
	protected AbstractFuture createFuture(IProgressMonitor progressMonitor) {
		return new SingleOperationFuture(progressMonitor);
	}

	@Override
	public IFuture execute(IProgressRunnable runnable, IProgressMonitor monitor) {
		return schedule(runnable, "", 0); //$NON-NLS-1$
	}

}
