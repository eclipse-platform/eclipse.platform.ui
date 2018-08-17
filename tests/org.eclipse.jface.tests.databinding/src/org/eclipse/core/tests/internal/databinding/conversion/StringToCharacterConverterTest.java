/*******************************************************************************
 * Copyright (c) 2007, 2018 Matt Carter and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matt Carter - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.eclipse.core.internal.databinding.conversion.StringToCharacterConverter;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class StringToCharacterConverterTest {

	private StringToCharacterConverter converter;
	private StringToCharacterConverter primitiveConverter;

	@Before
	public void setUp() throws Exception {
		converter = StringToCharacterConverter.toCharacter(false);
		primitiveConverter = StringToCharacterConverter.toCharacter(true);
	}

	@Test
	public void testConvertsToCharacter() throws Exception {
		Character value = Character.valueOf('X');
		Character result = converter.convert(Character
				.toString(value.charValue()));

		assertEquals(value, result);
	}

	@Test
	public void testConvertsToCharacterPrimitive() throws Exception {
		Character value = Character.valueOf('Y');
		Character result = primitiveConverter.convert(String
				.valueOf(value.charValue()));
		assertEquals(value, result);
	}

	@Test
	public void testFromTypeIsString() throws Exception {
		assertEquals(String.class, converter.getFromType());
	}

	@Test
	public void testToTypeIsCharacter() throws Exception {
		assertEquals(Character.class, converter.getToType());
	}

	@Test
	public void testToTypeIsCharacterPrimitive() throws Exception {
		assertEquals(Character.TYPE, primitiveConverter.getToType());
	}

	@Test
	public void testReturnsNullBoxedTypeForEmptyString() throws Exception {
		assertNull(converter.convert(""));
	}

	@Test
	public void testNullCharacterIsOK() throws Exception {
		assertNull(converter.convert(null));
	}

	@Test
	public void testNullCharacterIsNotOKForPrimitive() throws Exception {
		try {
			primitiveConverter.convert(null);
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testThrowsIllegalArgumentExceptionIfAskedToConvertNonString()
			throws Exception {
		try {
			converter.convert(Integer.valueOf(1));
			fail("exception should have been thrown");
		} catch (IllegalArgumentException e) {
		}
	}
}