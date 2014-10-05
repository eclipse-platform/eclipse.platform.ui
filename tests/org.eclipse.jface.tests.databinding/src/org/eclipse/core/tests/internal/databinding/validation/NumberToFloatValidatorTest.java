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

import org.eclipse.core.internal.databinding.conversion.NumberToFloatConverter;
import org.eclipse.core.internal.databinding.validation.NumberToFloatValidator;
import org.eclipse.core.internal.databinding.validation.NumberToNumberValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToFloatValidatorTest extends
		NumberToNumberValidatorTestHarness {

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.NumberToNumberValidatorTestHarness#doGetOutOfRangeNumber()
	 */
	@Override
	protected Number doGetOutOfRangeNumber() {
		return new Double(Double.MAX_VALUE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.NumberToNumberValidatorTestHarness#doGetToBoxedTypeValidator(java.lang.Class)
	 */
	@Override
	protected NumberToNumberValidator doGetToBoxedTypeValidator(Class fromType) {
		NumberToFloatConverter converter = new NumberToFloatConverter(NumberFormat.getInstance(), fromType, false);
		return new NumberToFloatValidator(converter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.NumberToNumberValidatorTestHarness#doGetToPrimitiveValidator(java.lang.Class)
	 */
	@Override
	protected NumberToNumberValidator doGetToPrimitiveValidator(Class fromType) {
		NumberToFloatConverter converter = new NumberToFloatConverter(NumberFormat.getInstance(), fromType, true);
		return new NumberToFloatValidator(converter);
	}
}
