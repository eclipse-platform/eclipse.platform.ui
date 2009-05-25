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
 *     Matthew Hall - bugs 208332, 263691
 *******************************************************************************/

package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;

/**
 * 
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * @since 1.0
 * 
 */
abstract public class AbstractObservableValue extends AbstractObservable implements IObservableValue {
	/**
	 * Constructs a new instance with the default realm.
	 */
	public AbstractObservableValue() {
		this(Realm.getDefault());
	}

	/**
	 * @param realm
	 */
	public AbstractObservableValue(Realm realm) {
		super(realm);
	}

	public synchronized void addValueChangeListener(IValueChangeListener listener) {
		addListener(ValueChangeEvent.TYPE, listener);
	}

	public synchronized void removeValueChangeListener(IValueChangeListener listener) {
		removeListener(ValueChangeEvent.TYPE, listener);
	}

	final public void setValue(Object value) {
		checkRealm();
		doSetValue(value);
	}

	/**
	 * Template method for setting the value of the observable. By default the
	 * method throws an {@link UnsupportedOperationException}.
	 * 
	 * @param value
	 */
	protected void doSetValue(Object value) {
		throw new UnsupportedOperationException();
	}

	protected void fireValueChange(ValueDiff diff) {
		// fire general change event first
		super.fireChange();
		fireEvent(new ValueChangeEvent(this, diff));
	}

	public final Object getValue() {
		getterCalled();
		return doGetValue();
	}

	abstract protected Object doGetValue();

	public boolean isStale() {
		getterCalled();
		return false;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireValueChange() instead"); //$NON-NLS-1$
	}
}
