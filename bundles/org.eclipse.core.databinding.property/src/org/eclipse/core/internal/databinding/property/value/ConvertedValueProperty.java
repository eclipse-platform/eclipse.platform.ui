/*******************************************************************************
 * Copyright (c) 2010, 2020 Ovidio Mallo and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 306611)
 *     Jens Lidestrom - move from org.eclipse.core.databinding (bug 558842)
 ******************************************************************************/
package org.eclipse.core.internal.databinding.property.value;

import java.util.function.Function;

import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * Simple value property which applies a given converter on a source object in
 * order to produce the property's value.
 *
 * @param <S> type of the source object
 * @param <T> type of the value of the property (after conversion)
 */
public class ConvertedValueProperty<S, T> extends SimpleValueProperty<S, T> {
	private final Function<? super S, ? extends T> converter;
	private final Object valueType;

	/**
	 * Creates a new value property which applies the given converter on the source
	 * object in order to produce the property's value.
	 *
	 * @param converter converter to apply to the source object.
	 */
	public ConvertedValueProperty(Object valueType, Function<? super S, ? extends T> converter) {
		this.valueType = valueType;
		this.converter = converter;
	}

	@Override
	public Object getValueType() {
		return valueType;
	}

	@Override
	public T getValue(S source) {
		// We do also pass null values to the converter
		return doGetValue(source);
	}

	@Override
	protected T doGetValue(S source) {
		return converter.apply(source);
	}

	@Override
	protected void doSetValue(S source, T value) {
		// Setting a value is not supported
		throw new UnsupportedOperationException(this + ": Setter not supported on a converted value!"); //$NON-NLS-1$
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ValueDiff<? extends T>> listener) {
		// No listener API
		return null;
	}
}
