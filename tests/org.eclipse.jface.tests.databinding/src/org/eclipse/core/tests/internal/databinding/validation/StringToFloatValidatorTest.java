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
import org.eclipse.core.internal.databinding.validation.StringToFloatValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToFloatValidatorTest extends
		StringToNumberValidatorTestHarness {

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#getInRangeNumber()
	 */
	protected Number getInRangeNumber() {
		return new Float(1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#getInvalidString()
	 */
	protected String getInvalidString() {
		return "1a";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#getOutOfRangeNumber()
	 */
	protected Number getOutOfRangeNumber() {
		return new Double(Double.MAX_VALUE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#setupNumberFormat()
	 */
	protected NumberFormat setupNumberFormat() {
		return NumberFormat.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.StringToNumberValidatorTestHarness#setupValidator(com.ibm.icu.text.NumberFormat)
	 */
	protected IValidator setupValidator(NumberFormat numberFormat) {
		StringToNumberConverter converter = StringToNumberConverter.toFloat(numberFormat, false);
		return new StringToFloatValidator(converter);
	}
}
