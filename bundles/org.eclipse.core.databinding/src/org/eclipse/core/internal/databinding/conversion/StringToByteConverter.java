/*
 * Copyright (C) 2005, 2015 db4objects Inc.  http://www.db4o.com
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.core.internal.databinding.conversion;

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser.ParseResult;
import org.eclipse.core.internal.databinding.validation.NumberFormatConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * Note that this class does not have precise type parameters because it
 * manually handles argument type mismatches and throws
 * {@link IllegalArgumentException}.
 *
 * @since 1.0
 */
public class StringToByteConverter extends NumberFormatConverter<Object, Byte> {
	private String outOfRangeMessage;
	private NumberFormat numberFormat;
	private boolean primitive;

	/**
	 * @param numberFormat
	 * @param toType
	 */
	private StringToByteConverter(NumberFormat numberFormat, Class<?> toType) {
		super(String.class, toType, numberFormat);
		primitive = toType.isPrimitive();
		this.numberFormat = numberFormat;
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return converter
	 */
	public static StringToByteConverter toByte(NumberFormat numberFormat, boolean primitive) {
		return new StringToByteConverter(numberFormat, (primitive) ? Byte.TYPE : Byte.class);
	}

	/**
	 * @param primitive
	 * @return converter
	 */
	public static StringToByteConverter toByte(boolean primitive) {
		return toByte(NumberFormat.getIntegerInstance(), primitive);
	}

	@Override
	public Byte convert(Object fromObject) {
		ParseResult result = StringToNumberParser.parse(fromObject,
				numberFormat, primitive);

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

		if (StringToNumberParser.inByteRange(result.getNumber())) {
			return Byte.valueOf(result.getNumber().byteValue());
		}

		synchronized (this) {
			if (outOfRangeMessage == null) {
				outOfRangeMessage = StringToNumberParser
						.createOutOfRangeMessage(Byte.valueOf(Byte.MIN_VALUE), Byte.valueOf(Byte.MAX_VALUE),
								numberFormat);
			}

			throw new IllegalArgumentException(outOfRangeMessage);
		}
	}
}
