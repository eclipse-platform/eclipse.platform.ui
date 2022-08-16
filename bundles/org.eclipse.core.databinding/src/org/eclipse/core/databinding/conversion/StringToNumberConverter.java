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
 *     Michael Scharf - bug 240562
 *     Matt Carter - bug 180392
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 445446
 ******************************************************************************/

package org.eclipse.core.databinding.conversion;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.core.internal.databinding.conversion.AbstractStringToNumberConverter;
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
 * {@link Object} to preserve backwards compatibility, but the argument is meant
 * to always be a {@link String}.
 *
 * @param <T> The type to which values are converted.
 *
 * @since 1.0
 * @deprecated Use
 *             {@link org.eclipse.core.databinding.conversion.text.StringToNumberConverter}
 *             instead, which does not use {@code com.ibm.icu} as that package
 *             is planned to be removed in the future from platform.
 * @noreference This class is not intended to be referenced by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
@Deprecated(forRemoval = true)
public class StringToNumberConverter<T extends Number> extends AbstractStringToNumberConverter<T> {
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
	private StringToNumberConverter(NumberFormat numberFormat, Class<T> toType, Number min, Number max,
			Class<T> boxedType) {
		super(numberFormat, toType, min, max, boxedType);
	}

	/**
	 * @implNote Overridden to avoid API tooling problem.
	 */
	@Override
	public T convert(Object fromObject) {
		return super.convert(fromObject);
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is an int
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a double
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a long
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a float
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
	 * @param numberFormat number format used by the converter
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
	 * @param numberFormat number format used by the converter
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a short
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
	 * @param numberFormat number format used by the converter
	 * @param primitive    <code>true</code> if the convert to type is a byte
	 * @return to Byte converter with the provided numberFormat
	 * @since 1.2
	 */
	public static StringToNumberConverter<Byte> toByte(NumberFormat numberFormat, boolean primitive) {
		return new StringToNumberConverter<>(numberFormat,
				(primitive) ? Byte.TYPE : Byte.class, MIN_BYTE,
				MAX_BYTE, Byte.class);
	}

}
