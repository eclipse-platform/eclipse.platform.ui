package org.eclipse.core.tests.runtime.locks;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import org.eclipse.core.internal.jobs.LockManager;
import org.eclipse.core.internal.jobs.OrderedLock;
import org.eclipse.core.runtime.jobs.ILock;

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
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		kill(allRunnables);
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
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		kill(allRunnables);
	}
	private void start(ArrayList allRunnables) {
		for (Iterator it = allRunnables.iterator(); it.hasNext();) {
			TestRunnable r = (TestRunnable) it.next();
			new Thread(r).start();
		}
	}
}