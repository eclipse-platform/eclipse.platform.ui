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

import junit.framework.TestSuite;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.FussyProgressMonitor;

/**
 * Tests API methods IJobManager.beginRule and IJobManager.endRule
 */
public class BeginEndRuleTest extends AbstractJobManagerTest {

	private class JobRuleRunner extends Job {
		private ISchedulingRule rule;
		private int[] status;
		private int index;
		private int numRepeats;

		/**
		 * This job will start applying the given rule in the manager
		*/
		public JobRuleRunner(String name, ISchedulingRule rule, int[] status, int index, int numRepeats) {
			super(name);
			this.status = status;
			this.rule = rule;
			this.index = index;
			this.numRepeats = numRepeats;
		}

		protected IStatus run(IProgressMonitor monitor) {
			//begin executing the job
			monitor.beginTask(getName(), numRepeats);
			try {
				//set the status flag to START
				status[index] = StatusChecker.STATUS_START;
				for (int i = 0; i < numRepeats; i++) {
					//wait until the tester allows this job to run again
					StatusChecker.waitForStatus(status, index, StatusChecker.STATUS_WAIT_FOR_RUN, 100);
					//start the given rule in the manager
					manager.beginRule(rule);
					//set status to RUNNING
					status[index] = StatusChecker.STATUS_RUNNING;

					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;

					//wait until tester allows this job to finish
					StatusChecker.waitForStatus(status, index, StatusChecker.STATUS_WAIT_FOR_DONE, 100);
					//end the given rule
					manager.endRule(rule);
					//set status to DONE
					status[index] = StatusChecker.STATUS_DONE;

					monitor.worked(1);
					Thread.yield();
				}

			} finally {
				monitor.done();
				Thread.yield();
			}
			return Status.OK_STATUS;
		}

	}

	/**
	 * This runnable will try to begin the given rule in the Job Manager.  It will
	 * end the rule before returning.
	 */
	private class SimpleRuleRunner implements Runnable {
		private ISchedulingRule rule;
		private IProgressMonitor monitor;
		private int[] status;
		RuntimeException exception;
		public SimpleRuleRunner(ISchedulingRule rule, int[] status, IProgressMonitor monitor) {
			this.rule = rule;
			this.monitor = monitor;
			this.status = status;
			this.exception = null;
		}
		public void run() {
			//tell the caller that we have entered the run method
			status[0] = StatusChecker.STATUS_RUNNING;
			try {
				try {
					manager.beginRule(rule, monitor);
				} finally {
					manager.endRule(rule);
				}
			} catch (OperationCanceledException e) {
				//ignore
			} catch (RuntimeException e) {
				exception = e;
			} finally {
				status[0] = StatusChecker.STATUS_DONE;
			}
		}
	}	/**
	 * This runnable will try to end the given rule in the Job Manager
	 */
	private class RuleEnder implements Runnable {
		private ISchedulingRule rule;

		public RuleEnder(ISchedulingRule rule) {
			this.rule = rule;
		}

		public void run() {
			try {
				manager.endRule(rule);
				fail("Ending Rule");

			} catch (RuntimeException e) {
				//should fail
			}

		}
	}
	public static TestSuite suite() {
		return new TestSuite(BeginEndRuleTest.class);
	}
	public BeginEndRuleTest() {
		super();
	}
	public BeginEndRuleTest(String name) {
		super(name);
	}
		
	public void _testComplexRuleStarting() {
		//test how the manager reacts when several different threads try to begin conflicting rules

		//array to communicate with the launched threads
		final int[] status =
			{ StatusChecker.STATUS_WAIT_FOR_START, StatusChecker.STATUS_WAIT_FOR_START, StatusChecker.STATUS_WAIT_FOR_START, StatusChecker.STATUS_WAIT_FOR_START, StatusChecker.STATUS_WAIT_FOR_START };
		//number of times to start each rule
		int NUM_REPEATS = 10;

		RuleSetA.conflict = true;
		Job[] jobs = new Job[5];

		jobs[0] = new JobRuleRunner("Job1", new RuleSetB(), status, 0, NUM_REPEATS);
		jobs[1] = new JobRuleRunner("Job2", new RuleSetB(), status, 1, NUM_REPEATS);
		jobs[2] = new JobRuleRunner("Job3", new RuleSetC(), status, 2, NUM_REPEATS);
		jobs[3] = new JobRuleRunner("Job4", new RuleSetD(), status, 3, NUM_REPEATS);
		jobs[4] = new JobRuleRunner("Job5", new RuleSetE(), status, 4, NUM_REPEATS);

		//schedule the jobs
		for (int i = 0; i < jobs.length; i++) {
			jobs[i].schedule();
		}

		//by the time the fifth job starts, the rest should have already started
		waitForStart(jobs[4]);
		StatusChecker.waitForStatus(status, 4, StatusChecker.STATUS_START, 100);

		//all jobs should be running
		//the status flag should be set to START
		for (int i = 0; i < status.length; i++) {
			assertTrue("1." + i, jobs[i].getState() == Job.RUNNING);
			assertTrue("2." + i, status[i] == StatusChecker.STATUS_START);
		}

		//the order that the jobs will be executed
		int[] order = { 0, 1, 2, 3, 4 };

		for (int j = 0; j < NUM_REPEATS; j++) {
			//let the first job in the order run
			status[order[0]] = StatusChecker.STATUS_WAIT_FOR_RUN;
			//wait until the first job in the order reads the flag
			StatusChecker.waitForStatus(status, order[0], StatusChecker.STATUS_RUNNING, 100);

			//let the second job run (it will be blocked), the other jobs will be waiting for the flag reset
			status[order[1]] = StatusChecker.STATUS_WAIT_FOR_RUN;
			assertTrue("3.0", status[order[0]] == StatusChecker.STATUS_RUNNING);
			assertTrue("3.1", status[order[1]] == StatusChecker.STATUS_WAIT_FOR_RUN);

			//let the first job finish			
			status[order[0]] = StatusChecker.STATUS_WAIT_FOR_DONE;
			//first job is done
			StatusChecker.waitForStatus(status, order[0], StatusChecker.STATUS_DONE, 100);

			//let the second job run
			StatusChecker.waitForStatus(status, order[1], StatusChecker.STATUS_RUNNING, 100);
			status[order[2]] = StatusChecker.STATUS_WAIT_FOR_RUN;

			assertTrue("4.0", status[order[1]] == StatusChecker.STATUS_RUNNING);
			assertTrue("4.1", status[order[2]] == StatusChecker.STATUS_WAIT_FOR_RUN);

			//let the second job finish
			status[order[1]] = StatusChecker.STATUS_WAIT_FOR_DONE;
			//second job is done
			StatusChecker.waitForStatus(status, order[1], StatusChecker.STATUS_DONE, 100);

			//start the third job
			StatusChecker.waitForStatus(status, order[2], StatusChecker.STATUS_RUNNING, 100);
			status[order[3]] = StatusChecker.STATUS_WAIT_FOR_RUN;

			assertTrue("5.0", status[order[2]] == StatusChecker.STATUS_RUNNING);
			assertTrue("5.1", status[order[3]] == StatusChecker.STATUS_WAIT_FOR_RUN);

			//let the third job finish
			status[order[2]] = StatusChecker.STATUS_WAIT_FOR_DONE;
			//third job is done
			StatusChecker.waitForStatus(status, order[2], StatusChecker.STATUS_DONE, 100);

			//start the fourth job
			StatusChecker.waitForStatus(status, order[3], StatusChecker.STATUS_RUNNING, 100);
			status[order[4]] = StatusChecker.STATUS_WAIT_FOR_RUN;

			assertTrue("6.0", status[order[3]] == StatusChecker.STATUS_RUNNING);
			assertTrue("6.1", status[order[4]] == StatusChecker.STATUS_WAIT_FOR_RUN);

			//let the fourth job finish
			status[order[3]] = StatusChecker.STATUS_WAIT_FOR_DONE;
			//fourth job is done
			StatusChecker.waitForStatus(status, order[3], StatusChecker.STATUS_DONE, 100);

			//start the fifth job
			StatusChecker.waitForStatus(status, order[4], StatusChecker.STATUS_RUNNING, 100);

			assertTrue("7.0", status[order[4]] == StatusChecker.STATUS_RUNNING);

			status[order[4]] = StatusChecker.STATUS_WAIT_FOR_DONE;
			//fifth job is done
			StatusChecker.waitForStatus(status, order[4], StatusChecker.STATUS_DONE, 100);

			if (j < 9) {
				for (int i = 0; i < status.length; i++) {
					assertTrue("7." + (i + 1), status[i] == StatusChecker.STATUS_DONE);
					assertTrue("8." + (i + 1), jobs[i].getState() == Job.RUNNING);
				}
			}

			//change the order of the jobs, nothing should change in the execution
			int temp = order[0];

			order[0] = order[2];
			order[2] = order[4];
			order[4] = order[1];
			order[1] = order[3];
			order[3] = temp;
		}

		for (int i = 0; i < jobs.length; i++) {
			//check that the final status of all jobs is correct		
			assertTrue("9." + i, status[i] == StatusChecker.STATUS_DONE);
			assertTrue("10." + i, jobs[i].getState() == Job.NONE);
			assertTrue("11." + i, jobs[i].getResult().getSeverity() == Status.OK);
		}
	}

	public void testSimpleRuleStarting() {
		//start two jobs, each of which will begin and end a rule several times
		//while one job starts a rule, the second job's call to begin rule should block that thread
		//until the first job calls end rule
		final int[] status = { StatusChecker.STATUS_WAIT_FOR_START, StatusChecker.STATUS_WAIT_FOR_START };
		//number of repetitions of beginning and ending the rule
		final int NUM_REPEATS = 10;
		//set the two rules to conflict with each other
		RuleSetA.conflict = true;
		Job[] jobs = new Job[2];

		jobs[0] = new JobRuleRunner("Job1", new RuleSetB(), status, 0, NUM_REPEATS);
		jobs[1] = new JobRuleRunner("Job2", new RuleSetD(), status, 1, NUM_REPEATS);

		//schedule both jobs to start their execution
		jobs[0].schedule();
		jobs[1].schedule();

		//make sure both jobs are running and their respective run methods have been invoked
		waitForStart(jobs[1]);
		StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_START, 100);

		assertTrue("2.0", jobs[0].getState() == Job.RUNNING);
		assertTrue("2.1", jobs[1].getState() == Job.RUNNING);
		assertTrue("2.2", status[0] == StatusChecker.STATUS_START);
		assertTrue("2.3", status[1] == StatusChecker.STATUS_START);

		//the order of execution of the jobs (by their index in the status array)
		int first = 0;
		int second = 1;

		//now both jobs are waiting for the STATUS_WAIT_FOR_RUN flag
		for (int j = 0; j < NUM_REPEATS; j++) {
			//let the first job start executing
			status[first] = StatusChecker.STATUS_WAIT_FOR_RUN;

			//wait for the first job to read the flag
			StatusChecker.waitForStatus(status, first, StatusChecker.STATUS_RUNNING, 100);

			//let the second job start, its thread will be blocked by the beginRule method
			status[second] = StatusChecker.STATUS_WAIT_FOR_RUN;

			//only the first job should be running
			//the other job should be blocked by the beginRule method
			assertTrue("3.1", status[first] == StatusChecker.STATUS_RUNNING);
			assertTrue("3.2", status[second] == StatusChecker.STATUS_WAIT_FOR_RUN);

			//let the first job finish execution and call endRule
			//the second thread will then become unblocked
			status[first] = StatusChecker.STATUS_WAIT_FOR_DONE;

			//wait until the first job is done
			StatusChecker.waitForStatus(status, first, StatusChecker.STATUS_DONE, 100);

			//now wait until the second job begins execution
			StatusChecker.waitForStatus(status, second, StatusChecker.STATUS_RUNNING, 100);

			//the first job is done, the second job is executing
			assertTrue("4.1", status[first] == StatusChecker.STATUS_DONE);
			assertTrue("4.2", status[second] == StatusChecker.STATUS_RUNNING);

			//let the second job finish execution
			status[second] = StatusChecker.STATUS_WAIT_FOR_DONE;

			//wait until the second job is finished
			StatusChecker.waitForStatus(status, second, StatusChecker.STATUS_DONE, 100);

			//both jobs are done now
			assertTrue("5.1", status[first] == StatusChecker.STATUS_DONE);
			assertTrue("5.2", status[second] == StatusChecker.STATUS_DONE);

			//flip the order of execution of the jobs
			int temp = first;
			first = second;
			second = temp;
		}

		//check that the final status of both jobs is correct		
		assertTrue("6.1", status[0] == StatusChecker.STATUS_DONE);
		assertTrue("6.2", status[1] == StatusChecker.STATUS_DONE);
		assertTrue("6.3", jobs[0].getState() == Job.NONE);
		assertTrue("6.4", jobs[1].getState() == Job.NONE);
		assertTrue("6.5", jobs[0].getResult().getSeverity() == Status.OK);
		assertTrue("6.6", jobs[1].getResult().getSeverity() == Status.OK);

	}
	public void testComplexRuleContainment() {
		ISchedulingRule rules[] = new ISchedulingRule[4];

		rules[0] = new RuleSetA();
		rules[1] = new RuleSetB();
		rules[2] = new RuleSetC();
		rules[3] = new RuleSetD();

		//adding multiple rules in correct order
		int RULE_REPEATS = 10;
		try {
			for (int i = 0; i < rules.length - 1; i++) {
				for (int j = 0; j < RULE_REPEATS; j++) {
					manager.beginRule(rules[i]);
				}
			}
			for (int i = rules.length - 1; i > 0; i--) {
				for (int j = 0; j < RULE_REPEATS; j++) {
					manager.endRule(rules[i - 1]);
				}
			}
		} catch (RuntimeException e) {
			fail("4.0");
		}

		//adding rules in proper order, then adding a rule from a bypassed branch
		//trying to end previous rules should not work
		for (int i = 0; i < rules.length; i++) {
			manager.beginRule(rules[i]);
		}
		try {
			manager.endRule(rules[2]);
			fail("4.1");
		} catch (RuntimeException e) {
			//should fail
			try {
				manager.endRule(rules[1]);
				fail("4.2");
			} catch (RuntimeException e1) {
				//should fail
				try {
					manager.endRule(rules[0]);
					fail("4.3");
				} catch (RuntimeException e2) {
					//should fail
				}
			}
		}
		for (int i = rules.length; i > 0; i--) {
			manager.endRule(rules[i - 1]);
		}
	}
	public void _testEndNullRule() {
		//see bug #43460
		//end null IScheduleRule without begin
		try {
			manager.endRule(null);
			fail("1.1");
		} catch (RuntimeException e) {
			//should fail
		}
	}
	public void testFailureCase() {
		ISchedulingRule rule1 = new IdentityRule();
		ISchedulingRule rule2 = new IdentityRule();

		//end without begin
		try {
			manager.endRule(rule1);
			fail("1.0");
		} catch (RuntimeException e) {
			//should fail
		}
		//simple mismatched begin/end
		manager.beginRule(rule1);
		try {
			manager.endRule(rule2);
			fail("1.2");
		} catch (RuntimeException e) {
			//should fail
		}
		//should still be able to end the original rule
		manager.endRule(rule1);

		//mismatched begin/end, ending a null rule
		manager.beginRule(rule1);
		try {
			manager.endRule(null);
			fail("1.3");
		} catch (RuntimeException e) {
			//should fail
		}
		//should still be able to end the original rule
		manager.endRule(rule1);
	}
	public void testNestedCase() {
		ISchedulingRule rule1 = new RuleSetA();
		ISchedulingRule rule2 = new RuleSetB();

		//ending an outer rule before an inner one
		manager.beginRule(rule1);
		manager.beginRule(rule2);
		try {
			manager.endRule(rule1);
			fail("2.0");
		} catch (RuntimeException e) {
			//should fail
		}
		manager.endRule(rule2);
		manager.endRule(rule1);

		//ending a rule that is not open
		manager.beginRule(rule1);
		manager.beginRule(rule2);
		try {
			manager.endRule(null);
			fail("2.1");
		} catch (RuntimeException e) {
			//should fail
		}
		manager.endRule(rule2);
		manager.endRule(rule1);

		//adding rules starting with null rule
		try {
			manager.beginRule(null);
			manager.beginRule(rule1);
			manager.endRule(rule1);
			manager.beginRule(rule2);
			manager.endRule(rule2);
			manager.endRule(null);
		} catch (RuntimeException e) {
			//should not fail
			fail("2.2");
		}

		//adding numerous instances of the same rule
		int NUM_ADDITIONS = 100;
		try {
			for (int i = 0; i < NUM_ADDITIONS; i++) {
				manager.beginRule(rule1);
			}
			for (int i = 0; i < NUM_ADDITIONS; i++) {
				manager.endRule(rule1);
			}
		} catch (RuntimeException e) {
			//should not fail
			fail("2.3");
		}

		//adding numerous instances of the null rule
		try {
			for (int i = 0; i < NUM_ADDITIONS; i++) {
				manager.beginRule(null);
			}
			manager.beginRule(rule1);
			manager.endRule(rule1);
			for (int i = 0; i < NUM_ADDITIONS; i++) {
				manager.endRule(null);
			}
		} catch (RuntimeException e) {
			//should not fail
			fail("2.4");
		}
	}
	/**
	 * Tests a failure where canceling an attempt to beginRule resulted in implicit jobs
	 * being recycled before they were finished.
	 */
	public void testBug44299() {
		ISchedulingRule rule = new IdentityRule();
		FussyProgressMonitor monitor = new FussyProgressMonitor();
		manager.beginRule(rule, monitor);
		int[] status = new int[1];
		SimpleRuleRunner runner = new SimpleRuleRunner(rule, status, monitor);
		new Thread(runner).start();
		
		//wait for the job to start
		StatusChecker.waitForStatus(status, StatusChecker.STATUS_RUNNING);
		
		//give the job a chance to enter the wait loop
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		
		//cancel the monitor
		monitor.setCanceled(true);
		
		StatusChecker.waitForStatus(status, StatusChecker.STATUS_DONE);
		if (runner.exception != null)
			fail("1.0", runner.exception);
		
		//finally clear the rule
		manager.endRule(rule);
	}
	public void _testRuleContainment() {
		ISchedulingRule rules[] = new ISchedulingRule[4];

		rules[0] = new RuleSetA();
		rules[1] = new RuleSetB();
		rules[2] = new RuleSetC();
		rules[3] = new RuleSetD();

		//simple addition of rules in incorrect containment order
		manager.beginRule(rules[1]);
		try {
			manager.beginRule(rules[0]);
			fail("3.0");
		} catch (RuntimeException e) {
			//should fail
		}
		manager.endRule(rules[1]);

		//adding rules in proper order, then adding a rule from different hierarchy
		manager.beginRule(rules[1]);
		manager.beginRule(rules[2]);
		try {
			manager.beginRule(rules[3]);
			fail("3.2");
		} catch (RuntimeException e) {
			//should fail
		}
		//should still be able to end the rules
		manager.endRule(rules[2]);
		manager.endRule(rules[1]);

	}
	public void testSimpleOtherThreadAccess() {
		//ending a rule started on this thread from another thread
		ISchedulingRule rule1 = new IdentityRule();
		Thread endingThread = new Thread(new RuleEnder(rule1));
		manager.beginRule(rule1);
		endingThread.start();

		//should be able to end the rule from this thread
		manager.endRule(rule1);

		//starting several rules on this thread, and trying to end them from other threads
		ISchedulingRule rules[] = new ISchedulingRule[3];

		rules[0] = new RuleSetA();
		rules[1] = new RuleSetB();
		rules[2] = new RuleSetC();

		for (int i = 0; i < rules.length; i++) {
			manager.beginRule(rules[i]);
			(new Thread(new RuleEnder(rules[i]))).start();
		}

		for (int i = 0; i < rules.length; i++) {
			(new Thread(new RuleEnder(rules[i]))).start();
		}

		for (int i = rules.length; i > 0; i--) {
			manager.endRule(rules[i - 1]);
			(new Thread(new RuleEnder(rules[i - 1]))).start();
		}
	}

	/**
	 * A job has been scheduled.  Pause this thread so that a worker thread
	 * has a chance to pick up the new job.
	 */
	private void waitForStart(Job job) {
		int i = 0;
		while (job.getState() != Job.RUNNING) {
			try {
				//Thread.yield();
				Thread.sleep(100);
			} catch (InterruptedException e) {

			}

			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for job to start", i++ < 1000);
		}
	}
}