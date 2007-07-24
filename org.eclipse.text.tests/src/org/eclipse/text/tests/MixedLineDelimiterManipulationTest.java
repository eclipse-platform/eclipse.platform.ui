/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;

public class MixedLineDelimiterManipulationTest extends TestCase {
	
	public static Test suite() {
		return new TestSuite(MixedLineDelimiterManipulationTest.class); 
	}
	
	private static final String CR= "\r";
	private static final String LF= "\n";
	private static final String CRLF= "\r\n";
    
	private Document fDocument;
	
	
	public MixedLineDelimiterManipulationTest(String name) {
		super(name);
	}
	
	protected void setUp() {
		fDocument= new Document();
	}
	
	protected void tearDown () {
		fDocument= null;
	}
	
	/**
	 * Checks line lengths.
	 * 
	 * @param lines an array of length lines * 2, each odd index being the line length, the even
	 *        index being the delimiter length
	 */
	protected void checkLines2(int[] lines) throws BadLocationException {
		assertTrue(lines.length % 2 == 0);
		int n= lines.length / 2;
		assertEquals("invalid number of lines, ", n, fDocument.getNumberOfLines());
		assertEquals("invalid number of lines, ", n, fDocument.getNumberOfLines(0, fDocument.getLength()));
		
		int offset= 0;
		for (int i= 0; i < n; i++) {
			
			int expectedLength= lines[i * 2];
			int expectedDelimLength= lines[i * 2 + 1];
			String expectedDelim= fDocument.get(offset + expectedLength, expectedDelimLength);
			
			IRegion info= fDocument.getLineInformation(i);
			String delimiter= fDocument.getLineDelimiter(i);
			if (delimiter == null) delimiter= "";
			
			assertEquals("line: " + i + " length=" + info.getLength() + " should be:" + expectedLength, expectedLength, info.getLength());
			assertEquals("line: " + i + " offset=" + info.getOffset() + " should be:" + offset, offset, info.getOffset());
			assertEquals(delimiter, expectedDelim);
			
			offset += expectedLength + expectedDelimLength;
		}
	}
	
	private void set(String content) {
	    fDocument.set(content);
    }
	
	private void replace(int offset, int length, String text) throws BadLocationException {
		fDocument.replace(offset, length, text);
	}
	
	public void testMixedDelimterTrackingWithCRLF() throws Exception {
		set("one\r\ntwo\r\ntre\r\n");
		
		int[] lines= {3, 2, 3, 2, 3, 2, 0, 0};
		checkLines2(lines);
    }

	public void testMixedDelimterTrackingWithLF() throws Exception {
		set("one\ntwo\ntre\n");
		
		int[] lines= {3, 1, 3, 1, 3, 1, 0, 0};
		checkLines2(lines);
	}
	
	public void testMixedDelimterTrackingWithMixed() throws Exception {
		set("one\ntwo\r\ntre\n");
		
		int[] lines= {3, 1, 3, 2, 3, 1, 0, 0};
		checkLines2(lines);
	}
	
	public void testMixedDelimterReplaceLFWithCRLF() throws Exception {
		set("one\ntwo\ntre\n");
		
		replace(7, 1, CRLF);
		
		int[] lines= {3, 1, 3, 2, 3, 1, 0, 0};
		checkLines2(lines);
	}

	public void testMixedDelimterReplaceCRLFWithLF() throws Exception {
		set("one\r\ntwo\r\ntre\r\n");
		
		replace(7, 2, LF);
		
		int[] lines= {3, 2, 2, 1, 0, 1, 3, 2, 0, 0};
		checkLines2(lines);
	}
	
	public void testMixedDelimterInsertCRBeforeLF() throws Exception {
		set("one\ntwo\ntre\n");
		
		replace(7, 0, CR);
		
		int[] lines= {3, 1, 3, 2, 3, 1, 0, 0};
		checkLines2(lines);
	}
	
	public void testMixedDelimterRemoveCRInCRLF() throws Exception {
		set("one\r\ntwo\r\ntre\r\n");
		
		replace(8, 1, "");
		
		int[] lines= {3, 2, 3, 1, 3, 2, 0, 0};
		checkLines2(lines);
	}
	
	public void testMixedDelimterReplaceCRInCRLF() throws Exception {
		set("one\r\ntwo\r\ntre\r\n");
		
		replace(8, 1, "x");
		
		int[] lines= {3, 2, 4, 1, 3, 2, 0, 0};
		checkLines2(lines);
	}
	
	public void testMixedDelimterInsertLFBeforeCRLF() throws Exception {
		set("one\r\ntwo\r\ntre\r\n");
		// CRLF
		// CRLF
		// CRLF
		
		replace(8, 0, LF);
		
		// CRLF
		// LFCR  <- CR "switches"
		// LF
		// CRLF
		
		int[] lines= {3, 2, 3, 1, 0, 2, 3, 2, 0, 0};
		checkLines2(lines);
	}
	
	public void testMixedDelimterInsertLFBeforeCR() throws Exception {
		set("one\r\ntwo\r\ntre\r\n");
		
		replace(8, 0, LF);
		
		int[] lines= {3, 2, 3, 1, 0, 2, 3, 2, 0, 0};
		checkLines2(lines);
	}
	
}
