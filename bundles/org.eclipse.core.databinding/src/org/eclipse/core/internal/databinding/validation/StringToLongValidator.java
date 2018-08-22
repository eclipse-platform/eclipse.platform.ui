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

package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;

/**
 * Validates that a string is of the appropriate format and is in the range of
 * an long.
 *
 * @since 1.0
 */
public class StringToLongValidator extends AbstractStringToNumberValidator {
	private static final Long MIN = Long.valueOf(Long.MIN_VALUE);
	private static final Long MAX = Long.valueOf(Long.MAX_VALUE);

	/**
	 * @param converter
	 */
	public StringToLongValidator(NumberFormatConverter<?, ?> converter) {
		super(converter, MIN, MAX);
	}

	@Override
	protected boolean isInRange(Number number) {
		return StringToNumberParser.inLongRange(number);
	}
}
