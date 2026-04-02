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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;
import java.util.Objects;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * Converts a Number to a String using <code>Format.format(...)</code>. This
 * class is thread safe.
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
			synchronized (numberFormat) {
				result = numberFormat.format(fromObject);
			}
		}

		return result;
	}
}
