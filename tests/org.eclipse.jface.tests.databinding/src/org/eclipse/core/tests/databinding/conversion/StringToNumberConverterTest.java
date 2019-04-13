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

import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToNumberConverterTest {
	private NumberFormat numberFormat;
	private NumberFormat numberIntegerFormat;

	@Before
	public void setUp() throws Exception {
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(305); // Used for BigDecimal test
		numberFormat.setGroupingUsed(false); // Not really needed
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
		catch(ClassNotFoundException e) {}
		catch(NoSuchMethodException e) {}
	}
	/**
	 * Takes a java.math.BigDecimal and returns an ICU formatted string for it.
	 * These tests depend on ICU to reliably format test strings for comparison.
	 * Java < 1.5 DecimalFormat did not format/parse BigDecimals properly,
	 * converting them via doubleValue(), so we have a dependency for this unit test on ICU4J.
	 * See Bug #180392 for more info.
	 * @param bd
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	private String formatBigDecimal(BigDecimal javabd) throws Exception {
		if(icuBigDecimal != null && icuBigDecimalCtr != null) {
			// ICU Big Decimal constructor available
			Number icubd = (Number) icuBigDecimalCtr.newInstance(javabd.unscaledValue(),
					Integer.valueOf(javabd.scale()));
			return numberFormat.format(icubd);
		}
		throw new IllegalArgumentException("ICU not present. Cannot reliably format large BigDecimal values; needed for testing. Java platforms prior to 1.5 fail to format/parse these decimals correctly.");
	}

	@Test
	public void testConvertsToBigDecimal() throws Exception {
		StringToNumberConverter<BigDecimal> converter = StringToNumberConverter.toBigDecimal();
		// Test 1: Decimal
		BigDecimal input = new BigDecimal("100.23");
		BigDecimal result = converter.convert(formatBigDecimal(input));
		assertEquals("Non-integer BigDecimal", input, result);

		// Test 2: Long
		input = BigDecimal.valueOf(Integer.MAX_VALUE + 100L);
		result = converter.convert(formatBigDecimal(input));
		assertEquals("Integral BigDecimal in long range", input, result);

		// Test 3: BigInteger range
		input = new BigDecimal("92233720368547990480");
		result = converter.convert(formatBigDecimal(input));
		assertEquals("Integral BigDecimal in long range", input, result);

		// Test 4: Very high precision Decimal.
		input = new BigDecimal("100404101.23345678345678893456789345678923198200134567823456789");
		result = converter.convert(formatBigDecimal(input));
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
		Double input = new Double(1000);

		StringToNumberConverter<Double> converter = StringToNumberConverter.toDouble(false);
		Double result = converter.convert(numberFormat.format(input.doubleValue()));

		assertEquals(input, result);
	}

	@Test
	public void testConvertsToLong() throws Exception {
		Long input = new Long(1000);

		StringToNumberConverter<Long> converter = StringToNumberConverter.toLong(false);
		Long result = converter.convert(numberIntegerFormat.format(input.longValue()));

		assertEquals(input, result);
	}

	@Test
	public void testConvertsToFloat() throws Exception {
		Float input = new Float(1000);

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
		Double input = new Double(1000);

		StringToNumberConverter<Double> converter = StringToNumberConverter.toDouble(true);
		Double result = converter.convert(numberFormat.format(input.doubleValue()));

		assertEquals(input, result);
	}

	@Test
	public void testConvertsToLongPrimitive() throws Exception {
		Long input = new Long(1000);

		StringToNumberConverter<Long> converter = StringToNumberConverter.toLong(true);
		Long result = converter.convert(numberIntegerFormat.format(input.longValue()));

		assertEquals(input, result);
	}

	@Test
	public void testConvertsToFloatPrimitive() throws Exception {
		Float input = new Float(1000);

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
