/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers.deferred;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Holds a queue of additions, removals, updates, and SET calls for a
 * BackgroundContentProvider
 */
final class ChangeQueue {
	public static final int ADD = 0;
	public static final int REMOVE = 1;
	public static final int SET = 2;
	public static final int UPDATE = 3;
	
	public static final class Change {
		private int type;
		private Object[] elements;
		
		public Change(int type, Object[] elements) {
			this.type = type;
			this.elements = elements;
		}
		
		public int getType() {
			return type;
		}
		
		public Object[] getElements() {
			return elements;
		}
	}
	
	private LinkedList queue = new LinkedList();
	private int workload = 0;
	
	public synchronized void enqueue(int type, Object[] elements) {
		enqueue(new Change(type, elements));
	}
	
	public synchronized void enqueue(Change toQueue) {
		// A SET event makes all previous adds, removes, and sets redundant... so remove
		// them from the queue
		if (toQueue.type == SET) {
			workload = 0;
			LinkedList newQueue = new LinkedList();
			for (Iterator iter = queue.iterator(); iter.hasNext();) {
				Change next = (Change) iter.next();
				
				if (next.getType() == ADD || next.getType() == REMOVE || next.getType() == SET) {
					continue;
				}
				
				newQueue.add(next);
				workload += next.elements.length;
			}
			queue = newQueue;
		}
		
		queue.add(toQueue);
		workload += toQueue.elements.length;
	}
	
	public synchronized Change dequeue() {
		Change result = (Change)queue.removeFirst();
		
		workload -= result.elements.length;
		return result;
	}
	
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}
}
