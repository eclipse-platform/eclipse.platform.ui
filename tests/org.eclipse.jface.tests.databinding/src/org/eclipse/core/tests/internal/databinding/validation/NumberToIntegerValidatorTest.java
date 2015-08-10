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

import org.eclipse.core.internal.databinding.conversion.NumberToIntegerConverter;
import org.eclipse.core.internal.databinding.validation.NumberToIntegerValidator;
import org.eclipse.core.internal.databinding.validation.NumberToNumberValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToIntegerValidatorTest extends
		NumberToNumberValidatorTestHarness {

	@Override
	protected Number doGetOutOfRangeNumber() {
		return new Long((long) Integer.MAX_VALUE + 1);
	}

	@Override
	protected NumberToNumberValidator doGetToBoxedTypeValidator(Class fromType) {
		NumberToIntegerConverter converter = new NumberToIntegerConverter(NumberFormat.getInstance(), fromType, false);
		return new NumberToIntegerValidator(converter);
	}

	@Override
	protected NumberToNumberValidator doGetToPrimitiveValidator(Class fromType) {
		NumberToIntegerConverter converter = new NumberToIntegerConverter(NumberFormat.getInstance(), fromType, true);
		return new NumberToIntegerValidator(converter);
	}
}
