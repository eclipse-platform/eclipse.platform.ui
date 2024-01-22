/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.activities;

/**
 * Signals that an attempt was made to access the properties of an undefined
 * object.
 *
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 *
 * @see IActivity
 * @see ICategory
 * @since 3.0
 */
public final class NotDefinedException extends Exception {

	/**
	 * Generated serial version UID for this class.
	 *
	 * @since 3.1
	 */
	private static final long serialVersionUID = 4050201929596811061L;

	/**
	 * Creates a new instance of this class with no specified detail message.
	 */
	public NotDefinedException() {
		// no-op
	}

	/**
	 * Creates a new instance of this class with the specified detail message.
	 *
	 * @param s the detail message.
	 */
	public NotDefinedException(String s) {
		super(s);
	}
}
