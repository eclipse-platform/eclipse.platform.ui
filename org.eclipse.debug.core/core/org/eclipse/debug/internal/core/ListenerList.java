package org.eclipse.debug.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Local version of org.eclipse.jface.util.ListenerList
 */
public class ListenerList {
	/**
	 * The initial capacity of the list. Always >= 1.
	 */
	private int fCapacity;

	/**
	 * The current number of listeners.
	 * Maintains invariant: 0 <= fSize <= listeners.length.
	 */
	private int fSize;

	/**
	 * The list of listeners.  Initially <code>null</code> but initialized
	 * to an array of size capacity the first time a listener is added.
	 * Maintains invariant: listeners != null IFF fSize != 0
	 */
	private Object[] fListeners= null;

	/**
	 * The empty array singleton instance, returned by getListeners()
	 * when size == 0.
	 */
	private static final Object[] EmptyArray= new Object[0];
	/**
	 * Creates a listener list with an initial capacity of 3.
	 */
	public ListenerList() {
		this(3);
	}

	/**
	 * Creates a listener list with the given initial capacity.
	 *
	 * @param capacity the number of listeners which this list can initially accept 
	 *    without growing its internal representation; must be at least 1
	 */
	public ListenerList(int capacity) {
		if (capacity < 1) {
			throw new IllegalArgumentException();
		}
		fCapacity= capacity;
	}

	/**
	 * Adds a listener to the list.
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener a listener
	 */
	public synchronized void add(Object listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}
		if (fSize == 0) {
			fListeners= new Object[fCapacity];
		} else {
			// check for duplicates using identity
			for (int i= 0; i < fSize; ++i) {
				if (fListeners[i] == listener) {
					return;
				}
			}
			// grow array if necessary
			if (fSize == fListeners.length) {
				System.arraycopy(fListeners, 0, fListeners= new Object[fSize * 2 + 1], 0, fSize);
			}
		}
		fListeners[fSize++]= listener;
	}

	/**
	 * Returns an array containing all the registered listeners.
	 * The resulting array is unaffected by subsequent adds or removes.
	 * If there are no listeners registered, the result is an empty array
	 * singleton instance (no garbage is created).
	 * Use this method when notifying listeners, so that any modifications
	 * to the listener list during the notification will have no effect on the
	 * notification itself.
	 */
	public synchronized Object[] getListeners() {
		if (fSize == 0)
			return EmptyArray;
		Object[] result= new Object[fSize];
		System.arraycopy(fListeners, 0, result, 0, fSize);
		return result;
	}

	/**
	 * Returns <code>true</code> if there are no registered listeners,
	 * <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return fSize == 0;
	}

	/**
	 * Removes a listener from the list.
	 * Has no effect if an identical listener was not already registered.
	 *
	 * @param listener a listener
	 */
	public synchronized void remove(Object listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}

		for (int i= 0; i < fSize; ++i) {
			if (fListeners[i] == listener) {
				if (fSize == 1) {
					fListeners= null;
					fSize= 0;
				} else {
					System.arraycopy(fListeners, i + 1, fListeners, i, --fSize - i);
					fListeners[fSize]= null;
				}
				return;
			}
		}
	}

	/**
	 * Returns the number of registered listeners
	 *
	 * @return the number of registered listeners
	 */
	public int size() {
		return fSize;
	}
}

