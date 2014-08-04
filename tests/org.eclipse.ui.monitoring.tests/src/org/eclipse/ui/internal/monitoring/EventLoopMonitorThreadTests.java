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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.monitoring.EventLoopMonitorThread.Parameters;
import org.eclipse.ui.monitoring.PreferenceConstants;
import org.eclipse.ui.monitoring.UiFreezeEvent;

import java.util.List;

/**
 * JUnit tests for {@link EventLoopMonitorThread}.
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
		 * argument, but in this testing environment giving the unit tests complete control over the
		 * elapsed time allows the tests to be more deterministic.
		 */
		@Override
		protected void sleepForMillis(long nanoseconds) {
			synchronized (sleepLock) {
				++numSleeps;
				sleepLock.notifyAll();
			}
		}
	}

	/* NOTE: All time-related values in this class are in milliseconds. */
	public static final int FORCE_DEADLOCK_LOG_TIME_MILLIS = 10 * 60 * 1000; // == 10 minutes
	public static final String FILTER_TRACES =
			"org.eclipse.swt.internal.gtk.OS.gtk_dialog_run,"
					+ "org.eclipse.e4.ui.workbench.addons.dndaddon.DnDManager.startDrag";
	private static final long MAX_TIMEOUT_MS = 1 * 1000; // 1 second
	private static final int THRESHOLD_MS = 100;
	private static final int POLLING_RATE_MS = THRESHOLD_MS / 2;
	private static final int MIN_STACK_TRACES = 5;
	private static final int MAX_STACK_TRACES = 11;
	private static final int MIN_MAX_STACK_TRACE_DELTA = MAX_STACK_TRACES - MIN_STACK_TRACES;
	MockEventLoopMonitorThread testThread;
	private MockUiFreezeEventLogger logger;
	private static Object sleepLock;
	private static long numSleeps;
	private static volatile long timestamp;

	@Override
	public void setUp() {
		getPreferences().setValue(PreferenceConstants.MONITORING_ENABLED, false);
		logger = new MockUiFreezeEventLogger();
		sleepLock = new Object();
		numSleeps = 0;
		timestamp = 1;
	}

	@Override
	public void tearDown() throws Exception {
		shutdownThread(testThread);
		testThread = null;
		getLoggedEvents().clear();
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
		args.loggingThreshold = threshold - 1;
		args.samplingThreshold = POLLING_RATE_MS - 1;
		args.dumpAllThreads = true;
		args.minimumPollingDelay = POLLING_RATE_MS - 1;
		args.loggedTraceCount = MIN_STACK_TRACES;
		args.deadlockDelta = FORCE_DEADLOCK_LOG_TIME_MILLIS;
		args.filterTraces = FILTER_TRACES;

		return new MockEventLoopMonitorThread(args);
	}

	/**
	 * Shuts down the given monitoring thread.
	 */
	private static void shutdownThread(EventLoopMonitorThread thread) throws Exception {
		thread.endSleep();
		thread.endEvent();
		thread.shutdown();
		thread.join();
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

	private List<UiFreezeEvent> getLoggedEvents() {
		return logger.getLoggedEvents();
	}

	public void testStackDecimation() throws Exception {
		UiFreezeEvent snapshot;

		testThread = createTestThread(THRESHOLD_MS * 2);
		testThread.start();
		testThread.beginEvent();

		// Cycle a few events
		synchronized (sleepLock) {
			for (int i = 0; i < 3; ++i) {
				testThread.beginEvent();
				runForCycles(1);
				testThread.endEvent();
			}
		}

		// Test going one beyond the MAX_STACK_TRACES count to see that the count is decimated.
		int eventLength = MAX_STACK_TRACES + 2;
		synchronized (sleepLock) {
			testThread.beginEvent();
			runForCycles(eventLength);
			testThread.endEvent();
			runForCycles(3);
		}

		snapshot = getLoggedEvents().get(0);
		assertNotNull("A long running event was not automatically published", snapshot);
		assertEquals("Decimation did not resize the stack trace array properly", MIN_STACK_TRACES,
				snapshot.getSampleCount());

		// Decimation slows down the sampling rate by a factor of 2, so test the resampling reduction.
		eventLength = MAX_STACK_TRACES + (MIN_MAX_STACK_TRACE_DELTA - 1) * 2;
		synchronized (sleepLock) {
			testThread.beginEvent();
			runForCycles(eventLength);
			testThread.endEvent();
			runForCycles(3);
		}

		snapshot = getLoggedEvents().get(1);
		assertNotNull("A long running event was not automatically published", snapshot);
		assertEquals("Decimation did not reset the sampiling rate properly", MIN_STACK_TRACES,
				snapshot.getSampleCount());

		// Test the resampling reduction after two decimations.
		eventLength =
				MAX_STACK_TRACES + (MIN_MAX_STACK_TRACE_DELTA) * 2 + (MIN_MAX_STACK_TRACE_DELTA - 2) * 4;
		synchronized (sleepLock) {
			testThread.beginEvent();
			runForCycles(eventLength);
			testThread.endEvent();
			runForCycles(3);
		}

		snapshot = getLoggedEvents().get(2);
		assertNotNull("A long running event was not automatically published", snapshot);
		assertEquals("Decimation did not reset the sampiling rate properly", MIN_STACK_TRACES,
				snapshot.getSampleCount());
	}

	public void testPublishPossibleDeadlock() throws Exception {
		testThread = createTestThread(Integer.MAX_VALUE - 1);
		testThread.start();
		long maxDeadlock = FORCE_DEADLOCK_LOG_TIME_MILLIS;
		testThread.beginEvent();

		synchronized (sleepLock) {
			// Cycle a few events to make sure the monitoring event thread is running.
			for (int i = 0; i < 3; ++i) {
				testThread.beginEvent();
				runForCycles(1);
				testThread.endEvent();
			}
			long startTime = timestamp;

			// Wait for the end of the event to propagate to the deadlock tracker.
			runForCycles(1);

			long remaining = maxDeadlock - (timestamp - startTime);
			runForTime(remaining - 1);
			assertTrue("No deadlock should get logged before its time", getLoggedEvents().isEmpty());

			// March time forward to trigger the possible deadlock logging.
			runForCycles(4);

			assertEquals("Incorrect number of events was logged", 1, getLoggedEvents().size());
			UiFreezeEvent snapshot = getLoggedEvents().get(0);
			assertTrue("Possible deadlock logging should have stack a valid number of stack traces",
					snapshot.getSampleCount() >= MIN_STACK_TRACES);

			// Extending the UI freeze shouldn't log any more events.
			runForTime(maxDeadlock * 2);
			runForCycles(3);
		}

		assertEquals("No more deadlock events should get logged", 1, getLoggedEvents().size());
	}

	public void testPublishNoDeadlocksWhenSleeping() throws Exception {
		testThread = createTestThread(Integer.MAX_VALUE - 1);
		testThread.start();
		testThread.beginEvent();

		synchronized (sleepLock) {
			// Cycle a few events to make sure the monitoring event thread is running.
			for (int i = 0; i < 3; ++i) {
				testThread.beginEvent();
				runForCycles(1);
				testThread.endEvent();
			}
			testThread.beginSleep();

			// Wait for the end of the event to propagate to the deadlock tracker.
			runForTime(FORCE_DEADLOCK_LOG_TIME_MILLIS * 2);
			runForCycles(3);
		}

		assertTrue("No deadlock events should get logged", getLoggedEvents().isEmpty());
	}

	public void testNoLoggingForSleep() throws Exception {
		int eventFactor = 5;
		testThread = createTestThread(THRESHOLD_MS);
		testThread.start();

		// One level deep
		synchronized (sleepLock) {
			testThread.beginSleep();
			runForTime(eventFactor * POLLING_RATE_MS);
			testThread.endSleep();
			runForCycles(3);
		}

		assertTrue("Sleeping should not trigger a long running event", getLoggedEvents().isEmpty());
	}

	public void testEventLogging() throws Exception {
		int eventFactor = 5;
		long eventStartTime = 0;
		long eventStallDuration = 0;

		testThread = createTestThread(THRESHOLD_MS);
		testThread.start();

		// One level deep
		synchronized (sleepLock) {
			testThread.beginEvent(); // level 1
			eventStartTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventStartTime;
			testThread.endEvent();
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, getLoggedEvents().size());
		UiFreezeEvent snapshot = getLoggedEvents().get(0);
		assertEquals("A long running event log has an incorrect start time", eventStartTime,
				snapshot.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				snapshot.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), snapshot.getSampleCount());
	}

	public void testNestedEventLogging() throws Exception {
		int eventFactor = 6;

		testThread = createTestThread(THRESHOLD_MS);
		testThread.start();
		long eventStartTime = 0;
		long eventStallDuration = 0;

		// Two levels deep
		synchronized (sleepLock) {
			testThread.beginEvent(); // level 1
			runForCycles(1);
			testThread.beginEvent(); // level 2
			eventStartTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventStartTime;
			testThread.endEvent();
			testThread.endEvent();
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, getLoggedEvents().size());
		UiFreezeEvent snapshot = getLoggedEvents().get(0);
		assertEquals("A long running event log has an incorrect start time", eventStartTime,
				snapshot.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				snapshot.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), snapshot.getSampleCount());
	}

	public void testDoublyNestedEventLogging() throws Exception {
		int eventFactor = 7;

		testThread = createTestThread(THRESHOLD_MS);
		testThread.start();
		long eventStartTime = 0;
		long eventStallDuration = 0;

		// Three levels deep
		synchronized (sleepLock) {
			testThread.beginEvent(); // level 1
			runForCycles(1);
			testThread.beginEvent(); // level 2
			runForCycles(1);
			testThread.beginEvent(); // level 3
			eventStartTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventStartTime;
			testThread.endEvent();
			testThread.endEvent();
			testThread.endEvent();
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, getLoggedEvents().size());
		UiFreezeEvent snapshot = getLoggedEvents().get(0);
		assertEquals("A long running event log has an incorrect start time", eventStartTime,
				snapshot.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				snapshot.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), snapshot.getSampleCount());
	}

	public void testSeeLongEventInContinuationAfterNestedCall() throws Exception {
		int eventFactor = 4;

		testThread = createTestThread(THRESHOLD_MS);
		testThread.start();
		long eventResumeTime = 0;
		long eventStallDuration = 0;

		// Exceed the threshold after the thread is started in the middle of an event, then end the
		// event and validate that no long event was logged.
		synchronized (sleepLock) {
			testThread.beginEvent();
			// Initially the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 4; i++) {
				runForCycles(1);
				testThread.beginEvent();
				testThread.endEvent();
			}

			eventResumeTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			testThread.endEvent();
			eventStallDuration = timestamp - eventResumeTime;
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, getLoggedEvents().size());
		UiFreezeEvent snapshot = getLoggedEvents().get(0);
		assertEquals("A long running event didn't start from the nested return point", eventResumeTime,
				snapshot.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				snapshot.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), snapshot.getSampleCount());
	}

	public void testSeeLongEventInTheMiddleOfNestedCalls() throws Exception {
		int eventFactor = 4;
		testThread = createTestThread(THRESHOLD_MS);
		testThread.start();
		long eventResumeTime = 0;
		long eventStallDuration = 0;

		// Exceed the threshold after the thread is started in the middle of an event, then end the
		// event and validate that no long event was logged.
		synchronized (sleepLock) {
			testThread.beginEvent();
			// Initially the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				runForCycles(1);
				testThread.beginEvent();
				testThread.endEvent();
			}

			// This is the nested event UI freeze
			eventResumeTime = timestamp;
			runForTime(eventFactor * THRESHOLD_MS);
			eventStallDuration = timestamp - eventResumeTime;

			// Before exiting the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				testThread.beginEvent();
				testThread.endEvent();
				runForCycles(1);
			}

			testThread.endEvent();
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, getLoggedEvents().size());
		UiFreezeEvent snapshot = getLoggedEvents().get(0);
		assertEquals("A long running event didn't start from the nested return point", eventResumeTime,
				snapshot.getStartTimestamp());
		assertEquals("A long running event's duration was incorrect", eventStallDuration,
				snapshot.getTotalDuration());
		assertEquals("A long running event didn't capture a good range of stack traces",
				expectedStackCount(eventStallDuration), snapshot.getSampleCount());
	}

	public void testSeeSleepInTheMiddleOfNestedCalls() throws Exception {
		int eventFactor = 4;
		testThread = createTestThread(THRESHOLD_MS);
		testThread.start();

		// Exceed the threshold after the thread is started in the middle of an event, then end the
		// event and validate that no long event was logged.
		synchronized (sleepLock) {
			testThread.beginEvent();
			// Initially the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				runForCycles(1);
				testThread.beginEvent();
				testThread.endEvent();
			}

			// Nested events
			for (int i = 0; i < eventFactor; ++i) {
				runForCycles(1);
				testThread.beginSleep();
				testThread.endSleep();
			}

			// Before exiting the outer thread is invoking nested events that are responsive.
			for (int i = 0; i < 3; i++) {
				testThread.beginEvent();
				testThread.endEvent();
				runForCycles(1);
			}
			testThread.endEvent();
			runForCycles(3);
		}

		assertTrue("A long running event should not be published if Display.sleep() was called",
				getLoggedEvents().isEmpty());
	}

	public void testConsecutiveSleeps() throws Exception {
		int eventFactor = 5;
		long eventStartTime = 0;
		long eventDuration = 0;

		testThread = createTestThread(THRESHOLD_MS);
		testThread.start();

		synchronized (sleepLock) {
			testThread.beginSleep();
			runForTime(THRESHOLD_MS);
			testThread.endSleep();
			eventStartTime = timestamp;
			runForCycles(3);
		}

		assertTrue("A long running event shold not be published during a sleep",
				getLoggedEvents().isEmpty());

		// Let a long time elapse between the last endSleep() and the next beginSleep().
		synchronized (sleepLock) {
			runForTime(THRESHOLD_MS * eventFactor);
			eventDuration = timestamp - eventStartTime;
			testThread.beginSleep();
			testThread.endSleep();
			runForCycles(3);
		}

		assertEquals("Incorrect number of long events was logged", 1, getLoggedEvents().size());
		UiFreezeEvent snapshot = getLoggedEvents().get(0);
		assertEquals("A long running event log has an incorrect start time", eventStartTime,
				snapshot.getStartTimestamp());
		assertEquals("A long running event's duration is incorrect", eventDuration,
				snapshot.getTotalDuration());
	}
}
