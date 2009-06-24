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

import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IEclipseContextAware;
import org.eclipse.e4.core.services.internal.context.ContextInjectionImpl;

/**
 * An injection factory is used to inject data and services from a context into a domain object. The
 * injection will try to find fields and methods in the user objects that correspond to the names of
 * the services present in the context. Subsequent changes to the context after injection will cause
 * the affected items to be re-injected into the object. Thus the object will remain synchronized
 * with the context once it has been injected.
 * <p>
 * The matching is done using {@link IContextConstants#INJECTION_FIELD_PREFIX} for fields,
 * {@link IContextConstants#INJECTION_SET_METHOD_PREFIX} for methods. For a context key called
 * "log", injection will attempt to find field "di_Log" or method "setLog()" that will accept the
 * associated service. The field's prefix can be overridden by the context. For field injection,
 * matching is also performed on the field type. A field of type {@link String} will be injected
 * with a context value stored under key "java.lang.String".
 * </p>
 * <p>
 * Generally speaking, name matching is case-sensitive. However, for convenience, when matching
 * service names to fields or methods:
 * <ul>
 * <li>Capitalization of the first character of the service name is ignored. For instance, the "log"
 * name will match both "di_Log" and "di_log" fields.</li>
 * <li>Dashes in the names ("-") are removed, and the next character is capitalized. For instance,
 * "log-general" will match "di_LogGeneral"</li>
 * </ul>
 * </p>
 * <p>
 * If annotations are supported by the runtime, matching of methods and fields to be injected is
 * also performed using the annotations defined in package org.eclipse.e4.core.services.annotations.
 * </p>
 * <p>
 * The injection of values is generally done as a number of calls. User objects that want to
 * finalize the injected data (for instance, to perform calculations based on multiple injected
 * values) can place such calculations in a method with one of the following signatures:
 * <ul>
 * <li><code>public void contextSet(IEquinoxContext context);</code></li>
 * <li><code>public void contextSet();</code></li>
 * <li>Any zero-argument method with the
 * <code>org.eclipse.e4.core.services.annotations.PostConstruct</code> annotation</li>
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
 * calling a disposal method. At most one disposal method will be invoked, according to the
 * following lookup order:
 * <ul>
 * <li>A zero-argument method on the injected object with the
 * <code>org.eclipse.e4.core.services.annotations.PreDestroy</code> annotation</li>
 * <li>The method with signature void contextDisposed(IEclipseContext context);</li>
 * <li>The method with signature void contextDisposed();</li>
 * <li>The method with signature void dispose();</li>
 * <li>The same lookup is then repeated on the superclass</li>
 * </ul>
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 0.9
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
		return inject(object, context, null, null);
	}

	/**
	 * Injects a context into a domain object. See the class comment for details on the injection
	 * algorithm that is used.
	 * 
	 * @param object
	 *            The object to perform injection on
	 * @param context
	 *            The context to obtain injected values from
	 * @param fieldPrefix
	 *            The prefix used to identify injected fields
	 * @param setMethodPrefix
	 *            The prefix used to identify setter injection methods
	 * @return Returns the injected object
	 */
	static public Object inject(Object object, IEclipseContext context, String fieldPrefix,
			String setMethodPrefix) {
		ContextInjectionImpl injector = new ContextInjectionImpl(fieldPrefix, setMethodPrefix);
		injector.injectInto(object, context);
		return object;
	}
}