/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import java.util.*;

import junit.framework.*;
import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.jobs.*;

/**
 * Tests the API of the class IJobManager
 */
public class IJobManagerTest extends TestCase {
	class TestJobListener extends JobChangeAdapter  {
		private Set scheduled = Collections.synchronizedSet(new HashSet());
		public void cancelAllJobs() {
			Job[] jobs = (Job[]) scheduled.toArray(new Job[0]);
			for (int i = 0; i < jobs.length; i++) {
				jobs[i].cancel();
			}
		}
		public void done(IJobChangeEvent event) {
			synchronized (IJobManagerTest.this) {
				if (scheduled.remove(event.getJob())) {
					//wake up the waitForCompletion method
					completedJobs++;
					IJobManagerTest.this.notify();
				}
			}
		}
		public void scheduled(IJobChangeEvent event) {
			Job job = event.getJob();
			synchronized (IJobManagerTest.this) {
				if (job instanceof TestJob) {
					scheduledJobs++;
					scheduled.add(job);
				}
			}
		}
	}
	public static Test suite() {
		return new TestSuite(IJobManagerTest.class);
	}
	/**
	 * Tests that are timing sensitive cannot be released in automated tests.
	 * Set this flag to true to do manual timing sanity tests
	 */
	private static final boolean PEDANTIC = false;

	protected int completedJobs;
	private IJobChangeListener[] jobListeners;
	private JobManager manager;
	private FussyProgressProvider progressProvider;
	protected int scheduledJobs;
	public IJobManagerTest(String name) {
		super(name);
	}
	public IJobManagerTest() {
		super("");
	}
	/**
	 * Asserts the current job state
	 */
	public void assertState(String msg, Job job, int expectedState) {
		if (job.getState() != expectedState)
			assertTrue(msg + ": expected state: " + printState(expectedState) + " actual state: " + printState(job.getState()), false);
	}
	/**
	 * Cancels a list of jobs
	 */
	protected void cancel(ArrayList jobs) {
		for (Iterator it = jobs.iterator(); it.hasNext();)
			 ((Job) it.next()).cancel();
	}
	private String printState(int state) {
		switch (state) {
			case Job.NONE :
				return "NONE";
			case Job.WAITING :
				return "WAITING";
			case Job.SLEEPING :
				return "SLEEPING";
			case Job.RUNNING :
				return "RUNNING";
		}
		return "UNKNOWN";
	}
	protected void setUp() throws Exception {
		super.setUp();
		manager = JobManager.getInstance();
		completedJobs = 0;
		scheduledJobs = 0;
		jobListeners = new IJobChangeListener[] {/* new VerboseJobListener(),*/
			new TestJobListener()};
		for (int i = 0; i < jobListeners.length; i++) {
			manager.addJobChangeListener(jobListeners[i]);
		}
		progressProvider = new FussyProgressProvider();
		manager.setProgressProvider(progressProvider);
	}
	private void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
		}
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		for (int i = 0; i < jobListeners.length; i++)
			if (jobListeners[i] instanceof TestJobListener)
				 ((TestJobListener) jobListeners[i]).cancelAllJobs();
		waitForCompletion();
		progressProvider.sanityCheck();
		manager.setProgressProvider(null);
		for (int i = 0; i < jobListeners.length; i++) {
			manager.removeJobChangeListener(jobListeners[i]);
		}
	}
	public void testDelayedJob() {
		//schedule a delayed job and ensure it doesn't start until instructed
		int[] sleepTimes = new int[] { 0, 10, 50, 100, 500, 1000, 2000, 2500 };
		for (int i = 0; i < sleepTimes.length; i++) {
			long start = System.currentTimeMillis();
			TestJob job = new TestJob("Noop", 0, 0);
			assertEquals("1.0", 0, job.getRunCount());
			job.schedule(sleepTimes[i]);
			waitForCompletion();
			assertEquals("1.1." + i, 1, job.getRunCount());
			long duration = System.currentTimeMillis() - start;
			assertTrue("1.2: duration: " + duration + " sleep: " + sleepTimes[i], duration >= sleepTimes[i]);
			//a no-op job shouldn't take any real time
			if (PEDANTIC)
				assertTrue("1.3: duration: " + duration + " sleep: " + sleepTimes[i], duration < sleepTimes[i] + 1000);
		}
	}
	public void testMutexRule() {
		final int JOB_COUNT = 10;
		Job[] jobs = new Job[JOB_COUNT];
		ISchedulingRule mutex = new IdentityRule();
		for (int i = 0; i < JOB_COUNT; i++) {
			jobs[i] = new TestJob("testSimpleRules", 1000000, 10);
			jobs[i].setRule(mutex);
			jobs[i].schedule();
		}
		//first job should be running, all others should be waiting
		waitForStart(jobs[0]);
		assertState("1.0", jobs[0], Job.RUNNING);
		for (int i = 1; i < JOB_COUNT; i++) {
			assertState("1.1." + i, jobs[i], Job.WAITING);
		}
		//cancel job i, then i+1 should run and all others should wait
		for (int i = 0; i < JOB_COUNT - 1; i++) {
			jobs[i].cancel();
			waitForStart(jobs[i + 1]);
			assertState("2.0." + i, jobs[i + 1], Job.RUNNING);
			for (int j = i + 2; j < JOB_COUNT; j++) {
				assertState("2.1" + i + "." + j, jobs[j], Job.WAITING);
			}
		}
		//cancel the final job
		jobs[JOB_COUNT - 1].cancel();
	}
	public void testOrder() {
		//ensure jobs are run in order from lowest to highest sleep time.
		final List done = Collections.synchronizedList(new ArrayList());
		IJobChangeListener listener = new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (event.getJob() instanceof TestJob)
					done.add(event.getJob());
			}
		};
		int[] sleepTimes = new int[] { 50, 250, 500, 800, 1000, 1500 };
		Job[] jobs = new Job[sleepTimes.length];
		manager.addJobChangeListener(listener);
		try {
			for (int i = 0; i < sleepTimes.length; i++)
				jobs[i] = new TestJob("testOrder(" + i + ")", 1, 1);
			for (int i = 0; i < sleepTimes.length; i++)
				jobs[i].schedule(sleepTimes[i]);
			waitForCompletion();
			//make sure listener has had a chance to process the finished job
			while (done.size() != jobs.length) {
				Thread.yield();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			Job[] doneOrder = (Job[]) done.toArray(new Job[done.size()]);
			assertEquals("1.0", jobs.length, doneOrder.length);
			for (int i = 0; i < doneOrder.length; i++)
				assertEquals("1.1." + i, jobs[i], doneOrder[i]);
		} finally {
			manager.removeJobChangeListener(listener);
		}
	}
	public void testSimple() {
		final int JOB_COUNT = 10;
		for (int i = 0; i < JOB_COUNT; i++) {
			new TestJob("testSimple").schedule();
		}
		waitForCompletion();
		//
		for (int i = 0; i < JOB_COUNT; i++) {
			new TestJob("testSimple").schedule(50);
		}
		waitForCompletion();
	}
	public void testSleep() {
		Job job = new TestJob("ParentJob", 10, 100);
		//sleeping a job that isn't scheduled should have no effect
		assertEquals("1.0", Job.NONE, job.getState());
		assertTrue("1.1", job.sleep());
		assertEquals("1.2", Job.NONE, job.getState());

		//sleeping a job that is already running should not work
		job.schedule();
		//give the job a chance to start
		waitForStart(job);
		assertState("2.0", job, Job.RUNNING);
		assertTrue("2.1", !job.sleep());
		assertState("2.2", job, Job.RUNNING);

		waitForCompletion();

		//sleeping a job that is already sleeping should make sure it never runs
		job.schedule(500);
		assertState("3.0", job, Job.SLEEPING);
		assertTrue("3.1", job.sleep());
		assertState("3.2", job, Job.SLEEPING);
		//wait awhile and ensure the job is still sleeping
		Thread.yield();
		sleep(600);
		Thread.yield();
		assertState("3.3", job, Job.SLEEPING);
		assertTrue("3.4", job.cancel()); //should be possible to cancel a sleeping job
	}
	public void testSleepOnWait() {
		//keep scheduling infinitely long jobs until we have a job waiting
		ArrayList longJobs = new ArrayList();
		TestJob job = null;
		//start enough jobs to saturate the worker pool
		final int MAX_THREADS = 100;
		for (int i = 0; i < MAX_THREADS; i++) {
			job = new TestJob("Long Job", 1000000, 10);
			job.schedule();
			longJobs.add(job);
		}
		job = new TestJob("Long Job", 1000000, 10);
		job.schedule();
		//we know this job is waiting, so putting it to sleep should prevent it from running
		assertState("1.0", job, Job.WAITING);
		assertTrue("1.1", job.sleep());
		assertState("1.2", job, Job.SLEEPING);

		//cancel all the long jobs, thus freeing the pool for the waiting job
		cancel(longJobs);
		//make sure the job is still waiting
		assertState("1.3", job, Job.SLEEPING);

		//now wake the job up
		job.wakeUp();
		waitForStart(job);
		assertState("2.0", job, Job.RUNNING);

		//finally cancel the job
		job.cancel();
	}
	/**
	 * Tests a batch of jobs that use two mutually exclusive rules.
	 */
	public void testTwoRules() {
		final int JOB_COUNT = 10;
		Job[] jobs = new Job[JOB_COUNT];
		ISchedulingRule evens = new IdentityRule();
		ISchedulingRule odds = new IdentityRule();
		for (int i = 0; i < JOB_COUNT; i++) {
			jobs[i] = new TestJob("testSimpleRules", 1000000, 10);
			jobs[i].setRule(((i & 0x1) == 0) ? evens : odds);
			jobs[i].schedule();
		}
		//first two jobs should be running, all others should be waiting
		waitForStart(jobs[0]);
		waitForStart(jobs[1]);
		assertState("1.0", jobs[0], Job.RUNNING);
		assertState("1.1", jobs[1], Job.RUNNING);
		for (int i = 2; i < JOB_COUNT; i++) {
			assertState("1.2." + i, jobs[i], Job.WAITING);
		}
		//cancel job i then i+1 and i+2 should run and all others should wait
		for (int i = 0; i < JOB_COUNT; i++) {
			jobs[i].cancel();
			try {
				waitForStart(jobs[i + 1]);
				assertState("2.0." + i, jobs[i + 1], Job.RUNNING);
				waitForStart(jobs[i + 2]);
				assertState("2.1." + i, jobs[i + 2], Job.RUNNING);
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			for (int j = i + 3; j < JOB_COUNT; j++) {
				assertState("2.2." + i + "." + j, jobs[j], Job.WAITING);
			}
		}
	}
	private synchronized void waitForCompletion() {
		int i = 0;
		assertTrue("Jobs completed that weren't scheduled", completedJobs <= scheduledJobs);
		while (completedJobs < scheduledJobs) {
			try {
				wait(500);
			} catch (InterruptedException e) {
			}
			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for job to complete", i++ < 1000);
		}
	}
	/**
	 * A job has been scheduled.  Pause this thread so that a worker thread
	 * has a chance to pick up the new job.
	 */
	private void waitForStart(Job job) {
		int i = 0;
		while (job.getState() != Job.RUNNING) {
			Thread.yield();
			sleep(100);
			Thread.yield();
			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for job to start", i++ < 1000);
		}
	}
}