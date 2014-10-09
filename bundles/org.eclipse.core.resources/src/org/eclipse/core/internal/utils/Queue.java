/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.utils;

import java.util.Collections;
import java.util.Iterator;

/**
 * A Queue of objects.
 */
@SuppressWarnings("unchecked")
public class Queue<T> {
	protected Object[] elements;
	protected int head;
	protected int tail;
	protected boolean reuse;

	public Queue() {
		this(20, false);
	}

	/**
	 * The parameter reuse indicates what do you want to happen with
	 * the object reference when you remove it from the queue. If
	 * reuse is false the queue no longer holds a reference to the
	 * object when it is removed. If reuse is true you can use the
	 * method getNextAvailableObject to get an used object, set its
	 * new values and add it again to the queue.
	 */
	public Queue(int size, boolean reuse) {
		elements = new Object[size];
		head = tail = 0;
		this.reuse = reuse;
	}

	public void add(T element) {
		int newTail = increment(tail);
		if (newTail == head) {
			grow();
			newTail = tail + 1;
		}
		elements[tail] = element;
		tail = newTail;
	}

	/**
	 * This method does not affect the queue itself. It is only a
	 * helper to decrement an index in the queue.
	 */
	public int decrement(int index) {
		return (index == 0) ? (elements.length - 1) : index - 1;
	}

	public T elementAt(int index) {
		return (T)elements[index];
	}

	@SuppressWarnings("rawtypes")
	public Iterator<T> iterator() {
		/**/
		if (isEmpty())
			return Collections.EMPTY_LIST.iterator();

		/* if head < tail we can use the same array */
		if (head <= tail)
			return new ArrayIterator(elements, head, tail - 1);

		/* otherwise we need to create a new array */
		Object[] newElements = new Object[size()];
		int end = (elements.length - head);
		System.arraycopy(elements, head, newElements, 0, end);
		System.arraycopy(elements, 0, newElements, end, tail);
		return new ArrayIterator(newElements);
	}

	/**
	 * Returns an object that has been removed from the queue, if any.
	 * The intention is to support reuse of objects that have already
	 * been processed and removed from the queue.  Returns null if there
	 * are no available objects.
	 */
	public T getNextAvailableObject() {
		int index = tail;
		while (index != head) {
			if (elements[index] != null) {
				T result = (T)elements[index];
				elements[index] = null;
				return result;
			}
			index = increment(index);
		}
		return null;
	}

	protected void grow() {
		int newSize = (int) (elements.length * 1.5);
		Object[] newElements = new Object[newSize];
		if (tail >= head)
			System.arraycopy(elements, head, newElements, head, size());
		else {
			int newHead = newSize - (elements.length - head);
			System.arraycopy(elements, 0, newElements, 0, tail + 1);
			System.arraycopy(elements, head, newElements, newHead, (newSize - newHead));
			head = newHead;
		}
		elements = newElements;
	}

	/**
	 * This method does not affect the queue itself. It is only a
	 * helper to increment an index in the queue.
	 */
	public int increment(int index) {
		return (index == (elements.length - 1)) ? 0 : index + 1;
	}

	public int indexOf(T target) {
		if (tail >= head) {
			for (int i = head; i < tail; i++)
				if (target.equals(elements[i]))
					return i;
		} else {
			for (int i = head; i < elements.length; i++)
				if (target.equals(elements[i]))
					return i;
			for (int i = 0; i < tail; i++)
				if (target.equals(elements[i]))
					return i;
		}
		return -1;
	}

	public boolean isEmpty() {
		return tail == head;
	}

	public T peek() {
		return (T)elements[head];
	}

	public T peekTail() {
		return (T)elements[decrement(tail)];
	}

	public T remove() {
		if (isEmpty())
			return null;
		T result = peek();
		if (!reuse)
			elements[head] = null;
		head = increment(head);
		return result;
	}

	public T removeTail() {
		T result = peekTail();
		tail = decrement(tail);
		if (!reuse)
			elements[tail] = null;
		return result;
	}

	public void reset() {
		tail = head = 0;
	}

	public int size() {
		return tail > head ? (tail - head) : ((elements.length - head) + tail);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		int count = 0;
		if (!isEmpty()) {
			Iterator<T> it = iterator();
			//only print a fixed number of elements to prevent debugger from choking
			while (count < 100) {
				sb.append(it.next());
				if (it.hasNext())
					sb.append(',').append(' ');
				else
					break;
			}
		}
		if (count < size())
			sb.append('.').append('.').append('.');
		sb.append(']');
		return sb.toString();
	}
}
