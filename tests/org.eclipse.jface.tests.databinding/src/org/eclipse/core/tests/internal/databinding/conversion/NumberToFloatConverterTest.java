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

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToFloatConverter;
import org.junit.Before;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToFloatConverterTest extends NumberToNumberTestHarness {
	private NumberFormat numberFormat;

	@Before
	public void setUp() throws Exception {
		numberFormat = NumberFormat.getInstance();
	}

	@Override
	protected Number doGetOutOfRangeNumber() {
		return new Double(Double.MAX_VALUE);
	}

	@Override
	protected IConverter<Object, Float> doGetToBoxedTypeValidator(Class<?> fromType) {
		return new NumberToFloatConverter(numberFormat, fromType, false);
	}

	@Override
	protected IConverter<Object, Float> doGetToPrimitiveValidator(Class<?> fromType) {
		return new NumberToFloatConverter(numberFormat, fromType, true);
	}

	@Override
	protected Class<?> doGetToType(boolean primitive) {
		return (primitive) ? Float.TYPE : Float.class;
	}
}
