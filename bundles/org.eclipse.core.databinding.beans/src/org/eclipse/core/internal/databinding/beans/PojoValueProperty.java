/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 264307, 265561
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;

/**
 * @param <S> type of the source object
 * @param <T> type of the value of the property
 *
 * @since 3.3
 *
 */
public class PojoValueProperty<S, T> extends SimpleValueProperty<S, T> {
	private final PropertyDescriptor propertyDescriptor;
	private final Class<T> valueType;

	/**
	 * @param propertyDescriptor
	 * @param valueType
	 */
	@SuppressWarnings("unchecked")
	public PojoValueProperty(PropertyDescriptor propertyDescriptor, Class<T> valueType) {
		this.propertyDescriptor = propertyDescriptor;
		this.valueType = valueType == null ? (Class<T>) propertyDescriptor.getPropertyType() : valueType;
	}

	@Override
	public Object getValueType() {
		return valueType;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T doGetValue(S source) {
		if (source == null)
			return null;
		return (T) BeanPropertyHelper.readProperty(source, propertyDescriptor);
	}

	@Override
	protected void doSetValue(Object source, Object value) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor, value);
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ValueDiff<? extends T>> listener) {
		return null;
	}

	@Override
	public String toString() {
		String s = BeanPropertyHelper.propertyName(propertyDescriptor);
		if (valueType != null)
			s += "<" + BeanPropertyHelper.shortClassName(valueType) + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
