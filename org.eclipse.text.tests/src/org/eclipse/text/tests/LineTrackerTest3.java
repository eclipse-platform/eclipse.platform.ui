/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ConfigurableLineTracker;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;

public class LineTrackerTest3 extends TestCase {
	
	private GapTextStore fText;
	private ILineTracker  fTracker;
	
	
	public LineTrackerTest3(String name) {
		super(name);
	}
	
	protected int getLineOffset(int line, int[] lines) {
		int offset= 0;
		for (int i= 0; i < line; i++)
			offset += (lines[i] + 1);
		return offset;
	}
	
	protected void checkLines(int[] lines) {
		
		assertTrue("invalid number of line", fTracker.getNumberOfLines() == lines.length);
	
		for (int i= 0; i < lines.length; i++) {
			
			try {
				
				IRegion line= fTracker.getLineInformation(i);
				
				assertTrue("line: " + i + " length=" + line.getLength() + " should be:" + lines[i], line.getLength() == lines[i]);
				
				int expected= getLineOffset(i, lines);
				assertTrue("line: " + i + " offset=" + line.getOffset() + " should be:" + expected, line.getOffset() == expected);
			
			} catch (BadLocationException x) {
				assertTrue(false);
			}
		}
	}
	
	protected void setUp() {
		
		fText= new GapTextStore(50, 300);
		fTracker= new ConfigurableLineTracker(new String[] { "\n" });
		fText.set("x\nx\nx\nx\nx\n");
		fTracker.set("x\nx\nx\nx\nx\n");
	}
	
	public static Test suite() {
		return new TestSuite(LineTrackerTest3.class); 
	}
	
	protected void tearDown () {
		fTracker= null;
		fText= null;
	}
	
	public void testEditScript1() {
	
		try {
	
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
	
			fTracker.replace(0, fText.getLength(), "x");
			fText.replace(0, fText.getLength(), "x");
			
			checkLines(new int[] { 1 });
	
			fTracker.replace(1, 0, "y");
			fText.replace(1, 0, "y");
			
			checkLines(new int[] { 2 });
	
			fTracker.replace(2, 0, "z");
			fText.replace(2, 0, "z");
	
			checkLines(new int[] { 3 });
	
			fTracker.replace(3, 0, "\n");
			fText.replace(3, 0, "\n");
	
			checkLines(new int[] { 3, 0 });
	
			
			fTracker.replace(4, 0, "x");
			fText.replace(4, 0, "x");
	
			checkLines(new int[] { 3, 1 });
	
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	
	}
	
	public void testEmptyLines() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(0, 10, null);
			fText.replace(0, 10, null);
			checkLines(new int[] { 0 });
	
			fTracker.replace(0, 0, "\n\n\n\n\n");
			fText.replace(0, 0, "\n\n\n\n\n");
			checkLines(new int[] { 0, 0, 0, 0, 0, 0 });
	
			for (int i= 0; i < 6; i++) {
				int no= fTracker.getLineNumberOfOffset(i);
				assertTrue("invalid line number " + no + " reported instead of " + i, no == i);
			}
	
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testInsert1() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(3, 0, "yyyy");
			fText.replace(3, 0, "yyyy");
			checkLines(new int[] { 1, 5, 1, 1, 1, 0 });
	
			fTracker.replace(9, 0, "y\n");
			fText.replace(9, 0, "y\n");
			checkLines(new int[] { 1, 5, 2, 0, 1, 1, 0 });
			
			fTracker.replace(11, 0, "y\n");
			fText.replace(11, 0, "y\n");
			checkLines(new int[] { 1, 5, 2, 1, 0, 1, 1, 0 });
			
			fTracker.replace(13, 0, "y");
			fText.replace(13, 0, "y");
			checkLines(new int[] { 1, 5, 2, 1, 1, 1, 1, 0 });
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testInsert2() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(3, 0, "yyyy");
			fText.replace(3, 0, "yyyy");
			checkLines(new int[] { 1, 5, 1, 1, 1, 0 });
	
			fTracker.replace(9, 0, "y\ny\ny");
			fText.replace(9, 0, "y\ny\ny");
			checkLines(new int[] {  1, 5, 2, 1, 1, 1, 1, 0 });
	
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testLinesNumbers() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(0, 10, "\na\nbb\nccc\ndddd\neeeee\n" );
			fText.replace(0, 10, "\na\nbb\nccc\ndddd\neeeee\n" );
			checkLines(new int[] { 0, 1, 2, 3, 4, 5, 0 });
	
			int offset= 0;
			for (int i= 0; i < 5; i++) {
				for (int j= 0; j < i; j++) {
					int no= fTracker.getLineNumberOfOffset(offset + j);
					assertTrue("invalid line number " + no + " reported instead of " + i, no == i);
				}
				offset += (i + 1);
			}
			
	
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testOffsets() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
	
			for (int i= 0; i < 5; i++) {
				IRegion line= fTracker.getLineInformation(i);
				int pos= line.getOffset() + line.getLength();
				int offset= (2 * i) + 1;
				assertTrue("invalid line end offset " + pos + " for line " + i + " should be " + offset,  offset == pos);
			}
	
			for (int i= 0; i < 5; i++) {
				int pos= fTracker.getLineOffset(i);
				int offset= 2 * i;
				assertTrue("invalid line start offset " + pos + " for line " + i + " should be "  + offset, pos == offset);
			}
	
			for (int i= 0; i < 10; i++) {
				int line= fTracker.getLineNumberOfOffset(i);
				double l= Math.floor(i/2);
				assertTrue("invalid line number " + line + " for position " + i + " should be " + l, l == line);
			}
	
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testRemove() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(3, 1, null);
			fText.replace(3, 1, null);
			checkLines(new int[] { 1, 2, 1, 1, 0 });
	
			fTracker.replace(6, 1, null);
			fText.replace(6, 1, null);
			checkLines(new int[] { 1, 2, 2, 0 });
	
			fTracker.replace(3, 5, null);
			fText.replace(3, 4, null);
			checkLines(new int[] { 1, 1 });
	
			fTracker.replace(0, 3, null);
			fText.replace(0, 3, null);
			checkLines(new int[] { 0 });
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testReplace() {
	
		try {
	
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
	
			fTracker.replace(0, fText.getLength(), "\tx\n\tx\n\tx\n\tx\n\tx\n");
			fText.replace(0, fText.getLength(), "\tx\n\tx\n\tx\n\tx\n\tx\n");
			
			checkLines(new int[] { 2, 2, 2, 2, 2, 0 });
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	
	}
	
	public void testReplace2() {
	
		try {
	
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
	
			fTracker.replace(0, fText.getLength(), "x");
			fText.replace(0, fText.getLength(), "x");
			
			checkLines(new int[] { 1 });
	
			fTracker.replace(0, fText.getLength(), "x\nx\nx\n");
			fText.replace(0, fText.getLength(),  "x\nx\nx\n");
			
			checkLines(new int[] { 1, 1, 1, 0 });
	
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	
	}
	
	public void testReplace3() {
	
		try {
	
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(1, 1, "\n");
			fText.replace(1, 1, "\n");
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	
	}
	
	public void testReplace4() {
	
		try {
	
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			int lines= fTracker.getNumberOfLines();
			IRegion previous= fTracker.getLineInformation(0);
			for (int i= 1; i < lines; i++) {
				int lastLineEnd= previous.getOffset() + previous.getLength();
				int lineStart= fTracker.getLineInformation(i).getOffset();
				fTracker.replace(lastLineEnd, lineStart - lastLineEnd, "\n");
				fText.replace(lastLineEnd, lineStart - lastLineEnd, "\n");
				checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
				previous= fTracker.getLineInformation(i);
			}
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	
	}

	
	public void testShiftLeft() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
	
			fTracker.replace(0, fText.getLength(), "\tx\n\tx\n\tx\n\tx\n\tx\n");
			fText.replace(0, fText.getLength(), "\tx\n\tx\n\tx\n\tx\n\tx\n");
			checkLines(new int[] { 2, 2, 2, 2, 2, 0 });
			
			for (int i= 0; i < 5; i++) {
				int pos= fTracker.getLineOffset(i);
				fTracker.replace(pos, 1, null);
				fText.replace(pos, 1, null);
			}
	
	
			String txt= fText.get(0, fText.getLength());
			assertEquals("invalid text", "x\nx\nx\nx\nx\n", txt);
			
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testShiftRight() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
	
			for (int i= 0; i < 5; i++) {
				int pos= fTracker.getLineOffset(i);
				fTracker.replace(pos, 0, "\t");
				fText.replace(pos, 0, "\t");
			}
	
			checkLines(new int[] { 2, 2, 2, 2, 2, 0 });
	
			String txt= fText.get(0, fText.getLength());
			assertEquals("invalid text", "\tx\n\tx\n\tx\n\tx\n\tx\n", txt);
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testMultipleNewlines() {
		fText= new GapTextStore(50, 300);
		fTracker= new ConfigurableLineTracker(new String[] { "\n" });
		fText.set("x\n\nx\nx\n\nx\nx\n");
		fTracker.set("x\n\nx\nx\n\nx\nx\n");
	
		checkLines(new int[] { 1, 0, 1, 1, 0, 1, 1, 0 });
		try {
			int line= fTracker.getLineNumberOfOffset(8);
			assertTrue(line == 5);
		} catch (BadLocationException e) {
			assertTrue("bad location", false);
		}
	}
}
