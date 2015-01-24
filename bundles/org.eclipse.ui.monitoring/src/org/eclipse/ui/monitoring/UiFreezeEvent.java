/*******************************************************************************
 * Copyright (C) 2014, 2015 Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.monitoring;

/**
 * Responsible for holding the stack traces for a UI event.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 */
public class UiFreezeEvent {
	private final long startTimestamp;
	private final long totalDuration;
	private final StackSample[] stackTraceSamples;
	private final boolean isStillRunning;

	/**
	 * Creates a UiFreezeEvent.
	 *
	 * @param startTime initial dispatch time for the event in milliseconds since January 1,
	 *     1970 UTC
	 * @param duration duration of the event in milliseconds
	 * @param samples array of {@link StackSample}s containing thread information
	 * @param stillRunning whether or not the event was still running when this UiFreezeEvent
	 *     was created. If {@code true}, this UiFreezeEvent may indicate a deadlock.
	 */
	public UiFreezeEvent(long startTime, long duration, StackSample[] samples,
			boolean stillRunning) {
		this.startTimestamp = startTime;
		this.stackTraceSamples = samples;
		this.totalDuration = duration;
		this.isStillRunning = stillRunning;
	}

	/**
	 * Returns the time when the UI thread froze, in milliseconds since January 1, 1970 UTC.
	 */
	public long getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * Returns the total amount of time in milliseconds that the UI thread remained frozen.
	 */
	public long getTotalDuration() {
		return totalDuration;
	}

	/**
	 * Returns the stack trace samples obtained during the event.
	 */
	public StackSample[] getStackTraceSamples() {
		return stackTraceSamples;
	}

	/**
	 * Returns {@code true} if this event was still ongoing at the time the event was logged,
	 * which can happen for deadlocks.
	 */
	public boolean isStillRunning() {
		return isStillRunning;
	}

	/** For debugging only. */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Freeze started at "); //$NON-NLS-1$
		buf.append(startTimestamp);
		if (isStillRunning) {
			buf.append(" still ongoing after "); //$NON-NLS-1$
		} else {
			buf.append(" lasted "); //$NON-NLS-1$
		}
		buf.append(totalDuration);
		buf.append("ms"); //$NON-NLS-1$
		if (stackTraceSamples.length != 0) {
			buf.append("\nStack trace samples:"); //$NON-NLS-1$
			for (StackSample stackTraceSample : stackTraceSamples) {
				buf.append('\n');
				buf.append(stackTraceSample.toString());
			}
		}
		return buf.toString();
	}
}
