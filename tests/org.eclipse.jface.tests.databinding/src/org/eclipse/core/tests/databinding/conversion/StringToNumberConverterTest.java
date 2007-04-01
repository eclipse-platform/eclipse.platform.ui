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

package org.eclipse.core.tests.databinding.conversion;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.eclipse.core.databinding.conversion.StringToNumberConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
//TODO test for ICU4Js BigDecimal
public class StringToNumberConverterTest extends TestCase {
	private NumberFormat numberFormat;
	private NumberFormat numberIntegerFormat;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		numberFormat = NumberFormat.getInstance();
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
			converter.convert("1 1 1");
			fail("exception should have been thrown");
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
