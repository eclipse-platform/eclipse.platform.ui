/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 263709)
 *     Matthew Hall - bugs 265561, 262287
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * @param <T>
 *            type of the value of the property
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
public class ObservableValueProperty<T> extends SimpleValueProperty<IObservableValue<T>, T> {
	private final Object valueType;

	/**
	 * @param valueType
	 */
	public ObservableValueProperty(Object valueType) {
		this.valueType = valueType;
	}

	@Override
	public Object getValueType() {
		return valueType;
	}

	@Override
	protected T doGetValue(IObservableValue<T> source) {
		return source.getValue();
	}

	@Override
	protected void doSetValue(IObservableValue<T> source, T value) {
		source.setValue(value);
	}

	@Override
	public INativePropertyListener<IObservableValue<T>> adaptListener(
			ISimplePropertyListener<IObservableValue<T>, ValueDiff<? extends T>> listener) {
		return new Listener(this, listener);
	}

	private class Listener extends NativePropertyListener<IObservableValue<T>, ValueDiff<? extends T>>
			implements IValueChangeListener<T>, IStaleListener {
		Listener(IProperty property, ISimplePropertyListener<IObservableValue<T>, ValueDiff<? extends T>> listener) {
			super(property, listener);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleValueChange(ValueChangeEvent<? extends T> event) {
			// This is a safe cast, since the only way to add this listener is
			// via doAddTo, below,
			// and that takes a source of type IObservableValue<T>
			fireChange((IObservableValue<T>) event.getObservableValue(), event.diff);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleStale(StaleEvent event) {
			// This is a safe cast, since the only way to add this listener is
			// via doAddTo, below,
			// and that takes a source of type IObservableValue<T>
			fireStale((IObservableValue<T>) event.getObservable());
		}

		@Override
		protected void doAddTo(IObservableValue<T> source) {
			IObservableValue<T> observable = source;
			observable.addValueChangeListener(this);
			observable.addStaleListener(this);
		}

		@Override
		protected void doRemoveFrom(IObservableValue<T> source) {
			IObservableValue<T> observable = source;
			observable.removeValueChangeListener(this);
			observable.removeStaleListener(this);
		}
	}

	@Override
	public IObservableValue<T> observe(Realm realm, IObservableValue<T> source) {
		// Ignore realm if different
		return source;
	}

	@Override
	public String toString() {
		String result = "IObservableValue#value"; //$NON-NLS-1$
		if (valueType != null)
			result += " <" + valueType + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}
}