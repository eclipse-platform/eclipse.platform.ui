/*******************************************************************************
 * Copyright (c) 2010, 2017 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     James Blackburn (Broadcom Corp.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.concurrency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.TestBarrier2;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;


/**
 * Test for an issue where a lock, held by the UI thread
 * is released while the UI thread is actually performing work
 * having acquired it...
 */
public class Bug_262032 {

	ISchedulingRule identityRule = new ISchedulingRule() {
		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	};

	volatile boolean concurrentAccess = false;

	/**
	 * Threads: UI(+asyncExec), j Locks: lock, IDRule
	 * <p>
	 * j holds identity Rule ui tries to acquire rule =&gt; block and performs
	 * asyncMessages asyncExec run and acquire()s lock j then attempts to acquire
	 * lock.
	 * <p>
	 * Deadlock manager believes that UI is waiting for IDrule while holding lock,
	 * and Job holds IDRule while attempting lock. Scheduling rules are never
	 * released by the Deadlock detector, so the lock is yielded!
	 * <p>
	 * The expectation is that when threads are 'waiting' they're sat in the ordered
	 * lock acquire which can give the locks safely to whoever is deemed to need it.
	 * In this case that's not true as the UI is running an async exec.
	 * <p>
	 * The result is concurrent running in a locked region.
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testBug262032() throws InterruptedException {
		final ILock lock = Job.getJobManager().newLock();
		final TestBarrier2 tb1 = new TestBarrier2(-1);

		// Job hols scheduling rule
		Job j = new Job ("Deadlocking normal Job") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				tb1.setStatus(TestBarrier2.STATUS_WAIT_FOR_START);
				tb1.waitForStatus(TestBarrier2.STATUS_RUNNING);
				lock.acquire();
				//test that we haven't both acquired the lock...
				assertTrue(!concurrentAccess);
				lock.release();

				tb1.setStatus(TestBarrier2.STATUS_WAIT_FOR_DONE);
				return Status.OK_STATUS;
			}
		};
		j.setRule(identityRule);
		j.schedule();

		// Wait for the job with scheduling rule to start
		tb1.waitForStatus(TestBarrier2.STATUS_WAIT_FOR_START);

		// asyncExec job that wants the lock
		Display.getDefault().asyncExec(() -> {
			lock.acquire();
			concurrentAccess = true;
			tb1.setStatus(TestBarrier2.STATUS_RUNNING);
			// Sleep to test for concurrent access
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				/* don't care */}
			concurrentAccess = false;
			lock.release();
		});

		// This will block, but the UI will continue to service async requests...
		Job.getJobManager().beginRule(identityRule, null);
		Job.getJobManager().endRule(identityRule);

		j.join();
		tb1.waitForStatus(TestBarrier2.STATUS_WAIT_FOR_DONE);
		assertEquals(Status.OK_STATUS, j.getResult());
	}

}
