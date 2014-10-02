/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Marcus Eng (Google) - initial API and implementation
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.monitoring;

import java.lang.management.ThreadInfo;

/**
 * A sample of the stack that contains the stack traces and the time stamp.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 1.0
 */
public class StackSample {
	private final long timestamp;
	private final ThreadInfo[] traces;

	/**
	 * Creates a StackSample.
	 *
	 * @param timestamp time in milliseconds since January 1, 1970 UTC when the thread stacks
	 *     were sampled
	 * @param traces thread information for either all threads or just the display thread,
	 *     depending on the value of the {@link PreferenceConstants#DUMP_ALL_THREADS} preference
	 */
	public StackSample(long timestamp, ThreadInfo[] traces) {
		this.timestamp = timestamp;
		this.traces = traces;
	}

	/**
	 * Returns the time stamp in milliseconds since January 1, 1970 UTC for this
	 * {@code StackSample}.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns an array of {@code ThreadInfo}s for this {@code StackSample}. The display thread is
	 * always the first in the array.
	 */
	public ThreadInfo[] getStackTraces() {
		return traces;
	}

	/** For debugging only. */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("At "); //$NON-NLS-1$
		buf.append(timestamp);
		if (traces.length != 0) {
			buf.append(" threads:\n"); //$NON-NLS-1$
			for (ThreadInfo threadInfo : traces) {
				buf.append(threadInfo.toString());
			}
		}
		return buf.toString();
	}
}
