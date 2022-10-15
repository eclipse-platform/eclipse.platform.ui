/*******************************************************************************
 * Copyright (c) 2022 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.concurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import org.junit.After;
import org.junit.Test;

/**
 * Regression test for
 * https://github.com/eclipse-platform/eclipse.platform/issues/227.
 *
 * Depending on the time spent in sleep() inside
 * {@link #waitForJobs(long, long, Object)}, the test might pass, fail or hang
 * forever.
 *
 * Reverting
 * https://github.com/eclipse-platform/eclipse.platform/commit/fc49c4ffaf8a6c07b58e437c62125203f80028f2
 * fixes the test.
 */
public class TestGitHubBug227 {

	AtomicLong runTime = new AtomicLong();
	AtomicLong runCount = new AtomicLong();
	AtomicLong waitTime = new AtomicLong();
	AtomicLong scheduleTime = new AtomicLong();
	int executions = 300;
	int jobsCount = 5;
	int reschedule;

	@Test
	public void testRescheduleOverhead() throws Exception {
		// baseline measurement with scheduling jobs once
		reschedule = 1;
		long timeSchedule = doTest();
		long runsSchedule = resetTimes();

		// measurement with re-scheduling jobs few times
		reschedule = 5;
		long timeReschedule = doTest();
		long runsReschedule = resetTimes();

		assertEquals("Scheduled number is unexpected", runsSchedule, executions * jobsCount);
		assertTrue("With re-schedule should run more as scheduled, observed: " + runsReschedule + ", expected at least "
				+ executions, runsReschedule >= executions * jobsCount);
		long runsDiff = (10 * runsReschedule / runsSchedule);
		long timesDiff = (10 * timeReschedule / timeSchedule);
		long overhead = timesDiff / runsDiff;
		System.out.println("execDiff * 10: " + runsDiff);
		System.out.println("timesDiff * 10: " + timesDiff);
		System.out.println("overhead: " + overhead);
		assertTrue("Time difference " + timesDiff / 10 + "  too high, should be less than runs difference "
				+ runsDiff / 10 + ", overhead " + overhead, overhead <= 1);
	}

	@After
	public void tearDown() {
		Job.getJobManager().cancel(TestGitHubBug227.class);
	}

	void printSummary(long executionTime) {
		System.out.println("Scheduled " + jobsCount + " jobs each " + (executions * reschedule) + " times ("
				+ (executions * (reschedule - 1)) + " times rescheduled)");
		System.out.println("Expect at least " + (jobsCount * executions) + " real executions");
		System.out.println("Really run " + runCount + " times");
		System.out.println("Spent " + scheduleTime + " ms to schedule");
		System.out.println("Spent " + runTime + " ms in a job");
		System.out.println("Spent " + waitTime + " ms to wait for jobs");
		System.out.println("Spent " + executionTime + " ms overall");
	}

	long resetTimes() {
		long runs = runCount.get();
		runTime = new AtomicLong();
		runCount = new AtomicLong();
		waitTime = new AtomicLong();
		scheduleTime = new AtomicLong();
		return runs;
	}

	private long doTest() {
		long start = currentTimeMs();
		List<UIJob> jobs = new ArrayList<>();
		for (int i = 0; i < jobsCount; i++) {
			jobs.add(createUiJob(i));
		}
		for (int i = 0; i < executions; i++) {
			long scheduleStart = currentTimeMs();
			for (int j = 0; j < reschedule; j++) {
				jobs.forEach(job -> job.schedule());
			}
			scheduleTime.addAndGet(currentTimeMs() - scheduleStart);

			long waitStart = currentTimeMs();
			boolean timeoutHit = waitForJobs(0, 10000, TestGitHubBug227.class);
			if (timeoutHit) {
				Job.getJobManager().cancel(TestGitHubBug227.class);
				fail("Tests run into timeout after " + 10000 + " ms");
			}
			waitTime.addAndGet(currentTimeMs() - waitStart);
		}
		long stop = currentTimeMs();
		long executionTime = stop - start;
		printSummary(executionTime);
		return executionTime;
	}

	private UIJob createUiJob(int i) {
		UIJob job = new UIJob("Worker in UI " + i) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				long start = currentTimeMs();
				// Just do something trivial on UI
//				Display.getDefault().getShells()[0].redraw();
				runTime.addAndGet(currentTimeMs() - start);
				runCount.incrementAndGet();
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return TestGitHubBug227.class == family;
			}
		};
		job.setUser(false);
		job.setSystem(true);
		return job;
	}

	public static boolean waitForJobs(long minTimeMs, long maxTimeMs, Object family) {
		wakeUpSleepingJobs(family);
		final long start = currentTimeMs();
		while (currentTimeMs() - start < minTimeMs) {
			processUIEvents(0);
		}
		while (!Job.getJobManager().isIdle()) {
			List<Job> jobs = getRunningOrWaitingJobs(family);
			if (jobs.isEmpty()) {
				break;
			}
			if (currentTimeMs() - start >= maxTimeMs) {
				System.out.println(dumpRunningOrWaitingJobs(jobs));
				return true;
			}
			try {
				// XXX This time determines if the test will pass, fail or hang, but it is very
				// unstable.
				// Removing sleep() here help to "fix" the test sometimes
				// Increasing time to 10 ms shows extreme slowdown or hang after
				// https://github.com/eclipse-platform/eclipse.platform/commit/fc49c4ffaf8a6c07b58e437c62125203f80028f2
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			wakeUpSleepingJobs(family);
			processUIEvents(0);
		}
		return false;
	}

	private static void wakeUpSleepingJobs(Object family) {
		for (Job job : getSleepingJobs(family)) {
			job.wakeUp();
		}
	}

	private static String dumpRunningOrWaitingJobs(List<Job> jobs) {
		if (jobs.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		for (Job job : jobs) {
			sb.append("\n'").append(job.toString()).append("'/");
			sb.append(job.getClass().getName());
			Thread thread = job.getThread();
			if (thread != null) {
				ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(new long[] { thread.getId() }, true, true);
				if (threadInfos[0] != null) {
					sb.append("\nthread info: ").append(threadInfos[0]);
				}
			}
			sb.append(", ");
		}

		Thread thread = Display.getDefault().getThread();
		ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(new long[] { thread.getId() }, true, true);
		if (threadInfos[0] != null) {
			sb.append("\n").append("UI thread info: ").append(threadInfos[0]);
		}
		long[] allThreadIds = threadMXBean.getAllThreadIds();
		for (long id : allThreadIds) {
			ThreadInfo ti = threadMXBean.getThreadInfo(new long[] { id }, true, true)[0];
			if (ti != null && ti.getThreadName().contains("Worker")) {
				sb.append("\n").append("worker: ").append(ti);
			}
		}
		return sb.toString();
	}

	public static List<Job> getRunningOrWaitingJobs(Object family) {
		List<Job> running = new ArrayList<>();
		for (Job job : Job.getJobManager().find(family)) {
			if (isRunningOrWaitingJob(job)) {
				running.add(job);
			}
		}
		return running;
	}

	private static List<Job> getSleepingJobs(Object family) {
		List<Job> sleeping = new ArrayList<>();
		for (Job job : Job.getJobManager().find(family)) {
			if (job.getState() == Job.SLEEPING) {
				sleeping.add(job);
			}
		}
		return sleeping;
	}

	private static boolean isRunningOrWaitingJob(Job job) {
		int state = job.getState();
		return (state == Job.RUNNING || state == Job.WAITING);
	}

	public static void processUIEvents(final long millis) {
		long start = currentTimeMs();
		Display display = Display.getCurrent();
		do {
			while (display.readAndDispatch()) {
				// Keep pumping events until the queue is empty
			}
		} while (currentTimeMs() - start < millis);
	}

	static long currentTimeMs() {
		return System.nanoTime() / (1000L * 1000);
	}
}
