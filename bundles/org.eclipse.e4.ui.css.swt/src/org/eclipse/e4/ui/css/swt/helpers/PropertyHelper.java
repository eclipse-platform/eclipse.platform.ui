/*******************************************************************************
 * Copyright (c) 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import java.util.HashMap;

import java.lang.reflect.Method;

import java.util.Map;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class PropertyHelper {
	private static final Map<String, Method> NOTNESTEDCACHE = new HashMap<String, Method>();

	public static Object getProperty(Object bean, String attr)
			throws Exception {
		String key = bean.getClass().getName() + "#" + attr;

		if (attr.indexOf('.') == -1) {
			Method readMethod = NOTNESTEDCACHE.get(key);
			if (readMethod != null) {
				return readMethod.invoke(bean);
			}
		}

		Method readMethod = null;
		Object value = null;
		value = bean;
		for (String part : attr.split("\\.")) {
			PropertyDescriptor desc = getPropertyDescriptor(value.getClass(),
					part);
			if (desc != null) {
				readMethod = desc.getReadMethod();
			}

			if (readMethod == null) {
				throw new IllegalArgumentException("Attribute '" + part
						+ "' is not known in '" + value + "'");
			} else {
				value = readMethod.invoke(value);
			}
		}

		if (attr.indexOf('.') == -1) {
			NOTNESTEDCACHE.put(key,readMethod);
		}
		
		return value;
	}

	private static PropertyDescriptor getPropertyDescriptor(Class<?> clazz,
			String name) throws IntrospectionException {
		PropertyDescriptor[] descs = getPropertyDescriptor(clazz);
		for (PropertyDescriptor desc : descs) {
			if (desc.getName().equals(name)) {
				return desc;
			}
		}
		return null;
	}

	private static PropertyDescriptor[] getPropertyDescriptor(Class<?> clazz)
			throws IntrospectionException {
		return Introspector.getBeanInfo(clazz).getPropertyDescriptors();
	}

}