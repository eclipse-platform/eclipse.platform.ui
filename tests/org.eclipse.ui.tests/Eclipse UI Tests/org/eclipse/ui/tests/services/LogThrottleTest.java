/*******************************************************************************
 * Copyright (c) 2020 Karakun GmbH
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Karsten Thoms - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.services;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.ui.internal.services.LogThrottle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogLevel;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;

public class LogThrottleTest {
	private static final int QUEUE_SIZE = 5;
	private ServiceTracker<LogReaderService, LogReaderService> logReaderTracker;
	private LogListener logListener;

	@Before
	public void setUp() {
		BundleContext context = Activator.getDefault().getContext();
		logReaderTracker = new ServiceTracker<>(context, LogReaderService.class.getName(), null);
		logReaderTracker.open();
		LogReaderService logReaderService = logReaderTracker.getService();
		logListener = mock(LogListener.class);
		logReaderService.addLogListener(logListener);
	}

	@After
	public void tearDown() {
		logReaderTracker.close();
	}

	@Test
	public void test_log_simple() {
		// given
		LogThrottle throttle = new LogThrottle(QUEUE_SIZE, 1);

		// when
		throttle.log(LogLevel.ERROR.ordinal(), "foo", null);

		// then
		verify(logListener, times(1)).logged(logEntryMatcher(LogLevel.ERROR, "foo"));
	}

	@Test
	public void test_log_throttled() {
		// given
		LogThrottle throttle = new LogThrottle(QUEUE_SIZE, 3);

		// when
		for (int i = 0; i < 5; i++) {
			throttle.log(LogLevel.ERROR.ordinal(), "foo", null);
		}

		// then
		verify(logListener, atMost(3)).logged(logEntryMatcher(LogLevel.ERROR, "foo"));
		verify(logListener, atMost(1))
				.logged(logEntryMatcher(LogLevel.WARN, "The previous message has been throttled.*"));
	}

	@Test
	public void test_log_setThrottle() {
		// given
		LogThrottle throttle = new LogThrottle(QUEUE_SIZE, 3);
		
		// when
		for (int i = 0; i < 5; i++) {
			throttle.log(LogLevel.ERROR.ordinal(), "foo", null);
		}
		
		// then
		verify(logListener, atMost(3)).logged(logEntryMatcher(LogLevel.ERROR, "foo"));
		verify(logListener, atMost(1))
		.logged(logEntryMatcher(LogLevel.WARN, "The previous message has been throttled.*"));
		
		// and
		throttle.setThrottle(2);

		// when
		for (int i = 0; i < 5; i++) {
			throttle.log(LogLevel.ERROR.ordinal(), "bar", null);
		}
		
		// then
		verify(logListener, atMost(2)).logged(logEntryMatcher(LogLevel.ERROR, "bar"));

	}

	private LogEntry logEntryMatcher(LogLevel level, String message) {
		return argThat(new ArgumentMatcher<LogEntry>() {

			@Override
			public boolean matches(LogEntry arg) {
				return arg.getLogLevel() == level && arg.getMessage().matches(message);
			}

			@Override
			public String toString() {
				return String.format("[%s] %s", level.name(), message);
			}

		});
	}
}
