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

package org.eclipse.core.tests.internal.databinding.conversion;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.internal.databinding.conversion.NumberToByteConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToByteConverterTest extends NumberToNumberTestHarness {
	private NumberFormat numberFormat;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		numberFormat = NumberFormat.getInstance();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.NumberToNumberTestHarness#doGetOutOfRangeNumber()
	 */
	protected Number doGetOutOfRangeNumber() {
		return new Integer(Byte.MAX_VALUE + 1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.NumberToNumberTestHarness#doGetToBoxedTypeValidator(java.lang.Class)
	 */
	protected IConverter doGetToBoxedTypeValidator(Class fromType) {
		return new NumberToByteConverter(numberFormat, fromType, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.NumberToNumberTestHarness#doGetToPrimitiveValidator(java.lang.Class)
	 */
	protected IConverter doGetToPrimitiveValidator(Class fromType) {
		return new NumberToByteConverter(numberFormat, fromType, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.NumberToNumberTestHarness#doGetToType()
	 */
	protected Class doGetToType(boolean primitive) {
		return (primitive) ? Byte.TYPE : Byte.class;
	}
}
