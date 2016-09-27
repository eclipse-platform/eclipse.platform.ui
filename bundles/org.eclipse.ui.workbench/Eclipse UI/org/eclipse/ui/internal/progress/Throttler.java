/*******************************************************************************
 * Copyright (c) 2016 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mikael Barbero (Eclipse Foundation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.time.Duration;
import org.eclipse.swt.widgets.Display;

/**
 * A utility class that throttles the execution of a runnable in the UI thread.
 */
public class Throttler {
	private final Runnable timerExec;

	private final Display display;

	private volatile boolean scheduled;

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
		if (minWaitTime.toMillis() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"Minimum wait time must be smaller than " + Integer.MAX_VALUE); //$NON-NLS-1$
		}
		int minWaitBetweenRunMillis = (int) minWaitTime.toMillis();
		this.timerExec = () -> {
			if (!display.isDisposed()) {
				display.timerExec(minWaitBetweenRunMillis, () -> {
					scheduled = false;
					runnable.run();
				});
			}
		};
	}

	/**
	 * Schedules the wrapped runnable to be run after the configured wait time
	 * or do nothing if it has already been scheduled but not executed yet.
	 */
	public void throttledExec() {
		if (!scheduled && !display.isDisposed()) {
			scheduled = true;
			if (Thread.currentThread() == display.getThread()) {
				timerExec.run();
			} else {
				display.asyncExec(timerExec);
			}
		}
	}
}