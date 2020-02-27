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
 *     Matt Carter - Bug 180392
 ******************************************************************************/

package org.eclipse.core.tests.databinding.conversion;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;

import org.eclipse.core.databinding.conversion.text.NumberToStringConverter;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class NumberToStringConverterTest {
	private Format numberFormat;
	private Format integerFormat;

	@Before
	public void setUp() throws Exception {
		numberFormat = StringToNumberParser.getDefaultNumberFormat();
		integerFormat = StringToNumberParser.getDefaultIntegerFormat();
	}

	@Test
	public void testFromTypes() throws Exception {
		assertEquals("Integer.class", Integer.class, NumberToStringConverter
				.fromInteger(false).getFromType());
		assertEquals("Integer.TYPE", Integer.TYPE, NumberToStringConverter
				.fromInteger(true).getFromType());
		assertEquals("Double.class", Double.class, NumberToStringConverter
				.fromDouble(false).getFromType());
		assertEquals("Double.TYPE", Double.TYPE, NumberToStringConverter
				.fromDouble(true).getFromType());
		assertEquals("Long.class", Long.class, NumberToStringConverter
				.fromLong(false).getFromType());
		assertEquals("Long.TYPE", Long.TYPE, NumberToStringConverter.fromLong(
				true).getFromType());
		assertEquals("Float.class", Float.class, NumberToStringConverter
				.fromFloat(false).getFromType());
		assertEquals("Float.TYPE", Float.TYPE, NumberToStringConverter
				.fromFloat(true).getFromType());
		assertEquals("BigInteger.class", BigInteger.class,
				NumberToStringConverter.fromBigInteger().getFromType());
		assertEquals("BigDecimal.class", BigDecimal.class,
				NumberToStringConverter.fromBigDecimal().getFromType());
		assertEquals("Short.class", Short.class,
				NumberToStringConverter.fromShort(false).getFromType());
		assertEquals("Byte.class", Byte.class,
				NumberToStringConverter.fromByte(false).getFromType());
	}

	@Test
	public void testToTypeIsStringClass() throws Exception {
		assertEquals(String.class, NumberToStringConverter.fromInteger(false)
				.getToType());
	}

	@Test
	public void testConvertIntegerToString() throws Exception {
		Integer input = Integer.valueOf(1000);
		String expected = integerFormat.format(input.longValue());

		NumberToStringConverter converter = NumberToStringConverter.fromInteger(false);
		String result = converter.convert(input);
		assertEquals(expected, result);
	}

	@Test
	public void testConvertDoubleToString() throws Exception {
		Double input = Double.valueOf(1000.1d);
		String expected = numberFormat.format(input.doubleValue());

		NumberToStringConverter converter = NumberToStringConverter
				.fromDouble(false);
		String result = converter.convert(input);
		assertEquals(expected, result);
	}

	@Test
	public void testConvertFloatToString() throws Exception {
		Float input = Float.valueOf(1000.1f);
		String expected = numberFormat.format(input.floatValue());

		NumberToStringConverter converter = NumberToStringConverter
				.fromFloat(false);
		String result = converter.convert(input);
		assertEquals(expected, result);
	}

	@Test
	public void testConvertLongToString() throws Exception {
		Long input = Long.valueOf(1000l);
		String expected = integerFormat.format(input.longValue());

		NumberToStringConverter converter = NumberToStringConverter
				.fromLong(false);
		String result = converter.convert(input);
		assertEquals(expected, result);
	}

	@Test
	public void testConvertBigIntegerToString() throws Exception {
		BigInteger input = BigInteger.valueOf(1000);
		String expected = integerFormat.format(input);

		NumberToStringConverter converter = NumberToStringConverter.fromBigInteger();
		String result = converter.convert(input);
		assertEquals(expected, result);
	}

	Class<?> icuBigDecimal = null;
	Constructor<?> icuBigDecimalCtr = null;
	{
		try {
			icuBigDecimal = Class.forName("com.ibm.icu.math.BigDecimal");
			icuBigDecimalCtr = icuBigDecimal.getConstructor(BigInteger.class, int.class);
		}
		catch(ClassNotFoundException | NoSuchMethodException e) {}
	}

	@Test
	public void testConvertBigDecimalToString() throws Exception {
		NumberToStringConverter converter = NumberToStringConverter.fromBigDecimal();
		// Test 1: Decimal
		BigDecimal input = new BigDecimal("100.23");
		String expected = numberFormat.format(input);
		String result = converter.convert(input);
		assertEquals("Non-integer BigDecimal", expected, result);

		// Test 2: Long
		input = BigDecimal.valueOf(Integer.MAX_VALUE + 100L);
		expected = numberFormat.format(input);
		result = converter.convert(input);
		assertEquals("Integral BigDecimal in long range", expected, result);

		// Test 3: BigInteger range
		input = new BigDecimal(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(100L)));
		expected = numberFormat.format(input);
		result = converter.convert(input);
		assertEquals("Integral BigDecimal in BigInteger range", expected, result);

		// Test 4: Very high precision Decimal
		input = new BigDecimal("100404101.233456783456788934567893456789231982001345678234567890");
		expected = numberFormat.format(input);
		result = converter.convert(input);
		assertEquals("High-precision BigDecimal", expected, result);

	}

	@Test
	public void testNullSourceConvertsToEmptyString() throws Exception {
		NumberToStringConverter converter = NumberToStringConverter
				.fromInteger(false);
		assertEquals("", converter.convert(null));
	}
}
