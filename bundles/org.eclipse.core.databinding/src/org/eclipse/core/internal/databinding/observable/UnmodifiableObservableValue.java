/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 219909)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;

/**
 * An unmodifiable wrapper class for IObservableValue instances.
 * @since 1.1
 */
public class UnmodifiableObservableValue extends AbstractObservableValue {
	private IObservableValue wrappedValue;

	/**
	 * Constructs an UnmodifiableObservableValue which wraps the given
	 * observable value
	 * 
	 * @param wrappedValue
	 *            the observable value to wrap in an unmodifiable instance.
	 */
	public UnmodifiableObservableValue(IObservableValue wrappedValue) {
		super(wrappedValue.getRealm());

		this.wrappedValue = wrappedValue;
		wrappedValue.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				fireValueChange(event.diff);
			}
		});
		wrappedValue.addStaleListener(new IStaleListener() {
			public void handleStale(StaleEvent staleEvent) {
				fireStale();
			}
		});
	}

	protected Object doGetValue() {
		return wrappedValue.getValue();
	}

	public Object getValueType() {
		return wrappedValue.getValueType();
	}
}
