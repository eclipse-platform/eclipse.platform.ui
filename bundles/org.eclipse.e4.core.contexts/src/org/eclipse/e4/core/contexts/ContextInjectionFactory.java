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

package org.eclipse.e4.core.contexts;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.e4.core.di.*;
import org.eclipse.e4.core.internal.contexts.ContextObjectSupplier;
import org.eclipse.e4.core.internal.contexts.EclipseContext;

/**
 * An injection factory is used to inject data and services from a context into a domain object. The
 * injection will try to find fields and methods in the user objects that correspond to the names of
 * the services present in the context. Subsequent changes to the context after injection will cause
 * the affected items to be re-injected into the object. Thus the object will remain synchronized
 * with the context once it has been injected.
 * <p>
 * If annotations are supported by the runtime, matching of methods and fields to be injected is
 * also performed using the annotations defined in packages javax.inject and
 * org.eclipse.e4.core.services.annotations.
 * </p>
 * <p>
 * The injection of values is generally done as a number of calls. User objects that want to
 * finalize the injected data (for instance, to perform calculations based on multiple injected
 * values) can place such calculations in a method with one of the following signatures:
 * <ul>
 * <li><code>public void contextSet(IEquinoxContext context);</code></li>
 * <li><code>public void contextSet();</code></li>
 * <li>A method with the <code>org.eclipse.e4.core.services.annotations.PostConstruct</code>
 * annotation.</li>
 * </ul>
 * </p>
 * <p>
 * This method will be called as a last step in the injection process.
 * </p>
 * <p>
 * When injecting values, all fields are injected prior to injection of methods. When values are
 * removed from the context or the context is disposed, injection of null values occurs in the
 * reverse order: methods and then fields. As a result, injection methods can safely make use of
 * injected field values. The order in which methods and fields are injected is undefined, so
 * injection methods should not rely on other injection methods having been run already.
 * </p>
 * <p>
 * When a context is disposed, the injection factory will attempt to notify all injected objects by
 * calling a disposal method. Disposal methods are:
 * <ul>
 * <li>Methods identified by the "@PreDestory" annotations, and</li>
 * <li>Methods implementing {@link IDisposable#dispose()}.</li>
 * </ul>
 * 
 * @noextend This class is not intended to be extended by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
final public class ContextInjectionFactory {

	final private static IInjector injector = InjectorFactory.getInjector();

	private ContextInjectionFactory() {
		// prevents instantiations
	}

	/**
	 * Injects a context into a domain object. See the class comment for details on the injection
	 * algorithm that is used.
	 * 
	 * @param object
	 *            The object to perform injection on
	 * @param context
	 *            The context to obtain injected values from
	 * @return Returns the injected object
	 */
	static public Object inject(Object object, IEclipseContext context) {
		AbstractObjectSupplier supplier = ContextObjectSupplier.getObjectSupplier(context, injector);
		injector.inject(object, supplier);
		return object;
	}

	/**
	 * Call a method, injecting the parameters from the context.
	 * 
	 * @param object
	 *            The object to perform injection on
	 * @param methodName
	 *            The method to call
	 * @param context
	 *            The context to obtain injected values from
	 * @return the return value of the method call, or <code>null</code>
	 * @throws InvocationTargetException
	 *             if an exception was thrown by the invoked method
	 */
	static public Object invoke(Object object, String methodName, IEclipseContext context) throws InvocationTargetException, InjectionException {
		AbstractObjectSupplier supplier = ContextObjectSupplier.getObjectSupplier(context, injector);
		return injector.invoke(object, methodName, supplier);
	}

	/**
	 * Call a method, injecting the parameters from the context.
	 * 
	 * @param object
	 *            The object to perform injection on
	 * @param methodName
	 *            The method to call
	 * @param context
	 *            The context to obtain injected values from
	 * @param defaultValue
	 *            A value to be returned if the method cannot be called
	 * @return the return value of the method call, or <code>null</code>
	 * @throws InvocationTargetException
	 *             if an exception was thrown by the invoked method
	 */
	static public Object invoke(Object object, String methodName, IEclipseContext context, Object defaultValue) throws InvocationTargetException {
		AbstractObjectSupplier supplier = ContextObjectSupplier.getObjectSupplier(context, injector);
		return injector.invoke(object, methodName, defaultValue, supplier);
	}

	/**
	 * Un-injects the context from the object.
	 * 
	 * @param object
	 *            the object previously injected into the context
	 * @param context
	 *            the context previously injected into the object
	 */
	static public void uninject(Object object, IEclipseContext context) {
		((EclipseContext) context).removeListenersTo(object);
	}

	/**
	 * Obtain an instance of the specified class and inject it with the context.
	 * <p>
	 * Class'es scope dictates if a new instance of the class will be created, or existing instance
	 * will be reused.
	 * </p>
	 * 
	 * @param clazz
	 *            the class to be instantiated
	 * @return an instance of the specified class
	 * @throws InstantiationException
	 *             if the class is abstract (or an interface) and can not instantiated
	 * @throws InvocationTargetException
	 *             if invoked constructor generated an exception
	 */
	static public Object make(Class clazz, final IEclipseContext context) throws InvocationTargetException, InstantiationException {
		AbstractObjectSupplier supplier = ContextObjectSupplier.getObjectSupplier(context, injector);
		return injector.make(clazz, supplier);
	}
}
