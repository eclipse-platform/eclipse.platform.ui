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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.core.tests.harness.FussyProgressMonitor;

/**
 * Tests the API of the class IJobManager
 */
public class IJobManagerTest extends AbstractJobManagerTest {
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
		completedJobs = 0;
		scheduledJobs = 0;
		jobListeners = new IJobChangeListener[] {/* new VerboseJobListener(),*/
			new TestJobListener()};
		for (int i = 0; i < jobListeners.length; i++) {
			manager.addJobChangeListener(jobListeners[i]);
		}
	}
	private void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
		}
	}
	protected void tearDown() throws Exception {
		for (int i = 0; i < jobListeners.length; i++)
			if (jobListeners[i] instanceof TestJobListener)
				 ((TestJobListener) jobListeners[i]).cancelAllJobs();
		waitForCompletion();
		for (int i = 0; i < jobListeners.length; i++) {
			manager.removeJobChangeListener(jobListeners[i]);
		}
		super.tearDown();
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
	public void testReverseOrder() {
		//ensure jobs are run in order from lowest to highest sleep time.
		final List done = Collections.synchronizedList(new ArrayList());
		IJobChangeListener listener = new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				if (event.getJob() instanceof TestJob)
					//add at start of list to get reverse order
					done.add(0, event.getJob());
			}
		};
		int[] sleepTimes = new int[] { 4000, 3000, 2000, 1000, 50 };
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
	
	public void testJobFamilyCancel() {
		//test the cancellation of a family of jobs
		final int NUM_JOBS = 20;
		Job [] jobs = new Job[NUM_JOBS];
		//create two different families of jobs
		TestJobFamily first = new TestJobFamily(TestJobFamily.TYPE_ONE);
		TestJobFamily second = new TestJobFamily(TestJobFamily.TYPE_TWO);
		//need a scheduling rule so that the jobs would be executed one by one
		ISchedulingRule rule = new IdentityRule();
		for(int i = 0; i < NUM_JOBS; i++) {
			//assign half the jobs to the first family, the other half to the second family
			if(i%2 == 0)
				jobs[i] = new CustomTestJob("TestFirstFamily", 1000000, 10, TestJobFamily.TYPE_ONE);
			else /*if(i%2 == 1)*/
				jobs[i] = new CustomTestJob("TestSecondFamily", 1000000, 10, TestJobFamily.TYPE_TWO);
				
			jobs[i].setRule(rule);
			jobs[i].schedule();
		}
				
		waitForStart(jobs[0]);
		
		assertState("1.0", jobs[0], Job.RUNNING);
		
		//first job is running, the rest are waiting
		for(int i = 1; i < NUM_JOBS; i++) {
			assertState("1." + i, jobs[i], Job.WAITING);
		}
		
		//cancel the first family of jobs		
		manager.cancel(first);
		waitForCancel(jobs[0]);
		
		//the previously running job should have no state
		assertState("2.0", jobs[0], Job.NONE);
		//the first job from the second family should now be running
		waitForStart(jobs[1]);
		
		for(int i = 2; i < NUM_JOBS; i++) {
			//all other jobs in the first family should be removed from the waiting queue
			//no operations can be performed on these jobs until they are scheduled with the manager again
			if(jobs[i].belongsTo(first)) {
				assertState("2." + i, jobs[i], Job.NONE);
				jobs[i].wakeUp();
				assertState("2." + i, jobs[i], Job.NONE);
				jobs[i].sleep();
				assertState("2." + i, jobs[i], Job.NONE);
			}
			//all other jobs in the second family should still be in the waiting queue
			else {
				assertState("3." + i, jobs[i], Job.WAITING);
			}
		}
						
		for(int i = 2; i < NUM_JOBS; i++) {
			//all the jobs in the second family that are waiting to start can now be set to sleep
			if(jobs[i].belongsTo(second)) {
				assertState("4." + i, jobs[i], Job.WAITING);
				assertTrue("5." + i, jobs[i].sleep());
				assertState("6." + i, jobs[i], Job.SLEEPING);
			}
		}	
		//cancel the second family of jobs
		manager.cancel(second);
		waitForCancel(jobs[1]);
		
		//the second job should now have no state
		assertState("7.0", jobs[1], Job.NONE);
				
		for(int i = 0; i < NUM_JOBS; i++) {
			//all jobs should now be in the NONE state
			assertState("8." + i, jobs[i], Job.NONE);
		}
	}
	
	public void testJobFamilyFind() {
		//test of finding jobs based on the job family they belong to
		final int NUM_JOBS = 20;
		Job [] jobs = new Job[NUM_JOBS];
		//create five different families of jobs
		TestJobFamily first = new TestJobFamily(TestJobFamily.TYPE_ONE);
		TestJobFamily second = new TestJobFamily(TestJobFamily.TYPE_TWO);
		TestJobFamily third = new TestJobFamily(TestJobFamily.TYPE_THREE);
		TestJobFamily fourth = new TestJobFamily(TestJobFamily.TYPE_FOUR);
		TestJobFamily fifth = new TestJobFamily(TestJobFamily.TYPE_FIVE);
		
		//need a scheduling rule so that the jobs would be executed one by one
		ISchedulingRule rule = new IdentityRule();
		
		for(int i = 0; i < NUM_JOBS; i++) {
			//assign four jobs to each family
			if(i%5 == 0)
				jobs[i] = new CustomTestJob("TestFirstFamily", 1000000, 10, TestJobFamily.TYPE_ONE);
			else if(i%5 == 1)
				jobs[i] = new CustomTestJob("TestSecondFamily", 1000000, 10, TestJobFamily.TYPE_TWO);
			else if(i%5 == 2)
				jobs[i] = new CustomTestJob("TestThirdFamily", 1000000, 10, TestJobFamily.TYPE_THREE);
			else if(i%5 == 3)
				jobs[i] = new CustomTestJob("TestFourthFamily", 1000000, 10, TestJobFamily.TYPE_FOUR);
			else /*if(i%5 == 4)*/
				jobs[i] = new CustomTestJob("TestFifthFamily", 1000000, 10, TestJobFamily.TYPE_FIVE);
				
			jobs[i].setRule(rule);
			jobs[i].schedule();
		}
		
		waitForStart(jobs[0]);
			
		//try finding all jobs by supplying the NULL parameter
		//note that this might find other jobs that are running as a side-effect of the test
		//suites running, such as snapshot
		HashSet allJobs = new HashSet();
		allJobs.addAll(Arrays.asList(jobs));
		Job [] result = manager.find(null);
		assertTrue("1.0", result.length >= NUM_JOBS);
		for(int i = 0; i < result.length; i++) {
			//only test jobs that we know about
			if (allJobs.remove(result[i]))
				assertTrue("1." + i, (result[i].belongsTo(first) || result[i].belongsTo(second) || result[i].belongsTo(third) || 
						result[i].belongsTo(fourth) || result[i].belongsTo(fifth)));
		}
		assertEquals("1.2", 0, allJobs.size());
		
		//try finding all jobs from the first family
		result = manager.find(first);
		assertTrue("2.0", result.length == 4);
		for(int i = 0; i < result.length; i++) {
				assertTrue("2." +(i+1), result[i].belongsTo(first));
		}
		
		//try finding all jobs from the second family
		result = manager.find(second);
		assertTrue("3.0", result.length == 4);
		for(int i = 0; i < result.length; i++) {
				assertTrue("3." +(i+1), result[i].belongsTo(second));
		}
		
		//try finding all jobs from the third family
		result = manager.find(third);
		assertTrue("4.0", result.length == 4);
		for(int i = 0; i < result.length; i++) {
				assertTrue("4." +(i+1), result[i].belongsTo(third));
		}
		
		//try finding all jobs from the fourth family
		result = manager.find(fourth);
		assertTrue("5.0", result.length == 4);
		for(int i = 0; i < result.length; i++) {
				assertTrue("5." +(i+1), result[i].belongsTo(fourth));
		}
		
		//try finding all jobs from the fifth family
		result = manager.find(fifth);
		assertTrue("6.0", result.length == 4);
		for(int i = 0; i < result.length; i++) {
				assertTrue("6." +(i+1), result[i].belongsTo(fifth));
		}
			
		//the first job should still be running
		assertState("7.0", jobs[0], Job.RUNNING);
		
		//put the second family of jobs to sleep
		manager.sleep(second);
		
		//cancel the first family of jobs
		manager.cancel(first);
				
		//the third job should start running
		waitForStart(jobs[2]);
		assertState("7.1", jobs[2], Job.RUNNING);
		
		//finding all jobs from the first family should return an empty array
		result = manager.find(first);
		assertTrue("7.2", result.length == 0);
		
		//finding all jobs from the second family should return all the jobs (they are just sleeping)
		result = manager.find(second);
		assertTrue("8.0", result.length == 4);
		for(int i = 0; i < result.length; i++) {
				assertTrue("8." +(i+1), result[i].belongsTo(second));
		}
		
		//cancel the second family of jobs
		manager.cancel(second);
		//finding all jobs from the second family should now return an empty array
		result = manager.find(second);
		assertTrue("9.0", result.length == 0);
		
		//cancel the fourth family of jobs
		manager.cancel(fourth);
		//finding all jobs from the fourth family should now return an empty array
		result = manager.find(fourth);
		assertTrue("9.1", result.length == 0);
		
		//put the third family of jobs to sleep
		manager.sleep(third);
		//the first job from the third family should still be running
		assertState("9.2", jobs[2], Job.RUNNING);
		//wake up the last job from the third family
		jobs[NUM_JOBS-3].wakeUp();
		//it should now be in the WAITING state
		assertState("9.3", jobs[NUM_JOBS-3], Job.WAITING);
		
		//finding all jobs from the third family should return all 4 jobs (1 is running, 1 is waiting, 2 are sleeping)
		result = manager.find(third);
		assertTrue("10.0", result.length == 4);
		for(int i = 0; i < result.length; i++) {
				assertTrue("10." +(i+1), result[i].belongsTo(third));
		}
		
		//finding all jobs by supplying the NULL parameter should return 8 jobs (4 from the 3rd family, and 4 from the 5th family)
		result = manager.find(null);
		assertTrue("11.0", result.length == 8);
		for(int i = 0; i < result.length; i++) {
				assertTrue("11." +(i+1), (result[i].belongsTo(third) || result[i].belongsTo(fifth)));
		}
		
		//cancel the fifth family of jobs
		manager.cancel(fifth);
		//cancel the third family of jobs
		manager.cancel(third);
		waitForCancel(jobs[2]);
		
		//all jobs should now be in the NONE state		
		for(int i = 0; i < NUM_JOBS; i++) {
			assertState("12." + i, jobs[i], Job.NONE);
		}
		
		//finding all jobs should return an empty array
		result = manager.find(null);
		assertTrue("13.0", result.length == 0);
	}
	
	public void testJobFamilyNULL() {
		//test methods that accept the null job family (ie. all jobs)
		final int NUM_JOBS = 20;
		Job [] jobs = new Job[NUM_JOBS];
		//create two different families of jobs
		TestJobFamily first = new TestJobFamily(TestJobFamily.TYPE_ONE);
		TestJobFamily second = new TestJobFamily(TestJobFamily.TYPE_TWO);
		//need one common scheduling rule so that the jobs would be executed one by one
		ISchedulingRule rule = new IdentityRule();
		for(int i = 0; i < NUM_JOBS; i++) {
			//assign half the jobs to the first family, the other half to the second family
			if(i%2 == 0)
				jobs[i] = new CustomTestJob("TestFirstFamily", 1000000, 10, TestJobFamily.TYPE_ONE);
			else /*if(i%2 == 1)*/
				jobs[i] = new CustomTestJob("TestSecondFamily", 1000000, 10, TestJobFamily.TYPE_TWO);
				
			jobs[i].setRule(rule);
			jobs[i].schedule();
		}
				
		waitForStart(jobs[0]);
		assertState("1.0", jobs[0], Job.RUNNING);
		
		//put all jobs to sleep
		manager.sleep(null);
		//the first job should still be running
		assertState("2.0", jobs[0], Job.RUNNING);
		
		//all the other jobs should be sleeping
		for(int i = 1; i < NUM_JOBS; i++) {
			assertState("2." + i, jobs[i], Job.SLEEPING);
		}
		
		//wake up all the jobs
		manager.wakeUp(null);
		//the first job should still be running
		assertState("3.0", jobs[0], Job.RUNNING);
		
		//all the other jobs should be waiting
		for(int i = 1; i < NUM_JOBS; i++) {
			assertState("3." + i, jobs[i], Job.WAITING);
		}
		
		//cancel all the jobs
		manager.cancel(null);
		waitForCancel(jobs[0]);
				
		//all the jobs should now be in the NONE state
		for(int i = 0; i < NUM_JOBS; i++) {
			assertState("4." + i, jobs[i], Job.NONE);
		}
						
	}
	
	public void testJobFamilyJoin() {
		//test the join method on a family of jobs
		final int[] status = new int[1];
		status[0] = StatusChecker.STATUS_WAIT_FOR_START;
		final int NUM_JOBS = 20;
		Job [] jobs = new Job[NUM_JOBS];
		//create two different families of jobs
		final TestJobFamily first = new TestJobFamily(TestJobFamily.TYPE_ONE);
		final TestJobFamily second = new TestJobFamily(TestJobFamily.TYPE_TWO);
		//need two scheduling rule so that jobs in each family would be executing one by one
		ISchedulingRule rule1 = new IdentityRule();
		ISchedulingRule rule2 = new IdentityRule();
		for(int i = 0; i < NUM_JOBS; i++) {
			//assign half the jobs to the first family, the other half to the second family
			if(i%2 == 0) {
				jobs[i] = new CustomTestJob("TestFirstFamily", 10, 10, TestJobFamily.TYPE_ONE);
				jobs[i].setRule(rule1);
				jobs[i].schedule(1000000);
			}
			else /*if(i%2 == 1)*/ {
				jobs[i] = new CustomTestJob("TestSecondFamily", 1000000, 10, TestJobFamily.TYPE_TWO);
				jobs[i].setRule(rule2);
				jobs[i].schedule();
			}	
			
			
		}
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				status[0] = StatusChecker.STATUS_START;
				try {
					StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_WAIT_FOR_RUN, 100);
					status[0] = StatusChecker.STATUS_RUNNING;
					manager.join(first, null);
				} catch (OperationCanceledException e) {
					
				} catch (InterruptedException e) {
					
				}
				status[0] = StatusChecker.STATUS_DONE;
				
			}
		});
		
		//start the thread that will join the first family of jobs and be blocked until they finish execution		
		t.start();
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_START, 100);
		status[0] = StatusChecker.STATUS_WAIT_FOR_RUN;
		//wake up the first family of jobs
		manager.wakeUp(first);
				
		int i = 0;
		for (; i < 100; i++) {
			Job[] result = manager.find(first);
			
			if (status[0] == StatusChecker.STATUS_DONE)
				break;
			
			//the thread is either blocked, or it is done
			assertTrue("2." + i, ((result.length > 0) || (status[0] == StatusChecker.STATUS_RUNNING)));
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			}
		}
		assertTrue("2.0", i < 100);
		
		//cancel the second family of jobs
		manager.cancel(second);
		waitForCancel(jobs[1]);
		
		//all the jobs should now be in the NONE state
		for(int j = 0; j < NUM_JOBS; j++) {
			assertState("3." + j, jobs[j], Job.NONE);
		}
	}
	
	public void testJobFamilyJoinCancelManager() {
		//test the join method on a family of jobs, then cancel the call
		final int[] status = new int[1];
		status[0] = StatusChecker.STATUS_WAIT_FOR_START;
		final int NUM_JOBS = 20;
		Job [] jobs = new Job[NUM_JOBS];
		//create a progress monitor to cancel the join call
		final IProgressMonitor canceller = new FussyProgressMonitor();
		//create two different families of jobs
		final TestJobFamily first = new TestJobFamily(TestJobFamily.TYPE_ONE);
		final TestJobFamily second = new TestJobFamily(TestJobFamily.TYPE_TWO);
		//need two scheduling rule so that jobs in each family would be executing one by one
		ISchedulingRule rule1 = new IdentityRule();
		ISchedulingRule rule2 = new IdentityRule();
		for(int i = 0; i < NUM_JOBS; i++) {
			//assign half the jobs to the first family, the other half to the second family
			if(i%2 == 0) {
				jobs[i] = new CustomTestJob("TestFirstFamily", 1000000, 10, TestJobFamily.TYPE_ONE);
				jobs[i].setRule(rule1);
			}
			else /*if(i%2 == 1)*/ {
				jobs[i] = new CustomTestJob("TestSecondFamily", 1000000, 10, TestJobFamily.TYPE_TWO);
				jobs[i].setRule(rule2);
			}	
			jobs[i].schedule();
			
		}
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				status[0] = StatusChecker.STATUS_START;
				try {
					StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_WAIT_FOR_RUN, 100);
					status[0] = StatusChecker.STATUS_RUNNING;
					manager.join(first, canceller);
					
				} catch (OperationCanceledException e) {
					
				} catch (InterruptedException e) {
					
				}
				status[0] = StatusChecker.STATUS_DONE;
			}
		});
		
		//start the thread that will join the first family of jobs
		//it will be blocked until the cancel call is made to the thread
		t.start();
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_START, 100);
		status[0] = StatusChecker.STATUS_WAIT_FOR_RUN;		
		waitForStart(jobs[0]);
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_RUNNING, 100);
				
		assertState("2.0", jobs[0], Job.RUNNING);
		assertTrue("2.1", status[0] == StatusChecker.STATUS_RUNNING);
		
		//cancel the monitor that is attached to the join call
		canceller.setCanceled(true);
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_DONE, 100);
		
		//the first job in the first family should still be running
		assertState("2.2", jobs[0], Job.RUNNING);
		assertTrue("2.3", status[0] == StatusChecker.STATUS_DONE);
		assertTrue("2.4", manager.find(first).length > 0);
		
		//cancel the second family of jobs
		manager.cancel(second);
		waitForCancel(jobs[1]);
		
		//cancel the first family of jobs
		manager.cancel(first);
		waitForCancel(jobs[0]);
				
		//all the jobs should now be in the NONE state
		for(int j = 0; j < NUM_JOBS; j++) {
			assertState("3." + j, jobs[j], Job.NONE);
		}
	}
	
	public void testJobFamilyJoinCancelJobs() {
		//test the join method on a family of jobs, then cancel the jobs that are blocking the join call
		final int[] status = new int[1];
		status[0] = StatusChecker.STATUS_WAIT_FOR_START;
		final int NUM_JOBS = 20;
		Job [] jobs = new Job[NUM_JOBS];
		//create two different families of jobs
		final TestJobFamily first = new TestJobFamily(TestJobFamily.TYPE_ONE);
		final TestJobFamily second = new TestJobFamily(TestJobFamily.TYPE_TWO);
		//need two scheduling rule so that jobs in each family would be executing one by one
		ISchedulingRule rule1 = new IdentityRule();
		ISchedulingRule rule2 = new IdentityRule();
		for(int i = 0; i < NUM_JOBS; i++) {
			//assign half the jobs to the first family, the other half to the second family
			if(i%2 == 0) {
				jobs[i] = new CustomTestJob("TestFirstFamily", 1000000, 10, TestJobFamily.TYPE_ONE);
				jobs[i].setRule(rule1);
			}
			else /*if(i%2 == 1)*/ {
				jobs[i] = new CustomTestJob("TestSecondFamily", 1000000, 10, TestJobFamily.TYPE_TWO);
				jobs[i].setRule(rule2);
			}	
			jobs[i].schedule();
			
		}
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				status[0] = StatusChecker.STATUS_START;
				try {
					StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_WAIT_FOR_RUN, 100);
					status[0] = StatusChecker.STATUS_RUNNING;
					manager.join(first, null);
					
				} catch (OperationCanceledException e) {
					
				} catch (InterruptedException e) {
					
				}
				status[0] = StatusChecker.STATUS_DONE;
			}
		});
		
		//start the thread that will join the first family of jobs
		//it will be blocked until the all jobs in the first family finish execution or are cancelled
		t.start();
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_START, 100);
		status[0] = StatusChecker.STATUS_WAIT_FOR_RUN;		
		waitForStart(jobs[0]);
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_RUNNING, 100);		
			
		assertState("2.0", jobs[0], Job.RUNNING);
		assertTrue("2.1", status[0] == StatusChecker.STATUS_RUNNING);
		
		//cancel the first family of jobs
		//the join call should be unblocked when all the jobs are cancelled
		manager.cancel(first);
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_DONE, 100);
		
		//all jobs in the first family should be removed from the manager
		assertTrue("2.2", manager.find(first).length == 0);
		
		//cancel the second family of jobs
		manager.cancel(second);
		waitForCancel(jobs[1]);
		
		//all the jobs should now be in the NONE state
		for(int j = 0; j < NUM_JOBS; j++) {
			assertState("3." + j, jobs[j], Job.NONE);
		}
	}	

	public void testJobFamilyJoinSimple() {
		//test the join method on a family of jobs that is empty
		final int[] status = new int[1];
		status[0] = StatusChecker.STATUS_WAIT_FOR_START;
		final int NUM_JOBS = 20;
		Job [] jobs = new Job[NUM_JOBS];
		//create three different families of jobs
		final TestJobFamily first = new TestJobFamily(TestJobFamily.TYPE_ONE);
		final TestJobFamily second = new TestJobFamily(TestJobFamily.TYPE_TWO);
		final TestJobFamily third = new TestJobFamily(TestJobFamily.TYPE_THREE);
		//need two scheduling rule so that jobs in each family would be executing one by one
		ISchedulingRule rule1 = new IdentityRule();
		ISchedulingRule rule2 = new IdentityRule();
		for(int i = 0; i < NUM_JOBS; i++) {
			//assign half the jobs to the first family, the other half to the second family
			if(i%2 == 0) {
				jobs[i] = new CustomTestJob("TestFirstFamily", 1000000, 10, TestJobFamily.TYPE_ONE);
				jobs[i].setRule(rule1);
			}
			else /*if(i%2 == 1)*/ {
				jobs[i] = new CustomTestJob("TestSecondFamily", 1000000, 10, TestJobFamily.TYPE_TWO);
				jobs[i].setRule(rule2);
			}	
			
			jobs[i].schedule();
		}
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				status[0] = StatusChecker.STATUS_START;
				try {
					StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_WAIT_FOR_RUN, 100);
					status[0] = StatusChecker.STATUS_RUNNING;
					manager.join(third, null);
					
				} catch (OperationCanceledException e) {
					
				} catch (InterruptedException e) {
					
				}
				
				status[0] = StatusChecker.STATUS_DONE;
				
			}
		});
		
		//try joining the third family of jobs, which is empty
		//join method should return without blocking
		waitForStart(jobs[0]);
		t.start();
		
		//let the thread execute the join call
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_START, 100);
		assertTrue("1.0", status[0] == StatusChecker.STATUS_START);
		long startTime = System.currentTimeMillis();
		status[0] = StatusChecker.STATUS_WAIT_FOR_RUN;
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_DONE, 100);
		long endTime = 	System.currentTimeMillis();	

		assertTrue("2.0", status[0] == StatusChecker.STATUS_DONE);
		assertTrue("2.1", endTime > startTime);		

		//the join call should take no actual time (join call should not block thread at all)
		if(PEDANTIC)
			assertTrue("2.2 start time: " + startTime + " end time: " + endTime , (endTime-startTime) < 300);		

		//cancel all jobs
		manager.cancel(null);
		waitForCancel(jobs[0]);
		waitForCancel(jobs[1]);
		
		//all the jobs should now be in the NONE state
		for(int j = 0; j < NUM_JOBS; j++) {
			assertState("3." + j, jobs[j], Job.NONE);
		}
	}
	
	public void testJobFamilyWakeUp() {
		//test the wake-up of a family of jobs
		final int NUM_JOBS = 20;
		Job [] jobs = new Job[NUM_JOBS];
		//create two different families of jobs
		TestJobFamily first = new TestJobFamily(TestJobFamily.TYPE_ONE);
		TestJobFamily second = new TestJobFamily(TestJobFamily.TYPE_TWO);
		//need one common scheduling rule so that the jobs would be executed one by one
		ISchedulingRule rule = new IdentityRule();
		for(int i = 0; i < NUM_JOBS; i++) {
			//assign half the jobs to the first family, the other half to the second family
			if(i%2 == 0)
				jobs[i] = new CustomTestJob("TestFirstFamily", 1000000, 10, TestJobFamily.TYPE_ONE);
			else /*if(i%2 == 1)*/
				jobs[i] = new CustomTestJob("TestSecondFamily", 1000000, 10, TestJobFamily.TYPE_TWO);
				
			jobs[i].setRule(rule);
			jobs[i].schedule();
		}
				
		waitForStart(jobs[0]);
		assertState("1.0", jobs[0], Job.RUNNING);
		
		//first job is running, the rest are waiting so put them to sleep
		for(int i = 1; i < NUM_JOBS; i++) {
			assertState("1." + i, jobs[i], Job.WAITING);
			assertTrue("2." + i, jobs[i].sleep());
		}
		
		//cancel the first job
		jobs[0].cancel();
		waitForCancel(jobs[0]);
		assertState("3.0", jobs[0], Job.NONE);
		
		//all jobs should be sleeping now
		for(int i = 1; i < NUM_JOBS; i++) {
			assertState("3." + i, jobs[i], Job.SLEEPING);
		}
		
		//wake-up the second family of jobs
		manager.wakeUp(second);
		waitForStart(jobs[1]);
		assertState("4.0", jobs[1], Job.RUNNING);
		
		//all other jobs in the second family should be in the waiting state
		//jobs in the first family should still be in the sleep state
		for(int i = 2; i < NUM_JOBS; i++) {
			if(jobs[i].belongsTo(first))
				assertState("4." + i, jobs[i], Job.SLEEPING);
			else
				assertState("4." + i, jobs[i], Job.WAITING);
		}
		
		//cycle through the jobs in the second family
		//canceling one of them should start the next one
		for(int i = 1; i < NUM_JOBS-2; i+=2) {
			jobs[i].cancel();
			waitForStart(jobs[i+2]);
			assertState("5." + i, jobs[i+2], Job.RUNNING);
		}
		
		jobs[NUM_JOBS-1].cancel();
			
		//all jobs in the first family should be sleeping
		for(int i = 2; i < NUM_JOBS; i+=2) {
			assertState("6." + i, jobs[i], Job.SLEEPING);
		}
		
		//wake up the first family
		manager.wakeUp(first);
		
		waitForStart(jobs[2]);
		//next job in the family should be running
		assertState("7.0", jobs[2], Job.RUNNING);	
		
		//cycle through the jobs in the first family
		//canceling one of them should start the next one
		for(int i = 2; i < NUM_JOBS-3; i+=2) {
			jobs[i].cancel();
			waitForStart(jobs[i+2]);
			assertState("7." + i, jobs[i+2], Job.RUNNING);
		}
		
		jobs[NUM_JOBS-2].cancel();
		waitForCancel(jobs[NUM_JOBS-2]);
		
		//all jobs should now be in the NONE state		
		for(int i = 0; i < NUM_JOBS; i++) {
			assertState("7." + i, jobs[i], Job.NONE);
		}
	}
	
	public void testJobFamilySleep() {
		//test the sleep method on a family of jobs
		final int NUM_JOBS = 20;
		Job [] jobs = new Job[NUM_JOBS];
		//create two different families of jobs
		TestJobFamily first = new TestJobFamily(TestJobFamily.TYPE_ONE);
		TestJobFamily second = new TestJobFamily(TestJobFamily.TYPE_TWO);
		//need a common scheduling rule so that the jobs would be executed one by one
		ISchedulingRule rule = new IdentityRule();
		for(int i = 0; i < NUM_JOBS; i++) {
			//assign half the jobs to the first family, the other half to the second family
			if(i%2 == 0)
				jobs[i] = new CustomTestJob("TestFirstFamily", 1000000, 10, TestJobFamily.TYPE_ONE);
			else /*if(i%2 == 1)*/
				jobs[i] = new CustomTestJob("TestSecondFamily", 1000000, 10, TestJobFamily.TYPE_TWO);
				
			jobs[i].setRule(rule);
			jobs[i].schedule();
		}
				
		waitForStart(jobs[0]);
		
		assertState("1.0", jobs[0], Job.RUNNING);
		
		//first job is running, the rest are waiting
		for(int i = 1; i < NUM_JOBS; i++) {
			assertState("1." + i, jobs[i], Job.WAITING);
		}
		
		//set the first family of jobs to sleep	
		manager.sleep(first);
		
		//the running job should still be running
		assertState("2.0", jobs[0], Job.RUNNING);
				
		for(int i = 1; i < NUM_JOBS; i++) {
			//all other jobs in the first family should be sleeping
			//they can now be cancelled
			if(jobs[i].belongsTo(first)) {
				assertState("2." + i, jobs[i], Job.SLEEPING);
				jobs[i].cancel();
			}
			//all jobs in the second family should still be in the waiting queue
			else {
				assertState("3." + i, jobs[i], Job.WAITING);
			}
		}
		
		manager.sleep(second);
		//cancel the running job
		jobs[0].cancel();
		waitForCancel(jobs[0]);
		
		//no job should now be running
		assertTrue("4.0", manager.currentJob()==null);
		
		for(int i = 1; i < NUM_JOBS; i++) {
			//all other jobs in the second family should be sleeping
			//they can now be cancelled
			if(jobs[i].belongsTo(second)) {
				assertState("4." + i, jobs[i], Job.SLEEPING);
				jobs[i].cancel();
			}
		}
		
		//all the jobs should now be in the NONE state
		for(int i = 0; i < NUM_JOBS; i++) {
			assertState("5." + i, jobs[i], Job.NONE);
		}
	}
	
	/**
	 * A job has been cancelled.  Pause this thread so that a worker thread
	 * has a chance to receive the cancel event.
	 */
	private void waitForCancel(Job job) {
		int i = 0;
		while (job.getState() == Job.RUNNING) {
			Thread.yield();
			sleep(100);
			Thread.yield();
			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for job to cancel", i++ < 1000);
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