package org.eclipse.core.internal.jobs;

import java.util.ArrayList;

import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.core.runtime.jobs.ILockListener;

/**
 * Manages a set of locks and ensures that deadlock never occurs.
 */
public class LockManager implements ILockListener {
	/**
	 * This class captures the state of suspended locks.  Locks are suspended if
	 * a thread tries to acquire locks out of order.
	 */
	public static class LockState {
		private int depth;
		private OrderedLock lock;
		/**
		 * Suspends ownership of the given lock, and returns the saved state.
		 */
		protected static LockState suspend(OrderedLock lock) {
			LockState state = new LockState();
			state.lock = lock;
			state.depth = lock.doRelease();
			return state;
		}
		/**
		 * Re-acquires a suspended lock and reverts to the correct lock depth.
		 */
		protected void resume() {
			//spin until the lock is successfully acquired
			//NOTE: spinning here allows the UI thread to service pending syncExecs
			//if the UI thread is waiting to acquire a lock.
			while (true) {
				try {
					if (lock.doAcquire(lock.createSemaphore(), Long.MAX_VALUE))
						break;
				} catch (InterruptedException e) {
				}
			}
			lock.setDepth(depth);
		}
	}
	private ILockListener lockListener;
	private final ArrayList locks = new ArrayList();
	public LockManager() {
	}
	/* (non-Javadoc)
	 * Method declared on ILockListener
	 */
	public void aboutToRelease() {
		if (lockListener != null)
			lockListener.aboutToRelease();
	}
	/* (non-Javadoc)
	 * Method declared on ILockListener
	 */
	public void aboutToWait(Thread lockOwner) {
		if (lockListener != null)
			lockListener.aboutToWait(lockOwner);
	}
	public synchronized OrderedLock newLock() {
		OrderedLock result = new OrderedLock(this);
		locks.add(result);
		return result;
	}
	public void setLockListener(ILockListener listener) {
		this.lockListener = listener;
	}
	/**
	 * The current thread is attempting to acquire the given lock.
	 * If this thread holds any locks greater than the given lock,
	 * release them, and build a list of locks that need to be
	 * acquired, in ascending order.  This ensures deadlock
	 * can never occur because locks are always acquired in
	 * ascending order.
	 * @return the list of locks that need to be acquired, or null if
	 * no other locks need to be acquired
	 */
	public synchronized LockState[] suspendGreaterLocks(OrderedLock toLock) {
		Thread currentThread = Thread.currentThread();
		//find the given lock in the lock list
		int lockCount = locks.size();
		int i = locks.indexOf(toLock);
		if (i == -1) {
			//we didn't find the given lock
			Assert.isTrue(false, "OrderedLock not found: " + toLock); //$NON-NLS-1$
			return null;
		}
		//gather all locks greater than the requested lock
		i++;
		ArrayList toAcquire = null;
		for (; i < lockCount; i++) {
			OrderedLock lock = (OrderedLock) locks.get(i);
			if (lock.getCurrentOperationThread() == currentThread) {
				if (toAcquire == null)
					toAcquire = new ArrayList();
				//release this lock so contending threads can access it, and remember old depth
				toAcquire.add(LockState.suspend(lock));
			}
		}
		if (toAcquire == null)
			return null;
		return (LockState[]) toAcquire.toArray(new LockState[toAcquire.size()]);
	}
}