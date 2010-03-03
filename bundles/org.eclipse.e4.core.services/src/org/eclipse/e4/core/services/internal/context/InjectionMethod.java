/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.internal.context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.injector.IObjectProvider;

/**
 * Collection of static methods that deal with reflection-based injection at a low level.
 */
public class InjectionMethod extends InjectionAbstract {

	private final Method method;

	public InjectionMethod(Object userObject, IObjectProvider primarySupplier, Method method) {
		super(userObject, primarySupplier);
		this.method = method;
		InjectionProperties methodProps = annotationSupport.getInjectProperties(method);
		optional = methodProps.isOptional();
	}

	public boolean notify(ContextChangeEvent event) {
		Object userObject = getObject();
		if (userObject == null)
			return false;
		int eventType = event.getEventType();
		IObjectProvider changed = event.getContext();
		boolean ignoreMissing = ignoreMissing(eventType, changed);
		boolean injectWithNulls = injectNulls(eventType, changed);
		try {
			invoke(ignoreMissing, injectWithNulls);
		} catch (InjectionException e) {
			logError(method, e);
			return false;
		}
		return true;
	}

	public Object invoke(boolean ignoreMissing, boolean injectWithNulls) throws InjectionException {
		Object[] actualParams = processParams(ignoreMissing, injectWithNulls);
		if (actualParams == null) {
			if (!optional) {
				String msg = "Unable to find matching argument to call method \""
						+ method.getName() + "\"";
				throw new InjectionException(msg);
			}
			return null;
		}
		try {
			return callMethod(actualParams);
		} catch (InvocationTargetException e) {
			String msg = "Unexpected error invoking method \"" + method.getName() + "\"";
			throw new InjectionException(msg, e);
		}
	}

	private Object[] processParams(boolean ignoreMissing, boolean injectWithNulls) {
		Class[] parameterTypes = method.getParameterTypes();
		InjectionProperties[] properties = annotationSupport.getInjectParamProperties(method);
		Object[] actualParams = new Object[properties.length];
		for (int i = 0; i < actualParams.length; i++) {
			try {
				Object actualValue = getValue(properties[i], parameterTypes[i], ignoreMissing,
						injectWithNulls);
				if (actualValue == NOT_A_VALUE)
					return null;
				actualParams[i] = actualValue;
			} catch (IllegalArgumentException e) {
				String msg = "Could not invoke " + method.getName();
				logError(msg, e);
				return null;
			}
		}
		return actualParams;
	}

	private Object callMethod(Object[] args) throws InvocationTargetException {
		Object userObject = getObject();
		Object result = null;
		boolean wasAccessible = true;
		if (!method.isAccessible()) {
			method.setAccessible(true);
			wasAccessible = false;
		}
		try {
			result = method.invoke(userObject, args);
		} catch (IllegalArgumentException e) {
			// should not happen, is checked during formation of the array of actual arguments
			logError(method, e);
			return null;
		} catch (IllegalAccessException e) {
			// should not happen, is checked at the start of this method
			logError(method, e);
			return null;
		} finally {
			if (!wasAccessible)
				method.setAccessible(false);
		}
		return result;
	}
}
