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

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;

/**
 * @since 1.0
 */
public class StringToByteValidator extends AbstractStringToNumberValidator {
	/**
	 * @param converter
	 */
	public StringToByteValidator(NumberFormatConverter<?, ?> converter) {
		super(converter, Byte.MIN_VALUE, Byte.MAX_VALUE);
	}

	@Override
	protected boolean isInRange(Number number) {
		return StringToNumberParser.inByteRange(number);
	}
}
