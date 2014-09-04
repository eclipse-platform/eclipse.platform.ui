/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
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
 * @since 1.0
 */
public class UiFreezeEvent {
	private final long startTimestamp;
	private final long totalDuration;
	private final StackSample[] stackTraceSamples;
	private final boolean isRunning;

	public UiFreezeEvent(long startTime, long totalTime, StackSample[] samples,
			boolean stillRunning) {
		this.startTimestamp = startTime;
		this.stackTraceSamples = samples;
		this.totalDuration = totalTime;
		this.isRunning = stillRunning;
	}

	/**
	 * Returns the time when the UI thread froze.
	 */
	public long getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * Returns the total amount of time the UI thread remained frozen.
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
	 * Returns {@code true} if this event is still running.
	 */
	public boolean isStillRunning() {
		return isRunning;
	}

	/** For debugging only. */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Freeze started at ");
		buf.append(startTimestamp);
		if (isRunning) {
			buf.append(" still ongoing after ");
		} else {
			buf.append(" lasted ");
		}
		buf.append(totalDuration);
		buf.append("ms");
		if (stackTraceSamples.length != 0) {
			buf.append("\nStack trace samples:");
			for (StackSample stackTraceSample : stackTraceSamples) {
				buf.append('\n');
				buf.append(stackTraceSample.toString());
			}
		}
		return buf.toString();
	}
}
