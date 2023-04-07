/*******************************************************************************
* Copyright (c) 2023 Ole Osterhagen and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Ole Osterhagen - initial API and implementation
*******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This test helper class coordinates the execution between a main thread and a
 * secondary thread.
 *
 * <ol>
 * <li>On start both threads a running.</li>
 * <li>At some point the main thread decides to wait until the secondary thread
 * is paused: {@link #waitUntilThreadIsPaused()}</li>
 * <li>The secondary thread pauses execution, notifies the main thread and waits
 * until it is resumed: {@link #pauseThreadAndWaitForResume()}</li>
 * <li>The waiting main thread continues execution.</li>
 * <li>Later the main thread may resume the paused secondary thread:
 * {@link #resumeThread()}</li>
 * </ol>
 */
class ThreadControl {

	private final long timeoutMillis;

	private final CyclicBarrier barrier = new CyclicBarrier(2);

	/**
	 * Creates a {@code ThreadControl} class with the specified timeout.
	 *
	 * @param timeoutMillis The maximum time in milliseconds to wait for thread
	 *                      status changes. This prevents a broken test from running
	 *                      forever.
	 */
	public ThreadControl(long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}

	/**
	 * Called from the main thread to wait until the secondary thread pauses
	 * execution with a call to {@link #pauseThreadAndWaitForResume()}.
	 */
	public void waitUntilThreadIsPaused() {
		await();
	}

	/**
	 * Called from the secondary thread to inform a waiting main thread that it can
	 * continue. The secondary thread waits until it is resumed with a call to
	 * {@link #resumeThread()} from the main thread.
	 */
	public void pauseThreadAndWaitForResume() {
		// the other thread (main thread) can continue
		await();

		// this thread (secondary thread) is paused
		await();
	}

	/**
	 * Called from the main thread to resume the paused secondary thread.
	 */
	public void resumeThread() {
		await();
	}

	private void await() {
		try {
			barrier.await(timeoutMillis, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
			throw new RuntimeException("Error while waiting for threads to synchronize.", e);
		}
	}

}
