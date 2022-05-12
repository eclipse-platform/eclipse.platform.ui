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
	private volatile long lastRunNanos;
	/**
	 * Initializes a new throttler object that will throttle the execution of
	 * the given runnable in the {@link Display#getThread() UI thread} of the
	 * given display. The throttler will ensure that the runnable will not run
	 * more than every {@code minWaitTime}, even if it is
	 * {@link #throttledExec() executed} more often.
	 *
	 * @param display
	 *            the display owning the thread onto which the runnable will be
	 *            executed.
	 * @param minWaitTime
	 *            the minimum duration between each execution of the given
	 *            runnable.
	 * @param runnable
	 *            the runnable to throttle.
	 */
	public Throttler(Display display, Duration minWaitTime, Runnable runnable) {
		this.display = display;
		if (minWaitTime.isNegative()) {
			throw new IllegalArgumentException("Minimum wait time must be positive"); //$NON-NLS-1$
		}
		if (minWaitTime.toMillis() >= Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"Minimum wait time in millis must be smaller than " + Integer.MAX_VALUE); //$NON-NLS-1$
		}
		int minWaitBetweenRunMillis = (int) minWaitTime.toMillis();
		Runnable runner = () -> {
			scheduled.set(false);
			runnable.run();
			lastRunNanos = System.nanoTime();
		};
		this.timerExec = () -> {
			long elapsedNanos = System.nanoTime() - lastRunNanos;
			long elapsedMillis = elapsedNanos / 1_000_000;
			if (elapsedMillis > minWaitBetweenRunMillis) {
				// run immediately
				runner.run();
			} else if (!display.isDisposed()) {
				// wait the remaining time
				long milisDifference = minWaitBetweenRunMillis - elapsedMillis;
				// milisDifference may be negative, or
				// milisDifference > Integer.MAX_VALUE (with initial elapsedNanos=0)
				// => limit to max:
				int milisToWait = Math.max((int) milisDifference, minWaitBetweenRunMillis);
				display.timerExec(milisToWait, runner);
			} else {
				// fail - display meanwhile disposed
				scheduled.set(false);
			}
		};
	}
	/**
	 * Schedules the wrapped runnable to be run after the configured wait time
	 * or do nothing if it has already been scheduled but not executed yet.
	 */
	public void throttledExec() {
		if (display.isDisposed()) {
			return;
		}
		if (scheduled.compareAndSet(false, true)) {
			boolean exception = true;
			try {
				if (Thread.currentThread() == display.getThread()) {
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