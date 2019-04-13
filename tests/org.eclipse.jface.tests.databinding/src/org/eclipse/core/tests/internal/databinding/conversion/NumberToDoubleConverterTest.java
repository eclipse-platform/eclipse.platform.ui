/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

package org.eclipse.core.tests.internal.databinding.conversion;

import java.math.BigDecimal;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToDoubleConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToDoubleConverterTest extends NumberToNumberTestHarness {
	@Override
	protected Number doGetOutOfRangeNumber() {
		return BigDecimal.valueOf(Double.MAX_VALUE).add(BigDecimal.valueOf(Double.MAX_VALUE));
	}

	@Override
	protected IConverter<Object, Double> doGetToBoxedTypeValidator(Class<?> fromType) {
		return new NumberToDoubleConverter(NumberFormat.getInstance(), fromType, false);
	}

	@Override
	protected IConverter<Object, Double> doGetToPrimitiveValidator(Class<?> fromType) {
		return new NumberToDoubleConverter(NumberFormat.getInstance(), fromType, true);
	}

	@Override
	protected Class<?> doGetToType(boolean primitive) {
		return (primitive) ? Double.TYPE : Double.class;
	}
}
