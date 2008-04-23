/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper class for calling non-accessible methods.
 * 
 * @since 3.4
 */
public class AccessorUtil {

	/**
	 * Invokes a zero-parameter method via reflection, making the method accessible first.
	 * 
	 * @param targetObject the target object
	 * @param declaringClass the declaring class of the method to be called
	 * @param methodName the method name
	 * @return the result
	 * @throws RuntimeException if the invocation fails
	 */
	public static Object invoke(Object targetObject, Class declaringClass, String methodName) {
		return invoke(targetObject, declaringClass, methodName, (Class[]) null, null);
	}

	/**
	 * Invokes a single-parameter method via reflection, making the method accessible first.
	 * 
	 * @param targetObject the target object
	 * @param declaringClass the declaring class of the method to be called
	 * @param methodName the method name
	 * @param parameterType0 the method's parameter type
	 * @param arg0 the argument used for the method call
	 * @return the result
	 * @throws RuntimeException if the invocation fails
	 */
	public static Object invoke(Object targetObject, Class declaringClass, String methodName, Class parameterType0, Object arg0) {
		return invoke(targetObject, declaringClass, methodName, new Class[] { parameterType0 }, new Object[] { arg0 });
	}
	
	/**
	 * Invokes a method via reflection, making the method accessible first.
	 * 
	 * @param targetObject the target object
	 * @param declaringClass the declaring class of the method to be called
	 * @param methodName the method name
	 * @param parameterTypes the method's parameter types, or <code>null</code> if none
	 * @param args the arguments used for the method call, or <code>null</code> if none
	 * @return the result
	 * @throws RuntimeException if the invocation fails
	 */
	public static Object invoke(Object targetObject, Class declaringClass, String methodName, Class[] parameterTypes, Object[] args) {
		try {
			Method method= declaringClass.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			return method.invoke(targetObject, args);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reads the value of a field via reflection, making the field accessible first.
	 * 
	 * @param targetObject the target object
	 * @param declaringClass the declaring class of the field to be read
	 * @param fieldName the field name
	 * @return the value of the field
	 * @throws RuntimeException if the invocation fails
	 */
	public static Object getValue(Object targetObject, Class declaringClass, String fieldName) {
		try {
			Field field= declaringClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(targetObject);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
}
