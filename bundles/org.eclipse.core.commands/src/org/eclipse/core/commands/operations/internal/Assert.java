/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands.operations.internal;

/**
 * Used to sanity check runtime conditions. Assertion failures typically occur
 * when something is being misused, such as calling a method with illegal
 * arguments. Runtime exceptions are thrown. Once an assertion failure occurs,
 * further behavior is unspecified.
 * 
 * @since 3.1
 */
public final class Assert {

	/**
	 * Asserts that the given object is not <code>null</code>. If this is not
	 * the case, some kind of unchecked exception is thrown.
	 * 
	 * @param object
	 *            the value to test
	 * @exception IllegalArgumentException
	 *                if the object is <code>null</code>
	 */
	public static void isNotNull(Object object) {
		isNotNull(object, ""); //$NON-NLS-1$
	}

	/**
	 * Asserts that the given object is not <code>null</code>. If this is not
	 * the case, some kind of unchecked exception is thrown. The given message
	 * is included in that exception, to aid debugging.
	 * 
	 * @param object
	 *            the value to test
	 * @param message
	 *            the message to include in the exception
	 * @exception IllegalArgumentException
	 *                if the object is <code>null</code>
	 */
	public static void isNotNull(Object object, String message) {
		if (object == null)
			throw new IllegalArgumentException("null argument:" + message); //$NON-NLS-1$
	}

	private Assert() {
		// This class should not be instantiated.
	}
}
