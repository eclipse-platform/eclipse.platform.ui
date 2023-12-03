/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bugs 118516, 146397, 249526
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @since 1.0
 * @implNote If methods are added to the interface which this class implements
 *           then implementations of those methods must be added to this class.
 */
public abstract class AbstractObservable extends ChangeManager implements IObservable {
	private boolean disposed = false;

	/**
	 * @param realm the realm to use; not <code>null</code>
	 */
	public AbstractObservable(Realm realm) {
		super(realm);
		ObservableTracker.observableCreated(this);
	}

	@Override
	public synchronized void addChangeListener(IChangeListener listener) {
		addListener(ChangeEvent.TYPE, listener);
	}

	@Override
	public synchronized void removeChangeListener(IChangeListener listener) {
		removeListener(ChangeEvent.TYPE, listener);
	}

	@Override
	public synchronized void addStaleListener(IStaleListener listener) {
		addListener(StaleEvent.TYPE, listener);
	}

	@Override
	public synchronized void removeStaleListener(IStaleListener listener) {
		removeListener(StaleEvent.TYPE, listener);
	}

	/**
	 * @since 1.2
	 */
	@Override
	public synchronized void addDisposeListener(IDisposeListener listener) {
		addListener(DisposeEvent.TYPE, listener);
	}

	/**
	 * @since 1.2
	 */
	@Override
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
	@Override
	public synchronized boolean isDisposed() {
		return disposed;
	}

	@Override
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
