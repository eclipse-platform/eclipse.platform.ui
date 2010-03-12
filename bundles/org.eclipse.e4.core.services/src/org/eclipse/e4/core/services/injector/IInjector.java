/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
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

/**
 * Describes externally-visible injector functionality.
 * <p>
 * NOTE: This is a preliminary form; this API will change.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IInjector {

	public boolean inject(Object object, IObjectProvider objectProvider);

	public Object make(Class clazz, IObjectProvider objectProvider)
			throws InvocationTargetException, InstantiationException;

	public Object invoke(Object object, String methodName, IObjectProvider objectProvider)
			throws InvocationTargetException, CoreException;

	public Object invoke(Object object, String methodName, Object defaultValue,
			IObjectProvider objectProvider) throws InvocationTargetException;

	public boolean injectStatic(Class clazz, IObjectProvider objectProvider);

}
