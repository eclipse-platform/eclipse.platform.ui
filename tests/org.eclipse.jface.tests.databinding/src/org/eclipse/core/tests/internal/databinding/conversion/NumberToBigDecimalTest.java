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

package org.eclipse.core.tests.internal.databinding.conversion;

import java.math.BigDecimal;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToBigDecimalConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToBigDecimalTest extends NumberToNumberTestHarness {

	@Override
	protected Number doGetOutOfRangeNumber() {
		return null; //does not exist
	}

	@Override
	protected IConverter<Object, BigDecimal> doGetToBoxedTypeValidator(Class<?> fromType) {
		return new NumberToBigDecimalConverter(NumberFormat.getInstance(), fromType);
	}

	@Override
	protected IConverter<Object, Number> doGetToPrimitiveValidator(Class<?> fromType) {
		return null; // does not exist
	}

	@Override
	protected Class<?> doGetToType(boolean primitive) {
		return (primitive) ? null : BigDecimal.class;
	}
}
