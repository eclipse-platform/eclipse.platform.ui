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

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser.ParseResult;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Validates a number that is to be converted by a {@link NumberFormatConverter}.
 * Validation is comprised of parsing the String and range checks.
 * 
 * @since 1.0
 */
public abstract class AbstractStringToNumberValidator implements IValidator {
	private final NumberFormatConverter converter;
	private final boolean toPrimitive;

	private final Number min;
	private final Number max;

	private String outOfRangeMessage;

	/**
	 * Constructs a new instance.
	 * 
	 * @param converter converter and thus formatter to be used in validation
	 * @param min minimum value, used for reporting a range error to the user
	 * @param max maximum value, used for reporting a range error to the user
	 */
	protected AbstractStringToNumberValidator(NumberFormatConverter converter,
			Number min, Number max) {
		this.converter = converter;
		this.min = min;
		this.max = max;

		if (converter.getToType() instanceof Class) {
			Class clazz = (Class) converter.getToType();
			toPrimitive = clazz.isPrimitive();
		} else {
			toPrimitive = false;
		}
	}

	/**
	 * Validates the provided <code>value</code>.  An error status is returned if:
	 * <ul>
	 * <li>The value cannot be parsed.</li>
	 * <li>The value is out of range.</li>
	 * </ul>
	 * 
	 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
	 */
	public final IStatus validate(Object value) {
		ParseResult result = StringToNumberParser.parse(value, converter
				.getNumberFormat(), toPrimitive);

		if (result.getNumber() != null) {
			if (!isInRange(result.getNumber())) {
				if (outOfRangeMessage == null) {
					outOfRangeMessage = StringToNumberParser
							.createOutOfRangeMessage(min, max, converter
									.getNumberFormat());
				}

				return ValidationStatus.error(outOfRangeMessage);
			}
		} else if (result.getPosition() != null) {
			String parseErrorMessage = StringToNumberParser.createParseErrorMessage(
					(String) value, result.getPosition());

			return ValidationStatus.error(parseErrorMessage);
		}

		return Status.OK_STATUS;
	}

	/**
	 * Invoked by {@link #validate(Object)} when the range is to be validated.
	 * 
	 * @param number
	 * @return <code>true</code> if in range
	 */
	protected abstract boolean isInRange(Number number);
}
