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

package org.eclipse.e4.core.services.context.spi;

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IEclipseContextAware;
import org.eclipse.e4.core.services.internal.context.ContextInjectionImpl;
import org.eclipse.e4.core.services.internal.context.EclipseContext;

/**
 * An injection factory is used to inject data and services from a context into a domain object. The
 * injection will try to find fields and methods in the user objects that correspond to the names of
 * the services present in the context. Subsequent changes to the context after injection will cause
 * the affected items to be re-injected into the object. Thus the object will remain synchronized
 * with the context once it has been injected.
 * <p>
 * The matching is done using {@link IContextConstants#INJECTION_PREFIX} for fields and methods. For
 * a context key called "Log", injection will attempt to find field "inject_Log" or method
 * "inject__method_name(Log log)" that will accept the associated service. Name matching is
 * case-sensitive.
 * </p>
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
 * This method will be called as a last step in the injection process. For convenience, the
 * signature of <code>contextSet</code> can be found in the {@link IEclipseContextAware} interface.
 * User objects don't have to implement this interface for the method to be called, but might find
 * it convenient to have the method's signature.
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
		ContextInjectionImpl injector = new ContextInjectionImpl();
		injector.injectInto(object, context);
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
	 * @param defaultValue
	 *            A value to be returned if the method cannot be called
	 * @return the return value of the method call, or <code>null</code>
	 */
	static public Object invoke(Object object, String methodName, IEclipseContext context,
			Object defaultValue) {
		return ContextInjectionImpl.invoke(object, methodName, context, defaultValue);
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
		if (context instanceof EclipseContext)
			((EclipseContext) context).uninject(object);
	}
}
