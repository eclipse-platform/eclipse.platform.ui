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
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.set.SimpleSetProperty;

/**
 * @param <S> type of the source object
 * @param <E> type of the elements in the set
 *
 * @since 3.3
 *
 */
public class PojoSetProperty<S, E> extends SimpleSetProperty<S, E> {
	private final PropertyDescriptor propertyDescriptor;
	private final Class<E> elementType;

	/**
	 * @param propertyDescriptor
	 * @param elementType
	 */
	@SuppressWarnings("unchecked")
	public PojoSetProperty(PropertyDescriptor propertyDescriptor, Class<E> elementType) {
		this.propertyDescriptor = propertyDescriptor;
		this.elementType = elementType == null
				? (Class<E>) BeanPropertyHelper.getCollectionPropertyElementType(propertyDescriptor)
				: elementType;
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	protected Set<E> doGetSet(S source) {
		return asSet(BeanPropertyHelper.readProperty(source, propertyDescriptor));
	}

	@SuppressWarnings("unchecked")
	private Set<E> asSet(Object propertyValue) {
		if (propertyValue == null)
			return Collections.emptySet();
		if (propertyDescriptor.getPropertyType().isArray())
			return new HashSet<>(Arrays.asList((E[]) propertyValue));
		return (Set<E>) propertyValue;
	}

	@Override
	protected void doSetSet(S source, Set<E> set, SetDiff<E> diff) {
		doSetSet(source, set);
	}

	@Override
	protected void doSetSet(S source, Set<E> set) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor, convertSetToBeanPropertyType(set));
	}

	private Object convertSetToBeanPropertyType(Set<E> set) {
		Object propertyValue = set;
		if (propertyDescriptor.getPropertyType().isArray()) {
			Class<?> componentType = propertyDescriptor.getPropertyType().getComponentType();
			Object[] array = (Object[]) Array.newInstance(componentType, set.size());
			propertyValue = set.toArray(array);
		}
		return propertyValue;
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, SetDiff<E>> listener) {
		return null;
	}

	@Override
	public String toString() {
		String s = BeanPropertyHelper.propertyName(propertyDescriptor) + "{}"; //$NON-NLS-1$
		if (elementType != null)
			s += "<" + BeanPropertyHelper.shortClassName(elementType) + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
