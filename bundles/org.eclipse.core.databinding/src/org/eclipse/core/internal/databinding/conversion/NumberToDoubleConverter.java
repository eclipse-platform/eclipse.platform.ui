/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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

import java.text.Format;

/**
 * Converts from a Number to a Double.
 * <p>
 * Class is thread safe.
 * </p>
 *
 * @since 1.0
 */
public class NumberToDoubleConverter extends NumberToNumberConverter<Double> {

	/**
	 * @param numberFormat
	 * @param fromType
	 * @param primitive
	 */
	public NumberToDoubleConverter(Format numberFormat, Class<?> fromType, boolean primitive) {
		super(numberFormat, fromType, (primitive) ? Double.TYPE : Double.class);
	}

	@Override
	protected Double doConvert(Number number) {
		if (StringToNumberParser.inDoubleRange(number)) {
			return Double.valueOf(number.doubleValue());
		}

		return null;
	}
}
