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
 *     Michael Scharf - bug 240562
 *     Matt Carter - bug 180392
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 445446
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
 * Note that this class does not have precise type parameters because it
 * manually handles argument type mismatches and throws
 * {@link IllegalArgumentException}.
 *
 * The first type parameter of {@link NumberFormatConverter} is set to
 * {@link Object} to preserve backwards compability, but the argument is meant
 * to always be a {@link String}.
 *
 * @param <T>
 *            The type to which values are converted.
 *
 * @since 1.0
 */
public class StringToNumberConverter<T extends Number> extends NumberFormatConverter<Object, T> {
	private Class<?> toType;
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
	private final Class<?> boxedType;

	private static final Integer MIN_INTEGER = Integer.valueOf(Integer.MIN_VALUE);
	private static final Integer MAX_INTEGER = Integer.valueOf(Integer.MAX_VALUE);

	// This code looks deceptive, but we can't use Double.MIN_VALUE because it
	// is actually the smallest *positive* number.
	private static final Double MIN_DOUBLE = Double.valueOf(-Double.MAX_VALUE);
	private static final Double MAX_DOUBLE = Double.valueOf(Double.MAX_VALUE);

	private static final Long MIN_LONG = Long.valueOf(Long.MIN_VALUE);
	private static final Long MAX_LONG = Long.valueOf(Long.MAX_VALUE);

	// This code looks deceptive, but we can't use Float.MIN_VALUE because it is
	// actually the smallest *positive* number.
	private static final Float MIN_FLOAT = Float.valueOf(-Float.MAX_VALUE);
	private static final Float MAX_FLOAT = Float.valueOf(Float.MAX_VALUE);

	private static final Short MIN_SHORT = Short.valueOf(Short.MIN_VALUE);
	private static final Short MAX_SHORT = Short.valueOf(Short.MAX_VALUE);

	private static final Byte MIN_BYTE = Byte.valueOf(Byte.MIN_VALUE);
	private static final Byte MAX_BYTE = Byte.valueOf(Byte.MAX_VALUE);

	static Class<?> icuBigDecimal = null;
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
			icuBigDecimalScale = icuBigDecimal.getMethod("scale"); //$NON-NLS-1$
			icuBigDecimalUnscaledValue = icuBigDecimal.getMethod("unscaledValue"); //$NON-NLS-1$
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
	private StringToNumberConverter(NumberFormat numberFormat, Class<T> toType, Number min, Number max,
			Class<T> boxedType) {
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
	 *             or the value is out of range for the {@link #getToType() to
	 *             type}.
	 * @throws IllegalArgumentException
	 *             if conversion was not possible
	 * @since 1.7
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T convert(Object fromObject) {
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
				return (T) Integer.valueOf(result.getNumber().intValue());
			}
		} else if (Double.class.equals(boxedType)) {
			if (StringToNumberParser.inDoubleRange(result.getNumber())) {
				return (T) Double.valueOf(result.getNumber().doubleValue());
			}
		} else if (Long.class.equals(boxedType)) {
			if (StringToNumberParser.inLongRange(result.getNumber())) {
				return (T) Long.valueOf(result.getNumber().longValue());
			}
		} else if (Float.class.equals(boxedType)) {
			if (StringToNumberParser.inFloatRange(result.getNumber())) {
				return (T) Float.valueOf(result.getNumber().floatValue());
			}
		} else if (BigInteger.class.equals(boxedType)) {
			Number n = result.getNumber();
			if(n instanceof Long)
				return (T) BigInteger.valueOf(n.longValue());
			else if(n instanceof BigInteger)
				return (T) n;
			else if(n instanceof BigDecimal)
				return (T) ((BigDecimal) n).toBigInteger();
			else
				return (T) BigDecimal.valueOf(n.doubleValue()).toBigInteger();
		} else if (BigDecimal.class.equals(boxedType)) {
			Number n = result.getNumber();
			if(n instanceof Long)
				return (T) BigDecimal.valueOf(n.longValue());
			else if(n instanceof BigInteger)
				return (T) new BigDecimal((BigInteger) n);
			else if(n instanceof BigDecimal)
				return (T) n;
			else if(icuBigDecimal != null && icuBigDecimal.isInstance(n)) {
				try {
					// Get ICU BigDecimal value and use to construct java.math.BigDecimal
					int scale = ((Integer) icuBigDecimalScale.invoke(n)).intValue();
					BigInteger unscaledValue = (BigInteger) icuBigDecimalUnscaledValue.invoke(n);
					return (T) new java.math.BigDecimal(unscaledValue, scale);
				} catch(IllegalAccessException e) {
					throw new IllegalArgumentException("Error (IllegalAccessException) converting BigDecimal using ICU"); //$NON-NLS-1$
				} catch(InvocationTargetException e) {
					throw new IllegalArgumentException("Error (InvocationTargetException) converting BigDecimal using ICU"); //$NON-NLS-1$
				}
			} else if(n instanceof Double) {
				BigDecimal bd = BigDecimal.valueOf(n.doubleValue());
				if (bd.scale() == 0)
					return (T) bd;
				throw new IllegalArgumentException("Non-integral Double value returned from NumberFormat " + //$NON-NLS-1$
						"which cannot be accurately stored in a BigDecimal due to lost precision. " + //$NON-NLS-1$
						"Consider using ICU4J or Java 5 which can properly format and parse these types."); //$NON-NLS-1$
			}
		} else if (Short.class.equals(boxedType)) {
			if (StringToNumberParser.inShortRange(result.getNumber())) {
				return (T) Short.valueOf(result.getNumber().shortValue());
			}
		} else if (Byte.class.equals(boxedType)) {
			if (StringToNumberParser.inByteRange(result.getNumber())) {
				return (T) Byte.valueOf(result.getNumber().byteValue());
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
	public static StringToNumberConverter<Integer> toInteger(boolean primitive) {
		return toInteger(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Integer converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Integer> toInteger(NumberFormat numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Integer.TYPE : Integer.class, MIN_INTEGER,
				MAX_INTEGER, Integer.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a double
	 * @return to Double converter for the default locale
	 */
	public static StringToNumberConverter<Double> toDouble(boolean primitive) {
		return toDouble(NumberFormat.getNumberInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Double converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Double> toDouble(NumberFormat numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Double.TYPE : Double.class, MIN_DOUBLE,
				MAX_DOUBLE, Double.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a long
	 * @return to Long converter for the default locale
	 */
	public static StringToNumberConverter<Long> toLong(boolean primitive) {
		return toLong(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Long converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Long> toLong(NumberFormat numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Long.TYPE : Long.class, MIN_LONG, MAX_LONG,
				Long.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a float
	 * @return to Float converter for the default locale
	 */
	public static StringToNumberConverter<Float> toFloat(boolean primitive) {
		return toFloat(NumberFormat.getNumberInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Float converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Float> toFloat(NumberFormat numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Float.TYPE : Float.class, MIN_FLOAT, MAX_FLOAT,
				Float.class);
	}

	/**
	 * @return to BigInteger converter for the default locale
	 */
	public static StringToNumberConverter<BigInteger> toBigInteger() {
		return toBigInteger(NumberFormat.getIntegerInstance());
	}

	/**
	 * @param numberFormat
	 * @return to BigInteger converter with the provided numberFormat
	 */
	public static StringToNumberConverter<BigInteger> toBigInteger(NumberFormat numberFormat) {
		return new StringToNumberConverter<>(numberFormat, BigInteger.class,
				null, null, BigInteger.class);
	}

	/**
	 * @return to BigDecimal converter for the default locale
	 * @since 1.2
	 */
	public static StringToNumberConverter<BigDecimal> toBigDecimal() {
		return toBigDecimal(NumberFormat.getNumberInstance());
	}

	/**
	 * @param numberFormat
	 * @return to BigDecimal converter with the provided numberFormat
	 * @since 1.2
	 */
	public static StringToNumberConverter<BigDecimal> toBigDecimal(NumberFormat numberFormat) {
		return new StringToNumberConverter<>(numberFormat, BigDecimal.class,
				null, null, BigDecimal.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a short
	 * @return to Short converter for the default locale
	 * @since 1.2
	 */
	public static StringToNumberConverter<Short> toShort(boolean primitive) {
		return toShort(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Short converter with the provided numberFormat
	 * @since 1.2
	 */
	public static StringToNumberConverter<Short> toShort(NumberFormat numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Short.TYPE : Short.class, MIN_SHORT,
				MAX_SHORT, Short.class);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a byte
	 * @return to Byte converter for the default locale
	 * @since 1.2
	 */
	public static StringToNumberConverter<Byte> toByte(boolean primitive) {
		return toByte(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Byte converter with the provided numberFormat
	 * @since 1.2
	 */
	public static StringToNumberConverter<Byte> toByte(NumberFormat numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Byte.TYPE : Byte.class, MIN_BYTE,
				MAX_BYTE, Byte.class);
	}

}
