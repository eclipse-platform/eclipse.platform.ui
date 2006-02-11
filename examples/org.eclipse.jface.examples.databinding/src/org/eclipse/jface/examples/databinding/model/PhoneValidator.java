/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;

import org.eclipse.jface.internal.databinding.api.validation.IValidator;
import org.eclipse.jface.internal.databinding.api.validation.ValidationError;

public class PhoneValidator implements IValidator {

	public ValidationError isPartiallyValid(Object value) {
		return isValid(value);
	}

	public ValidationError isValid(Object value) {
		String rawPhoneNumber = PhoneConverter.removeFormatting((String)value);
		if(rawPhoneNumber.length() != 10){
			return ValidationError.error("Phone number must be 10 characters");
		} else {
			return null;
		}
	}

}
