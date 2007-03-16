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

import org.eclipse.core.databinding.conversion.NumberToStringConverter;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class NumberToStringConverterTest extends TestCase {
	private NumberFormat numberFormat;
	private NumberFormat integerFormat;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		numberFormat = NumberFormat.getInstance();
		integerFormat = NumberFormat.getIntegerInstance();
	}

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
	}

	public void testToTypeIsStringClass() throws Exception {
		assertEquals(String.class, NumberToStringConverter.fromInteger(false)
				.getToType());
	}

	public void testConvertIntegerToString() throws Exception {
		Integer input = new Integer(1000);
		String expected = integerFormat.format(input.longValue());

		NumberToStringConverter converter = NumberToStringConverter
				.fromInteger(false);
		String result = (String) converter.convert(input);
		assertEquals(expected, result);
	}

	public void testConvertDoubleToString() throws Exception {
		Double input = new Double(1000.1d);
		String expected = numberFormat.format(input.doubleValue());

		NumberToStringConverter converter = NumberToStringConverter
				.fromDouble(false);
		String result = (String) converter.convert(input);
		assertEquals(expected, result);
	}

	public void testConvertFloatToString() throws Exception {
		Float input = new Float(1000.1f);
		String expected = numberFormat.format(input.floatValue());

		NumberToStringConverter converter = NumberToStringConverter
				.fromFloat(false);
		String result = (String) converter.convert(input);
		assertEquals(expected, result);
	}

	public void testConvertLongToString() throws Exception {
		Long input = new Long(1000l);
		String expected = integerFormat.format(input.longValue());

		NumberToStringConverter converter = NumberToStringConverter
				.fromLong(false);
		String result = (String) converter.convert(input);
		assertEquals(expected, result);
	}
	
	public void testConvertBigIntegerToString() throws Exception {
		BigInteger input = BigInteger.valueOf(1000);
		String expected = integerFormat.format(input);
		
		NumberToStringConverter converter = NumberToStringConverter.fromBigInteger();
		String result = (String) converter.convert(input);
		assertEquals(expected, result);
	}

	public void testNullSourceConvertsToEmptyString() throws Exception {
		NumberToStringConverter converter = NumberToStringConverter
				.fromInteger(false);
		assertEquals("", converter.convert(null));
	}
}
