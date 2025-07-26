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

import java.text.Format;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * Base class for number to number converters.
 * <p>
 * This class is thread safe.
 * </p>
 *
 * @param <T>
 *            type of the converted value
 *
 * @since 1.0
 */
public abstract class NumberToNumberConverter<T extends Number> extends Converter<Object, T> {
	private Format numberFormat;

	private boolean primitive;

	private String outOfRangeMessage;

	protected NumberToNumberConverter(Format numberFormat,
			Class<?> fromType, Class<?> toType) {
		super(fromType, toType);
		this.numberFormat = numberFormat;
		this.primitive = toType.isPrimitive();
	}

	@Override
	public final T convert(Object fromObject) {
		if (fromObject == null) {
			if (primitive) {
				throw new IllegalArgumentException(
						"Parameter 'fromObject' cannot be null."); //$NON-NLS-1$
			}

			return null;
		}

		if (!(fromObject instanceof Number)) {
			throw new IllegalArgumentException(
					"Parameter 'fromObject' must be of type Number."); //$NON-NLS-1$
		}

		Number number = (Number) fromObject;
		T result = doConvert(number);

		if (result != null) {
			return result;
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
	 * Invoked when the number should converted.
	 *
	 * @return number if conversion was successful, <code>null</code> if the
	 *         number was out of range
	 */
	protected abstract T doConvert(Number number);

	/**
	 * NumberFormat being used by the converter. Access to the format must be
	 * synchronized on the number format instance.
	 *
	 * @return number format
	 */
	public Format getNumberFormat() {
		return numberFormat;
	}
}
