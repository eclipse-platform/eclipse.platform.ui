/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.services.injector;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.services.internal.context.ContextInjector;

/**
 * Injector implementation. NOTE: This is a preliminary form; this API will change.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
final public class Injector {

	final private ContextInjector eInjector;

	public Injector(IObjectProvider context) {
		eInjector = new ContextInjector(context);
	}

	public boolean inject(Object object) {
		return eInjector.inject(object);
	}

	public Object make(Class clazz) throws InvocationTargetException, InstantiationException {
		return eInjector.make(clazz);
	}

	public Object invoke(Object object, String methodName) throws InvocationTargetException,
			CoreException {
		return eInjector.invoke(object, methodName);
	}

	public Object invoke(Object object, String methodName, Object defaultValue)
			throws InvocationTargetException {
		return eInjector.invoke(object, methodName, defaultValue);
	}

	public boolean injectStatic(Class clazz) {
		return eInjector.injectStatic(clazz);
	}

}
