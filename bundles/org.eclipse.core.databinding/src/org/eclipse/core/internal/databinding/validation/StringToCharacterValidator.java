/*******************************************************************************
 * Copyright (c) 2007, 2008 Matt Carter and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matt Carter - initial API and implementation
 *     Tom Schindl<tom.schindl@bestsolution.at> - bugfix for 217940
 ******************************************************************************/

package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.internal.databinding.conversion.StringToCharacterConverter;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Validates a String to Character conversion.
 */
public class StringToCharacterValidator implements IValidator {

	private final StringToCharacterConverter converter;

	/**
	 * @param converter
	 */
	public StringToCharacterValidator(StringToCharacterConverter converter) {
		this.converter = converter;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
	 */
	public IStatus validate(Object value) {
		try {
			converter.convert(value);
		} catch (IllegalArgumentException e) {
			// The StringToCharacterConverter throws an IllegalArgumentException
			// if it cannot convert.
			return ValidationStatus.error(BindingMessages
					.getString(BindingMessages.VALIDATE_CHARACTER_HELP));
		}
		return Status.OK_STATUS;
	}

}
