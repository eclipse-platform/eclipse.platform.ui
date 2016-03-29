/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;


/**
 * Validates that a string is of the appropriate format and is in the range of
 * an integer.
 *
 * @since 1.0
 */
public class StringToIntegerValidator extends AbstractStringToNumberValidator {
	private static final Integer MIN = Integer.valueOf(Integer.MIN_VALUE);
	private static final Integer MAX = Integer.valueOf(Integer.MAX_VALUE);

	/**
	 * @param converter
	 */
	public StringToIntegerValidator(NumberFormatConverter converter) {
		super(converter, MIN, MAX);
	}

	@Override
	protected boolean isInRange(Number number) {
		return StringToNumberParser.inIntegerRange(number);
	}
}
