/*******************************************************************************
 * Copyright (c) 2006-2008 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 208332
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ObservableList;

/**
 * ObservableList implementation that prevents modification by consumers. Events
 * in the originating wrapped list are propagated and thrown from this instance
 * when appropriate.  All mutators throw an UnsupportedOperationException.
 * 
 * @since 1.0
 */
/*
 * Implementation makes the assumption that the superclass (ObservableList) is
 * unmodifiable and that all modify methods throw an
 * UnsupportedOperationException.
 */
public class UnmodifiableObservableList extends ObservableList {
	/**
	 * List that is being made unmodifiable.
	 */
	private IObservableList wrappedList;

	private IListChangeListener listChangeListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			// Fires a Change and then ListChange event.
			fireListChange(event.diff);
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		public void handleStale(StaleEvent event) {
			fireStale();
		}
	};

	/**
	 * @param wrappedList
	 */
	public UnmodifiableObservableList(IObservableList wrappedList) {
		super(wrappedList.getRealm(), wrappedList, wrappedList.getElementType());
		this.wrappedList = wrappedList;

		wrappedList.addListChangeListener(listChangeListener);

		wrappedList.addStaleListener(staleListener);
	}

	/**
	 * Because this instance is immutable staleness cannot be changed.
	 * 
	 * @throws UnsupportedOperationException
	 *             because this instance is unmodifiable.
	 */
	public void setStale(boolean stale) {
		throw new UnsupportedOperationException();
	}

	public boolean isStale() {
		getterCalled();
		return wrappedList == null ? false : wrappedList.isStale();
	}

	public synchronized void dispose() {
		if (wrappedList != null) {
			wrappedList.removeListChangeListener(listChangeListener);
			wrappedList.removeStaleListener(staleListener);
			wrappedList = null;
		}
		listChangeListener = null;
		staleListener = null;
		super.dispose();
	}
}
