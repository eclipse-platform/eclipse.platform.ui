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

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import org.eclipse.core.internal.jobs.LockManager;
import org.eclipse.core.internal.jobs.OrderedLock;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.LockListener;

/**
 * Tests implementation of ILock objects
 */
public class OrderedLockTest extends TestCase {
	public OrderedLockTest() {
		super(null);
	}
	public OrderedLockTest(String name) {
		super(name);
	}
	/**
	 * Creates n runnables on the given lock and adds them to the given list.
	 */
	private void createRunnables(ILock[] locks, int n, ArrayList allRunnables) {
		for (int i = 0; i < n; i++)
			allRunnables.add(new TestRunnable(locks));
	}
	private void kill(ArrayList allRunnables) {
		for (Iterator it = allRunnables.iterator(); it.hasNext();) {
			TestRunnable r = (TestRunnable) it.next();
			r.kill();
		}
	}
	public void testComplex() {
		ArrayList allRunnables = new ArrayList();
		LockManager manager = new LockManager();
		OrderedLock lock1 = manager.newLock();
		OrderedLock lock2 = manager.newLock();
		OrderedLock lock3 = manager.newLock();
		createRunnables(new ILock[] { lock1, lock2, lock3 }, 5, allRunnables);
		createRunnables(new ILock[] { lock3, lock2, lock1 }, 5, allRunnables);
		createRunnables(new ILock[] { lock1, lock3, lock2 }, 5, allRunnables);
		createRunnables(new ILock[] { lock2, lock3, lock1 }, 5, allRunnables);
		start(allRunnables);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		kill(allRunnables);
		for(int i = 0; i < allRunnables.size(); i++) {
			((TestRunnable)allRunnables.get(i)).isDone();
		}
		//the underlying array has to be empty
		assertTrue("Locks not removed from graph.", manager.isEmpty());
	}
	public void testSimple() {
		ArrayList allRunnables = new ArrayList();
		LockManager manager = new LockManager();
		OrderedLock lock1 = manager.newLock();
		OrderedLock lock2 = manager.newLock();
		OrderedLock lock3 = manager.newLock();
		createRunnables(new ILock[] { lock1, lock2, lock3 }, 1, allRunnables);
		createRunnables(new ILock[] { lock3, lock2, lock1 }, 1, allRunnables);
		start(allRunnables);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		kill(allRunnables);
		for(int i = 0; i < allRunnables.size(); i++) {
			((TestRunnable)allRunnables.get(i)).isDone();
		}
		//the underlying array has to be empty
		assertTrue("Locks not removed from graph.", manager.isEmpty());
	}
	
	/**
	 * test that an acquire call that times out does not 
	 * become the lock owner (regression test)
	 */
	public void testLockTimeout() {
		//create a new lock manager and 1 lock
		final LockManager manager = new LockManager();
		final OrderedLock lock = manager.newLock();
		//status array for communicating between threads
		final int[] status = {StatusChecker.STATUS_START};
		//array to end a runnable after it is no longer needed
		final boolean[] alive = {true};
		
		//first runnable which is going to hold the created lock
		Runnable getLock = new Runnable() {
			public void run() {
				lock.acquire();
				status[0] = StatusChecker.STATUS_RUNNING;
				while(alive[0]) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						
					}
				}
				lock.release();
				status[0] = StatusChecker.STATUS_DONE;
			}
		};
		
		//second runnable which is going to try and acquire the given lock and then time out
		Runnable tryForLock = new Runnable() {
			public void run() {
				boolean success = false;
				try {
					success = lock.acquire(100);
				} catch (InterruptedException e) {
					
				}
				assertTrue("1.0", !success);
				assertTrue("1.1", !manager.isLockOwner());
				status[0] = StatusChecker.STATUS_WAIT_FOR_DONE;
			}
		};
		
		Thread first = new Thread(getLock);
		Thread second = new Thread(tryForLock);
		
		//start the first thread and wait for it to acquire the lock
		first.start();
		StatusChecker.waitForStatus(status, StatusChecker.STATUS_RUNNING);
		//start the second thread, make sure the assertion passes
		second.start();
		StatusChecker.waitForStatus(status, StatusChecker.STATUS_WAIT_FOR_DONE);
		//let the first thread die
		alive[0] = false;
		StatusChecker.waitForStatus(status, StatusChecker.STATUS_DONE);
		//the underlying array has to be empty
		assertTrue("Locks not removed from graph.", manager.isEmpty());
	}
	
	/**
	 * test that when a Lock Listener forces the Lock Manager to grant a lock
	 * to a waiting thread, that other threads in the queue don't get disposed (regression test)
	 */
	public void testLockRequestDisappearence() {
		//create a new lock manager and 1 lock
		final LockManager manager = new LockManager();
		final OrderedLock lock = manager.newLock();
		//status array for communicating between threads
		final int[] status = {StatusChecker.STATUS_WAIT_FOR_START, StatusChecker.STATUS_WAIT_FOR_START};
				
		//first runnable which is going to hold the created lock
		Runnable getLock = new Runnable() {
			public void run() {
				lock.acquire();
				status[0] = StatusChecker.STATUS_START;
				StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_RUNNING, 100);
				lock.release();
				status[0] = StatusChecker.STATUS_DONE;
			}
		};
		
		//second runnable which is going to submit a request for this lock and wait until it is available
		Runnable waitForLock = new Runnable() {
			public void run() {
				status[1] = StatusChecker.STATUS_START;
				lock.acquire();
				assertTrue("1.0", manager.isLockOwner());
				lock.release();
				status[1] = StatusChecker.STATUS_DONE;
				
			}
		};
		
		//third runnable which is going to submit a request for this lock but not wait 
		//because the hook is going to force it to be given the lock (implicitly)
		Runnable forceGetLock = new Runnable() {
			public void run() {
				lock.acquire();
				lock.release();
				status[0] = StatusChecker.STATUS_WAIT_FOR_DONE;
			}
		};
		
		//a locklistener to force lock manager to give the lock to the third runnable (implicitly)
		LockListener listener = new LockListener() {
			public boolean aboutToWait(Thread lockOwner)  {
				return true;
			}
		};
		
		//assign each runnable to a separate thread
		Thread first = new Thread(getLock);
		Thread second = new Thread(waitForLock);
		Thread third = new Thread(forceGetLock);
		
		//start the first thread and wait for it to acquire the lock
		first.start();
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_START, 100);
		//start the second thread, make sure it is added to the lock wait queue
		second.start();
		StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_START, 100);
		
		//assign our listener to the manager
		manager.setLockListener(listener);
		//start the third thread
		third.start();
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_WAIT_FOR_DONE, 100);
		
		//let the first runnable complete
		status[0] = StatusChecker.STATUS_RUNNING;
		StatusChecker.waitForStatus(status, 0, StatusChecker.STATUS_DONE, 100);
		
		//now wait for the second runnable to get the lock, and have the assertion pass
		StatusChecker.waitForStatus(status, 1, StatusChecker.STATUS_DONE, 100);
		
		//the underlying array has to be empty
		assertTrue("Locks not removed from graph.", manager.isEmpty());
	}
	
	private void start(ArrayList allRunnables) {
		for (Iterator it = allRunnables.iterator(); it.hasNext();) {
			TestRunnable r = (TestRunnable) it.next();
			new Thread(r).start();
		}
	}
}