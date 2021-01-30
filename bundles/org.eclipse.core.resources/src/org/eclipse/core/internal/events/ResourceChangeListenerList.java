/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.core.internal.events;

import java.util.Arrays;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.Assert;

/**
 * This class is used to maintain a list of listeners. It is a fairly lightweight object,
 * occupying minimal space when no listeners are registered.
 * <p>
 * Note that the <code>add</code> method checks for and eliminates
 * duplicates based on identity (not equality).  Likewise, the
 * <code>remove</code> method compares based on identity.
 * </p>
 * <p>
 * This implementation is thread safe.  The listener list is copied every time
 * it is modified, so readers do not need to copy or synchronize. This optimizes
 * for frequent reads and infrequent writes, and assumes that readers can
 * be trusted not to modify the returned array.
 */
public class ResourceChangeListenerList {

	static class ListenerEntry {
		int eventMask;
		IResourceChangeListener listener;

		ListenerEntry(IResourceChangeListener listener, int eventMask) {
			this.listener = listener;
			this.eventMask = eventMask;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Listener [eventMask="); //$NON-NLS-1$
			sb.append(eventMask);
			sb.append(", "); //$NON-NLS-1$
			sb.append(listener);
			sb.append("]"); //$NON-NLS-1$
			return sb.toString();
		}
	}

	/**
	 * The empty array singleton instance.
	 */
	private static final ListenerEntry[] EMPTY_ARRAY = new ListenerEntry[0];

	private int count1 = 0;
	private int count2 = 0;
	private int count4 = 0;
	private int count8 = 0;
	private int count16 = 0;
	private int count32 = 0;

	/**
	 * The list of listeners.  Maintains invariant: listeners != null.
	 */
	private volatile ListenerEntry[] listeners = EMPTY_ARRAY;

	/**
	 * Adds the given listener to this list. Has no effect if an identical listener
	 * is already registered.
	 *
	 * @param listener the listener
	 * @param mask event types
	 */
	public synchronized void add(IResourceChangeListener listener, int mask) {
		Assert.isNotNull(listener);
		if (mask == 0) {
			remove(listener);
			return;
		}
		ResourceChangeListenerList.ListenerEntry entry = new ResourceChangeListenerList.ListenerEntry(listener, mask);
		final int oldSize = listeners.length;
		// check for duplicates using identity
		for (int i = 0; i < oldSize; ++i) {
			if (listeners[i].listener == listener) {
				removing(listeners[i].eventMask);
				adding(mask);
				listeners[i] = entry;
				return;
			}
		}
		adding(mask);
		// Thread safety: copy on write to protect concurrent readers.
		ListenerEntry[] newListeners = new ListenerEntry[oldSize + 1];
		System.arraycopy(listeners, 0, newListeners, 0, oldSize);
		newListeners[oldSize] = entry;
		//atomic assignment
		this.listeners = newListeners;
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
		if ((mask & 32) != 0)
			count32++;
	}

	/**
	 * Returns an array containing all the registered listeners.
	 * The resulting array is unaffected by subsequent adds or removes.
	 * If there are no listeners registered, the result is an empty array
	 * singleton instance (no garbage is created).
	 * Use this method when notifying listeners, so that any modifications
	 * to the listener list during the notification will have no effect on the
	 * notification itself.
	 * <p>
	 * Note: Clients must not modify the returned list
	 * @return the list of registered listeners that must not be modified
	 */
	public ListenerEntry[] getListeners() {
		return listeners;
	}

	public boolean hasListenerFor(int event) {
		switch (event) {
		case 1:
			return count1 > 0;
		case 2:
			return count2 > 0;
		case 4:
			return count4 > 0;
		case 8:
			return count8 > 0;
		case 16:
			return count16 > 0;
		case 32:
			return count32 > 0;
		default:
			return false;
		}
	}

	/**
	 * Removes the given listener from this list. Has no effect if an identical
	 * listener was not already registered.
	 *
	 * @param listener the listener to remove
	 */
	public synchronized void remove(IResourceChangeListener listener) {
		Assert.isNotNull(listener);
		final int oldSize = listeners.length;
		for (int i = 0; i < oldSize; ++i) {
			if (listeners[i].listener == listener) {
				removing(listeners[i].eventMask);
				if (oldSize == 1) {
					listeners = EMPTY_ARRAY;
				} else {
					// Thread safety: create new array to avoid affecting concurrent readers
					ListenerEntry[] newListeners = new ListenerEntry[oldSize - 1];
					System.arraycopy(listeners, 0, newListeners, 0, i);
					System.arraycopy(listeners, i + 1, newListeners, i, oldSize - i - 1);
					//atomic assignment to field
					this.listeners = newListeners;
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
		if ((mask & 32) != 0)
			count32--;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourceChangeListenerList ["); //$NON-NLS-1$
		if (listeners != null) {
			builder.append("listeners="); //$NON-NLS-1$
			builder.append(Arrays.toString(listeners));
		}
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
}
