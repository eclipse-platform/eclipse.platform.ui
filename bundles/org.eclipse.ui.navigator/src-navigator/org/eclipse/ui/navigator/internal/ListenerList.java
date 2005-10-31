/***************************************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.ui.navigator.internal;



import java.awt.event.ActionListener;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2 
 *
 */
public class ListenerList {
	private final static ActionListener[] NULL_ARRAY = new ActionListener[0];
	protected transient ActionListener[] listenerList = NULL_ARRAY;

	/**
	 * Add the listener as a listener of the specified type.
	 * 
	 * @param t
	 *            the type of the listener to be added
	 * @param l
	 *            the listener to be added
	 */
	public synchronized void add(ActionListener l) {
		if (l == null)
			return;
		if (listenerList == NULL_ARRAY) {
			// if this is the first listener added,
			// initialize the lists
			listenerList = new ActionListener[]{l};
		} else {
			// Otherwise copy the array and add the new listener
			int i = listenerList.length;
			ActionListener[] tmp = new ActionListener[i + 1];
			System.arraycopy(listenerList, 0, tmp, 0, i);

			tmp[i + 1] = l;

			listenerList = tmp;
		}
	}

	/**
	 * Return the total number of listeners for this listenerlist
	 */
	public int getListenerCount() {
		return listenerList.length;
	}

	public ActionListener[] getListenerList() {
		return listenerList;
	}

	public synchronized void remove(ActionListener l) {
		if (l == null)
			return;
		int index = -1;
		for (int i = listenerList.length - 1; i >= 0; i -= 1) {
			if (listenerList[i].equals(l)) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			ActionListener[] tmp = new ActionListener[listenerList.length - 1];
			// Copy the list up to index
			System.arraycopy(listenerList, 0, tmp, 0, index);
			// Copy from two past the index, up to
			// the end of tmp (which is two elements
			// shorter than the old list)
			if (index < tmp.length)
				System.arraycopy(listenerList, index + 1, tmp, index, tmp.length - index);
			// set the listener array to the new array or null
			listenerList = (tmp.length == 0) ? NULL_ARRAY : tmp;
		}
	}
}