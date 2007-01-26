/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164134
 ******************************************************************************/
package org.eclipse.core.databinding.validation;

import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Convenience class for creating status objects.
 * 
 * @since 3.3
 * 
 */
public class ValidationStatus extends Status {

	/**
	 * Creates a new validation status with the given severity, message, and
	 * exception.
	 * 
	 * @param severity
	 * @param message
	 * @param exception
	 */
	private ValidationStatus(int severity, String message, Throwable exception) {
		super(severity, Policy.JFACE_DATABINDING, IStatus.OK, message, exception);
	}

	/**
	 * Creates a new validation status with the given severity and message.
	 * 
	 * @param severity
	 * @param message
	 */
	private ValidationStatus(int severity, String message) {
		super(severity, Policy.JFACE_DATABINDING,IStatus.OK, message, null);
	}

	/**
	 * Creates a new validation error status with the given message.
	 * 
	 * @param message
	 * @return a new error status with the given message
	 */
	public static IStatus error(String message) {
		return new ValidationStatus(IStatus.ERROR, message);
	}

	/**
	 * Creates a new validation error status with the given message and
	 * exception.
	 * 
	 * @param message
	 * @param exception
	 * @return a new error status with the given message and exception
	 */
	public static IStatus error(String message, Throwable exception) {
		return new ValidationStatus(IStatus.ERROR, message, exception);
	}

	/**
	 * Creates a new validation warning status with the given message.
	 * 
	 * @param message
	 * @return a new warning status with the given message
	 */
	public static IStatus warning(String message) {
		return new ValidationStatus(IStatus.WARNING, message);
	}
	
	/**
	 * Creates a new validation info status with the given message.
	 * 
	 * @param message
	 * @return a new info status with the given message
	 */
	public static IStatus info(String message) {
		return new ValidationStatus(IStatus.INFO, message);
	}
	
	/**
	 * Returns an OK status.
	 * 
	 * @return an ok status
	 */
	public static IStatus ok() {
		return Status.OK_STATUS;
	}
	
}
