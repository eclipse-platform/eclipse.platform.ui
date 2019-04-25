/*******************************************************************************
 * Copyright (c) 2008, 2019 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 247997)
 *     Matthew Hall - bugs 264307, 264619
 *     Justin Kuenzel - NPE
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;

/**
 * @param <S> type of the source object
 * @param <T> type of the value of the property
 * @since 3.3
 *
 */
public class AnonymousBeanValueProperty<S, T> extends DelegatingValueProperty<S, T> {
	private final String propertyName;

	private Map<Class<S>, IValueProperty<S, T>> delegates;

	/**
	 * @param propertyName
	 * @param valueType
	 */
	public AnonymousBeanValueProperty(String propertyName, Class<T> valueType) {
		super(valueType);
		this.propertyName = propertyName;
		this.delegates = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IValueProperty<S, T> doGetDelegate(S source) {
		return getClassDelegate((Class<S>) source.getClass());
	}

	@SuppressWarnings("unchecked")
	private IValueProperty<S, T> getClassDelegate(Class<S> beanClass) {
		if (delegates.containsKey(beanClass))
			return delegates.get(beanClass);

		IValueProperty<S, T> delegate;
		try {
			delegate = BeanProperties.value(beanClass, propertyName, (Class<T>) getValueType());
		} catch (IllegalArgumentException noSuchProperty) {
			delegate = null;
		}
		delegates.put(beanClass, delegate);
		return delegate;
	}

	@Override
	public <M extends S> IObservableValue<T> observeDetail(IObservableValue<M> master) {
		Object valueType = getValueType();
		if (valueType == null)
			valueType = inferValueType(master.getValueType());
		return MasterDetailObservables.detailValue(master, valueFactory(master
				.getRealm()), valueType);
	}

	@SuppressWarnings("unchecked")
	private Object inferValueType(Object masterObservableValueType) {
		if (masterObservableValueType instanceof Class) {
			IValueProperty<?, ?> classDelegate = getClassDelegate((Class<S>) masterObservableValueType);
			return classDelegate != null ? classDelegate.getValueType() : null;
		}
		return null;
	}

	@Override
	public String toString() {
		String s = "?." + propertyName; //$NON-NLS-1$
		Class<?> valueType = (Class<?>) getValueType();
		if (valueType != null)
			s += "<" + BeanPropertyHelper.shortClassName(valueType) + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
