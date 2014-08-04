/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steve Foreman (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

/**
 * Information captured about a set of events.
 */
public class LongEventInfo {
	/**
	 * The start time of the first event, in milliseconds since 00:00 of 1 January 1970 UTC.
	 *
	 * @see System#currentTimeMillis
	 */
	public final long start;

	/**
	 * The total duration of all events, in milliseconds
	 */
	public final long duration;

	/**
	 * Constructs an event snapshot object from a contiguous range of events.
	 *
	 * @param start the start timestamp in milliseconds since 00:00 of 1 Jan 1970
	 * @param duration the duration of the captured events, in milliseconds
	 */
	public LongEventInfo(long start, long duration) {
		this.start = start;
		this.duration = duration;
	}
}
