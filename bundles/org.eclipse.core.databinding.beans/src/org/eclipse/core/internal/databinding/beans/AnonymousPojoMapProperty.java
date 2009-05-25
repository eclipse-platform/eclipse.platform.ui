/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 247997)
 *     Matthew Hall - bug 264307
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.property.map.DelegatingMapProperty;
import org.eclipse.core.databinding.property.map.IMapProperty;

/**
 * @since 3.3
 * 
 */
public class AnonymousPojoMapProperty extends DelegatingMapProperty {
	private final String propertyName;

	private Map delegates;

	/**
	 * @param propertyName
	 * @param keyType
	 * @param valueType
	 */
	public AnonymousPojoMapProperty(String propertyName, Class keyType,
			Class valueType) {
		super(keyType, valueType);
		this.propertyName = propertyName;
		this.delegates = new HashMap();
	}

	protected IMapProperty doGetDelegate(Object source) {
		Class beanClass = source.getClass();
		if (delegates.containsKey(beanClass))
			return (IMapProperty) delegates.get(beanClass);

		IMapProperty delegate;
		try {
			delegate = PojoProperties.map(beanClass, propertyName,
					(Class) getKeyType(), (Class) getValueType());
		} catch (IllegalArgumentException noSuchProperty) {
			delegate = null;
		}
		delegates.put(beanClass, delegate);
		return delegate;
	}

	public String toString() {
		String s = "?." + propertyName + "{:}"; //$NON-NLS-1$ //$NON-NLS-2$
		Class keyType = (Class) getKeyType();
		Class valueType = (Class) getValueType();
		if (keyType != null || valueType != null) {
			s += "<" + BeanPropertyHelper.shortClassName(keyType) + ", " //$NON-NLS-1$//$NON-NLS-2$
					+ BeanPropertyHelper.shortClassName(valueType) + ">"; //$NON-NLS-1$
		}
		return s;
	}
}
