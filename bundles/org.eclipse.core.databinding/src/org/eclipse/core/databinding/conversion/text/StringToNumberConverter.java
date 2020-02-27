/*******************************************************************************
 * Copyright (c) 2020 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.conversion.text;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;

import org.eclipse.core.internal.databinding.conversion.AbstractStringToNumberConverter;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;
import org.eclipse.core.internal.databinding.validation.NumberFormatConverter;

/**
 * Converts a String to a Number using <code>NumberFormat.parse(...)</code>.
 * This class is thread safe.
 * <p>
 * Note that this class does not have precise type parameters because it
 * manually handles argument type mismatches and throws
 * {@link IllegalArgumentException}.
 * <p>
 * The first type parameter of {@link NumberFormatConverter} is set to
 * {@link Object} to preserve backwards compatibility, but the argument is meant
 * to always be a {@link String}.
 * <p>
 * This class is a variant of the class with the same name in the parent
 * package, but it uses {@code java.text} instead of {@code com.ibm.icu}.
 * <p>
 * Methods on this class that don't take an argument number format use ICU if it
 * is available on the classpath, otherwise they use {@code java.text}.
 *
 * @param <T> The type to which values are converted.
 *
 * @since 1.9
 */
public final class StringToNumberConverter<T extends Number> extends AbstractStringToNumberConverter<T> {
	/**
	 * @param numberFormat used to parse the strings numbers.
	 * @param toType       target number type.
	 * @param min          minimum possible value for the type, can be
	 *                     <code>null</code> as BigInteger doesn't have bounds
	 * @param max          maximum possible value for the type, can be
	 *                     <code>null</code> as BigInteger doesn't have bounds
	 * @param boxedType    a convenience that allows for the checking against one
	 *                     type rather than boxed and unboxed types
	 */
	private StringToNumberConverter(Format numberFormat, Class<T> toType, Number min, Number max,
			Class<T> boxedType) {
		super(numberFormat, toType, min, max, boxedType);
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is an int
	 * @return to Integer converter for the default locale
	 */
	public static StringToNumberConverter<Integer> toInteger(boolean primitive) {
		return toInteger(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is an int
	 * @return to Integer converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Integer> toInteger(Format numberFormat, boolean primitive) {
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
		return toDouble(StringToNumberParser.getDefaultFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a double
	 * @return to Double converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Double> toDouble(Format numberFormat, boolean primitive) {
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
		return toLong(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a long
	 * @return to Long converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Long> toLong(Format numberFormat, boolean primitive) {
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
		return toFloat(StringToNumberParser.getDefaultNumberFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a float
	 * @return to Float converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Float> toFloat(Format numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Float.TYPE : Float.class, MIN_FLOAT, MAX_FLOAT,
				Float.class);
	}

	/**
	 * @return to BigInteger converter for the default locale
	 */
	public static StringToNumberConverter<BigInteger> toBigInteger() {
		return toBigInteger(StringToNumberParser.getDefaultIntegerBigDecimalFormat());
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @return to BigInteger converter with the provided numberFormat
	 */
	public static StringToNumberConverter<BigInteger> toBigInteger(Format numberFormat) {
		return new StringToNumberConverter<>(numberFormat, BigInteger.class,
				null, null, BigInteger.class);
	}

	/**
	 * @return to BigDecimal converter for the default locale
	 */
	public static StringToNumberConverter<BigDecimal> toBigDecimal() {
		return toBigDecimal(StringToNumberParser.getDefaultBigDecimalFormat());
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @return to BigDecimal converter with the provided numberFormat
	 */
	public static StringToNumberConverter<BigDecimal> toBigDecimal(Format numberFormat) {
		return new StringToNumberConverter<>(numberFormat, BigDecimal.class,
				null, null, BigDecimal.class);
	}

	/**
	 * @param primitive <code>true</code> if the convert to type is a short
	 * @return to Short converter for the default locale
	 */
	public static StringToNumberConverter<Short> toShort(boolean primitive) {
		return toShort(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a short
	 * @return to Short converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Short> toShort(Format numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Short.TYPE : Short.class, MIN_SHORT,
				MAX_SHORT, Short.class);
	}

	/**
	 * @param primitive <code>true</code> if the convert to type is a byte
	 * @return to Byte converter for the default locale
	 */
	public static StringToNumberConverter<Byte> toByte(boolean primitive) {
		return toByte(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a byte
	 * @return to Byte converter with the provided numberFormat
	 */
	public static StringToNumberConverter<Byte> toByte(Format numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Byte.TYPE : Byte.class, MIN_BYTE,
				MAX_BYTE, Byte.class);
	}

}
