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
 *     Matthew Hall - initial API and implementation (bug 247997)
 *     Matthew Hall - bug 264307
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.property.set.DelegatingSetProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;

/**
 * @param <S> type of the source object
 * @param <E> type of the elements in the set
 *
 * @since 3.3
 *
 */
public class AnonymousPojoSetProperty<S, E> extends DelegatingSetProperty<S, E> {
	private final String propertyName;

	private Map<Class<S>, ISetProperty<S, E>> delegates;

	/**
	 * @param propertyName
	 * @param elementType
	 */
	public AnonymousPojoSetProperty(String propertyName, Class<E> elementType) {
		super(elementType);
		this.propertyName = propertyName;
		this.delegates = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected ISetProperty<S, E> doGetDelegate(S source) {
		Class<S> beanClass = (Class<S>) source.getClass();
		if (delegates.containsKey(beanClass))
			return delegates.get(beanClass);

		ISetProperty<S, E> delegate;
		try {
			delegate = PojoProperties.set(beanClass, propertyName, (Class<E>) getElementType());
		} catch (IllegalArgumentException noSuchProperty) {
			delegate = null;
		}
		delegates.put(beanClass, delegate);
		return delegate;
	}

	@Override
	public String toString() {
		String s = "?." + propertyName + "{}"; //$NON-NLS-1$ //$NON-NLS-2$
		Class<?> elementType = (Class<?>) getElementType();
		if (elementType != null)
			s += "<" + BeanPropertyHelper.shortClassName(elementType) + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
