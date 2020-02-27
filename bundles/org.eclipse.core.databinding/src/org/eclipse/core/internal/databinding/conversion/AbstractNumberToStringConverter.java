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
package org.eclipse.core.internal.databinding.conversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;
import java.util.Objects;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * Converts a Number to a String using <code>Format.format(...)</code>. This
 * class is thread safe. This class is used to share code between converters
 * that are based on ICU and java.text.
 *
 * @since 1.9
 */
public class AbstractNumberToStringConverter extends Converter<Object, String> {
	private final Format numberFormat;
	private final Class<?> fromType;
	private boolean fromTypeFitsLong;
	private boolean fromTypeIsDecimalType;
	private boolean fromTypeIsBigInteger;
	private boolean fromTypeIsBigDecimal;

	static Class<?> icuBigDecimal = null;
	static Constructor<?> icuBigDecimalCtr = null;
	static Class<?> icuDecimalFormat = null;

	{
		/*
		 * If the full ICU4J library is available, we use the ICU BigDecimal class to
		 * support proper formatting and parsing of java.math.BigDecimal.
		 *
		 * The version of ICU NumberFormat (DecimalFormat) included in eclipse excludes
		 * support for java.math.BigDecimal, and if used falls back to converting as an
		 * unknown Number type via doubleValue(), which is undesirable.
		 *
		 * See Bug #180392.
		 */
		try {
			icuBigDecimal = Class.forName("com.ibm.icu.math.BigDecimal"); //$NON-NLS-1$
			icuBigDecimalCtr = icuBigDecimal.getConstructor(BigInteger.class, int.class);
			icuDecimalFormat = Class.forName("com.ibm.icu.text.DecimalFormat"); //$NON-NLS-1$
//			System.out.println("DEBUG: Full ICU4J support state: icuBigDecimal="+(icuBigDecimal != null)+", icuBigDecimalCtr="+(icuBigDecimalCtr != null)); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ClassNotFoundException | NoSuchMethodException e) {
		}
	}

	/**
	 * Constructs a new instance.
	 * <p>
	 * Private to restrict public instantiation.
	 * </p>
	 *
	 * @param numberFormat used to format the numbers into strings. Non-null.
	 * @param fromType     type of the source numbers. Non-null.
	 */
	protected AbstractNumberToStringConverter(Format numberFormat, Class<?> fromType) {
		super(fromType, String.class);

		this.numberFormat = Objects.requireNonNull(numberFormat);
		this.fromType = Objects.requireNonNull(fromType);

		if (Integer.class.equals(fromType) || Integer.TYPE.equals(fromType) || Long.class.equals(fromType)
				|| Long.TYPE.equals(fromType) || Short.class.equals(fromType) || Short.TYPE.equals(fromType)
				|| Byte.class.equals(fromType) || Byte.TYPE.equals(fromType)) {
			fromTypeFitsLong = true;
		} else if (Float.class.equals(fromType) || Float.TYPE.equals(fromType) || Double.class.equals(fromType)
				|| Double.TYPE.equals(fromType)) {
			fromTypeIsDecimalType = true;
		} else if (BigInteger.class.equals(fromType)) {
			fromTypeIsBigInteger = true;
		} else if (BigDecimal.class.equals(fromType)) {
			fromTypeIsBigDecimal = true;
		}
	}

	/**
	 * Converts the provided <code>fromObject</code> to a <code>String</code>. If
	 * the converter was constructed for an object type, non primitive, a
	 * <code>fromObject</code> of <code>null</code> will be converted to an empty
	 * string.
	 *
	 * @param fromObject value to convert. May be <code>null</code> if the converter
	 *                   was constructed for a non primitive type.
	 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
	 * @since 1.7
	 */
	@Override
	public String convert(Object fromObject) {
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
				result = numberFormat.format(number);
			}
		} else if (fromTypeIsBigDecimal) {
			if (icuBigDecimal != null && icuBigDecimalCtr != null && icuDecimalFormat != null
					&& icuDecimalFormat.isInstance(numberFormat)) {
				// Full ICU4J present. Convert java.math.BigDecimal to ICU BigDecimal to format.
				// Bug #180392.
				BigDecimal o = (BigDecimal) fromObject;
				try {
					fromObject = icuBigDecimalCtr.newInstance(o.unscaledValue(), Integer.valueOf(o.scale()));
				} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				}
				// Otherwise, replacement plugin present and supports java.math.BigDecimal.
			}
			synchronized (numberFormat) {
				result = numberFormat.format(fromObject);
			}
		}

		return result;
	}
}
