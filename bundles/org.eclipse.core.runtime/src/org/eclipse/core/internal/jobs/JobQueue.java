/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.jobs;

/**
 * A linked list based priority queue.  
 * Either the elements in the queue must implement Comparable, or a Comparator
 * must be provided.
 */
public class JobQueue {
	/**
	 * The dummy entry sits between the head and the tail of the queue.  
	 * dummy.previous() is the head, and dummy.next() is the tail.
	 */
	private final InternalJob dummy;

	/**
	 * Create a new job queue.
	 */
	public JobQueue() {
		//compareTo on dummy is never called
		dummy = new InternalJob("Queue-Head") {}; //$NON-NLS-1$
		dummy.setNext(dummy);
		dummy.setPrevious(dummy);
	}
	/** 
	 * remove all elements 
	 */
	public void clear() {
		dummy.setNext(dummy);
		dummy.setPrevious(dummy);
	}
	/**
	 * Returns true if the given element is in the queue, and false otherwise. 
	 * 
	 * NOTE: Containment is based on identity, not equality.
	 */
	public boolean contains(Object object) {
		InternalJob entry = dummy.next();
		while (entry != dummy && entry != object)
			entry = entry.next();
		return entry == object;
	}
	/**
	 * Return and remove the element with highest priority, or
	 * null if empty.
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
		InternalJob tail = dummy.next();
		//overtake lower priority jobs that are not conflicting
		while (tail != dummy && tail.compareTo(newEntry) < 0 && !newEntry.isConflicting(tail))
			tail = tail.next();
		//new entry is smaller than tail
		newEntry.setNext(tail);
		newEntry.setPrevious(tail.previous());
		tail.previous().setNext(newEntry);
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
	 * The given object has changed priority.  Reshuffle the heap until it is valid.
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