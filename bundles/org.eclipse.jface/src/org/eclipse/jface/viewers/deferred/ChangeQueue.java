/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import java.util.LinkedList;

/**
 * Holds a queue of additions, removals, updates, and SET calls for a
 * BackgroundContentProvider
 */
final class ChangeQueue {
	/**
	 * Represents the addition of an item
	 * @since 3.1
	 */
	public static final int ADD = 0;
	/**
	 * Represents the removal of an item
	 * @since 3.1
	 */
	public static final int REMOVE = 1;
	/**
	 * Represents a reset of all the items
	 * @since 3.1
	 */
	public static final int SET = 2;
	/**
	 * Represents an update of an item
	 * @since 3.1
	 */
	public static final int UPDATE = 3;

	/**
	 *
	 * @since 3.1
	 */
	public static final class Change {
		private int type;
		private Object[] elements;

		/**
		 * Create a change of the specified type that affects the given elements.
		 *
		 * @param type one of <code>ADD</code>, <code>REMOVE</code>, <code>SET</code>, or <code>UPDATE</code>.
		 * @param elements the elements affected by the change.
		 *
		 * @since 3.1
		 */
		public Change(int type, Object[] elements) {
			this.type = type;
			this.elements = elements;
		}

		/**
		 * Get the type of change.
		 * @return one of <code>ADD</code>, <code>REMOVE</code>, <code>SET</code>, or <code>UPDATE</code>.
		 *
		 * @since 3.1
		 */
		public int getType() {
			return type;
		}

		/**
		 * Return the elements associated with the change.
		 * @return the elements affected by the change.
		 *
		 * @since 3.1
		 */
		public Object[] getElements() {
			return elements;
		}
	}

	private LinkedList<Change> queue = new LinkedList<>();

	/**
	 * Create a change of the given type and elements and enqueue it.
	 *
	 * @param type the type of change to be created
	 * @param elements the elements affected by the change
	 */
	public synchronized void enqueue(int type, Object[] elements) {
		enqueue(new Change(type, elements));
	}

	/**
	 * Add the specified change to the queue
	 * @param toQueue the change to be added
	 */
	public synchronized void enqueue(Change toQueue) {
		// A SET event makes all previous adds, removes, and sets redundant... so remove
		// them from the queue
		if (toQueue.type == SET) {
			LinkedList<Change> newQueue = new LinkedList<>();
			for (Change next : queue) {
				if (next.getType() == ADD || next.getType() == REMOVE || next.getType() == SET) {
					continue;
				}

				newQueue.add(next);
			}
			queue = newQueue;
		}

		queue.add(toQueue);
	}

	/**
	 * Remove the first change from the queue.
	 * @return the first change
	 */
	public synchronized Change dequeue() {
		Change result = queue.removeFirst();
		return result;
	}

	/**
	 * Return whether the queue is empty
	 * @return <code>true</code> if empty, <code>false</code> otherwise
	 */
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}
}
