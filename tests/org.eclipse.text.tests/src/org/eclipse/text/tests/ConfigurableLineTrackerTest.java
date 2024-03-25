/*******************************************************************************
 * Copyright (c) 2019 Thomas Wolf and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.text.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.ConfigurableLineTracker;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.GapTextStore;

public class ConfigurableLineTrackerTest extends AbstractLineTrackerTest {

	@Before
	public void setUp() {
		fText= new GapTextStore();
	}

	@After
	public void tearDown() {
		fTracker= null;
		fText= null;
	}

	/**
	 * Expected length of the tracked delimiter. First entry is length of first
	 * delimiter and so on.
	 */
	private int[] lineEndLengths;

	@Override
	protected int getLineOffset(int line, int[] lines) {
		int offset= 0;
		for (int i= 0; i < line; i++) {
			offset += (lines[i] + lineEndLengths[i]);
		}
		return offset;
	}

	private void setLegalDelimiters(String... delimiters) {
		fTracker = new ConfigurableLineTracker(delimiters);
	}

	@Test
	public void testLongestMatch() throws Exception {
		setLegalDelimiters(DefaultLineTracker.DELIMITERS);
		set("xxxx\r\nxx");
		lineEndLengths = new int[] {2};
		checkLines(new int[] {4, 2});
	}

	@Test
	public void testMixedDefaultDelimiter() throws Exception {
		setLegalDelimiters(DefaultLineTracker.DELIMITERS);
		set("1234\r\n123\r1234\n1234567\r");
		lineEndLengths = new int[] { 2, 1, 1, 1 };
		checkLines(new int[] { 4, 3, 4, 7, 0 });
		set("first\r\r\nthird");
		lineEndLengths = new int[] { 1, 2 };
		checkLines(new int[] { 5, 0, 5 });
	}

	@Test
	public void testSingleDelimiter() throws Exception {
		setLegalDelimiters("\n");
		set("xxxx\nxx");
		lineEndLengths = new int[] { 1 };
		checkLines(new int[] { 4, 2 });
	}

	@Test
	public void testAlternativeDelimiters1() throws Exception {
		// create a line tracker with "show whitespace" characters as delimiter
		setLegalDelimiters("\u00a4", "\u00b6", "\u00a4\u00b6");
		set("xxxx\u00a4\u00b6xx");
		lineEndLengths = new int[] { 2 };
		checkLines(new int[] { 4, 2 });
	}

	@Test
	public void testAlternativeDelimiters2() throws Exception {
		setLegalDelimiters("{NewLine}", "[NewLine]");
		set("A line{NewLine}AnotherLine[NewLine}A third line[newline]End");
		lineEndLengths = new int[] { 9 };
		checkLines(new int[] { 6, 44 });
	}
}
