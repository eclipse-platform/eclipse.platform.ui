/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.validation;

/**
 * @since 1.0
 *
 */
public class ValidationError {
	
	/**
	 * A constant indicating that something may not be quite right.
	 */
	public static final int WARNING = 1;
	
	/**
	 * A constant indicating that something bad has happened.
	 */
	public static final int ERROR = 2;

	/**
	 * Indicates the current status.
	 */
	public final int status;
	
	/**
	 * Holds the current error or warning message.
	 */
	public final String message;
	
	/**
	 * A convenience factory for {@link #ERROR} ValidationErrors.
	 * 
	 * @param message The error message
	 * @return A new ValidationError representing the error
	 */
	public static ValidationError error(String message) {
		return new ValidationError(ERROR, message);
	}
	
	/**
	 * A convenience factory for {@link #WARNING} ValidationErrors.
	 * 
	 * @param message The warning message
	 * @return A new ValidationError representing the warning
	 */
	public static ValidationError warning(String message) {
		return new ValidationError(WARNING, message);
	}
	
	/**
	 * Construct a ValidationError with a status and error message.
	 * 
	 * @param status either {@link #WARNING} or {@link #ERROR}
	 * @param message An error message string or warning.
	 */
	public ValidationError(int status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public String toString() {
		return message;
	}
}

