/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.internal.databinding.Util;

/**
 * {@link IBeanObservable} decorator for an {@link IObservableValue}.
 * 
 * @since 3.3
 */
public class BeanObservableValueDecorator implements IObservableValue,
		IBeanObservable {
	private final IObservableValue delegate;
	private final PropertyDescriptor descriptor;
	private final IObservableValue observed;

	/**
	 * @param delegate
	 * @param observed 
	 * @param descriptor
	 */
	public BeanObservableValueDecorator(IObservableValue delegate, IObservableValue observed,
			PropertyDescriptor descriptor) {
		this.delegate = delegate;
		this.observed = observed;
		this.descriptor = descriptor;
	}

	public void addChangeListener(IChangeListener listener) {
		delegate.addChangeListener(listener);
	}

	public void addStaleListener(IStaleListener listener) {
		delegate.addStaleListener(listener);
	}

	public void addValueChangeListener(IValueChangeListener listener) {
		delegate.addValueChangeListener(listener);
	}

	public void dispose() {
		delegate.dispose();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof BeanObservableValueDecorator) {
			BeanObservableValueDecorator other = (BeanObservableValueDecorator) obj;
			return Util.equals(other.delegate, delegate);
		}
		return Util.equals(delegate, obj);
	}

	public Realm getRealm() {
		return delegate.getRealm();
	}

	public Object getValue() {
		return delegate.getValue();
	}

	public Object getValueType() {
		return delegate.getValueType();
	}
	
	public int hashCode() {
		return delegate.hashCode();
	}

	public boolean isStale() {
		return delegate.isStale();
	}

	public void removeChangeListener(IChangeListener listener) {
		delegate.removeChangeListener(listener);
	}

	public void removeStaleListener(IStaleListener listener) {
		delegate.removeStaleListener(listener);
	}

	public void removeValueChangeListener(IValueChangeListener listener) {
		delegate.removeValueChangeListener(listener);
	}

	public void setValue(Object value) {
		delegate.setValue(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.beans.IBeanObservable#getObserved()
	 */
	public Object getObserved() {
		return observed.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.beans.IBeanObservable#getPropertyDescriptor()
	 */
	public PropertyDescriptor getPropertyDescriptor() {
		return descriptor;
	}
	
	/**
	 * @return observable value delegate
	 */
	public IObservableValue getDelegate() {
		return delegate;
	}
}
