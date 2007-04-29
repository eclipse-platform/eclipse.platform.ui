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

package org.eclipse.core.internal.databinding.conversion;

import org.eclipse.core.databinding.conversion.Converter;

import com.ibm.icu.text.NumberFormat;

/**
 * Base class for number to number converters.
 * <p>
 * This class is thread safe.
 * </p>
 * 
 * @since 1.0
 */
public abstract class NumberToNumberConverter extends Converter {
	private NumberFormat numberFormat;

	private boolean primitive;

	private String outOfRangeMessage;

	protected NumberToNumberConverter(NumberFormat numberFormat,
			Class fromType, Class toType) {
		super(fromType, toType);
		this.numberFormat = numberFormat;
		this.primitive = toType.isPrimitive();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
	 */
	public final Object convert(Object fromObject) {
		if (fromObject == null) {
			if (primitive) {
				throw new IllegalArgumentException(
						"Parameter 'fromObject' cannot be null."); //$NON-NLS-1$	
			}

			return null;
		}

		if (!(fromObject instanceof Number)) {
			throw new IllegalArgumentException(
					"Parameter 'fromObject' must be of type Number."); //$NON-NLS-1$
		}

		Number number = (Number) fromObject;
		Number result = doConvert(number);

		if (result != null) {
			return result;
		}

		synchronized (this) {
			if (outOfRangeMessage == null) {
				outOfRangeMessage = StringToNumberParser
						.createOutOfRangeMessage(new Short(Short.MIN_VALUE),
								new Short(Short.MAX_VALUE), numberFormat);
			}

			throw new IllegalArgumentException(outOfRangeMessage);
		}
	}

	/**
	 * Invoked when the number should converted.
	 * 
	 * @param number
	 * @return number if conversion was successfule, <code>null</code> if the
	 *         number was out of range
	 */
	protected abstract Number doConvert(Number number);

	/**
	 * NumberFormat being used by the converter. Access to the format must be
	 * synchronized on the number format instance.
	 * 
	 * @return number format
	 */
	public NumberFormat getNumberFormat() {
		return numberFormat;
	}
}
