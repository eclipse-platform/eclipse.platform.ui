/*******************************************************************************
 * Copyright (c) 2010, 2015 Ovidio Mallo and others.
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
 ******************************************************************************/

package org.eclipse.core.internal.databinding;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * Simple value property which applies a given converter on a source object in
 * order to produce the property's value.
 *
 * @param <S>
 *            type of the source object
 * @param <T>
 *            type of the value of the property (after conversion)
 */
public class ConverterValueProperty<S, T> extends SimpleValueProperty<S, T> {

	private final IConverter<S, T> converter;

	/**
	 * Creates a new value property which applies the given converter on the
	 * source object in order to produce the property's value.
	 *
	 * @param converter
	 *            The converter to apply to the source object.
	 */
	public ConverterValueProperty(IConverter<S, T> converter) {
		this.converter = converter;
	}

	@Override
	public Object getValueType() {
		// the property type is the converter's target type
		return converter.getToType();
	}

	@Override
	public T getValue(S source) {
		// We do also pass null values to the converter.
		return doGetValue(source);
	}

	@Override
	protected T doGetValue(S source) {
		// delegate to the IConverter
		return converter.convert(source);
	}

	@Override
	protected void doSetValue(S source, T value) {
		// setting a value is not supported
		throw new UnsupportedOperationException(toString()
				+ ": Setter not supported on a converted value!"); //$NON-NLS-1$
	}

	@Override
	public INativePropertyListener<S> adaptListener(
			ISimplePropertyListener<S, ValueDiff<? extends T>> listener) {
		// no listener API
		return null;
	}

	@Override
	public String toString() {
		return "IConverter#convert(source) <IConverter#getToType()>"; //$NON-NLS-1$
	}
}
