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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import junit.framework.TestCase;

/**
 * Tests the implemented get/set methods of the abstract class Job
 */
public class JobTest extends TestCase {
	Job main;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		main = new TestJob("Testing Job Class", 100, 10);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetName() {
		assertTrue("1.0", main.getName().equals("Testing Job Class"));
		
		//try creating a job with a null name
		try {
			Job test = new TestJob(null);
			fail("1.1");
		} catch (RuntimeException e) {
			//should fail
		}
	}

	public void testGetPriority() {
		//set priorities to all allowed options
		//check if getPriority() returns proper result
		main.setPriority(Job.SHORT);
		assertTrue("1.0", main.getPriority() == Job.SHORT);
		main.setPriority(Job.LONG);
		assertTrue("1.1", main.getPriority() == Job.LONG);
		main.setPriority(Job.INTERACTIVE);
		assertTrue("1.2", main.getPriority() == Job.INTERACTIVE);
		main.setPriority(Job.BUILD);
		assertTrue("1.3", main.getPriority() == Job.BUILD);
		main.setPriority(Job.DECORATE);
		assertTrue("1.4", main.getPriority() == Job.DECORATE);
	}

	public void testGetResult() {
		//execute the job several times and check if get result returns the result of job execution
		assertTrue("1.0", main.getResult() == null);
		main.schedule();
		while(main.getState() != Job.NONE) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			} 
		}
		assertTrue("1.1", main.getResult().getSeverity() == IStatus.OK);
		main.schedule(1000);
		assertTrue("1.3", main.sleep());
		main.wakeUp();
		waitForStart(main);
		main.cancel();
		while(main.getState() != Job.NONE) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			} 
		}
		assertTrue("2.0", main.getResult().getSeverity() == IStatus.CANCEL);
	}

	public void testGetRule() {
		//set several rules for the job, check if getRule returns the rule that was set
		assertTrue("1.0", main.getRule() == null);
		main.setRule(new IdentityRule());
		assertTrue("1.1", (main.getRule() instanceof IdentityRule));
		ISchedulingRule rule = new RuleSetA();
		main.setRule(rule);
		assertTrue("1.2", main.getRule() == rule);
		main.setRule(null);
		assertTrue("1.3", main.getRule() == null);
	}

	public void testGetThread() {
		//check that getThread returns the thread that was passed in setThread, when the job is not running
		//if the job is scheduled, only jobs that return the asynch_exec status will run in the indicated thread
		
		//main is not running now
		assertTrue("1.0", main.getThread() == null);
		
		Thread t = new Thread();
		main.setThread(t);
		assertTrue("1.1", main.getThread() == t);
		
		main.setThread(new Thread());
		assertTrue("1.2", main.getThread() instanceof Thread);
		
		main.setThread(null);
		assertTrue("1.3", main.getThread() == null);
		
	}

	public void testIsSystem() {
		//reset the system parameter several times
		assertTrue("1.0", !main.isSystem());
		main.setSystem(true);
		assertTrue("1.1", main.isSystem());
		main.setSystem(false);
		assertTrue("1.2", !main.isSystem());
	}

	//see bug #43458
	public void _testSetPriority() {
		//set priority to non-existent type
		try {
			main.setPriority(1000);
			fail("1.0");
		} catch (RuntimeException e) {
			//should fail
		}
		//set priority to non-existent type
		try {
			main.setPriority(-Job.DECORATE);
			fail("1.1");
		} catch (RuntimeException e) {
			//should fail
		}
		//set priority to non-existent type
		try {
			main.setPriority(25);
			fail("1.2");
		} catch (RuntimeException e) {
			//should fail
		}
		//set priority to non-existent type
		try {
			main.setPriority(0);
			fail("1.3");
		} catch (RuntimeException e) {
			//should fail
		}
		//set priority to non-existent type
		try {
			main.setPriority(5);
			fail("1.4");
		} catch (RuntimeException e) {
			//should fail
		}
		//set priority to non-existent type
		try {
			main.setPriority(Job.INTERACTIVE - Job.BUILD);
			fail("1.5");
		} catch (RuntimeException e) {
			//should fail
		}
	}

	//see bug #43459
	public void _testSetRule() {
		//setting a scheduling rule for a job after it was already scheduled should throw an exception
		
		main.setRule(new IdentityRule());
		assertTrue("1.0", main.getRule() instanceof IdentityRule);
		main.schedule();
		try {
			main.setRule(new RuleSetA());
			fail("1.1");
		} catch (RuntimeException e) {
			//should fail
		}
		while(main.getState() != Job.NONE) {
			try {
				main.setRule(new RuleSetB());
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
		main.setRule(new RuleSetD());
		assertTrue("1.2", main.getRule() instanceof RuleSetD);
		main.setRule(null);
		assertTrue("1.3", main.getRule() == null);
	}

	public void testSetThread() {
		//setting the thread of a job that is not an asynchronous job should not affect the actual thread the job will run in
		
		assertTrue("1.0", main.getThread() == null);
		main.setThread(Thread.currentThread());
		assertTrue("2.0", main.getThread() == Thread.currentThread());
		main.schedule();
		waitForStart(main);
		
		//the setThread method should have no effect on jobs that execute normally
		assertTrue("2.0", main.getThread() != Thread.currentThread());
		
		while(main.getState() == Job.RUNNING) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			} 
		}
		
		//the thread should reset to null when the job finishes execution
		assertTrue("3.0", main.getThread() == null);
		
		main.setThread(null);
		assertTrue("4.0", main.getThread() == null);
		
		main.schedule();
		waitForStart(main);
		
		//the thread that the job is executing in is not the one that was set
		assertTrue("5.0", main.getThread() != null);
		
		while(main.getState() == Job.RUNNING) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			} 
		}
		//thread should reset to null after execution of job
		assertTrue("6.0", main.getThread() == null);
		
		Thread t = new Thread();
		main.setThread(t);
		assertTrue("4.0", main.getThread() == t);
		main.schedule();
		
		waitForStart(main);
		
		//the thread that the job is executing in is not the one that it was set to
		assertTrue("5.0", main.getThread() != t);
		
		while(main.getState() == Job.RUNNING) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			} 
		}
		
		//execution thread should reset to null after job is finished
		assertTrue("6.0", main.getThread() == null);
	}
	
	/**
	 * This test needs to be fixed.  "Main" is not a long running job, so it is not safe
	 * to call waitForStart.  Should instead use an "infinitely long" job:
	 * - schedule infinitely long job
	 * - wait for it to start
	 * - trying calling done with invalid status
	 * - cancel job
	 * - check status
	 * 
	 * Currently fails due to bug 43591.
	 */
	public void _testDone() {
		//calling the done method on a job that is not executing asynchronously should have no effect
		
		main.done(Status.OK_STATUS);
		assertTrue("1.0", main.getResult() == null);
		
		main.done(Status.CANCEL_STATUS);
		assertTrue("2.0", main.getResult() == null);
		
		main.schedule();
		waitForStart(main);
				
		while(main.getState() == Job.RUNNING) {
			try {
				main.done(Status.CANCEL_STATUS);
				Thread.sleep(100);
			} catch (InterruptedException e) {
						
			} 
		}
		
		assertTrue("3.0", main.getResult().getSeverity() == IStatus.OK);
		
		main.done(Status.CANCEL_STATUS);
		assertTrue("4.0", main.getResult().getSeverity() == IStatus.OK);
		
		main.schedule();
		waitForStart(main);
		main.cancel();
		
		while(main.getState() == Job.RUNNING) {
			try {
				main.done(Status.OK_STATUS);
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			} 
		}
		
		assertTrue("5.0", main.getResult().getSeverity() == IStatus.CANCEL);
		
		main.done(Status.OK_STATUS);
		assertTrue("6.0", main.getResult().getSeverity() == IStatus.CANCEL);
		
	}
	
	//see bug #43566
	public void testAsynchJob() {
		final int [] status = {0};
		
		//execute a job asynchronously and check the result
		AsynchTestJob main = new AsynchTestJob("Test Asynch Finish", status);
				
		assertTrue("1.0", main.getThread() == null);
		assertTrue("2.0", main.getResult() == null);
		
		main.schedule();
		
		waitForStart(main);
		assertTrue("3.0", main.getState() == Job.RUNNING);
		
		//make sure the job has set the thread it is going to run in
		int i = 0;
		while(status[0] == 0) {
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
				
			}
			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for thread to start", i++ < 100);
		}
		
		assertTrue("3.1", status[0] == 1);
		assertTrue("3.2", main.getThread() != null);
		
		while(main.getState() == Job.RUNNING) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			} 
		}
		
		assertTrue("4.0", main.getState() == Job.NONE);
		assertTrue("4.1", main.getResult().getSeverity() == IStatus.OK);
		assertTrue("4.2", main.getThread() == null);
		
		//reset status
		status[0] = 0;
		
		main.schedule();
		waitForStart(main);
		assertTrue("5.0", main.getState() == Job.RUNNING);
		
		//make sure the job has set the thread it is going to run in
		i = 0;
		while(status[0] == 0) {
			try {
				Thread.sleep(100);
			} catch(InterruptedException e) {
				
			}
			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for thread to start", i++ < 100);
		}
		
		assertTrue("5.1",status[0] == 1);
		assertTrue("5.2", main.getThread() != null);
		main.cancel();
		
		while(main.getState() == Job.RUNNING) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			} 
		}
		
		assertTrue("6.0", main.getState() == Job.NONE);
		assertTrue("6.1", main.getResult().getSeverity() == IStatus.CANCEL);
		assertTrue("6.2", main.getThread() == null);		
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

}
