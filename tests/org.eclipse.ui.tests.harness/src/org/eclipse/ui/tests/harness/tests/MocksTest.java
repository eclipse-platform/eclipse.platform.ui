/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.harness.tests;

import java.lang.reflect.UndeclaredThrowableException;

import junit.framework.TestCase;

import org.eclipse.ui.tests.harness.util.Mocks;

/**
 * Tests for the Mocks class.
 * 
 * @since 1.1
 */
public class MocksTest extends TestCase {
	private IPrimitive primitiveMock;

	private static boolean uninitializedBoolean;
	private static byte unitializedByte;
	private static char unitializedChar;
	private static short unitializedShort;
	private static int unitializedInt;
	private static long unitializedLong;
	private static float unitializedFloat;
	private static double unitializedDouble;
	
	protected void setUp() throws Exception {
		primitiveMock = (IPrimitive) Mocks.createRelaxedMock(IPrimitive.class);
	}

	public void testPrimitiveBooleanReturnType() throws Exception {
		try {
			boolean value = primitiveMock.getBoolean();
			assertEquals(uninitializedBoolean, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}
	
	public void testPrimitiveBooleanSetLastReturnValue() throws Exception {
		Boolean value = Boolean.TRUE;
		primitiveMock.getBoolean();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);
		
		assertEquals(value.booleanValue(), primitiveMock.getBoolean());		
	}
	
	public void testPrimitiveByteReturnType() throws Exception {
		try {
			byte value = primitiveMock.getByte();
			assertEquals(unitializedByte, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}
	
	public void testPrimitiveByteSetLastReturnValue() throws Exception {
		Byte value = new Byte((byte) 1);
		primitiveMock.getByte();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);
		
		assertEquals(value.byteValue(), primitiveMock.getByte());
	}
	
	public void testPrimitiveCharReturnType() throws Exception {
		try {
			char value = primitiveMock.getChar();
			assertEquals(unitializedChar, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}
	
	public void testPrimitiveCharSetLastReturnValue() throws Exception {
		Character value = new Character('a');
		primitiveMock.getChar();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);
		
		assertEquals(value.charValue(), primitiveMock.getChar());
	}
	
	public void testPrimitiveShortReturnType() throws Exception {
		try {
			short value = primitiveMock.getShort();
			assertEquals(unitializedShort, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}

	public void testPrimitiveShortSetLastReturnValue() throws Exception {
		Short value = new Short((short) 1);
		primitiveMock.getShort();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);
		
		assertEquals(value.shortValue(), primitiveMock.getShort());
	}
	
	public void testPrimitiveIntReturnType() throws Exception {
		try {
			int value = primitiveMock.getInt();
			assertEquals(unitializedInt, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}
	
	public void testPrimitiveIntSetLastReturnValue() throws Exception {
		Integer value = new Integer(1);
		primitiveMock.getInt();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);
		
		assertEquals(value.intValue(), primitiveMock.getInt());
	}
	
	public void testPrimitiveLongReturnType() throws Exception {
		try {
			long value = primitiveMock.getLong();
			assertEquals(unitializedLong, value);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}
	
	public void testPrimitiveLongSetLastReturnValue() throws Exception {
		Long value = new Long(1);
		primitiveMock.getLong();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);
		
		assertEquals(value.longValue(), primitiveMock.getLong());
	}
	
	public void testPrimitiveFloatReturnType() throws Exception {
		try {
			float value = primitiveMock.getFloat();
			assertEquals(unitializedFloat, value, 0);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}
	
	public void testPrimitiveFloatSetLastReturnValue() throws Exception {
		Float value = new Float(1);
		primitiveMock.getFloat();
		Mocks.setLastReturnValue(primitiveMock, value);
		Mocks.startChecking(primitiveMock);
		
		assertEquals(value.floatValue(), primitiveMock.getFloat(), 0);
	}
	
	public void testPrimitiveDoubleReturnType() throws Exception {
		try {
			double value = primitiveMock.getDouble();
			assertEquals(unitializedDouble, value, 0);
		} catch (UndeclaredThrowableException e) {
			fail("exception should not have been thrown");
		}
	}
	
	public void testPrimitiveDoubleSetLastReturnValue() throws Exception {
		Double value = new Double(1);
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
