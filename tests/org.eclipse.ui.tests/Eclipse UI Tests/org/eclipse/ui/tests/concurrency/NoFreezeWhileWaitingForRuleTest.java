/*******************************************************************************
 * Copyright (c) 2022 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christoph Läubrich - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.concurrency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.tests.concurrency.SyncExecWhileUIThreadWaitsForRuleTest.TestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test makes sure that a waiting on a rule do not blocks the UI thread, it
 * has three participants
 *
 * <ol>
 * <li>A Job that acquire and block a rule until it is notified</li>
 * <li>A runnable in the UI thread that tries to acquire the rule and will be
 * blocked</li>
 * <li>A thread that notify the first job via the UI thread to simulate some UI
 * events after a certain number of UI events have happened</li>
 * </ol>
 */
public class NoFreezeWhileWaitingForRuleTest {

	TestRule rule;
	IProgressMonitor ruleMonitor;
	private CountDownLatch eventQueueLatch;

	@Before
	public void setUp() {
		rule = new SyncExecWhileUIThreadWaitsForRuleTest.TestRule();
		ruleMonitor = new ProgressMonitorWrapper(new NullProgressMonitor()) {
			AtomicBoolean canceled = new AtomicBoolean();

			@Override
			public void setCanceled(boolean cancel) {
				canceled.set(cancel);
			}

			@Override
			public boolean isCanceled() {
				return canceled.get();
			}
		};
		eventQueueLatch = new CountDownLatch(1);
	}

	@After
	public void tearDown() {
		ruleMonitor.setCanceled(true); // just in case...
	}

	@Test
	public void testWaiting() throws InterruptedException {
		// If you want to see the blocking uncomment the following line:
		// Job.getJobManager().setProgressProvider(null);
		Display display = Display.getDefault();
		assertTrue(display.getThread() == Thread.currentThread());
		try {
			Job blockingJob = spinRuleBlockingJob();
			CountDownLatch runnableLatch = spinUIblockingRunnable(display);
			Thread uiEventProducer = spinUIEventProducer(runnableLatch, display);
			while (!eventQueueLatch.await(10, TimeUnit.MILLISECONDS) && !ruleMonitor.isCanceled()) {
				while (display.readAndDispatch()) {
				}
				Thread.onSpinWait();
			}
			uiEventProducer.interrupt();
			blockingJob.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw e;
		}
		assertFalse("Timeout reached, blocking occurred!", ruleMonitor.isCanceled());
	}

	private Thread spinUIEventProducer(CountDownLatch runnableLatch, Display display) {
		Thread thread = new Thread(() -> {
			// Stage 1: Wait for the UI-Thread to block...
			try {
				runnableLatch.await();
			} catch (InterruptedException e) {
				return;
			}
			// Stage 2: Schedule async exec to simulate some UI events happen...
			CountDownLatch eventLatch = new CountDownLatch(10);
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					if (ruleMonitor.isCanceled()) {
						// break out...
						return;
					}
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (InterruptedException e) {
					}
					if (eventLatch.getCount() > 0) {
						eventLatch.countDown();
						display.asyncExec(this);
					}
				}
			});
			// Stage 3: wait for the events
			try {
				eventLatch.await();
			} catch (InterruptedException e) {
				return;
			}
			// Stage 4: signal the blocking thread...
			eventQueueLatch.countDown();
		});
		thread.setDaemon(true);
		thread.start();
		return thread;
	}

	private CountDownLatch spinUIblockingRunnable(Display display) {
		CountDownLatch runnableRunning = new CountDownLatch(1);
		display.asyncExec(() -> {
			runnableRunning.countDown();
			Job.getJobManager().beginRule(rule, ruleMonitor);
			Job.getJobManager().endRule(rule);
		});
		return runnableRunning;
	}

	private Job spinRuleBlockingJob() throws InterruptedException {
		CountDownLatch jobStarted = new CountDownLatch(1);
		long timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20);
		ICoreRunnable ruleBlockingRunnable = monitor -> {
			Job.getJobManager().beginRule(rule, ruleMonitor);
			jobStarted.countDown();
			try {
				while (!eventQueueLatch.await(1, TimeUnit.SECONDS)) {
					if (System.currentTimeMillis() > timeout) {
						ruleMonitor.setCanceled(true);
						break;
					}
					Thread.yield();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				Job.getJobManager().endRule(rule);
			}
		};
		Job job = Job.create("Runs until notified", ruleBlockingRunnable);
		job.schedule();
		assertTrue("Job was not started", jobStarted.await(10, TimeUnit.SECONDS));
		return job;
	}
}
