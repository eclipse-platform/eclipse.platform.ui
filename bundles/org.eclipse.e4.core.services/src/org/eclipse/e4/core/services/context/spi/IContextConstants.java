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
	 * Default prefix for the fields to be injected. 
	 * <p>
	 * For example, if the context has an object under the name "log", the object will be
	 * injected into the field named "diLog".
	 * </p> 
	 */
	public static final String INJECTION_FIELD_PREFIX = "di"; //$NON-NLS-1$

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
	public static final String INJECTION_SET_CONTEXT_METHOD = "contextSet"; //$NON-NLS-1$

	/**
	 * Default prefix for the methods to be injected. 
	 * <p>
	 * For example, if the context has an object under the name "log", the object will be
	 * injected into the method named "setLog".
	 * </p> 
	 */
	public static final String INJECTION_SET_METHOD_PREFIX = "set"; //$NON-NLS-1$

	/**
	 * A context key identifying a value to use in debug statements
	 * for a context. A computed value can be used to embed more complex
	 * information in debug statements.
	 */
	public static final String DEBUG_STRING = "DEBUG_STRING"; //$NON-NLS-1$

}
