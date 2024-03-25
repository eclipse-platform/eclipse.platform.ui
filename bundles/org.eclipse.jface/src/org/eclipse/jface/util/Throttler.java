/*******************************************************************************
 * Copyright (c) 2016, 2021 Eclipse Foundation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mikael Barbero (Eclipse Foundation) - initial API and implementation
 *     Joerg Kubitz                        - fixes
 *******************************************************************************/
package org.eclipse.jface.util;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
/**
 * A utility class that throttles the execution of a runnable in the UI thread.
 *
 * @since 3.14
 */
public class Throttler {
	private final Runnable timerExec;
	private final Display display;
	private final AtomicBoolean scheduled = new AtomicBoolean();
	/**
	 * Timestamp of last execution returned. A value of 0 means the runner was never
	 * run yet.
	 **/
	private volatile long lastRunFinishedNanos;
	/**
	 * Initializes a new throttler object that will throttle the execution of
	 * the given runnable in the {@link Display#getThread() UI thread} of the
	 * given display. The throttler will ensure that the runnable will not run
	 * more than every {@code minWaitTime}, even if it is
	 * {@link #throttledExec() executed} more often.
	 *
	 * @param display
	 *            The display owning the thread onto which the runnable will be
	 *            executed.
	 * @param minWaitTime
	 *            The minimum duration between each execution of the given
	 *            runnable (from runnable to return until next run).
	 * @param runnable
	 *            The runnable to throttle.
	 */
	public Throttler(Display display, Duration minWaitTime, Runnable runnable) {
		Objects.requireNonNull(runnable);
		Objects.requireNonNull(display);
		this.display = display;
		if (minWaitTime.isNegative()) {
			throw new IllegalArgumentException("Minimum wait time must be positive"); //$NON-NLS-1$
		}
		if (minWaitTime.toMillis() >= Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"Minimum wait time in millis must be smaller than " + Integer.MAX_VALUE); //$NON-NLS-1$
		}
		int minWaitBetweenRunMillis = (int) minWaitTime.toMillis();
		Runnable runner = () -> { // Always runs in Display Thread
			scheduled.set(false);
			runnable.run();
			long nanoTime = System.nanoTime();
			if (nanoTime == 0) {
				// prevent further 0 values.
				nanoTime = -1;
			}
			lastRunFinishedNanos = nanoTime;
		};
		this.timerExec = () -> { // Always runs in Display Thread
			long elapsedNanos = System.nanoTime() - lastRunFinishedNanos;
			long elapsedMillis = elapsedNanos / 1_000_000;
			if (lastRunFinishedNanos == 0 || elapsedMillis > minWaitBetweenRunMillis) {
				// run immediately
				runner.run();
			} else if (!display.isDisposed()) {
				// wait the remaining time
				long millisDifference = minWaitBetweenRunMillis - elapsedMillis;
				// prevent negative values of millisToWait:
				int millisToWait = Math.max((int) millisDifference, 0);
				display.timerExec(millisToWait, runner);
			} else {
				// fail - display meanwhile disposed
				scheduled.set(false);
			}
		};
	}

	/**
	 * Schedules the wrapped Runnable to be run after the configured wait time or do
	 * nothing if it has already been scheduled but not executed yet. Can be called
	 * from any Thread. If called from Display Thread it may run the Runnable before
	 * returning.
	 */
	public void throttledExec() {
		throttledExec(true);
	}

	/**
	 * Like {@link #throttledExec()} but if called from Display Thread guaranteed to
	 * return before Runnable is run
	 *
	 * @since 3.34
	 */
	public void throttledAsyncExec() {
		throttledExec(false);
	}

	private void throttledExec(boolean allowSyncExec) {
		if (display.isDisposed()) {
			return;
		}
		if (scheduled.compareAndSet(false, true)) {
			boolean exception = true;
			try {
				if (allowSyncExec && Thread.currentThread() == display.getThread()) {
					timerExec.run();
				} else {
					display.asyncExec(timerExec);
				}
				exception = false;
			} catch (SWTException e) {
				// Don't care if display is disposed, and report otherwise
				if (!display.isDisposed()) {
					throw e;
				}
			} finally {
				if (exception) {
					scheduled.set(false);
				}
			}
		}
	}
}