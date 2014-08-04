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
package org.eclipse.ui.internal.monitoring;

import junit.framework.TestCase;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.monitoring.PreferenceConstants;
import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * JUnit test for the {@link DefaultUiFreezeEventLogger}.
 */
public class DefaultLoggerTest extends TestCase {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	private static final String RUNTIME_ID = "org.eclipse.core.runtime";
	private static final long TIME = 120000000;
	private static final long DURATION = 500;
	private DefaultUiFreezeEventLogger logger;
	private ThreadInfo thread;
	private IStatus loggedStatus;

	@Override
	public void setUp() {
		logger = new DefaultUiFreezeEventLogger();
		createLogListener();
	}

	private void createLogListener() {
		Platform.addLogListener(new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				if (plugin.equals(RUNTIME_ID)) {
					loggedStatus = status;
				}
			}
		});
	}

	private UiFreezeEvent createFreezeEvent() {
		ThreadMXBean jvmThreadManager = ManagementFactory.getThreadMXBean();
		thread = jvmThreadManager.getThreadInfo(Thread.currentThread().getId(), Integer.MAX_VALUE);

		StackSample[] samples = { new StackSample(TIME, new ThreadInfo[] { thread }) };
		UiFreezeEvent event = new UiFreezeEvent(TIME, DURATION, samples, 1, false);
		return event;
	}

	public void testLogEvent() throws Exception {
		UiFreezeEvent event = createFreezeEvent();
		String expectedTime = dateFormat.format(new Date(TIME));
		String expectedHeader =
				String.format("UI Delay of %.2fs at %s", DURATION / 1000.0, expectedTime);
		String expectedEventMessage = String.format("Sample at %s (+%.3fs)", expectedTime, 0.000);

		logger.log(event);

		assertEquals(PreferenceConstants.PLUGIN_ID, loggedStatus.getPlugin());
		assertTrue("Logged status was not a MultiStatus", loggedStatus.isMultiStatus());
		assertEquals(expectedHeader, loggedStatus.getMessage());
		assertEquals("One nested IStatus did not get logged correctly.", 1,
				loggedStatus.getChildren().length);

		IStatus freezeEvent = loggedStatus.getChildren()[0];
		assertTrue(freezeEvent.getMessage().contains(expectedEventMessage));

		StackTraceElement[] threadStackTrace = thread.getStackTrace();
		StackTraceElement[] loggedStackTrace = freezeEvent.getException().getStackTrace();
		assertEquals(threadStackTrace.length, loggedStackTrace.length);

		for (int i = 0; i < threadStackTrace.length; i++) {
			assertEquals(threadStackTrace[i], loggedStackTrace[i]);
		}
	}
}
