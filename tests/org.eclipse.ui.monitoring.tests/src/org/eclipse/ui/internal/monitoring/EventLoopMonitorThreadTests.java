/*******************************************************************************
 * Copyright (C) 2014, 2019 Google Inc and others.
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
 *     Christoph Läubrich - change to new preference store API
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.monitoring.EventLoopMonitorThread.Parameters;
import org.eclipse.ui.monitoring.PreferenceConstants;
import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link EventLoopMonitorThread} class.
 */
public class EventLoopMonitorThreadTests {
	/* NOTE: All time-related values in this class are in milliseconds. */
	private static final long MAX_TIMEOUT_MS = 3 * 1000; // 3 seconds
	private static final int FREEZE_THRESHOLD_MS = 100;
	private static final int SAMPLE_INTERVAL_MS = FREEZE_THRESHOLD_MS * 2 / 3;
	public static final int FORCE_DEADLOCK_LOG_TIME_MS = 10 * 60 * 1000; // == 10 minutes
	private static final int MIN_STACK_TRACES = 5;
	private static final int MAX_STACK_TRACES = 11;
	private static final int MIN_MAX_STACK_TRACE_DELTA = MAX_STACK_TRACES - MIN_STACK_TRACES;
	private static final String UI_THREAD_FILTER =
			"org.eclipse.swt.internal.gtk.OS.gtk_dialog_run"
			+ ",org.eclipse.e4.ui.workbench.addons.dndaddon.DnDManager.startDrag";
	private static final String NONINTERESTING_THREAD_FILTER =
			"java.*"
			+ ",sun.*"
			+ ",org.eclipse.core.internal.jobs.WorkerPool.sleep"
			+ ",org.eclipse.core.internal.jobs.WorkerPool.startJob"
			+ ",org.eclipse.core.internal.jobs.Worker.run";

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
			monitorThreadControl.pauseThreadAndWaitForResume();
		}
	}

	private MockEventLoopMonitorThread monitoringThread;
	private MockUiFreezeEventLogger logger;
	private List<UiFreezeEvent> loggedEvents;
	private static ThreadControl monitorThreadControl;
	private static long timestamp;

	@Before
	public void setUp() {
		MonitoringPlugin.getPreferenceStore().setValue(PreferenceConstants.MONITORING_ENABLED, false);
		logger = new MockUiFreezeEventLogger();
		loggedEvents = logger.getLoggedEvents();
		monitorThreadControl = new ThreadControl(MAX_TIMEOUT_MS);
		timestamp = 1;
	}

	@After
	public void tearDown() throws Exception {
		if (monitoringThread != null) {
			shutdownMonitoringThread();
			monitoringThread = null;
		}
		loggedEvents.clear();
		MonitoringPlugin.getPreferenceStore().setToDefault(PreferenceConstants.MONITORING_ENABLED);
	}

	/**
	 * Creates and returns a EventLoopMonitorThread that fakes out the timer management to enable
	 * testing various freeze event scenarios.
	 */
	private static MockEventLoopMonitorThread createTestThread(int threshold) throws Exception {
		EventLoopMonitorThread.Parameters args = new Parameters();
		args.longEventWarningThreshold = threshold - 1;
		args.longEventErrorThreshold = threshold - 1;
		args.maxStackSamples = MIN_STACK_TRACES;
		args.deadlockThreshold = FORCE_DEADLOCK_LOG_TIME_MS;
		args.uiThreadFilter = UI_THREAD_FILTER;
		args.noninterestingThreadFilter = NONINTERESTING_THREAD_FILTER;

		return new MockEventLoopMonitorThread(args);
	}

	/**
	 * Shuts down the event monitoring thread.
	 */
	private void shutdownMonitoringThread() throws Exception {
		sendEvent(SWT.PostExternalEventDispatch);
		sendEvent(SWT.PostEvent);
		monitorThreadControl.waitUntilThreadIsPaused();
		monitoringThread.shutdown();
		monitorThreadControl.resumeThread();
		monitoringThread.join();
	}

	/**
	 * Runs the current thread for a specified amount of time for delays.
	 */
	private static void runForCycles(long numCycles) throws Exception {
		runForTime(SAMPLE_INTERVAL_MS * numCycles);
	}

	/**
	 * Runs the current thread for a specified amount of time in milliseconds.
	 */
	private static void runForTime(long millis) throws Exception {
		while (millis > 0) {
			monitorThreadControl.waitUntilThreadIsPaused();

			long next = Math.min(millis, SAMPLE_INTERVAL_MS);
			timestamp += next;
			millis -= next;

			monitorThreadControl.resumeThread();
		}
	}

	/**
	 * Returns the expected number of stack traces captured.
	 */
	private static int expectedStackCount(long runningTimeMs) {
		return Math.min((int) (runningTimeMs / SAMPLE_INTERVAL_MS), MIN_STACK_TRACES);
	}

	private void sendEvent(int eventType) {
		monitorThreadControl.waitUntilThreadIsPaused();

		Event event = new Event();
		event.type = eventType;
		monitoringThread.handleEvent(event);

		monitorThreadControl.resumeThread();
	}

	/**
	 * Returns relative times of the stack samples in a text form.
	 */
	private String getStackSamplesTimeline(UiFreezeEvent event) {
		StringBuilder buf = new StringBuilder();
		for (StackSample sample : event.getStackTraceSamples()) {
			if (buf.length() != 0) {
				buf.append(' ');
			}
			buf.append(sample.getTimestamp() - event.getStartTimestamp());
		}
		return buf.toString();
	}

	@Test
	public void testStackDecimation() throws Exception {
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS * 2);
		monitoringThread.start();
		sendEvent(SWT.PreEvent);

		// Cycle a few events
		for (int i = 0; i < 3; ++i) {
			sendEvent(SWT.PreEvent);
			runForCycles(1);
			sendEvent(SWT.PostEvent);
		}

		// Test going one beyond the MAX_STACK_TRACES count to see that the count is decimated.
		int eventLength = MAX_STACK_TRACES + 2;
		sendEvent(SWT.PreEvent);
		runForCycles(eventLength);
		sendEvent(SWT.PostEvent);
		runForCycles(3);

		UiFreezeEvent event = loggedEvents.get(0);
		assertNotNull("A freeze event was not automatically published", event);
		assertEquals("Decimation did not resize the stack trace array properly", MIN_STACK_TRACES,
				event.getStackTraceSamples().length);

		// Decimation slows down the sampling rate by a factor of 2, so test the resampling reduction.
		eventLength = MAX_STACK_TRACES + (MIN_MAX_STACK_TRACE_DELTA - 1) * 2;
		sendEvent(SWT.PreEvent);
		runForCycles(eventLength);
		sendEvent(SWT.PostEvent);
		runForCycles(3);

		event = loggedEvents.get(1);
		assertNotNull("A freeze event was not automatically published", event);
		assertEquals("Decimation did not reset the sampiling rate properly", MIN_STACK_TRACES,
				event.getStackTraceSamples().length);

		// Test the resampling reduction after two decimations.
		eventLength =
				MAX_STACK_TRACES + (MIN_MAX_STACK_TRACE_DELTA) * 2 + (MIN_MAX_STACK_TRACE_DELTA - 2) * 4;
		sendEvent(SWT.PreEvent);
		runForCycles(eventLength);
		sendEvent(SWT.PostEvent);
		runForCycles(3);

		event = loggedEvents.get(2);
		assertNotNull("A freeze event was not automatically published", event);
		assertEquals("Decimation did not reset the sampiling rate properly", MIN_STACK_TRACES,
				event.getStackTraceSamples().length);
	}

	@Test
	public void testPublishPossibleDeadlock() throws Exception {
		monitoringThread = createTestThread(SAMPLE_INTERVAL_MS * 4);
		monitoringThread.start();
		long maxDeadlock = FORCE_DEADLOCK_LOG_TIME_MS;
		sendEvent(SWT.PreEvent);

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
		assertTrue("Possible deadlock logging should have a valid number of stack traces",
				event.getStackTraceSamples().length >= MIN_STACK_TRACES);

		// Extending the UI freeze shouldn't log any more events.
		runForTime(maxDeadlock * 2);
		runForCycles(3);

		assertEquals("No more deadlock events should get logged", 1, loggedEvents.size());
	}

	@Test
	public void testPublishNoDeadlocksWhenSleeping() throws Exception {
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS);
		monitoringThread.start();
		sendEvent(SWT.PreEvent);

		// Cycle a few events to make sure the monitoring event thread is running.
		for (int i = 0; i < 3; ++i) {
			sendEvent(SWT.PreEvent);
			runForCycles(1);
			sendEvent(SWT.PostEvent);
		}
		sendEvent(SWT.PreExternalEventDispatch);

		// Wait for the end of the event to propagate to the deadlock tracker.
		runForTime(FORCE_DEADLOCK_LOG_TIME_MS * 2);
		runForCycles(3);

		assertTrue("No deadlock events should get logged", loggedEvents.isEmpty());
	}

	@Test
	public void testNoLoggingForSleep() throws Exception {
		final int freezeDurationFactor = 4;
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS);
		monitoringThread.start();

		// One level deep
		sendEvent(SWT.PreExternalEventDispatch);
		runForTime(FREEZE_THRESHOLD_MS * freezeDurationFactor);
		sendEvent(SWT.PostExternalEventDispatch);
		runForCycles(3);

		assertTrue("Sleeping should not trigger a freeze event", loggedEvents.isEmpty());
	}

	@Test
	public void testEventLogging() throws Exception {
		final int freezeDurationFactor = 5;
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS);
		monitoringThread.start();
		long eventStartTime;
		long freezeDuration;

		// One level deep
		sendEvent(SWT.PreEvent); // level 1
		eventStartTime = timestamp;
		runForTime(FREEZE_THRESHOLD_MS * freezeDurationFactor);
		freezeDuration = timestamp - eventStartTime;
		sendEvent(SWT.PostEvent);
		runForCycles(3);

		assertEquals("Incorrect number of freeze events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A freeze event log has an incorrect start time", eventStartTime,
				event.getStartTimestamp());
		assertEquals("A freeze event's duration was incorrect", freezeDuration,
				event.getTotalDuration());
		assertEquals("A freeze event didn't capture a good range of stack samples ("
				+ getStackSamplesTimeline(event) + ")",
				expectedStackCount(freezeDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testNestedEventLogging() throws Exception {
		final int freezeDurationFactor = 6;
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS);
		monitoringThread.start();
		long eventStartTime;
		long freezeDuration;

		// Two levels deep
		sendEvent(SWT.PreEvent); // level 1
		runForCycles(1);
		sendEvent(SWT.PreEvent); // level 2
		eventStartTime = timestamp;
		runForTime(FREEZE_THRESHOLD_MS * freezeDurationFactor);
		freezeDuration = timestamp - eventStartTime;
		sendEvent(SWT.PostEvent);
		sendEvent(SWT.PostEvent);
		runForCycles(3);

		assertEquals("Incorrect number of freeze events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A freeze event log has an incorrect start time", eventStartTime,
				event.getStartTimestamp());
		assertEquals("A freeze event's duration was incorrect", freezeDuration,
				event.getTotalDuration());
		assertEquals("A freeze event didn't capture a good range of stack samples ("
				+ getStackSamplesTimeline(event) + ")",
				expectedStackCount(freezeDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testDoublyNestedEventLogging() throws Exception {
		final int freezeDurationFactor = 7;
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS);
		monitoringThread.start();
		long eventStartTime;
		long freezeDuration;

		// Three levels deep
		sendEvent(SWT.PreEvent); // level 1
		runForCycles(1);
		sendEvent(SWT.PreEvent); // level 2
		runForCycles(1);
		sendEvent(SWT.PreEvent); // level 3
		eventStartTime = timestamp;
		runForTime(FREEZE_THRESHOLD_MS * freezeDurationFactor);
		freezeDuration = timestamp - eventStartTime;
		sendEvent(SWT.PostEvent);
		sendEvent(SWT.PostEvent);
		sendEvent(SWT.PostEvent);
		runForCycles(3);

		assertEquals("Incorrect number of freeze events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A freeze event log has an incorrect start time", eventStartTime,
				event.getStartTimestamp());
		assertEquals("A freeze event's duration was incorrect", freezeDuration,
				event.getTotalDuration());
		assertEquals("A freeze event didn't capture a good range of stack samples ("
				+ getStackSamplesTimeline(event) + ")",
				expectedStackCount(freezeDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testSeeLongEventInContinuationAfterNestedCall() throws Exception {
		final int freezeDurationFactor = 5;
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS);
		monitoringThread.start();
		long eventResumeTime;
		long freezeDuration;

		// Exceed the threshold after the thread is started in the middle of an event, then end
		// the event and validate that no freeze event was logged.
		sendEvent(SWT.PreEvent);
		// Initially the outer thread is invoking nested events that are responsive.
		for (int i = 0; i < 4; i++) {
			runForCycles(1);
			sendEvent(SWT.PreEvent);
			sendEvent(SWT.PostEvent);
		}

		eventResumeTime = timestamp;
		runForTime(FREEZE_THRESHOLD_MS * freezeDurationFactor);
		sendEvent(SWT.PostEvent);
		freezeDuration = timestamp - eventResumeTime;
		runForCycles(3);

		assertEquals("Incorrect number of freeze events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A freeze event didn't start from the nested return point",
				eventResumeTime, event.getStartTimestamp());
		assertEquals("A freeze event's duration was incorrect", freezeDuration,
				event.getTotalDuration());
		assertEquals("A freeze event didn't capture a good range of stack samples ("
				+ getStackSamplesTimeline(event) + ")",
				expectedStackCount(freezeDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testSeeLongEventInTheMiddleOfNestedCalls() throws Exception {
		final int freezeDurationFactor = 5;
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS);
		monitoringThread.start();
		long eventResumeTime;
		long freezeDuration;

		// Exceed the threshold after the thread is started in the middle of an event, then end
		// the event and validate that no freeze event was logged.
		sendEvent(SWT.PreEvent);
		// Initially the outer thread is invoking nested events that are responsive.
		for (int i = 0; i < 3; i++) {
			runForCycles(1);
			sendEvent(SWT.PreEvent);
			sendEvent(SWT.PostEvent);
		}

		// This is the nested event UI freeze
		eventResumeTime = timestamp;
		runForTime(FREEZE_THRESHOLD_MS * freezeDurationFactor);
		freezeDuration = timestamp - eventResumeTime;

		// Before exiting the outer thread is invoking nested events that are responsive.
		for (int i = 0; i < 3; i++) {
			sendEvent(SWT.PreEvent);
			sendEvent(SWT.PostEvent);
			runForCycles(1);
		}

		sendEvent(SWT.PostEvent);
		runForCycles(3);

		assertEquals("Incorrect number of freeze events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A freeze event didn't start from the nested return point",
				eventResumeTime, event.getStartTimestamp());
		assertEquals("A freeze event's duration was incorrect", freezeDuration,
				event.getTotalDuration());
		assertEquals("A freeze event didn't capture a good range of stack samples ("
				+ getStackSamplesTimeline(event) + ")",
				expectedStackCount(freezeDuration), event.getStackTraceSamples().length);
	}

	@Test
	public void testSeeSleepInTheMiddleOfNestedCalls() throws Exception {
		final int freezeDurationFactor = 4;
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS);
		monitoringThread.start();

		// Exceed the threshold after the thread is started in the middle of an event, then end
		// the event and validate that no freeze event was logged.
		sendEvent(SWT.PreEvent);
		// Initially the outer thread is invoking nested events that are responsive.
		for (int i = 0; i < 3; i++) {
			runForCycles(1);
			sendEvent(SWT.PreEvent);
			sendEvent(SWT.PostEvent);
		}

		// Nested events
		for (int i = 0; i < freezeDurationFactor; ++i) {
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

		assertTrue("A freeze event should not be published during an external event dispatch",
				loggedEvents.isEmpty());
	}

	@Test
	public void testConsecutiveSleeps() throws Exception {
		final int freezeDurationFactor = 5;
		monitoringThread = createTestThread(FREEZE_THRESHOLD_MS);
		monitoringThread.start();
		long eventStartTime;
		long eventDuration;

		sendEvent(SWT.PreEvent);
		sendEvent(SWT.PreExternalEventDispatch);
		runForTime(FREEZE_THRESHOLD_MS);
		sendEvent(SWT.PostExternalEventDispatch);
		eventStartTime = timestamp;
		runForCycles(3);

		assertTrue("A freeze event shold not be published during an external event dispatch",
				loggedEvents.isEmpty());

		// Let a long time elapse between the last PostExternalEventDispatch and the next
		// PreExternalEventDispatch.
		runForTime(FREEZE_THRESHOLD_MS * freezeDurationFactor);
		eventDuration = timestamp - eventStartTime;
		sendEvent(SWT.PreExternalEventDispatch);
		sendEvent(SWT.PostExternalEventDispatch);
		sendEvent(SWT.PostEvent);
		runForCycles(3);

		assertEquals("Incorrect number of freeze events was logged", 1, loggedEvents.size());
		UiFreezeEvent event = loggedEvents.get(0);
		assertEquals("A freeze event log has an incorrect start time", eventStartTime,
				event.getStartTimestamp());
		assertEquals("A freeze event's duration is incorrect", eventDuration,
				event.getTotalDuration());
	}
}
