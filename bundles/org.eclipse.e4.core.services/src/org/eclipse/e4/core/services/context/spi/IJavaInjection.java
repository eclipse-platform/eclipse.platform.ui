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

// XXX add conversion of dashes
/**
 * This interface represents a hierarchical context with dependency injection capabilities. 
 * The context might have number of objects associated with it. For instance, a context might 
 * include log, status, and preferences. We'll call such associated objects "services" 
 * (through they don't have to be OSGi services).
 * <p>
 * Objects are associated with the context using names. For instance, a logging
 * service can have a name "log" and the code to add it could look like:
 * <pre>
 * 	context.addObject("log", loggingService);
 * </pre> 
 * </p><p> 
 * The context can be injected into an object. During injection context attempts to find
 * fields and methods in the user objects that correspond to the names of associated 
 * services.
 * <pre>
 * 	context.injectInto(myObject);
 * </pre> 
 * </p><p> 
 * The matching is done using {@link #FIELD_PREFIX} for fields, {@link #SET_METHOD_PREFIX}
 * for methods. For the "log" example, injection will attempt to find field "equinoxLog" or method
 * "setLog()" that could accept associated service. (The field's prefix can be overridden
 * by the context.)
 * </p><p>
 * Multiple objects can be added under a single name. In this case the setter method will be called
 * multiple times, ones for each associated service. 
 * </p><p>
 * The contexts can be created as in a hierarchical fashion with child contexts inheriting associated
 * services from their parents and allowing services to be overridden for children.
 * </p><p>
 * Generally speaking, name matching is case-sensitive. However, for convenience, when matching 
 * service names to fields or methods:
 * <ul>
 * <li>Capitalization of the first character of the service name is ignored. For instance, the "log" name 
 * will match both "equinoxLog" and "equinoxlog" fields.</li>
 * <li>Dashes in the names ("-") are removed, and the next character is capitalized. 
 * For instance, "log-general" will match "equinoxLogGeneral"</li> 
 * </ul>   
 * </p><p>
 * The injection of values is generally done as a number of calls. User objects that want to have 
 * an ability to finalize the injected data (for instance, to perform calculations based on multiple
 * injected values) might want to place such calculations into a method 
 * <code>public void equinoxContextSet(IEquinoxContext context) {...}</code>.
 * </p><p>
 * This method will be called as a last step in the injection process. For convenience, the signature
 * of this method can be found in the {@link IEclipseContextAware} interface. (User objects don't have to 
 * implement this interface for the method to be called, but might find it convenient to have
 * the method's signature.)
 * </p>
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.5
 */
public interface IJavaInjection {

	/**
	 * Default prefix for the fields to be injected. 
	 * <p>
	 * For example, if the context has an object under the name "log", the object will be
	 * injected into the field named "diLog".
	 * </p> 
	 */
	public String FIELD_PREFIX = "di"; //$NON-NLS-1$

	/**
	 * Default prefix for the methods to be injected. 
	 * <p>
	 * For example, if the context has an object under the name "log", the object will be
	 * injected into the method named "setLog".
	 * </p> 
	 */
	public String SET_METHOD_PREFIX = "set"; //$NON-NLS-1$

	/**
	 * Default prefix for the methods to be called when an object is removed from the context. 
	 * <p>
	 * For example, if the object named "log" was removed from the context,
	 * the method "removeLog" will be called.
	 * </p> 
	 */
	public String REMOVE_METHOD_PREFIX = "remove"; //$NON-NLS-1$

	/**
	 * The name of the method to be called when a context is injected into the object.
	 * <p>
	 * This method will be called after all other injection calls are done. As such it can be
	 * used to perform all calculations based on multiple injected values.
	 * </p><p>
	 * For convenience the definition of this method is present in the {@link IEclipseContextAware} interface.
	 * </p>
	 * @see IEclipseContextAware#contextSet(IEclipseContext)
	 */
	static public final String CONTEXT_SET_METHOD = "contextSet"; //$NON-NLS-1$

	/**
	 * The name of the method to be called when a context previously injected in the object
	 * has been disposed.
	 * <p>
	 * For convenience the definition of this method is present in the {@link IEclipseContextAware} interface.
	 * </p>
	 * @see IEclipseContextAware#contextDisposed(IEclipseContext)
	 */
	static public final String CONTEXT_DISPOSED_METHOD = "contextDisposed"; //$NON-NLS-1$
}
