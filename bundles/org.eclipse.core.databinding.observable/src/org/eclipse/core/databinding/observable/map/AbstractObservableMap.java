/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bugs 118516, 146397, 226289, 246103, 249526, 264307,
 *                    349038
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.AbstractMap;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.ChangeSupport;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 *
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 *
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 * @since 1.0
 */
public abstract class AbstractObservableMap<K, V> extends AbstractMap<K, V>
		implements IObservableMap<K, V> {

	private final class PrivateChangeSupport extends ChangeSupport {
		private PrivateChangeSupport(Realm realm) {
			super(realm);
		}

		@Override
		protected void firstListenerAdded() {
			AbstractObservableMap.this.firstListenerAdded();
		}

		@Override
		protected void lastListenerRemoved() {
			AbstractObservableMap.this.lastListenerRemoved();
		}

		@Override
		protected boolean hasListeners() {
			return super.hasListeners();
		}
	}

	private final Realm realm;
	private PrivateChangeSupport changeSupport;
	private volatile boolean disposed = false;

	private boolean stale;

	/**
	 */
	public AbstractObservableMap() {
		this(Realm.getDefault());
	}

	/**
	 *
	 */
	protected void lastListenerRemoved() {
	}

	/**
	 *
	 */
	protected void firstListenerAdded() {
	}

	/**
	 * @param realm
	 */
	public AbstractObservableMap(Realm realm) {
		Assert.isNotNull(realm, "Realm cannot be null"); //$NON-NLS-1$
		ObservableTracker.observableCreated(this);
		this.realm = realm;
		changeSupport = new PrivateChangeSupport(realm);
	}

	@Override
	public synchronized void addMapChangeListener(IMapChangeListener<? super K, ? super V> listener) {
		if (!disposed) {
			changeSupport.addListener(MapChangeEvent.TYPE, listener);
		}
	}

	@Override
	public synchronized void removeMapChangeListener(IMapChangeListener<? super K, ? super V> listener) {
		if (!disposed) {
			changeSupport.removeListener(MapChangeEvent.TYPE, listener);
		}
	}

	@Override
	public synchronized void addChangeListener(IChangeListener listener) {
		if (!disposed) {
			changeSupport.addChangeListener(listener);
		}
	}

	@Override
	public synchronized void addStaleListener(IStaleListener listener) {
		if (!disposed) {
			changeSupport.addStaleListener(listener);
		}
	}

	/**
	 * @return whether the observable map has listeners registered
	 * @since 1.2
	 */
	protected synchronized boolean hasListeners() {
		return !disposed && changeSupport.hasListeners();
	}

	/**
	 * @since 1.2
	 */
	@Override
	public synchronized void addDisposeListener(IDisposeListener listener) {
		if (!disposed) {
			changeSupport.addDisposeListener(listener);
		}
	}

	/**
	 * @since 1.2
	 */
	@Override
	public synchronized void removeDisposeListener(IDisposeListener listener) {
		if (!disposed) {
			changeSupport.removeDisposeListener(listener);
		}
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
			changeSupport.fireEvent(new DisposeEvent(this));
			changeSupport.dispose();
			changeSupport = null;
		}
	}

	@Override
	public Realm getRealm() {
		return realm;
	}

	@Override
	public boolean isStale() {
		checkRealm();
		return stale;
	}

	/**
	 * @since 1.2
	 */
	@Override
	public Object getKeyType() {
		return null;
	}

	/**
	 * @since 1.2
	 */
	@Override
	public Object getValueType() {
		return null;
	}

	@Override
	public synchronized void removeChangeListener(IChangeListener listener) {
		if (!disposed) {
			changeSupport.removeChangeListener(listener);
		}
	}

	@Override
	public synchronized void removeStaleListener(IStaleListener listener) {
		if (!disposed) {
			changeSupport.removeStaleListener(listener);
		}
	}

	/**
	 * Sets the stale state. Must be invoked from the current realm.
	 *
	 * @param stale
	 */
	public void setStale(boolean stale) {
		checkRealm();
		this.stale = stale;
		if (stale) {
			fireStale();
		}
	}

	/**
	 * Fires stale events. Must be invoked from current realm.
	 */
	protected void fireStale() {
		checkRealm();
		changeSupport.fireEvent(new StaleEvent(this));
	}

	/**
	 * Fires change events. Must be invoked from current realm.
	 */
	protected void fireChange() {
		checkRealm();
		changeSupport.fireEvent(new ChangeEvent(this));
	}

	/**
	 * Fires map change events. Must be invoked from current realm.
	 *
	 * @param diff
	 */
	protected void fireMapChange(MapDiff<K, V> diff) {
		checkRealm();
		fireChange();
		changeSupport.fireEvent(new MapChangeEvent<>(this, diff));
	}

	/**
	 * Asserts that the realm is the current realm.
	 *
	 * @see Realm#isCurrent()
	 * @throws AssertionFailedException
	 *             if the realm is not the current realm
	 */
	protected void checkRealm() {
		Assert.isTrue(getRealm().isCurrent(),
				"This operation must be run within the observable's realm"); //$NON-NLS-1$
	}
}
