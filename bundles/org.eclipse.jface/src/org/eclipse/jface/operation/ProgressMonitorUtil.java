/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *     Christoph Laeubrich - Bug 552683 provide a deprecation free method as alternative
 *******************************************************************************/
package org.eclipse.jface.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

/**
 * Contains static methods for constructing and manipulating progress monitors.
 *
 * @since 3.13
 */
public final class ProgressMonitorUtil {

	/**
	 * Wraps an {@link IProgressMonitor} associated with the UI thread, producing a
	 * new {@link IProgressMonitor} which can be used from any one thread. The
	 * resulting progress monitor will send changes to the wrapped monitor
	 * asynchronously.
	 * <p>
	 * May be called from any thread. The thread that uses the resulting progress
	 * monitor need not be the same as the thread that constructs it.
	 *
	 * @deprecated use {@link #createUIProgressMonitor(IProgressMonitor, Display)}
	 *             instead
	 * @param monitor a progress monitor that should only be updated on the UI
	 *                thread
	 *
	 * @param display Display associated with the progress monitor's UI thread
	 *
	 * @return a progress monitor wrapper that can accumulate progress events from a
	 *         non-ui thread, and send them to the wrapped monitor on the UI thread
	 *
	 * @since 3.13
	 */
	@Deprecated
	public static org.eclipse.core.runtime.IProgressMonitorWithBlocking createAccumulatingProgressMonitor(
			IProgressMonitor monitor,
			Display display) {
		return new AccumulatingProgressMonitor(monitor, display);
	}

	/**
	 * Wraps an {@link IProgressMonitor} associated with the UI thread, producing a
	 * new {@link IProgressMonitor} which can be used from any one thread. The
	 * resulting progress monitor will send changes to the wrapped monitor
	 * asynchronously.
	 * <p>
	 * May be called from any thread. The thread that uses the resulting progress
	 * monitor need not be the same as the thread that constructs it.
	 *
	 * @param monitor a progress monitor that should only be updated on the UI
	 *                thread
	 *
	 * @param display Display associated with the progress monitor's UI thread
	 *
	 * @return a progress monitor wrapper that can accumulate progress events from a
	 *         non-ui thread, and send them to the wrapped monitor on the UI thread
	 *
	 * @since 3.22
	 */
	public static IProgressMonitor createUIProgressMonitor(IProgressMonitor monitor, Display display) {
		return new AccumulatingProgressMonitor(monitor, display);
	}
}
