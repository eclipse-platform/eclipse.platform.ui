/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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

import org.eclipse.core.runtime.ListenerList;


/**
 * Abstract base class for all IConcurrentModel implementations. Clients should
 * subclass this class instead of implementing IConcurrentModel directly.
 *
 * @since 3.1
 */
public abstract class AbstractConcurrentModel implements
		IConcurrentModel {

	private final ListenerList<IConcurrentModelListener> listeners = new ListenerList<>();

	@Override
	public void addListener(IConcurrentModelListener listener) {
		listeners.add(listener);
	}

	/**
	 * Fires an add notification to all listeners
	 *
	 * @param added objects added to the set
	 */
	protected final void fireAdd(Object[] added) {
		for (IConcurrentModelListener next : listeners) {
			next.add(added);
		}
	}

	/**
	 * Fires a remove notification to all listeners
	 *
	 * @param removed objects removed from the set
	 */
	protected final void fireRemove(Object[] removed) {
		for (IConcurrentModelListener next : listeners) {
			next.remove(removed);
		}
	}

	/**
	 * Fires an update notification to all listeners
	 *
	 * @param updated objects that have changed
	 */
	protected final void fireUpdate(Object[] updated) {
		for (IConcurrentModelListener next : listeners) {
			next.update(updated);
		}
	}

	/**
	 * Returns the array of listeners for this model
	 *
	 * @return the array of listeners for this model
	 */
	protected final IConcurrentModelListener[] getListeners() {
		Object[] l = listeners.getListeners();
		IConcurrentModelListener[] result = new IConcurrentModelListener[l.length];

		for (int i = 0; i < l.length; i++) {
			result[i] = (IConcurrentModelListener)l[i];
		}

		return result;
	}

	@Override
	public void removeListener(IConcurrentModelListener listener) {
		listeners.remove(listener);
	}
}
