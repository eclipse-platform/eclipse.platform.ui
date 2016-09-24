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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.eclipse.core.internal.databinding.conversion.StringToShortConverter;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToShortConverterTest {
	private NumberFormat numberFormat;
	private StringToShortConverter converter;

	@Before
	public void setUp() throws Exception {
		numberFormat = NumberFormat.getIntegerInstance();
		converter = StringToShortConverter.toShort(numberFormat, false);
	}

	@Test
	public void testConvertsToShort() throws Exception {
		Short value = Short.valueOf((short) 1);
		Short result = (Short) converter.convert(numberFormat.format(value));

		assertEquals(value, result);
	}

	@Test
	public void testConvertsToShortPrimitive() throws Exception {
		converter = StringToShortConverter.toShort(numberFormat, true);
		Short value = Short.valueOf((short) 1);
		Short result = (Short) converter.convert(numberFormat.format(value));
		assertEquals(value, result);
	}

	@Test
	public void testFromTypeIsString() throws Exception {
		assertEquals(String.class, converter.getFromType());
	}

	@Test
	public void testToTypeIsShort() throws Exception {
		assertEquals(Short.class, converter.getToType());
	}

	@Test
	public void testToTypeIsShortPrimitive() throws Exception {
		converter = StringToShortConverter.toShort(true);
		assertEquals(Short.TYPE, converter.getToType());
	}

	@Test
	public void testReturnsNullBoxedTypeForEmptyString() throws Exception {
		assertNull(converter.convert(""));
	}

	@Test
	public void testThrowsIllegalArgumentExceptionIfAskedToConvertNonString()
			throws Exception {
		try {
			converter.convert(Integer.valueOf(1));
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
