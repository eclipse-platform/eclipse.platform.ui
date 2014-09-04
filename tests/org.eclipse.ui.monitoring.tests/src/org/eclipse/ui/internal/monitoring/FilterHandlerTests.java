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

import junit.framework.TestCase;

import org.eclipse.ui.monitoring.StackSample;

/**
 * Tests for {@link FilterHandler} class.
 */
public class FilterHandlerTests extends TestCase {
	private static final String FILTER_TRACES =
			"org.eclipse.ui.internal.monitoring.FilterHandlerTests.createFilteredStackSamples";
	private static final long THREAD_ID = Thread.currentThread().getId();

	private StackSample[] createStackSamples() {
		ThreadMXBean jvmThreadManager = ManagementFactory.getThreadMXBean();
		ThreadInfo thread =
				jvmThreadManager.getThreadInfo(Thread.currentThread().getId(), Integer.MAX_VALUE);
		return new StackSample[] { new StackSample(0, new ThreadInfo[] { thread }) };
	}

	/**
	 * Creates stack samples that should not be filtered.
	 */
	private StackSample[] createUnfilteredStackSamples() {
		return createStackSamples();
	}

	/**
	 * Creates stack samples that should be filtered.
	 */
	private StackSample[] createFilteredStackSamples() {
		return createStackSamples();
	}

	public void testUnfilteredEventLogging() {
		FilterHandler filterHandler = new FilterHandler(FILTER_TRACES);
		StackSample[] samples = createUnfilteredStackSamples();
		assertTrue(filterHandler.shouldLogEvent(samples, samples.length, THREAD_ID));
	}

	public void testFilteredEventLogging() {
		FilterHandler filterHandler = new FilterHandler(FILTER_TRACES);
		StackSample[] samples = createFilteredStackSamples();
		assertFalse(filterHandler.shouldLogEvent(samples, samples.length, THREAD_ID));
	}
}
