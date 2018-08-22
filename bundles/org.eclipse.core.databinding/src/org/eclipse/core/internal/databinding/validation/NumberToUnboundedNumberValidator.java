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

package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.internal.databinding.conversion.NumberToNumberConverter;

/**
 * Validates if a Number can fit in an unbounded number (e.g. BigInteger, BigDecimal, etc.).
 * <p>
 * Class is thread safe.
 * </p>
 *
 * @since 1.0
 */
public class NumberToUnboundedNumberValidator extends NumberToNumberValidator {
	/**
	 * @param converter
	 */
	public NumberToUnboundedNumberValidator(NumberToNumberConverter<?> converter) {
		super(converter, null, null);
	}

	@Override
	protected boolean inRange(Number number) {
		return true;
	}
}
