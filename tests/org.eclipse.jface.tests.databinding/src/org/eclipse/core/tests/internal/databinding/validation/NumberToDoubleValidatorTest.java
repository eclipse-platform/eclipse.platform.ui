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

import java.math.BigDecimal;

import org.eclipse.core.internal.databinding.conversion.NumberToDoubleConverter;
import org.eclipse.core.internal.databinding.validation.NumberToDoubleValidator;
import org.eclipse.core.internal.databinding.validation.NumberToNumberValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToDoubleValidatorTest extends
		NumberToNumberValidatorTestHarness {

	@Override
	protected Number doGetOutOfRangeNumber() {
		return new BigDecimal(Double.MAX_VALUE).add(new BigDecimal(Double.MAX_VALUE));
	}

	@Override
	protected NumberToNumberValidator doGetToBoxedTypeValidator(Class fromType) {
		NumberToDoubleConverter converter = new NumberToDoubleConverter(NumberFormat.getInstance(), fromType, false);
		return new NumberToDoubleValidator(converter);
	}

	@Override
	protected NumberToNumberValidator doGetToPrimitiveValidator(Class fromType) {
		NumberToDoubleConverter converter = new NumberToDoubleConverter(NumberFormat.getInstance(), fromType, true);
		return new NumberToDoubleValidator(converter);
	}
}
