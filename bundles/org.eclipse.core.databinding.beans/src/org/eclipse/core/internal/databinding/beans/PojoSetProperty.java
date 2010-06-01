/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @since 3.3
 * 
 */
public class PojoSetProperty extends SimpleSetProperty {
	private final PropertyDescriptor propertyDescriptor;
	private final Class elementType;

	/**
	 * @param propertyDescriptor
	 * @param elementType
	 */
	public PojoSetProperty(PropertyDescriptor propertyDescriptor,
			Class elementType) {
		this.propertyDescriptor = propertyDescriptor;
		this.elementType = elementType == null ? BeanPropertyHelper
				.getCollectionPropertyElementType(propertyDescriptor)
				: elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	protected Set doGetSet(Object source) {
		return asSet(BeanPropertyHelper
				.readProperty(source, propertyDescriptor));
	}

	private Set asSet(Object propertyValue) {
		if (propertyValue == null)
			return Collections.EMPTY_SET;
		if (propertyDescriptor.getPropertyType().isArray())
			return new HashSet(Arrays.asList((Object[]) propertyValue));
		return (Set) propertyValue;
	}

	protected void doSetSet(Object source, Set set, SetDiff diff) {
		doSetSet(source, set);
	}

	protected void doSetSet(Object source, Set set) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor,
				convertSetToBeanPropertyType(set));
	}

	private Object convertSetToBeanPropertyType(Set set) {
		Object propertyValue = set;
		if (propertyDescriptor.getPropertyType().isArray()) {
			Class componentType = propertyDescriptor.getPropertyType()
					.getComponentType();
			Object[] array = (Object[]) Array.newInstance(componentType, set
					.size());
			propertyValue = set.toArray(array);
		}
		return propertyValue;
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null;
	}

	public String toString() {
		String s = BeanPropertyHelper.propertyName(propertyDescriptor) + "{}"; //$NON-NLS-1$
		if (elementType != null)
			s += "<" + BeanPropertyHelper.shortClassName(elementType) + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
