/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.databinding;

/**
 * Created to avoid a dependency on java.util.LinkedList, see bug 205224.
 * 
 * @since 1.1
 * 
 */
public class Queue {

	static class Entry {
		Object object;

		Entry(Object o) {
			this.object = o;
		}

		Entry next;
	}

	Entry first;
	Entry last;

	/**
	 * Adds the given object to the end of the queue.
	 * 
	 * @param o
	 */
	public void enqueue(Object o) {
		Entry oldLast = last;
		last = new Entry(o);
		if (oldLast != null) {
			oldLast.next = last;
		} else {
			first = last;
		}
	}

	/**
	 * Returns the first object in the queue. The queue must not be empty.
	 * 
	 * @return the first object
	 */
	public Object dequeue() {
		Entry oldFirst = first;
		if (oldFirst == null) {
			throw new IllegalStateException();
		}
		first = oldFirst.next;
		if (first == null) {
			last = null;
		}
		oldFirst.next = null;
		return oldFirst.object;
	}

	/**
	 * Returns <code>true</code> if the list is empty.
	 * 
	 * @return <code>true</code> if the list is empty
	 */
	public boolean isEmpty() {
		return first == null;
	}
}