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
 *     Matthew Hall - bug 195222, 264307, 265561
 *     Ovidio Mallo - bug 306633
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.list.SimpleListProperty;

/**
 * @param <S> type of the source object
 * @param <E> type of the elements in the list
 *
 * @since 3.3
 *
 */
public class PojoListProperty<S, E> extends SimpleListProperty<S, E> {
	private final PropertyDescriptor propertyDescriptor;
	private final Class<E> elementType;

	/**
	 * @param propertyDescriptor
	 * @param elementType
	 */
	@SuppressWarnings("unchecked")
	public PojoListProperty(PropertyDescriptor propertyDescriptor, Class<E> elementType) {
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
	protected List<E> doGetList(S source) {
		return asList(BeanPropertyHelper.readProperty(source, propertyDescriptor));
	}

	@SuppressWarnings("unchecked")
	private List<E> asList(Object propertyValue) {
		if (propertyValue == null)
			return Collections.emptyList();
		if (propertyDescriptor.getPropertyType().isArray())
			return Arrays.asList((E[]) propertyValue);
		return (List<E>) propertyValue;
	}

	@Override
	protected void doSetList(S source, List<E> list, ListDiff<E> diff) {
		doSetList(source, list);
	}

	@Override
	protected void doSetList(S source, List<E> list) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor, convertListToBeanPropertyType(list));
	}

	private Object convertListToBeanPropertyType(List<E> list) {
		Object propertyValue = list;
		if (propertyDescriptor.getPropertyType().isArray()) {
			Class<?> componentType = propertyDescriptor.getPropertyType().getComponentType();
			Object[] array = (Object[]) Array.newInstance(componentType, list.size());
			list.toArray(array);
			propertyValue = array;
		}
		return propertyValue;
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ListDiff<E>> listener) {
		return null;
	}

	@Override
	public String toString() {
		String s = BeanPropertyHelper.propertyName(propertyDescriptor) + "[]"; //$NON-NLS-1$
		if (elementType != null)
			s += "<" + BeanPropertyHelper.shortClassName(elementType) + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
