/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.internal.utils.Assert;
 
/**
 * This class is used to maintain a list of listeners.
 * It is a fairly lightweight object, occupying minimal space when
 * no listeners are registered.
 * <p>
 * Note that the <code>add</code> method checks for and eliminates 
 * duplicates based on identity (not equality).  Likewise, the
 * <code>remove</code> method compares based on identity.
 * </p>
 * <p>
 * Use the <code>getListeners</code> method when notifying listeners.
 * Note that no garbage is created if no listeners are registered.
 * The recommended code sequence for notifying all registered listeners
 * of say, <code>FooListener.eventHappened</code>, is:
 * <pre>
 * Object[] listeners = myListenerList.getListeners();
 * for (int i = 0; i < listeners.length; ++i) {
 *    ((FooListener) listeners[i]).eventHappened(event);
 * }
 * </pre>
 * </p>
 */
public class ResourceChangeListenerList {
	/**
	 * The initial capacity of the list. Always >= 1.
	 */
	private int capacity;

	/**
	 * The current number of listeners.
	 * Maintains invariant: 0 <= size <= listeners.length.
	 */
	private int size;

	/**
	 * The list of listeners.  Initially <code>null</code> but initialized
	 * to an array of size capacity the first time a listener is added.
	 * Maintains invariant: listeners != null IFF size != 0
	 */
	private ListenerEntry[] listeners = null;

	private int count1 = 0;
	private int count2 = 0;
	private int count4 = 0;
	private int count8 = 0;
	private int count16 = 0;
	
	/**
	 * The empty array singleton instance, returned by getListeners()
	 * when size == 0.
	 */
	private static final ListenerEntry[] EMPTY_ARRAY = new ListenerEntry[0];

	static class ListenerEntry {
		IResourceChangeListener listener;
		int eventMask;
		ListenerEntry(IResourceChangeListener listener, int eventMask) {
			this.listener = listener;
			this.eventMask = eventMask;
		}
	}
/**
 * Creates a listener list with an initial capacity of 3.
 */
public ResourceChangeListenerList() {
	this(3);
}
/**
 * Creates a listener list with the given initial capacity.
 *
 * @param capacity the number of listeners which this list can initially accept 
 *    without growing its internal representation; must be at least 1
 */
public ResourceChangeListenerList(int capacity) {
	Assert.isTrue(capacity >= 1);
	this.capacity = capacity;
}
/**
 * Adds the given listener to this list. Has no effect if an identical listener
 * is already registered.
 *
 * @param entry the entry
 */
public void add(IResourceChangeListener listener, int mask) {
	Assert.isNotNull(listener);
	if (mask == 0) {
		remove(listener);
		return;
	}
	ResourceChangeListenerList.ListenerEntry entry = new ResourceChangeListenerList.ListenerEntry (listener, mask);
	if (size == 0) {
		listeners = new ListenerEntry[capacity];
	} else {
		// check for duplicates using identity
		for (int i = 0; i < size; ++i) {
			if (listeners[i].listener == listener) {
				removing(listeners[i].eventMask);
				adding(mask);
				listeners[i] = entry;
				return;
			}
		}
		// grow array if necessary
		if (size == listeners.length) {
			System.arraycopy(listeners, 0, listeners = new ListenerEntry[size * 2 + 1], 0, size);
		}
	}
	adding(mask);
	listeners[size++] = entry;
}
private void adding(int mask) {
	if ((mask & 1) != 0)
		count1++;
	if ((mask & 2) != 0)
		count2++;
	if ((mask & 4) != 0)
		count4++;
	if ((mask & 8) != 0)
		count8++;
	if ((mask & 16) != 0)
		count16++;
}
/**
 * Returns an array containing all the registered listeners.
 * The resulting array is unaffected by subsequent adds or removes.
 * If there are no listeners registered, the result is an empty array
 * singleton instance (no garbage is created).
 * Use this method when notifying listeners, so that any modifications
 * to the listener list during the notification will have no effect on the
 * notification itself.
 *
 * @return the list of registered listeners
 */
public ListenerEntry[] getListeners() {
	if (size == 0)
		return EMPTY_ARRAY;
	ListenerEntry[] result = new ListenerEntry[size];
	System.arraycopy(listeners, 0, result, 0, size);
	return result;
}
public boolean hasListenerFor(int event) {
	if (event == 1)
		return count1 > 0;
	if (event == 2)
		return count2 > 0;
	if (event == 4)
		return count4 > 0;
	if (event == 8)
		return count8 > 0;
	if (event == 16)
		return count16 > 0;
	return false;
}
/**
 * Returns whether this listener list is empty.
 *
 * @return <code>true</code> if there are no registered listeners, and
 *   <code>false</code> otherwise
 */
public boolean isEmpty() {
	return size == 0;
}
public void recomputeCounts() {
	count1 = 0;
	count2 = 0;
	count4 = 0;
	count8 = 0;
	count16 = 0;
	for (int i = 0; i < listeners.length; i++)
		adding(listeners[i].eventMask);
}
/**
 * Removes the given listener from this list. Has no effect if an identical
 * listener was not already registered.
 *
 * @param entry the entry to remove
 */
public void remove(IResourceChangeListener listener) {
	Assert.isNotNull(listener);
	for (int i = 0; i < size; ++i) {
		if (listeners[i].listener == listener) {
			removing (listeners[i].eventMask);
			if (size == 1) {
				listeners = null;
				size = 0;
			} else {
				System.arraycopy(listeners, i + 1, listeners, i, --size - i);
				listeners[size] = null;
			}
			return;
		}
	}
}
private void removing(int mask) {
	if ((mask & 1) != 0)
		count1--;
	if ((mask & 2) != 0)
		count2--;
	if ((mask & 4) != 0)
		count4--;
	if ((mask & 8) != 0)
		count8--;
	if ((mask & 16) != 0)
		count16--;
}
/**
 * Returns the number of registered listeners.
 *
 * @return the number of registered listeners
 */
public int size() {
	return size;
}
}
