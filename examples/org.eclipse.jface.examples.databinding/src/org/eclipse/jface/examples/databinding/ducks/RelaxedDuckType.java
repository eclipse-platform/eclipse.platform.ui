/*
 * Copyright (C) 2005, 2015 db4objects Inc.  http://www.db4o.com
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * RelaxedDuckType. Implements Duck Typing for Java.  ("If it walks like a duck,
 * quacks like a duck, it...").  Essentially allows programs to treat
 * objects from separate hierarchies as if they were designed with common
 * interfaces as long as they adhere to common naming conventions.
 * <p>
 * This version is the relaxed DuckType.  If a method in the interface is
 * not present on the underlying object, the proxy simply returns null.
 *
 * @author djo
 */
public class RelaxedDuckType extends DuckType implements InvocationHandler {

	public static Object implement(Class interfaceToImplement, Object object) {
		return Proxy.newProxyInstance(interfaceToImplement.getClassLoader(),
				new Class[] {interfaceToImplement}, new RelaxedDuckType(object));
	}

	public static boolean includes(Object object, String method, Class<?>[] args) {
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
        NULL_VALUES.put(Float.TYPE, new Float(0));
        NULL_VALUES.put(Long.TYPE, new Long(0));
        NULL_VALUES.put(Double.TYPE, new Double(0));
        NULL_VALUES.put(Character.TYPE, new Character(' '));
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
