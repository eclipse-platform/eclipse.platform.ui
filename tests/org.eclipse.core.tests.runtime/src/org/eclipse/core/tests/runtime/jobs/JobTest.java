/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import junit.framework.*;
import org.eclipse.core.internal.jobs.Worker;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.core.tests.harness.TestBarrier;
import org.eclipse.core.tests.harness.TestJob;

/**
 * Tests the implemented get/set methods of the abstract class Job
 */
public class JobTest extends TestCase {
	protected Job longJob;
	protected Job shortJob;

	public static Test suite() {
		return new TestSuite(JobTest.class);
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
		waitForState(shortJob, Job.NONE);

		//the done call should be ignored, and the job should finish execution normally
		assertTrue("3.0", shortJob.getResult().getSeverity() == IStatus.OK);

		shortJob.done(Status.CANCEL_STATUS);
		assertTrue("4.0", shortJob.getResult().getSeverity() == IStatus.OK);

		//calling the done method before a job is canceled
		longJob.schedule();
		waitForState(longJob, Job.RUNNING);
		longJob.done(Status.OK_STATUS);
		longJob.cancel();
		waitForState(longJob, Job.NONE);

		//the done call should be ignored, and the job status should still be canceled
		assertTrue("5.0", longJob.getResult().getSeverity() == IStatus.CANCEL);

		longJob.done(Status.OK_STATUS);
		assertTrue("6.0", longJob.getResult().getSeverity() == IStatus.CANCEL);

	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		shortJob = new TestJob("Short Test Job", 100, 10);
		longJob = new TestJob("Long Test Job", 1000000, 10);
	}

	private void sleep(long duration) {
		try {
			Thread.sleep(duration);
		} catch (InterruptedException e) {
			//ignore
		}
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//see bug #43566
	public void testAsynchJob() {
		final int[] status = {TestBarrier.STATUS_WAIT_FOR_START};

		//execute a job asynchronously and check the result
		AsynchTestJob main = new AsynchTestJob("Test Asynch Finish", status, 0);

		assertTrue("1.0", main.getThread() == null);
		assertTrue("2.0", main.getResult() == null);
		//schedule the job to run
		main.schedule();
		TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_RUNNING);
		assertTrue("3.0", main.getState() == Job.RUNNING);
		//the asynchronous process that assigns the thread the job is going to run in has not been started yet
		//the job is running in the thread provided to it by the manager
		assertTrue("3.1" + main.getThread().getName(), main.getThread() instanceof Worker);

		status[0] = TestBarrier.STATUS_START;
		TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_WAIT_FOR_START);

		//the asynchronous process has been started, but the set thread method has not been called yet
		assertTrue("3.2", main.getThread() instanceof Worker);

		status[0] = TestBarrier.STATUS_WAIT_FOR_RUN;

		//make sure the job has set the thread it is going to run in
		TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_RUNNING);

		assertTrue("3.3", status[0] == TestBarrier.STATUS_RUNNING);
		assertTrue("3.4", main.getThread() instanceof AsynchExecThread);

		//let the job run
		status[0] = TestBarrier.STATUS_WAIT_FOR_DONE;
		TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_DONE);
		waitForState(main, Job.NONE);

		//after the job is finished, the thread should be reset
		assertTrue("4.0", main.getState() == Job.NONE);
		assertTrue("4.1", main.getResult().getSeverity() == IStatus.OK);
		assertTrue("4.2", main.getThread() == null);

		//reset status
		status[0] = TestBarrier.STATUS_WAIT_FOR_START;

		//schedule the job to run again
		main.schedule();
		TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_RUNNING);
		assertTrue("5.0", main.getState() == Job.RUNNING);

		//the asynchronous process that assigns the thread the job is going to run in has not been started yet
		//job is running in the thread provided by the manager
		assertTrue("5.1", main.getThread() instanceof Worker);

		status[0] = TestBarrier.STATUS_START;
		TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_WAIT_FOR_START);

		//the asynchronous process has been started, but the set thread method has not been called yet
		assertTrue("5.2", main.getThread() instanceof Worker);

		status[0] = TestBarrier.STATUS_WAIT_FOR_RUN;

		//make sure the job has set the thread it is going to run in
		TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_RUNNING);

		assertTrue("5.3", status[0] == TestBarrier.STATUS_RUNNING);
		assertTrue("5.4", main.getThread() instanceof AsynchExecThread);

		//cancel the job, then let the job get the cancellation request
		main.cancel();
		status[0] = TestBarrier.STATUS_WAIT_FOR_DONE;
		TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_DONE);
		waitForState(main, Job.NONE);

		//thread should be reset to null after cancellation
		assertTrue("6.0", main.getState() == Job.NONE);
		assertTrue("6.1", main.getResult().getSeverity() == IStatus.CANCEL);
		assertTrue("6.2", main.getThread() == null);
	}

	public void testAsynchJobComplex() {
		final int[] status = {TestBarrier.STATUS_WAIT_FOR_START, TestBarrier.STATUS_WAIT_FOR_START, TestBarrier.STATUS_WAIT_FOR_START, TestBarrier.STATUS_WAIT_FOR_START, TestBarrier.STATUS_WAIT_FOR_START};

		//test the interaction of several asynchronous jobs
		AsynchTestJob[] jobs = new AsynchTestJob[5];

		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new AsynchTestJob("TestJob" + (i + 1), status, i);
			assertTrue("1." + i, jobs[i].getThread() == null);
			assertTrue("2." + i, jobs[i].getResult() == null);
			jobs[i].schedule();
			//status[i] = TestBarrier.STATUS_START;
		}
		//all the jobs should be running at the same time
		waitForStart(jobs, status);

		//every job should now be waiting for the STATUS_START flag
		for (int i = 0; i < status.length; i++) {
			assertTrue("3." + i, jobs[i].getState() == Job.RUNNING);
			assertTrue("4." + i, jobs[i].getThread() instanceof Worker);
			status[i] = TestBarrier.STATUS_START;
		}

		for (int i = 0; i < status.length; i++)
			TestBarrier.waitForStatus(status, i, TestBarrier.STATUS_WAIT_FOR_START);

		//every job should now be waiting for the STATUS_WAIT_FOR_RUN flag
		for (int i = 0; i < status.length; i++) {
			assertTrue("5. " + i, jobs[i].getThread() instanceof Worker);
			status[i] = TestBarrier.STATUS_WAIT_FOR_RUN;
		}

		//wait until all jobs are in the running state
		for (int i = 0; i < status.length; i++)
			TestBarrier.waitForStatus(status, i, TestBarrier.STATUS_RUNNING);

		//let the jobs execute
		for (int i = 0; i < status.length; i++) {
			assertTrue("6. " + i, jobs[i].getThread() instanceof AsynchExecThread);
			status[i] = TestBarrier.STATUS_WAIT_FOR_DONE;
		}

		for (int i = 0; i < status.length; i++)
			TestBarrier.waitForStatus(status, i, TestBarrier.STATUS_DONE);

		//the status for every job should be STATUS_OK
		//the threads should have been reset to null
		for (int i = 0; i < status.length; i++) {
			assertEquals("7." + i, TestBarrier.STATUS_DONE, status[i]);
			assertEquals("8." + i, Job.NONE, jobs[i].getState());
			assertEquals("9." + i, IStatus.OK, jobs[i].getResult().getSeverity());
			assertNull("10." + i, jobs[i].getThread());
		}
	}

	public void testAsynchJobConflict() {
		final int[] status = {TestBarrier.STATUS_WAIT_FOR_START, TestBarrier.STATUS_WAIT_FOR_START, TestBarrier.STATUS_WAIT_FOR_START, TestBarrier.STATUS_WAIT_FOR_START, TestBarrier.STATUS_WAIT_FOR_START};

		//test the interaction of several asynchronous jobs when a conflicting rule is assigned to some of them
		AsynchTestJob[] jobs = new AsynchTestJob[5];

		ISchedulingRule rule = new IdentityRule();

		for (int i = 0; i < jobs.length; i++) {
			jobs[i] = new AsynchTestJob("TestJob" + (i + 1), status, i);
			assertTrue("1." + i, jobs[i].getThread() == null);
			assertTrue("2." + i, jobs[i].getResult() == null);
			if (i < 2) {
				jobs[i].schedule();
			} else if (i > 2) {
				jobs[i].setRule(rule);
			} else {
				jobs[i].setRule(rule);
				jobs[i].schedule();
			}

		}

		//these 3 jobs should be waiting for the STATUS_START flag
		for (int i = 0; i < 3; i++) {
			TestBarrier.waitForStatus(status, i, TestBarrier.STATUS_RUNNING);
			assertTrue("3." + i, jobs[i].getState() == Job.RUNNING);
			assertTrue("4." + i, jobs[i].getThread() instanceof Worker);
			status[i] = TestBarrier.STATUS_START;
		}

		//the first 3 jobs should be running at the same time
		for (int i = 0; i < 3; i++)
			TestBarrier.waitForStatus(status, i, TestBarrier.STATUS_WAIT_FOR_START);

		//the 3 jobs should now be waiting for the STATUS_WAIT_FOR_RUN flag
		for (int i = 0; i < 3; i++) {
			assertTrue("5. " + i, jobs[i].getThread() instanceof Worker);
			status[i] = TestBarrier.STATUS_WAIT_FOR_RUN;
		}

		//wait until jobs block on running state
		for (int i = 0; i < 3; i++)
			TestBarrier.waitForStatus(status, i, TestBarrier.STATUS_RUNNING);

		//schedule the 2 remaining jobs
		jobs[3].schedule();
		jobs[4].schedule();

		//the 2 newly scheduled jobs should be waiting since they conflict with the third job
		//no threads were assigned to them yet
		assertEquals("6.1", Job.WAITING, jobs[3].getState());
		assertNull("6.2", jobs[3].getThread());
		assertEquals("6.3", Job.WAITING, jobs[4].getState());
		assertNull("6.4", jobs[4].getThread());

		//let the two non-conflicting jobs execute together
		for (int i = 0; i < 2; i++) {
			assertTrue("7. " + i, jobs[i].getThread() instanceof AsynchExecThread);
			status[i] = TestBarrier.STATUS_WAIT_FOR_DONE;
		}
		//wait until the non-conflicting jobs are done
		TestBarrier.waitForStatus(status, 1, TestBarrier.STATUS_DONE);

		//the third job should still be in the running state
		assertEquals("8.1", Job.RUNNING, jobs[2].getState());
		//the 2 conflicting jobs should still be in the waiting state
		assertEquals("8.2", Job.WAITING, jobs[3].getState());
		assertEquals("8.3", Job.WAITING, jobs[4].getState());

		//let the third job finish execution
		assertTrue("8.4", jobs[2].getThread() instanceof AsynchExecThread);
		status[2] = TestBarrier.STATUS_WAIT_FOR_DONE;

		//wait until the third job is done
		TestBarrier.waitForStatus(status, 2, TestBarrier.STATUS_DONE);

		//the fourth job should now start running, the fifth job should still be waiting
		TestBarrier.waitForStatus(status, 3, TestBarrier.STATUS_RUNNING);
		assertEquals("9.1", Job.RUNNING, jobs[3].getState());
		assertEquals("9.2", Job.WAITING, jobs[4].getState());

		//let the fourth job run, the fifth job is still waiting
		status[3] = TestBarrier.STATUS_START;
		assertEquals("9.3", Job.WAITING, jobs[4].getState());
		TestBarrier.waitForStatus(status, 3, TestBarrier.STATUS_WAIT_FOR_START);
		status[3] = TestBarrier.STATUS_WAIT_FOR_RUN;
		assertEquals("9.4", Job.WAITING, jobs[4].getState());
		TestBarrier.waitForStatus(status, 3, TestBarrier.STATUS_RUNNING);
		assertEquals("9.5", Job.WAITING, jobs[4].getState());

		//cancel the fifth job, finish the fourth job
		jobs[4].cancel();
		assertTrue("9.6", jobs[3].getThread() instanceof AsynchExecThread);
		status[3] = TestBarrier.STATUS_WAIT_FOR_DONE;

		//wait until the fourth job is done
		TestBarrier.waitForStatus(status, 3, TestBarrier.STATUS_DONE);

		//the status for the first 4 jobs should be STATUS_OK
		//the threads should have been reset to null
		for (int i = 0; i < status.length - 1; i++) {
			assertEquals("10." + i, TestBarrier.STATUS_DONE, status[i]);
			assertEquals("11." + i, Job.NONE, jobs[i].getState());
			assertEquals("12." + i, IStatus.OK, jobs[i].getResult().getSeverity());
			assertNull("13." + i, jobs[i].getThread());
		}

		//the fifth job should have null as its status (it never finished running)
		//the thread for it should have also been reset
		assertEquals("14.1", TestBarrier.STATUS_WAIT_FOR_START, status[4]);
		assertEquals("14.2", Job.NONE, jobs[4].getState());
		assertNull("14.3", jobs[4].getResult());
		assertNull("14.4", jobs[4].getThread());
	}

	/**
	 * Tests cancelation of a job from the aboutToRun job event.
	 * See bug 70434 for details.
	 */
	public void testCancelFromAboutToRun() {
		final int[] doneCount = new int[] {0};
		final int[] runningCount = new int[] {0};
		TestJob job = new TestJob("testCancelFromAboutToRun", 0, 0);
		job.addJobChangeListener(new JobChangeAdapter() {
			public void aboutToRun(IJobChangeEvent event) {
				event.getJob().cancel();
			}
			public void done(IJobChangeEvent event) {
				doneCount[0]++;
			}
			public void running(IJobChangeEvent event) {
				runningCount[0]++;
			}
		});
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("0.99 " + e.getMessage());
		}
		assertEquals("1.0", 0, job.getRunCount());
		assertEquals("1.1", 1, doneCount[0]);
		assertEquals("1.2", 0, runningCount[0]);
	}
	
	public void testGetName() {
		assertTrue("1.0", shortJob.getName().equals("Short Test Job"));
		assertTrue("1.1", longJob.getName().equals("Long Test Job"));

		//try creating a job with a null name
		try {
			new TestJob(null);
			fail("2.0");
		} catch (RuntimeException e) {
			//should fail
		}
	}

	public void testGetPriority() {
		//set priorities to all allowed options
		//check if getPriority() returns proper result

		int[] priority = {Job.SHORT, Job.LONG, Job.INTERACTIVE, Job.BUILD, Job.DECORATE};

		for (int i = 0; i < priority.length; i++) {
			shortJob.setPriority(priority[i]);
			assertTrue("1." + i, shortJob.getPriority() == priority[i]);
		}
	}

	public void testGetProperty() {
		QualifiedName n1 = new QualifiedName("org.eclipse.core.tests.runtime", "p1");
		QualifiedName n2 = new QualifiedName("org.eclipse.core.tests.runtime", "p2");
		assertNull("1.0", shortJob.getProperty(n1));
		shortJob.setProperty(n1, null);
		assertNull("1.1", shortJob.getProperty(n1));
		shortJob.setProperty(n1, shortJob);
		assertTrue("1.2", shortJob.getProperty(n1) == shortJob);
		assertNull("1.3", shortJob.getProperty(n2));
		shortJob.setProperty(n1, "hello");
		assertEquals("1.4", "hello", shortJob.getProperty(n1));
		shortJob.setProperty(n1, null);
		assertNull("1.5", shortJob.getProperty(n1));
		assertNull("1.6", shortJob.getProperty(n2));
	}

	public void testGetResult() {
		//execute a short job
		assertTrue("1.0", shortJob.getResult() == null);
		shortJob.schedule();
		waitForState(shortJob, Job.NONE);
		assertTrue("1.1", shortJob.getResult().getSeverity() == IStatus.OK);

		//cancel a long job
		longJob.schedule(1000000);
		assertTrue("1.3", longJob.sleep());
		longJob.wakeUp();
		waitForState(longJob, Job.RUNNING);
		longJob.cancel();
		waitForState(longJob, Job.NONE);
		assertTrue("2.0", longJob.getResult().getSeverity() == IStatus.CANCEL);
	}

	public void testGetRule() {
		//set several rules for the job, check if getRule returns the rule that was set
		//no rule was set yet
		assertTrue("1.0", shortJob.getRule() == null);

		shortJob.setRule(new IdentityRule());
		assertTrue("1.1", (shortJob.getRule() instanceof IdentityRule));

		ISchedulingRule rule = new PathRule("/testGetRule");
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
		assertTrue("1.2", shortJob.getThread() != t);

		shortJob.setThread(null);
		assertTrue("1.3", shortJob.getThread() == null);
	}

	public void testIsSystem() {
		//reset the system parameter several times
		shortJob.setUser(false);
		shortJob.setSystem(false);
		assertTrue("1.0", !shortJob.isUser());
		assertTrue("1.1", !shortJob.isSystem());
		shortJob.setSystem(true);
		assertTrue("1.2", !shortJob.isUser());
		assertTrue("1.3", shortJob.isSystem());
		shortJob.setSystem(false);
		assertTrue("1.4", !shortJob.isUser());
		assertTrue("1.5", !shortJob.isSystem());
	}

	public void testIsUser() {
		//reset the user parameter several times
		shortJob.setUser(false);
		shortJob.setSystem(false);
		assertTrue("1.0", !shortJob.isUser());
		assertTrue("1.1", !shortJob.isSystem());
		shortJob.setUser(true);
		assertTrue("1.2", shortJob.isUser());
		assertTrue("1.3", !shortJob.isSystem());
		shortJob.setUser(false);
		assertTrue("1.4", !shortJob.isUser());
		assertTrue("1.5", !shortJob.isSystem());
	}

	public void testJoin() {
		longJob.schedule(100000);
		//create a thread that will join the test job
		final int[] status = new int[1];
		status[0] = TestBarrier.STATUS_WAIT_FOR_START;
		Thread t = new Thread(new Runnable() {
			public void run() {
				status[0] = TestBarrier.STATUS_START;
				try {
					longJob.join();
				} catch (InterruptedException e) {
					Assert.fail("0.99");
				}
				status[0] = TestBarrier.STATUS_DONE;
			}
		});
		t.start();
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_START);
		assertEquals("1.0", TestBarrier.STATUS_START, status[0]);
		//putting the job to sleep should not affect the join call
		longJob.sleep();
		//give a chance for the sleep to take effect
		sleep(100);
		assertEquals("1.0", TestBarrier.STATUS_START, status[0]);
		//similarly waking the job up should not affect the join
		longJob.wakeUp(100000);
		sleep(100);
		assertEquals("1.0", TestBarrier.STATUS_START, status[0]);

		//finally canceling the job will cause the join to return
		longJob.cancel();
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_DONE);
	}

	/**
	 * Tests a job change listener that throws an exception.
	 * This would previously cause join attempts on that job to
	 * hang indefinitely because they would miss the notification
	 * required to end the join.
	 */
	public void testJoinFailingListener() {
		shortJob.addJobChangeListener(new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				throw new RuntimeException("This exception thrown on purpose as part of a test");
			}
		});
		final int[] status = new int[1];
		//create a thread that will join the job
		Thread t = new Thread(new Runnable() {
			public void run() {
				status[0] = TestBarrier.STATUS_START;
				try {
					shortJob.join();
				} catch (InterruptedException e) {
					Assert.fail("0.99");
				}
				status[0] = TestBarrier.STATUS_DONE;
			}
		});
		//schedule the job and then fork the thread to join it
		shortJob.schedule();
		t.start();
		//wait until the join succeeds
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_DONE);
	}
	
	/**
	 * This is a regression test for bug 60323. If a job change listener
	 * removed itself from the listener list during the done() change event,
	 * then anyone joining on that job would hang forever.
	 */
	public void testJoinRemoveListener() {
		final IJobChangeListener listener = new JobChangeAdapter() {
			public void done(IJobChangeEvent event) {
				shortJob.removeJobChangeListener(this);
			}
		};
		shortJob.addJobChangeListener(listener);
		final int[] status = new int[1];
		//create a thread that will join the job
		Thread t = new Thread(new Runnable() {
			public void run() {
				status[0] = TestBarrier.STATUS_START;
				try {
					shortJob.join();
				} catch (InterruptedException e) {
					Assert.fail("0.99");
				}
				status[0] = TestBarrier.STATUS_DONE;
			}
		});
		//schedule the job and then fork the thread to join it
		shortJob.schedule();
		t.start();
		//wait until the join succeeds
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_DONE);
	}

	/*
	 * Test that a canceled job is rescheduled
	 */
	public void testRescheduleCancel() {
		final int[] status = {TestBarrier.STATUS_WAIT_FOR_START};
		Job job = new Job("Testing") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Testing", 1);
					status[0] = TestBarrier.STATUS_WAIT_FOR_RUN;
					TestBarrier.waitForStatus(status, TestBarrier.STATUS_RUNNING);
					monitor.worked(1);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		//schedule the job, cancel it, then reschedule
		job.schedule();
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_WAIT_FOR_RUN);
		job.cancel();
		job.schedule();
		//let the first iteration of the job finish
		status[0] = TestBarrier.STATUS_RUNNING;
		//wait until the job runs again
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_WAIT_FOR_RUN);
		//let the job finish
		status[0] = TestBarrier.STATUS_RUNNING;
		waitForState(job, Job.NONE);
	}

	/*
	 * Test that multiple reschedules of the same job while it is running
	 * only remembers the last reschedule request
	 */
	public void testRescheduleComplex() {
		final int[] status = {TestBarrier.STATUS_WAIT_FOR_START};
		final int[] runCount = new int[] {0};
		Job job = new Job("Testing") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Testing", 1);
					status[0] = TestBarrier.STATUS_WAIT_FOR_RUN;
					TestBarrier.waitForStatus(status, TestBarrier.STATUS_RUNNING);
					monitor.worked(1);
				} finally {
					runCount[0]++;
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		//schedule the job, reschedule when it is running
		job.schedule();
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_WAIT_FOR_RUN);
		//the last schedule value should win
		job.schedule(1000000);
		job.schedule(3000);
		job.schedule(200000000);
		job.schedule();
		status[0] = TestBarrier.STATUS_RUNNING;
		//wait until the job runs again
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_WAIT_FOR_RUN);
		assertEquals("1.0", 1, runCount[0]);
		//let the job finish
		status[0] = TestBarrier.STATUS_RUNNING;
		waitForState(job, Job.NONE);
		assertEquals("1.0", 2, runCount[0]);
	}

	/*
	 * Reschedule a running job with a delay
	 */
	public void testRescheduleDelay() {
		final int[] status = {TestBarrier.STATUS_WAIT_FOR_START};
		final int[] runCount = new int[] {0};
		Job job = new Job("Testing") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Testing", 1);
					status[0] = TestBarrier.STATUS_WAIT_FOR_RUN;
					TestBarrier.waitForStatus(status, TestBarrier.STATUS_RUNNING);
					monitor.worked(1);
				} finally {
					runCount[0]++;
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		//schedule the job, reschedule when it is running
		job.schedule();
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_WAIT_FOR_RUN);
		job.schedule(1000000);
		status[0] = TestBarrier.STATUS_RUNNING;
		//now wait until the job is scheduled again and put to sleep
		waitForState(job, Job.SLEEPING);
		assertEquals("1.0", 1, runCount[0]);

		//reschedule the job while it is sleeping
		job.schedule();
		//wake up the currently sleeping job
		job.wakeUp();
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_WAIT_FOR_RUN);
		status[0] = TestBarrier.STATUS_RUNNING;
		//make sure the job was not rescheduled while the executing job was sleeping
		waitForState(job, Job.NONE);
		assertTrue("1.0", job.getState() == Job.NONE);
		assertEquals("1.0", 2, runCount[0]);
	}

	/*
	 * Schedule a simple job that repeats several times from within the run method.
	 */
	public void testRescheduleRepeat() {
		final int[] count = new int[] {0};
		final int REPEATS = 10;
		Job job = new Job("testRescheduleRepeat") {
			protected IStatus run(IProgressMonitor monitor) {
				count[0]++;
				schedule();
				return Status.OK_STATUS;
			}

			public boolean shouldSchedule() {
				return count[0] < REPEATS;
			}
		};
		job.schedule();
		int timeout = 0;
		while (timeout++ < 100 && count[0] < REPEATS) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//ignore
			}
		}
		assertTrue("1.0", timeout < 100);
		assertEquals("1.1", REPEATS, count[0]);
	} /*
	 * Schedule a simple job that repeats several times from within the run method.
	 */

	public void testRescheduleRepeatWithDelay() {
		final int[] count = new int[] {0};
		final int REPEATS = 10;
		Job job = new Job("testRescheduleRepeat") {
			protected IStatus run(IProgressMonitor monitor) {
				count[0]++;
				schedule(10);
				return Status.OK_STATUS;
			}

			public boolean shouldSchedule() {
				return count[0] < REPEATS;
			}
		};
		job.schedule();
		int timeout = 0;
		while (timeout++ < 100 && count[0] < REPEATS) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//ignore
			}
		}
		assertTrue("1.0", timeout < 100);
		assertEquals("1.1", REPEATS, count[0]);
	}

	/*
	 * Schedule a job to run, and then reschedule it
	 */
	public void testRescheduleSimple() {
		final int[] status = {TestBarrier.STATUS_WAIT_FOR_START};
		Job job = new Job("testRescheduleSimple") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Testing", 1);
					status[0] = TestBarrier.STATUS_WAIT_FOR_RUN;
					TestBarrier.waitForStatus(status, TestBarrier.STATUS_RUNNING);
					monitor.worked(1);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		//schedule the job, reschedule when it is running
		job.schedule();
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_WAIT_FOR_RUN);
		job.schedule();
		status[0] = TestBarrier.STATUS_RUNNING;
		//wait until the job runs again
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_WAIT_FOR_RUN);
		//let the job finish
		status[0] = TestBarrier.STATUS_RUNNING;
		waitForState(job, Job.NONE);

		//the job should only run once the second time around
		job.schedule();
		TestBarrier.waitForStatus(status, TestBarrier.STATUS_WAIT_FOR_RUN);
		//let the job finish
		status[0] = TestBarrier.STATUS_RUNNING;
		//wait until the job truly finishes and has a chance to be rescheduled (it shouldn't reschedule)
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			//ignore
		}
		waitForState(job, Job.NONE);
	}

	/*
	 * Reschedule a waiting job.
	 */
	public void testRescheduleWaiting() {
		final int[] status = {TestBarrier.STATUS_WAIT_FOR_START, TestBarrier.STATUS_WAIT_FOR_START};
		final int[] runCount = new int[] {0};
		final ISchedulingRule rule = new IdentityRule();
		Job first = new Job("Testing1") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Testing", 1);
					status[0] = TestBarrier.STATUS_WAIT_FOR_RUN;
					TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_RUNNING);
					monitor.worked(1);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		Job second = new Job("Testing2") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Testing", 1);
					status[1] = TestBarrier.STATUS_WAIT_FOR_RUN;
					TestBarrier.waitForStatus(status, 1, TestBarrier.STATUS_RUNNING);
					monitor.worked(1);
				} finally {
					runCount[0]++;
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		//set the same rule for both jobs so that the second job would have to wait
		first.setRule(rule);
		first.schedule();
		second.setRule(rule);
		TestBarrier.waitForStatus(status, 0, TestBarrier.STATUS_WAIT_FOR_RUN);
		second.schedule();
		waitForState(second, Job.WAITING);
		//reschedule the second job while it is waiting
		second.schedule();
		//let the first job finish
		status[0] = TestBarrier.STATUS_RUNNING;
		//the second job will start
		TestBarrier.waitForStatus(status, 1, TestBarrier.STATUS_WAIT_FOR_RUN);
		//let the second job finish
		status[1] = TestBarrier.STATUS_RUNNING;

		//make sure the second job was not rescheduled
		waitForState(second, Job.NONE);
		assertEquals("2.0", Job.NONE, second.getState());
		assertEquals("2.1", 1, runCount[0]);
	}

	/*
	 * see bug #43458
	 */
	public void testSetPriority() {
		int[] wrongPriority = {1000, -Job.DECORATE, 25, 0, 5, Job.INTERACTIVE - Job.BUILD};

		for (int i = 0; i < wrongPriority.length; i++) {
			//set priority to non-existent type
			try {
				shortJob.setPriority(wrongPriority[i]);
				fail("1." + (i + 1));
			} catch (RuntimeException e) {
				//should fail
			}
		}
	}

	/**
	 * Tests the API methods Job.setProgressGroup
	 */
	public void testSetProgressGroup() {
		//null group
		try {
			longJob.setProgressGroup(null, 5);
			fail("1.0");
		} catch (RuntimeException e) {
			//should fail
		}
		IProgressMonitor group = Platform.getJobManager().createProgressGroup();
		group.beginTask("Group task name", 10);
		longJob.setProgressGroup(group, 5);

		//ignore changes to group while waiting or running
		longJob.schedule(100);
		longJob.setProgressGroup(group, 0);
		waitForState(longJob, Job.RUNNING);
		longJob.setProgressGroup(group, 0);

		//ensure cancelation still works
		longJob.cancel();
		waitForState(longJob, Job.NONE);
		group.done();
	}

	/*
	 * see bug #43459
	 */
	public void testSetRule() {
		//setting a scheduling rule for a job after it was already scheduled should throw an exception
		shortJob.setRule(new IdentityRule());
		assertTrue("1.0", shortJob.getRule() instanceof IdentityRule);
		shortJob.schedule(1000000);
		try {
			shortJob.setRule(new PathRule("/testSetRule"));
			fail("1.1");
		} catch (RuntimeException e) {
			//should fail
		}

		//wake up the sleeping job
		shortJob.wakeUp();

		//setting the rule while running should fail
		try {
			shortJob.setRule(new PathRule("/testSetRule/B"));
			fail("2.0");
		} catch (RuntimeException e1) {
			//should fail
		}

		try {
			//wait for the job to complete
			shortJob.join();
		} catch (InterruptedException e2) {
			//ignore
		}

		//after the job has finished executing, the scheduling rule for it can once again be reset
		shortJob.setRule(new PathRule("/testSetRule/B/C/D"));
		assertTrue("1.2", shortJob.getRule() instanceof PathRule);
		shortJob.setRule(null);
		assertTrue("1.3", shortJob.getRule() == null);
	}

	public void testSetThread() {
		//setting the thread of a job that is not an asynchronous job should not affect the actual thread the job will run in
		assertTrue("0.0", longJob.getThread() == null);

		longJob.setThread(Thread.currentThread());
		assertTrue("1.0", longJob.getThread() == Thread.currentThread());
		longJob.schedule();
		waitForState(longJob, Job.RUNNING);

		//the setThread method should have no effect on jobs that execute normally
		assertTrue("2.0", longJob.getThread() != Thread.currentThread());

		longJob.cancel();
		waitForState(longJob, Job.NONE);

		//the thread should reset to null when the job finishes execution
		assertTrue("3.0", longJob.getThread() == null);

		longJob.setThread(null);
		assertTrue("4.0", longJob.getThread() == null);

		longJob.schedule();
		waitForState(longJob, Job.RUNNING);

		//the thread that the job is executing in is not the one that was set
		assertTrue("5.0", longJob.getThread() != null);
		longJob.cancel();
		waitForState(longJob, Job.NONE);

		//thread should reset to null after execution of job
		assertTrue("6.0", longJob.getThread() == null);

		Thread t = new Thread();
		longJob.setThread(t);
		assertTrue("7.0", longJob.getThread() == t);
		longJob.schedule();
		waitForState(longJob, Job.RUNNING);

		//the thread that the job is executing in is not the one that it was set to
		assertTrue("8.0", longJob.getThread() != t);
		longJob.cancel();
		waitForState(longJob, Job.NONE);

		//execution thread should reset to null after job is finished
		assertTrue("9.0", longJob.getThread() == null);
	}

	/**
	 * Several jobs were scheduled to run.
	 * Pause this thread until all the jobs start running.
	 */
	private void waitForStart(Job[] jobs, int[] status) {
		for (int i = 0; i < jobs.length; i++)
			TestBarrier.waitForStatus(status, i, TestBarrier.STATUS_RUNNING);
	}

	/**
	 * A job has been scheduled.  Pause this thread so that a worker thread
	 * has a chance to pick up the new job.
	 */
	private void waitForState(Job job, int state) {
		int i = 0;
		while (job.getState() != state) {
			try {
				Thread.yield();
				Thread.sleep(100);
				Thread.yield();
			} catch (InterruptedException e) {
				//ignore
			}
			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for job to change state.", i++ < 100);
		}
	}

	/**
	 * A job was scheduled to run.  Pause this thread so that a worker thread
	 * has a chance to finish the job
	 */
	//	private void waitForEnd(Job job) {
	//		int i = 0;
	//		while(job.getState() != Job.NONE) {
	//			try {
	//				Thread.sleep(100);
	//			} catch (InterruptedException e) {
	//				
	//			} 
	//			
	//			//sanity test to avoid hanging tests
	//			assertTrue("Timeout waiting for job to end", i++ < 1000);
	//		}
	//	}	
}
