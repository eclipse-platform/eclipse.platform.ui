package org.eclipse.core.tests.runtime.jobs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.internal.jobs.LockManager;
import org.eclipse.core.internal.jobs.OrderedLock;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.FussyProgressMonitor;


import junit.framework.TestCase;

/**
 * Tests implementation of ILock objects
 */
public class DeadlockDetectionTest extends TestCase {
	public DeadlockDetectionTest() {
		super(null);
	}
	public DeadlockDetectionTest(String name) {
		super(name);
	}
	/**
	 * Creates n runnables on the given lock and adds them to the given list.
	 */
	private void createRunnables(ILock[] locks, int n, ArrayList allRunnables, boolean cond) {
		for (int i = 0; i < n; i++) {
			allRunnables.add(new RandomTestRunnable(locks, "# " + (allRunnables.size() + 1), cond));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				
			}
		}
	}
	/**
	 * Asks all threads to stop executing
	 */
	private void kill(ArrayList allRunnables) {
		for (Iterator it = allRunnables.iterator(); it.hasNext();) {
			RandomTestRunnable r = (RandomTestRunnable) it.next();
			r.kill();
		}
	}
	
	public void testComplex() {
		ArrayList allRunnables = new ArrayList();
		LockManager manager = new LockManager();
		OrderedLock lock1 = manager.newLock();
		OrderedLock lock2 = manager.newLock();
		OrderedLock lock3 = manager.newLock();
		OrderedLock lock4 = manager.newLock();
		OrderedLock lock5 = manager.newLock();
		OrderedLock lock6 = manager.newLock();
		createRunnables(new ILock[] { lock1, lock2, lock3 }, 1, allRunnables, true);
		createRunnables(new ILock[] { lock2, lock3, lock4 }, 1, allRunnables, true);
		createRunnables(new ILock[] { lock3, lock4, lock5 }, 1, allRunnables, true);
		createRunnables(new ILock[] { lock4, lock5, lock6 }, 1, allRunnables, true);
		createRunnables(new ILock[] { lock5, lock6, lock1 }, 1, allRunnables, true);
		createRunnables(new ILock[] { lock6, lock1, lock2 }, 1, allRunnables, true);
		start(allRunnables);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		kill(allRunnables);
		
		for(int i = 0; i < allRunnables.size(); i++) {
			try {
				((Thread)allRunnables.get(i)).join(100000);
			} catch (InterruptedException e1) {
			}
			assertTrue("1." + i, !((Thread)allRunnables.get(i)).isAlive());
		}
		//the underlying array has to be empty
		assertTrue("Locks not removed from graph.", manager.isEmpty());
	}
	
	public void testSimpleDeadlock() {
		ArrayList allRunnables = new ArrayList();
		LockManager manager = new LockManager();
		OrderedLock lock1 = manager.newLock();
		OrderedLock lock2 = manager.newLock();
		
		createRunnables(new ILock[] {lock1, lock2}, 1, allRunnables, false);
		createRunnables(new ILock[] {lock2, lock1}, 1, allRunnables, false);
		
		start(allRunnables);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		kill(allRunnables);
		
		for(int i = 0; i < allRunnables.size(); i++) {
			try {
				((Thread)allRunnables.get(i)).join(100000);
			} catch (InterruptedException e1) {
			}
			assertTrue("1." + i, !((Thread)allRunnables.get(i)).isAlive());
		}
		//the underlying array has to be empty
		assertTrue("Locks not removed from graph.", manager.isEmpty());
	}
	
	public void testThreeLocks() {
		ArrayList allRunnables = new ArrayList();
		LockManager manager = new LockManager();
		OrderedLock lock1 = manager.newLock();
		OrderedLock lock2 = manager.newLock();
		OrderedLock lock3 = manager.newLock();
		
		createRunnables(new ILock[] {lock1, lock2}, 1, allRunnables, false);
		createRunnables(new ILock[] {lock2, lock3}, 1, allRunnables, false);
		createRunnables(new ILock[] {lock3, lock1}, 1, allRunnables, false);
		
		start(allRunnables);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
		kill(allRunnables);
		
		for(int i = 0; i < allRunnables.size(); i++) {
			try {
				((Thread)allRunnables.get(i)).join(100000);
			} catch (InterruptedException e1) {
			}
			assertTrue("1." + i, !((Thread)allRunnables.get(i)).isAlive());
		}
		//the underlying array has to be empty
		assertTrue("Locks not removed from graph.", manager.isEmpty());
	}
	
	public void testRuleLockInteraction() {
		final JobManager manager = JobManager.getInstance();
		final ILock lock = manager.newLock();
		final ISchedulingRule rule = new IdentityRule();
		final int [] status = {StatusChecker.STATUS_WAIT_FOR_START, StatusChecker.STATUS_WAIT_FOR_START};
		
		Thread first = new Thread("Test1") {
			public void run() {
				lock.acquire();
				status[0] = StatusChecker.STATUS_START;
				assertTrue("1.0", manager.getLockManager().isLockOwner());
				StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_RUNNING, 100);
				manager.beginRule(rule, null);
				assertTrue("2.0", manager.getLockManager().isLockOwner());
				manager.endRule(rule);
				lock.release();
				status[0] = StatusChecker.STATUS_DONE;
			}
		};
		
		Thread second = new Thread("Test2") {
			public void run() {
				manager.beginRule(rule, null);
				status[1] = StatusChecker.STATUS_START;
				assertTrue("1.0", manager.getLockManager().isLockOwner());
				StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_RUNNING, 100);
				lock.acquire();
				assertTrue("2.0", manager.getLockManager().isLockOwner());
				lock.release();
				manager.endRule(rule);
				status[1] = StatusChecker.STATUS_DONE;
			}
		};
		
		first.start();
		second.start();
		
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_START, 100);
		StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_START, 100);
		
		status[0] = StatusChecker.STATUS_RUNNING;
		status[1] = StatusChecker.STATUS_RUNNING;
		
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_DONE, 100);
		StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_DONE, 100);
		waitForThreadDeath(first);
		waitForThreadDeath(second);
		assertTrue("3.0", !first.isAlive());
		assertTrue("4.0", !second.isAlive());
		//the underlying array has to be empty
		assertTrue("Jobs not removed from graph.", manager.getLockManager().isEmpty());	
	}
	
	public void testJobRuleLockInteraction() {
		final JobManager manager = JobManager.getInstance();
		final int [] status = {StatusChecker.STATUS_WAIT_FOR_START, StatusChecker.STATUS_WAIT_FOR_START};
		final ISchedulingRule rule1 = new IdentityRule();
		final ISchedulingRule rule2 = new IdentityRule();
		final ILock lock = manager.newLock();
		
		Job first = new Job("Test1") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					assertTrue("1.0", manager.getLockManager().isLockOwner());
					monitor.beginTask("Testing", 1);
					status[0] = StatusChecker.STATUS_START;
					lock.acquire();
					StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_RUNNING, 100);
					assertTrue("2.0", manager.getLockManager().isLockOwner());
					lock.release();
					monitor.worked(1);
					status[0] = StatusChecker.STATUS_DONE;
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		
		Job second = new Job("Test2") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					assertTrue("1.0", manager.getLockManager().isLockOwner());
					monitor.beginTask("Testing", 1);
					status[1] = StatusChecker.STATUS_START;
					lock.acquire();
					StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_RUNNING, 100);
					assertTrue("2.0", manager.getLockManager().isLockOwner());
					lock.release();
					monitor.worked(1);
					status[1] = StatusChecker.STATUS_DONE;
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		
		first.setRule(rule1);
		second.setRule(rule2);
		first.schedule();
		second.schedule();
		
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_START, 100);
		StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_START, 100);
		
		status[0] = StatusChecker.STATUS_RUNNING;
		status[1] = StatusChecker.STATUS_RUNNING;
		
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_DONE, 100);
		StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_DONE, 100);
		waitForCompletion(first);
		waitForCompletion(second);
		
		assertEquals("3.0", Job.NONE, first.getState());
		assertEquals("3.1", Status.OK_STATUS, first.getResult());
		assertEquals("4.0", Job.NONE, second.getState());
		assertEquals("4.1", Status.OK_STATUS, second.getResult());
		//the underlying array has to be empty
		assertTrue("Jobs not removed from graph.", manager.getLockManager().isEmpty());
	}
	public void testComplexRuleLockInteraction() {
		final JobManager manager = JobManager.getInstance();
		final int NUM_LOCKS = 5;
		final int [] status = {StatusChecker.STATUS_WAIT_FOR_START};
		final ISchedulingRule[] rules = {new IdentityRule(), new IdentityRule(), new IdentityRule(), new IdentityRule(), new IdentityRule()};
		final ILock[] locks = {manager.newLock(), manager.newLock(), manager.newLock(), manager.newLock(), manager.newLock()};
		Job[] jobs = new Job[NUM_LOCKS*3];
		final Random random = new Random();
		
		for(int i = 0; i < jobs.length; i++) {
			jobs[i] = new Job("Test"+i) {
				protected IStatus run(IProgressMonitor monitor) {
					try {
						monitor.beginTask("Testing", IProgressMonitor.UNKNOWN);
						while(status[0] != StatusChecker.STATUS_DONE) {
							int indexRule = random.nextInt(NUM_LOCKS);
							int indexLock = random.nextInt(NUM_LOCKS);
							int secondIndex = random.nextInt(NUM_LOCKS);
							if((indexRule%2) == 0) {
								manager.beginRule(rules[indexRule], null);
								locks[indexLock].acquire();
								locks[secondIndex].acquire();
								assertTrue(indexRule + ".0", manager.getLockManager().isLockOwner());
								locks[secondIndex].release();
								locks[indexLock].release();
								manager.endRule(rules[indexRule]);
							}
							else {
								locks[indexLock].acquire();
								manager.beginRule(rules[indexRule], null);
								locks[secondIndex].acquire();
								assertTrue(indexLock + ".0", manager.getLockManager().isLockOwner());
								locks[secondIndex].release();
								manager.endRule(rules[indexRule]);
								locks[indexLock].release();
							}
							monitor.worked(1);
						}
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			};
			jobs[i].schedule();
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			
		}
		
		status[0] = StatusChecker.STATUS_DONE;
		
		for(int i = 0; i < jobs.length; i++) {
			int j = 0;
			while(jobs[i].getState() != Job.NONE) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					
				}
				//sanity check to avoid hanging tests
				assertTrue("Timeout waiting for jobs to finish.", ++j < 1000);
			}
		}
			
		for(int i = 0; i < jobs.length; i++) {
			assertEquals("10." + i, Job.NONE, jobs[i].getState());
			assertEquals("10." + i, Status.OK_STATUS, jobs[i].getResult());
		}
		//the underlying array has to be empty
		assertTrue("Jobs not removed from graph.", manager.getLockManager().isEmpty());
	}
	
	public void testVeryComplex() {
		ArrayList allRunnables = new ArrayList();
		LockManager manager = new LockManager();
		OrderedLock lock1 = manager.newLock();
		OrderedLock lock2 = manager.newLock();
		OrderedLock lock3 = manager.newLock();
		OrderedLock lock4 = manager.newLock();
		OrderedLock lock5 = manager.newLock();
		OrderedLock lock6 = manager.newLock();
		createRunnables(new ILock[] { lock1, lock2, lock3 }, 10, allRunnables, true);
		createRunnables(new ILock[] { lock2, lock3, lock4 }, 10, allRunnables, true);
		createRunnables(new ILock[] { lock3, lock4, lock5 }, 10, allRunnables, true);
		createRunnables(new ILock[] { lock4, lock5, lock6 }, 10, allRunnables, true);
		createRunnables(new ILock[] { lock5, lock6, lock1 }, 10, allRunnables, true);
		createRunnables(new ILock[] { lock6, lock1, lock2 }, 10, allRunnables, true);
		start(allRunnables);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		kill(allRunnables);
		
		for(int i = 0; i < allRunnables.size(); i++) {
			try {
				((Thread)allRunnables.get(i)).join(100000);
			} catch (InterruptedException e1) {
			}
			assertTrue("1." + i, !((Thread)allRunnables.get(i)).isAlive());
		}
		//the underlying array has to be empty
		assertTrue("Locks not removed from graph.", manager.isEmpty());
	}
	/**
	 * Spin until the given job completes
	 */
	private void waitForCompletion(Job job) {
		int i = 0;
		while(job.getState() != Job.NONE) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			assertTrue("Timeout waiting for job to end.", ++i < 100);
		}
	}
	/**
	 * Spin until the given thread dies
	 */
	private void waitForThreadDeath(Thread thread) {
		int i = 0;
		while(thread.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			assertTrue("Timeout waiting for job to end.", ++i < 100);
		}
	}
	
	public void testJobRuleCancellation() {
		final JobManager manager = JobManager.getInstance();
		final ISchedulingRule rule = new IdentityRule();
		final int[] status = {StatusChecker.STATUS_WAIT_FOR_START};
		
		Job first = new Job("Test1") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					assertTrue("1.0", manager.getLockManager().isLockOwner());
					status[0] = StatusChecker.STATUS_START;
					StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_RUNNING, 1000);
					monitor.worked(1);
				} finally {
					monitor.done();
					status[0] = StatusChecker.STATUS_DONE;
				}
				return Status.OK_STATUS;
			}
		};
		
		Job second = new Job("Test2") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					assertTrue("2.0", manager.getLockManager().isLockOwner());
					monitor.worked(1);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		
		first.setRule(rule);
		second.setRule(rule);
		
		first.schedule();
		StatusChecker.waitForStatus(status, StatusChecker.STATUS_START);
				
		//schedule a job with the same rule and then cancel it
		second.schedule();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		second.cancel();
		status[0] = StatusChecker.STATUS_RUNNING;
		StatusChecker.waitForStatus(status, StatusChecker.STATUS_DONE);
		waitForCompletion(first);
		//the underlying graph should now be empty
		assertTrue("Cancelled job not removed from graph.", manager.getLockManager().isEmpty());
	}
	
	public void testBeginRuleCancelAfterWait() {
		final JobManager manager = JobManager.getInstance();
		final ISchedulingRule rule1 = new RuleSetA();
		final ISchedulingRule rule2 = new RuleSetB();
		RuleSetA.conflict = true;
		final int[] status = {StatusChecker.STATUS_WAIT_FOR_START, StatusChecker.STATUS_WAIT_FOR_START};
		final IProgressMonitor canceller = new FussyProgressMonitor();
		
		Job ruleOwner = new Job("Test1") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					status[0] = StatusChecker.STATUS_START;
					manager.beginRule(rule1, null);
					StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_RUNNING, 1000);
					manager.endRule(rule1);
					monitor.worked(1);
				} finally {
					monitor.done();
					status[0] = StatusChecker.STATUS_DONE;
				}
				return Status.OK_STATUS;
			}
		};
		
		Job ruleWait = new Job("Test2") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					status[1] = StatusChecker.STATUS_RUNNING;
					manager.beginRule(rule2, canceller);
					monitor.worked(1);
				} finally {
					monitor.done();
					status[1] = StatusChecker.STATUS_DONE;
				}
				return Status.OK_STATUS;
			}
		};
		
		ruleOwner.schedule();
		StatusChecker.waitForStatus(status, StatusChecker.STATUS_START);
		
		//schedule a job that is going to begin a conflicting rule and then cancel the wait
		ruleWait.schedule();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		//cancel the wait for the rule
		canceller.setCanceled(true);
		//wait until the job completes
		StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_DONE, 100);
		
		//let the first job finish
		status[0] = StatusChecker.STATUS_RUNNING;
		StatusChecker.waitForStatus(status, StatusChecker.STATUS_DONE);
		int i = 0;
		waitForCompletion(ruleOwner);
		RuleSetA.conflict = false;
		//the underlying graph should now be empty
		assertTrue("Cancelled rule not removed from graph.", manager.getLockManager().isEmpty());
	}
	
	private void start(ArrayList allRunnables) {
		for (Iterator it = allRunnables.iterator(); it.hasNext();) {
			RandomTestRunnable r = (RandomTestRunnable) it.next();
			r.start();
		}
	}
}
