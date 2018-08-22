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
 ******************************************************************************/

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
public class StringToShortConverter extends NumberFormatConverter<Object, Short> {
	private final NumberFormat numberFormat;
	private final boolean primitive;

	private String outOfRangeMessage;

	/**
	 * Constructs a new instance.
	 */
	private StringToShortConverter(NumberFormat numberFormat, Class<?> toType) {
		super(String.class, toType, numberFormat);
		this.numberFormat = numberFormat;
		primitive = toType.isPrimitive();
	}

	@Override
	public Short convert(Object fromObject) {
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

		if (StringToNumberParser.inShortRange(result.getNumber())) {
			return Short.valueOf(result.getNumber().shortValue());
		}

		synchronized (this) {
			if (outOfRangeMessage == null) {
				outOfRangeMessage = StringToNumberParser
						.createOutOfRangeMessage(Short.valueOf(Short.MIN_VALUE), Short.valueOf(Short.MAX_VALUE),
								numberFormat);
			}

			throw new IllegalArgumentException(outOfRangeMessage);
		}
	}

	/**
	 * @param primitive
	 *            <code>true</code> if the convert to type is a short
	 * @return to Short converter for the default locale
	 */
	public static StringToShortConverter toShort(boolean primitive) {
		return toShort(NumberFormat.getIntegerInstance(), primitive);
	}

	/**
	 * @param numberFormat
	 * @param primitive
	 * @return to Short converter with the provided numberFormat
	 */
	public static StringToShortConverter toShort(NumberFormat numberFormat,
			boolean primitive) {
		return new StringToShortConverter(numberFormat,
				(primitive) ? Short.TYPE : Short.class);
	}
}
