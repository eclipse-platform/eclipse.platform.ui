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
package org.eclipse.jface.resource;

/**
 * An exception indicating that a string value could not be
 * converted into the requested data type.
 *
 * @see StringConverter
 */
public class DataFormatException extends IllegalArgumentException {

	/**
	 * Generated serial version UID for this class.
	 * @since 3.1
	 */
	private static final long serialVersionUID = 3544955467404031538L;

	/**
	 * Creates a new exception.
	 */
	public DataFormatException() {
		super();
	}

	/**
	 * Creates a new exception.
	 *
	 * @param message the message
	 */
	public DataFormatException(String message) {
		super(message);
	}
}
