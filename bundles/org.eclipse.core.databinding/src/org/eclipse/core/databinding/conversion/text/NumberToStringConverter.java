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

import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.internal.databinding.conversion.AbstractNumberToStringConverter;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;

/**
 * Converts a Number to a String using <code>Format.format(...)</code>. This
 * class is thread safe.
 * <p>
 * The first type parameter of {@link Converter} is set to {@link Object} to
 * preserve backwards compatibility, but the argument is meant to always be a
 * {@link Number}.
 * <p>
 * This class is a variant of the class with the same name in the parent
 * package, but it uses {@code java.text} instead of {@code com.ibm.icu}.
 * <p>
 * Methods on this class that don't take an argument number format use ICU if it
 * is available on the classpath, otherwise they use {@code java.text}.
 *
 * @since 1.9
 */
public final class NumberToStringConverter extends AbstractNumberToStringConverter {
	/**
	 * Constructs a new instance.
	 * <p>
	 * Private to restrict public instantiation.
	 * </p>
	 *
	 * @param numberFormat used to format the numbers into strings. Non-null.
	 * @param fromType     type of the source numbers. Non-null.
	 */
	private NumberToStringConverter(Format numberFormat, Class<?> fromType) {
		super(numberFormat, fromType);
	}

	/**
	 * @param primitive <code>true</code> if the type is a double
	 * @return Double converter for the default locale
	 */
	public static NumberToStringConverter fromDouble(boolean primitive) {
		return fromDouble(StringToNumberParser.getDefaultNumberFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a double
	 * @return Double converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromDouble(Format numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat, (primitive) ? Double.TYPE : Double.class);
	}

	/**
	 * @param primitive <code>true</code> if the type is a long
	 * @return Long converter for the default locale
	 */
	public static NumberToStringConverter fromLong(boolean primitive) {
		return fromLong(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a long
	 * @return Long convert with the provided numberFormat
	 */
	public static NumberToStringConverter fromLong(Format numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat, (primitive) ? Long.TYPE : Long.class);
	}

	/**
	 * @param primitive <code>true</code> if the type is a float
	 * @return Float converter for the default locale
	 */
	public static NumberToStringConverter fromFloat(boolean primitive) {
		return fromFloat(StringToNumberParser.getDefaultNumberFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a float
	 * @return Float converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromFloat(Format numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat, (primitive) ? Float.TYPE : Float.class);
	}

	/**
	 * @param primitive <code>true</code> if the type is a int
	 * @return Integer converter for the default locale
	 */
	public static NumberToStringConverter fromInteger(boolean primitive) {
		return fromInteger(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a int
	 * @return Integer converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromInteger(Format numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat, (primitive) ? Integer.TYPE : Integer.class);
	}

	/**
	 * @return BigInteger convert for the default locale
	 */
	public static NumberToStringConverter fromBigInteger() {
		return fromBigInteger(StringToNumberParser.getDefaultIntegerBigDecimalFormat());
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @return BigInteger converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromBigInteger(Format numberFormat) {
		return new NumberToStringConverter(numberFormat, BigInteger.class);
	}

	/**
	 * @return BigDecimal convert for the default locale
	 */
	public static NumberToStringConverter fromBigDecimal() {
		return fromBigDecimal(StringToNumberParser.getDefaultBigDecimalFormat());
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @return BigDecimal converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromBigDecimal(Format numberFormat) {
		return new NumberToStringConverter(numberFormat, BigDecimal.class);
	}

	/**
	 * @param primitive <code>true</code> if the type is a short
	 * @return Short converter for the default locale
	 */
	public static NumberToStringConverter fromShort(boolean primitive) {
		return fromShort(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a short
	 * @return Short converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromShort(Format numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat, (primitive) ? Short.TYPE : Short.class);
	}

	/**
	 * @param primitive <code>true</code> if the type is a byte
	 * @return Byte converter for the default locale
	 */
	public static NumberToStringConverter fromByte(boolean primitive) {
		return fromByte(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a byte
	 * @return Byte converter with the provided numberFormat
	 */
	public static NumberToStringConverter fromByte(Format numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat, (primitive) ? Byte.TYPE : Byte.class);
	}

}
