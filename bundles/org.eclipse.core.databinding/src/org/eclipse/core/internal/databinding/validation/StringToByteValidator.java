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
 * @since 1.0
 */
public class StringToByteValidator extends AbstractStringToNumberValidator {
	private static final Byte MIN = new Byte(Byte.MIN_VALUE);
	private static final Byte MAX = new Byte(Byte.MAX_VALUE);

	/**
	 * @param converter
	 */
	public StringToByteValidator(NumberFormatConverter converter) {
		super(converter, MIN, MAX);
	}

	@Override
	protected boolean isInRange(Number number) {
		return StringToNumberParser.inByteRange(number);
	}
}
