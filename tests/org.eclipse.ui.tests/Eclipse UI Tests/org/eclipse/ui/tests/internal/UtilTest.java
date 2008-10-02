/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.internal;

import org.eclipse.ui.internal.util.Util;

import junit.framework.TestCase;

/**
 * @since 3.5
 */
public class UtilTest extends TestCase {
	public UtilTest(String name) {
		super(name);
	}
	
	public void testBasicSplit() {
		final String field = "field1";
		String[] result = Util.split(field, ',');
		assertEquals(1, result.length);
		assertEquals(field, result[0]);
	}
	
	public void testBasic2Split() {
		final String field = "field1,field2";
		String[] result = Util.split(field, ',');
		assertEquals(2, result.length);
		assertEquals("field1", result[0]);
		assertEquals("field2", result[1]);
	}
	
	public void testBasic3Split() {
		final String field = "field1,field3,field2";
		String[] result = Util.split(field, ',');
		assertEquals(3, result.length);
		assertEquals("field1", result[0]);
		assertEquals("field3", result[1]);
		assertEquals("field2", result[2]);
	}
	
	public void testNothingSplit() {
		final String field = "";
		String[] result = Util.split(field, ',');
		assertEquals(1, result.length);
		assertEquals(0, result[0].length());
	}
	
	public void testNothingUsefulSplit() {
		final String field = ",";
		String[] result = Util.split(field, ',');
		assertEquals(0, result.length);
	}
	
	public void testNothingUseful2Split() {
		final String field = ",,";
		String[] result = Util.split(field, ',');
		assertEquals(0, result.length);
	}
	
	public void testNothingUsefulSpaceSplit() {
		final String field = " ,";
		String[] result = Util.split(field, ',');
		assertEquals(1, result.length);
		assertEquals(" ", result[0]);
	}

	public void testNothingUsefulSpaceSplit2() {
		final String field = ", ";
		String[] result = Util.split(field, ',');
		assertEquals(2, result.length);
		assertEquals(0, result[0].length());
		assertEquals(" ", result[1]);
	}
	
	public void testNothingUsefulSpaceSplit3() {
		final String field = " , ";
		String[] result = Util.split(field, ',');
		assertEquals(2, result.length);
		assertEquals(" ", result[0]);
		assertEquals(" ", result[1]);
	}
	
	public void test2Delimiters() {
		final String field = "field1,,field3,field2";
		String[] result = Util.split(field, ',');
		assertEquals(4, result.length);
		assertEquals("field1", result[0]);
		assertEquals(0, result[1].length());
		assertEquals("field3", result[2]);
		assertEquals("field2", result[3]);
	}

	public void test3Delimiters() {
		final String field = "field1,,,field3,field2";
		String[] result = Util.split(field, ',');
		assertEquals(5, result.length);
		assertEquals("field1", result[0]);
		assertEquals(0, result[1].length());
		assertEquals(0, result[2].length());
		assertEquals("field3", result[3]);
		assertEquals("field2", result[4]);
	}
}
