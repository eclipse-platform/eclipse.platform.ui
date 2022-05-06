/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.core.internal.databinding.conversion;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.Supplier;

import org.eclipse.core.internal.databinding.BindingMessages;

/**
 * Utility class for the parsing of strings to numbers.
 *
 * @since 1.0
 */
public class StringToNumberParser {
	private static final BigDecimal FLOAT_MAX_BIG_DECIMAL = BigDecimal.valueOf(Float.MAX_VALUE);
	private static final BigDecimal FLOAT_MIN_BIG_DECIMAL = BigDecimal.valueOf(-Float.MAX_VALUE);

	private static final BigDecimal DOUBLE_MAX_BIG_DECIMAL = BigDecimal.valueOf(Double.MAX_VALUE);
	private static final BigDecimal DOUBLE_MIN_BIG_DECIMAL = BigDecimal.valueOf(-Double.MAX_VALUE);

	private static final Supplier<Format> GET_INSTANCE = findMethod(NumberFormat::getInstance, "getInstance"); //$NON-NLS-1$
	private static final Supplier<Format> GET_NUMBER_INSTANCE = findMethod(NumberFormat::getNumberInstance,
			"getNumberInstance"); //$NON-NLS-1$
	private static final Supplier<Format> GET_INTEGER_INSTANCE = findMethod(NumberFormat::getIntegerInstance,
			"getIntegerInstance"); //$NON-NLS-1$

	/**
	 * @param value
	 * @param numberFormat
	 * @param primitive
	 * @return result
	 */
	public static ParseResult parse(Object value, Format numberFormat, boolean primitive) {
		if (!(value instanceof String)) {
			throw new IllegalArgumentException(
					"Value to convert is not a String"); //$NON-NLS-1$
		}

		String source = (String) value;
		ParseResult result = new ParseResult();
		if (!primitive && source.trim().isEmpty()) {
			return result;
		}

		synchronized (numberFormat) {
			ParsePosition position = new ParsePosition(0);
			Number parseResult = (Number) numberFormat.parseObject(source, position);
			if (position.getIndex() != source.length()
					|| position.getErrorIndex() > -1) {

				result.position = position;
			} else {
				result.number = parseResult;
			}
		}

		return result;
	}

	/**
	 * The result of a parse operation.
	 *
	 * @since 1.0
	 */
	public static class ParseResult {
		/* package */Number number;
		/* package */ParsePosition position;

		/**
		 * The number as a result of the conversion. <code>null</code> if the
		 * value could not be converted or if the type is not a primitive and
		 * the value was an empty string.
		 *
		 * @return number
		 */
		public Number getNumber() {
			return number;
		}

		/**
		 * ParsePosition if an error occurred while parsing. <code>null</code>
		 * if no error occurred.
		 *
		 * @return parse position
		 */
		public ParsePosition getPosition() {
			return position;
		}
	}

	/**
	 * Formats an appropriate message for a parsing error.
	 *
	 * @param value
	 * @param position
	 * @return message
	 */
	public static String createParseErrorMessage(String value,
			ParsePosition position) {
		int errorIndex = (position.getErrorIndex() > -1) ? position
				.getErrorIndex() : position.getIndex();

		if (errorIndex < value.length()) {
			return BindingMessages.formatString(
					BindingMessages.VALIDATE_NUMBER_PARSE_ERROR, new Object[] {
							value, Integer.valueOf(errorIndex + 1),
							Character.valueOf(value.charAt(errorIndex)) });
		}
		return BindingMessages.formatString(
				BindingMessages.VALIDATE_NUMBER_PARSE_ERROR_NO_CHARACTER,
				new Object[] { value, Integer.valueOf(errorIndex + 1) });
	}

	/**
	 * Formats an appropriate message for an out of range error.
	 *
	 * @param minValue
	 * @param maxValue
	 * @param numberFormat
	 *            when accessed method synchronizes on instance
	 * @return message
	 */
	public static String createOutOfRangeMessage(Number minValue,
			Number maxValue, Format numberFormat) {
		String min = null;
		String max = null;

		synchronized (numberFormat) {
			min = numberFormat.format(minValue);
			max = numberFormat.format(maxValue);
		}

		return BindingMessages.formatString(
				"Validate_NumberOutOfRangeError", new Object[] { min, max }); //$NON-NLS-1$
	}

	/**
	 * Returns <code>true</code> if the provided <code>number</code> is in the
	 * range of a integer.
	 *
	 * @param number
	 * @return <code>true</code> if a valid integer
	 * @throws IllegalArgumentException
	 *             if the number type is unsupported
	 */
	public static boolean inIntegerRange(Number number) {
		return checkInteger(number, 31);
	}

	/**
	 * Validates the range of the provided <code>number</code>.
	 *
	 * @param number
	 * @param bitLength
	 *            number of bits allowed to be in range
	 * @return <code>true</code> if in range
	 */
	private static boolean checkInteger(Number number, int bitLength) {
		BigInteger bigInteger = null;

		if (number instanceof Integer || number instanceof Long) {
			bigInteger = BigInteger.valueOf(number.longValue());
		} else if (number instanceof Float || number instanceof Double) {
			double doubleValue = number.doubleValue();
			if (!Double.isNaN(doubleValue) && !Double.isInfinite(doubleValue)) {
				bigInteger = BigDecimal.valueOf(doubleValue).toBigInteger();
			} else {
				return false;
			}
		} else if (number instanceof BigInteger) {
			bigInteger = (BigInteger) number;
		} else if (number instanceof BigDecimal) {
			bigInteger = ((BigDecimal) number).toBigInteger();
		} else {
			/*
			 * The else is necessary as the ICU4J plugin has it's own BigDecimal
			 * implementation which isn't part of the replacement plugin. So
			 * that this will work we fall back on the double value of the
			 * number.
			 */
			bigInteger = BigDecimal.valueOf(number.doubleValue()).toBigInteger();
		}

		if (bigInteger != null) {
			return bigInteger.bitLength() <= bitLength;
		}

		throw new IllegalArgumentException(
				"Number of type [" + number.getClass().getName() + "] is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns <code>true</code> if the provided <code>number</code> is in the
	 * range of a long.
	 *
	 * @param number
	 * @return <code>true</code> if in range
	 * @throws IllegalArgumentException
	 *             if the number type is unsupported
	 */
	public static boolean inLongRange(Number number) {
		return checkInteger(number, 63);
	}

	/**
	 * Returns <code>true</code> if the provided <code>number</code> is in the
	 * range of a float.
	 *
	 * @param number
	 * @return <code>true</code> if in range
	 * @throws IllegalArgumentException
	 *             if the number type is unsupported
	 */
	public static boolean inFloatRange(Number number) {
		return checkDecimal(number, FLOAT_MIN_BIG_DECIMAL,
				FLOAT_MAX_BIG_DECIMAL);
	}

	private static boolean checkDecimal(Number number, BigDecimal min,
			BigDecimal max) {
		BigDecimal bigDecimal = null;
		if (number instanceof Integer || number instanceof Long) {
			bigDecimal = BigDecimal.valueOf(number.doubleValue());
		} else if (number instanceof Float || number instanceof Double) {
			double doubleValue = number.doubleValue();

			if (!Double.isNaN(doubleValue) && !Double.isInfinite(doubleValue)) {
				bigDecimal = BigDecimal.valueOf(doubleValue);
			} else {
				return false;
			}
		} else if (number instanceof BigInteger) {
			bigDecimal = new BigDecimal((BigInteger) number);
		} else if (number instanceof BigDecimal) {
			bigDecimal = (BigDecimal) number;
		} else {
			/*
			 * The else is necessary as the ICU4J plugin has it's own BigDecimal
			 * implementation which isn't part of the replacement plugin. So
			 * that this will work we fall back on the double value of the
			 * number.
			 */
			// if this is ever taken out, take care to un-comment the throw
			// clause and the if condition below, they were commented because
			// the
			// compiler complained about dead code..
			double doubleValue = number.doubleValue();

			if (!Double.isNaN(doubleValue) && !Double.isInfinite(doubleValue)) {
				bigDecimal = BigDecimal.valueOf(doubleValue);
			} else {
				return false;
			}
		}

		/* if (bigDecimal != null) */return max.compareTo(bigDecimal) >= 0
				&& min.compareTo(bigDecimal) <= 0;

		// throw new IllegalArgumentException(
		//				"Number of type [" + number.getClass().getName() + "] is not supported."); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns <code>true</code> if the provided <code>number</code> is in the
	 * range of a double.
	 *
	 * @param number
	 * @return <code>true</code> if in range
	 * @throws IllegalArgumentException
	 *             if the number type is unsupported
	 */
	public static boolean inDoubleRange(Number number) {
		return checkDecimal(number, DOUBLE_MIN_BIG_DECIMAL,
				DOUBLE_MAX_BIG_DECIMAL);
	}

	/**
	 * Returns <code>true</code> if the provided <code>number</code> is in the
	 * range of a short.
	 *
	 * @param number
	 * @return <code>true</code> if in range
	 */
	public static boolean inShortRange(Number number) {
		return checkInteger(number, 15);
	}

	/**
	 * Returns <code>true</code> if the provided <code>number</code> is in the
	 * range of a byte.
	 *
	 * @param number
	 * @return <code>true</code> if in range
	 */
	public static boolean inByteRange(Number number) {
		return checkInteger(number, 7);
	}

	/**
	 * Returns the default number format.
	 * {@code com.ibm.icu.text.NumberFormat.getNumberInstance()} if it is available,
	 * otherwise {@code java.text.NumberFormat.getNumberInstance()}.
	 *
	 * @return the number format
	 */
	public static Format getDefaultFormat() {
		return GET_INSTANCE.get();
	}

	/**
	 * Returns the default number format.
	 * {@code com.ibm.icu.text.NumberFormat.getNumberInstance()} if it is available,
	 * otherwise {@code java.text.NumberFormat.getNumberInstance()}.
	 *
	 * @return the number format
	 */
	public static Format getDefaultBigDecimalFormat() {
		Format format = GET_NUMBER_INSTANCE.get();
		if (format instanceof DecimalFormat) {
			((DecimalFormat) format).setParseBigDecimal(true);
		}
		return format;
	}

	/**
	 * Returns the default number format.
	 * {@code com.ibm.icu.text.NumberFormat.getNumberInstance()} if ICU is
	 * available, otherwise {@code java.text.NumberFormat.getNumberInstance()}.
	 *
	 * @return the number format
	 */
	public static Format getDefaultNumberFormat() {
		return GET_NUMBER_INSTANCE.get();
	}

	/**
	 * Returns the default integer format.
	 * {@code com.ibm.icu.text.NumberFormat.getIntegerInstance()} if ICU is
	 * available, otherwise {@code java.text.NumberFormat.getIntegerInstance()}.
	 *
	 * @return the number format
	 */
	public static Format getDefaultIntegerFormat() {
		return GET_INTEGER_INSTANCE.get();
	}

	/**
	 * Returns the default integer format.
	 * {@code com.ibm.icu.text.NumberFormat.getIntegerInstance()} if ICU is
	 * available, otherwise {@code java.text.NumberFormat.getIntegerInstance()}.
	 *
	 * @return the number format
	 */
	public static Format getDefaultIntegerBigDecimalFormat() {
		Format format = GET_INTEGER_INSTANCE.get();
		if (format instanceof DecimalFormat) {
			((DecimalFormat) format).setParseBigDecimal(true);
		}
		return format;
	}

	/**
	 * Creates a factory for {@link Format}s. The factory uses ICU if it is
	 * available on the class path, otherwise it uses the given supplier.
	 */
	private static Supplier<Format> findMethod(Supplier<Format> javaTextMethod, String methodName) {
		try {
			Method method = Class.forName("com.ibm.icu.text.NumberFormat").getMethod(methodName); //$NON-NLS-1$
			return () -> {
				try {
					return (Format) method.invoke(null);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e); // Should never happen
				}
			};
		} catch (ClassNotFoundException | SecurityException e) {
			return javaTextMethod;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e); // Should never happen
		}
	}
}
