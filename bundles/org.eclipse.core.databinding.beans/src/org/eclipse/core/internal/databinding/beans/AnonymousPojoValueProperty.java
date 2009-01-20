/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 247997)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;

/**
 * @since 3.3
 * 
 */
public class AnonymousPojoValueProperty extends DelegatingValueProperty {
	private final String propertyName;

	private Map delegates;

	/**
	 * @param propertyName
	 * @param valueType
	 */
	public AnonymousPojoValueProperty(String propertyName, Class valueType) {
		super(valueType);
		this.propertyName = propertyName;
		this.delegates = new HashMap();
	}

	protected IValueProperty doGetDelegate(Object source) {
		Class pojoClass = source.getClass();
		if (delegates.containsKey(pojoClass))
			return (IValueProperty) delegates.get(pojoClass);

		IValueProperty delegate;
		try {
			delegate = PojoProperties.value(pojoClass, propertyName,
					(Class) getValueType());
		} catch (BindingException noSuchProperty) {
			delegate = null;
		}
		delegates.put(pojoClass, delegate);
		return delegate;
	}

	public String toString() {
		String s = "{{Generic POJO}}." + propertyName; //$NON-NLS-1$
		Class valueType = (Class) getValueType();
		if (valueType != null)
			s += " <" + valueType.getName() + ">"; //$NON-NLS-1$ //$NON-NLS-2$
		return s;
	}
}
