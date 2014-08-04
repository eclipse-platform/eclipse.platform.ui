/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
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
	private final int numSamples;
	private final boolean isRunning;

	public UiFreezeEvent(long startTime, long totalTime, StackSample[] samples, int sampleCount,
			boolean stillRunning) {
		this.startTimestamp = startTime;
		this.stackTraceSamples = samples;
		this.numSamples = sampleCount;
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
	 * Returns a list of stack trace samples obtained during the event.
	 */
	public StackSample[] getStackTraceSamples() {
		return stackTraceSamples;
	}

	/**
	 * Returns the number stack traces obtained when the UI thread was frozen.
	 */
	public int getSampleCount() {
		return numSamples;
	}

	/**
	 * Returns {@code true} if this event is still running.
	 */
	public boolean isStillRunning() {
		return isRunning;
	}
}
