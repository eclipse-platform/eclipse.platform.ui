/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263709)
 *     Matthew Hall - bugs 265561, 262287
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * @since 3.3
 * 
 */
/*
 * This class extends SimpleValueProperty rather than ValueProperty to make it
 * easy to observe multiple IObservableValues, for example an IObservableList of
 * IObservableValues. In the simple case of observe(Object) or
 * observeDetail(IObservableValue) we just cast the source object to
 * IObservableValue and return it.
 */
public class ObservableValueProperty extends SimpleValueProperty {
	private final Object valueType;

	/**
	 * @param valueType
	 */
	public ObservableValueProperty(Object valueType) {
		this.valueType = valueType;
	}

	public Object getValueType() {
		return valueType;
	}

	protected Object doGetValue(Object source) {
		return ((IObservableValue) source).getValue();
	}

	protected void doSetValue(Object source, Object value) {
		((IObservableValue) source).setValue(value);
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return new Listener(this, listener);
	}

	private class Listener extends NativePropertyListener implements
			IValueChangeListener, IStaleListener {
		Listener(IProperty property, ISimplePropertyListener listener) {
			super(property, listener);
		}

		public void handleValueChange(ValueChangeEvent event) {
			fireChange(event.getObservable(), event.diff);
		}

		public void handleStale(StaleEvent event) {
			fireStale(event.getObservable());
		}

		protected void doAddTo(Object source) {
			IObservableValue observable = (IObservableValue) source;
			observable.addValueChangeListener(this);
			observable.addStaleListener(this);
		}

		protected void doRemoveFrom(Object source) {
			IObservableValue observable = (IObservableValue) source;
			observable.removeValueChangeListener(this);
			observable.removeStaleListener(this);
		}
	}

	public IObservableValue observe(Realm realm, Object source) {
		// Ignore realm if different
		return (IObservableValue) source;
	}

	public String toString() {
		String result = "IObservableValue#value"; //$NON-NLS-1$
		if (valueType != null)
			result += " <" + valueType + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}
}