/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
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
 *     Cagatay Calli <ccalli@gmail.com> - [find/replace] retain caps when replacing - https://bugs.eclipse.org/bugs/show_bug.cgi?id=28949
 *     Cagatay Calli <ccalli@gmail.com> - [find/replace] define & fix behavior of retain caps with other escapes and text before \C - https://bugs.eclipse.org/bugs/show_bug.cgi?id=217061
 *******************************************************************************/
package org.eclipse.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Tests the FindRepla ceDocumentAdapter.
 *
 * @since 3.1
 */
public class FindReplaceDocumentAdapterTest {

	private static final boolean BUG_392594= true;

	private Document fDocument;

	@Before
	public void setUp() {

		fDocument= new Document();

		String text;
		text= "package TestPackage;\n" + //$NON-NLS-1$
		"/*\n" + //$NON-NLS-1$
		"* comment\n" + //$NON-NLS-1$
		"*/\n" + //$NON-NLS-1$
		"	public class Class {\n" + //$NON-NLS-1$
		"		// comment1\n" + //$NON-NLS-1$
		"		public void method1() {\n" + //$NON-NLS-1$
		"		}\n" + //$NON-NLS-1$
		"		// comment2\n" + //$NON-NLS-1$
		"		public void method2() {\n" + //$NON-NLS-1$
		"		}\n" + //$NON-NLS-1$
		"	}\n" + //$NON-NLS-1$S
		"// Gel\u00F6st"; //$NON-NLS-1$S

		fDocument.set(text);
	}

	@After
	public void tearDown () {
		fDocument= null;
	}

	@org.junit.Test
	public void testFind() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {
			IRegion result= new Region(8, 11);

			// Find case-sensitive
			IRegion r= findReplaceDocumentAdapter.find(0, "TestPackage", true, true, false, false); //$NON-NLS-1$
			assertEquals(result, r);
			r= findReplaceDocumentAdapter.find(0, "testpackage", true, true, false, false); //$NON-NLS-1$
			assertNull(r);

			// Find non-case-sensitive
			r= findReplaceDocumentAdapter.find(0, "TestPackage", true, false, false, false); //$NON-NLS-1$
			assertEquals(r, result);
			r= findReplaceDocumentAdapter.find(0, "testpackage", true, false, false, false); //$NON-NLS-1$
			assertEquals(r, result);

		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}

	@Test
	public void testFindCaretInMiddleOfWord() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {

			// Find forward when caret is inside word
			IRegion r= findReplaceDocumentAdapter.find(12, "TestPackage", true, false, false, false); //$NON-NLS-1$
			assertNull(r);

			// Find backward when caret is inside word
			r= findReplaceDocumentAdapter.find(12, "TestPackage", false, false, false, false); //$NON-NLS-1$
			assertNull(r);

		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}

	@Test
	public void testFindCaretAtWordStart() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {

			// Find forward when caret is just before a word
			IRegion r= findReplaceDocumentAdapter.find(8, "TestPackage", true, false, false, false); //$NON-NLS-1$
			assertEquals(new Region(8, 11), r);

		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}

	@Test
	public void testFindCaretAtEndStart() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {

			// Find forward when caret is just before a word
			IRegion r= findReplaceDocumentAdapter.find(19, "TestPackage", false, false, false, false); //$NON-NLS-1$
			assertEquals(new Region(8, 11), r);

		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}

	/**
	 * Test case for: https://bugs.eclipse.org/bugs/show_bug.cgi?id=74993
	 */
	@Test
	public void testBug74993() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {
			IRegion r= findReplaceDocumentAdapter.find(12, "\\w+", false, false, false, true); //$NON-NLS-1$
			assertEquals(new Region(6, 1), r);

		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}

	/**
	 * Test case for: https://bugs.eclipse.org/386751
	 */
	@Test
	public void testBug386751() {
		FindReplaceDocumentAdapter adapter= new FindReplaceDocumentAdapter(fDocument);
		try {
			IRegion result= adapter.find(0, ".", true, false, true, false);
			assertNull(result);
		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}

	@Test
	public void testUTF8Pattern() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {
			IRegion result= new Region(153, 6);

			// Find case-sensitive
			IRegion r= findReplaceDocumentAdapter.find(0, "Gel\u00F6st", true, true, false, false); //$NON-NLS-1$
			assertEquals(result, r);
			r= findReplaceDocumentAdapter.find(0, "Gel\u00F6st", true, true, false, false); //$NON-NLS-1$
			assertEquals(result, r);

			// Find non-case-sensitive
			r= findReplaceDocumentAdapter.find(0, "Gel\u00D6st", true, false, false, false); //$NON-NLS-1$
			assertEquals(result, r);
			r= findReplaceDocumentAdapter.find(0, "Gel\u00D6st", true, false, false, false); //$NON-NLS-1$
			assertEquals(result, r);

		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}

	@Test
	public void testReplace() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {
			findReplaceDocumentAdapter.find(0, "public", true, true, false, false); //$NON-NLS-1$
			IRegion r= findReplaceDocumentAdapter.replace("private", false); //$NON-NLS-1$
			assertNotNull(r);

			findReplaceDocumentAdapter.find(0, "public", true, true, false, false); //$NON-NLS-1$
			r= findReplaceDocumentAdapter.replace("private", false); //$NON-NLS-1$
			assertNotNull(r);

			findReplaceDocumentAdapter.find(0, "public", true, true, false, false); //$NON-NLS-1$
			r= findReplaceDocumentAdapter.replace("private", false); //$NON-NLS-1$
			assertNotNull(r);

			// Search again: there will be no match
			findReplaceDocumentAdapter.find(0, "public", true, true, false, false); //$NON-NLS-1$
			try {
				findReplaceDocumentAdapter.replace("private", false); //$NON-NLS-1$
			} catch (IllegalStateException e) {
				assertTrue(true);
			}

			String text=
				"package TestPackage;\n" + //$NON-NLS-1$
				"/*\n" + //$NON-NLS-1$
				"* comment\n" + //$NON-NLS-1$
				"*/\n" + //$NON-NLS-1$
				"	private class Class {\n" + //$NON-NLS-1$
				"		// comment1\n" + //$NON-NLS-1$
				"		private void method1() {\n" + //$NON-NLS-1$
				"		}\n" + //$NON-NLS-1$
				"		// comment2\n" + //$NON-NLS-1$
				"		private void method2() {\n" + //$NON-NLS-1$
				"		}\n" + //$NON-NLS-1$
				"	}\n" + //$NON-NLS-1$
				"// Gel\u00f6st"; //$NON-NLS-1$S
			assertEquals(text, fDocument.get());

		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}

	@Test
	@Ignore
	public void _testRegexReplace() throws Exception {
		fDocument.set(
				"UnixWindowsMacInferred\n" +
				"Chars"
		);
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		regexReplace("Unix", "$0\\n", findReplaceDocumentAdapter);
		regexReplace("(Windows)", "$1\\r\\n", findReplaceDocumentAdapter);
		regexReplace("(M)ac", "\\0\\r", findReplaceDocumentAdapter);
		regexReplace("(Inferred)", "\\1\\R", findReplaceDocumentAdapter);
		regexReplace("Chars", "\\\\, \\xF6, \\u00F6, \\t, \\n, \\r, \\f, \\a, \\e, \\cF", findReplaceDocumentAdapter);

		String text= "Unix\nWindows\r\nMac\rInferred\n\n\\, \u00F6, \u00F6, \t, \n, \r, \f, \u0007, \u001B, \u0006";
		assertEquals(text, fDocument.get());
	}

	@Test
	public void testRegexReplace2() throws Exception {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);

		fDocument.set("foo");
		regexReplace("foo", "\\00", findReplaceDocumentAdapter);
		assertEquals("foo0", fDocument.get());

		fDocument.set("foo");
		regexReplace("foo", "\\010", findReplaceDocumentAdapter);
		assertEquals("foo10", fDocument.get());

		fDocument.set("foo");
		regexReplace("foo", "$00", findReplaceDocumentAdapter);
		assertEquals("foo0", fDocument.get());

		fDocument.set("foo");
		regexReplace("foo", "$010", findReplaceDocumentAdapter);
		assertEquals("foo10", fDocument.get());
	}

	@Test
	public void testRegexReplace3() throws Exception {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);

		fDocument.set("foo");
		regexReplace("(f)oo", "\\10", findReplaceDocumentAdapter);
		assertEquals("f0", fDocument.get());

		fDocument.set("foo");
		regexReplace("(f)oo", "$10", findReplaceDocumentAdapter);
		assertEquals("f0", fDocument.get());
	}

	@Test
	public void testRegexReplace_invalidRegex() throws Exception {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(fDocument);

		fDocument.set("foo");
		assertThrows(PatternSyntaxException.class, () -> regexReplace("foo", "foo\\", findReplaceDocumentAdapter));
		assertEquals("foo", fDocument.get());

		findReplaceDocumentAdapter.replace("foo" + System.lineSeparator(), true);
		assertEquals("foo" + System.lineSeparator(), fDocument.get());
	}

	/*
	 * @since 3.4
	 */
	@Test
	public void testRegexRetainCase() throws Exception {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);

		fDocument.set("foo");
		regexReplace("foo", "xyz\\Cbar\\Cfar", findReplaceDocumentAdapter);
		assertEquals("xyzbarfar", fDocument.get());

		fDocument.set("FOO");
		regexReplace("FOO", "xyz\\Cbar\\Cfar", findReplaceDocumentAdapter);
		assertEquals("xyzBARFAR", fDocument.get());

		fDocument.set("Foo");
		regexReplace("Foo", "xyz\\Cbar\\Cfar", findReplaceDocumentAdapter);
		assertEquals("xyzBarFar", fDocument.get());

		/* Current behavior - may seem strange but it's expected
		 * Retain case does not apply inside groups for now.
		 */
		fDocument.set("Foox");
		regexReplace("F(oo)x", "\\C$1", findReplaceDocumentAdapter);
		assertEquals("oo", fDocument.get());

		fDocument.set("Foo");
		regexReplace("Foo", "xyz\\Cna\\u00EFve\\xFF\\C\\xFF", findReplaceDocumentAdapter);
		assertEquals("xyzNa\u00EFve\u00FF\u0178", fDocument.get());

		fDocument.set("FOO");
		regexReplace("FOO", "xyz\\Cna\\u00EFve\\xFF", findReplaceDocumentAdapter);
		assertEquals("xyzNA\u00CFVE\u0178", fDocument.get());

		fDocument.set("A");
		regexReplace("A", "\\Ci", findReplaceDocumentAdapter);
		assertEquals("I", fDocument.get());

		Locale currentLocale= Locale.getDefault();
		try {
			Locale.setDefault(new Locale("tr"));
			fDocument.set("A");
			regexReplace("A", "\\Ci", findReplaceDocumentAdapter);
			assertEquals("\u0130", fDocument.get());

			fDocument.set("a");
			regexReplace("a", "\\CI", findReplaceDocumentAdapter);
			assertEquals("\u0131", fDocument.get());
		} finally {
			Locale.setDefault(currentLocale);
		}
	}

	private void regexReplace(String find, String replace, FindReplaceDocumentAdapter findReplaceDocumentAdapter) throws BadLocationException {
		findReplaceDocumentAdapter.find(0, find, true, true, false, true);
		IRegion r= findReplaceDocumentAdapter.replace(replace, true);
		assertNotNull(r);
	}

	@Test
	public void testRegexFindLinebreak() throws Exception {
		FindReplaceDocumentAdapter adapter= new FindReplaceDocumentAdapter(fDocument);
		String contents= "Unix\nWindows\r\nMac\rEnd";
		fDocument.set(contents);

		int n= contents.indexOf('\n');
		int rn= contents.indexOf("\r\n");
		int r= contents.indexOf("\rEnd");

		IRegion region= adapter.find(0, "\\R", true, false, false, true);
		assertEquals(new Region(n, 1), region);

		region= adapter.find(n + 1, "\\R", true, false, false, true);
		assertEquals(new Region(rn, 2), region);

		region= adapter.find(rn + 2, "\\R", true, false, false, true);
		assertEquals(new Region(r, 1), region);

		region= adapter.find(r + 1, "\\R", true, false, false, true);
		assertNull(region);
	}

	@Test
	public void testRegexFindLinebreak2_fail() throws Exception {
		FindReplaceDocumentAdapter adapter= new FindReplaceDocumentAdapter(fDocument);
		String contents= "Unix\n[\\R]\\R\r\n";
		fDocument.set(contents);

		int n= contents.indexOf('\n');
		int rn= contents.indexOf("\r\n");

		IRegion region= adapter.find(0, "[a-zA-Z\\t{\\\\R}]*\\{?\\R", true, false, false, true);
		assertEquals(new Region(0, n + 1), region);

		region= adapter.find(n + 1, "\\Q[\\R]\\R\\E{0,1}(\\R)", true, false, false, true);
		assertEquals(new Region(n + 1, rn + 2 - (n + 1)), region);
		try {
			adapter.replace("Win\\1$1", true);
		} catch (PatternSyntaxException ex) {
			return;
		}
		fail();
	}

	@Test
	@Ignore
	public void _testRegexFindLinebreak2() throws Exception {
		FindReplaceDocumentAdapter adapter= new FindReplaceDocumentAdapter(fDocument);
		String contents= "+[\\R]\\R\r\n";
		fDocument.set(contents);

		int n= contents.indexOf('[');
		int rn= contents.indexOf("\r\n");

		IRegion region= adapter.find(0, "[a-zA-Z\\t{\\\\R}]*\\{?\\R", true, false, false, true);
		assertEquals(new Region(0, n - 1), region);

		region= adapter.find(n, "\\Q[\\R]\\R\\E{0,1}(\\R)", true, false, false, true);
		assertEquals(new Region(n, rn + 2 - n), region);
		adapter.replace("Win\\1$1", true);
		assertEquals("+Win\r\n\r\n", fDocument.get());
	}

	@Test
	public void testRegexFindLinebreak3() throws Exception {
		FindReplaceDocumentAdapter adapter= new FindReplaceDocumentAdapter(fDocument);
		String contents= "One\r\nTwo\r\n\r\nEnd";
		fDocument.set(contents);

		int two= contents.indexOf("Two");
		int end= contents.indexOf("End");

		IRegion region= adapter.find(0, "[a-zA-Z]+\\R", true, false, false, true);
		assertEquals(new Region(0, two), region);
		region= adapter.find(two, "[a-zA-Z]+\\R", true, false, false, true);
		assertEquals(new Region(two, 3 + 2), region);

		region= adapter.find(0, "[a-zA-Z]+\\R{2}", true, false, false, true);
		assertEquals(new Region(two, end - two), region);
	}

	@Test
	public void testRegexFindLinebreakIllegal() throws Exception {
		FindReplaceDocumentAdapter adapter= new FindReplaceDocumentAdapter(fDocument);
		fDocument.set("\n");

		IRegion region= null;
		try {
			region= adapter.find(0, "[\\R]", true, false, false, true);
		} catch (PatternSyntaxException e) {
			//expected
		}
		assertNull(region);

		try {
			region= adapter.find(0, "[\\s&&[^\\R]]", true, false, false, true);
		} catch (PatternSyntaxException e) {
			//expected
		}
		assertNull(region);

		try {
			region= adapter.find(0, "\\p{\\R}", true, false, false, true);
		} catch (PatternSyntaxException e) {
			//expected
		}
		assertNull(region);
	}

	@Test
	public void testIllegalState() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {
			findReplaceDocumentAdapter.replace("TestPackage", false); //$NON-NLS-1$
		} catch (IllegalStateException e) {
			Assert.assertTrue(true);
		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}

		findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {
			findReplaceDocumentAdapter.replace("TestPackage", true); //$NON-NLS-1$
		} catch (IllegalStateException e) {
			Assert.assertTrue(true);
		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}

	@Test
	public void testRegexFindStackOverflow_fail() throws Exception {
		// test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=102699

		if (BUG_392594 && System.getProperty("os.name").contains("Mac"))
			return; // VM crash on the Mac, see https://bugs.eclipse.org/392594

		FindReplaceDocumentAdapter adapter= new FindReplaceDocumentAdapter(fDocument);

		int len= 100000;
		char[] chars= new char[len];
		Arrays.fill(chars, '\n');
		chars[0]= '{';
		chars[len - 1]= '}';
		fDocument.set(new String(chars));

		try {
			adapter.find(0, "\\{(.|[\\r\\n])*\\}", true, false, false, true);
		} catch (PatternSyntaxException ex) {
			return;
		}
		fail();
	}
}
