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
import javax.inject.Scope;
import javax.inject.Singleton;
import org.eclipse.e4.core.di.annotations.PostConstruct;
import org.eclipse.e4.core.di.annotations.PreDestroy;
import org.eclipse.e4.core.di.suppliers.PrimaryObjectSupplier;

/**
 * An injector is used to inject data from the object supplier into a domain object. The
 * injection will try to find fields and methods in the user objects that correspond to the names of
 * the services present in the context. Subsequent changes to the context after injection will cause
 * the affected items to be re-injected into the object. Thus the object will remain synchronized
 * with the context once it has been injected.
 * <p>
 * Matching of methods and fields to be injected is performed using the annotations defined in 
 * packages javax.inject and org.eclipse.e4.core.services.annotations.
 * </p>
 * <p>
 * The injection of values is generally done as a number of calls. User objects that want to
 * finalize the injected data (for instance, to perform calculations based on multiple injected
 * values) can place such calculations in a method with the {@link PostConstruct} annotation.
 * </p>
 * <p>
 * When injecting values, all fields are injected prior to injection of methods. When values are
 * removed from the context or the context is disposed, injection of null values occurs in the
 * reverse order: methods and then fields. As a result, injection methods can safely make use of
 * injected field values. The order in which methods are injected is undefined, so
 * injection methods should not rely on other injection methods having been run already. Methods
 * and field on superclasses are injected before methods in fields on the subclasses. 
 * </p>
 * <p>
 * When supplier is disposed, the injector will attempt to notify all injected objects by
 * calling methods with the {@link PreDestroy} annotation before resetting injected values 
 * to <code>null</code>.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IInjector {

	/**
	 * Methods may return this to indicate that the requested object was not found
	 * wherever <code>null</code> can be a valid return value. 
	 */
	final public static Object NOT_A_VALUE = new Object();

	/**
	 * Injects data from the supplier into a domain object. See the class comment for details on 
	 * the injection algorithm that is used.
	 * @param object the object to perform injection on
	 * @param objectSupplier primary object supplier for the injection
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public void inject(Object object, PrimaryObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * Un-injects the supplier from the object.
	 * @param object the domain object previously injected with the supplier's data
	 * @param objectSupplier primary object supplier for the injection
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public void uninject(Object object, PrimaryObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * Call the annotated method on an object, injecting the parameters from the supplier.
	 * @param object the object on which the method should be called
	 * @param qualifier the annotation tagging method to be called
	 * @param objectSupplier primary object supplier
	 * @return the return value of the method call, might be <code>null</code>
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public Object invoke(Object object, Class<? extends Annotation> qualifier, PrimaryObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * Call the annotated method on an object, injecting the parameters from the supplier.
	 * @param object the object on which the method should be called
	 * @param qualifier the annotation tagging method to be called
	 * @param defaultValue a value to be returned if the method cannot be called, might be <code>null</code>
	 * @param objectSupplier primary object supplier
	 * @return the return value of the method call, might be <code>null</code>
	 * @throws InjectionException if an exception occurred while performing this operation
	 */
	public Object invoke(Object object, Class<? extends Annotation> qualifier, Object defaultValue, PrimaryObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * Obtain an instance of the specified class and inject it with the data from the supplier.
	 * @param <T> the type of the object to be created
	 * @param clazz the class to be instantiated
	 * @param objectSupplier primary object supplier for the injection
	 * @return an instance of the specified class
	 * @throws InjectionException if an exception occurred while performing this operation
	 * @see Scope
	 * @see Singleton
	 */
	public <T> T make(Class<T> clazz, PrimaryObjectSupplier objectSupplier) throws InjectionException;

	/**
	 * Creates a binding for the specified class and adds it to the injector. 
	 * @param clazz the class that the injector should be aware of
	 * @return binding for the specified class
	 */
	public IBinding addBinding(Class<?> clazz);

	/**
	 * Adds binding to the injector.
	 * @param binding the binding to add to the injector
	 * @return the binding added to the injector
	 */
	public IBinding addBinding(IBinding binding);

}
