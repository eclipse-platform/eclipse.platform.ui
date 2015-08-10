/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Carter - Bug 180392
 ******************************************************************************/

package org.eclipse.core.tests.databinding.conversion;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;

import junit.framework.TestCase;

import org.eclipse.core.databinding.conversion.StringToNumberConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToNumberConverterTest extends TestCase {
	private NumberFormat numberFormat;
	private NumberFormat numberIntegerFormat;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(305); // Used for BigDecimal test
		numberFormat.setGroupingUsed(false); // Not really needed
		numberIntegerFormat = NumberFormat.getIntegerInstance();
	}

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

	public void testFromTypeIsString() throws Exception {
		assertEquals(String.class, StringToNumberConverter.toInteger(false)
				.getFromType());
	}

	public void testConvertsToBigInteger() throws Exception {
		BigInteger input = BigInteger.valueOf(1000);

		StringToNumberConverter converter = StringToNumberConverter.toBigInteger();
		BigInteger result = (BigInteger) converter.convert(numberFormat.format(input));

		assertEquals(input, result);
	}

	Class icuBigDecimal = null;
	Constructor icuBigDecimalCtr = null;
	{
		try {
			icuBigDecimal = Class.forName("com.ibm.icu.math.BigDecimal");
			icuBigDecimalCtr = icuBigDecimal.getConstructor(new Class[] {BigInteger.class, int.class});
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
			Number icubd = (Number) icuBigDecimalCtr.newInstance(
					new Object[] { javabd.unscaledValue(), new Integer(javabd.scale()) });
			return numberFormat.format(icubd);
		}
		throw new IllegalArgumentException("ICU not present. Cannot reliably format large BigDecimal values; needed for testing. Java platforms prior to 1.5 fail to format/parse these decimals correctly.");
	}

	public void testConvertsToBigDecimal() throws Exception {
		StringToNumberConverter converter = StringToNumberConverter.toBigDecimal();
		// Test 1: Decimal
		BigDecimal input = new BigDecimal("100.23");
		BigDecimal result = (BigDecimal) converter.convert(formatBigDecimal(input));
		assertEquals("Non-integer BigDecimal", input, result);

		// Test 2: Long
		input = new BigDecimal(Integer.MAX_VALUE + 100L);
		result = (BigDecimal) converter.convert(formatBigDecimal(input));
		assertEquals("Integral BigDecimal in long range", input, result);

		// Test 3: BigInteger range
		input = new BigDecimal("92233720368547990480");
		result = (BigDecimal) converter.convert(formatBigDecimal(input));
		assertEquals("Integral BigDecimal in long range", input, result);

		// Test 4: Very high precision Decimal.
		input = new BigDecimal("100404101.23345678345678893456789345678923198200134567823456789");
		result = (BigDecimal) converter.convert(formatBigDecimal(input));
		assertEquals("Non-integer BigDecimal", input, result);
	}

	public void testConvertsToInteger() throws Exception {
		Integer input = new Integer(1000);

		StringToNumberConverter converter = StringToNumberConverter.toInteger(false);
		Integer result = (Integer) converter.convert(numberIntegerFormat.format(input
				.longValue()));
		assertEquals(input, result);
	}

	public void testConvertsToDouble() throws Exception {
		Double input = new Double(1000);

		StringToNumberConverter converter = StringToNumberConverter.toDouble(false);
		Double result = (Double) converter.convert(numberFormat.format(input
				.doubleValue()));

		assertEquals(input, result);
	}

	public void testConvertsToLong() throws Exception {
		Long input = new Long(1000);

		StringToNumberConverter converter = StringToNumberConverter.toLong(false);
		Long result = (Long) converter.convert(numberIntegerFormat.format(input
				.longValue()));

		assertEquals(input, result);
	}

	public void testConvertsToFloat() throws Exception {
		Float input = new Float(1000);

		StringToNumberConverter converter = StringToNumberConverter.toFloat(false);
		Float result = (Float) converter.convert(numberFormat.format(input
				.floatValue()));

		assertEquals(input, result);
	}

	public void testConvertedToIntegerPrimitive() throws Exception {
		Integer input = new Integer(1000);

		StringToNumberConverter converter = StringToNumberConverter.toInteger(true);
		Integer result = (Integer) converter.convert(numberIntegerFormat.format(input
				.longValue()));
		assertEquals(input, result);
	}

	public void testConvertsToDoublePrimitive() throws Exception {
		Double input = new Double(1000);

		StringToNumberConverter converter = StringToNumberConverter.toDouble(true);
		Double result = (Double) converter.convert(numberFormat.format(input
				.doubleValue()));

		assertEquals(input, result);
	}

	public void testConvertsToLongPrimitive() throws Exception {
		Long input = new Long(1000);

		StringToNumberConverter converter = StringToNumberConverter.toLong(true);
		Long result = (Long) converter.convert(numberIntegerFormat.format(input
				.longValue()));

		assertEquals(input, result);
	}

	public void testConvertsToFloatPrimitive() throws Exception {
		Float input = new Float(1000);

		StringToNumberConverter converter = StringToNumberConverter.toFloat(true);
		Float result = (Float) converter.convert(numberFormat.format(input
				.floatValue()));

		assertEquals(input, result);
	}

	public void testReturnsNullBoxedTypeForEmptyString() throws Exception {
		StringToNumberConverter converter = StringToNumberConverter.toInteger(false);
		try {
			assertNull(converter.convert(""));
		} catch (Exception e) {
			fail("exception should not have been thrown");
		}
	}

	public void testThrowsIllegalArgumentExceptionIfAskedToConvertNonString()
			throws Exception {
		StringToNumberConverter converter = StringToNumberConverter.toInteger(false);
		try {
			converter.convert(new Integer(1));
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
	public void testInvalidInteger() throws Exception {
		StringToNumberConverter converter = StringToNumberConverter
				.toInteger(false);

		try {
			Object result = converter.convert("1 1 -1");
			fail("exception should have been thrown, but result was " + result);
		} catch (IllegalArgumentException e) {
		}
	}

	public void testThrowsIllegalArgumentExceptionIfNumberIsOutOfRange() throws Exception {
		StringToNumberConverter converter = StringToNumberConverter.toInteger(false);
		try {
			converter.convert(numberFormat.format(Long.MAX_VALUE));
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}
