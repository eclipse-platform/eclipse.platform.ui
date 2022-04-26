/*******************************************************************************
 * Copyright (c) 2009, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christoph Läubrich - reproducer for issue #40
 ******************************************************************************/

package org.eclipse.ui.tests.concurrency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;

/**
 * This tests the simple traditional deadlock of a thread holding a scheduling rule trying
 * to perform a syncExec, while the UI thread is waiting for that scheduling rule.
 * UISynchronizer and UILockListener conspire to prevent deadlock in this case.
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=296056.
 */
public class SyncExecWhileUIThreadWaitsForRuleTest {
	static class TestRule implements ISchedulingRule {
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}

	@Test
	public void testRuleWaiting() throws InterruptedException {
		Display display = Display.getDefault();
		final SubMonitor beginRuleMonitor = SubMonitor.convert(null);
		CountDownLatch latch_async = new CountDownLatch(1);
		CountDownLatch latch_job = new CountDownLatch(1);
		final ISchedulingRule rule = new TestRule();
		Job job1 = Job.create("Runs until notified", new ICoreRunnable() {

			@Override
			public void run(IProgressMonitor monitor) {
				System.out.println("Job is beginning rule...");
				Job.getJobManager().beginRule(rule, beginRuleMonitor);
				System.out.println("Countdown the async latch...");
				latch_async.countDown();
				try {
					System.out.println("Waiting for the job latch...");
					latch_job.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					Job.getJobManager().endRule(rule);
				}
			}
		});
		job1.schedule();
		assertTrue("Job 1 was not started", latch_async.await(10, TimeUnit.SECONDS));
		Job job2 = Job.create("Just another job", new ICoreRunnable() {

			@Override
			public void run(IProgressMonitor monitor) {
				Display.getDefault().asyncExec(() -> {
					System.out.println("Async exec... entering rule...");
					Job.getJobManager().beginRule(rule, beginRuleMonitor);
					try {
						System.out.println("Count down latch...");
						latch_job.countDown();
					} finally {
						Job.getJobManager().endRule(rule);
					}

				});
			}
		});
		job2.schedule();

		long timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
		Thread canceler = new Thread("Canceler") {
			@Override
			public void run() {
				while (true) {
					if (System.currentTimeMillis() > timeout) {
						System.out.println("Cancel...");
						job1.cancel();
						job2.cancel();
						beginRuleMonitor.setCanceled(true);
						break;
					}
					Thread.yield();
				}

			}
		};
		canceler.start();

		try {
			while (!latch_job.await(10, TimeUnit.MILLISECONDS)) {
				while (display.readAndDispatch()) {
				}
				Thread.onSpinWait();
			}
		} finally {
			display.dispose();
		}
		assertTrue("Job 2 did not complete", latch_job.await(10, TimeUnit.MILLISECONDS));
	}

	@Test
	public void testDeadlock() {
		final ISchedulingRule rule = new TestRule();
		final boolean[] blocked = new boolean[] {false};
		final boolean[] lockAcquired = new boolean[] {false};
		final SubMonitor beginRuleMonitor = SubMonitor.convert(null);
		Thread locking = new Thread("SyncExecWhileUIThreadWaitsForRuleTest") {
			@Override
			public void run() {
				try {
					//first make sure this background thread owns the lock
					Job.getJobManager().beginRule(rule, null);
					//spawn an asyncExec that will cause the UI thread to be blocked
					Display.getDefault().asyncExec(() -> {
						blocked[0] = true;
						try {
							Job.getJobManager().beginRule(rule, beginRuleMonitor);
						} finally {
							Job.getJobManager().endRule(rule);
						}
						blocked[0] = false;
					});
					//wait until the UI thread is blocked waiting for the lock
					while (!blocked[0]) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
					//now attempt to do a syncExec that also acquires the lock
					//this should succeed even while the above asyncExec is blocked, thanks to UISynchronizer
					Display.getDefault().syncExec(() -> {
						// use a timeout to avoid deadlock in case of regression
						Job.getJobManager().beginRule(rule, null);
						lockAcquired[0] = true;
						Job.getJobManager().endRule(rule);
					});
				} finally {
					Job.getJobManager().endRule(rule);
				}
			}
		};
		locking.start();
		//create a thread that will cancel the monitor after 60 seconds so we don't hang the tests
		final long waitStart = System.currentTimeMillis();
		Thread canceler = new Thread("Canceler") {
			@Override
			public void run() {
				while (true) {
					if (System.currentTimeMillis() - waitStart > 60000) {
						beginRuleMonitor.setCanceled(true);
						break;
					}
				}

			}
		};
		canceler.start();
		//wait until we succeeded to acquire the lock in the UI thread
		Display display = Display.getDefault();
		while (!lockAcquired[0]) {
			//spin event loop so that asyncExed above gets run
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (SWTException e) {
				fail("Deadlock occurred");
			}
		}
		//if the monitor was canceled then we got a deadlock
		assertFalse("deadlock occurred", beginRuleMonitor.isCanceled());
		// if we get here, the test succeeded
		if (Thread.interrupted()) {
			// TODO: re-enable this check after bug 505920 is fixed
			// fail("Thread was interrupted at end of test");
		}
	}
}
