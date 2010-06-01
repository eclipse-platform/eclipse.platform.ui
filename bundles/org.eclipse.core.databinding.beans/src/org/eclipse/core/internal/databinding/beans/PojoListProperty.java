/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @since 3.3
 * 
 */
public class PojoListProperty extends SimpleListProperty {
	private final PropertyDescriptor propertyDescriptor;
	private final Class elementType;

	/**
	 * @param propertyDescriptor
	 * @param elementType
	 */
	public PojoListProperty(PropertyDescriptor propertyDescriptor,
			Class elementType) {
		this.propertyDescriptor = propertyDescriptor;
		this.elementType = elementType == null ? BeanPropertyHelper
				.getCollectionPropertyElementType(propertyDescriptor)
				: elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	protected List doGetList(Object source) {
		return asList(BeanPropertyHelper.readProperty(source,
				propertyDescriptor));
	}

	private List asList(Object propertyValue) {
		if (propertyValue == null)
			return Collections.EMPTY_LIST;
		if (propertyDescriptor.getPropertyType().isArray())
			return Arrays.asList((Object[]) propertyValue);
		return (List) propertyValue;
	}

	protected void doSetList(Object source, List list, ListDiff diff) {
		doSetList(source, list);
	}

	protected void doSetList(Object source, List list) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor,
				convertListToBeanPropertyType(list));
	}

	private Object convertListToBeanPropertyType(List list) {
		Object propertyValue = list;
		if (propertyDescriptor.getPropertyType().isArray()) {
			Class componentType = propertyDescriptor.getPropertyType()
					.getComponentType();
			Object[] array = (Object[]) Array.newInstance(componentType, list
					.size());
			list.toArray(array);
			propertyValue = array;
		}
		return propertyValue;
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null;
	}

	public String toString() {
		String s = BeanPropertyHelper.propertyName(propertyDescriptor) + "[]"; //$NON-NLS-1$
		if (elementType != null)
			s += "<" + BeanPropertyHelper.shortClassName(elementType) + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
