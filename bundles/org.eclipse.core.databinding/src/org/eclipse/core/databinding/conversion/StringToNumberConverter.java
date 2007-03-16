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

package org.eclipse.core.databinding.conversion;

import java.math.BigInteger;
import java.text.ParsePosition;

import com.ibm.icu.text.NumberFormat;

/**
 * Converts a String to a Number using <code>NumberFormat.parse(...)</code>.
 * This class is thread safe.
 * 
 * @since 1.0
 */
public class StringToNumberConverter extends Converter {
	private Class toType;
	/**
	 * NumberFormat instance to use for conversion. Access must be synchronized.
	 */
	private NumberFormat numberFormat;

	/**
	 * @param toType
	 */
	private StringToNumberConverter(NumberFormat numberFormat, Class toType) {
		super(String.class, toType);
		
		this.toType = toType;
		this.numberFormat = numberFormat;
	}

	/**
	 * 
	 * 
	 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object fromObject) {
		if (!(fromObject instanceof String)) {
			throw new IllegalArgumentException(
					"'fromObject' not instanceof String"); //$NON-NLS-1$
		}
		String source = (String) fromObject;
		if (!toType.isPrimitive() && source.trim().length() == 0) {
			return null;
		}

		Number result = null;

		synchronized (numberFormat) {
			ParsePosition position = new ParsePosition(0);
			result = numberFormat.parse(source, position);

			if (position.getIndex() != source.length()
					|| position.getErrorIndex() > -1) {
				int errorIndex = (position.getErrorIndex() > -1) ? position
						.getErrorIndex() : position.getIndex();

				throw new IllegalArgumentException(
						"FromObject " + fromObject + " was invalid at character " + errorIndex); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		if (Integer.class.equals(toType)
				|| Integer.TYPE.equals(toType)) {
			return new Integer(result.intValue());
		} else if (Double.class.equals(toType)
				|| Double.TYPE.equals(toType)) {
			return new Double(result.doubleValue());
		} else if (Long.class.equals(toType)
				|| Long.TYPE.equals(toType)) {
			return new Long(result.longValue());
		} else if (Float.class.equals(toType)
				|| Float.TYPE.equals(toType)) {
			return new Float(result.floatValue());
		} else if (BigInteger.class.equals(toType)) {
			return BigInteger.valueOf(result.longValue());
		}

		return null;
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is an int
	 * @return to Integer converter for the default locale
	 */
	public static StringToNumberConverter toInteger(boolean primitive) {
		return toInteger(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Integer converter with the provided numberFormat
	 */
	public static StringToNumberConverter toInteger(NumberFormat numberFormat,
			boolean primitive) {
		return new StringToNumberConverter(numberFormat,
				(primitive) ? Integer.TYPE : Integer.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a double
	 * @return to Double converter for the default locale
	 */
	public static StringToNumberConverter toDouble(boolean primitive) {
		return toDouble(NumberFormat.getNumberInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Double converter with the provided numberFormat
	 */
	public static StringToNumberConverter toDouble(NumberFormat numberFormat,
			boolean primitive) {
		return new StringToNumberConverter(numberFormat,
				(primitive) ? Double.TYPE : Double.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a long
	 * @return to Long converter for the default locale
	 */
	public static StringToNumberConverter toLong(boolean primitive) {
		return toLong(NumberFormat.getIntegerInstance(), primitive);
	}
	
	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Long converter with the provided numberFormat
	 */
	public static StringToNumberConverter toLong(NumberFormat numberFormat, boolean primitive) {
		return new StringToNumberConverter(numberFormat,
				(primitive) ? Long.TYPE : Long.class);		
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a float
	 * @return to Float converter for the default locale
	 */
	public static StringToNumberConverter toFloat(boolean primitive) {
		return toFloat(NumberFormat.getNumberInstance(), primitive);
	}
	
	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Float converter with the provided numberFormat
	 */
	public static StringToNumberConverter toFloat(NumberFormat numberFormat, boolean primitive) {
		return new StringToNumberConverter(numberFormat,
				(primitive) ? Float.TYPE : Float.class);		
	}

	/**
	 * @return to BigInteger converter for the default locale
	 */
	public static StringToNumberConverter toBigInteger() {
		return toBigInteger(NumberFormat.getIntegerInstance());
	}
	
	/**
	 * @param numberFormat
	 * @return to BigInteger converter with the provided numberFormat
	 */
	public static StringToNumberConverter toBigInteger(NumberFormat numberFormat) {
		return new StringToNumberConverter(numberFormat, BigInteger.class);
	}
}
