/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.text.tests;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.jface.text.ConfigurableLineTracker;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.IRegion;

public class LineTrackerTest4 extends AbstractLineTrackerTest {


	@BeforeEach
	public void setUp() {
		fText= new GapTextStore();
		fTracker= new ConfigurableLineTracker(new String[] { "\r\n" });
		set("x\r\nx\r\nx\r\nx\r\nx\r\n");
	}

	@AfterEach
	public void tearDown() {
		fTracker= null;
		fText= null;
	}

	@Override
	protected int getLineOffset(int line, int[] lines) {
		int offset= 0;
		for (int i= 0; i < line; i++)
			offset += (lines[i] + 2);
		return offset;
	}

	@Test
	public void testEditScript1() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, fText.getLength(), "x");
		checkLines(new int[] { 1 });

		replace(1, 0, "y");
		checkLines(new int[] { 2 });

		replace(2, 0, "z");
		checkLines(new int[] { 3 });

		replace(3, 0, "\r\n");
		checkLines(new int[] { 3, 0 });

		replace(5, 0, "x");
		checkLines(new int[] { 3, 1 });
	}

	@Test
	public void testEmptyLines() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, 15, null);
		checkLines(new int[] { 0 });

		replace(0, 0, "\r\n\r\n\r\n\r\n\r\n");
		checkLines(new int[] { 0, 0, 0, 0, 0, 0 });

		for (int i= 0; i < 10; i++) {
			int no= fTracker.getLineNumberOfOffset(i);
			double l= Math.floor(i / 2);
			assertTrue(l == no, "invalid line number " + no + " for position " + i + " should be " + l);
		}
	}

	@Test
	public void testInsert1() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(4, 0, "yyyy");
		checkLines(new int[] { 1, 5, 1, 1, 1, 0 });

		replace(11, 0, "y\r\n");
		checkLines(new int[] { 1, 5, 2, 0, 1, 1, 0 });

		replace(14, 0, "y\r\n");
		checkLines(new int[] { 1, 5, 2, 1, 0, 1, 1, 0 });

		replace(17, 0, "y");
		checkLines(new int[] { 1, 5, 2, 1, 1, 1, 1, 0 });
	}

	@Test
	public void testInsert2() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(4, 0, "yyyy");
		checkLines(new int[] { 1, 5, 1, 1, 1, 0 });

		replace(11, 0, "y\r\ny\r\ny");
		checkLines(new int[] { 1, 5, 2, 1, 1, 1, 1, 0 });
	}

	@Test
	public void testLinesNumbers() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, 15, "\r\na\r\nbb\r\nccc\r\ndddd\r\neeeee\r\n");
		checkLines(new int[] { 0, 1, 2, 3, 4, 5, 0 });

		int offset= 0;
		for (int i= 0; i < 5; i++) {
			for (int j= 0; j <= i; j++) {
				int no= fTracker.getLineNumberOfOffset(offset + j);
				assertTrue(no == i, "invalid line number " + no + " reported instead of " + i);
			}
			offset+= (i + 2);
		}
	}

	@Test
	public void testOffsets() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		for (int i= 0; i < 5; i++) {
			IRegion line= fTracker.getLineInformation(i);
			int pos= line.getOffset() + line.getLength() + 1;
			int offset= (3 * i) + 2;
			assertTrue(offset == pos, "invalid line end offset " + pos + " for line " + i + " should be " + offset);
		}

		for (int i= 0; i < 5; i++) {
			int pos= fTracker.getLineOffset(i);
			int offset= 3 * i;
			assertTrue(pos == offset, "invalid line start offset " + pos + " for line " + i + " should be " + offset);
		}

		for (int i= 0; i < 15; i++) {
			int line= fTracker.getLineNumberOfOffset(i);
			double l= Math.floor(i / 3);
			assertTrue(l == line, "invalid line number " + line + " for position " + i + " should be " + l);
		}

		int lastLine= fTracker.getLineNumberOfOffset(fText.getLength());
		assertTrue(5 == lastLine, "invalid last line number " + lastLine);

		int offset= fTracker.getLineOffset(lastLine);
		assertTrue(fText.getLength() == offset, "invalid last line start offset " + offset);

		int length= fTracker.getLineLength(lastLine);
		assertTrue(0 == length, "invalid last line end offset " + (offset + length - 1));
	}

	@Test
	public void testRemove() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(4, 2, null);
		checkLines(new int[] { 1, 2, 1, 1, 0 });

		replace(8, 2, null);
		checkLines(new int[] { 1, 2, 2, 0 });

		replace(4, 7, null);
		checkLines(new int[] { 1, 1 });

		replace(0, 4, null);
		checkLines(new int[] { 0 });
	}

	@Test
	public void testReplace() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, fText.getLength(), "\tx\r\n\tx\r\n\tx\r\n\tx\r\n\tx\r\n");
		checkLines(new int[] { 2, 2, 2, 2, 2, 0 });
	}

	@Test
	public void testShiftLeft() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, fText.getLength(), "\tx\r\n\tx\r\n\tx\r\n\tx\r\n\tx\r\n");
		checkLines(new int[] { 2, 2, 2, 2, 2, 0 });

		for (int i= 0; i < 5; i++) {
			int pos= fTracker.getLineOffset(i);
			replace(pos, 1, null);
		}

		String txt= fText.get(0, fText.getLength());
		assertEquals("x\r\nx\r\nx\r\nx\r\nx\r\n", txt, "invalid text");
	}

	@Test
	public void testShiftRight() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		for (int i= 0; i < 5; i++) {
			int pos= fTracker.getLineOffset(i);
			replace(pos, 0, "\t");
		}

		checkLines(new int[] { 2, 2, 2, 2, 2, 0 });

		String txt= fText.get(0, fText.getLength());
		assertEquals("\tx\r\n\tx\r\n\tx\r\n\tx\r\n\tx\r\n", txt, "invalid text");
	}
}
