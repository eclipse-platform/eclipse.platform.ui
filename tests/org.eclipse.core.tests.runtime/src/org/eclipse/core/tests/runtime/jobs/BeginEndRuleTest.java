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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Tests API methods IJobManager.beginRule and IJobManager.endRule
 */
public class BeginEndRuleTest extends AbstractJobManagerTest {
	
	private class JobRuleRunner extends Job{
		private ISchedulingRule rule;
		private int[] status;
			
		/**
		 * This job will start applying the given rule in the manager
		*/	 
		public JobRuleRunner(String name, ISchedulingRule rule, int [] status) {
			super(name);
			this.status = status;
			this.rule = rule;
		}
			
		protected IStatus run(IProgressMonitor monitor) {
			
			monitor.beginTask(getName(), 10);
			try {
				for (int i = 0; i < 10; i++) {
					status[0] = 2;
					manager.beginRule(rule);
					status[0] = 3;
					monitor.subTask("Tick: " + i);
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					monitor.worked(1);
					status[0] = 4;
					manager.endRule(rule);
					status[0] = 1;
					Thread.yield();
				}
						
			} finally {
				monitor.done();
				/*for(int i = 0; i < 10; i++) {
					manager.endRule(rule);
				}*/
			}
			return Status.OK_STATUS;
		}

		
	}
	
	private class RuleEnder implements Runnable {
		private ISchedulingRule rule;
		
		/**
		 * This runnable will try to end the given rule in the Job Manager
		*/	 
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
		
		final int[] status1 = {0};
		final int[] status2 = {0};
		final int[] status3 = {0};
		final int[] status4 = {0};
		
		RuleSetA.conflict = true;
		Job job1 = new JobRuleRunner("Job1", new RuleSetB(), status1);
		Job job2 = new JobRuleRunner("Job2", new RuleSetC(), status2);
		Job job3 = new JobRuleRunner("Job3", new RuleSetD(), status3);
		Job job4 = new JobRuleRunner("Job4", new RuleSetE(), status4);
						
		job1.schedule();
		job2.schedule();
		job3.schedule();
		job4.schedule();
		
		waitForStart(job1);
		assertTrue("1.0", status1[0] > 0);
		waitForStart(job2);
		assertTrue("2.0", status2[0] > 0);
		waitForStart(job3);
		assertTrue("3.0", status3[0] > 0);
		waitForStart(job4);
		assertTrue("4.0", status4[0] > 0);
				
		while(manager.currentJob() != null) {
			try {
				assertTrue("5.0 : " + status1[0] + " , " + status2[0] + " , " + status3[0] + " , " + status4[0], 
				((status1[0] > 2) && (status2[0] < 3) && (status3[0] < 3) && (status4[0] < 3)) 
				|| ((status1[0] < 3) && (status2[0] > 2) && (status3[0] < 3) && (status4[0] < 3))
				|| ((status1[0] < 3) && (status2[0] < 3) && (status3[0] > 2) && (status4[0] < 3))
				|| ((status1[0] < 3) && (status2[0] < 3) && (status3[0] < 3) && (status4[0] > 2)));
				
				//Thread.yield();
				Thread.sleep(100);
			} catch(InterruptedException e) {
			
			}
		}
		
		assertTrue("6.1", status1[0] == 1);
				
//		while(job2.getState() != Job.NONE) {
//			try {
//				Thread.sleep(100);
//			} catch(InterruptedException e) {
//			
//			}
//		}
//		
		assertTrue("6.2", status2[0] == 1);
//		
//		while(job3.getState() != Job.NONE) {
//			try {
//				Thread.sleep(100);
//			} catch(InterruptedException e) {
//			
//			}
//		}
//		
		assertTrue("6.3", status3[0] == 1);
		
//		while(job4.getState() != Job.NONE) {
//			try {
//				Thread.sleep(100);
//			} catch(InterruptedException e) {
//			
//			}
//		}
		
		assertTrue("6.4", status4[0] == 1);
		
		assertTrue("7.1", job1.getResult().isOK());
		assertTrue("7.2", job2.getResult().isOK());
		assertTrue("7.3", job3.getResult().isOK());
		assertTrue("7.4", job4.getResult().isOK());
		
	}
	
	public void _testSimpleRuleStarting() {
		//start two jobs, each of which will begin and end a rule several times
		//while one job starts a rule, the second job's call to begin rule should block that thread
		//until the first job calls end rule
		final int[] status1 = {0};
		final int[] status2 = {0};
		
		RuleSetA.conflict = true;
		Job job1 = new JobRuleRunner("Job1", new RuleSetB(), status1);
		Job job2 = new JobRuleRunner("Job2", new RuleSetD(), status2);
						
		//schedule both jobs to start their execution
		job1.schedule();
		job2.schedule();
		
		waitForStart(job1);
		assertTrue("1.0", status1[0] > 0);
		waitForStart(job2);
		assertTrue("1.1", status2[0] > 0);
		
		
		assertTrue("2.0", job1.getState() == Job.RUNNING);
		assertTrue("2.1", job2.getState() == Job.RUNNING);
		
		while(manager.currentJob() != null) {
			try {
				//only one job can be executing at a time, the other one should be blocked
				assertTrue("3.0", ((status1[0] < 3) && (status2[0] > 2)) || ((status1[0] > 2) && (status2[0] < 3)));
				
				Thread.yield();
				Thread.sleep(100);
			} catch(InterruptedException e) {
			
			}
		}
		
		assertTrue("3.0", status1[0] == 1);
				
//		while(job2.getState() != Job.NONE) {
//			try {
//				Thread.sleep(100);
//			} catch(InterruptedException e) {
//			
//			}
//		}
		
		assertTrue("4.0", status2[0] == 1);
		
		assertTrue("5.0", job1.getState() == Job.NONE);
		assertTrue("6.0", job2.getState() == Job.NONE);
		
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
			for(int i = 0; i < rules.length-1; i++) {
				for(int j = 0; j < RULE_REPEATS; j++) {
					manager.beginRule(rules[i]);
				}
			}
			for(int i = rules.length-1; i > 0; i--) {
				for(int j = 0; j < RULE_REPEATS; j++) {
					manager.endRule(rules[i-1]);
				}
			}
		} catch(RuntimeException e) {
			fail("4.0");
		}
		
		
		//adding rules in proper order, then adding a rule from a bypassed branch
		//trying to end previous rules should not work
		for(int i = 0; i < rules.length; i++) {
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
			} catch(RuntimeException e1) {
				//should fail
				try {
					manager.endRule(rules[0]);
					fail("4.3");
				} catch(RuntimeException e2) {
					//should fail
				}
			}
		}
		for(int i = rules.length; i > 0; i--) {
			manager.endRule(rules[i-1]);
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
		} catch(RuntimeException e) {
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
		} catch(RuntimeException e) {
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
			for(int i = 0; i <  NUM_ADDITIONS; i++) {
				manager.beginRule(rule1);
			}
			for(int i = 0; i <  NUM_ADDITIONS; i++) {
				manager.endRule(rule1);
			}
		} catch(RuntimeException e) {
			//should not fail
			fail("2.3");
		}
		
		//adding numerous instances of the null rule
		try {
			for(int i = 0; i <  NUM_ADDITIONS; i++) {
				manager.beginRule(null);
			}
			manager.beginRule(rule1);
			manager.endRule(rule1);
			for(int i = 0; i <  NUM_ADDITIONS; i++) {
				manager.endRule(null);
			}
		} catch(RuntimeException e) {
			//should not fail
			fail("2.4");
		}
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
		} catch(RuntimeException e) {
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
				
		for(int i = 0; i < rules.length; i++) {
			manager.beginRule(rules[i]);
			(new Thread(new RuleEnder(rules[i]))).start();
		}
		
		for(int i = 0; i < rules.length; i++) {
			(new Thread(new RuleEnder(rules[i]))).start();
		}
		
		for(int i = rules.length; i > 0; i--) {
			manager.endRule(rules[i-1]);
			(new Thread(new RuleEnder(rules[i-1]))).start();
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
				Thread.yield();
				Thread.sleep(100);
			} catch(InterruptedException e) {
				
			}
			
			//sanity test to avoid hanging tests
			assertTrue("Timeout waiting for job to start", i++ < 1000);
		}
	}	
}