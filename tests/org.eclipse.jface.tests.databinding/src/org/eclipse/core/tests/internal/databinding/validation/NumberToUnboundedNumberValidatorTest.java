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

import org.eclipse.core.internal.databinding.conversion.NumberToBigIntegerConverter;
import org.eclipse.core.internal.databinding.validation.NumberToNumberValidator;
import org.eclipse.core.internal.databinding.validation.NumberToUnboundedNumberValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToUnboundedNumberValidatorTest extends
		NumberToNumberValidatorTestHarness {

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.NumberToNumberValidatorTestHarness#doGetOutOfRangeNumber()
	 */
	@Override
	protected Number doGetOutOfRangeNumber() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.NumberToNumberValidatorTestHarness#doGetToBoxedTypeValidator(java.lang.Class)
	 */
	@Override
	protected NumberToNumberValidator doGetToBoxedTypeValidator(Class fromType) {
		NumberToBigIntegerConverter converter = new NumberToBigIntegerConverter(NumberFormat.getInstance(), fromType);
		return new NumberToUnboundedNumberValidator(converter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.NumberToNumberValidatorTestHarness#doGetToPrimitiveValidator(java.lang.Class)
	 */
	@Override
	protected NumberToNumberValidator doGetToPrimitiveValidator(Class fromType) {
		return null;  // primitive BigInteger does not exist
	}
}
