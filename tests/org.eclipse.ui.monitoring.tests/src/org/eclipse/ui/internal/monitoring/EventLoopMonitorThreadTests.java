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

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.monitoring.EventLoopMonitorThread.Parameters;
import org.eclipse.ui.monitoring.PreferenceConstants;
import org.eclipse.ui.monitoring.UiFreezeEvent;

/**
 * Tests for {@link EventLoopMonitorThread} class.
 */
public class EventLoopMonitorThreadTests extends TestCase {
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

	@Override
	public void setUp() {
		getPreferences().setValue(PreferenceConstants.MONITORING_ENABLED, false);
		logger = new MockUiFreezeEventLogger();
		loggedEvents = logger.getLoggedEvents();
		sleepLock = new Object();
		numSleeps = 0;
		timestamp = 1;
	}

	@Override
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
		monitoringThread.endSleep();
		monitoringThread.endEvent();
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

	public void testStackDecimation() throws Exception {
		UiFreezeEvent event;

		monitoringThread = createTestThread(THRESHOLD_MS * 2);
		monitoringThread.start();
		monitoringThread.beginEvent();

		// Cycle a few events
		synchronized (sleepLock) {
			for (int i = 0; i < 3; ++i) {
				monitoringThread.beginEvent();
				runForCycles(1);
				monitoringThread.endEvent();
			}
		}

		// Test going one beyond the MAX_STACK_TRACES count to see that the count is decimated.
		int eventLength = MAX_STACK_TRACES + 2;
		synchronized (sleepLock) {
			monitoringThread.beginEvent();
			runForCycles(eventLength);
			monitoringThread.endEvent();
			runForCycles(3);
		}

		event = loggedEvents.get(0);
		assertNotNull("A long running event was not automatically published", event);
		assertEquals("Decimation did not resize the stack trace array properly", MIN_STACK_TRACES,
				event.getStackTraceSamples().length);

		// Decimation slows down the sampling rate by a factor of 2, so test the resampling reduction.
		eventLength = MAX_STACK_TRACES + (MIN_MAX_STACK_TRACE_DELTA - 1) * 2;
		synchronized (sleepLock) {
			monitoringThread.beginEvent();
			runForCycles(eventLength);
			monitoringThread.endEvent();
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
			monitoringThread.beginEvent();
			runForCycles(eventLength);
			monitoringThread.endEvent();
			runForCycles(3);
		}

		event = loggedEvents.get(2);
		assertNotNull("A long running event was not automatically published", event);
		assertEquals("Decimation did not reset the sampiling rate properly", MIN_STACK_TRACES,
				event.getStackTraceSamples().length);
	}

	public void testPublishPossibleDeadlock() throws Exception {
		monitoringThread = createTestThread(POLLING_RATE_MS * 4);
		monitoringThread.start();
		long maxDeadlock = FORCE_DEADLOCK_LOG_TIME_MILLIS;
		monitoringThread.beginEvent();

		synchronized (sleepLock) {
			// Cycle a few events to make sure the monitoring event thread is running.
			for (int i = 0; i < 3; ++i) {
				monitoringThread.beginEvent();
				runForCycles(1);
				monitoringThread.endEvent();
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

	public void testPublishNoDeadlocksWhenSleeping() throws Exception {
		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		monitoringThread.beginEvent();

		synchronized (sleepLock) {
			// Cycle a few events to make sure the monitoring event thread is running.
			for (int i = 0; i < 3; ++i) {
				monitoringThread.beginEvent();
				runForCycles(1);
				monitoringThread.endEvent();
			}
			monitoringThread.beginSleep();

			// Wait for the end of the event to propagate to the deadlock tracker.
			runForTime(FORCE_DEADLOCK_LOG_TIME_MILLIS * 2);
			runForCycles(3);
		}

		assertTrue("No deadlock events should get logged", loggedEvents.isEmpty());
	}

	public void testNoLoggingForSleep() throws Exception {
		int eventFactor = 5;
		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();

		// One level deep
		synchronized (sleepLock) {
			monitoringThread.beginSleep();
			runForTime(eventFactor * POLLING_RATE_MS);
			monitoringThread.endSleep();
			runForCycles(3);
		}

		assertTrue("Sleeping should not trigger a long running event", loggedEvents.isEmpty());
	}

	public void testEventLogging() throws Exception {
		int eventFactor = 5;
		long eventStartTime = 0;
		long eventStallDuration = 0;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();

		// One level deep
		synchronized (sleepLock) {
			monitoringThread.beginEvent(); // level 1
			eventStartTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventStartTime;
			monitoringThread.endEvent();
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

	public void testNestedEventLogging() throws Exception {
		int eventFactor = 6;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		long eventStartTime = 0;
		long eventStallDuration = 0;

		// Two levels deep
		synchronized (sleepLock) {
			monitoringThread.beginEvent(); // level 1
			runForCycles(1);
			monitoringThread.beginEvent(); // level 2
			eventStartTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventStartTime;
			monitoringThread.endEvent();
			monitoringThread.endEvent();
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

	public void testDoublyNestedEventLogging() throws Exception {
		int eventFactor = 7;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		long eventStartTime = 0;
		long eventStallDuration = 0;

		// Three levels deep
		synchronized (sleepLock) {
			monitoringThread.beginEvent(); // level 1
			runForCycles(1);
			monitoringThread.beginEvent(); // level 2
			runForCycles(1);
			monitoringThread.beginEvent(); // level 3
			eventStartTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventStartTime;
			monitoringThread.endEvent();
			monitoringThread.endEvent();
			monitoringThread.endEvent();
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

	public void testSeeLongEventInContinuationAfterNestedCall() throws Exception {
		int eventFactor = 4;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		long eventResumeTime = 0;
		long eventStallDuration = 0;

		// Exceed the threshold after the thread is started in the middle of an event, then end the
		// event and validate that no long event was logged.
		synchronized (sleepLock) {
			monitoringThread.beginEvent();
			// Initially the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 4; i++) {
				runForCycles(1);
				monitoringThread.beginEvent();
				monitoringThread.endEvent();
			}

			eventResumeTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			monitoringThread.endEvent();
			eventStallDuration = timestamp - eventResumeTime;
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A long running event didn't start from the nested return point", eventResumeTime,
				event.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				event.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), event.getStackTraceSamples().length);
	}

	public void testSeeLongEventInTheMiddleOfNestedCalls() throws Exception {
		int eventFactor = 4;
		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();
		long eventResumeTime = 0;
		long eventStallDuration = 0;

		// Exceed the threshold after the thread is started in the middle of an event, then end the
		// event and validate that no long event was logged.
		synchronized (sleepLock) {
			monitoringThread.beginEvent();
			// Initially the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				runForCycles(1);
				monitoringThread.beginEvent();
				monitoringThread.endEvent();
			}

			// This is the nested event UI freeze
			eventResumeTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventResumeTime;

			// Before exiting the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				monitoringThread.beginEvent();
				monitoringThread.endEvent();
				runForCycles(1);
			}

			monitoringThread.endEvent();
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A long running event didn't start from the nested return point", eventResumeTime,
				event.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				event.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), event.getStackTraceSamples().length);
	}

	public void testSeeSleepInTheMiddleOfNestedCalls() throws Exception {
		int eventFactor = 4;
		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();

		// Exceed the threshold after the thread is started in the middle of an event, then end the
		// event and validate that no long event was logged.
		synchronized (sleepLock) {
			monitoringThread.beginEvent();
			// Initially the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				runForCycles(1);
				monitoringThread.beginEvent();
				monitoringThread.endEvent();
			}

			// Nested events
			for (int i = 0; i < eventFactor; ++i) {
				runForCycles(1);
				monitoringThread.beginSleep();
				monitoringThread.endSleep();
			}

			// Before exiting the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				monitoringThread.beginEvent();
				monitoringThread.endEvent();
				runForCycles(1);
			}
			monitoringThread.endEvent();
			runForCycles(3);
		}

		assertTrue("A long running event should not be published if Display.sleep() was called",
				loggedEvents.isEmpty());
	}

	public void testConsecutiveSleeps() throws Exception {
		int eventFactor = 5;
		long eventStartTime = 0;
		long eventDuration = 0;

		monitoringThread = createTestThread(THRESHOLD_MS);
		monitoringThread.start();

		synchronized (sleepLock) {
			monitoringThread.beginSleep();
			runForTime(THRESHOLD_MS);
			monitoringThread.endSleep();
			eventStartTime = timestamp;
			runForCycles(3);
		}

		assertTrue("A long running event shold not be published during a sleep",
				loggedEvents.isEmpty());

		// Let a long time elapse between the last endSleep() and the next beginSleep().
		synchronized (sleepLock) {
			runForTime(THRESHOLD_MS * eventFactor);
			eventDuration = timestamp - eventStartTime;
			monitoringThread.beginSleep();
			monitoringThread.endSleep();
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
