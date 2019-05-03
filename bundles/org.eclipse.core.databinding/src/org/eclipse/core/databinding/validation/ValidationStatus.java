/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Brad Reynolds - bug 164134
 *******************************************************************************/
package org.eclipse.core.databinding.validation;

import java.util.Objects;

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
	 * Creates a new validation cancel status with the given message.
	 *
	 * @param message
	 * @return a new cancel status with the given message
	 */
	public static IStatus cancel(String message) {
		return new ValidationStatus(IStatus.CANCEL, message);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		String message = getMessage();
		int severity = getSeverity();
		Throwable throwable = getException();

		result = prime * result + Objects.hashCode(message);
		result = prime * result + severity;
		result = prime * result + Objects.hashCode(throwable);
		return result;
	}

	/**
	 * Equality is based upon instance equality rather than identity.
	 *
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ValidationStatus other = (ValidationStatus) obj;

		return getSeverity() == other.getSeverity() && Objects.equals(getMessage(), other.getMessage())
				&& Objects.equals(getException(), other.getException());
	}
}
