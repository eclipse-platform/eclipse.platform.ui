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



public class LineTrackerTest4 extends TestCase {
	
	private GapTextStore fText;
	private ILineTracker  fTracker;
	
	
	public LineTrackerTest4(String name) {
		super(name);
	}
	
	protected int getLineOffset(int line, int[] lines) {
		int offset= 0;
		for (int i= 0; i < line; i++)
			offset += (lines[i] + 2);
		return offset;
	}
	
	protected void checkLines(int[] lines) {
		
		assertEquals("invalid number of line", lines.length, fTracker.getNumberOfLines());
	
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
		fTracker= new ConfigurableLineTracker(new String[] { "\r\n" });
		fText.set("x\r\nx\r\nx\r\nx\r\nx\r\n");
		fTracker.set("x\r\nx\r\nx\r\nx\r\nx\r\n");
	}
	
	public static Test suite() {
		return new TestSuite(LineTrackerTest4.class); 
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
	
			fTracker.replace(3, 0, "\r\n");
			fText.replace(3, 0, "\r\n");
	
			checkLines(new int[] { 3, 0 });
	
			
			fTracker.replace(5, 0, "x");
			fText.replace(5, 0, "x");
	
			checkLines(new int[] { 3, 1 });
	
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	
	}
	
	public void testEmptyLines() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(0, 15, null);
			fText.replace(0, 15, null);
			checkLines(new int[] { 0 });
	
			fTracker.replace(0, 0, "\r\n\r\n\r\n\r\n\r\n");
			fText.replace(0, 0, "\r\n\r\n\r\n\r\n\r\n");
			checkLines(new int[] { 0, 0, 0, 0, 0, 0 });
	
			for (int i= 0; i < 10; i++) {
				int no= fTracker.getLineNumberOfOffset(i);
				double l= Math.floor(i/2);
				assertTrue("invalid line number " + no + " for position " + i + " should be " + l, l == no);
			}
	
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testInsert1() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(4, 0, "yyyy");
			fText.replace(4, 0, "yyyy");
			checkLines(new int[] { 1, 5, 1, 1, 1, 0 });
	
			fTracker.replace(11, 0, "y\r\n");
			fText.replace(11, 0, "y\r\n");
			checkLines(new int[] { 1, 5, 2, 0, 1, 1, 0 });
			
			fTracker.replace(14, 0, "y\r\n");
			fText.replace(14, 0, "y\r\n");
			checkLines(new int[] { 1, 5, 2, 1, 0, 1, 1, 0 });
			
			fTracker.replace(17, 0, "y");
			fText.replace(17, 0, "y");
			checkLines(new int[] { 1, 5, 2, 1, 1, 1, 1, 0 });
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testInsert2() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(4, 0, "yyyy");
			fText.replace(4, 0, "yyyy");
			checkLines(new int[] { 1, 5, 1, 1, 1, 0 });
	
			fTracker.replace(11, 0, "y\r\ny\r\ny");
			fText.replace(11, 0, "y\r\ny\r\ny");
			checkLines(new int[] {  1, 5, 2, 1, 1, 1, 1, 0 });
	
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testLinesNumbers() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(0, 15, "\r\na\r\nbb\r\nccc\r\ndddd\r\neeeee\r\n" );
			fText.replace(0, 10, "\na\nbb\nccc\ndddd\neeeee\n" );
			checkLines(new int[] { 0, 1, 2, 3, 4, 5, 0 });
	
			int offset= 0;
			for (int i= 0; i < 5; i++) {
				for (int j= 0; j <= i; j++) {
					int no= fTracker.getLineNumberOfOffset(offset + j);
					assertTrue("invalid line number " + no + " reported instead of " + i, no == i);
				}
				offset += (i + 2);
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
				int pos= line.getOffset() + line.getLength() + 1;
				int offset= (3 * i) + 2;
				assertTrue("invalid line end offset " + pos + " for line " + i + " should be " + offset,  offset == pos);
			}
	
			for (int i= 0; i < 5; i++) {
				int pos= fTracker.getLineOffset(i);
				int offset= 3 * i;
				assertTrue("invalid line start offset " + pos + " for line " + i + " should be "  + offset, pos == offset);
			}
	
			for (int i= 0; i < 15; i++) {
				int line= fTracker.getLineNumberOfOffset(i);
				double l= Math.floor(i/3);
				assertTrue("invalid line number " + line + " for position " + i + " should be " + l, l == line);
			}
			
			int lastLine= fTracker.getLineNumberOfOffset(fText.getLength());
			assertTrue("invalid last line number " + lastLine, 5 == lastLine);
			
			int offset= fTracker.getLineOffset(lastLine);
			assertTrue("invalid last line start offset " + offset, fText.getLength() == offset);
			
			int length= fTracker.getLineLength(lastLine);
			assertTrue("invalid last line end offset " + (offset + length -1),  0 == length);
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testRemove() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
			
			fTracker.replace(4, 2, null);
			fText.replace(4, 2, null);
			checkLines(new int[] { 1, 2, 1, 1, 0 });
	
			fTracker.replace(8, 2, null);
			fText.replace(8, 2, null);
			checkLines(new int[] { 1, 2, 2, 0 });
	
			fTracker.replace(4, 7, null);
			fText.replace(4, 7, null);
			checkLines(new int[] { 1, 1 });
	
			fTracker.replace(0, 4, null);
			fText.replace(0, 4, null);
			checkLines(new int[] { 0 });
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
	
	public void testReplace() {
	
		try {
	
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
	
			fTracker.replace(0, fText.getLength(), "\tx\r\n\tx\r\n\tx\r\n\tx\r\n\tx\r\n");
			fText.replace(0, fText.getLength(), "\tx\r\n\tx\r\n\tx\r\n\tx\r\n\tx\r\n");
			
			checkLines(new int[] { 2, 2, 2, 2, 2, 0 });
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	
	}
	
	public void testShiftLeft() {
	
		try {
			
			checkLines(new int[] { 1, 1, 1, 1, 1, 0 });
	
			fTracker.replace(0, fText.getLength(), "\tx\r\n\tx\r\n\tx\r\n\tx\r\n\tx\r\n");
			fText.replace(0, fText.getLength(), "\tx\r\n\tx\r\n\tx\r\n\tx\r\n\tx\r\n");
			checkLines(new int[] { 2, 2, 2, 2, 2, 0 });
			
			for (int i= 0; i < 5; i++) {
				int pos= fTracker.getLineOffset(i);
				fTracker.replace(pos, 1, null);
				fText.replace(pos, 1, null);
			}
	
	
			String txt= fText.get(0, fText.getLength());
			assertEquals("invalid text", "x\r\nx\r\nx\r\nx\r\nx\r\n", txt);
			
			
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
			assertEquals("invalid text", "\tx\r\n\tx\r\n\tx\r\n\tx\r\n\tx\r\n", txt);
			
		} catch (BadLocationException e) {
			assertTrue("BadLocationException", false);
		}
	}
}
