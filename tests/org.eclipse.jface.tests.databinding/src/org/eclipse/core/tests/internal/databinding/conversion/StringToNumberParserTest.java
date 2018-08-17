/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;
import org.eclipse.core.internal.databinding.conversion.StringToNumberParser.ParseResult;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToNumberParserTest {
	private NumberFormat integerFormat;

	@Before
	public void setUp() throws Exception {
		integerFormat = NumberFormat.getIntegerInstance();
	}

	@Test
	public void testParseNonStringThrowsIllegalArgumentException()
			throws Exception {
		try {
			StringToNumberParser.parse(Integer.valueOf(0), integerFormat, false);
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testEmptyStringReturnsNullIfNotPrimitive() throws Exception {
		ParseResult result = StringToNumberParser.parse("",
				integerFormat, false);
		assertNull(result.getNumber());
	}

	@Test
	public void testReturnsParsePositionWhenValueCannotBeParsed()
			throws Exception {
		ParseResult result = StringToNumberParser.parse("adsf",
				integerFormat, false);
		assertNotNull(result.getPosition());
		assertNull(result.getNumber());
	}

	@Test
	public void testReturnsNumberWhenSuccessfullyParsed() throws Exception {
		Integer number = Integer.valueOf(5);
		ParseResult result = StringToNumberParser.parse(integerFormat
				.format(number.longValue()), integerFormat, false);
		assertNull(result.getPosition());
		assertEquals(number.intValue(), result.getNumber().intValue());
	}
}
