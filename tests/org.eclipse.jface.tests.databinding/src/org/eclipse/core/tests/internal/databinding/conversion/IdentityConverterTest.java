/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 *     Brad Reynolds - bug 116920
 *     Matt Carter - bug 197679
 *******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.BindingException;
import org.eclipse.core.internal.databinding.conversion.IdentityConverter;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class IdentityConverterTest {

	private IdentityConverter c;

	@Before
	public void setUp() throws Exception {
		c = new IdentityConverter(Integer.TYPE, Integer.TYPE);
	}

	@Test
	public void testIsPrimitiveTypeMatchedWithBoxed() throws Exception {
		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Integer.class,
				Integer.TYPE));
		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Integer.TYPE,
				Integer.class));

		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Byte.class, Byte.TYPE));
		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Byte.TYPE, Byte.class));

		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Short.class, Short.TYPE));
		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Short.TYPE, Short.class));

		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Long.class, Long.TYPE));
		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Long.TYPE, Long.class));

		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Float.class, Float.TYPE));
		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Float.TYPE, Float.class));

		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Double.class, Double.TYPE));
		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Double.TYPE, Double.class));

		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Boolean.class,
				Boolean.TYPE));
		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Boolean.TYPE,
				Boolean.class));

		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Character.class,
				Character.TYPE));
		assertTrue(c.isPrimitiveTypeMatchedWithBoxed(Character.TYPE,
				Character.class));

		assertFalse(c.isPrimitiveTypeMatchedWithBoxed(Boolean.class,
				Integer.TYPE));
	}

	@Test
	public void testConvert_NullToPrimitive() {
		IdentityConverter p2b = new IdentityConverter(Float.TYPE, Float.TYPE);
		assertThrows(BindingException.class, () -> p2b.convert(null));
	}

	@Test
	public void testConvert_PrimitiveToBoxed() throws Exception {
		IdentityConverter p2b = new IdentityConverter(Float.TYPE, Float.class);
		assertEquals("4.2", Float.valueOf((float) 4.2), p2b.convert(Float.valueOf((float) 4.2)));
	}

	@Test
	public void testConvert_BoxedToPrimitive() throws Exception {
		IdentityConverter p2b = new IdentityConverter(Float.class, Float.TYPE);
		assertEquals("4.2", Float.valueOf((float) 4.2), p2b.convert(Float.valueOf((float) 4.2)));
	}

	@Test
	public void testConvert_PrimitiveToPrimitive() throws Exception {
		IdentityConverter p2b = new IdentityConverter(Float.TYPE, Float.TYPE);
		assertEquals("4.2", Float.valueOf((float) 4.2), p2b.convert(Float.valueOf((float) 4.2)));
	}

	@Test
	public void testConvert_BoxedToBoxed() throws Exception {
		IdentityConverter p2b = new IdentityConverter(Float.class, Float.class);
		assertEquals("4.2", Float.valueOf((float) 4.2), p2b.convert(Float.valueOf((float) 4.2)));
	}

	public static class Person {
		public String foo = "blah";
	}

	public static class Animal {
		public String name = "fido";
	}

	@Test
	public void test_Convert_ValidAssignment() throws Exception {
		IdentityConverter pc = new IdentityConverter(Object.class, Person.class);
		Person orig = new Person();
		Object person = pc.convert(orig);
		assertTrue("Person class", person.getClass().equals(Person.class));
		assertTrue("Need correct Person", person.equals(orig));
	}

	@Test
	public void test_Convert_ValidAssignment2() throws Exception {
		IdentityConverter pc = new IdentityConverter(Person.class, Object.class);
		Person orig = new Person();
		Object person = pc.convert(orig);
		assertTrue("Person class", person.getClass().equals(Person.class));
		assertTrue("Need correct Person", person.equals(orig));
	}

	@Test
	public void testConvert_InvalidAssignment() throws Exception {
		IdentityConverter pc = new IdentityConverter(Object.class, Person.class);
		assertThrows(BindingException.class, () -> pc.convert(new Animal()));
	}
}
