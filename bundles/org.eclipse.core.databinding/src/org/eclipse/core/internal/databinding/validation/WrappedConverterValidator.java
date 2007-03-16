/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Validator that invokes the converter and if it fails will return an error status with a message.
 * 
 * @since 1.1
 */
public abstract class WrappedConverterValidator implements IValidator {
	private final IConverter converter;
	
	/**
	 * @param converter
	 */
	public WrappedConverterValidator(IConverter converter) {
		this.converter = converter;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
	 */
	public IStatus validate(Object value) {
		try {
			converter.convert(value);
			return Status.OK_STATUS;
		} catch (Exception e) {
			return ValidationStatus.error(getErrorMessage(), e);
		}
	}
	
	/**
	 * Error message to return when validation fails.
	 * 
	 * @return error message
	 */
	protected abstract String getErrorMessage();
}
