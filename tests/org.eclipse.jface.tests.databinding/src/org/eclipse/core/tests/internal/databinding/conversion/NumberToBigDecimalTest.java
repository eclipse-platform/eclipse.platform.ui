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

package org.eclipse.core.tests.internal.databinding.conversion;

import java.math.BigDecimal;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToBigDecimalTest extends NumberToNumberTestHarness {

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.NumberToNumberTestHarness#doGetOutOfRangeNumber()
	 */
	@Override
	protected Number doGetOutOfRangeNumber() {
		return null; //does not exist
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.NumberToNumberTestHarness#doGetToBoxedTypeValidator(java.lang.Class)
	 */
	@Override
	protected IConverter doGetToBoxedTypeValidator(Class fromType) {
		return new NumberToBigDecimalConverter(NumberFormat.getInstance(), fromType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.NumberToNumberTestHarness#doGetToPrimitiveValidator(java.lang.Class)
	 */
	@Override
	protected IConverter doGetToPrimitiveValidator(Class fromType) {
		return null; // does not exist
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.NumberToNumberTestHarness#doGetToType(boolean)
	 */
	@Override
	protected Class doGetToType(boolean primitive) {
		return (primitive) ? null : BigDecimal.class;
	}
}
