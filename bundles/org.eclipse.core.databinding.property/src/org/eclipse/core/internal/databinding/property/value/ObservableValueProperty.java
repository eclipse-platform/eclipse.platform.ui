/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263709)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * @since 3.3
 * 
 */
/*
 * This class extends SimpleValueProperty rather than ValueProperty so make it
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
		return new Listener(listener);
	}

	private class Listener implements INativePropertyListener,
			IValueChangeListener {
		private final ISimplePropertyListener listener;

		Listener(ISimplePropertyListener listener) {
			this.listener = listener;
		}

		public void handleValueChange(ValueChangeEvent event) {
			listener
					.handlePropertyChange(new SimplePropertyEvent(event
							.getObservable(), ObservableValueProperty.this,
							event.diff));
		}
	}

	protected void doAddListener(Object source, INativePropertyListener listener) {
		((IObservableValue) source)
				.addValueChangeListener((IValueChangeListener) listener);
	}

	protected void doRemoveListener(Object source,
			INativePropertyListener listener) {
		((IObservableValue) source)
				.removeValueChangeListener((IValueChangeListener) listener);
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