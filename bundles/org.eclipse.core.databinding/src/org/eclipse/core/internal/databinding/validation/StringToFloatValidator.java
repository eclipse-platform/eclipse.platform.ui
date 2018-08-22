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
 * Validates that a string is of the appropriate format and is in the range of a
 * float.
 *
 * @since 1.0
 */
public class StringToFloatValidator extends AbstractStringToNumberValidator {

	private static final Float MIN = Float.valueOf(-Float.MAX_VALUE);
	private static final Float MAX = Float.valueOf(Float.MAX_VALUE);

	/**
	 * @param converter
	 */
	public StringToFloatValidator(NumberFormatConverter<?, ?> converter) {
		super(converter, MIN, MAX);
	}

	@Override
	protected boolean isInRange(Number number) {
		return StringToNumberParser.inFloatRange(number);
	}
}
