/*******************************************************************************
 * Copyright (c) 2020 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - initial API and implementation (Bug 558842)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.conversion.EnumConverters;
import org.eclipse.core.databinding.conversion.IConverter;
import org.junit.Test;

/**
 * Test for {@link EnumConverters}.
 */
public class EnumConvertersTest {

	enum TestEnum {
		A, B;

		@Override
		public String toString() {
			// To be able to tell apart from the enum name
			return name().toLowerCase();
		}
	}

	@Test
	public void testFromOrdinal() {
		IConverter<Integer, TestEnum> fromOrdinal = EnumConverters.fromOrdinal(TestEnum.class);
		assertEquals(TestEnum.A, fromOrdinal.convert(0));
		assertEquals(null, fromOrdinal.convert(null));
		assertEquals(null, fromOrdinal.convert(100));
		assertEquals(Integer.class, fromOrdinal.getFromType());
		assertEquals(TestEnum.class, fromOrdinal.getToType());
	}

	@Test
	public void testFromString() {
		IConverter<String, TestEnum> fromOrdinal = EnumConverters.fromString(TestEnum.class);
		assertEquals(TestEnum.A, fromOrdinal.convert("A"));
		assertEquals(null, fromOrdinal.convert("a"));
		assertEquals(null, fromOrdinal.convert(null));
		assertEquals(String.class, fromOrdinal.getFromType());
		assertEquals(TestEnum.class, fromOrdinal.getToType());
	}

	@Test
	public void testToString() {
		IConverter<TestEnum, String> fromOrdinal = EnumConverters.toString(TestEnum.class);
		assertEquals("a", fromOrdinal.convert(TestEnum.A));
		assertEquals(null, fromOrdinal.convert(null));
		assertEquals(TestEnum.class, fromOrdinal.getFromType());
		assertEquals(String.class, fromOrdinal.getToType());
	}

	@Test
	public void testToOrdinal() {
		IConverter<TestEnum, Integer> converter = EnumConverters.toOrdinal(TestEnum.class);
		assertEquals(0, (int) converter.convert(TestEnum.A));
		assertEquals(null, converter.convert(null));
		assertEquals(TestEnum.class, converter.getFromType());
		assertEquals(Integer.class, converter.getToType());
	}
}
