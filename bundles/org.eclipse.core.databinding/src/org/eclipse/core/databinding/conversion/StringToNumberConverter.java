/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Scharf - bug 240562
 *     Matt Carter - bug 180392
 ******************************************************************************/

package org.eclipse.core.databinding.conversion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser.ParseResult;
import org.eclipse.core.internal.databinding.validation.NumberFormatConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * Converts a String to a Number using <code>NumberFormat.parse(...)</code>.
 * This class is thread safe.
 * 
 * @since 1.0
 */
public class StringToNumberConverter extends NumberFormatConverter {
	private Class toType;
	/**
	 * NumberFormat instance to use for conversion. Access must be synchronized.
	 */
	private NumberFormat numberFormat;

	/**
	 * Minimum possible value for the type. Can be <code>null</code> as
	 * BigInteger doesn't have bounds.
	 */
	private final Number min;
	/**
	 * Maximum possible value for the type. Can be <code>null</code> as
	 * BigInteger doesn't have bounds.
	 */
	private final Number max;

	/**
	 * The boxed type of the toType;
	 */
	private final Class boxedType;

	private static final Integer MIN_INTEGER = new Integer(Integer.MIN_VALUE);
	private static final Integer MAX_INTEGER = new Integer(Integer.MAX_VALUE);

	// This code looks deceptive, but we can't use Double.MIN_VALUE because it
	// is actually the smallest *positive* number.
	private static final Double MIN_DOUBLE = new Double(-Double.MAX_VALUE);
	private static final Double MAX_DOUBLE = new Double(Double.MAX_VALUE);

	private static final Long MIN_LONG = new Long(Long.MIN_VALUE);
	private static final Long MAX_LONG = new Long(Long.MAX_VALUE);

	// This code looks deceptive, but we can't use Float.MIN_VALUE because it is
	// actually the smallest *positive* number.
	private static final Float MIN_FLOAT = new Float(-Float.MAX_VALUE);
	private static final Float MAX_FLOAT = new Float(Float.MAX_VALUE);

	private static final Short MIN_SHORT = new Short(Short.MIN_VALUE);
	private static final Short MAX_SHORT = new Short(Short.MAX_VALUE);
	
	private static final Byte MIN_BYTE = new Byte(Byte.MIN_VALUE);
	private static final Byte MAX_BYTE = new Byte(Byte.MAX_VALUE);
	
	static Class icuBigDecimal = null;
	static Method icuBigDecimalScale = null;
	static Method icuBigDecimalUnscaledValue = null;
	
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
			icuBigDecimalScale = icuBigDecimal.getMethod("scale", null); //$NON-NLS-1$
			icuBigDecimalUnscaledValue = icuBigDecimal.getMethod("unscaledValue", null); //$NON-NLS-1$
/*			System.out.println("DEBUG: Full ICU4J support state: icuBigDecimal="+ //$NON-NLS-1$
					(icuBigDecimal != null)+", icuBigDecimalScale="+(icuBigDecimalScale != null)+ //$NON-NLS-1$
					", icuBigDecimalUnscaledValue="+(icuBigDecimalUnscaledValue != null)); //$NON-NLS-1$ */  
		} 
		catch(ClassNotFoundException e) {}
		catch(NoSuchMethodException e) {}
	}		
	/**
	 * @param numberFormat
	 * @param toType
	 * @param min
	 *            minimum possible value for the type, can be <code>null</code>
	 *            as BigInteger doesn't have bounds
	 * @param max
	 *            maximum possible value for the type, can be <code>null</code>
	 *            as BigInteger doesn't have bounds
	 * @param boxedType
	 *            a convenience that allows for the checking against one type
	 *            rather than boxed and unboxed types
	 */
	private StringToNumberConverter(NumberFormat numberFormat, Class toType,
			Number min, Number max, Class boxedType) {
		super(String.class, toType, numberFormat);

		this.toType = toType;
		this.numberFormat = numberFormat;
		this.min = min;
		this.max = max;
		this.boxedType = boxedType;
	}

	/**
	 * Converts the provided <code>fromObject</code> to the requested
	 * {@link #getToType() to type}.
	 * 
	 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
	 * @throws IllegalArgumentException
	 *             if the value isn't in the format required by the NumberFormat
	 *             or the value is out of range for the
	 *             {@link #getToType() to type}.
	 * @throws IllegalArgumentException
	 *             if conversion was not possible
	 */
	public Object convert(Object fromObject) {
		ParseResult result = StringToNumberParser.parse(fromObject,
				numberFormat, toType.isPrimitive());

		if (result.getPosition() != null) {
			// this shouldn't happen in the pipeline as validation should catch
			// it but anyone can call convert so we should return a properly
			// formatted message in an exception
			throw new IllegalArgumentException(StringToNumberParser
					.createParseErrorMessage((String) fromObject, result
							.getPosition()));
		} else if (result.getNumber() == null) {
			// if an error didn't occur and the number is null then it's a boxed
			// type and null should be returned
			return null;
		}

		/*
		 * Technically the checks for ranges aren't needed here because the
		 * validator should have validated this already but we shouldn't assume
		 * this has occurred.
		 */
		if (Integer.class.equals(boxedType)) {
			if (StringToNumberParser.inIntegerRange(result.getNumber())) {
				return new Integer(result.getNumber().intValue());
			}
		} else if (Double.class.equals(boxedType)) {
			if (StringToNumberParser.inDoubleRange(result.getNumber())) {
				return new Double(result.getNumber().doubleValue());
			}
		} else if (Long.class.equals(boxedType)) {
			if (StringToNumberParser.inLongRange(result.getNumber())) {
				return new Long(result.getNumber().longValue());
			}
		} else if (Float.class.equals(boxedType)) {
			if (StringToNumberParser.inFloatRange(result.getNumber())) {
				return new Float(result.getNumber().floatValue());
			}
		} else if (BigInteger.class.equals(boxedType)) {
			Number n = result.getNumber();
			if(n instanceof Long)
				return BigInteger.valueOf(n.longValue());
			else if(n instanceof BigInteger)
				return n;
			else if(n instanceof BigDecimal)
				return ((BigDecimal) n).toBigInteger();
			else
				return new BigDecimal(n.doubleValue()).toBigInteger();
		} else if (BigDecimal.class.equals(boxedType)) {
			Number n = result.getNumber();
			if(n instanceof Long)
				return BigDecimal.valueOf(n.longValue());
			else if(n instanceof BigInteger)
				return new BigDecimal((BigInteger) n);
			else if(n instanceof BigDecimal)
				return n;
			else if(icuBigDecimal != null && icuBigDecimal.isInstance(n)) {
				try {
					// Get ICU BigDecimal value and use to construct java.math.BigDecimal
					int scale = ((Integer) icuBigDecimalScale.invoke(n, null)).intValue();
					BigInteger unscaledValue = (BigInteger) icuBigDecimalUnscaledValue.invoke(n, null);
					return new java.math.BigDecimal(unscaledValue, scale);
				} catch(IllegalAccessException e) {
					throw new IllegalArgumentException("Error (IllegalAccessException) converting BigDecimal using ICU"); //$NON-NLS-1$
				} catch(InvocationTargetException e) {
					throw new IllegalArgumentException("Error (InvocationTargetException) converting BigDecimal using ICU"); //$NON-NLS-1$
				}
			} else if(n instanceof Double) {
				BigDecimal bd = new BigDecimal(n.doubleValue());
				if(bd.scale() == 0) return bd;
				throw new IllegalArgumentException("Non-integral Double value returned from NumberFormat " + //$NON-NLS-1$
						"which cannot be accurately stored in a BigDecimal due to lost precision. " + //$NON-NLS-1$
						"Consider using ICU4J or Java 5 which can properly format and parse these types."); //$NON-NLS-1$
			}
		} else if (Short.class.equals(boxedType)) {
			if (StringToNumberParser.inShortRange(result.getNumber())) {
				return new Short(result.getNumber().shortValue());
			}
		} else if (Byte.class.equals(boxedType)) {
			if (StringToNumberParser.inByteRange(result.getNumber())) {
				return new Byte(result.getNumber().byteValue());
			}
		}

		if (min != null && max != null) {
			throw new IllegalArgumentException(StringToNumberParser
					.createOutOfRangeMessage(min, max, numberFormat));
		}

		/*
		 * Fail safe. I don't think this could even be thrown but throwing the
		 * exception is better than returning null and hiding the error.
		 */
		throw new IllegalArgumentException(
				"Could not convert [" + fromObject + "] to type [" + toType + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
				(primitive) ? Integer.TYPE : Integer.class, MIN_INTEGER,
				MAX_INTEGER, Integer.class);
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
				(primitive) ? Double.TYPE : Double.class, MIN_DOUBLE,
				MAX_DOUBLE, Double.class);
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
	public static StringToNumberConverter toLong(NumberFormat numberFormat,
			boolean primitive) {
		return new StringToNumberConverter(numberFormat,
				(primitive) ? Long.TYPE : Long.class, MIN_LONG, MAX_LONG,
				Long.class);
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
	public static StringToNumberConverter toFloat(NumberFormat numberFormat,
			boolean primitive) {
		return new StringToNumberConverter(numberFormat,
				(primitive) ? Float.TYPE : Float.class, MIN_FLOAT, MAX_FLOAT,
				Float.class);
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
		return new StringToNumberConverter(numberFormat, BigInteger.class,
				null, null, BigInteger.class);
	}

	/**
	 * @return to BigDecimal converter for the default locale
	 * @since 1.2
	 */
	public static StringToNumberConverter toBigDecimal() {
		return toBigDecimal(NumberFormat.getNumberInstance());
	}
	
	/**
	 * @param numberFormat
	 * @return to BigDecimal converter with the provided numberFormat
	 * @since 1.2
	 */
	public static StringToNumberConverter toBigDecimal(NumberFormat numberFormat) {
		return new StringToNumberConverter(numberFormat, BigDecimal.class,
				null, null, BigDecimal.class);
	}
	
	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a short
	 * @return to Short converter for the default locale
	 * @since 1.2
	 */
	public static StringToNumberConverter toShort(boolean primitive) {
		return toShort(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Short converter with the provided numberFormat
	 * @since 1.2
	 */
	public static StringToNumberConverter toShort(NumberFormat numberFormat,
			boolean primitive) {
		return new StringToNumberConverter(numberFormat,
				(primitive) ? Short.TYPE : Short.class, MIN_SHORT,
				MAX_SHORT, Short.class);
	}
	
	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a byte
	 * @return to Byte converter for the default locale
	 * @since 1.2
	 */
	public static StringToNumberConverter toByte(boolean primitive) {
		return toByte(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Byte converter with the provided numberFormat
	 * @since 1.2
	 */
	public static StringToNumberConverter toByte(NumberFormat numberFormat,
			boolean primitive) {
		return new StringToNumberConverter(numberFormat,
				(primitive) ? Byte.TYPE : Byte.class, MIN_BYTE,
				MAX_BYTE, Byte.class);
	}
	
}
