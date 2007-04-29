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
import org.eclipse.core.internal.databinding.conversion.NumberToNumberConverter;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Base class for validators that validate if a Number can fit in another Number type.
 * <p>
 * Class is thread safe.
 * </p>
 * 
 * @since 1.0
 */
public abstract class NumberToNumberValidator implements IValidator {
	private final NumberToNumberConverter converter;

	private final Number min;

	private final Number max;

	private String outOfRangeMessage;

	private final boolean primitive;

	/**
	 * @param converter
	 * @param min
	 *            can be <code>null</code>
	 * @param max
	 *            can be <code>null</code>
	 */
	protected NumberToNumberValidator(NumberToNumberConverter converter,
			Number min, Number max) {
		this.converter = converter;
		this.min = min;
		this.max = max;

		primitive = ((Class) converter.getToType()).isPrimitive();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
	 */
	public final IStatus validate(Object value) {
		if (value == null) {
			if (primitive) {
				throw new IllegalArgumentException(
						"Parameter 'value' cannot be null."); //$NON-NLS-1$
			}

			return Status.OK_STATUS;
		}

		if (!(value instanceof Number)) {
			throw new IllegalArgumentException(
					"Parameter 'value' is not of type Number."); //$NON-NLS-1$
		}

		Number number = (Number) value;
		if (inRange(number)) {
			return Status.OK_STATUS;
		}

		synchronized (this) {
			if (outOfRangeMessage == null && min != null && max != null) {
				outOfRangeMessage = StringToNumberParser
						.createOutOfRangeMessage(min, max, converter
								.getNumberFormat());
			}

			return ValidationStatus.error(outOfRangeMessage);
		}
	}

	/**
	 * Invoked to determine if the value is in range.
	 * 
	 * @param number
	 * @return <code>true</code> if in range
	 */
	protected abstract boolean inRange(Number number);
}
