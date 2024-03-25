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
 *     Matthew Hall - bug 146397
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

/**
 * @since 1.0
 */
public abstract class ChangeSupport extends ChangeManager {

	/**
	 * @param realm the realm
	 */
	public ChangeSupport(Realm realm) {
		super(realm);
	}

	@Override
	public void addListener(Object listenerType,
			IObservablesListener listener) {
		super.addListener(listenerType, listener);
	}

	@Override
	public void removeListener(Object listenerType,
			IObservablesListener listener) {
		super.removeListener(listenerType, listener);
	}

	@Override
	public void fireEvent(ObservableEvent event) {
		super.fireEvent(event);
	}

	@Override
	protected abstract void firstListenerAdded();

	@Override
	protected abstract void lastListenerRemoved();

	/**
	 * @param listener the listener to add; not <code>null</code>
	 */
	public void addChangeListener(IChangeListener listener) {
		addListener(ChangeEvent.TYPE, listener);
	}

	/**
	 * @param listener the listener to remove; not <code>null</code>
	 */
	public void removeChangeListener(IChangeListener listener) {
		removeListener(ChangeEvent.TYPE, listener);
	}

	/**
	 * @param listener the listener to add; not <code>null</code>
	 */
	public void addStaleListener(IStaleListener listener) {
		addListener(StaleEvent.TYPE, listener);
	}

	/**
	 * @param listener the listener to remove; not <code>null</code>
	 */
	public void removeStaleListener(IStaleListener listener) {
		removeListener(StaleEvent.TYPE, listener);
	}

	/**
	 * @param listener the listener to add; not <code>null</code>
	 * @since 1.2
	 */
	public void addDisposeListener(IDisposeListener listener) {
		addListener(DisposeEvent.TYPE, listener);
	}

	/**
	 * @param listener the listener to remove; not <code>null</code>
	 * @since 1.2
	 */
	public void removeDisposeListener(IDisposeListener listener) {
		removeListener(DisposeEvent.TYPE, listener);
	}
}
