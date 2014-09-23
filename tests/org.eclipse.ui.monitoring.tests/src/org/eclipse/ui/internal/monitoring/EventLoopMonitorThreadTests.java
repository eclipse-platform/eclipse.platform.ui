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
 *	   Simon Scholz <simon.scholz@vogella.com> - Bug 443391
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.monitoring.EventLoopMonitorThread.Parameters;
import org.eclipse.ui.monitoring.PreferenceConstants;
import org.eclipse.ui.monitoring.UiFreezeEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link EventLoopMonitorThread} class.
 */
public class EventLoopMonitorThreadTests {
	/**
	 * A mock event loop monitor thread used for JUnit tests.
	 */
	private static class MockEventLoopMonitorThread extends EventLoopMonitorThread {
		public MockEventLoopMonitorThread(Parameters args) throws Exception {
			super(args);
		}

		/**
		 * Overridden to provide an artificial time scale for testing.
		 */
		@Override
		protected long getTimestamp() {
			return timestamp;
		}

		/**
		 * Replaces the super-class implementation with a no-op. This breaks the implicit contract
		 * that some amount of time should have passed when sleepForMillis is called with a non-zero
		 * argument, but in this testing environment giving the unit tests complete control over
		 * the elapsed time allows the tests to be more deterministic.
		 */
		@Override
		protected void sleepForMillis(long nanoseconds) {
			synchronized (sleepLock) {
				++numSleeps;
				sleepLock.notifyAll();
			}
		}
	}

	private static final String FILTER_TRACES =
			"org.eclipse.swt.internal.gtk.OS.gtk_dialog_run,"
					+ "org.eclipse.e4.ui.workbench.addons.dndaddon.DnDManager.startDrag";
	/* NOTE: All time-related values in this class are in milliseconds. */
	private static final long MAX_TIMEOUT_MS = 1 * 1000; // 1 second
	private static final int THRESHOLD_MS = 100;
	private static final int POLLING_RATE_MS = THRESHOLD_MS / 2;
	public static final int FORCE_DEADLOCK_LOG_TIME_MILLIS = 10 * 60 * 1000; // == 10 minutes
	private static final int MIN_STACK_TRACES = 5;
	private static final int MAX_STACK_TRACES = 11;
	private static final int MIN_MAX_STACK_TRACE_DELTA = MAX_STACK_TRACES - MIN_STACK_TRACES;
	private MockEventLoopMonitorThread monitoringThread;
	private MockUiFreezeEventLogger logger;
	private List<UiFreezeEvent> loggedEvents;
	private static Object sleepLock;
	private static long numSleeps;
	private static volatile long timestamp;

	@Before
	public void setUp() {
		getPreferences().setValue(PreferenceConstants.MONITORING_ENABLED, false);
		logger = new MockUiFreezeEventLogger();
		loggedEvents = logger.getLoggedEvents();
		sleepLock = new Object();
		numSleeps = 0;
		timestamp = 1;
	}

	@After
	public void tearDown() throws Exception {
		if (monitoringThread != null) {
			shutdownMonitoringThread();
			monitoringThread = null;
		}
		loggedEvents.clear();
		getPreferences().setToDefault(PreferenceConstants.MONITORING_ENABLED);
	}

	private static IPreferenceStore getPreferences() {
		return MonitoringPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Creates and returns a EventLoopMonitorThread that fakes out the timer management to enable
	 * testing various long event scenarios.
	 */
	private static MockEventLoopMonitorThread createTestThread(int threshold) throws Exception {
		EventLoopMonitorThread.Parameters args = new Parameters();
		args.longEventThreshold = threshold - 1;
		args.initialSampleDelay = POLLING_RATE_MS - 1;
		args.dumpAllThreads = true;
		args.sampleInterval = POLLING_RATE_MS - 1;
		args.maxStackSamples = MIN_STACK_TRACES;
		args.deadlockThreshold = FORCE_DEADLOCK_LOG_TIME_MILLIS;
		args.filterTraces = FILTER_TRACES;

		return new MockEventLoopMonitorThread(args);
	}

	/**
	 * Shuts down the event monitoring thread.
	 */
	private void shutdownMonitoringThread() throws Exception {
		sendEvent(SWT.PostExternalEventDispatch);
		sendEvent(SWT.PostEvent);
		monitoringThread.shutdown();
		monitoringThread.join();
	}

	/**
	 * Runs the current thread for a specified amount of time for delays.
	 */
	private static void runForCycles(long numCycles) throws Exception {
		runForTime(POLLING_RATE_MS * numCycles);
	}

	/**
	 * Runs the current thread for a specified amount of time in milliseconds.
	 */
	private static void runForTime(long millis) throws Exception {
		synchronized (sleepLock) {
			while (millis > 0) {
				long next = Math.min(millis, POLLING_RATE_MS);
				timestamp += next;

				long sleeps = numSleeps;
				long endTime = System.currentTimeMillis() + MAX_TIMEOUT_MS;
				do {
					sleepLock.wait(MAX_TIMEOUT_MS);
				} while (sleeps == numSleeps && endTime - System.currentTimeMillis() > 0);

				millis -= next;
			}
		}
	}

	/**
	 * Returns the expected number of stack traces captured.
	 */
	private static int expectedStackCount(long runningTimeMs) {
		return Math.min((int) (runningTimeMs / POLLING_RATE_MS), MIN_STACK_TRACES);
	}

	private void sendEvent(int eventType) {
		Event event = new Event();
		event.type = eventType;
		monitoringThread.handleEvent(event);
	}

	@Test
	public void testStackDecimation() throws Exception {
		UiFreezeEvent event;

		monitoringThread = createTestThread(THRESHOLD_MS * 2);
		monitoringThread.start();
		sendEvent(SWT.PreEvent);

		// Cycle a few events
		synchronized (sleepLock) {
			for (int i = 0; i < 3; ++i) {
				sendEvent(SWT.PreEvent);
				runForCycles(1);
				sendEvent(SWT.PostEvent);
			}
		}

		// Test going one beyond the MAX_STACK_TRACES count to see that the count is decimated.
		int eventLength = MAX_STACK_TRACES + 2;
		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent);
			runForCycles(eventLength);
			sendEvent(SWT.PostEvent);
			runForCycles(3);
		}

		event = loggedEvents.get(0);
		assertNotNull("A long running event was not automatically published", event);
		assertEquals("Decimation did not resize the stack trace array properly", MIN_STACK_TRACES,
				event.getStackTraceSamples().length);

		// Decimation slows down the sampling rate by a factor of 2, so test the resampling reduction.
		eventLength = MAX_STACK_TRACES + (MIN_MAX_STACK_TRACE_DELTA - 1) * 2;
		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent);
			runForCycles(eventLength);
			sendEvent(SWT.PostEvent);
			runForCycles(3);
		}

		event = loggedEvents.get(1);
		assertNotNull("A long running event was not automatically published", event);
		assertEquals("Decimation did not reset the sampiling rate properly", MIN_STACK_TRACES,
				event.getStackTraceSamples().length);

		// Test the resampling reduction after two decimations.
		eventLength =
				MAX_STACK_TRACES + (MIN_MAX_STACK_TRACE_DELTA) * 2 + (MIN_MAX_STACK_TRACE_DELTA - 2) * 4;
		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent);
			runForCycles(eventLength);
			sendEvent(SWT.PostEvent);
			runForCycles(3);
		}

		event = loggedEvents.get(2);
		assertNotNull("A long running event was not automatically published", event);
		assertEquals("Decimation did not reset the sampiling rate properly", MIN_STACK_TRACES,
				event.getStackTraceSamples().length);
	}

	@Test
	public void testPublishPossibleDeadlock() throws Exception {
		monitoringThread = createTestThread(POLLING_RATE_MS * 4);
		monitoringThread.start();
		long maxDeadlock = FORCE_DEADLOCK_LOG_TIME_MILLIS;
		sendEvent(SWT.PreEvent);

		synchronized (sleepLock) {
			// Cycle a few events to make sure the monitoring event thread is running.
			for (int i = 0; i < 3; ++i) {
				sendEvent(SWT.PreEvent);
				runForCycles(1);
				sendEvent(SWT.PostEvent);
			}
			long startTime = timestamp;

			// Wait for the end of the event to propagate to the deadlock tracker.
			runForCycles(1);

			long remaining = maxDeadlock - (timestamp - startTime);
			runForTime(remaining - 1);
			assertTrue("No deadlock should get logged before its time", loggedEvents.isEmpty());

			// March time forward to trigger the possible deadlock logging.
			runForCycles(4);

			assertEquals("Incorrect number of events was logged", 1, loggedEvents.size());
			UiFreezeEvent event = loggedEvents.get(0);
			assertTrue("Possible deadlock logging should have stack a valid number of stack traces",
					event.getStackTraceSamples().length >= MIN_STACK_TRACES);

			// Extending the UI freeze shouldn't log any more events.
			runForTime(maxDeadlock * 2);
			runForCycles(3);
		}

		assertEquals("No more deadlock events should get logged", 1, loggedEvents.size());
	}

	@Test
	public void testPublishNoDeadlocksWhenSleeping() throws Exception {
		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		sendEvent(SWT.PreEvent);

		synchronized (sleepLock) {
			// Cycle a few events to make sure the monitoring event thread is running.
			for (int i = 0; i < 3; ++i) {
				sendEvent(SWT.PreEvent);
				runForCycles(1);
				sendEvent(SWT.PostEvent);
			}
			sendEvent(SWT.PreExternalEventDispatch);

			// Wait for the end of the event to propagate to the deadlock tracker.
			runForTime(FORCE_DEADLOCK_LOG_TIME_MILLIS * 2);
			runForCycles(3);
		}

		assertTrue("No deadlock events should get logged", loggedEvents.isEmpty());
	}

	@Test
	public void testNoLoggingForSleep() throws Exception {
		int eventFactor = 5;
		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();

		// One level deep
		synchronized (sleepLock) {
			sendEvent(SWT.PreExternalEventDispatch);
			runForTime(eventFactor * POLLING_RATE_MS);
			sendEvent(SWT.PostExternalEventDispatch);
			runForCycles(3);
		}

		assertTrue("Sleeping should not trigger a long running event", loggedEvents.isEmpty());
	}

	@Test
	public void testEventLogging() throws Exception {
		int eventFactor = 5;
		long eventStartTime = 0;
		long eventStallDuration = 0;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();

		// One level deep
		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent); // level 1
			eventStartTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventStartTime;
			sendEvent(SWT.PostEvent);
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A long running event log has an incorrect start time", eventStartTime,
				event.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				event.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testNestedEventLogging() throws Exception {
		int eventFactor = 6;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		long eventStartTime = 0;
		long eventStallDuration = 0;

		// Two levels deep
		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent); // level 1
			runForCycles(1);
			sendEvent(SWT.PreEvent); // level 2
			eventStartTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventStartTime;
			sendEvent(SWT.PostEvent);
			sendEvent(SWT.PostEvent);
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A long running event log has an incorrect start time", eventStartTime,
				event.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				event.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testDoublyNestedEventLogging() throws Exception {
		int eventFactor = 7;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		long eventStartTime = 0;
		long eventStallDuration = 0;

		// Three levels deep
		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent); // level 1
			runForCycles(1);
			sendEvent(SWT.PreEvent); // level 2
			runForCycles(1);
			sendEvent(SWT.PreEvent); // level 3
			eventStartTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventStartTime;
			sendEvent(SWT.PostEvent);
			sendEvent(SWT.PostEvent);
			sendEvent(SWT.PostEvent);
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A long running event log has an incorrect start time", eventStartTime,
				event.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				event.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testSeeLongEventInContinuationAfterNestedCall() throws Exception {
		int eventFactor = 4;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		long eventResumeTime = 0;
		long eventStallDuration = 0;

		// Exceed the threshold after the thread is started in the middle of an event, then end the
		// event and validate that no long event was logged.
		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent);
			// Initially the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 4; i++) {
				runForCycles(1);
				sendEvent(SWT.PreEvent);
				sendEvent(SWT.PostEvent);
			}

			eventResumeTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			sendEvent(SWT.PostEvent);
			eventStallDuration = timestamp - eventResumeTime;
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A long running event didn't start from the nested return point",
				eventResumeTime, event.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				event.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testSeeLongEventInTheMiddleOfNestedCalls() throws Exception {
		int eventFactor = 4;
		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		long eventResumeTime = 0;
		long eventStallDuration = 0;

		// Exceed the threshold after the thread is started in the middle of an event, then end
		// the event and validate that no long event was logged.
		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent);
			// Initially the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				runForCycles(1);
				sendEvent(SWT.PreEvent);
				sendEvent(SWT.PostEvent);
			}

			// This is the nested event UI freeze
			eventResumeTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventResumeTime;

			// Before exiting the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				sendEvent(SWT.PreEvent);
				sendEvent(SWT.PostEvent);
				runForCycles(1);
			}

			sendEvent(SWT.PostEvent);
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A long running event didn't start from the nested return point",
				eventResumeTime, event.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				event.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testSeeSleepInTheMiddleOfNestedCalls() throws Exception {
		int eventFactor = 4;
		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();

		// Exceed the threshold after the thread is started in the middle of an event, then end
		// the event and validate that no long event was logged.
		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent);
			// Initially the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				runForCycles(1);
				sendEvent(SWT.PreEvent);
				sendEvent(SWT.PostEvent);
			}

			// Nested events
			for (int i = 0; i < eventFactor; ++i) {
				runForCycles(1);
				sendEvent(SWT.PreExternalEventDispatch);
				sendEvent(SWT.PostExternalEventDispatch);
			}

			// Before exiting the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				sendEvent(SWT.PreEvent);
				sendEvent(SWT.PostEvent);
				runForCycles(1);
			}
			sendEvent(SWT.PostEvent);
			runForCycles(3);
		}

		assertTrue("A long running event should not be published during an external event dispatch",
				loggedEvents.isEmpty());
	}

	@Test
	public void testConsecutiveSleeps() throws Exception {
		int eventFactor = 5;
		long eventStartTime = 0;
		long eventDuration = 0;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();

		synchronized (sleepLock) {
			sendEvent(SWT.PreEvent);
			sendEvent(SWT.PreExternalEventDispatch);
			runForTime(THRESHOLD_MS);
			sendEvent(SWT.PostExternalEventDispatch);
			eventStartTime = timestamp;
			runForCycles(3);
		}

		assertTrue("A long running event shold not be published during an external event dispatch",
				loggedEvents.isEmpty());

		// Let a long time elapse between the last PostExternalEventDispatch and the next
		// PreExternalEventDispatch.
		synchronized (sleepLock) {
			runForTime(THRESHOLD_MS * eventFactor);
			eventDuration = timestamp - eventStartTime;
			sendEvent(SWT.PreExternalEventDispatch);
			sendEvent(SWT.PostExternalEventDispatch);
			sendEvent(SWT.PostEvent);
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A long running event log has an incorrect start time", eventStartTime,
				event.getStartTimestamp());
		assertEquals("A long running event's duration is incorrect", eventDuration,
				event.getTotalDuration());
	}
}
