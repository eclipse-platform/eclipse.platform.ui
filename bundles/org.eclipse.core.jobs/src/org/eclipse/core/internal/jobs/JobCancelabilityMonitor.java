/*******************************************************************************
 * Copyright (c) 2016 Eclipse Foundation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mikaël Barbero (Eclipse Foundation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * A progress monitor wrapper that computes the number of calls to
 * {@link #isCanceled()} and the maximum time interval without calls to
 * {@link #isCanceled()}.
 * <p>
 * Clients are expected to call {@link #aboutToStart()} and
 * {@link #hasStopped()} shortly before and after the job execution.
 * <p>
 * After {@link #hasStopped()} has been called, client can call
 * {@link #createCancelabilityStatus()} and retrieve an {@link IStatus} stating
 * whether the Job follows best practices regarding cancelability. Best
 * practices threshold and report details can be configured through an
 * {@link Options} given at instantiation time.
 */
public final class JobCancelabilityMonitor extends ProgressMonitorWrapper {
	/**
	 * For conversion in {@link #nanosToString(long)}
	 */
	private static final long _1_SECOND_IN_NANOS = TimeUnit.SECONDS.toNanos(1);

	/**
	 * Specific error code to help the Automatic Error Reporting Initiative
	 * (AERI) identifying cancelability issues.
	 */
	private static final int CANCELABILITY_ERROR_CODE = 8;

	/**
	 * Will be incremented every time {@link #isCanceled()} is called.
	 */
	private int isCanceledHitCount = 0;

	/**
	 * Will be set to {@link System#nanoTime()} when {@link #aboutToStart()}
	 * will be called.
	 */
	private long startNano = -1;

	/**
	 * Will be set to "{@link System#nanoTime()} - {@link #startNano}" when
	 * hasStopped(); will be called.
	 */
	private long elapsedNano = -1;

	/**
	 * Keep the {@link System#nanoTime()} value of previous hit to
	 * {@link #isCanceled()}. Will be initialized in {@link #aboutToStart()}.
	 */
	private long lastHit = -1;

	/**
	 * At every call to {@link #isCanceled()}, will be set to
	 * {@code Math.max(maxTimeBetweenTwoCancelationCheck, System.nanoTime() - lastHit)}
	 * .
	 */
	private long maxTimeBetweenTwoCancelationCheck = -1;

	/**
	 * List of stack traces that will be computed during calls to some
	 * {@link IProgressMonitor} methods
	 */
	private List<StackTraceSample> stackTraces;

	/**
	 * Temporary holder of the last captured stack trace that may be added to
	 * {@link #stackTraces} if {@link Options#maxStackSamples()} is not reached
	 * or if it longer than one of the already recorded sample.
	 */
	private StackTraceElement[] lastCapturedStackTrace;

	/**
	 * Configurable threshold and options for the result of
	 * {@link #createCancelabilityStatus()}.
	 */
	private final Options options;

	/**
	 * The job that report progress to this progress monitor.
	 */
	private final InternalJob job;

	JobCancelabilityMonitor(InternalJob job, Options options) {
		super(job.getProgressMonitor());
		this.job = job;
		this.options = options;
		this.stackTraces = new ArrayList<>(options.maxStackSamples() + 1);
	}

	/**
	 * Must be called before the {@link #job} starts.
	 *
	 * @return this to simplify calling code.
	 */
	IProgressMonitor aboutToStart() {
		lastCapturedStackTrace = captureStackTrace();
		startNano = System.nanoTime();
		lastHit = startNano;
		return this;
	}

	/**
	 * Must be called after the {@link #job} ends.
	 */
	void hasStopped() {
		elapsedNano = System.nanoTime() - startNano;
	}

	/**
	 * Captures the current thread stack trace and removes the top 3 frames to
	 * avoid displaying the monitoring related frames in the log.
	 */
	private StackTraceElement[] captureStackTrace() {
		final StackTraceElement[] ret;
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (stackTrace.length > 3) {
			ret = Arrays.copyOfRange(stackTrace, 3, stackTrace.length);
		} else {
			ret = stackTrace;
		}
		return ret;
	}

	private static class StackTraceSample {
		final long nanoBetweenStackTraces;
		final StackTraceElement[] firstSte;
		final StackTraceElement[] secondSte;

		public StackTraceSample(long nanoBetweenStackTrace, StackTraceElement[] firstSte,
				StackTraceElement[] secondSte) {
			nanoBetweenStackTraces = nanoBetweenStackTrace;
			this.firstSte = firstSte;
			this.secondSte = secondSte;
		}
	}

	@Override
	public boolean isCanceled() {
		long elapsedSinceLastHit = System.nanoTime() - lastHit;
		lastHit = System.nanoTime();
		maxTimeBetweenTwoCancelationCheck = Math.max(maxTimeBetweenTwoCancelationCheck, elapsedSinceLastHit);
		lastCapturedStackTrace = storeStackTraceSample(elapsedSinceLastHit, captureStackTrace());
		isCanceledHitCount++;
		return super.isCanceled();
	}

	private StackTraceElement[] storeStackTraceSample(long elapsedSinceLastHit, StackTraceElement[] currentStackTrace) {
		if (elapsedSinceLastHit >= options.warningThreshold()) {
			if (stackTraces.size() >= options.maxStackSamples()) {
				int shortestStackTraceIdx = findShortestStackTraceSample(elapsedSinceLastHit);
				if (shortestStackTraceIdx >= 0) {
					stackTraces.set(shortestStackTraceIdx,
							new StackTraceSample(elapsedSinceLastHit, lastCapturedStackTrace, currentStackTrace));
				}
			} else {
				stackTraces.add(new StackTraceSample(elapsedSinceLastHit, lastCapturedStackTrace, currentStackTrace));
			}
		}
		return currentStackTrace;
	}

	/**
	 * Returns the index of the {@link StackTraceSample} with the shortest
	 * {@link StackTraceSample#nanoBetweenStackTraces} that is shorter than the
	 * given {@code shorterThan} value.
	 *
	 * @param shorterThanNanos
	 *            threshold above which
	 *            {@link StackTraceSample#nanoBetweenStackTraces} will not be
	 *            considered during the search.
	 * @return the index of the {@link StackTraceSample} with the shortest
	 *         nanoBetweenStackTraces that is shorter than the given
	 *         {@code shorterThan} value, or -1 if there is no such value.
	 */
	private int findShortestStackTraceSample(long shorterThanNanos) {
		long minValue = shorterThanNanos;
		int shortest = -1;
		for (int i = 0; i < stackTraces.size(); i++) {
			final StackTraceSample stackTraceSample = stackTraces.get(i);
			if (stackTraceSample.nanoBetweenStackTraces < minValue) {
				shortest = i;
				minValue = stackTraceSample.nanoBetweenStackTraces;
			}
		}
		return shortest;
	}

	IStatus createCancelabilityStatus() {
		IStatus ret;

		if (isCanceledHitCount > 0) {
			ret = createCancelabilityStatus(severityForElapsedTime(maxTimeBetweenTwoCancelationCheck),
					NLS.bind(
							JobMessages.cancelability_monitor_waitedTooLong,
							new Object[] { job.getName(), nanosToString(maxTimeBetweenTwoCancelationCheck),
									isCanceledHitCount,
									nanosToString(elapsedNano) }));
		} else {
			ret = createCancelabilityStatus(severityForElapsedTime(elapsedNano),
					NLS.bind(JobMessages.cancelability_monitor_noCancelationCheck,
							new Object[] { job.getName(), isCanceledHitCount, nanosToString(elapsedNano) }));
		}

		return ret;
	}

	private int severityForElapsedTime(long nanoTime) {
		final int severity;
		if (job.isUser() && isCanceledHitCount == 0 && options.alwaysReportNonCancelableUserJobAsError()) {
			// even a short user job should check for cancelation
			severity = IStatus.ERROR;
		} else if (nanoTime >= options.errorThreshold()) {
			severity = IStatus.ERROR;
		} else if (nanoTime >= options.warningThreshold()) {
			severity = IStatus.WARNING;
		} else {
			severity = IStatus.OK;
		}
		return severity;
	}

	private IStatus createCancelabilityStatus(int severity, String msg) {
		IStatus ret;
		if (severity > IStatus.OK) {
			final MultiStatus ms = new MultiStatus(JobManager.PI_JOBS, CANCELABILITY_ERROR_CODE, msg, null);
			// Sort stack traces samples by elapsed time
			Collections.sort(stackTraces, new Comparator<StackTraceSample>() {
				@Override
				public int compare(StackTraceSample s1, StackTraceSample s2) {
					return (int) (s1.nanoBetweenStackTraces - s2.nanoBetweenStackTraces);
				}
			});
			for (StackTraceSample sts : stackTraces) {
				ms.add(createStatusFromStackTraceSample(sts));
			}
			ret = ms;
		} else {
			ret = Status.OK_STATUS;
		}
		return ret;
	}

	private IStatus createStatusFromStackTraceSample(StackTraceSample stackTraceSample) {
		MultiStatus ms = new MultiStatus(JobManager.PI_JOBS, CANCELABILITY_ERROR_CODE,
				NLS.bind(JobMessages.cancelability_monitor_sampledStackTraces,
						nanosToString(stackTraceSample.nanoBetweenStackTraces)),
				null);
		int severity = severityForElapsedTime(stackTraceSample.nanoBetweenStackTraces);
		ms.add(createStatusFromStackTrace(severity, JobMessages.cancelability_monitor_secondStackTrace,
				stackTraceSample.secondSte));
		ms.add(createStatusFromStackTrace(severity, JobMessages.cancelability_monitor_initialStackTrace,
				stackTraceSample.firstSte));
		return ms;
	}

	private static IStatus createStatusFromStackTrace(int severity, String msg, StackTraceElement[] stackTrace) {
		return new Status(severity, JobManager.PI_JOBS, msg, createThrowableFromStackTrace(stackTrace, msg));
	}

	private static Throwable createThrowableFromStackTrace(StackTraceElement[] stackTrace, String msg) {
		Throwable throwable = new Throwable(msg);
		throwable.setStackTrace(stackTrace);
		return throwable;
	}

	private static String nanosToString(long nanos) {
		double value = (double) nanos / _1_SECOND_IN_NANOS;
		final String format = nanos >= TimeUnit.SECONDS.toNanos(100) ? "%.0f %s" //$NON-NLS-1$
				: nanos >= TimeUnit.MILLISECONDS.toNanos(10) ? "%.2g %s" : "%.1g %s"; //$NON-NLS-1$ //$NON-NLS-2$
		return String.format(format, value, JobMessages.cancelability_monitor_abbrevUnitSeconds);
	}

	public static interface Options {
		boolean enabled();

		long errorThreshold();

		long warningThreshold();

		int maxStackSamples();

		boolean alwaysReportNonCancelableUserJobAsError();
	}

	/**
	 * Static inactive singleton that will be used when no service has been
	 * registered.
	 */
	static final Options DEFAULT_OPTIONS = new BasicOptionsImpl();
	static {
		((BasicOptionsImpl) DEFAULT_OPTIONS).setEnabled(false);
	}

	public static class BasicOptionsImpl implements Options {
		private boolean enabled;
		private long errorThreshold;
		private long warningThreshold;
		private int maxStackSamples;
		private boolean alwaysReportNonCancelableUserJobAsError;

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public void setErrorThreshold(long errorThreshold) {
			this.errorThreshold = errorThreshold;
		}

		public void setWarningThreshold(long warningThreshold) {
			this.warningThreshold = warningThreshold;
		}

		public void setMaxStackSamples(int maxStackSamples) {
			this.maxStackSamples = maxStackSamples;
		}

		public void setAlwaysReportNonCancelableUserJobAsError(boolean alwaysReportNonCancelableUserJobAsError) {
			this.alwaysReportNonCancelableUserJobAsError = alwaysReportNonCancelableUserJobAsError;
		}

		@Override
		public boolean enabled() {
			return enabled;
		}

		@Override
		public long errorThreshold() {
			return errorThreshold;
		}

		@Override
		public long warningThreshold() {
			return warningThreshold;
		}

		@Override
		public int maxStackSamples() {
			return maxStackSamples;
		}

		@Override
		public boolean alwaysReportNonCancelableUserJobAsError() {
			return alwaysReportNonCancelableUserJobAsError;
		}
	}
}