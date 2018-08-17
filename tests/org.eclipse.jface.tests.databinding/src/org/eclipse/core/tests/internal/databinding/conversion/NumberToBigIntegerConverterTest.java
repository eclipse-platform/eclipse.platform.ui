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

import java.math.BigInteger;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToBigIntegerConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToBigIntegerConverterTest extends NumberToNumberTestHarness {
	private NumberFormat numberFormat;

	@Override
	protected Number doGetOutOfRangeNumber() {
		return null;
	}

	@Override
	protected IConverter<Object, BigInteger> doGetToBoxedTypeValidator(Class<?> fromType) {
		return new NumberToBigIntegerConverter(numberFormat, fromType);
	}

	@Override
	protected IConverter<Object, Number> doGetToPrimitiveValidator(Class<?> fromType) {
		return null;  //no such thing
	}

	@Override
	protected Class<?> doGetToType(boolean primitive) {
		return (primitive) ? null : BigInteger.class;
	}
}
