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

import org.eclipse.core.databinding.conversion.Converter;

import com.ibm.icu.text.NumberFormat;

/**
 * Converter that uses a number format for conversion.
 * 
 * @since 1.0
 */
public abstract class NumberFormatConverter extends Converter {
	private final NumberFormat numberFormat;
	
	/**
	 * @param fromType
	 * @param toType
	 * @param numberFormat 
	 */
	public NumberFormatConverter(Object fromType, Object toType, NumberFormat numberFormat) {
		super(fromType, toType);
		
		this.numberFormat = numberFormat;
	}

	/**
	 * @return number format
	 */
	/*package */ NumberFormat getNumberFormat() {
		return numberFormat;
	}
}
