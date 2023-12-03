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
 ******************************************************************************/

package org.eclipse.core.internal.databinding.conversion;

import java.text.Format;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * Converts a value that is an integer, non decimal, to a String using a
 * NumberFormat.
 * <p>
 * This class is a temporary as this ability exists in NumberToStringConverter
 * except that short and byte are missing.
 * </p>
 *
 * @since 1.0
 */
public class IntegerToStringConverter extends Converter<Object, String> {
	private final boolean primitive;
	private final Format numberFormat;
	private final Class<?> boxedType;

	private IntegerToStringConverter(Format numberFormat, Class<?> fromType, Class<?> boxedType) {
		super(fromType, String.class);
		this.primitive = fromType.isPrimitive();
		this.numberFormat = numberFormat;
		this.boxedType = boxedType;
	}

	@Override
	public String convert(Object fromObject) {
		// Null is allowed when the type is not primitve.
		if (fromObject == null && !primitive) {
			return ""; //$NON-NLS-1$
		}

		if (!boxedType.isInstance(fromObject)) {
			throw new IllegalArgumentException(
					"'fromObject' is not of type [" + boxedType + "]."); //$NON-NLS-1$//$NON-NLS-2$
		}

		return numberFormat.format(((Number) fromObject).longValue());
	}

	/**
	 * @return converter
	 */
	public static IntegerToStringConverter fromShort(boolean primitive) {
		return fromShort(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @return converter
	 */
	public static IntegerToStringConverter fromShort(Format numberFormat,
			boolean primitive) {
		return new IntegerToStringConverter(numberFormat,
				primitive ? Short.TYPE : Short.class, Short.class);
	}

	/**
	 * @return converter
	 */
	public static IntegerToStringConverter fromByte(boolean primitive) {
		return fromByte(StringToNumberParser.getDefaultIntegerFormat(), primitive);
	}

	/**
	 * @return converter
	 */
	public static IntegerToStringConverter fromByte(Format numberFormat,
			boolean primitive) {
		return new IntegerToStringConverter(numberFormat, primitive ? Byte.TYPE
				: Byte.class, Byte.class);
	}
}
