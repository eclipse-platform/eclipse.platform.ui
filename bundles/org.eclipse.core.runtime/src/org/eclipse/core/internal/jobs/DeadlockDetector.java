/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

class DeadlockDetector {
	private static int NO_STATE = 0;
	//state variables in the graph
	private static int WAITING_FOR_LOCK = -10;
	//whether deadlock exists between the threads that are in the graph
	private boolean deadlockExists = false;
	//matrix of relationships between threads and locks
	private int[][] graph = new int[0][0];
	//index is column in adjacency matrix for the lock
	private final ArrayList locks = new ArrayList();
	//index is row in adjacency matrix for the thread
	private final ArrayList lockThreads = new ArrayList();
	//whether the graph needs to be resized
	private boolean resize = false;

	/**
	 * Add the thread that owns the given lock to the list of threads that are
	 * waiting for some action. (these threads cannot continue until the lock
	 * they are waiting for is free)
	 */
	private boolean addWaitingThread(int[] waitingThreads, int column) {
		boolean found = false;
		int index = -1;
		//find the thread that owns the lock with the given index
		//(go through all the threads in the graph as a type of error
		// checking)
		for (int i = 0; i < graph.length; i++) {
			if (graph[i][column] > NO_STATE) {
				Assert.isTrue(!found, "The lock " + locks.get(column).toString() + " is owned by 2 threads."); //$NON-NLS-1$ //$NON-NLS-2$
				found = true;
				index = i;
			}
		}
		//matrix could be in an unsynchronized state so a lock could be
		// unowned (index of -1)
		if (index < NO_STATE)
			return false;
		if (waitingThreads[index] > NO_STATE)
			deadlockExists = true;
		waitingThreads[index] = 1;
		return true;
	}
	/**
	 * Get the thread that owns the lock this thread is waiting for.
	 */
	private Thread blockingThread(Thread current) {
		ISchedulingRule lock = (ISchedulingRule) getWaitingLock(current);
		return getThreadOwningLock(lock);
	}
	/**
	 * Check that the addition of a waiting thread did not pruduce deadlock. If
	 * deadlock is detected, the deadlockExists variable is set to true.
	 */
	private void checkWaitCycles(int[] waitingThreads, int column) {
		if (!addWaitingThread(waitingThreads, column))
			return;
		if (deadlockExists)
			return;

		for (int i = 0; i < graph.length; i++) {
			if (graph[i][column] > NO_STATE) {
				for (int j = 0; j < graph[i].length; j++) {
					if (graph[i][j] == WAITING_FOR_LOCK) {
						checkWaitCycles(waitingThreads, j);
					}
					if (deadlockExists)
						break;
				}
			}
		}
	}
	/**
	 * Combine the entries for two conflicting rule columns into a single column.
	 */
	private void combineColumns(int from, int to) {
		for (int i = 0; i < graph.length; i++) {
			if ((graph[i][to] != 0) && (graph[i][from] != 0))
				Assert.isLegal(false, "Incorrect Graph."); //$NON-NLS-1$

			graph[i][to] += graph[i][from];
			graph[i][from] = NO_STATE;
		}
		removeExtraRows(0, from);
	}
	/**
	 * Returns true IFF the matrix contains a row for the given thread.
	 * (meaning the given thread either owns locks or is waiting for locks)
	 */
	boolean contains(Thread t) {
		return lockThreads.contains(t);
	}
	/**
	 * Returns an array of contested locks that are owned by the given thread.
	 * Contested locks are locks that are owned by this thread, but have other
	 * threads waiting for them.
	 */
	ISchedulingRule[] contestedLocksForThread(Thread owner) {
		int threadIndex = indexOf(owner);
		ArrayList ownedLocks = new ArrayList(1);

		for (int j = 0; j < graph[threadIndex].length; j++) {
			if ((graph[threadIndex][j] > NO_STATE) && (locks.get(j) instanceof ILock))
				// && (isWaitedFor((ISchedulingRule)locks.get(j))))
				ownedLocks.add(locks.get(j));
		}
		Assert.isLegal(ownedLocks.size() > 0, "A thread with no contested locks caused deadlock."); //$NON-NLS-1$
		return (ISchedulingRule[]) ownedLocks.toArray(new ISchedulingRule[ownedLocks.size()]);
	}
	/**
	 * Reset the deadlock exists variable.
	 */
	void deadlockSolved() {
		deadlockExists = false;
	}
	/**
	 * Make sure a thread owns, or is waiting for, at most 1 rule.  For debugging
	 * purposes only.
	 */
	public void ensureGraphIntegrity(Thread owner, ISchedulingRule rule) {
		try {
			int threadIndex = lockThreads.indexOf(owner);
			if (threadIndex < 0)
				return;
			for (int j = 0; j < graph[threadIndex].length; j++) {
				if ((graph[threadIndex][j] != NO_STATE) && (!(locks.get(j) instanceof ILock)) && (!rule.isConflicting((ISchedulingRule) locks.get(j)))) {
					graph[threadIndex][j] = NO_STATE;
					removeExtraRows(threadIndex, j);
				}
			}
		} catch (RuntimeException e) {
			if (JobManager.DEBUG_LOCKS) {
				Policy.debug(false, "Error while ensuring graph integrity: "); //$NON-NLS-1$
				e.printStackTrace();
			}
		}
	}
	/**
	 * Returns all the locks owned by the given thread
	 */
	private Object[] getOwnedLocks(Thread current) {
		ArrayList ownedLocks = new ArrayList(1);
		int index = indexOf(current);

		for (int j = 0; j < graph[index].length; j++) {
			if (graph[index][j] > NO_STATE)
				ownedLocks.add(locks.get(j));
		}
		Assert.isLegal(ownedLocks.size() > 0, "A thread with no locks is part of a deadlock."); //$NON-NLS-1$
		return ownedLocks.toArray();
	}
	/**
	 * Returns the thread that owns the given lock.
	 */
	private Thread getThreadOwningLock(ISchedulingRule lock) {
		int index = indexOf(lock);

		for (int i = 0; i < graph.length; i++) {
			if (graph[i][index] > NO_STATE)
				return (Thread) lockThreads.get(i);
		}
		//toDebugString();
		throw new IllegalStateException("Lock " + lock + " is involved in deadlock but is not owned by any thread."); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/**
	 * Returns the true index of the lock in the array. Uses the
	 * ISchedulingRule.isConflicting relationship.
	 */
	private int getTrueLockIndex(ISchedulingRule lock) {
		for (int i = 0; i < locks.size(); i++) {
			ISchedulingRule present = (ISchedulingRule) locks.get(i);
			if (present.isConflicting(lock))
				return i;
		}
		return -1;
	}
	/**
	 * Returns the lock the given thread is waiting for.
	 */
	private Object getWaitingLock(Thread current) {
		int index = indexOf(current);

		for (int j = 0; j < graph[index].length; j++) {
			if (graph[index][j] == WAITING_FOR_LOCK)
				return locks.get(j);
		}
		throw new IllegalStateException("Thread " + current.getName() + " is involved in deadlock but is not waiting for any lock."); //$NON-NLS-1$ //$NON-NLS-2$
	}
	/**
	 * Returns the index of the given lock in the lock array. If the lock is
	 * not present in the array, it is added to the end.
	 */
	private int indexOf(ISchedulingRule lock) {
		int index = getTrueLockIndex(lock);
		if (index < 0) {
			locks.add(lock);
			resize = true;
			index = locks.size() - 1;
		}
		return index;
	}
	/**
	 * Returns the index of the given thread in the thread array. If the thread
	 * is not present in the array, it is added to the end.
	 */
	private int indexOf(Thread owner) {
		int index = lockThreads.indexOf(owner);
		if (index < 0) {
			lockThreads.add(owner);
			resize = true;
			index = lockThreads.size() - 1;
		}
		return index;
	}
	/**
	 * Returns true IFF deadlock exists
	 */
	boolean isDeadlocked() {
		return deadlockExists;
	}
	/**
	 * Returns true if the adjacency matrix is empty.
	 */
	boolean isEmpty() {
		return (locks.size() == 0) && (lockThreads.size() == 0) && (graph.length == 0);
	}
	/**
	 * The given lock was aquired by the given thread.
	 */
	void lockAcquired(Thread owner, ISchedulingRule lock) {
		if (!(lock instanceof ILock))
			ensureGraphIntegrity(owner, lock);
		int lockIndex = indexOf(lock);
		int threadIndex = indexOf(owner);
		if (resize)
			resizeGraph();

		//the rule this thread is acquiring may not be the rule
		//it was waiting for, so replace the entry in the graph
		//also, transfer all entries for any rule that conflicts with this rule
		//to the new rule and erase the columns that correspond to the old rules from the graph
		if (!(lock instanceof ILock)) {
			locks.set(lockIndex, lock);
			transferConflictingRules(lock, lockIndex);
		}
		if (graph[threadIndex][lockIndex] == WAITING_FOR_LOCK)
			graph[threadIndex][lockIndex] = 1;
		else
			graph[threadIndex][lockIndex]++;
	}
	/**
	 * The given lock was released by the given thread. Update the graph.
	 */
	void lockReleased(Thread owner, ISchedulingRule lock) {
		if (!lockThreads.contains(owner)) {
			if (JobManager.DEBUG_LOCKS)
				System.out.println("[lockReleased] Lock " + lock + " was already released by thread " + owner.getName()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (!locks.contains(lock)) {
			if (JobManager.DEBUG_LOCKS)
				System.out.println("[lockReleased] Thread " + owner.getName() + " already released lock " + lock); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		int lockIndex = indexOf(lock);
		int threadIndex = indexOf(owner);

		//if this lock was suspended, set it to NO_STATE
		if (graph[threadIndex][lockIndex] == WAITING_FOR_LOCK) {
			graph[threadIndex][lockIndex] = NO_STATE;
			return;
		}
		if (!JobManager.DEBUG_LOCKS && graph[threadIndex][lockIndex] == NO_STATE)
			return;
		graph[threadIndex][lockIndex]--;
		Assert.isTrue(graph[threadIndex][lockIndex] > -1, "More releases than acquires for thread " + owner.getName() + " and lock " + lock); //$NON-NLS-1$ //$NON-NLS-2$
		if (graph[threadIndex][lockIndex] == NO_STATE)
			removeExtraRows(threadIndex, lockIndex);
	}
	/**
	 * The given scheduling rule is no longer used because the job that invoked
	 * it is done Release this rule regardless of how many times it was
	 * acquired.
	 */
	void lockReleasedCompletely(Thread owner, ISchedulingRule rule) {
		//need to make sure that the given thread was not already removed from the graph
		//and that the given rule (not a rule that conflicts with it) was not removed either
		if (!lockThreads.contains(owner)) {
			if (JobManager.DEBUG_LOCKS)
				System.out.println("[lockReleasedCompletely] Lock " + rule + " was already released by thread " + owner.getName()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (!locks.contains(rule)) {
			if (JobManager.DEBUG_LOCKS)
				System.out.println("[lockReleasedCompletely] Thread " + owner.getName() + " already released lock " + rule); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		int lockIndex = indexOf(rule);
		int threadIndex = indexOf(owner);
		//set this lock to NO_STATE
		graph[threadIndex][lockIndex] = NO_STATE;
		removeExtraRows(threadIndex, lockIndex);
	}
	/**
	 * The given thread could not get the given lock and is waiting for it.
	 * Update the graph.
	 */
	void lockWaitStart(Thread client, ISchedulingRule lock) {
		if (!(lock instanceof ILock))
			ensureGraphIntegrity(client, lock);
		setToWait(client, lock);
		int lockIndex = indexOf(lock);
		int[] temp = new int[lockThreads.size()];
		Arrays.fill(temp, 0);
		checkWaitCycles(temp, lockIndex);
		temp = null;
	}
	/**
	 * The given thread has stopped waiting for the given lock. Update the
	 * graph.
	 */
	void lockWaitStop(Thread owner, ISchedulingRule lock) {
		int lockIndex = getTrueLockIndex(lock);
		int threadIndex = lockThreads.indexOf(owner);
		if (threadIndex < 0) {
			if (JobManager.DEBUG_LOCKS)
				System.out.println("Thread " + owner.getName() + " was already removed."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (lockIndex < 0) {
			if (JobManager.DEBUG_LOCKS)
				System.out.println("Lock " + lock + " was already removed."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (graph[threadIndex][lockIndex] != WAITING_FOR_LOCK)
			Assert.isTrue(false, "Thread " + owner.getName() + " was not waiting for lock " + lock.toString() + " so it could not time out."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		graph[threadIndex][lockIndex] = NO_STATE;
		removeExtraRows(threadIndex, lockIndex);
	}
	private boolean ownsLocks(Thread owner) {
		int index = indexOf(owner);

		for (int j = 0; j < graph[index].length; j++) {
			if (graph[index][j] > NO_STATE) {
				Object lock = locks.get(j);
				if (lock instanceof ILock)
					return true;
			}
		}
		return false;
	}
	/**
	 * Return true IFF this thread owns rule locks (ie. implicit locks which
	 * cannot be suspended)
	 */
	private boolean ownsRuleLocks(Thread owner) {
		int index = indexOf(owner);

		for (int j = 0; j < graph[index].length; j++) {
			if (graph[index][j] > NO_STATE) {
				Object lock = locks.get(j);
				if (!(lock instanceof ILock))
					return true;
			}
		}
		return false;
	}
	/**
	 * The matrix has been simplified. Check if any unnecessary rows or columns
	 * can be removed.
	 */
	private void removeExtraRows(int row, int column) {
		boolean rowEmpty = true;
		boolean colEmpty = true;
		for (int j = 0; j < graph[row].length; j++) {
			if (graph[row][j] != NO_STATE) {
				rowEmpty = false;
				break;
			}
		}
		for (int i = 0; i < graph.length; i++) {
			if (graph[i][column] != NO_STATE) {
				colEmpty = false;
				break;
			}
		}
		if ((!colEmpty) && (!rowEmpty))
			return;

		if (rowEmpty)
			lockThreads.remove(row);
		else
			row = lockThreads.size();
		if (colEmpty)
			locks.remove(column);
		else
			column = locks.size();

		int[][] temp = new int[lockThreads.size()][locks.size()];
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < column; j++) {
				temp[i][j] = graph[i][j];
			}
		}

		for (int i = row; i < temp.length; i++) {
			for (int j = 0; j < temp[i].length; j++) {
				temp[i][j] = graph[i + 1][j];
			}
		}

		for (int i = 0; i < temp.length; i++) {
			for (int j = column; j < temp[i].length; j++) {
				temp[i][j] = graph[i][j + 1];
			}
		}

		for (int i = row; i < temp.length; i++) {
			for (int j = column; j < temp[i].length; j++) {
				temp[i][j] = graph[i + 1][j + 1];
			}
		}
		graph = null;
		graph = temp;
	}
	/**
	 * Adds a 'deadlock detected' message to the log with a stack trace.
	 */
	void reportDeadlock(Thread thread, ISchedulingRule lock, Thread toSuspend) {
		ArrayList deadlockedThreads = new ArrayList(2);
		deadlockedThreads.add(thread);
		Thread owner = getThreadOwningLock(lock);
		while (!deadlockedThreads.contains(owner)) {
			deadlockedThreads.add(owner);
			ISchedulingRule waitingLock = (ISchedulingRule) getWaitingLock(owner);
			owner = getThreadOwningLock(waitingLock);
		}
		String msg = "Deadlock detected. All locks owned by thread " + toSuspend.getName() + " will be suspended."; //$NON-NLS-1$ //$NON-NLS-2$
		MultiStatus main = new MultiStatus(IPlatform.PI_RUNTIME, IPlatform.PLUGIN_ERROR, msg, new IllegalStateException());
		for (int i = 0; i < deadlockedThreads.size(); i++) {
			Thread current = (Thread) deadlockedThreads.get(i);
			Object[] ownedLocks = getOwnedLocks(current);
			Object waitLock = getWaitingLock(current);
			StringBuffer buf = new StringBuffer("Thread "); //$NON-NLS-1$
			buf.append(current.getName());
			buf.append(" has locks: "); //$NON-NLS-1$
			for (int j = 0; j < ownedLocks.length; j++) {
				buf.append(ownedLocks[j]);
				buf.append((j < ownedLocks.length - 1) ? ", " : " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			buf.append("and is waiting for lock "); //$NON-NLS-1$
			buf.append(waitLock);
			Status child = new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, IPlatform.PLUGIN_ERROR, buf.toString(), null);
			main.add(child);
		}
		InternalPlatform.getDefault().log(main);
	}
	/**
	 * The number of threads/locks in the graph has changed. Update the
	 * underlying matrix.
	 */
	private void resizeGraph() {
		int[][] tempGraph = new int[lockThreads.size()][locks.size()];
		for (int i = 0; i < graph.length; i++) {
			for (int j = 0; j < graph[i].length; j++) {
				tempGraph[i][j] = graph[i][j];
			}
		}
		for (int i = 0; i < tempGraph.length; i++) {
			for (int j = (graph.length == 0 ? 0 : graph[0].length); j < tempGraph[i].length; j++) {
				tempGraph[i][j] = NO_STATE;
			}
		}
		for (int i = graph.length; i < tempGraph.length; i++) {
			for (int j = 0; j < tempGraph[i].length; j++) {
				tempGraph[i][j] = NO_STATE;
			}
		}
		graph = null;
		graph = tempGraph;
		resize = false;
	}
	/**
	 * Get the thread whose locks can be suspended. (ie. all locks it owns are
	 * actual locks and not rules) If not found, return the given thread.
	 */
	Thread resolutionCandidate(Thread thread, ISchedulingRule lock) {
		Thread candidate = thread;
		//first look for a candidate that has no scheduling rules
		for (int i = 0; i < lockThreads.size(); i++) {
			if (!ownsRuleLocks(candidate))
				return candidate;
			candidate = blockingThread(candidate);
		}
		//next look for any candidate with a lock
		candidate = thread;
		for (int i = 0; i < lockThreads.size(); i++) {
			if (ownsLocks(candidate))
				return candidate;
			candidate = blockingThread(candidate);
		}
		return thread;
	}
	/**
	 * The given thread is waiting for the given lock. Update the graph.
	 */
	void setToWait(Thread owner, ISchedulingRule lock) {
		int lockIndex = indexOf(lock);
		int threadIndex = indexOf(owner);
		if (resize)
			resizeGraph();

		graph[threadIndex][lockIndex] = WAITING_FOR_LOCK;
	}
	/**
	 * Prints out the current matrix to standard output. Only used for
	 * debugging.
	 */
	public void toDebugString() {
		System.out.println(" :: "); //$NON-NLS-1$
		for (int j = 0; j < locks.size(); j++) {
			System.out.print(" " + locks.get(j) + ','); //$NON-NLS-1$
		}
		System.out.println();
		for (int i = 0; i < graph.length; i++) {
			System.out.print(" " + ((Thread) lockThreads.get(i)).getName() + " : "); //$NON-NLS-1$ //$NON-NLS-2$
			for (int j = 0; j < graph[i].length; j++) {
				System.out.print(" " + graph[i][j] + ','); //$NON-NLS-1$
			}
			System.out.println();
		}
		System.out.println("-------"); //$NON-NLS-1$
	}
	/**
	 * Remove a rule that is conflicting with another rule in the graph
	 * (conflicting rules can only have 1 entry in the graph)
	 */
	private void transferConflictingRules(ISchedulingRule rule, int column) {
		for (int i = 0; i < locks.size(); i++) {
			if ((i != column) && (rule.isConflicting((ISchedulingRule) locks.get(i)))) {
				combineColumns(i, column);
			}
		}
	}
}