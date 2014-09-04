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
 * @since 1.0
 */
public class StackSample {
	private final long timestamp;
	private final ThreadInfo[] traces;

	public StackSample(long timestamp, ThreadInfo[] traces) {
		this.timestamp = timestamp;
		this.traces = traces;
	}

	/**
	 * Returns the time stamp for this {@code StackSample}.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns an array of {@code ThreadInfo} for this {@code StackSample}. The display thread is
	 * always the first in the array.
	 */
	public ThreadInfo[] getStackTraces() {
		return traces;
	}

	/** For debugging only. */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("At ");
		buf.append(timestamp);
		if (traces.length != 0) {
			buf.append(" threads:\n");
			for (ThreadInfo threadInfo : traces) {
				buf.append(threadInfo.toString());
			}
		}
		return buf.toString();
	}
}
