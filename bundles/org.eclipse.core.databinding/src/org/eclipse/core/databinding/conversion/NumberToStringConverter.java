/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Carter - bug 180392
 ******************************************************************************/

package org.eclipse.core.databinding.conversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;

/**
 * Converts a Number to a String using <code>NumberFormat.format(...)</code>.
 * This class is thread safe.
 * 
 * @since 1.0
 */
public class NumberToStringConverter extends Converter {
	private final NumberFormat numberFormat;
	private final Class fromType;
	private boolean fromTypeFitsLong;
	private boolean fromTypeIsDecimalType;
	private boolean fromTypeIsBigInteger;
	private boolean fromTypeIsBigDecimal;

	static Class icuBigDecimal = null;
	static Constructor icuBigDecimalCtr = null; 
	
	{
		/*
		 * If the full ICU4J library is available, we use the ICU BigDecimal
		 * class to support proper formatting and parsing of java.math.BigDecimal.
		 * 
		 * The version of ICU NumberFormat (DecimalFormat) included in eclipse excludes 
		 * support for java.math.BigDecimal, and if used falls back to converting as
		 * an unknown Number type via doubleValue(), which is undesirable.
		 * 
		 * See Bug #180392.
		 */
		try {
			icuBigDecimal = Class.forName("com.ibm.icu.math.BigDecimal"); //$NON-NLS-1$
			icuBigDecimalCtr = icuBigDecimal.getConstructor(new Class[] {BigInteger.class, int.class});
//			System.out.println("DEBUG: Full ICU4J support state: icuBigDecimal="+(icuBigDecimal != null)+", icuBigDecimalCtr="+(icuBigDecimalCtr != null)); //$NON-NLS-1$ //$NON-NLS-2$
		} 
		catch(ClassNotFoundException e) {}
		catch(NoSuchMethodException e) {}
	}	
	
	/**
	 * Constructs a new instance.
	 * <p>
	 * Private to restrict public instantiation.
	 * </p>
	 * 
	 * @param numberFormat
	 * @param fromType
	 */
	private NumberToStringConverter(NumberFormat numberFormat, Class fromType) {
		super(fromType, String.class);

		this.numberFormat = numberFormat;
		this.fromType = fromType;

		if (Integer.class.equals(fromType) || Integer.TYPE.equals(fromType)
				|| Long.class.equals(fromType) || Long.TYPE.equals(fromType)
				|| Short.class.equals(fromType) || Short.TYPE.equals(fromType)
				|| Byte.class.equals(fromType) || Byte.TYPE.equals(fromType)) {
			fromTypeFitsLong = true;
		} else if (Float.class.equals(fromType) || Float.TYPE.equals(fromType)
				|| Double.class.equals(fromType)
				|| Double.TYPE.equals(fromType)) {
			fromTypeIsDecimalType = true;
		} else if (BigInteger.class.equals(fromType)) {
			fromTypeIsBigInteger = true;
		} else if (BigDecimal.class.equals(fromType)) {
			fromTypeIsBigDecimal = true;
		}
	}

	/**
	 * Converts the provided <code>fromObject</code> to a <code>String</code>.
	 * If the converter was constructed for an object type, non primitive, a
	 * <code>fromObject</code> of <code>null</code> will be converted to an
	 * empty string.
	 * 
	 * @param fromObject
	 *            value to convert. May be <code>null</code> if the converter
	 *            was constructed for a non primitive type.
	 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object fromObject) {
		// Null is allowed when the type is not primitve.
		if (fromObject == null && !fromType.isPrimitive()) {
			return ""; //$NON-NLS-1$
		}

		Number number = (Number) fromObject;
		String result = null;
		if (fromTypeFitsLong) {
			synchronized (numberFormat) {
				result = numberFormat.format(number.longValue());
			}
		} else if (fromTypeIsDecimalType) {
			synchronized (numberFormat) {
				result = numberFormat.format(number.doubleValue());
			}
		} else if (fromTypeIsBigInteger) {
			synchronized (numberFormat) {
				result = numberFormat.format((BigInteger) number);
			}
		} else if (fromTypeIsBigDecimal) {
			if(icuBigDecimal != null && icuBigDecimalCtr != null && numberFormat instanceof DecimalFormat) {
				// Full ICU4J present. Convert java.math.BigDecimal to ICU BigDecimal to format. Bug #180392.
				BigDecimal o = (BigDecimal) fromObject;
				try {
					fromObject = icuBigDecimalCtr.newInstance(new Object[] {o.unscaledValue(), new Integer(o.scale())});
				}
				catch(InstantiationException e) {}
				catch(InvocationTargetException e) {}
				catch(IllegalAccessException e) {}
				// Otherwise, replacement plugin present and supports java.math.BigDecimal.
			}
			synchronized (numberFormat) {
				result = numberFormat.format(fromObject);
			}
		}
		

		return result;
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the type is a double
	 * @return Double converter for the default locale
	 */
	public static NumberToStringConverter fromDouble(boolean primitive) {
		return fromDouble(NumberFormat.getNumberInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return Double converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromDouble(NumberFormat numberFormat,
			boolean primitive) {
		return new NumberToStringConverter(numberFormat,
				(primitive) ? Double.TYPE : Double.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the type is a long
	 * @return Long converter for the default locale
	 */
	public static NumberToStringConverter fromLong(boolean primitive) {
		return fromLong(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return Long convert with the provided numberFormat
	 */
	public static NumberToStringConverter fromLong(NumberFormat numberFormat,
			boolean primitive) {
		return new NumberToStringConverter(numberFormat,
				(primitive) ? Long.TYPE : Long.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the type is a float
	 * @return Float converter for the default locale
	 */
	public static NumberToStringConverter fromFloat(boolean primitive) {
		return fromFloat(NumberFormat.getNumberInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return Float converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromFloat(NumberFormat numberFormat,
			boolean primitive) {
		return new NumberToStringConverter(numberFormat,
				(primitive) ? Float.TYPE : Float.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the type is a int
	 * @return Integer converter for the default locale
	 */
	public static NumberToStringConverter fromInteger(boolean primitive) {
		return fromInteger(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return Integer converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromInteger(
			NumberFormat numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat,
				(primitive) ? Integer.TYPE : Integer.class);
	}

	/**
	 * @return BigInteger convert for the default locale
	 */
	public static NumberToStringConverter fromBigInteger() {
		return fromBigInteger(NumberFormat.getIntegerInstance());
	}

	/**
	 * @param numberFormat
	 * @return BigInteger converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromBigInteger(
			NumberFormat numberFormat) {
		return new NumberToStringConverter(numberFormat, BigInteger.class);
	}
	
	/**
	 * @return BigDecimal convert for the default locale
	 * @since 1.2
	 */
	public static NumberToStringConverter fromBigDecimal() {
		return fromBigDecimal(NumberFormat.getNumberInstance());
	}

	/**
	 * @param numberFormat
	 * @return BigDecimal converter with the provided numberFormat
	 * @since 1.2
	 */
	public static NumberToStringConverter fromBigDecimal(
			NumberFormat numberFormat) {
		return new NumberToStringConverter(numberFormat, BigDecimal.class);
	}
	
	/**
	 * @param primitive
	 *            <code>true</code> if the type is a short
	 * @return Short converter for the default locale
	 * @since 1.2
	 */
	public static NumberToStringConverter fromShort(boolean primitive) {
		return fromShort(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return Short converter with the provided numberFormat
	 * @since 1.2
	 */
	public static NumberToStringConverter fromShort(
			NumberFormat numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat,
				(primitive) ? Short.TYPE : Short.class);
	}
	
	/**
	 * @param primitive
	 *            <code>true</code> if the type is a byte
	 * @return Byte converter for the default locale
	 * @since 1.2
	 */
	public static NumberToStringConverter fromByte(boolean primitive) {
		return fromByte(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return Byte converter with the provided numberFormat
	 * @since 1.2
	 */
	public static NumberToStringConverter fromByte(
			NumberFormat numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat,
				(primitive) ? Byte.TYPE : Byte.class);
	}
	
}
