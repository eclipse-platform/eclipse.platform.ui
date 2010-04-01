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

	final public static Object NOT_A_VALUE = new Object();

	public boolean inject(Object object, AbstractObjectSupplier objectSupplier);

	public boolean uninject(Object object, AbstractObjectSupplier objectSupplier);

	public Object invoke(Object object, String methodName, AbstractObjectSupplier objectSupplier)
			throws InvocationTargetException, CoreException;

	public Object invoke(Object object, String methodName, Object defaultValue,
			AbstractObjectSupplier objectSupplier) throws InvocationTargetException;

	public Object make(Class<?> clazz, AbstractObjectSupplier objectSupplier)
			throws InvocationTargetException, InstantiationException;

	public Object make(IObjectDescriptor descriptor, AbstractObjectSupplier objectSupplier)
			throws InvocationTargetException, InstantiationException;

	public boolean injectStatic(Class<?> clazz, AbstractObjectSupplier objectSupplier);

	public boolean update(IRequestor[] requestors, AbstractObjectSupplier objectSupplier);

	public boolean disposed(AbstractObjectSupplier objectSupplier);

	public IBinding addBinding(Class<?> clazz);

	public IBinding addBinding(IBinding binding);

}
