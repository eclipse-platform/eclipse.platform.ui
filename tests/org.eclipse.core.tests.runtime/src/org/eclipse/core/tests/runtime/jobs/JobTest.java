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

import org.eclipse.core.internal.jobs.Worker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import junit.framework.TestCase;

/**
 * Tests the implemented get/set methods of the abstract class Job
 */
public class JobTest extends TestCase {
	Job shortJob;
	Job longJob;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		shortJob = new TestJob("Short Test Job", 100, 10);
		longJob = new TestJob("Long Test Job", 1000000, 10);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetName() {
		assertTrue("1.0", shortJob.getName().equals("Short Test Job"));
		assertTrue("1.1", longJob.getName().equals("Long Test Job"));
		
		
		//try creating a job with a null name
		try {
			Job test = new TestJob(null);
			fail("2.0");
		} catch (RuntimeException e) {
			//should fail
		}
	}

	public void testGetPriority() {
		//set priorities to all allowed options
		//check if getPriority() returns proper result
		
		int [] priority = {Job.SHORT, Job.LONG, Job.INTERACTIVE, Job.BUILD, Job.DECORATE};
		
		for(int i = 0; i < priority.length; i++) {
			shortJob.setPriority(priority[i]);
			assertTrue("1." + (i+1), shortJob.getPriority() == priority[i]);
		}
	}

	public void testGetResult() {
		//execute the job several times and check if get result returns the result of job execution
		//execute a short job
		assertTrue("1.0", shortJob.getResult() == null);
		shortJob.schedule();
		waitForEnd(shortJob);
		assertTrue("1.1", shortJob.getResult().getSeverity() == IStatus.OK);
		
		//cancel a long job
		longJob.schedule(1000000);
		assertTrue("1.3", longJob.sleep());
		longJob.wakeUp();
		waitForStart(longJob);
		longJob.cancel();
		waitForEnd(longJob);
		assertTrue("2.0", longJob.getResult().getSeverity() == IStatus.CANCEL);
	}

	public void testGetRule() {
		//set several rules for the job, check if getRule returns the rule that was set
		//no rule was set yet
		assertTrue("1.0", shortJob.getRule() == null);
		
		shortJob.setRule(new IdentityRule());
		assertTrue("1.1", (shortJob.getRule() instanceof IdentityRule));
		
		ISchedulingRule rule = new RuleSetA();
		shortJob.setRule(rule);
		assertTrue("1.2", shortJob.getRule() == rule);
		
		shortJob.setRule(null);
		assertTrue("1.3", shortJob.getRule() == null);
	}

	public void testGetThread() {
		//check that getThread returns the thread that was passed in setThread, when the job is not running
		//if the job is scheduled, only jobs that return the asynch_exec status will run in the indicated thread
		
		//main is not running now
		assertTrue("1.0", shortJob.getThread() == null);
		
		Thread t = new Thread();
		shortJob.setThread(t);
		assertTrue("1.1", shortJob.getThread() == t);
		
		shortJob.setThread(new Thread());
		assertTrue("1.2", shortJob.getThread() instanceof Thread);
		
		shortJob.setThread(null);
		assertTrue("1.3", shortJob.getThread() == null);
	}

	public void testIsSystem() {
		//reset the system parameter several times
		assertTrue("1.0", !shortJob.isSystem());
		shortJob.setSystem(true);
		assertTrue("1.1", shortJob.isSystem());
		shortJob.setSystem(false);
		assertTrue("1.2", !shortJob.isSystem());
	}

	//see bug #43458
	public void _testSetPriority() {
		int [] wrongPriority = {1000, -Job.DECORATE, 25, 0, 5, Job.INTERACTIVE - Job.BUILD};
		
		for(int i = 0; i < wrongPriority.length; i++) {
			//set priority to non-existent type
			try {
				shortJob.setPriority(wrongPriority[i]);
				fail("1." + (i+1));
			} catch (RuntimeException e) {
				//should fail
			}
		}
	}

	//see bug #43459
	public void _testSetRule() {
		//setting a scheduling rule for a job after it was already scheduled should throw an exception
		shortJob.setRule(new IdentityRule());
		assertTrue("1.0", shortJob.getRule() instanceof IdentityRule);
		shortJob.schedule(1000000);
		try {
			shortJob.setRule(new RuleSetA());
			fail("1.1");
		} catch (RuntimeException e) {
			//should fail
		}
		
		//wake up the sleeping job
		shortJob.wakeUp();
		
		while(shortJob.getState() != Job.NONE) {
			try {
				shortJob.setRule(new RuleSetB());
				fail("2.0");
			}  catch (RuntimeException e1) {
				//should fail
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				
				}
			}
		}
		
		//after the job has finished executing, the scheduling rule for it can once again be reset
		shortJob.setRule(new RuleSetD());
		assertTrue("1.2", shortJob.getRule() instanceof RuleSetD);
		shortJob.setRule(null);
		assertTrue("1.3", shortJob.getRule() == null);
	}

	public void testSetThread() {
		//setting the thread of a job that is not an asynchronous job should not affect the actual thread the job will run in
		assertTrue("0.0", longJob.getThread() == null);
		
		longJob.setThread(Thread.currentThread());
		assertTrue("1.0", longJob.getThread() == Thread.currentThread());
		longJob.schedule();
		waitForStart(longJob);
		
		//the setThread method should have no effect on jobs that execute normally
		assertTrue("2.0", longJob.getThread() != Thread.currentThread());
		
		longJob.cancel();
		waitForEnd(longJob);
		
		//the thread should reset to null when the job finishes execution
		assertTrue("3.0", longJob.getThread() == null);
		
		longJob.setThread(null);
		assertTrue("4.0", longJob.getThread() == null);
		
		longJob.schedule();
		waitForStart(longJob);
		
		//the thread that the job is executing in is not the one that was set
		assertTrue("5.0", longJob.getThread() != null);
		longJob.cancel();
		waitForEnd(longJob);
		
		//thread should reset to null after execution of job
		assertTrue("6.0", longJob.getThread() == null);
		
		Thread t = new Thread();
		longJob.setThread(t);
		assertTrue("7.0", longJob.getThread() == t);
		longJob.schedule();
		waitForStart(longJob);
		
		//the thread that the job is executing in is not the one that it was set to
		assertTrue("8.0", longJob.getThread() != t);
		longJob.cancel();
		waitForEnd(longJob);
		
		//execution thread should reset to null after job is finished
		assertTrue("9.0", longJob.getThread() == null);
	}
	
	//see bug #43591
	public void _testDone() {
		//calling the done method on a job that is not executing asynchronously should have no effect
		
		shortJob.done(Status.OK_STATUS);
		assertTrue("1.0", shortJob.getResult() == null);
		
		shortJob.done(Status.CANCEL_STATUS);
		assertTrue("2.0", shortJob.getResult() == null);
		
		//calling the done method after the job is scheduled
		shortJob.schedule();
		shortJob.done(Status.CANCEL_STATUS);
		waitForEnd(shortJob);

		//the done call should be ignored, and the job should finish execution normally
		assertTrue("3.0", shortJob.getResult().getSeverity() == IStatus.OK);
		
		shortJob.done(Status.CANCEL_STATUS);
		assertTrue("4.0", shortJob.getResult().getSeverity() == IStatus.OK);
		
		//calling the done method before a job is cancelled
		longJob.schedule();
		waitForStart(longJob);
		longJob.done(Status.OK_STATUS);
		longJob.cancel();
		waitForEnd(longJob);
		
		//the done call should be ignored, and the job status should still be cancelled
		assertTrue("5.0", longJob.getResult().getSeverity() == IStatus.CANCEL);
		
		longJob.done(Status.OK_STATUS);
		assertTrue("6.0", longJob.getResult().getSeverity() == IStatus.CANCEL);
		
	}
	
	//see bug #43566
	public void testAsynchJob() {
		final int [] status = {StatusChecker.STATUS_WAIT_FOR_START};
				
		//execute a job asynchronously and check the result
		AsynchTestJob main = new AsynchTestJob("Test Asynch Finish", status, 0);
				
		assertTrue("1.0", main.getThread() == null);
		assertTrue("2.0", main.getResult() == null);
		//schedule the job to run
		main.schedule();
		waitForStart(main);
		assertTrue("3.0", main.getState() == Job.RUNNING);
		//the asynchronous process that assigns the thread the job is going to run in has not been started yet
		//the job is running in the thread provided to it by the manager
		assertTrue("3.1", main.getThread() instanceof Worker);
		
		status[0] = StatusChecker.STATUS_START;
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_WAIT_FOR_START, 100);
		
		//the asynchronous process has been started, but the set thread method has not been called yet
		assertTrue("3.2", main.getThread() instanceof Worker);
		
		status[0] = StatusChecker.STATUS_WAIT_FOR_RUN;
				
		//make sure the job has set the thread it is going to run in
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_RUNNING, 100);
			
		assertTrue("3.3", status[0] == StatusChecker.STATUS_RUNNING);
		assertTrue("3.4", main.getThread() instanceof AsynchExecThread);
		
		//let the job run
		status[0] = StatusChecker.STATUS_WAIT_FOR_DONE;
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_DONE, 100);
		waitForEnd(main);
		
		//after the job is finished, the thread should be reset
		assertTrue("4.0", main.getState() == Job.NONE);
		assertTrue("4.1", main.getResult().getSeverity() == IStatus.OK);
		assertTrue("4.2", main.getThread() == null);
		
		//reset status
		status[0] = StatusChecker.STATUS_WAIT_FOR_START;
		
		//schedule the job to run again
		main.schedule();
		waitForStart(main);
		assertTrue("5.0", main.getState() == Job.RUNNING);
		
		//the asynchronous process that assigns the thread the job is going to run in has not been started yet
		//job is running in the thread provided by the manager
		assertTrue("5.1", main.getThread() instanceof Worker);
		
		status[0] = StatusChecker.STATUS_START;
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_WAIT_FOR_START, 100);
		
		//the asynchronous process has been started, but the set thread method has not been called yet
		assertTrue("5.2", main.getThread() instanceof Worker);
		
		status[0] = StatusChecker.STATUS_WAIT_FOR_RUN;
		
		//make sure the job has set the thread it is going to run in
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_RUNNING, 100);
		
		assertTrue("5.3",status[0] == StatusChecker.STATUS_RUNNING);
		assertTrue("5.4", main.getThread() instanceof AsynchExecThread);
		
		//cancel the job, then let the job get the cancellation request
		main.cancel();
		status[0] = StatusChecker.STATUS_WAIT_FOR_DONE;
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_DONE, 100);
		waitForEnd(main);
		
		//thread should be reset to null after cancellation
		assertTrue("6.0", main.getState() == Job.NONE);
		assertTrue("6.1", main.getResult().getSeverity() == IStatus.CANCEL);
		assertTrue("6.2", main.getThread() == null);		
	}
	
	public void testAsynchJobComplex() {
		final int [] status = {StatusChecker.STATUS_WAIT_FOR_START, 
								StatusChecker.STATUS_WAIT_FOR_START,
								StatusChecker.STATUS_WAIT_FOR_START,
								StatusChecker.STATUS_WAIT_FOR_START,
								StatusChecker.STATUS_WAIT_FOR_START};
				
		//test the interaction of several asynchronous jobs
		AsynchTestJob[] jobs = new AsynchTestJob[5];
		
		for(int i = 0; i < jobs.length; i++) {
			jobs[i] = new AsynchTestJob("TestJob" + (i+1), status, i);
			assertTrue("1." + i, jobs[i].getThread() == null);
			assertTrue("2." + i, jobs[i].getResult() == null);
			jobs[i].schedule();
			//status[i] = StatusChecker.STATUS_START;
		}
		//all the jobs should be running at the same time
		waitForStart(jobs[4]);
		
		//every job should now be waiting for the STATUS_START flag
		for(int i = 0; i < status.length; i++) {
			assertTrue("3." + i, jobs[i].getState() == Job.RUNNING);
			assertTrue("4." + i, jobs[i].getThread() instanceof Worker);
			status[i] = StatusChecker.STATUS_START;
		}
		
		//all the jobs should be running at the same time
		//by the time the last job changes the status flag, the other jobs should have already done so
		//for(int i = 0; i < status.length; i++) {
		StatusChecker.waitForStatus(status, 4, StatusChecker.STATUS_WAIT_FOR_START, 100);
		//}
		
		//every job should now be waiting for the STATUS_WAIT_FOR_RUN flag
		for(int i = 0; i < status.length; i++) {
			assertTrue("5. " + i, jobs[i].getThread() instanceof Worker);
			status[i] = StatusChecker.STATUS_WAIT_FOR_RUN;
		}
		
		//all the jobs should be running at the same time
		//by the time the last job changes the status flag, the other jobs should have already done so
		//for(int i = 0; i < status.length; i++) {
		StatusChecker.waitForStatus(status, 4, StatusChecker.STATUS_RUNNING, 100);
		//}
		
		//let the jobs execute
		for(int i = 0; i < status.length; i++) {
			assertTrue("6. " + i, jobs[i].getThread() instanceof AsynchExecThread);
			status[i] = StatusChecker.STATUS_WAIT_FOR_DONE;
		}
		
		//all jobs should run at the same time
		//by the time the last one finishes, the rest should already be done
		StatusChecker.waitForStatus(status, 4, StatusChecker.STATUS_DONE, 100);
		
		//the status for every job should be STATUS_OK
		//the threads should have been reset to null
		for(int i = 0; i < status.length; i++) {
			assertTrue("7." + i, status[i] == StatusChecker.STATUS_DONE);
			assertTrue("8." + i, jobs[i].getState() == Job.NONE);
			assertTrue("9." + i, jobs[i].getResult().getSeverity() == Status.OK);
			assertTrue("10." + i, jobs[i].getThread() == null);
		}
		
	}

	public void testAsynchJobConflict() {
		final int [] status = {StatusChecker.STATUS_WAIT_FOR_START, 
								StatusChecker.STATUS_WAIT_FOR_START,
								StatusChecker.STATUS_WAIT_FOR_START,
								StatusChecker.STATUS_WAIT_FOR_START,
								StatusChecker.STATUS_WAIT_FOR_START};
				
		//test the interaction of several asynchronous jobs when a conflicting rule is assigned to some of them
		AsynchTestJob[] jobs = new AsynchTestJob[5];
		
		ISchedulingRule rule = new IdentityRule();
		
		for(int i = 0; i < jobs.length; i++) {
			jobs[i] = new AsynchTestJob("TestJob" + (i+1), status, i);
			assertTrue("1." + i, jobs[i].getThread() == null);
			assertTrue("2." + i, jobs[i].getResult() == null);
			if(i < 2) {
				jobs[i].schedule();
			}
			else if(i > 2) {
				jobs[i].setRule(rule);
			}
			else {
				jobs[i].setRule(rule);
				jobs[i].schedule();
			}
			
		}
		//the first 3 jobs should be running at the same time
		waitForStart(jobs[2]);
		
		//these 3 jobs should be waiting for the STATUS_START flag
		for(int i = 0; i < 3; i++) {
			assertTrue("3." + i, jobs[i].getState() == Job.RUNNING);
			assertTrue("4." + i, jobs[i].getThread() instanceof Worker);
			status[i] = StatusChecker.STATUS_START;
		}
		
		//all the jobs should be running at the same time
		//by the time the third job changes the status flag, the other jobs should have already done so
		StatusChecker.waitForStatus(status, 2, StatusChecker.STATUS_WAIT_FOR_START, 100);
				
		//the 3 jobs should now be waiting for the STATUS_WAIT_FOR_RUN flag
		for(int i = 0; i < 3; i++) {
			assertTrue("5. " + i, jobs[i].getThread() instanceof Worker);
			status[i] = StatusChecker.STATUS_WAIT_FOR_RUN;
		}
		
		//the 3 jobs should be running at the same time
		//by the time the third job changes the status flag, the other jobs should have already done so
		StatusChecker.waitForStatus(status, 2, StatusChecker.STATUS_RUNNING, 100);
		
		//schedule the 2 remaining jobs
		jobs[3].schedule();
		jobs[4].schedule();
		
		//the 2 newly scheduled jobs should be waiting since they conflict with the third job
		//no threads were assigned to them yet
		assertTrue("6.1", jobs[3].getState() == Job.WAITING);
		assertTrue("6.2", jobs[3].getThread() == null);
		assertTrue("6.3", jobs[4].getState() == Job.WAITING);
		assertTrue("6.4", jobs[4].getThread() == null);
		
		//let the two non-conflicting jobs execute together
		for(int i = 0; i < 2; i++) {
			assertTrue("7. " + i, jobs[i].getThread() instanceof AsynchExecThread);
			status[i] = StatusChecker.STATUS_WAIT_FOR_DONE;
		}
		//wait until the non-conflicting jobs are done
		StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_DONE, 100);
		
		//the third job should still be in the running state
		assertTrue("8.1", jobs[2].getState() == Job.RUNNING);
		//the 2 conflicting jobs should still be in the waiting state
		assertTrue("8.2", jobs[3].getState() == Job.WAITING);
		assertTrue("8.3", jobs[4].getState() == Job.WAITING);
		
		//let the third job finish execution
		assertTrue("8.4", jobs[2].getThread() instanceof AsynchExecThread);
		status[2] = StatusChecker.STATUS_WAIT_FOR_DONE;
		
		//wait until the third job is done
		StatusChecker.waitForStatus(status, 2, StatusChecker.STATUS_DONE, 100);
		
		//the fourth job should now start running, the fifth job should still be waiting
		waitForStart(jobs[3]);
		assertTrue("9.1", jobs[3].getState() == Job.RUNNING);
		assertTrue("9.2", jobs[4].getState() == Job.WAITING);
		
		//let the fourth job run, the fifth job is still waiting
		status[3] = StatusChecker.STATUS_START;
		assertTrue("9.3", jobs[4].getState() == Job.WAITING);
		StatusChecker.waitForStatus(status, 3, StatusChecker.STATUS_WAIT_FOR_START, 100);
		status[3] = StatusChecker.STATUS_WAIT_FOR_RUN;
		assertTrue("9.4", jobs[4].getState() == Job.WAITING);
		StatusChecker.waitForStatus(status, 3, StatusChecker.STATUS_RUNNING, 100);
		assertTrue("9.5", jobs[4].getState() == Job.WAITING);
		
		//cancel the fifth job, finish the fourth job
		jobs[4].cancel();
		assertTrue("9.6", jobs[3].getThread() instanceof AsynchExecThread);
		status[3] = StatusChecker.STATUS_WAIT_FOR_DONE;
		
		//wait until the fourth job is done
		StatusChecker.waitForStatus(status, 3, StatusChecker.STATUS_DONE, 100);
				
		//the status for the first 4 jobs should be STATUS_OK
		//the threads should have been reset to null
		for(int i = 0; i < status.length - 1; i++) {
			assertTrue("10." + i, status[i] == StatusChecker.STATUS_DONE);
			assertTrue("11." + i, jobs[i].getState() == Job.NONE);
			assertTrue("12." + i, jobs[i].getResult().getSeverity() == Status.OK);
			assertTrue("13." + i, jobs[i].getThread() == null);
		}
		
		//the fifth job should have null as its status (it never finished running)
		//the thread for it should have also been reset
		assertTrue("14.1", status[4] == StatusChecker.STATUS_WAIT_FOR_START);
		assertTrue("14.2", jobs[4].getState() == Job.NONE);
		assertTrue("14.3", jobs[4].getResult() == null);
		assertTrue("14.4", jobs[4].getThread() == null);
		
	}

	/**
	 * A job has been scheduled.  Pause this thread so that a worker thread
	 * has a chance to pick up the new job.
	 */
	private void waitForStart(Job job) {
		int i = 0;
		while (job.getState() != Job.RUNNING) {
			try {
				Thread.yield();
				Thread.sleep(100);
			} catch(InterruptedException e) {
				
			}
			
			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for job to start", i++ < 1000);
		}
	}
	
	/**
	 * A job was scheduled to run.  Pause this thread so that a worker thread
	 * has a chance to finish the job
	 */
	private void waitForEnd(Job job) {
		int i = 0;
		while(job.getState() != Job.NONE) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			} 
			
			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for job to end", i++ < 1000);
		}
	}	

}
