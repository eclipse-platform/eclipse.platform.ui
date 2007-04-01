/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.conversion;

import org.eclipse.core.databinding.conversion.Converter;

import com.ibm.icu.text.NumberFormat;

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
public class IntegerToStringConverter extends Converter {
	private final boolean primitive;
	private final NumberFormat numberFormat;
	private final Class boxedType;

	/**
	 * @param numberFormat
	 * @param fromType
	 * @param boxedType
	 */
	private IntegerToStringConverter(NumberFormat numberFormat, Class fromType,
			Class boxedType) {
		super(fromType, String.class);
		this.primitive = fromType.isPrimitive();
		this.numberFormat = numberFormat;
		this.boxedType = boxedType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
	 */
	public Object convert(Object fromObject) {
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
	 * @param primitive
	 * @return converter
	 */
	public static IntegerToStringConverter fromShort(boolean primitive) {
		return fromShort(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return converter
	 */
	public static IntegerToStringConverter fromShort(NumberFormat numberFormat,
			boolean primitive) {
		return new IntegerToStringConverter(numberFormat,
				primitive ? Short.TYPE : Short.class, Short.class);
	}

	/**
	 * @param primitive
	 * @return converter
	 */
	public static IntegerToStringConverter fromByte(boolean primitive) {
		return fromByte(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return converter
	 */
	public static IntegerToStringConverter fromByte(NumberFormat numberFormat,
			boolean primitive) {
		return new IntegerToStringConverter(numberFormat, primitive ? Byte.TYPE
				: Byte.class, Byte.class);
	}
}
