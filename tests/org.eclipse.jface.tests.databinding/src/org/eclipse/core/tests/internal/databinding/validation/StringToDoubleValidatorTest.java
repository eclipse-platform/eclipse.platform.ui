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

import java.math.BigDecimal;

import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.validation.StringToDoubleValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToDoubleValidatorTest extends
		StringToNumberValidatorTestHarness {

	@Override
	protected Number getInRangeNumber() {
		return new Double(1);
	}

	@Override
	protected String getInvalidString() {
		return "1a";
	}

	@Override
	protected Number getOutOfRangeNumber() {
		BigDecimal decimal = BigDecimal.valueOf(Double.MAX_VALUE);
		return decimal.add(BigDecimal.valueOf(Double.MAX_VALUE));
	}

	@Override
	protected NumberFormat setupNumberFormat() {
		return NumberFormat.getInstance();
	}

	@Override
	protected IValidator<Object> setupValidator(NumberFormat numberFormat) {
		StringToNumberConverter<Double> converter = StringToNumberConverter.toDouble(numberFormat, false);
		return new StringToDoubleValidator(converter);
	}
}
