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
 * @since 1.0
 */
public class StringToDoubleValidator extends AbstractStringToNumberValidator {
	private static final Double MIN = new Double(-Double.MAX_VALUE);
	private static final Double MAX = new Double(Double.MAX_VALUE);
	
	/**
	 * @param converter
	 */
	public StringToDoubleValidator(NumberFormatConverter converter) {
		super(converter, MIN, MAX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.databinding.validation.AbstractStringToNumberValidator#inRange(java.lang.Number)
	 */
	protected boolean isInRange(Number number) {
		return StringToNumberParser.inDoubleRange(number);
	}
}
