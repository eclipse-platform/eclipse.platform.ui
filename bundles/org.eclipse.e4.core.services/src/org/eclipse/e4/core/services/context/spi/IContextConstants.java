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

/**
 * This class contains various constants used by the context API.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IContextConstants {
	/**
	 * Default prefix for the fields and methods to be injected (value "inject__").
	 * <p>
	 * For example, if the context has an object under the name "log", the object will be injected
	 * into the field named "inject__log".
	 * </p>
	 */
	public static final String INJECTION_PREFIX = "inject__"; //$NON-NLS-1$

	/**
	 * The name of the method to be called when a context is injected into the object (value
	 * "contextSet").
	 * <p>
	 * This method will be called after all other injection calls are done. As such it can be used
	 * to perform all calculations based on multiple injected values.
	 * </p>
	 * <p>
	 * For convenience the definition of this method is present in the {@link IEclipseContextAware}
	 * interface.
	 * </p>
	 * 
	 * @see IEclipseContextAware#contextSet(IEclipseContext)
	 */
	public static final String INJECTION_SET_CONTEXT_METHOD = "contextSet"; //$NON-NLS-1$

	/**
	 * A context key (value "debugString") identifying a value to use in debug statements for a
	 * context. A computed value can be used to embed more complex information in debug statements.
	 */
	public static final String DEBUG_STRING = "debugString"; //$NON-NLS-1$

	/**
	 * A context key (value "parentContext") identifying the parent context, which can be retrieved
	 * with {@link IEclipseContext#get(String)}.
	 */
	public static final String PARENT = "parentContext"; //$NON-NLS-1$

	/**
	 * A context key (value "activeChildContext") that identifies another {@link IEclipseContext}
	 * that is a child of the context. The meaning of active is up to the application.
	 */
	public static final String ACTIVE_CHILD = "activeChildContext"; //$NON-NLS-1$

	/**
	 * A context key (value "rootContext") that identifies the root of this context chain. It does
	 * not have to be the global root, but signifies the topmost context for the purposes of
	 * function management and active context chains.
	 */
	public static final String ROOT_CONTEXT = "rootContext"; //$NON-NLS-1$
}
