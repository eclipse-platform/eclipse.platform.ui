/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 *******************************************************************************/
package org.eclipse.jface.databinding.conformance.util;

import java.util.List;

import org.eclipse.core.databinding.observable.IObservablesListener;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;

/**
 * Listener for tracking the firing of ListChangeEvents.
 */
public class ListChangeEventTracker<E> implements IListChangeListener<E> {
	public int count;

	public ListChangeEvent<? extends E> event;

	/**
	 * Queue that the listener will add itself too when it is notified of an
	 * event. Used to determine order of notifications of listeners.
	 */
	public final List<IObservablesListener> listenerQueue;

	public ListChangeEventTracker() {
		this(null);
	}

	public ListChangeEventTracker(List<IObservablesListener> listenerQueue) {
		this.listenerQueue = listenerQueue;
	}

	@Override
	public void handleListChange(ListChangeEvent<? extends E> event) {
		count++;
		this.event = event;
		if (listenerQueue != null) {
			listenerQueue.add(this);
		}
	}

	/**
	 * Convenience method to register a new listener.
	 *
	 * @param observable
	 * @return tracker
	 */
	public static <E> ListChangeEventTracker<E> observe(IObservableList<E> observable) {
		ListChangeEventTracker<E> tracker = new ListChangeEventTracker<>();
		observable.addListChangeListener(tracker);
		return tracker;
	}
}
