/*******************************************************************************
 * Copyright (c) 2007 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 208332)
 *     Brad Reynolds - initial API and implementation
 *         (through UnmodifiableObservableList.java)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.set.*;

/**
 * ObservableList implementation that prevents modification by consumers. Events
 * in the originating wrapped list are propagated and thrown from this instance
 * when appropriate. All mutators throw an UnsupportedOperationException.
 * 
 * @since 1.1
 */
public class UnmodifiableObservableSet extends ObservableSet {
	private ISetChangeListener setChangeListener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			fireSetChange(event.diff);
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		public void handleStale(StaleEvent event) {
			fireStale();
		}
	};

	private IObservableSet wrappedSet;

	/**
	 * @param wrappedSet
	 */
	public UnmodifiableObservableSet(IObservableSet wrappedSet) {
		super(wrappedSet.getRealm(), wrappedSet, wrappedSet.getElementType());

		this.wrappedSet = wrappedSet;

		wrappedSet.addSetChangeListener(setChangeListener);
		wrappedSet.addStaleListener(staleListener);
	}

	/**
	 * Because this instance is immutable staleness cannot be changed.
	 */
	public void setStale(boolean stale) {
		throw new UnsupportedOperationException();
	}

	public boolean isStale() {
		getterCalled();
		return wrappedSet == null ? false : wrappedSet.isStale();
	}

	public synchronized void dispose() {
		if (wrappedSet != null) {
			wrappedSet.removeSetChangeListener(setChangeListener);
			wrappedSet.removeStaleListener(staleListener);
			wrappedSet = null;
		}
		setChangeListener = null;
		staleListener = null;
		super.dispose();
	}
}
