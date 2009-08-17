/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bugs 118516, 146397, 249526
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @since 1.0
 */
public abstract class AbstractObservable extends ChangeManager implements IObservable {
	private boolean disposed = false;

	/**
	 * @param realm
	 */
	public AbstractObservable(Realm realm) {
		super(realm);
		ObservableTracker.observableCreated(this);
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		addListener(ChangeEvent.TYPE, listener);
	}

	public synchronized void removeChangeListener(IChangeListener listener) {
		removeListener(ChangeEvent.TYPE, listener);
	}

	public synchronized void addStaleListener(IStaleListener listener) {
		addListener(StaleEvent.TYPE, listener);
	}

	public synchronized void removeStaleListener(IStaleListener listener) {
		removeListener(StaleEvent.TYPE, listener);
	}

	/**
	 * @since 1.2
	 */
	public synchronized void addDisposeListener(IDisposeListener listener) {
		addListener(DisposeEvent.TYPE, listener);
	}

	/**
	 * @since 1.2
	 */
	public synchronized void removeDisposeListener(IDisposeListener listener) {
		removeListener(DisposeEvent.TYPE, listener);
	}

	protected void fireChange() {
		checkRealm();
		fireEvent(new ChangeEvent(this));
	}

	protected void fireStale() {
		checkRealm();
		fireEvent(new StaleEvent(this));
	}

	/**
	 * @since 1.2
	 */
	public synchronized boolean isDisposed() {
		return disposed;
	}

	/**
	 * 
	 */
	public synchronized void dispose() {
		if (!disposed) {
			disposed = true;
			fireEvent(new DisposeEvent(this));
			super.dispose();
		}
	}

	/**
	 * Asserts that the realm is the current realm.
	 * 
	 * @see Realm#isCurrent()
	 * @throws AssertionFailedException if the realm is not the current realm
	 */
	protected void checkRealm() {
		Assert.isTrue(getRealm().isCurrent(),
				"This operation must be run within the observable's realm"); //$NON-NLS-1$
	}
}
