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
package org.eclipse.e4.core.di;

import java.lang.annotation.Annotation;
import org.eclipse.e4.core.di.suppliers.AbstractObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;

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

	/**
	 * @param object
	 * @param objectSupplier
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public void inject(Object object, AbstractObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * @param object
	 * @param objectSupplier
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public void uninject(Object object, AbstractObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * @param object
	 * @param qualifier
	 * @param objectSupplier
	 * @return the return value of the method call, might be <code>null</code>
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public Object invoke(Object object, Class<? extends Annotation> qualifier, AbstractObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * @param object
	 * @param qualifier
	 * @param defaultValue
	 * @param objectSupplier
	 * @return the return value of the method call, might be <code>null</code>
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public Object invoke(Object object, Class<? extends Annotation> qualifier, Object defaultValue, AbstractObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * @param clazz
	 * @param objectSupplier
	 * @return an instance of the specified class
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public <T> T make(Class<T> clazz, AbstractObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * @param descriptor
	 * @param objectSupplier
	 * @return an instance of the specified class
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public Object make(IObjectDescriptor descriptor, AbstractObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * @param objectSupplier
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public void disposed(AbstractObjectSupplier objectSupplier) throws InjectionException;

	public IBinding addBinding(Class<?> clazz);

	public IBinding addBinding(IBinding binding);

	/**
	 * @param requestor
	 * @param objectSupplier
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public void resolveArguments(IRequestor requestor, AbstractObjectSupplier objectSupplier) throws InjectionException;
}
