/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime;

import java.text.MessageFormat;
import org.eclipse.core.internal.runtime.InternalPlatform;

/**
 * Common superclass for all message bundle classes.  Provides convenience
 * methods for manipulating messages.
 * 
 * @since 3.1
 */
public abstract class NLS {
	public NLS() {
		super();
	}

	/**
	 * Bind the given message's substitution locations with the given string values.
	 */
	public static String bind(String message, Object binding) {
		return bind(message, new Object[] {binding});
	}

	/**
	 * Bind the given message's substitution locations with the given string values.
	 */
	public static String bind(String message, Object binding1, Object binding2) {
		return bind(message, new Object[] {binding1, binding2});
	}

	/**
	 * Bind the given message's substitution locations with the given string values.
	 */
	public static String bind(String message, Object[] bindings) {
		if (message == null)
			return "No message available"; //$NON-NLS-1$
		if (bindings == null)
			return message;
		return MessageFormat.format(message, bindings);
	}

	/**
	 * Initialize the given class with the values from the specified message bundle.
	 * <p>
	 * Note this is interim API and may change before the 3.1 release.
	 * </p>
	 * 
	 * @param bundleName fully qualified path of the class name
	 * @param clazz the class where the constants will exist
	 */
	public static void initializeMessages(String bundleName, Class clazz) {
		InternalPlatform.getDefault().initializeMessages(bundleName, clazz);
	}
}