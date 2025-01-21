/*
 * Copyright (C) 2005, 2025 db4objects Inc.  http://www.db4o.com
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.ducks;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * RelaxedDuckType. Implements Duck Typing for Java. ("If it walks like a duck,
 * quacks like a duck, it..."). Essentially allows programs to treat objects
 * from separate hierarchies as if they were designed with common interfaces as
 * long as they adhere to common naming conventions.
 * <p>
 * This version is the relaxed DuckType. If a method in the interface is not
 * present on the underlying object, the proxy simply returns null.
 *
 * @author djo
 */
public class RelaxedDuckType extends DuckType implements InvocationHandler {

	public static Object implement(Class<?> interfaceToImplement, Object object) {
		return Proxy.newProxyInstance(interfaceToImplement.getClassLoader(), new Class[] { interfaceToImplement },
				new RelaxedDuckType(object));
	}

	public static boolean includes(Object object, String method, Class<?>... args) {
		try {
			object.getClass().getMethod(method, args);
		} catch (NoSuchMethodException e) {
			return false;
		}
		return true;
	}

	private static final Map<Class<?>, Object> NULL_VALUES = new HashMap<>();

	{
		NULL_VALUES.put(Boolean.TYPE, Boolean.FALSE);
		NULL_VALUES.put(Integer.TYPE, Integer.valueOf(0));
		NULL_VALUES.put(Float.TYPE, Float.valueOf(0));
		NULL_VALUES.put(Long.TYPE, Long.valueOf(0));
		NULL_VALUES.put(Double.TYPE, Double.valueOf(0));
		NULL_VALUES.put(Character.TYPE, Character.valueOf(' '));
	}

	protected RelaxedDuckType(Object object) {
		super(object);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			Method realMethod = objectClass.getMethod(method.getName(), method.getParameterTypes());
			return realMethod.invoke(object, args);
		} catch (NoSuchMethodException e) {
			return NULL_VALUES.get(method.getReturnType());
		} catch (Throwable t) {
			throw t;
		}
	}

}
