/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A more efficient alternative to an IProgressMonitor. In particular, the
 * implementation is designed to make isCanceled() run as efficiently as
 * possible. Currently package-visible because the implementation is incomplete.
 *
 * @since 3.1
 *
 * @deprecated use SubMonitor instead
 *
 *             TODO mark for deletion
 */
@Deprecated
final class FastProgressReporter {
	private IProgressMonitor monitor;
	private volatile boolean canceled = false;
	private int cancelCheck = 0;

	private static int CANCEL_CHECK_PERIOD = 40;

	/**
	 * Constructs a null FastProgressReporter
	 */
	public FastProgressReporter() {
	}

	/**
	 * Constructs a FastProgressReporter that wraps the given progress monitor
	 *
	 * @param monitor the monitor to wrap
	 * @param totalProgress the total progress to be reported
	 */
	public FastProgressReporter(IProgressMonitor monitor, int totalProgress) {
		this.monitor = monitor;
		canceled = monitor.isCanceled();
	}
	/**
	 * Return whether the progress monitor has been canceled.
	 *
	 * @return <code>true</code> if the monitor has been cancelled, <code>false</code> otherwise.
	 */
	public boolean isCanceled() {
		if (monitor == null) {
			return canceled;
		}

		cancelCheck++;
		if (cancelCheck > CANCEL_CHECK_PERIOD) {
			canceled = monitor.isCanceled();
			cancelCheck = 0;
		}
		return canceled;
	}

	/**
	 * Cancel the progress monitor.
	 */
	public void cancel() {
		canceled = true;

		if (monitor == null) {
			return;
		}
		monitor.setCanceled(true);
	}
}
