/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.validation;

import java.text.NumberFormat;

import org.eclipse.core.databinding.conversion.text.StringToNumberConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.validation.NumberFormatConverter;
import org.eclipse.core.internal.databinding.validation.StringToLongValidator;

/**
 * @since 1.1
 */
public class StringToLongValidatorTest extends StringToNumberValidatorTestHarness {
	@Override
	protected Number getInRangeNumber() {
		return Long.valueOf(1);
	}

	@Override
	protected String getInvalidString() {
		return "1.1";
	}

	@Override
	protected Number getOutOfRangeNumber() {
		return Double.valueOf(Double.MAX_VALUE);
	}

	@Override
	protected NumberFormat setupNumberFormat() {
		return NumberFormat.getIntegerInstance();
	}

	@Override
	protected IValidator<Object> setupValidator(NumberFormat numberFormat) {
		NumberFormatConverter<Object, Integer> converter = StringToNumberConverter.toInteger(numberFormat, false);
		return new StringToLongValidator(converter);
	}
}
