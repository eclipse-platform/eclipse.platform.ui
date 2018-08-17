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

import org.eclipse.core.internal.databinding.conversion.NumberToShortConverter;
import org.eclipse.core.internal.databinding.validation.NumberToNumberValidator;
import org.eclipse.core.internal.databinding.validation.NumberToShortValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToShortValidatorTest extends NumberToNumberValidatorTestHarness {
	@Override
	protected Number doGetOutOfRangeNumber() {
		return Integer.valueOf(Short.MAX_VALUE + 1);
	}

	@Override
	protected NumberToNumberValidator doGetToBoxedTypeValidator(Class<?> fromType) {
		NumberToShortConverter converter = new NumberToShortConverter(NumberFormat.getInstance(),
				Integer.class, false);
		return new NumberToShortValidator(converter);
	}

	@Override
	protected NumberToNumberValidator doGetToPrimitiveValidator(Class<?> fromType) {
		NumberToShortConverter converter = new NumberToShortConverter(NumberFormat.getInstance(),
				Integer.class, true);
		return new NumberToShortValidator(converter);
	}
}
