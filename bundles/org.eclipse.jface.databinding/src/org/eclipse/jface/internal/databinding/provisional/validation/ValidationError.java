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
	 * The exception that caused the error (if any).  Note that this
	 * field should not be used for user errors, but only when the program
	 * has detected a failure and needs to capture and retain the 
	 * call stack for the programmer to evaluate later.
	 */
	public final Throwable exception;
	
	/**
	 * A convenience factory for {@link #ERROR} ValidationErrors.
	 * 
	 * @param message The error message
	 * @return A new ValidationError representing the error
	 */
	public static ValidationError error(String message) {
		return new ValidationError(ERROR, message, null);
	}
	
	/**
	 * A convenience factory for {@link #ERROR} ValidationErrors caused
	 * by a program malfunction.
	 * 
	 * @param message The error message
	 * @param exception The exception representing the malfunction
	 * @return A new ValidationError representing the error
	 */
	public static ValidationError error(String message, Throwable exception) {
		return new ValidationError(ERROR, message, exception);
	}
	
	/**
	 * A convenience factory for {@link #WARNING} ValidationErrors.
	 * 
	 * @param message The warning message
	 * @return A new ValidationError representing the warning
	 */
	public static ValidationError warning(String message) {
		return new ValidationError(WARNING, message, null);
	}
	
	/**
	 * Construct a ValidationError with a status and error message.
	 * 
	 * @param status either {@link #WARNING} or {@link #ERROR}
	 * @param message An error message string or warning.
	 * @param exception The exception representing the program's malfunction or null if none
	 */
	public ValidationError(int status, String message, Throwable exception) {
		this.status = status;
		this.message = message;
		this.exception = exception;
	}
	
	public String toString() {
		return message;
	}
}

