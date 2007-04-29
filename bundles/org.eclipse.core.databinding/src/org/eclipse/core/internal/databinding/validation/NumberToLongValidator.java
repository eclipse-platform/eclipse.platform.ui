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

package org.eclipse.core.internal.databinding.validation;

import org.eclipse.core.internal.databinding.conversion.NumberToLongConverter;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;

/**
 * Validates if a Number can fit in a Long.
 * <p>
 * Class is thread safe.
 * </p>
 * @since 1.0
 */
public class NumberToLongValidator extends NumberToNumberValidator {
	private static final Long MIN = new Long(Long.MIN_VALUE);
	private static final Long MAX = new Long(Long.MAX_VALUE);
	
	/**
	 * @param converter
	 */
	public NumberToLongValidator(NumberToLongConverter converter) {
		super(converter, MIN, MAX);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.databinding.validation.NumberToNumberValidator#inRange(java.lang.Number)
	 */
	protected boolean inRange(Number number) {
		return StringToNumberParser.inLongRange(number);
	}
}
