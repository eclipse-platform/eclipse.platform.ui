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

import org.eclipse.core.internal.databinding.conversion.NumberToIntegerConverter;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;

/**
 * Validates if a Number can fit in a Integer.
 * <p>
 * Class is thread safe.
 * </p>
 * @since 1.0
 */
public class NumberToIntegerValidator extends NumberToNumberValidator {
	private static final Integer MIN = Integer.valueOf(Integer.MIN_VALUE);
	private static final Integer MAX = Integer.valueOf(Integer.MAX_VALUE);

	public NumberToIntegerValidator(NumberToIntegerConverter converter) {
		super(converter, MIN, MAX);
	}

	@Override
	protected boolean inRange(Number number) {
		return StringToNumberParser.inIntegerRange(number);
	}
}
