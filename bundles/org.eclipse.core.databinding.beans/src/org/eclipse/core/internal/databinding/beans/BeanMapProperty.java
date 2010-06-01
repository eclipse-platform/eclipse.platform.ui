/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 264307, 265561, 301774
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.map.SimpleMapProperty;

/**
 * @since 3.3
 * 
 */
public class BeanMapProperty extends SimpleMapProperty {
	private final PropertyDescriptor propertyDescriptor;
	private final Class keyType;
	private final Class valueType;

	/**
	 * @param propertyDescriptor
	 * @param keyType
	 * @param valueType
	 */
	public BeanMapProperty(PropertyDescriptor propertyDescriptor,
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
			return Collections.EMPTY_MAP;
		return (Map) propertyValue;
	}

	protected void doSetMap(Object source, Map map, MapDiff diff) {
		doSetMap(source, map);
	}

	protected void doSetMap(Object source, Map map) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor, map);
	}

	public INativePropertyListener adaptListener(
			final ISimplePropertyListener listener) {
		return new BeanPropertyListener(this, propertyDescriptor, listener) {
			protected IDiff computeDiff(Object oldValue, Object newValue) {
				return Diffs.computeMapDiff(asMap(oldValue), asMap(newValue));
			}
		};
	}

	public String toString() {
		String s = BeanPropertyHelper.propertyName(propertyDescriptor) + "{:}"; //$NON-NLS-1$

		if (keyType != null || valueType != null)
			s += "<" + BeanPropertyHelper.shortClassName(keyType) + ", " //$NON-NLS-1$ //$NON-NLS-2$
					+ BeanPropertyHelper.shortClassName(valueType) + ">"; //$NON-NLS-1$
		return s;
	}
}
