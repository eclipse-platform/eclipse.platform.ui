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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Tests the FindReplaceDocumentAdapter.
 * 
 * @since 3.1
 */
public class FindReplaceDocumentAdapterTest extends TestCase {
	
	private Document fDocument;
	
	
	public FindReplaceDocumentAdapterTest(String name) {
		super(name);
	}
	
	
	protected void setUp() {
		
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
		"// Gelöst"; //$NON-NLS-1$S
	
		fDocument.set(text);
	}
	
	public static Test suite() {
		return new TestSuite(FindReplaceDocumentAdapterTest.class); 
	}
	
	protected void tearDown () {
		fDocument= null;
	}
	
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
	public void testBug74993() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {
			IRegion r= findReplaceDocumentAdapter.find(12, "\\w+", false, false, false, true); //$NON-NLS-1$
			assertEquals(new Region(6, 1), r);
			
		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}
	
	public void testUTF8Pattern() {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter= new FindReplaceDocumentAdapter(fDocument);
		try {
			IRegion result= new Region(153, 6);
			
			// Find case-sensitive
			IRegion r= findReplaceDocumentAdapter.find(0, "Gelöst", true, true, false, false); //$NON-NLS-1$
			assertEquals(result, r);
			r= findReplaceDocumentAdapter.find(0, "Gelöst", true, true, false, false); //$NON-NLS-1$
			assertEquals(result, r);
			
			// Find non-case-sensitive
			r= findReplaceDocumentAdapter.find(0, "GelÖst", true, false, false, false); //$NON-NLS-1$
			assertEquals(result, r);
			r= findReplaceDocumentAdapter.find(0, "GelÖst", true, false, false, false); //$NON-NLS-1$
			assertEquals(result, r);
			
		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}
	
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
				"// Gelöst"; //$NON-NLS-1$S
			assertEquals(text, fDocument.get());
			
		} catch (BadLocationException e) {
			Assert.assertTrue(false);
		}
	}
	
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
}
