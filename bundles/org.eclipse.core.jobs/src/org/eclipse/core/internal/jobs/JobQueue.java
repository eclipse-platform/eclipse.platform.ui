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
package org.eclipse.core.internal.jobs;

import org.eclipse.core.runtime.*;

/**
 * A linked list based priority queue. Either the elements in the queue must
 * implement Comparable, or a Comparator must be provided.
 */
public class JobQueue {
	/**
	 * The dummy entry sits between the head and the tail of the queue.
	 * dummy.previous() is the head, and dummy.next() is the tail.
	 */
	private final InternalJob dummy;

	/**
	 * If true, conflicting jobs will be allowed to overtake others in the
	 * queue that have lower priority. If false, higher priority jumps can only
	 * move up the queue by overtaking jobs that they don't conflict with.
	 */
	private boolean allowConflictOvertaking;

	/**
	 * Create a new job queue. 
	 */
	public JobQueue(boolean allowConflictOvertaking) {
		//compareTo on dummy is never called
		dummy = new InternalJob("Queue-Head") {//$NON-NLS-1$
			public IStatus run(IProgressMonitor m) {
				return Status.OK_STATUS;
			}
		};
		dummy.setNext(dummy);
		dummy.setPrevious(dummy);
		this.allowConflictOvertaking = allowConflictOvertaking;
	}

	/** 
	 * remove all elements 
	 */
	public void clear() {
		dummy.setNext(dummy);
		dummy.setPrevious(dummy);
	}

	/**
	 * Return and remove the element with highest priority, or null if empty. 
	 */
	public InternalJob dequeue() {
		InternalJob toRemove = dummy.previous();
		if (toRemove == dummy)
			return null;
		return toRemove.remove();
	}

	/**
	 * Adds an item to the queue 
	 */
	public void enqueue(InternalJob newEntry) {
		//assert new entry is does not already belong to some other data structure
		Assert.isTrue(newEntry.next() == null);
		Assert.isTrue(newEntry.previous() == null);
		InternalJob tail = dummy.next();
		//overtake lower priority jobs. Only overtake conflicting jobs if allowed to
		while (tail != dummy && tail.compareTo(newEntry) < 0 && (allowConflictOvertaking || !newEntry.isConflicting(tail)))
			tail = tail.next();
		//new entry is smaller than tail
		final InternalJob tailPrevious = tail.previous();
		newEntry.setNext(tail);
		newEntry.setPrevious(tailPrevious);
		tailPrevious.setNext(newEntry);
		tail.setPrevious(newEntry);
	}

	/**
	 * Removes the given element from the queue. 
	 */
	public void remove(InternalJob toRemove) {
		toRemove.remove();
		//previous of toRemove might now bubble up
	}

	/**
	 * The given object has changed priority. Reshuffle the heap until it is
	 * valid.
	 */
	public void resort(InternalJob entry) {
		remove(entry);
		enqueue(entry);
	}

	/**
	 * Returns true if the queue is empty, and false otherwise. 
	 */
	public boolean isEmpty() {
		return dummy.next() == dummy;
	}

	/** 
	 * Return greatest element without removing it, or null if empty 
	 */
	public InternalJob peek() {
		return dummy.previous() == dummy ? null : dummy.previous();
	}
}
