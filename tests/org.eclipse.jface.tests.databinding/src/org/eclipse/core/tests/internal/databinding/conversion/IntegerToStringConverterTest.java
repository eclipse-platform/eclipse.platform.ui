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

package org.eclipse.core.tests.internal.databinding.conversion;

import org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.text.NumberFormat;

import junit.framework.TestCase;

/**
 * @since 1.1
 */
public class IntegerToStringConverterTest extends TestCase {
	private NumberFormat integerFormat;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		integerFormat = NumberFormat.getIntegerInstance();
	}

	@Test
	public void testFromTypeShort() throws Exception {
		assertEquals(Short.class, IntegerToStringConverter.fromShort(false)
				.getFromType());
		assertEquals(Short.TYPE, IntegerToStringConverter.fromShort(true)
				.getFromType());
		assertEquals(Byte.class, IntegerToStringConverter.fromByte(false)
				.getFromType());
		assertEquals(Byte.TYPE, IntegerToStringConverter.fromByte(true)
				.getFromType());
	}

	@Test
	public void testToTypeIsStringClass() throws Exception {
		assertEquals(String.class, IntegerToStringConverter.fromShort(false)
				.getToType());
	}

	@Test
	public void testConvertShortToString() throws Exception {
		Short value = Short.valueOf((short) 1);
		String expected = integerFormat.format(value);

		IntegerToStringConverter converter = IntegerToStringConverter
				.fromShort(integerFormat, false);
		String result = converter.convert(value);
		assertEquals(expected, result);
	}

	@Test
	public void testConvertByteToString() throws Exception {
		Byte value = Byte.valueOf((byte) 1);
		String expected = integerFormat.format(value);

		IntegerToStringConverter converter = IntegerToStringConverter.fromByte(
				integerFormat, false);
		String result = converter.convert(value);
		assertEquals(expected, result);
	}

	@Test
	public void testNullSourceConvertsToEmptyString() throws Exception {
		IntegerToStringConverter converter = IntegerToStringConverter
				.fromByte(false);
		assertEquals("", converter.convert(null));
	}

	@Test
	public void testIllegalArgumentExceptionIfSourceIsNotExpectedType() throws Exception {
		IntegerToStringConverter converter = IntegerToStringConverter.fromByte(false);
		try {
			converter.convert(Integer.valueOf(1));
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
