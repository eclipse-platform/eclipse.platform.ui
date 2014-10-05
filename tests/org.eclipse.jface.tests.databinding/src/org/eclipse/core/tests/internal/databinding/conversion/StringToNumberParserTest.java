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

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser.ParseResult;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToNumberParserTest extends TestCase {
	private NumberFormat integerFormat;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		integerFormat = NumberFormat.getIntegerInstance();
	}

	public void testParseNonStringThrowsIllegalArgumentException()
			throws Exception {
		try {
			StringToNumberParser.parse(new Integer(0), integerFormat, false);
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	public void testEmptyStringReturnsNullIfNotPrimitive() throws Exception {
		ParseResult result = StringToNumberParser.parse("",
				integerFormat, false);
		assertNull(result.getNumber());
	}

	public void testReturnsParsePositionWhenValueCannotBeParsed()
			throws Exception {
		ParseResult result = StringToNumberParser.parse("adsf",
				integerFormat, false);
		assertNotNull(result.getPosition());
		assertNull(result.getNumber());
	}

	public void testReturnsNumberWhenSuccessfullyParsed() throws Exception {
		Integer number = new Integer(5);
		ParseResult result = StringToNumberParser.parse(integerFormat
				.format(number.longValue()), integerFormat, false);
		assertNull(result.getPosition());
		assertEquals(number.intValue(), result.getNumber().intValue());
	}
}
