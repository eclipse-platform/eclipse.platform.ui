/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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

package org.eclipse.core.internal.databinding.conversion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;

/**
 * Converts from a Number to a BigInteger.
 * <p>
 * Class is thread safe.
 * </p>
 *
 * @since 1.0
 */
public class NumberToBigIntegerConverter extends NumberToNumberConverter<BigInteger> {
	/**
	 * @param numberFormat
	 * @param fromType
	 */
	public NumberToBigIntegerConverter(Format numberFormat, Class<?> fromType) {
		super(numberFormat, fromType, BigInteger.class);
	}

	@Override
	protected BigInteger doConvert(Number number) {
		return toBigDecimal(number).toBigInteger();
	}

	private static BigDecimal toBigDecimal(Number number) {
		if (number instanceof BigDecimal) {
			return (BigDecimal) number;
		}

		return BigDecimal.valueOf(number.doubleValue());
	}
}
