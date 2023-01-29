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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.text.NumberFormat;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.internal.databinding.conversion.StringToByteConverter;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class StringToByteConverterTest {
	private NumberFormat numberFormat;
	private StringToByteConverter converter;

	@Before
	public void setUp() {
		numberFormat = NumberFormat.getIntegerInstance();
		converter = StringToByteConverter.toByte(numberFormat, false);
	}

	@Test
	public void testConvertsToByte() {
		Byte value = (byte) 1;
		Byte result = converter.convert(numberFormat.format(value));

		assertEquals(value, result);
	}

	@Test
	public void testConvertsToBytePrimitive() {
		converter = StringToByteConverter.toByte(numberFormat, true);
		Byte value = (byte) 1;
		Byte result = converter.convert(numberFormat.format(value));
		assertEquals(value, result);
	}

	@Test
	public void testFromTypeIsString() {
		assertEquals(String.class, converter.getFromType());
	}

	@Test
	public void testToTypeIsShort() {
		assertEquals(Byte.class, converter.getToType());
	}

	@Test
	public void testToTypeIsBytePrimitive() {
		converter = StringToByteConverter.toByte(true);
		assertEquals(Byte.TYPE, converter.getToType());
	}

	@Test
	public void testReturnsNullBoxedTypeForEmptyString() {
		assertNull(converter.convert(""));
	}

	@Test
	public void testThrowsIllegalArgumentExceptionIfAskedToConvertNonString() {
		assertThrows(IllegalArgumentException.class,
				() -> ((IConverter<Object, ?>) converter).convert(Integer.valueOf(1)));
	}
}
