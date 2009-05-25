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
import org.eclipse.core.databinding.property.list.DelegatingListProperty;
import org.eclipse.core.databinding.property.list.IListProperty;

/**
 * @since 3.3
 * 
 */
public class AnonymousPojoListProperty extends DelegatingListProperty {
	private final String propertyName;

	private Map delegates;

	/**
	 * @param propertyName
	 * @param elementType
	 */
	public AnonymousPojoListProperty(String propertyName, Class elementType) {
		super(elementType);
		this.propertyName = propertyName;
		this.delegates = new HashMap();
	}

	protected IListProperty doGetDelegate(Object source) {
		Class beanClass = source.getClass();
		if (delegates.containsKey(beanClass))
			return (IListProperty) delegates.get(beanClass);

		IListProperty delegate;
		try {
			delegate = PojoProperties.list(beanClass, propertyName,
					(Class) getElementType());
		} catch (IllegalArgumentException noSuchProperty) {
			delegate = null;
		}
		delegates.put(beanClass, delegate);
		return delegate;
	}

	public String toString() {
		String s = "?." + propertyName + "{}"; //$NON-NLS-1$ //$NON-NLS-2$
		Class elementType = (Class) getElementType();
		if (elementType != null)
			s += "<" + BeanPropertyHelper.shortClassName(elementType) + ">"; //$NON-NLS-1$//$NON-NLS-2$
		return s;
	}
}
