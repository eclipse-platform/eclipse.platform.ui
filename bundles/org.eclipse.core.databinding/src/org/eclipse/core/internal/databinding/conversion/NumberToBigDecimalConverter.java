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
 * Converts from a Number to a BigDecimal.
 * <p>
 * Class is thread safe.
 * </p>
 *
 * @since 1.0
 */
public class NumberToBigDecimalConverter extends NumberToNumberConverter<BigDecimal> {
	/**
	 * @param numberFormat
	 * @param fromType
	 */
	public NumberToBigDecimalConverter(Format numberFormat, Class<?> fromType) {
		super(numberFormat, fromType, BigDecimal.class);
	}

	@Override
	protected BigDecimal doConvert(Number number) {
		if (number instanceof BigInteger) {
			return new BigDecimal((BigInteger) number);
		}

		return BigDecimal.valueOf(number.doubleValue());
	}
}
