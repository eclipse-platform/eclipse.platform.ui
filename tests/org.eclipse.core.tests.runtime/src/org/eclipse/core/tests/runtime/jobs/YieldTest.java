/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.runtime.RuntimeLog;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.core.tests.harness.TestBarrier;
import org.eclipse.core.tests.harness.TestJob;

/**
 * Tests for {@link Job#yieldRule()}.
 */
public class YieldTest extends AbstractJobManagerTest implements ILogListener {

	class TestJobListener extends JobChangeAdapter {
		private Set scheduled = Collections.synchronizedSet(new HashSet());

		public void cancelAllJobs() {
			Job[] jobs = (Job[]) scheduled.toArray(new Job[0]);
			for (int i = 0; i < jobs.length; i++) {
				jobs[i].cancel();
			}
		}

		public void done(IJobChangeEvent event) {
			synchronized (YieldTest.this) {
				if (scheduled.remove(event.getJob())) {
					//wake up the waitForCompletion method
					completedJobs++;
					YieldTest.this.notify();
				}
			}
		}

		public void scheduled(IJobChangeEvent event) {
			Job job = event.getJob();
			synchronized (YieldTest.this) {
				if (job instanceof TestJob) {
					scheduledJobs++;
					scheduled.add(job);
				}
			}
		}
	}

	protected int completedJobs;

	private IJobChangeListener[] jobListeners;

	protected int scheduledJobs;

	public static Test suite() {
		return new TestSuite(YieldTest.class);
		//		TestSuite suite = new TestSuite();
		//		for (int i = 0; i < 1000; i++) {
		//			suite.addTest(new YieldTest("testYieldJobToJob"));
		//		}
		//		return suite;
	}

	public YieldTest(String name) {
		super(name);
	}

	public void logging(IStatus status, String plugin) {
		System.out.println(status);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		completedJobs = 0;
		scheduledJobs = 0;
		jobListeners = new IJobChangeListener[] {/* new VerboseJobListener(),*/
		new TestJobListener()};
		for (int i = 0; i < jobListeners.length; i++) {
			manager.addJobChangeListener(jobListeners[i]);
		}
		RuntimeLog.addLogListener(this);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		for (int i = 0; i < jobListeners.length; i++)
			if (jobListeners[i] instanceof TestJobListener)
				((TestJobListener) jobListeners[i]).cancelAllJobs();
		waitForCompletion();
		for (int i = 0; i < jobListeners.length; i++) {
			manager.removeJobChangeListener(jobListeners[i]);
		}
		super.tearDown();
		RuntimeLog.removeLogListener(this);
		//		manager.startup();
	}

	public void testExceptionWhenYieldingNotOwner() {
		int[] location = new int[2];
		final TestBarrier barrier1 = new TestBarrier(location, 0);
		final TestBarrier barrier2 = new TestBarrier(location, 1);

		final Job yielding = new Job("Yielding") {
			protected IStatus run(IProgressMonitor monitor) {
				barrier2.setStatus(TestBarrier.STATUS_START);
				barrier1.waitForStatus(TestBarrier.STATUS_START);
				return Status.OK_STATUS;
			}
		};
		Job otherJob = new Job("ShouldFault") {
			protected IStatus run(IProgressMonitor monitor) {
				barrier2.waitForStatus(TestBarrier.STATUS_START);
				try {
					yielding.yieldRule();
				} finally {
					barrier1.setStatus(TestBarrier.STATUS_START);
				}
				return Status.OK_STATUS;
			}
		};
		yielding.schedule();
		otherJob.schedule();

		waitForCompletion(otherJob);
		assertTrue(!otherJob.getResult().isOK());
		waitForCompletion(yielding);
	}

	public void testExceptionWhenYieldingNotRunning() {
		final Job yielding = new Job("Yielding") {
			protected IStatus run(IProgressMonitor monitor) {
				return Status.OK_STATUS;
			}
		};
		try {
			yielding.yieldRule();
			fail("Did not throw exception");
		} catch (IllegalArgumentException e) {
			// ignore
		}

	}

	public void testThreadRestored() {
		final PathRule rule = new PathRule("testThreadRestored");

		final Job[] jobs = new Job[2];
		Job yieldJob = new Job("Yielding") {
			protected IStatus run(IProgressMonitor monitor) {
				assertNull("Conflicting job result is not null: " + jobs[1].getResult(), jobs[1].getResult());
				Thread before = getThread();
				while (true)
					if (yieldRule())
						break;
				waitForCompletion(jobs[1]);
				Thread after = getThread();
				assertEquals("Thread not restored", before, after);
				assertTrue("Conflicting job not done", jobs[1].getResult().isOK());
				assertTrue("Conflicting job still running", jobs[1].getState() == Job.NONE);
				return Status.OK_STATUS;
			}
		};
		jobs[0] = yieldJob;
		yieldJob.setRule(rule);

		Job conflictingJob = new Job("Conflicting") {
			protected IStatus run(IProgressMonitor monitor) {
				assertTrue(jobs[0].getState() == WAITING);
				assertTrue(jobs[0].getResult() == null);
				return Status.OK_STATUS;
			}
		};
		jobs[1] = conflictingJob;
		conflictingJob.setRule(rule);

		yieldJob.schedule();
		conflictingJob.schedule();
		waitForCompletion(yieldJob);
		assertTrue("Result is not ok: " + yieldJob.getResult(), yieldJob.getResult().isOK());
		waitForCompletion(conflictingJob);
		assertTrue(conflictingJob.getResult().isOK());
	}

	public void testYieldJobToJob() {
		final PathRule rule = new PathRule("testYieldJobToJob");

		final Job[] jobs = new Job[2];
		Job yieldJob = new Job("Yielding") {
			protected IStatus run(IProgressMonitor monitor) {
				assertTrue(jobs[1].getResult() == null);
				while (!yieldRule()) {
					//loop until yield succeeds
				}
				waitForCompletion(jobs[1]);
				assertTrue(jobs[1].getResult().isOK());
				assertTrue(jobs[1].getState() == Job.NONE);
				return Status.OK_STATUS;
			}
		};
		jobs[0] = yieldJob;
		yieldJob.setRule(rule);

		Job conflictingJob = new Job("Conflicting") {
			protected IStatus run(IProgressMonitor monitor) {
				assertEquals(WAITING, jobs[0].getState());
				assertTrue(jobs[0].getResult() == null);
				return Status.OK_STATUS;
			}
		};
		jobs[1] = conflictingJob;
		conflictingJob.setRule(rule);

		yieldJob.schedule();
		conflictingJob.schedule();
		waitForCompletion(yieldJob);
		final IStatus yieldResult = yieldJob.getResult();
		if (!yieldResult.isOK()) {
			Throwable t = yieldResult.getException();
			if (t != null)
				fail("yieldJob failed", t);
			fail("yieldJob failed:" + yieldResult);
		}
		waitForCompletion(conflictingJob);
		assertTrue(conflictingJob.getResult().isOK());
	}

	//	public void testILockTransfer() {
	//		final PathRule rule = new PathRule("testYield");
	//		final ILock lock = Job.getJobManager().newLock();
	//		final Job[] jobs = new Job[2];
	//		final IStatus[] results = new IStatus[2];
	//		Job yieldJob = new Job("Yielding") {
	//			protected IStatus run(IProgressMonitor monitor) {
	//				assertTrue(results[1] == null);
	//				assertTrue(jobs[1].getState() == Job.WAITING);
	//				lock.acquire();
	//				while (true)
	//					if (yield())
	//						break;
	//				assertTrue(results[1].isOK());
	//				assertTrue(jobs[1].getState() == Job.NONE);
	//				return Status.OK_STATUS;
	//			}
	//		};
	//		jobs[0] = yieldJob;
	//		yieldJob.setRule(rule);
	//		yieldJob.addJobChangeListener(new YieldJobListener(results, 0));
	//
	//		Job conflictingJob = new Job("Conflicting") {
	//			protected IStatus run(IProgressMonitor monitor) {
	//				lock.acquire();
	//				assertTrue(jobs[0].getState() == WAITING);
	//				assertTrue(results[0] == null);
	//				return Status.OK_STATUS;
	//			}
	//		};
	//		jobs[1] = conflictingJob;
	//		conflictingJob.setRule(rule);
	//		conflictingJob.addJobChangeListener(new YieldJobListener(results, 1));
	//
	//		yieldJob.schedule();
	//		conflictingJob.schedule();
	//		waitForCompletion(yieldJob);
	//		assertTrue(yieldJob.getResult().isOK());
	//		waitForCancel(conflictingJob);
	//		assertTrue(conflictingJob.getResult().isOK());
	//	}

	public void testYieldJobToJobAndEnsureConflictingRunsBeforeResume() {
		final PathRule rule = new PathRule("testYield");
		int[] location = new int[2];
		final TestBarrier barrier1 = new TestBarrier(location, 0);
		final Job[] jobs = new Job[2];
		Job yieldJob = new Job("Yielding") {
			protected IStatus run(IProgressMonitor monitor) {
				barrier1.waitForStatus(TestBarrier.STATUS_START);
				barrier1.setStatus(TestBarrier.STATUS_RUNNING);
				while (true) {
					if (yieldRule())
						break;
				}
				barrier1.waitForStatus(TestBarrier.STATUS_DONE);
				return Status.OK_STATUS;
			}
		};
		jobs[0] = yieldJob;
		yieldJob.setRule(rule);

		final Job conflictingJob1 = new Job("Conflicting1") {
			protected IStatus run(IProgressMonitor monitor) {
				barrier1.setStatus(TestBarrier.STATUS_DONE);
				return Status.OK_STATUS;
			}
		};
		conflictingJob1.setRule(rule);

		Job nonConflict = new Job("Non conflict") {
			protected IStatus run(IProgressMonitor monitor) {
				return Status.OK_STATUS;
			}
		};

		yieldJob.schedule();
		conflictingJob1.schedule();
		barrier1.setStatus(TestBarrier.STATUS_START);
		// we are testing a race-condition here... (This test will not catch all conditions)
		// make sure starting a non-conflicting job does not cause 
		// the yielding job to resume before conflicting job has a chance to run
		nonConflict.schedule(1);

		waitForCompletion(yieldJob);
		waitForCompletion(conflictingJob1);
		waitForCompletion(nonConflict);
		assertTrue(yieldJob.getResult().isOK());
		assertTrue(conflictingJob1.getResult().isOK());
		assertTrue(nonConflict.getResult().isOK());
		System.out.println("");
	}

	public void testYieldJobToThread() {

		final PathRule rule = new PathRule("testYieldJobToThread");
		final TestBarrier barrier = new TestBarrier();
		final Job yielding = new Job("Yielding") {
			protected IStatus run(IProgressMonitor monitor) {
				barrier.setStatus(TestBarrier.STATUS_START);
				while (true)
					if (yieldRule())
						break;
				// ensure that the other job ran 
				barrier.waitForStatus(TestBarrier.STATUS_DONE);
				return Status.OK_STATUS;
			}
		};
		yielding.setRule(rule);
		Thread t = new Thread("Conflicting") {
			public void run() {
				try {
					Job.getJobManager().beginRule(rule, null);
					barrier.setStatus(TestBarrier.STATUS_DONE);
				} finally {
					Job.getJobManager().endRule(rule);
				}
			}
		};

		yielding.schedule();
		barrier.waitForStatus(TestBarrier.STATUS_START);
		t.start();

		waitForCompletion(yielding);
		assertTrue(yielding.getResult().isOK());
	}

	public void testYieldThreadJobToThread() {
		final PathRule rule = new PathRule("testYield");
		final TestBarrier barrier = new TestBarrier();
		final Job yielding = new Job("Yielding") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Job.getJobManager().beginRule(rule, null);
					barrier.setStatus(TestBarrier.STATUS_START);
					while (true)
						if (yieldRule())
							break;
					// ensure that the other job ran 
					barrier.waitForStatus(TestBarrier.STATUS_DONE);
				} finally {
					Job.getJobManager().endRule(rule);
				}
				return Status.OK_STATUS;
			}
		};

		Thread t = new Thread("Conflicting") {
			public void run() {
				try {
					Job.getJobManager().beginRule(rule, null);
					barrier.setStatus(TestBarrier.STATUS_DONE);
				} finally {
					Job.getJobManager().endRule(rule);
				}
			}
		};

		yielding.schedule();
		barrier.waitForStatus(TestBarrier.STATUS_START);
		t.start();

		waitForCompletion(yielding, 5000);
		assertTrue(yielding.getResult().isOK());
	}

	public void testYieldThreadToJob() {

		final PathRule rule = new PathRule("testYield");
		final TestBarrier barrier = new TestBarrier();
		final Thread t = new Thread() {
			public void run() {
				IJobManager m = Job.getJobManager();
				try {
					m.beginRule(rule, null);
					Job j = m.currentJob();
					barrier.setStatus(TestBarrier.STATUS_START);
					barrier.waitForStatus(TestBarrier.STATUS_WAIT_FOR_START);
					while (true)
						if (j.yieldRule())
							break;
					// ensure that the other job ran 
					barrier.waitForStatus(TestBarrier.STATUS_DONE);
				} finally {
					m.endRule(rule);
					barrier.setStatus(TestBarrier.STATUS_WAIT_FOR_DONE);
				}
			}
		};

		final Job conflictingJob = new Job("Conflicting") {
			protected IStatus run(IProgressMonitor monitor) {
				barrier.setStatus(TestBarrier.STATUS_DONE);
				return Status.OK_STATUS;
			}
		};

		t.start();
		barrier.waitForStatus(TestBarrier.STATUS_START);
		conflictingJob.setRule(rule);
		conflictingJob.schedule();
		barrier.setStatus(TestBarrier.STATUS_WAIT_FOR_START);
		barrier.waitForStatus(TestBarrier.STATUS_WAIT_FOR_DONE);
		waitForCompletion(conflictingJob);
		assertTrue(conflictingJob.getResult().isOK());
	}

	public void testYieldThreadToThreadJob() {
		final PathRule rule = new PathRule("testYield");
		final TestBarrier barrier = new TestBarrier();
		Thread t = new Thread("Yielding") {
			public void run() {
				try {
					Job.getJobManager().beginRule(rule, null);
					barrier.setStatus(TestBarrier.STATUS_START);
					while (true)
						if (Job.getJobManager().currentJob().yieldRule())
							break;
					// ensure that the other job ran 
					barrier.waitForStatus(TestBarrier.STATUS_DONE);
				} finally {
					Job.getJobManager().endRule(rule);
				}
				// status code irrelevant
				barrier.setStatus(TestBarrier.STATUS_BLOCKED);
			}
		};

		final Job conflicting = new Job("Conflicting") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					Job.getJobManager().beginRule(rule, null);
				} finally {
					Job.getJobManager().endRule(rule);
				}
				barrier.setStatus(TestBarrier.STATUS_DONE);
				return Status.OK_STATUS;
			}
		};

		t.start();
		barrier.waitForStatus(TestBarrier.STATUS_START);
		conflicting.schedule();

		waitForCompletion(conflicting, 5000);
		assertTrue(conflicting.getResult().isOK());
		barrier.waitForStatus(TestBarrier.STATUS_BLOCKED);
	}

	private synchronized void waitForCompletion() {
		int i = 0;
		assertTrue("Jobs completed that weren't scheduled", completedJobs <= scheduledJobs);
		while (completedJobs < scheduledJobs) {
			try {
				wait(500);
			} catch (InterruptedException e) {
				//ignore
			}
			//sanity test to avoid hanging tests
			if (i++ > 1000) {
				dumpState();
				assertTrue("Timeout waiting for job to complete", false);
			}
		}
	}
}
