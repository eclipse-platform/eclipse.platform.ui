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

import junit.framework.TestCase;

import org.eclipse.core.internal.databinding.conversion.IntegerToStringConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class IntegerToStringConverterTest extends TestCase {
	private NumberFormat integerFormat;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		integerFormat = NumberFormat.getIntegerInstance();
	}

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

	public void testToTypeIsStringClass() throws Exception {
		assertEquals(String.class, IntegerToStringConverter.fromShort(false)
				.getToType());
	}

	public void testConvertShortToString() throws Exception {
		Short value = new Short((short) 1);
		String expected = integerFormat.format(value);

		IntegerToStringConverter converter = IntegerToStringConverter
				.fromShort(integerFormat, false);
		String result = (String) converter.convert(value);
		assertEquals(expected, result);
	}

	public void testConvertByteToString() throws Exception {
		Byte value = new Byte((byte) 1);
		String expected = integerFormat.format(value);

		IntegerToStringConverter converter = IntegerToStringConverter.fromByte(
				integerFormat, false);
		String result = (String) converter.convert(value);
		assertEquals(expected, result);
	}

	public void testNullSourceConvertsToEmptyString() throws Exception {
		IntegerToStringConverter converter = IntegerToStringConverter
				.fromByte(false);
		assertEquals("", converter.convert(null));
	}

	public void testIllegalArgumentExceptionIfSourceIsNotExpectedType() throws Exception {
		IntegerToStringConverter converter = IntegerToStringConverter.fromByte(false);
		try {
			converter.convert(new Integer(1));
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
