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
 *     Marcus Eng (Google) - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Christoph Läubrich - adjust log call
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import java.lang.management.LockInfo;
import java.lang.management.ThreadInfo;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.monitoring.IUiFreezeEventLogger;
import org.eclipse.ui.monitoring.PreferenceConstants;
import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

/**
 * Writes {@link UiFreezeEvent}s to the Eclipse error log.
 */
public class DefaultUiFreezeEventLogger implements IUiFreezeEventLogger {
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault()); //$NON-NLS-1$
	private final long longEventErrorThresholdMillis;

	private static class StackTrace extends Throwable {
		private static final long serialVersionUID = -2829405667536819137L;

		StackTrace(StackTraceElement[] stackTraceElements) {
			setStackTrace(stackTraceElements);
		}

		@Override
		public String toString() {
			return Messages.DefaultUiFreezeEventLogger_stack_trace_header;
		}
	}

	private static class SeverityMultiStatus extends MultiStatus {
		public SeverityMultiStatus(int severity, String pluginId, String message, Throwable exception) {
			super(pluginId, OK, message, exception);
			setSeverity(severity);
		}
	}

	public DefaultUiFreezeEventLogger(long longEventErrorThresholdMillis) {
		this.longEventErrorThresholdMillis = longEventErrorThresholdMillis;
	}

	/**
	 * Converts the given {@link UiFreezeEvent} into a {@link MultiStatus} and writes it to the log.
	 *
	 * @param event the event that caused the UI thread to freeze
	 */
	@Override
	public void log(UiFreezeEvent event) {
		long lastTimestamp = event.getStartTimestamp();
		String startTime = dateFormat.format(new Date(lastTimestamp).toInstant());

		String template = event.isStillRunning()
				? Messages.DefaultUiFreezeEventLogger_ui_freeze_ongoing_header_2
				: Messages.DefaultUiFreezeEventLogger_ui_freeze_finished_header_2;
		long duration = event.getTotalDuration();
		String format = duration >= 100000 ? "%.0f" : duration >= 10 ? "%.2g" : "%.1g"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String header = NLS.bind(template, String.format(format, duration / 1000.0), startTime);

		StackSample[] stackTraceSamples = event.getStackTraceSamples();
		if (stackTraceSamples.length == 0 && (event.isStarvedAwake() || event.isStarvedAsleep())) {
			String note =
					(event.isStarvedAwake() || event.isStarvedAsleep()) ?
							Messages.DefaultUiFreezeEventLogger_starved_awake_and_asleep :
					event.isStarvedAwake() ?
							Messages.DefaultUiFreezeEventLogger_starved_awake :
							Messages.DefaultUiFreezeEventLogger_starved_asleep;
			header += note;
		}

		int severity = duration >= longEventErrorThresholdMillis ?
				IStatus.ERROR : IStatus.WARNING;
		MultiStatus loggedEvent =
				new SeverityMultiStatus(severity, PreferenceConstants.PLUGIN_ID, header, null);

		for (StackSample sample : stackTraceSamples) {
			double deltaInSeconds = (sample.getTimestamp() - lastTimestamp) / 1000.0;
			ThreadInfo[] threads = sample.getStackTraces();

			// The first thread is guaranteed to be the display thread.
			Throwable stackTrace = new StackTrace(threads[0].getStackTrace());
			String traceText = NLS.bind(
					Messages.DefaultUiFreezeEventLogger_sample_header_2,
					dateFormat.format(Instant.ofEpochMilli(sample.getTimestamp())),
					String.format("%.3f", deltaInSeconds)); //$NON-NLS-1$
			MultiStatus traceStatus = new SeverityMultiStatus(IStatus.INFO,
					PreferenceConstants.PLUGIN_ID,
					String.format("%s\n%s", traceText, createThreadMessage(threads[0])), //$NON-NLS-1$
					stackTrace);
			loggedEvent.add(traceStatus);

			for (int j = 1; j < threads.length; j++) {
				traceStatus.add(createThreadStatus(threads[j]));
			}

			lastTimestamp = sample.getTimestamp();
		}

		ILog.of(getClass()).log(loggedEvent);
	}

	private static IStatus createThreadStatus(ThreadInfo thread) {
		Throwable stackTrace = new StackTrace(thread.getStackTrace());
		StringBuilder threadText = createThreadMessage(thread);
		String lockName = thread.getLockName();
		if (lockName != null && !lockName.isEmpty()) {
			LockInfo lock = thread.getLockInfo();
			String lockOwnerName = thread.getLockOwnerName();
			if (lockOwnerName == null) {
				threadText.append(NLS.bind(
						Messages.DefaultUiFreezeEventLogger_waiting_for_1,
						getClassAndHashCode(lock)));
			} else {
				threadText.append(NLS.bind(
						Messages.DefaultUiFreezeEventLogger_waiting_for_with_lock_owner_3,
						new Object[] { getClassAndHashCode(lock), lockOwnerName,
								thread.getLockOwnerId() }));
			}
		}

		for (LockInfo lockInfo : thread.getLockedSynchronizers()) {
			threadText.append(NLS.bind(
					Messages.DefaultUiFreezeEventLogger_holding_1, getClassAndHashCode(lockInfo)));
		}

		return new Status(IStatus.INFO, PreferenceConstants.PLUGIN_ID, threadText.toString(),
				stackTrace);
	}

	private static StringBuilder createThreadMessage(ThreadInfo thread) {
		String threadDetails = NLS.bind(
				Messages.DefaultUiFreezeEventLogger_thread_details,
				thread.getThreadId(), thread.getThreadState());

		return new StringBuilder(NLS.bind(
				Messages.DefaultUiFreezeEventLogger_thread_header_2,
				thread.getThreadName(), threadDetails));
	}

	private static String getClassAndHashCode(LockInfo info) {
		return String.format("%s@%08x", info.getClassName(), info.getIdentityHashCode()); //$NON-NLS-1$
	}
}
