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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;
import java.text.NumberFormat;

import org.eclipse.core.databinding.conversion.text.StringToNumberConverter;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class StringToNumberConverterTest {
	private Format numberFormat;
	private Format numberIntegerFormat;

	@Before
	public void setUp() throws Exception {
		numberFormat = StringToNumberParser.getDefaultNumberFormat();

		// Use reflection to work for both ICU and java.text
		numberFormat.getClass().getMethod("setMaximumFractionDigits", int.class).invoke(numberFormat, 305);
		numberFormat.getClass().getMethod("setParseBigDecimal", boolean.class).invoke(numberFormat, true);

		numberIntegerFormat = NumberFormat.getIntegerInstance();
	}


	@Test
	public void testToTypes() throws Exception {
		assertEquals("Integer.class", Integer.class, StringToNumberConverter.toInteger(false).getToType());
		assertEquals("Integer.TYPE", Integer.TYPE, StringToNumberConverter.toInteger(true).getToType());
		assertEquals("Double.class", Double.class, StringToNumberConverter.toDouble(false).getToType());
		assertEquals("Double.TYPE", Double.TYPE, StringToNumberConverter.toDouble(true).getToType());
		assertEquals("Long.class", Long.class, StringToNumberConverter.toLong(false).getToType());
		assertEquals("Long.TYPE", Long.TYPE, StringToNumberConverter.toLong(true).getToType());
		assertEquals("Float.class", Float.class, StringToNumberConverter.toFloat(false).getToType());
		assertEquals("Float.TYPE", Float.TYPE, StringToNumberConverter.toFloat(true).getToType());
		assertEquals("BigInteger.TYPE", BigInteger.class, StringToNumberConverter.toBigInteger().getToType());
		assertEquals("BigDecimal.TYPE", BigDecimal.class, StringToNumberConverter.toBigDecimal().getToType());
		assertEquals("Short.class", Short.class, StringToNumberConverter.toShort(false).getToType());
		assertEquals("Short.TYPE", Short.TYPE, StringToNumberConverter.toShort(true).getToType());
		assertEquals("Byte.class", Byte.class, StringToNumberConverter.toByte(false).getToType());
		assertEquals("Byte.TYPE", Byte.TYPE, StringToNumberConverter.toByte(true).getToType());
	}

	@Test
	public void testFromTypeIsString() throws Exception {
		assertEquals(String.class, StringToNumberConverter.toInteger(false)
				.getFromType());
	}

	@Test
	public void testConvertsToBigInteger() throws Exception {
		BigInteger input = BigInteger.valueOf(1000);

		StringToNumberConverter<BigInteger> converter = StringToNumberConverter.toBigInteger();
		BigInteger result = converter.convert(numberFormat.format(input));

		assertEquals(input, result);
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
	public void testConvertsToBigDecimal() throws Exception {
		StringToNumberConverter<BigDecimal> converter = StringToNumberConverter.toBigDecimal(numberFormat);
		// Test 1: Decimal
		BigDecimal input = new BigDecimal("100.23");
		BigDecimal result = converter.convert(numberFormat.format(input));
		assertEquals("Non-integer BigDecimal", input, result);

		// Test 2: Long
		input = BigDecimal.valueOf(Integer.MAX_VALUE + 100L);
		result = converter.convert(numberFormat.format(input));
		assertEquals("Integral BigDecimal in long range", input, result);

		// Test 3: BigInteger range
		input = new BigDecimal("92233720368547990480");
		result = converter.convert(numberFormat.format(input));
		assertEquals("Integral BigDecimal in long range", input, result);

		// Test 4: Very high precision Decimal.
		input = new BigDecimal("100404101.23345678345678893456789345678923198200134567823456789");
		result = converter.convert(numberFormat.format(input));
		assertEquals("Non-integer BigDecimal", input, result);
	}

	@Test
	public void testConvertsToInteger() throws Exception {
		Integer input = Integer.valueOf(1000);

		StringToNumberConverter<Integer> converter = StringToNumberConverter.toInteger(false);
		Integer result = converter.convert(numberIntegerFormat.format(input.longValue()));
		assertEquals(input, result);
	}

	@Test
	public void testConvertsToDouble() throws Exception {
		Double input = Double.valueOf(1000);

		StringToNumberConverter<Double> converter = StringToNumberConverter.toDouble(false);
		Double result = converter.convert(numberFormat.format(input.doubleValue()));

		assertEquals(input, result);
	}

	@Test
	public void testConvertsToLong() throws Exception {
		Long input = Long.valueOf(1000);

		StringToNumberConverter<Long> converter = StringToNumberConverter.toLong(false);
		Long result = converter.convert(numberIntegerFormat.format(input.longValue()));

		assertEquals(input, result);
	}

	@Test
	public void testConvertsToFloat() throws Exception {
		Float input = Float.valueOf(1000);

		StringToNumberConverter<Float> converter = StringToNumberConverter.toFloat(false);
		Float result = converter.convert(numberFormat.format(input.floatValue()));

		assertEquals(input, result);
	}

	@Test
	public void testConvertedToIntegerPrimitive() throws Exception {
		Integer input = Integer.valueOf(1000);

		StringToNumberConverter<Integer> converter = StringToNumberConverter.toInteger(true);
		Integer result = converter.convert(numberIntegerFormat.format(input.longValue()));
		assertEquals(input, result);
	}

	@Test
	public void testConvertsToDoublePrimitive() throws Exception {
		Double input = Double.valueOf(1000);

		StringToNumberConverter<Double> converter = StringToNumberConverter.toDouble(true);
		Double result = converter.convert(numberFormat.format(input.doubleValue()));

		assertEquals(input, result);
	}

	@Test
	public void testConvertsToLongPrimitive() throws Exception {
		Long input = Long.valueOf(1000);

		StringToNumberConverter<Long> converter = StringToNumberConverter.toLong(true);
		Long result = converter.convert(numberIntegerFormat.format(input.longValue()));

		assertEquals(input, result);
	}

	@Test
	public void testConvertsToFloatPrimitive() throws Exception {
		Float input = Float.valueOf(1000);

		StringToNumberConverter<Float> converter = StringToNumberConverter.toFloat(true);
		Float result = converter.convert(numberFormat.format(input.floatValue()));

		assertEquals(input, result);
	}

	@Test
	public void testReturnsNullBoxedTypeForEmptyString() throws Exception {
		StringToNumberConverter<Integer> converter = StringToNumberConverter.toInteger(false);
		try {
			assertNull(converter.convert(""));
		} catch (Exception e) {
			fail("exception should not have been thrown");
		}
	}

	@Test
	public void testThrowsIllegalArgumentExceptionIfAskedToConvertNonString() throws Exception {
		StringToNumberConverter<Integer> converter = StringToNumberConverter.toInteger(false);
		try {
			converter.convert(1);
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Asserts a use case where the integer starts with a valid value but ends
	 * in an unparsable format.
	 *
	 * @throws Exception
	 */
	@Test
	public void testInvalidInteger() throws Exception {
		StringToNumberConverter<Integer> converter = StringToNumberConverter.toInteger(false);

		try {
			Object result = converter.convert("1 1 -1");
			fail("exception should have been thrown, but result was " + result);
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testThrowsIllegalArgumentExceptionIfNumberIsOutOfRange() throws Exception {
		StringToNumberConverter<Integer> converter = StringToNumberConverter.toInteger(false);
		try {
			converter.convert(numberFormat.format(Long.MAX_VALUE));
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
