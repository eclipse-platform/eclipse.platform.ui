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

package org.eclipse.core.tests.internal.databinding.validation;

import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.validation.StringToIntegerValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToIntegerValidatorTest extends
		StringToNumberValidatorTestHarness {
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#setupNumberFormat()
	 */
	@Override
	protected NumberFormat setupNumberFormat() {
		return NumberFormat.getIntegerInstance();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#setupValidator(com.ibm.icu.text.NumberFormat)
	 */
	@Override
	protected IValidator setupValidator(NumberFormat numberFormat) {
		StringToNumberConverter converter = StringToNumberConverter.toInteger(
				numberFormat, false);
		return new StringToIntegerValidator(converter);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#getInRangeNumber()
	 */
	@Override
	protected Number getInRangeNumber() {
		return new Integer(1);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#getInvalidString()
	 */
	@Override
	protected String getInvalidString() {
		return "1.1";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#getOutOfRangeNumber()
	 */
	@Override
	protected Number getOutOfRangeNumber() {
		return new Double(Double.MAX_VALUE);
	}
}
