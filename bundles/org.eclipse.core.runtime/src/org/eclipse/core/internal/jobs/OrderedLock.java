package org.eclipse.core.internal.jobs;

import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.core.runtime.jobs.ILock;

/**
 * A lock used to control write access to an exclusive resource.
 * 
 * The lock avoids circular waiting deadlocks by ensuring that locks
 * are always acquired in a strict order.  This makes it impossible for n such 
 * locks to deadlock while waiting for each other.  The downside is that this means
 * that during an interval when a process owns a lock, it can be forced
 * to give the lock up and wait until all locks it requires become
 * available.  This removes the feature of exclusive access to the
 * resource in contention for the duration between acquire() and
 * release() calls.
 * 
 * The lock implementation prevents starvation by granting the
 * lock in the same order in which acquire() requests arrive. In
 * this scheme, starvation is only possible if a thread retains
 * a lock indefinitely.
 */
public class OrderedLock implements ILock {
	private static final boolean DEBUG = false;
	/**
	 * Records the number of successive acquires in the same
	 * thread. The thread is released only when the depth
	 * reaches zero.
	 */
	private int depth;
	/**
	 * Locks are sequentially ordered for debugging purposes.
	 */
	private static int nextLockNumber = 0;
	/**
	 * The thread of the operation that currently owns the lock.
	 */
	private Thread currentOperationThread;
	/**
	 * The manager that implements the circular wait protocol.
	 */
	private final LockManager manager;
	private final int number;
	/**
	 * Queue of semaphores for operations currently waiting
	 * on the lock.
	 */
	private final Queue operations = new Queue();

	/**
	 * Returns a new workspace lock.
	 */
	protected OrderedLock(LockManager manager) {
		this.manager = manager;
		this.number = nextLockNumber++;
	}
	/* (non-Javadoc)
	 * @see ILock#acquire(long)
	 */
	public boolean acquire(long delay) throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException();
		boolean success = true;
		if (delay <= 0) {
			success = attempt();
		} else {
			Semaphore semaphore = createSemaphore();
			if (semaphore != null) {
				if (DEBUG)
					System.out.println("[" + Thread.currentThread() + "] Operation waiting to be executed... :-/"); //$NON-NLS-1$ //$NON-NLS-2$
				//free all greater locks that this thread currently holds
				LockManager.LockState[] oldLocks = manager.suspendGreaterLocks(this);
				//now it is safe to acquire this lock
				success = doAcquire(semaphore, delay);
				//finally, re-acquire the greater locks that we freed earlier
				if (oldLocks != null)
					for (int i = 0; i < oldLocks.length; i++)
						oldLocks[i].resume();
				if (DEBUG)
					System.out.println("[" + Thread.currentThread() + "] Operation started... :-)"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (success)
			depth++;
		return success;
	}
	/* (non-Javadoc)
	 * @see ILock#acquire
	 */
	public void acquire() {
		//spin until the lock is successfully acquired
		//NOTE: spinning here allows the UI thread to service pending syncExecs
		//if the UI thread is trying to acquire a lock.
		while (true) {
			try {
				if (acquire(Long.MAX_VALUE))
					return;
			} catch (InterruptedException e) {
				//clear the interrupted state
				Thread.interrupted();
			}
		}
	}
	/**
	 * Attempts to acquire the lock.  Returns false if the lock is not available and
	 * true if the lock has been successfully acquired.
	 */
	protected synchronized boolean attempt() {
		//return null if we already own the lock
		if (currentOperationThread == Thread.currentThread())
			return true;
		//if nobody is waiting, grant the lock immediately
		if (currentOperationThread == null && operations.isEmpty()) {
			currentOperationThread = Thread.currentThread();
			return true;
		}
		return false;
	}
	/**
	 * Returns null if acquired and a Semaphore object otherwise.
	 */
	protected synchronized Semaphore createSemaphore() {
		return attempt() ? null : enqueue(new Semaphore(Thread.currentThread()));
	}
	/**
	 * Attempts to acquire this lock.  Callers will block  until this lock comes available to 
	 * them, or until the specified delay has elapsed.  A negative delay indicates an
	 * infinite delay.
	 */
	protected boolean doAcquire(Semaphore semaphore, long delay) throws InterruptedException {
		if (semaphore != null) {
			boolean success = false;
			//notifiy hook to service pending syncExecs before falling asleep
			manager.aboutToWait(getCurrentOperationThread());
			try {
				success = semaphore.acquire(delay);
			} catch (InterruptedException e) {
				if (DEBUG)
					System.out.println("[" + Thread.currentThread() + "] Operation interrupted while waiting... :-|"); //$NON-NLS-1$ //$NON-NLS-2$
				throw e;
			}
			updateCurrentOperation();
			return success;
		}
		return true;
	}
	/**
	 * If there is another semaphore with the same runnable in the
	 * queue, the other is returned and the new one is not added.
	 */
	private synchronized Semaphore enqueue(Semaphore newSemaphore) {
		Semaphore semaphore = (Semaphore) operations.get(newSemaphore);
		if (semaphore == null) {
			operations.enqueue(newSemaphore);
			return newSemaphore;
		}
		return semaphore;
	}
	/**
	 * Force this lock to release, regardless of depth.  Returns the current depth.
	 */
	protected synchronized int doRelease() {
		//notifiy hook
		manager.aboutToRelease();
		int oldDepth = depth;
		depth = 0;
		Semaphore next = (Semaphore) operations.peek();
		currentOperationThread = null;
		if (next != null)
			next.release();
		return oldDepth;
	}
	/**
	 * Returns the thread of the current operation, or <code>null</code> if
	 * there is no current operation
	 */
	public synchronized Thread getCurrentOperationThread() {
		return currentOperationThread;
	}
	public synchronized int getDepth()  {
		return depth;
	}
	/* (non-Javadoc)
	 * @see ILock#release
	 */
	public synchronized void release() {
		if (currentOperationThread != Thread.currentThread() || depth == 0)
			return;
		//only release the lock when the depth reaches zero
		Assert.isTrue(depth >= 0, "Lock released too many times"); //$NON-NLS-1$
		if (--depth == 0)
			doRelease();
	}
	/**
	 * Forces the lock to be at the given depth.  Used when re-acquiring a suspended
	 * lock.
	 */
	protected void setDepth(int newDepth) {
		this.depth = newDepth;
	}
	/**
	 * For debugging purposes only.
	 */
	public String toString() {
		return "OrderedLock(" + number + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	/**
	 * Removes the waiting operation from the queue
	 * and updates the current operation thread.
	 */
	private synchronized void updateCurrentOperation() {
		operations.dequeue();
		currentOperationThread = Thread.currentThread();
	}
}