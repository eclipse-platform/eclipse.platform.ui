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

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;

/**
 * Validates that a string is of the appropriate format and is in the range of
 * an long.
 * 
 * @since 1.0
 */
public class StringToLongValidator extends AbstractStringToNumberValidator {
	private static final Long MIN = new Long(Long.MIN_VALUE);
	private static final Long MAX = new Long(Long.MAX_VALUE);

	/**
	 * @param converter
	 */
	public StringToLongValidator(NumberFormatConverter converter) {
		super(converter, MIN, MAX);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.databinding.validation.AbstractStringToNumberValidator#inRange(java.lang.Number)
	 */
	protected boolean isInRange(Number number) {
		return StringToNumberParser.inLongRange(number);
	}
}
