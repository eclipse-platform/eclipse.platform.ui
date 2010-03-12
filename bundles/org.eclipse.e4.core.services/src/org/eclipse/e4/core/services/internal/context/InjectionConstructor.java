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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.injector.IObjectDescriptor;
import org.eclipse.e4.core.services.injector.IObjectProvider;

/**
 * Collection of static methods that deal with reflection-based injection at a low level.
 */
public class InjectionConstructor extends InjectionAbstract {

	private final Constructor constructor;

	public InjectionConstructor(Object userObject, IObjectProvider primarySupplier,
			Constructor constructor) {
		super(userObject, primarySupplier, false);
		this.constructor = constructor;
	}

	public boolean notify(ContextChangeEvent event) {
		if (event.getEventType() == ContextChangeEvent.INITIAL)
			make();
		return false; // constructor injection is static by nature
	}

	public Object make() {
		Object[] actualParams = processParams();
		if (actualParams == null)
			return null;
		try {
			return callConstructor(constructor, actualParams);
		} catch (InvocationTargetException e) {
			String msg = "Could not invoke " + constructor.getName();
			logError(msg, e);
			return null;
		} catch (InstantiationException e) {
			String msg = "Could not instantiate " + constructor.getName();
			logError(msg, e);
			return null;
		}
	}

	private Object[] processParams() {
		Class[] parameterTypes = constructor.getParameterTypes();
		InjectionProperties[] properties = annotationSupport.getInjectParamsProperties(constructor,
				primarySupplier);
		Object[] actualParams = new Object[properties.length];
		for (int i = 0; i < actualParams.length; i++) {
			try {
				IObjectDescriptor objectDescriptor = primarySupplier.makeDescriptor(properties[i]
						.getPropertyName(), parameterTypes[i]);
				actualParams[i] = getValue(objectDescriptor, properties[i], parameterTypes[i],
						false, false);
			} catch (IllegalArgumentException e) {
				String msg = "Unable to find matching arguments for " + constructor.getName();
				logError(msg, e);
				return null;
			}
		}
		return actualParams;
	}

	private Object callConstructor(Constructor constructor, Object[] args)
			throws InvocationTargetException, InstantiationException {
		if (args != null) { // make sure args are assignable
			Class[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length != args.length) {
				// internal error, log it
				logError(constructor, new IllegalArgumentException());
				return null;
			}
			for (int i = 0; i < args.length; i++) {
				if ((args[i] != null) && !parameterTypes[i].isAssignableFrom(args[i].getClass()))
					return null;
			}
		}

		Object result = null;
		boolean wasAccessible = true;
		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);
			wasAccessible = false;
		}
		try {
			result = constructor.newInstance(args);
		} catch (IllegalArgumentException e) {
			// should not happen, is checked at the start of this method
			logError(constructor, e);
			return null;
		} catch (IllegalAccessException e) {
			// should not happen as we set constructor to be accessible
			logError(constructor, e);
			return null;
		} finally {
			if (!wasAccessible)
				constructor.setAccessible(false);
		}
		return result;
	}
}
