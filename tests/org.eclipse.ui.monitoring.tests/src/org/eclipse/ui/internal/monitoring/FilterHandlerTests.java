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

import junit.framework.TestCase;

import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Tests for {@link FilterHandler} class.
 */
public class FilterHandlerTests extends TestCase {
	private static final String FILTER_TRACES =
			"org.eclipse.ui.internal.monitoring.FilterHandlerTests.createFilteredUiFreezeEvent";
	private static final long THREAD_ID = Thread.currentThread().getId();

	private UiFreezeEvent createUiFreezeEvent() {
		ThreadMXBean jvmThreadManager = ManagementFactory.getThreadMXBean();
		ThreadInfo thread =
				jvmThreadManager.getThreadInfo(Thread.currentThread().getId(), Integer.MAX_VALUE);
		StackSample[] samples = { new StackSample(0, new ThreadInfo[] { thread }) };
		return new UiFreezeEvent(0, 0, samples, 1, false);
	}

	/**
	 * Creates a {@link UiFreezeEvent} with a stack trace that is not filtered.
	 */
	private UiFreezeEvent createUnfilteredUiFreezeEvent() {
		return createUiFreezeEvent();
	}

	/**
	 * Creates a {@link UiFreezeEvent} with a stack trace that should be filtered.
	 */
	private UiFreezeEvent createFilteredUiFreezeEvent() {
		return createUiFreezeEvent();
	}

	public void testUnfilteredEventLogging() {
		FilterHandler filterHandler = new FilterHandler(FILTER_TRACES);
		UiFreezeEvent unfilteredEvent = createUnfilteredUiFreezeEvent();

		assertTrue(filterHandler.shouldLogEvent(unfilteredEvent, THREAD_ID));
	}

	public void testFilteredEventLogging() {
		FilterHandler filterHandler = new FilterHandler(FILTER_TRACES);
		UiFreezeEvent filteredEvent = createFilteredUiFreezeEvent();

		assertFalse(filterHandler.shouldLogEvent(filteredEvent, THREAD_ID));
	}
}
