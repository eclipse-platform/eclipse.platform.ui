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
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.map.SimpleMapProperty;

/**
 * @since 3.3
 * 
 */
public class PojoMapProperty extends SimpleMapProperty {
	private final PropertyDescriptor propertyDescriptor;
	private final Class keyType;
	private final Class valueType;

	/**
	 * @param propertyDescriptor
	 * @param keyType
	 * @param valueType
	 */
	public PojoMapProperty(PropertyDescriptor propertyDescriptor,
			Class keyType, Class valueType) {
		this.propertyDescriptor = propertyDescriptor;
		this.keyType = keyType;
		this.valueType = valueType;
	}

	public Object getKeyType() {
		return keyType;
	}

	public Object getValueType() {
		return valueType;
	}

	protected Map doGetMap(Object source) {
		return asMap(BeanPropertyHelper
				.readProperty(source, propertyDescriptor));
	}

	private Map asMap(Object propertyValue) {
		if (propertyValue == null)
			return new HashMap();
		return (Map) propertyValue;
	}

	protected void doSetMap(Object source, Map map, MapDiff diff) {
		doSetMap(source, map);
	}

	protected void doSetMap(Object source, Map map) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor, map);
	}

	public INativePropertyListener adaptListener(
			ISimplePropertyListener listener) {
		return null;
	}

	public String toString() {
		String s = BeanPropertyHelper.propertyName(propertyDescriptor) + "{:}"; //$NON-NLS-1$

		if (keyType != null || valueType != null)
			s += "<" + BeanPropertyHelper.shortClassName(keyType) + ", " //$NON-NLS-1$ //$NON-NLS-2$
					+ BeanPropertyHelper.shortClassName(valueType) + ">"; //$NON-NLS-1$
		return s;
	}
}
