/*******************************************************************************
 * Copyright (c) 2005, 2009 Matt Carter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matt Carter - initial API and implementation (bug 212518)
 *     Matthew Hall - bug 212518, 146397, 249526
 *******************************************************************************/
package org.eclipse.core.internal.databinding.observable;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;

/**
 * An immutable {@link IObservableValue}.
 * 
 * @see WritableValue
 */
public class ConstantObservableValue implements IObservableValue {
	final Realm realm;
	final Object value;
	final Object type;

	/**
	 * Construct a constant value of the given type, in the default realm.
	 * 
	 * @param value
	 *            immutable value
	 * @param type
	 *            type
	 */
	public ConstantObservableValue(Object value, Object type) {
		this(Realm.getDefault(), value, type);
	}

	/**
	 * Construct a constant value of the given type, in the given realm.
	 * 
	 * @param realm
	 *            Realm
	 * @param value
	 *            immutable value
	 * @param type
	 *            type
	 */
	public ConstantObservableValue(Realm realm, Object value, Object type) {
		Assert.isNotNull(realm, "Realm cannot be null"); //$NON-NLS-1$
		this.realm = realm;
		this.value = value;
		this.type = type;
		ObservableTracker.observableCreated(this);
	}

	@Override
	public Object getValueType() {
		return type;
	}

	@Override
	public Object getValue() {
		ObservableTracker.getterCalled(this);
		return value;
	}

	@Override
	public void setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addValueChangeListener(IValueChangeListener listener) {
		// ignore
	}

	@Override
	public void removeValueChangeListener(IValueChangeListener listener) {
		// ignore
	}

	@Override
	public void addChangeListener(IChangeListener listener) {
		// ignore
	}

	@Override
	public void addDisposeListener(IDisposeListener listener) {
		// ignore
	}

	@Override
	public void addStaleListener(IStaleListener listener) {
		// ignore
	}

	@Override
	public boolean isDisposed() {
		return false;
	}

	@Override
	public void dispose() {
		// nothing to dispose
	}

	@Override
	public Realm getRealm() {
		return realm;
	}

	@Override
	public boolean isStale() {
		return false;
	}

	@Override
	public void removeChangeListener(IChangeListener listener) {
		// ignore
	}

	@Override
	public void removeDisposeListener(IDisposeListener listener) {
		// ignore
	}

	@Override
	public void removeStaleListener(IStaleListener listener) {
		// ignore
	}
}