/*******************************************************************************
 * Copyright (c) 2011, 2020 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - bug fixes
 *     Christoph Läubrich - Bug 563459 - Enhance UISynchronize to make it more useful
 *******************************************************************************/
package org.eclipse.e4.ui.di;

/**
 * Widget toolkit abstract to synchronize back into the UI-Thread from other
 * threads
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 */
public abstract class UISynchronize {
	/**
	 * Executes the runnable on the UI-Thread and blocks until the runnable is
	 * finished
	 *
	 * @param runnable the runnable to execute
	 */
	public abstract void syncExec(Runnable runnable);

	/**
	 * Schedules the runnable on the UI-Thread for execution and returns immediately
	 *
	 * @param runnable the runnable to execute
	 */
	public abstract void asyncExec(Runnable runnable);

	/**
	 * Checks if the given thread is the UI-Thread.
	 *
	 * @param thread to check
	 * @return <code>true</code> if the thread is the UI-Thread <code>false</code>
	 *         otherwise
	 * @since 1.3
	 */
	protected abstract boolean isUIThread(Thread thread);

	/**
	 * Shows a busy-indicator to the user while this runnable is executed, long
	 * running operations in the UI thread may freeze the UI.
	 *
	 * @param runnable the runnable to execute
	 * @since 1.3
	 */
	protected abstract void showBusyWhile(Runnable runnable);

	/**
	 * Request to perform an event dispatch cycle.
	 *
	 * @return <code>true</code> if there might be more work to perform
	 *         <code>false</code> otherwise
	 * @since 1.3
	 */
	protected abstract boolean dispatchEvents();


}
