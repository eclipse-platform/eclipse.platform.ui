/*******************************************************************************
 * Copyright (C) 2014, 2022 Google Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *	   Steve Foreman (Google) - initial API and implementation
 *	   Marcus Eng (Google)
 *	   Sergey Prigogin (Google)
 *	   Simon Scholz <simon.scholz@vogella.com> - Bug 443391
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.eclipse.ui.monitoring.StackSample;
import org.junit.Test;

/**
 * Tests for {@link FilterHandler} class.
 */
public class FilterHandlerTests {
	private static final String FILTER_TRACES =
			"org.eclipse.ui.internal.monitoring.FilterHandlerTests.createFilteredStackSamples"
			+ ",org.eclipse.ui.internal.monitoring.SomeClass.someMethod"
			+ ",org.eclipse.ui.internal.monitoring.OtherClass.otherMethod";
	private static final long THREAD_ID = Thread.currentThread().getId();

	private StackSample[] createStackSamples() throws Exception {
		ThreadMXBean jvmThreadManager = ManagementFactory.getThreadMXBean();
		ThreadInfo threadInfo =
				jvmThreadManager.getThreadInfo(Thread.currentThread().getId(), Integer.MAX_VALUE);
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

	@Test
	public void testUnfilteredEventLogging() throws Exception {
		FilterHandler filterHandler = new FilterHandler(FILTER_TRACES);
		StackSample[] samples = createUnfilteredStackSamples();
		assertTrue(filterHandler.shouldLogEvent(samples, samples.length, THREAD_ID));
	}

	@Test
	public void testFilteredEventLogging() throws Exception {
		FilterHandler filterHandler = new FilterHandler(FILTER_TRACES);
		StackSample[] samples = createFilteredStackSamples();
		assertFalse(filterHandler.shouldLogEvent(samples, samples.length, THREAD_ID));
	}

	@Test
	public void testWildcardFilter() throws Exception {
		FilterHandler filterHandler = new FilterHandler("*.FilterHandlerTests.testW?ld*Filter");
		ThreadMXBean jvmThreadManager = ManagementFactory.getThreadMXBean();
		ThreadInfo threadInfo =
				jvmThreadManager.getThreadInfo(Thread.currentThread().getId(), Integer.MAX_VALUE);
		boolean matched = false;
		for (StackTraceElement element : threadInfo.getStackTrace()) {
			if (filterHandler.matchesFilter(element)) {
				matched = true;
			}
		}
		assertTrue(matched);
	}
}
