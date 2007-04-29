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

package org.eclipse.core.internal.databinding.conversion;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.ibm.icu.text.NumberFormat;

/**
 * Converts from a Number to a BigInteger.
 * <p>
 * Class is thread safe.
 * </p>
 * 
 * @since 1.0
 */
public class NumberToBigIntegerConverter extends NumberToNumberConverter {
	/**
	 * @param numberFormat
	 * @param fromType
	 */
	public NumberToBigIntegerConverter(NumberFormat numberFormat, Class fromType) {
		super(numberFormat, fromType, BigInteger.class);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.databinding.conversion.NumberToNumberConverter#doConvert(java.lang.Number)
	 */
	protected Number doConvert(Number number) {	
		return toBigDecimal(number).toBigInteger();
	}
	
	private static BigDecimal toBigDecimal(Number number) {
		if (number instanceof BigDecimal) {
			return (BigDecimal) number;
		}
		
		return new BigDecimal(number.doubleValue());
	}
}
