/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.indexing;

import java.util.*;
import junit.framework.*;
import org.eclipse.core.internal.indexing.*;

public class BasicFieldTest extends TestCase {

	protected TestEnvironment env;

	public BasicFieldTest(String name, TestEnvironment env) {
		super(name);
		this.env = env;
	}

	public static Test suite(TestEnvironment env) {
		List names = new Vector(10);
		names.add("testBuffer");
		names.add("testPointer");
		names.add("testField");
		names.add("testFieldArray");
		TestSuite suite = new TestSuite(BasicFieldTest.class.getName());
		for (Iterator z = names.iterator(); z.hasNext();) {
			suite.addTest(new BasicFieldTest((String) z.next(), env));
		}
		return suite;
	}

	public void testBuffer() throws Exception {
		byte[] b = new byte[256];
		Buffer buf = new Buffer(b);
		int n = buf.length();
		buf.clear();
		b[0] = -128;
		b[1] = -0;
		b[2] = -0;
		b[3] = -0;
		assertEquals("t01", 0xFFFFFF80, buf.getInt(0, 1));
		assertEquals("t02", 0xFFFF8000, buf.getInt(0, 2));
		assertEquals("t03", 0xFF800000, buf.getInt(0, 3));
		assertEquals("t04", 0x80000000, buf.getInt(0, 4));
		assertEquals("t05", 0x00000080, buf.getUInt(0, 1));
		assertEquals("t06", 0x00008000, buf.getUInt(0, 2));
		assertEquals("t07", 0x00800000, buf.getUInt(0, 3));
		assertEquals("t08", 0x00000000, buf.getUInt(0, 4));

		for (int i = 0; i < n; i++)
			b[i] = 1;
		buf.clear(10, 10);
		for (int i = 0; i < n; i++) {
			if (i < 10)
				assertEquals("t31", 1, b[i]);
			else if (i < 20)
				assertEquals("t32", 0, b[i]);
			else
				assertEquals("t33", 1, b[i]);
		}

		n = 16;
		b = new byte[n];
		buf = new Buffer(b);
		b[0] = 1;
		buf.copyInternal(0, 1, 1);
		buf.copyInternal(0, 2, 2);
		buf.copyInternal(0, 4, 4);
		buf.copyInternal(0, 8, 8);
		for (int i = 0; i < n; i++) {
			assertEquals("t21", 1, b[i]);
		}

		byte[] b1 = new byte[4];
		byte[] b2 = new byte[2];
		Buffer buf1 = new Buffer(b1);
		Buffer buf2 = new Buffer(b2);
		assertEquals("t22", 1, Buffer.compare(buf1, buf2));
		assertEquals("t23", -1, Buffer.compare(buf2, buf1));
		assertEquals("t24", 0, Buffer.compare(buf1, buf1));
		b2[0] = 1;
		assertEquals("t25", -1, Buffer.compare(buf1, buf2));
		assertEquals("t26", 1, Buffer.compare(buf2, buf1));
	}

	public void testField() throws Exception {
	}

	public void testFieldArray() throws Exception {
	}

	public void testPointer() throws Exception {
	}

	public void testFieldDef() throws Exception {
		byte[] b = new byte[256];
		Field f = new Field(b);
		Buffer buf = new Buffer(b);
		FieldDef d01 = new FieldDef(FieldDef.F_INT, 2, 1);
		FieldDef d02 = new FieldDef(FieldDef.F_INT, 2, 2);
		FieldDef d03 = new FieldDef(FieldDef.F_INT, 2, 3);
		FieldDef d04 = new FieldDef(FieldDef.F_INT, 2, 4);
		FieldDef d05 = new FieldDef(FieldDef.F_LONG, 2, 5);
		FieldDef d06 = new FieldDef(FieldDef.F_LONG, 2, 6);
		FieldDef d07 = new FieldDef(FieldDef.F_LONG, 2, 7);
		FieldDef d08 = new FieldDef(FieldDef.F_LONG, 2, 8);
		FieldDef d99 = new FieldDef(FieldDef.F_BYTES, 2, 99);

		f.clear();
		f.put(d01, 255);
		assertEquals("1d01", 255L << 56, buf.getLong(2, 8));
		f.put(d02, 255);
		assertEquals("1d02", 255L << 48, buf.getLong(2, 8));
		f.put(d03, 255);
		assertEquals("1d03", 255L << 40, buf.getLong(2, 8));
		f.put(d04, 255);
		assertEquals("1d04", 255L << 32, buf.getLong(2, 8));
		f.put(d05, 255);
		assertEquals("1d05", 255L << 24, buf.getLong(2, 8));
		f.put(d06, 255);
		assertEquals("1d06", 255L << 16, buf.getLong(2, 8));
		f.put(d07, 255);
		assertEquals("1d07", 255L << 8, buf.getLong(2, 8));
		f.put(d08, 255);
		assertEquals("1d08", 255L << 0, buf.getLong(2, 8));

		f.clear();
		f.put(d01, 255);
		assertEquals("2d01", 255, f.getUInt(d01));
		f.put(d02, 255);
		assertEquals("2d02", 255, f.getUInt(d02));
		f.put(d03, 255);
		assertEquals("2d03", 255, f.getUInt(d03));
		f.put(d04, 255);
		assertEquals("2d04", 255, f.getUInt(d04));
		f.put(d05, 255);
		assertEquals("2d05", 255, f.getUInt(d05));
		f.put(d06, 255);
		assertEquals("2d06", 255, f.getUInt(d06));
		f.put(d07, 255);
		assertEquals("2d07", 255, f.getUInt(d07));
		f.put(d08, 255);
		assertEquals("2d08", 255, f.getUInt(d08));
		f.put(d99, 255);
		assertEquals("2d99", 255, f.getUInt(d99));
		assertEquals("3d99", 255, buf.getUInt(100, 1));
	}

}