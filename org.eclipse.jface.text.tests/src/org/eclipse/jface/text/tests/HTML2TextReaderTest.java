/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc.) - [509032] Support additional tags
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

import org.eclipse.jface.internal.text.html.HTML2TextReader;

import org.eclipse.jface.text.TextPresentation;

public class HTML2TextReaderTest {

	private static final boolean DEBUG= false;

	private static final String LD= System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$


	/**
	 * @param input input
	 * @param expectedOutput expected output
	 * @param styleRangeCount count
	 * @throws IOException test failure
	 * @deprecated pass actual style ranges
	 */
	@Deprecated
	private void verify(String input, String expectedOutput, int styleRangeCount) throws IOException {
		Reader reader= new StringReader(input);
		TextPresentation textPresentation= new TextPresentation();
		String result;
		try (HTML2TextReader htmlReader= new HTML2TextReader(reader, textPresentation)) {
			result= htmlReader.getString();
		}
		if (DEBUG)
			System.out.println("<" + result + "/>");
		assertEquals(expectedOutput, result);

		Iterator<StyleRange> styleRangeIterator= textPresentation.getAllStyleRangeIterator();
		List<StyleRange> ranges= new ArrayList<>();
		while (styleRangeIterator.hasNext()) {
			ranges.add(styleRangeIterator.next());
		}

		assertEquals("Incorrect number of style ranges", styleRangeCount, ranges.size());

		Collections.sort(ranges, (r1, r2) -> r1.start - r2.start);

		for (int i= 0; i < ranges.size() - 1; i++) {
			StyleRange range1= ranges.get(i);
			StyleRange range2= ranges.get(i + 1);

			if (range1.start + range1.length > range2.start) {
				assertTrue("StyleRanges overlap", false);
			}
		}

	}

	private void verify(String input, String expectedOutput, StyleRange[] styleRanges) throws IOException {
		Reader reader= new StringReader(input);
		TextPresentation textPresentation= new TextPresentation();
		String result;
		try (HTML2TextReader htmlReader= new HTML2TextReader(reader, textPresentation)) {
			result= htmlReader.getString();
		}
		if (DEBUG)
			System.out.println("<" + result + "/>");
		assertEquals(expectedOutput, result);
		
		Iterator<StyleRange> styleRangeIterator= textPresentation.getAllStyleRangeIterator();
		List<StyleRange> ranges= new ArrayList<>();
		while (styleRangeIterator.hasNext()) {
			ranges.add(styleRangeIterator.next());
		}
		
		Collections.sort(ranges, (r1, r2) -> r1.start - r2.start);
		
		assertEquals(Arrays.asList(styleRanges), ranges);
		
		for (int i= 0; i < ranges.size() - 1; i++) {
			StyleRange range1= ranges.get(i);
			StyleRange range2= ranges.get(i + 1);
			
			if (range1.start + range1.length > range2.start) {
				assertTrue("StyleRanges overlap", false);
			}
		}
		
	}

	@Test
	public void test0() throws IOException{
		String string= "<code>3<5<code>";
		String expected= "3<5";
		verify(string, expected, 0);
	}
	
	@Test
	public void test1() throws IOException{
		String string= "<dl><dt>@author</dt><dd>Foo Bar</dd></dl>";
		String expected= LD+ "@author"+LD+"\tFoo Bar"+LD;
		verify(string, expected, 1);
	}
	
	@Test
	public void test2() throws IOException{
		String string= "<code>3>5<code>";
		String expected= "3>5";
		verify(string, expected, 0);
	}

	@Test
	public void test3() throws IOException{
		String string= "<a href= \"<p>this is only a string - not a tag<p>\">text</a>";
		String expected= "text";
		verify(string, expected, 0);
	}

	@Test
	public void test4() throws IOException{
		String string= 	"<html><body text=\"#000000\" bgcolor=\"#FFFF88\"><font size=-1><h5>void p.Bb.fes()</h5><p><dl><dt>Parameters:</dt><dd><b>i</b> fred or <code>null</code></dd></dl></font></body></html>";
		String expected= "void p.Bb.fes()"+ LD + LD + LD+ "Parameters:"+ LD + "\ti fred or null"+LD;
		verify(string, expected, 3);
	}
	
	@Test
	public void test5() throws IOException{
		String string= "<code>1<2<3<4</code>";
		String expected= "1<2<3<4";
		verify(string, expected, 0);
	}

	@Test
	public void test6() throws IOException{
		//test for bug 19070
		String string= "<p>Something.<p>Something more.";
		String expected= LD + "Something." + LD + "Something more.";
		verify(string, expected, 0);
	}
	
	@Test
	public void testEntity1() throws IOException {
		String string= "&amp;";
		String expected= "&";
		verify(string, expected, 0);
	}

	@Test
	public void testEntity2() throws IOException {
		String string= "&unknown;";
		String expected= "&unknown;";
		verify(string, expected, 0);
	}
	
	@Test
	public void testBug367378() throws IOException {
		verify("<head>", "", 0);
		verify("<head>some styles</html>", "", 0);

		char[] cb= new char[20];
		StringReader reader= new StringReader("<head>");
		try (HTML2TextReader r= new HTML2TextReader(reader, null)) {
			r.read(cb);
		}
		assertTrue(Arrays.equals(new char[20], cb));
	}

	@Test
	public void testComments() throws Exception {
		String string= "<!-- begin-user-doc -->no comment<!-- end-user-doc -->";
		String expected= "no comment";
		verify(string, expected, 0);
	}
	
	@Test
	public void testStyles1() throws IOException {
		String string= "<b>Hello World</b>";
		String expected= "Hello World";
		verify(string, expected, 1);
	}

	@Test
	public void testStyles2() throws IOException {
		String string= "<del>Hello World</del>";
		String expected= "Hello World";
		verify(string, expected, 1);
	}

	@Test
	public void testStyles3() throws IOException {
		String string= "<b><del>Hello World</del></b>";
		String expected= "Hello World";
		verify(string, expected, 1);
	}

	@Test
	public void testStyles4() throws IOException {
		String string= "<del><b>Hello World</b></del>";
		String expected= "Hello World";
		verify(string, expected, 1);
	}

	@Test
	public void testStyles5() throws IOException {
		String string= "<b>This <del> is a </del> test</b>";
		String expected= "This is a test";
		verify(string, expected, 3);
	}

	@Test
	public void testStyles6() throws IOException {
		String string= "<del>This <b> is a </b> test</del>";
		String expected= "This is a test";
		verify(string, expected, 3);
	}

	@Test
	public void testStyles7() throws IOException {
		String string= "<b>This<del>is a</del>test</b>";
		String expected= "Thisis atest";
		verify(string, expected, 3);
	}

	@Test
	public void testStyles8() throws IOException {
		String string= "<del>This<b>is a</b>test</del>";
		String expected= "Thisis atest";
		verify(string, expected, 3);
	}

	@Test
	public void testStyles9() throws IOException {
		String string= "<b>This <del>is <b>yet</b> another</del> test</b>";
		String expected= "This is yet another test";
		verify(string, expected, 3);
	}

	@Test
	public void testStyles10() throws IOException {
		String string= "<del>This <b>is <del>yet</del> another </b>test</del>";
		String expected= "This is yet another test";
		verify(string, expected, 3);
	}
	
	@Test
	public void testStylesWithPre() throws IOException {
		String string= "I am <b>bold</b>." + LD + "<p>" + LD + "<pre>One" + LD + LD + "<b>T</b>hree.</pre>" + LD + "<p>" + LD + "<b>Author:</b> me.";
		String expected= "I am bold. " + LD + "One" + LD + LD + "Three. " + LD + "Author: me.";
		StyleRange[] ranges= {
				new StyleRange(5, 4, null, null, SWT.BOLD),
				new StyleRange(14 + 3*LD.length(), 1, null, null, SWT.BOLD),
				new StyleRange(21 + 4*LD.length(), 7, null, null, SWT.BOLD)
		};
		verify(string, expected, ranges);
	}
	
	@Test
	public void testCombineBoldItalicStriker() throws IOException {
		String string= "<strong>strong</strong>,<em>italic</em>,<s>strike</s>," +
				"<strong><em>strongItalic</em></strong>,<s><strong>strongStrike</strong></s>," +
				"<em><s>italicStrike</s></em>," +
				"<em><strong><s>strongItalicStrike</s></strong></em>," +
				"none";
		String expected= "strong,italic,strike,strongItalic,strongStrike,italicStrike,strongItalicStrike,none";
		StyleRange[] ranges= {
				new StyleRange(0, 6, null, null, SWT.BOLD),
				new StyleRange(7, 6, null, null, SWT.ITALIC),
				strike(new StyleRange(14, 6, null, null)),
				new StyleRange(21, 12, null, null, SWT.BOLD | SWT.ITALIC),
				strike(new StyleRange(34, 12, null, null, SWT.BOLD)),
				strike(new StyleRange(47, 12, null, null, SWT.ITALIC)),
				strike(new StyleRange(60, 18, null, null, SWT.BOLD | SWT.ITALIC)),
		};
		verify(string, expected, ranges);
	}

	private StyleRange strike(StyleRange styleRange) {
		styleRange.strikeout = true;
		return styleRange;
	}
}

