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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.internal.databinding.BindingMessages;
import org.eclipse.core.internal.databinding.conversion.StringToBooleanConverter;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class StringToBooleanConverterTest {
	private StringToBooleanConverter converter;

	private List<String> trueValues;

	private List<String> falseValues;

	@Before
	public void setUp() {
		trueValues = Collections.unmodifiableList(toValues(BindingMessages
				.getString("ValueDelimiter"), BindingMessages
				.getString("TrueStringValues")));
		falseValues = Collections.unmodifiableList(toValues(BindingMessages
				.getString("ValueDelimiter"), BindingMessages
				.getString("FalseStringValues")));

		converter = new StringToBooleanConverter();
		assertTrue(trueValues.size() > 0);
		assertTrue(falseValues.size() > 0);
	}

	private List<String> toValues(String delimiter, String values) {
		StringTokenizer tokenizer = new StringTokenizer(values, delimiter);
		List<String> result = new LinkedList<>();

		while (tokenizer.hasMoreTokens()) {
			result.add(tokenizer.nextToken());
		}

		return result;
	}

	@Test
	public void testConvertsToTrue() throws Exception {
		Boolean result = converter.convert(trueValues.get(0));
		assertTrue(result.booleanValue());
	}

	@Test
	public void testConvertsToFalse() throws Exception {
		Boolean result = converter.convert(falseValues.get(0));
		assertFalse(result.booleanValue());
	}

	@Test
	public void testUpperCaseStringConvertsToTrue() throws Exception {
		Boolean result = converter.convert(trueValues.get(0)
				.toUpperCase());
		assertTrue(result.booleanValue());
	}

	@Test
	public void testUpperCaseStringConvertsToFalse() throws Exception {
		Boolean result = converter.convert(falseValues.get(0)
				.toUpperCase());
		assertFalse(result.booleanValue());
	}
}
