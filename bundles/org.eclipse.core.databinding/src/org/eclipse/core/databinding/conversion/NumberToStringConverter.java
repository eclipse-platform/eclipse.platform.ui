/*******************************************************************************
 * Copyright (c) 2007, 2022 IBM Corporation and others.
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
 *     Matt Carter - bug 180392
 ******************************************************************************/

package org.eclipse.core.databinding.conversion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;

import org.eclipse.core.internal.databinding.conversion.AbstractNumberToStringConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * Converts a Number to a String using <code>NumberFormat.format(...)</code>.
 * This class is thread safe.
 *
 * The first type parameter of {@link Converter} is set to {@link Object} to
 * preserve backwards compatibility, but the argument is meant to always be a
 * {@link Number}.
 *
 * @since 1.0
 * @deprecated Use
 *             {@link org.eclipse.core.databinding.conversion.text.NumberToStringConverter}
 *             instead, which does not use {@code com.ibm.icu} as that package
 *             is planned to be removed in the future from platform.
 * @noreference This class is not intended to be referenced by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
@Deprecated(forRemoval = true)
public class NumberToStringConverter extends AbstractNumberToStringConverter {
	private NumberToStringConverter(Format numberFormat, Class<?> fromType) {
		super(numberFormat, fromType);
	}

	/**
	 * @implNote Overridden to avoid API tooling problem.
	 */
	@Override
	public String convert(Object fromObject) {
		return super.convert(fromObject);
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a double
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a long
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a float
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a int
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
	 * @param numberFormat number format used by the converter
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
	 * @param numberFormat number format used by the converter
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a short
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the type is a byte
	 * @return Byte converter with the provided numberFormat
	 * @since 1.2
	 */
	public static NumberToStringConverter fromByte(
			NumberFormat numberFormat, boolean primitive) {
		return new NumberToStringConverter(numberFormat,
				(primitive) ? Byte.TYPE : Byte.class);
	}

}
