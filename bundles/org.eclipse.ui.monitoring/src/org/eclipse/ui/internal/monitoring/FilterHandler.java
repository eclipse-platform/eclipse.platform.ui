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
package org.eclipse.ui.internal.monitoring;

import java.lang.management.ThreadInfo;
import java.util.Arrays;

import org.eclipse.jface.util.Util;
import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

/**
 * Checks if the {@link UiFreezeEvent} matches any defined filters.
 */
public class FilterHandler {
	/**
	 * Groups the class name and method name defined in the filter.
	 */
	private class Filter implements Comparable<Filter> {
		final String className;
		final String methodName;

		public Filter(String className, String methodName) {
			this.className = className;
			this.methodName = methodName;
		}

		@Override
		public int compareTo(Filter other) {
			int c = methodName.compareTo(other.methodName);
			if (c != 0) {
				return c;
			}
			return className.compareTo(other.className);
		}
	}

	private final Filter[] filters;

	public FilterHandler(String unparsedFilters) {
		String[] rawFilters = unparsedFilters.split(","); //$NON-NLS-1$
		filters = new Filter[rawFilters.length];

		for (int i = 0; i < rawFilters.length; i++) {
			String currentFilter = rawFilters[i];
			int period = currentFilter.lastIndexOf('.');

			if (period < 0) {
				filters[i] = new Filter("", currentFilter); //$NON-NLS-1$
				continue;
			}

			filters[i] = new Filter(currentFilter.substring(0, period),
					currentFilter.substring(period + 1));
		}

		Arrays.sort(filters);
	}

	/**
	 * Returns {@code true} if the stack samples do not contain filtered stack frames in the stack
	 * traces of the display thread.
	 *
	 * @param stackSamples the array containing stack trace samples for a long event in the first
	 *     {@code numSamples} elements
	 * @param numSamples the number of valid stack trace samples in the {@code stackSamples} array
	 * @param displayThreadId the ID of the display thread
	 */
	public boolean shouldLogEvent(StackSample[] stackSamples, int numSamples,
			long displayThreadId) {
		for (int i = 0; i < numSamples; i++) {
			if (hasFilteredTraces(stackSamples[i].getStackTraces(), displayThreadId)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the top frame of the stack trace of the display thread contains the fully qualified
	 * name of a method that should be ignored.
	 */
	private boolean hasFilteredTraces(ThreadInfo[] stackTraces, long displayThreadId) {
		for (ThreadInfo threadInfo : stackTraces) {
			if (threadInfo.getThreadId() == displayThreadId) {
				return matchesFilter(threadInfo.getStackTrace());
			}
		}

		MonitoringPlugin.logError(Messages.FilterHandler_missing_thread_error, null);
		return false;
	}

	private boolean matchesFilter(StackTraceElement[] stackTraces) {
		if (stackTraces.length > 0) {
			StackTraceElement element = stackTraces[0];
			String methodName = element.getMethodName();
			String className = element.getClassName();
			if (Util.isCocoa()
					&& methodName.startsWith("objc_msgSend")
					&& className.equals("org.eclipse.swt.internal.cocoa.OS")
					&& stackTraces.length > 1) {
				// Skip the objc_msgSend frame at the top of the stack on Cocoa.
				element = stackTraces[1];
				methodName = element.getMethodName();
				className = element.getClassName();
			}
			// Binary search.
			int low = 0;
			int high = filters.length;
			while (low < high) {
				int mid = (low + high) >>> 1;
				Filter filter = filters[mid];
				int c = methodName.compareTo(filter.methodName);
				if (c == 0) {
					c = className.compareTo(filter.className);
				}
				if (c == 0) {
					return true;
				} else if (c < 0) {
					high = mid;
				} else {
					low = mid + 1;
				}
			}
		}
		return false;
	}
}
