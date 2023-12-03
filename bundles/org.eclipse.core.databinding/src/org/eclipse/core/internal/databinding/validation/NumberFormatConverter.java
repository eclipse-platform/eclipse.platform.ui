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

package org.eclipse.core.internal.databinding.validation;

import java.text.Format;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * Converter that uses a number format for conversion.
 *
 * @param <F> The type from which values are converted.
 * @param <T> The type to which values are converted.
 *
 * @since 1.0
 */
public abstract class NumberFormatConverter<F, T extends Number> extends Converter<F, T> {
	private final Format numberFormat;

	public NumberFormatConverter(Object fromType, Object toType, Format numberFormat) {
		super(fromType, toType);

		this.numberFormat = numberFormat;
	}

	/**
	 * @return number format
	 */
	/* package */ Format getNumberFormat() {
		return numberFormat;
	}
}
