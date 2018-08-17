/*******************************************************************************
 * Copyright (c) 2006, 2018 Brad Reynolds.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *    Lars Vogel <Lars.Vogel@vogella.com> - Bug 443804
 *    Jeanderson Candido <http://jeandersonbc.github.io> - Bug 443804
 *******************************************************************************/

package org.eclipse.ui.tests.harness.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.UndeclaredThrowableException;

import org.eclipse.ui.tests.harness.util.Mocks;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Mocks class.
 *
 * @since 1.1
 */
public class MocksTest  {
	private IPrimitive primitiveMock;

	private static boolean uninitializedBoolean;
	private static byte unitializedByte;
	private static char unitializedChar;
	private static short unitializedShort;
	private static int unitializedInt;
	private static long unitializedLong;
	private static float unitializedFloat;
	private static double unitializedDouble;

	@Before
	public void setUp()  {
		primitiveMock = (IPrimitive) Mocks.createRelaxedMock(IPrimitive.class);
	}

	@Test
	public void testPrimitiveBooleanReturnType() {
		try {
			boolean value = primitiveMock.getBoolean();
			assertEquals(uninitializedBoolean, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}

	@Test
	public void testPrimitiveBooleanSetLastReturnValue() {
		Boolean value = Boolean.TRUE;
		primitiveMock.getBoolean();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);

		assertEquals(value.booleanValue(), primitiveMock.getBoolean());
	}

	@Test
	public void testPrimitiveByteReturnType() {
		try {
			byte value = primitiveMock.getByte();
			assertEquals(unitializedByte, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}

	@Test
	public void testPrimitiveByteSetLastReturnValue() {
		Byte value = Byte.valueOf((byte) 1);
		primitiveMock.getByte();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);

		assertEquals(value.byteValue(), primitiveMock.getByte());
	}

	@Test
	public void testPrimitiveCharReturnType() {
		try {
			char value = primitiveMock.getChar();
			assertEquals(unitializedChar, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}

	@Test
	public void testPrimitiveCharSetLastReturnValue() {
		Character value = Character.valueOf('a');
		primitiveMock.getChar();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);

		assertEquals(value.charValue(), primitiveMock.getChar());
	}

	@Test
	public void testPrimitiveShortReturnType() {
		try {
			short value = primitiveMock.getShort();
			assertEquals(unitializedShort, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}

	@Test
	public void testPrimitiveShortSetLastReturnValue() {
		Short value = Short.valueOf((short) 1);
		primitiveMock.getShort();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);

		assertEquals(value.shortValue(), primitiveMock.getShort());
	}

	@Test
	public void testPrimitiveIntReturnType() {
		try {
			int value = primitiveMock.getInt();
			assertEquals(unitializedInt, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}

	@Test
	public void testPrimitiveIntSetLastReturnValue() {
		Integer value = Integer.valueOf(1);
		primitiveMock.getInt();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);

		assertEquals(value.intValue(), primitiveMock.getInt());
	}

	@Test
	public void testPrimitiveLongReturnType() {
		try {
			long value = primitiveMock.getLong();
			assertEquals(unitializedLong, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}

	@Test
	public void testPrimitiveLongSetLastReturnValue() {
		Long value = Long.valueOf(1);
		primitiveMock.getLong();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);

		assertEquals(value.longValue(), primitiveMock.getLong());
	}

	@Test
	public void testPrimitiveFloatReturnType() {
		try {
			float value = primitiveMock.getFloat();
			assertEquals(unitializedFloat, value, 0);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}

	@Test
	public void testPrimitiveFloatSetLastReturnValue()  {
		Float value = Float.valueOf(1);
		primitiveMock.getFloat();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);

		assertEquals(value.floatValue(), primitiveMock.getFloat(), 0);
	}

	@Test
	public void testPrimitiveDoubleReturnType() {
		try {
			double value = primitiveMock.getDouble();
			assertEquals(unitializedDouble, value, 0);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}

	@Test
	public void testPrimitiveDoubleSetLastReturnValue() {
		Double value = Double.valueOf(1);
		primitiveMock.getDouble();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);

		assertEquals(value.doubleValue(), primitiveMock.getDouble(), 0);
	}

	public interface IPrimitive {
		public boolean getBoolean();

		public byte getByte();

		public char getChar();

		public short getShort();

		public int getInt();

		public long getLong();

		public float getFloat();

		public double getDouble();
	}
}
