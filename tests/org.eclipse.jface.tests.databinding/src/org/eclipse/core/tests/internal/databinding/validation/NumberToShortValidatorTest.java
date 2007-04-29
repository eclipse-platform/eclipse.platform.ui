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

import org.eclipse.core.internal.databinding.conversion.NumberToShortConverter;
import org.eclipse.core.internal.databinding.validation.NumberToNumberValidator;
import org.eclipse.core.internal.databinding.validation.NumberToShortValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToShortValidatorTest extends NumberToNumberValidatorTestHarness {
	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.NumberToNumberValidatorTestHarness#doGetOutOfRangeNumber()
	 */
	protected Number doGetOutOfRangeNumber() {
		return new Integer(Short.MAX_VALUE + 1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.NumberToNumberValidatorTestHarness#doGetToBoxedTypeValidator(java.lang.Class)
	 */
	protected NumberToNumberValidator doGetToBoxedTypeValidator(Class fromType) {
		NumberToShortConverter converter = new NumberToShortConverter(NumberFormat.getInstance(),
				Integer.class, false);
		return new NumberToShortValidator(converter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.validation.NumberToNumberValidatorTestHarness#doGetToPrimitiveValidator(java.lang.Class)
	 */
	protected NumberToNumberValidator doGetToPrimitiveValidator(Class fromType) {
		NumberToShortConverter converter = new NumberToShortConverter(NumberFormat.getInstance(),
				Integer.class, true);
		return new NumberToShortValidator(converter);
	}
}
