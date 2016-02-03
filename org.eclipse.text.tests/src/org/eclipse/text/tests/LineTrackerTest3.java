/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ConfigurableLineTracker;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.IRegion;

public class LineTrackerTest3 extends AbstractLineTrackerTest {

	@Before
	public void setUp() {
		fText= new GapTextStore();
		fTracker= new ConfigurableLineTracker(new String[] { "\n" });
		set("x\nx\nx\nx\nx\n");
	}

	@After
	public void tearDown() {
		fTracker= null;
		fText= null;
	}

	@Override
	protected int getLineOffset(int line, int[] lines) {
		int offset= 0;
		for (int i= 0; i < line; i++)
			offset += (lines[i] + 1);
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

		replace(3, 0, "\n");
		checkLines(new int[] { 3, 0 });

		replace(4, 0, "x");
		checkLines(new int[] { 3, 1 });
	}
	
	@Test
	public void testEmptyLines() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, 10, null);
		checkLines(new int[] { 0 });

		replace(0, 0, "\n\n\n\n\n");
		checkLines(new int[] { 0, 0, 0, 0, 0, 0 });

		for (int i= 0; i < 6; i++) {
			int no= fTracker.getLineNumberOfOffset(i);
			assertTrue("invalid line number " + no + " reported instead of " + i, no == i);
		}
	}
	
	@Test
	public void testInsert1() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(3, 0, "yyyy");
		checkLines(new int[] { 1, 5, 1, 1, 1, 0 });

		replace(9, 0, "y\n");
		checkLines(new int[] { 1, 5, 2, 0, 1, 1, 0 });

		replace(11, 0, "y\n");
		checkLines(new int[] { 1, 5, 2, 1, 0, 1, 1, 0 });

		replace(13, 0, "y");
		checkLines(new int[] { 1, 5, 2, 1, 1, 1, 1, 0 });
	}
	
	@Test
	public void testInsert2() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(3, 0, "yyyy");
		checkLines(new int[] { 1, 5, 1, 1, 1, 0 });

		replace(9, 0, "y\ny\ny");
		checkLines(new int[] { 1, 5, 2, 1, 1, 1, 1, 0 });
	}
	
	@Test
	public void testLinesNumbers() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, 10, "\na\nbb\nccc\ndddd\neeeee\n");
		checkLines(new int[] { 0, 1, 2, 3, 4, 5, 0 });

		int offset= 0;
		for (int i= 0; i < 5; i++) {
			for (int j= 0; j < i; j++) {
				int no= fTracker.getLineNumberOfOffset(offset + j);
				assertTrue("invalid line number " + no + " reported instead of " + i, no == i);
			}
			offset+= (i + 1);
		}
	}
	
	@Test
	public void testOffsets() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		for (int i= 0; i < 5; i++) {
			IRegion line= fTracker.getLineInformation(i);
			int pos= line.getOffset() + line.getLength();
			int offset= (2 * i) + 1;
			assertTrue("invalid line end offset " + pos + " for line " + i + " should be " + offset, offset == pos);
		}

		for (int i= 0; i < 5; i++) {
			int pos= fTracker.getLineOffset(i);
			int offset= 2 * i;
			assertTrue("invalid line start offset " + pos + " for line " + i + " should be " + offset, pos == offset);
		}

		for (int i= 0; i < 10; i++) {
			int line= fTracker.getLineNumberOfOffset(i);
			double l= Math.floor(i / 2);
			assertTrue("invalid line number " + line + " for position " + i + " should be " + l, l == line);
		}
	}
	
	@Test
	public void testRemove() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(3, 1, null);
		checkLines(new int[] { 1, 2, 1, 1, 0 });

		replace(6, 1, null);
		checkLines(new int[] { 1, 2, 2, 0 });

		replace(3, 5, null);
		checkLines(new int[] { 1, 1 });

		replace(0, 3, null);
		checkLines(new int[] { 0 });
	}
	
	@Test
	public void testReplace() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, fText.getLength(), "\tx\n\tx\n\tx\n\tx\n\tx\n");

		checkLines(new int[] { 2, 2, 2, 2, 2, 0 });
	}
	
	@Test
	public void testReplace2() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, fText.getLength(), "x");

		checkLines(new int[] { 1 });

		replace(0, fText.getLength(), "x\nx\nx\n");

		checkLines(new int[] { 1, 1, 1, 0 });
	}
	
	@Test
	public void testReplace3() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(1, 1, "\n");

		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
	}
	
	@Test
	public void testReplace4() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		int lines= fTracker.getNumberOfLines();
		IRegion previous= fTracker.getLineInformation(0);
		for (int i= 1; i < lines; i++) {
			int lastLineEnd= previous.getOffset() + previous.getLength();
			int lineStart= fTracker.getLineInformation(i).getOffset();
			replace(lastLineEnd, lineStart - lastLineEnd, "\n");
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			previous= fTracker.getLineInformation(i);
		}
	}
	
	@Test
	public void testShiftLeft() throws Exception {
		checkLines(new int[] { 1, 1, 1, 1, 1, 0 });

		replace(0, fText.getLength(), "\tx\n\tx\n\tx\n\tx\n\tx\n");
		checkLines(new int[] { 2, 2, 2, 2, 2, 0 });

		for (int i= 0; i < 5; i++) {
			int pos= fTracker.getLineOffset(i);
			replace(pos, 1, null);
		}

		String txt= fText.get(0, fText.getLength());
		assertEquals("invalid text", "x\nx\nx\nx\nx\n", txt);
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
		assertEquals("invalid text", "\tx\n\tx\n\tx\n\tx\n\tx\n", txt);
	}
	
	@Test
	public void testMultipleNewlines() throws Exception {
		fText= new GapTextStore();
		fTracker= new ConfigurableLineTracker(new String[] { "\n" });
		set("x\n\nx\nx\n\nx\nx\n");

		checkLines(new int[] { 1, 0, 1, 1, 0, 1, 1, 0 });
		int line= fTracker.getLineNumberOfOffset(8);
		assertTrue(line == 5);
	}

	@Test
	public void testDeleteEmptyLine() throws Exception {
		set("x\nx\n\nx\n\n");

		int[] lengths= new int[] { 1, 1, 0, 1, 0, 0 };
		checkLines(lengths);
		for (int line= lengths.length - 1; line >= 0; line--)
			fTracker.replace(fTracker.getLineOffset(line), fTracker.getLineLength(line), null);

	}
	
	@Test
	public void testDeleteLinesFromEnd() throws Exception {
		set("x\nx\n\nx\n\n");

		int[] lengths= new int[] { 1, 1, 0, 1, 0, 0 };
		checkLines(lengths);
		for (int line= lengths.length - 1; line >= 0; line--)
			fTracker.replace(fTracker.getLineOffset(line), fTracker.getLineLength(line), null);

	}
	
	@Test
	public void testDeleteLines() throws Exception {
		String content= "";
		for (int i= 0; i < 50; i++) {
			fTracker.set(content + "x\nx\n\nx\n\n");

			int lines= fTracker.getNumberOfLines();
			for (int line= 0; line < lines; line++)
				fTracker.replace(fTracker.getLineOffset(0), fTracker.getLineLength(0), null);
		}
		content= "";
		for (int i= 0; i < 50; i++) {
			fTracker.set(content + "x\nx\n\nx\n\n");

			int lines= fTracker.getNumberOfLines();
			for (int line= lines - 1; line >= 0; line--)
				fTracker.replace(fTracker.getLineOffset(line), fTracker.getLineLength(line), null);
		}
	}
	
	@Test
	public void testSet() throws Exception {
		String content= "";
		for (int i= 0; i < 35; i++) {
			int[] lenghts= new int[i + 1];
			for (int j= 0; j < i +1; j++)
				lenghts[j]= j;
			for (int j= 0; j < i; j++)
				content += "x";

			set(content);
			checkLines(lenghts);

			content += "\n";
		}
	}
	
	@Test
	public void testFunnyLastLineCompatibility() throws Exception {
		/* empty last line */
		set("x\n");
		int[] offsets= { 0, 2 };
		int[] lengths= { 1, 0 };

		assertEquals("invalid number of lines, ", lengths.length, fTracker.getNumberOfLines());
		assertEquals("invalid number of lines, ", lengths.length, fTracker.getNumberOfLines(0, fText.getLength()));
		for (int i= 0; i < lengths.length; i++) {
			IRegion line= fTracker.getLineInformation(i);
			assertEquals("line: " + i, lengths[i], line.getLength());
			assertEquals("line: " + i, offsets[i], line.getOffset());
		}
		try {
			fTracker.getLineInformation(lengths.length);
			fail();
		} catch (Exception e) {
		}

		try {
			fTracker.getLineInformationOfOffset(offsets[offsets.length] + 1);
			fail();
		} catch (Exception e) {
		}


		/* phantom last line when the last line is not empty */
		set("x\nx");
		offsets= new int[] { 0, 2, 3 };
		lengths= new int[] {1, 1, 0};
		assertEquals("invalid number of lines, ", lengths.length - 1 /* !!!! */, fTracker.getNumberOfLines());
		assertEquals("invalid number of lines, ", lengths.length - 1 /* !!!! */, fTracker.getNumberOfLines(0, fText.getLength()));
		for (int i= 0; i < lengths.length; i++) {
			IRegion line= fTracker.getLineInformation(i);
			int len= lengths[i];
			int offset= offsets[i];
			assertEquals("length of line: " + i, len, line.getLength());
			assertEquals("offset of line: " + i, offset, line.getOffset());

			line= fTracker.getLineInformationOfOffset(offset);
			if ( i == lengths.length - 1) { // phantom line cannot be queried by offset
				len= lengths[i - 1];
				offset= offsets[i - 1];
			}
			assertEquals("length of line: " + i, len, line.getLength());
			assertEquals("offset of line: " + i, offset, line.getOffset());
		}

		try {
			fTracker.getLineInformation(lengths.length);
			fail();
		} catch (Exception e) {
		}

		try {
			fTracker.getLineInformationOfOffset(offsets[offsets.length] + 1);
			fail();
		} catch (Exception e) {
		}

	}
	
	@Test
	public void testNoDelimiterLine() throws Exception {
	    set("abcef");
	    checkLines(new int[] { 5 });

	    replace(0, 0, ""); // switch to TreeLineTracker
	    checkLines(new int[] { 5 });
    }
	
	@Test
	public void testFunnyLastLineCompatibility2() throws Exception {
		/* empty last line */
		set("x\n");
		replace(0, 2, "x\n");

		int[] offsets= { 0, 2 };
		int[] lengths= { 1, 0 };

		assertEquals("invalid number of lines, ", lengths.length, fTracker.getNumberOfLines());
		assertEquals("invalid number of lines, ", lengths.length, fTracker.getNumberOfLines(0, fText.getLength()));
		for (int i= 0; i < lengths.length; i++) {
			IRegion line= fTracker.getLineInformation(i);
			assertEquals("line: " + i, lengths[i], line.getLength());
			assertEquals("line: " + i, offsets[i], line.getOffset());
		}
		try {
			fTracker.getLineInformation(lengths.length);
			fail();
		} catch (Exception e) {
		}

		try {
			fTracker.getLineInformationOfOffset(offsets[offsets.length] + 1);
			fail();
		} catch (Exception e) {
		}


		/* phantom last line when the last line is not empty */
		set("x\nx");
		offsets= new int[] { 0, 2, 3 };
		lengths= new int[] {1, 1, 0};
		assertEquals("invalid number of lines, ", lengths.length - 1 /* !!!! */, fTracker.getNumberOfLines());
		assertEquals("invalid number of lines, ", lengths.length - 1 /* !!!! */, fTracker.getNumberOfLines(0, fText.getLength()));
		for (int i= 0; i < lengths.length; i++) {
			IRegion line= fTracker.getLineInformation(i);
			int len= lengths[i];
			int offset= offsets[i];
			assertEquals("length of line: " + i, len, line.getLength());
			assertEquals("offset of line: " + i, offset, line.getOffset());

			line= fTracker.getLineInformationOfOffset(offset);
			if ( i == lengths.length - 1) { // phantom line cannot be queried by offset
				len= lengths[i - 1];
				offset= offsets[i - 1];
			}
			assertEquals("length of line: " + i, len, line.getLength());
			assertEquals("offset of line: " + i, offset, line.getOffset());
		}

		try {
			fTracker.getLineInformation(lengths.length);
			fail();
		} catch (Exception e) {
		}

		try {
			fTracker.getLineInformationOfOffset(offsets[offsets.length] + 1);
			fail();
		} catch (Exception e) {
		}

	}
	
	@Test
	public void testNegativeOffset() throws Exception {
		try {
			assertEquals(-1, fTracker.getLineNumberOfOffset(-1));
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineInformationOfOffset(-1);
			fail();
		} catch (BadLocationException e) {
		}

		try {
			assertEquals(-1, fTracker.getLineNumberOfOffset(-1000));
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineInformationOfOffset(-1000);
			fail();
		} catch (BadLocationException e) {
		}

		try {
			fTracker.getLineInformationOfOffset(1000);
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineNumberOfOffset(1000);
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineOffset(-1000);
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineInformation(-1000);
			fail();
		} catch (BadLocationException e) {
		}
	}
	public void testNegativeOffset2() throws Exception {
		replace(0, 0, "x");
		try {
			assertEquals(-1, fTracker.getLineNumberOfOffset(-1));
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineInformationOfOffset(-1);
			fail();
		} catch (BadLocationException e) {
		}

		try {
			assertEquals(-1, fTracker.getLineNumberOfOffset(-1000));
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineInformationOfOffset(-1000);
			fail();
		} catch (BadLocationException e) {
		}

		try {
			fTracker.getLineInformationOfOffset(1000);
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineNumberOfOffset(1000);
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineOffset(-1000);
			fail();
		} catch (BadLocationException e) {
		}
		try {
			fTracker.getLineInformation(-1000);
			fail();
		} catch (BadLocationException e) {
		}
	}
}
