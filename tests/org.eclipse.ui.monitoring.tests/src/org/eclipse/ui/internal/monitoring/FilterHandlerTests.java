/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Steve Foreman (Google) - initial API and implementation
 *	   Marcus Eng (Google)
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.ui.monitoring.StackSample;

/**
 * Tests for {@link FilterHandler} class.
 */
public class FilterHandlerTests extends TestCase {
	private static final String FILTER_TRACES =
			"org.eclipse.ui.internal.monitoring.FilterHandlerTests.createFilteredStackSamples";
	private static final long THREAD_ID = Thread.currentThread().getId();

	private StackSample[] createStackSamples() throws Exception {
		ThreadMXBean jvmThreadManager = ManagementFactory.getThreadMXBean();
		ThreadInfo threadInfo =
				jvmThreadManager.getThreadInfo(Thread.currentThread().getId(), Integer.MAX_VALUE);
		// Remove the top 4 frames of the stack trace so that createFilteredStackSamples or
		// createUnfilteredStackSamples appears at the top of the stack. We have to use reflection
		// since ThreadInfo.stackTrace field is private and cannot be changed through the public
		// methods.
		StackTraceElement[] stackTrace = threadInfo.getStackTrace();
		Field field = ThreadInfo.class.getDeclaredField("stackTrace");
		field.setAccessible(true);
		field.set(threadInfo, Arrays.copyOfRange(stackTrace, 4, stackTrace.length));
		return new StackSample[] { new StackSample(0, new ThreadInfo[] { threadInfo }) };
	}

	/**
	 * Creates stack samples that should not be filtered.
	 */
	private StackSample[] createUnfilteredStackSamples() throws Exception {
		return createStackSamples();
	}

	/**
	 * Creates stack samples that should be filtered.
	 */
	private StackSample[] createFilteredStackSamples() throws Exception {
		return createStackSamples();
	}

	public void testUnfilteredEventLogging() throws Exception {
		FilterHandler filterHandler = new FilterHandler(FILTER_TRACES);
		StackSample[] samples = createUnfilteredStackSamples();
		assertTrue(filterHandler.shouldLogEvent(samples, samples.length, THREAD_ID));
	}

	public void testFilteredEventLogging() throws Exception {
		FilterHandler filterHandler = new FilterHandler(FILTER_TRACES);
		StackSample[] samples = createFilteredStackSamples();
		assertFalse(filterHandler.shouldLogEvent(samples, samples.length, THREAD_ID));
	}
}
