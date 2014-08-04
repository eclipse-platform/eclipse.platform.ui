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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.monitoring.IUiFreezeEventLogger;
import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SWT long event monitoring thread. Can be used to report freezes on the UI thread.
 */
public class EventLoopMonitorThread extends Thread implements Listener {
	private static final String EXTENSION_ID = "org.eclipse.ui.monitoring.logger"; //$NON-NLS-1$
	private static final String NEW_LINE_AND_BULLET = "\n* "; //$NON-NLS-1$

	/* NOTE: All time-related values in this class are in milliseconds. */
	/**
	 * Helper object for passing preference-based arguments by name to the constructor, making the
	 * code more readable than a large parameter list of ints and bools.
	 */
	public static class Parameters {
		/** Milliseconds after which a long event should get logged */
		public int loggingThreshold;
		/** Milliseconds to wait before collecting the first sample */
		public int samplingThreshold;
		/**
		 * If true, includes call stacks of all threads into the logged message. Otherwise, only the
		 * stack of the watched thread is included.
		 */
		public boolean dumpAllThreads;
		/**
		 * Milliseconds at which stack traces should be sampled. Down-sampling of a long event may
		 * extend the polling delay for that event.
		 */
		public int minimumPollingDelay;
		/** Maximum number of stack samples to log */
		public int loggedTraceCount;
		/** Milliseconds after which a long event should logged as a deadlock */
		public long deadlockDelta;
		/**
		 * If true, log freeze events to the Eclipse error log on the local machine.
		 */
		public boolean logLocally;
		/**
		 * Contains the list of fully qualified methods to filter out.
		 */
		public String filterTraces;


		/**
		 * Checks if parameters for plug-in are valid before startup.
		 *
		 * @throws IllegalArgumentException if the parameter values are invalid or inconsistent.
		 */
		public void checkParameters() throws IllegalArgumentException {
			StringBuilder error = new StringBuilder();
			if (!(this.loggingThreshold > 0)) {
				error.append(NEW_LINE_AND_BULLET + NLS.bind(
						Messages.EventLoopMonitorThread_logging_threshold_error_1, this.loggingThreshold));
			}
			if (!(this.minimumPollingDelay > 0)) {
				error.append(NEW_LINE_AND_BULLET + NLS.bind(
						Messages.EventLoopMonitorThread_sample_interval_error_1, this.minimumPollingDelay));
			}
			if (!(this.loggedTraceCount > 0)) {
				error.append(NEW_LINE_AND_BULLET + NLS.bind(
						Messages.EventLoopMonitorThread_max_log_count_error_1, this.loggedTraceCount));
			}
			if (!(this.samplingThreshold > 0)) {
				error.append(NEW_LINE_AND_BULLET + NLS.bind(
						Messages.EventLoopMonitorThread_capture_threshold_error_1, this.samplingThreshold));
			}
			if (this.loggingThreshold < this.samplingThreshold) {
				error.append(NEW_LINE_AND_BULLET + Messages.EventLoopMonitorThread_invalid_threshold_error);
			}
			if (!(this.deadlockDelta > 0)) {
				error.append(NEW_LINE_AND_BULLET
						+ NLS.bind(Messages.EventLoopMonitorThread_deadlock_error_1, this.deadlockDelta));
			}

			String errorString = error.toString();
			if (!errorString.isEmpty()) {
				throw new IllegalArgumentException(
						Messages.EventLoopMonitorThread_invalid_argument_error + errorString);
			}
		}
	}

	/*
	 * Tracks when the current event was started, or if the event has nested {@link Event#sendEvent}
	 * calls, then the time when the most recent nested call returns and the current event is resumed.
	 *
	 * Accessed by both the UI and monitoring threads. Updated by the UI thread and read by the
	 * polling thread. Changing this in the UI thread causes the polling thread to reset its stalled
	 * event state. The UI thread sets this value to zero to indicate a sleep state and to a positive
	 * value to represent a dispatched state. (Using zero as an invalid event start time will be wrong
	 * for a 1 millisecond window when the 32-bit system clock rolls over in 2038, but we can live
	 * with skipping any events that fall in that window).
	 */
	private volatile long eventStartOrResumeTime;

	// Accessed by both the UI and monitoring threads.
	private final int loggingThreshold;
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private final AtomicReference<LongEventInfo> publishEvent =
			new AtomicReference<LongEventInfo>(null);

	// Accessed only on the polling thread.
	private ArrayList<IUiFreezeEventLogger> externalLoggers;
	private final DefaultUiFreezeEventLogger defaultLogger;
	private final Tracer localTraceLog;
	private final Display display;
	private final FilterHandler filterHandler;
	private final long samplingThreshold;
	private final long minimumPollingDelay;
	private final int maxTraceCount;
	private final int loggedTraceCount;
	private final long deadlockDelta;
	private final long uiThreadId;
	private final Object sleepMonitor;
	private final boolean dumpAllThreads;
	private final boolean logLocally;

	/**
	 * A helper class to track and report potential deadlocks.
	 */
	private class DeadlockTracker {
		private boolean haveAlreadyLoggedPossibleDeadlock;

		// The last time a state transition between events or sleep/wake was seen. May be set to zero
		// to indicate that deadlocks should not be tracked.
		private long lastActive;

		/**
		 * Logs a possible deadlock to the remote log. {@code lastActive} is zero if the interval is for
		 * a sleep, in which case we don't log a deadlock.
		 *
		 * @param currTime the current time
		 * @param stackTraces stack traces for the currently stalled event
		 * @param numStacks the number of valid traces for the currently stalled event
		 */
		public void logPossibleDeadlock(long currTime, StackSample[] stackTraces, int numStacks) {
			long totalDuration = currTime - lastActive;

			if (!haveAlreadyLoggedPossibleDeadlock && lastActive > 0 && totalDuration > deadlockDelta) {
				logEvent(new UiFreezeEvent(lastActive, totalDuration, stackTraces, numStacks, true));
				haveAlreadyLoggedPossibleDeadlock = true;
				Arrays.fill(stackTraces, null);
			}
		}

		/**
		 * Resets the deadlock tracker's state.
		 */
		public void reset(long lastActive) {
			this.lastActive = lastActive;
			haveAlreadyLoggedPossibleDeadlock = false;
		}
	}

	/**
	 * Initializes the static state of the monitoring thread.
	 *
	 * @param args parameters derived from preferences
	 * @throws IllegalArgumentException if monitoring thread cannot be initialized due to an error.
	 */
	public EventLoopMonitorThread(Parameters args) throws IllegalArgumentException {
		super("Event Loop Monitor"); //$NON-NLS-1$

		Assert.isNotNull(args);

		args.checkParameters();

		setDaemon(true);
		setPriority(NORM_PRIORITY + 1);
		this.display = getDisplay();
		this.uiThreadId = this.display.getThread().getId();
		this.filterHandler = new FilterHandler(args.filterTraces);
		this.samplingThreshold = args.samplingThreshold;
		this.minimumPollingDelay = args.minimumPollingDelay;
		this.loggedTraceCount = args.loggedTraceCount;
		this.maxTraceCount = 2 * (args.loggedTraceCount - 1);
		this.loggingThreshold = args.loggingThreshold;
		this.dumpAllThreads = args.dumpAllThreads;
		this.localTraceLog = getTracer();
		this.deadlockDelta = args.deadlockDelta;
		this.logLocally = args.logLocally;
		this.sleepMonitor = new Object();
		defaultLogger = new DefaultUiFreezeEventLogger();

		loadLoggerExtensions();

		if (!logLocally && externalLoggers.isEmpty()) {
			MonitoringPlugin.logWarning(Messages.EventLoopMonitorThread_logging_disabled_error);
		}
	}

	/**
	 * Shuts down the monitoring thread. Must be called on the display thread.
	 */
	public void shutdown() throws SWTException {
		cancelled.set(true);
		if (!display.isDisposed()) {
			display.removeListener(SWT.PreEvent, this);
			display.removeListener(SWT.PostEvent, this);
			display.removeListener(SWT.Sleep, this);
			display.removeListener(SWT.Wakeup, this);
		}
		wakeUp();
	}

	@Override
	public void handleEvent(Event event) {
		/*
		 * Freeze monitoring involves seeing long intervals between BeginEvent/EndEvent messages,
		 * regardless of the level of event nesting. For example:
		 * 1) Log if a top-level or nested dispatch takes too long (interval is between BeginEvent and
		 *    EndEvent).
		 * 2) Log if preparation before popping up a dialog takes too long (interval is between two
		 *    BeginEvent messages).
		 * 3) Log if processing after dismissing a dialog takes too long (interval is between two
		 *    EndEvent messages).
		 * 4) Log if there is a long delay between nested calls (interval is between EndEvent and
		 *    BeginEvent). This could happen after a dialog is dismissed, does too much processing on
		 *    the UI thread, and then pops up a notification dialog.
		 * 5) Don't log for long delays between top-level events (interval is between EndEvent and
		 *    BeginEvent at the top level), which should involve sleeping.
		 *
		 * Calls to Display.sleep() make the UI responsive, whether or not events are actually
		 * dispatched, so items 1-4 above assume that there are no intervening calls to sleep() between
		 * the event transitions. Treating the BeginSleep event as an event transition lets us
		 * accurately capture true freeze intervals.
		 *
		 * Correct management of BeginSleep/EndSleep events allow us to handle items 4 and 5 above
		 * since we can tell if a long delay between an EndEvent and a BeginEvent are due to an idle
		 * state (in Display.sleep()) or a UI freeze.
		 *
		 * Since an idle system can potentially sleep for a long time, we need to avoid logging long
		 * delays that are due to sleeps. The eventStartOrResumeTime variable is set to zero
		 * when the thread is sleeping so that deadlock logging can be avoided for this case.
		 */
		switch (event.type) {
		case SWT.PreEvent:
			beginEvent();
			break;
		case SWT.PostEvent:
			endEvent();
			break;
		case SWT.Sleep:
			beginSleep();
			break;
		case SWT.Wakeup:
			endSleep();
			break;
		default:
		}
	}

	// Called on the UI thread!
	// VisibleForTesting
	public void beginEvent() {
		// Log a long interval, not entering sleep
		handleEventTransition(true, false);
	}

	// Called on the UI thread!
	// VisibleForTesting
	public void endEvent() {
		// Log a long interval, not entering sleep
		handleEventTransition(true, false);
	}

	// Called on the UI thread!
	// VisibleForTesting
	public void beginSleep() {
		// Log a long interval, entering sleep
		handleEventTransition(true, true);
	}

	// Called on the UI thread!
	// VisibleForTesting
	public void endSleep() {
		// Don't log the long sleep interval, not entering sleep
		handleEventTransition(false, false);
	}

	// Called on the UI thread!
	private void handleEventTransition(boolean attemptToLogLongDelay, boolean isEnteringSleep) {
		/*
		 * On transition between events or sleeping/wake up, we need to reset the delay tracking state
		 * and possibly publish a long delay message. Updating eventStartOrResumeTime causes the polling
		 * thread to reset its stack traces, so it should always be changed *after* the event is
		 * published. The indeterminacy of threading may cause the polling thread to see both changes or
		 * only the (first) publishEvent change, but the only difference is a small window where if an
		 * additional stack trace was scheduled to be sampled, a bogus stack trace sample will be
		 * appended to the end of the samples. Analysis code needs to be aware that the last sample may
		 * not be relevant to the issue which caused the freeze.
		 */
		long currTime = getTimestamp();
		if (attemptToLogLongDelay) {
			long startTime = eventStartOrResumeTime;
			if (startTime != 0) {
				int duration = (int) (currTime - startTime);
				if (duration >= loggingThreshold) {
					LongEventInfo info = new LongEventInfo(startTime, duration);
					publishEvent.set(info);
					wakeUp();
				}
			}
		}
		// Using zero as an invalid event time will be wrong for a 1 millisecond window when the system
		// clock rolls over in 2038, but we can live with that.
		eventStartOrResumeTime = !isEnteringSleep ? currTime : 0;
	}

	@Override
	public void run() {
		/*
		 * If this event loop starts in the middle of a UI freeze, it will succeed in capturing the
		 * portion of that UI freeze that it sees.
		 *
		 * Our timer resolution is, at best, 1 millisecond so we can never try to catch events of a
		 * duration less than that.
		 */
		boolean resetStalledEventState = true;

		DeadlockTracker deadlockTracker = new DeadlockTracker();

		final long pollingNyquistDelay = minimumPollingDelay / 2;
		long pollingDelay = 0; // immediately updated by resetStalledEventState
		long grabStackTraceAt = 0; // immediately updated by resetStalledEventState
		long lastEventStartOrResumeTime = 0; // immediately updated by resetStalledEventState

		StackSample[] stackTraces = new StackSample[maxTraceCount];
		int numStacks = 0;

		ThreadMXBean jvmThreadManager = ManagementFactory.getThreadMXBean();
		boolean dumpLockedMonitors = jvmThreadManager.isObjectMonitorUsageSupported();
		boolean dumpLockedSynchronizers = jvmThreadManager.isSynchronizerUsageSupported();
		if (dumpAllThreads && jvmThreadManager.isThreadContentionMonitoringSupported()) {
			jvmThreadManager.setThreadContentionMonitoringEnabled(true);
		}

		// Register for events
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				registerDisplayListeners();
			}
		});

		long currTime = getTimestamp();

		while (!cancelled.get()) {
			long sleepFor;
			if (resetStalledEventState) {
				long eventTime = eventStartOrResumeTime;
				deadlockTracker.reset(eventTime);
				if (eventTime == 0) {
					eventTime = currTime;
				}
				grabStackTraceAt = eventTime + samplingThreshold;
				numStacks = 0;
				pollingDelay = minimumPollingDelay;
				sleepFor = pollingNyquistDelay;
				resetStalledEventState = false;
			} else if (lastEventStartOrResumeTime == 0) {
				sleepFor = pollingNyquistDelay;
			} else {
				sleepFor = Math.min(pollingNyquistDelay, Math.max(1, grabStackTraceAt - currTime));
			}

			// This is the top of the polling loop.
			long sleepAt = getTimestamp();

			/*
			 * Check for starvation outside of sleeping. If we sleep or process much longer than expected
			 * (e.g. > threshold/2 longer), then the polling thread has been starved and it's very likely
			 * that the UI thread has been as well. Starvation freezes do not have useful information, so
			 * don't log them.
			 */
			long awakeDuration = currTime - sleepAt;
			boolean starvedAwake = awakeDuration > (sleepFor + loggingThreshold / 2);
			sleepForMillis(sleepFor);
			currTime = getTimestamp();
			long currEventStartOrResumeTime = eventStartOrResumeTime;
			long sleepDuration = currTime - sleepAt;
			boolean starvedSleep = sleepDuration > (sleepFor + loggingThreshold / 2);
			boolean starved = starvedSleep || starvedAwake;

			/*
			 * If after sleeping we see that a new event has been dispatched, mark that we should update
			 * the stalled event state. Otherwise, check if we have surpassed our threshold and collect a
			 * stack trace.
			 */
			if (lastEventStartOrResumeTime != currEventStartOrResumeTime || starved) {
				resetStalledEventState = true;
				if (localTraceLog != null && starved) {
					if (starvedAwake) {
						localTraceLog.trace(String.format(
								"Starvation detected! Polling loop took a significant amount of threshold: %dms", //$NON-NLS-1$
								awakeDuration));
					}

					if (starvedSleep) {
						localTraceLog.trace(String.format(
								"Starvation detected! Expected a sleep of %dms but actually slept for %dms", //$NON-NLS-1$
								sleepFor, sleepDuration));
					}
				}
			} else if (lastEventStartOrResumeTime != 0) {
				deadlockTracker.logPossibleDeadlock(currTime, stackTraces, numStacks);

				// Collect additional stack traces if enough time has elapsed.
				if (maxTraceCount > 0 && currTime - grabStackTraceAt > 0) {
					if (numStacks == maxTraceCount) {
						numStacks = maxTraceCount / 2;
						decimate(stackTraces, maxTraceCount, numStacks, 0);
						pollingDelay *= 2;
					}

					try {
						ThreadInfo[] rawThreadStacks = dumpAllThreads
							? jvmThreadManager.dumpAllThreads(dumpLockedMonitors, dumpLockedSynchronizers)
									: new ThreadInfo[] {
									jvmThreadManager.getThreadInfo(uiThreadId, Integer.MAX_VALUE)
							};

						ThreadInfo[] threadStacks = rawThreadStacks;
						// If all threads were dumped, we remove the info for the monitoring thread.
						if (dumpAllThreads) {
							int index = 0;
							threadStacks = new ThreadInfo[rawThreadStacks.length - 1];

							for (int i = 0; i < rawThreadStacks.length; i++) {
								ThreadInfo currentThread = rawThreadStacks[i];

								// Skip if stack trace is from the current (UI monitoring) thread.
								if (!isCurrentThread(currentThread.getThreadId())) {
									if (currentThread.getThreadId() == uiThreadId && i > 0) {
										// Swap main thread to first slot in array if it is not already.
										currentThread = threadStacks[0];
										threadStacks[0] = rawThreadStacks[i];
									}
									threadStacks[index++] = currentThread;
								}
							}
						}

						stackTraces[numStacks++] = new StackSample(getTimestamp(), threadStacks);
						grabStackTraceAt += pollingDelay;
					} catch (SWTException e) {
						// Display is disposed so start terminating
						cancelled.set(true);
						resetStalledEventState = true;
					}
				}
			}

			// If a stalled event has finished, publish it and mark that the information should be reset.
			LongEventInfo eventSnapshot = publishEvent.getAndSet(null);
			if (starved || eventSnapshot != null) {
				if (eventSnapshot != null) {
					// Trim last stack trace if it is too close to the end of the event.
					int trimLast = 0;
					if (numStacks - 1 > loggedTraceCount) {
						long eventEnd = eventSnapshot.start + eventSnapshot.duration;
						if (eventEnd - stackTraces[numStacks - 1].getTimestamp() < minimumPollingDelay) {
							trimLast = 1;
						}
					}

					if (numStacks > loggedTraceCount) {
						decimate(stackTraces, numStacks, loggedTraceCount, trimLast);
						numStacks = loggedTraceCount;
					}

					logEvent(new UiFreezeEvent(eventSnapshot.start, eventSnapshot.duration, stackTraces,
							numStacks, false));
				}

				resetStalledEventState = true;
				Arrays.fill(stackTraces, null);
			}

			lastEventStartOrResumeTime = currEventStartOrResumeTime;
		}
	}

	private static Display getDisplay() throws IllegalArgumentException {
		IWorkbench workbench = MonitoringPlugin.getDefault().getWorkbench();
		if (workbench == null) {
			throw new IllegalArgumentException(Messages.EventLoopMonitorThread_workbench_was_null);
		}

		Display display = workbench.getDisplay();
		if (display == null) {
			throw new IllegalArgumentException(Messages.EventLoopMonitorThread_display_was_null);
		}

		return display;
	}

	// VisibleForTesting
	protected long getTimestamp() {
		return System.currentTimeMillis();
	}

	// VisibleForTesting
	protected void sleepForMillis(long milliseconds) {
		if (milliseconds > 0) {
			try {
				synchronized (sleepMonitor) {
					// Spurious wake ups are OK; they will just burn a few extra CPU cycles.
					sleepMonitor.wait(milliseconds);
				}
			} catch (InterruptedException e) {
				// Wake up.
			}
		}
	}

	private Tracer getTracer() {
		return MonitoringPlugin.getTracer();
	}

	private void loadLoggerExtensions() {
		externalLoggers = new ArrayList<IUiFreezeEventLogger>();
		IConfigurationElement[] configElements =
				Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_ID);

		for (IConfigurationElement element : configElements) {
			try {
				Object object = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (object instanceof IUiFreezeEventLogger) {
					externalLoggers.add((IUiFreezeEventLogger) object);
				} else {
					MonitoringPlugin.logWarning(String.format(
							Messages.EventLoopMonitorThread_invalid_logger_type_error_4,
							object.getClass().getName(), IUiFreezeEventLogger.class.getClass().getSimpleName(),
							EXTENSION_ID, element.getContributor().getName()));
				}
			} catch (CoreException e) {
				MonitoringPlugin.logError(e.getMessage(), e);
			}
		}
	}

	/**
	 * Returns {@code true} if given thread is the same as the current thread.
	 */
	private static boolean isCurrentThread(long threadId) {
		return threadId == Thread.currentThread().getId();
	}

	private void registerDisplayListeners() {
		display.addListener(SWT.PreEvent, EventLoopMonitorThread.this);
		display.addListener(SWT.PostEvent, EventLoopMonitorThread.this);
		display.addListener(SWT.Sleep, EventLoopMonitorThread.this);
		display.addListener(SWT.Wakeup, EventLoopMonitorThread.this);
	}

	private static void decimate(Object[] list, int fromSize, int toSize, int trimTail) {
		fromSize -= trimTail;
		for (int i = 1; i < toSize; ++i) {
			int j = (i * fromSize + toSize / 2) / toSize; // == floor(i*(from/to)+0.5) == round(i*from/to)
			list[i] = list[j];
		}
	}

	private void wakeUp() {
		synchronized (sleepMonitor) {
			sleepMonitor.notify();
		}
	}

	/**
	 * Writes the snapshot and stack captures to the workspace log.
	 */
	private void logEvent(UiFreezeEvent event) {
		if (!filterHandler.shouldLogEvent(event, uiThreadId)) {
			return;
		}

		if (logLocally) {
			defaultLogger.log(event);
		}

		for (int i = 0; i < externalLoggers.size(); i++) {
			IUiFreezeEventLogger currentLogger = externalLoggers.get(i);
			try {
				currentLogger.log(event);
			} catch (Throwable t) {
				externalLoggers.remove(i);
				i--;
				MonitoringPlugin.logError(NLS.bind(
						Messages.EventLoopMonitorThread_external_exception_error_1,
						currentLogger.getClass().getName()), t);
			}
		}
	}
}
