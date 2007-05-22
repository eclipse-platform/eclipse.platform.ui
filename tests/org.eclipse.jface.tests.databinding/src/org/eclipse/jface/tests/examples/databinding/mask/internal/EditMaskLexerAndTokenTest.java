/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.examples.databinding.mask.internal;

import junit.framework.TestCase;

import org.eclipse.jface.examples.databinding.mask.internal.EditMaskLexerAndToken;

public class EditMaskLexerAndTokenTest extends TestCase {

	private EditMaskLexerAndToken token;
	
	protected void setUp() throws Exception {
		token = new EditMaskLexerAndToken();
	}

	public void testInitWithNumeric() throws Exception {
		token.initializeEditMask("#", 0);
		assertTrue("Should accept a digit", token.accept("0"));
		token.clear();
		assertTrue("Should accept a digit", token.accept("1"));
		token.clear();
		assertTrue("Should accept a digit", token.accept("2"));
		token.clear();
		assertTrue("Should accept a digit", token.accept("3"));
		token.clear();
		assertTrue("Should accept a digit", token.accept("4"));
		token.clear();
		assertTrue("Should accept a digit", token.accept("5"));
		token.clear();
		assertTrue("Should accept a digit", token.accept("6"));
		token.clear();
		assertTrue("Should accept a digit", token.accept("7"));
		token.clear();
		assertTrue("Should accept a digit", token.accept("8"));
		token.clear();
		assertTrue("Should accept a digit", token.accept("9"));
		token.clear();
		assertFalse("Should not accept an alpha", token.accept("A"));
		token.clear();
		assertFalse("Should not accept an alpha", token.accept("z"));
		assertFalse("Placeholders are not read-only", token.isReadOnly());
	}
	
	public void testInitWithLiteral() throws Exception {
		token.initializeEditMask("(", 0);
		assertEquals("Literals automatically set their input", "(", token.getInput());
		assertFalse("Literals don't accept anything", token.accept("("));
		assertTrue("literals are read-only", token.isReadOnly());
		assertTrue("Literals are complete", token.isComplete());
		assertFalse("Literals cannot accept characters", token.canAcceptMoreCharacters());
	}
	
	public void testInitWithBackslashLiteral() throws Exception {
		token.initializeEditMask("\\#", 0);
		assertEquals("Should get backslash literal", "#", token.getInput());
	}
	
	public void testAcceptWithValidInputAndEmpty() throws Exception {
		token.initializeEditMask("#", 0);
		assertTrue("Should accept a 0", token.accept("0"));
	}
	
	public void testAcceptWhenParserCannotAcceptMoreCharacters() throws Exception {
		token.initializeEditMask("#", 0);
		assertTrue("Should accept a 0", token.accept("0"));
		assertFalse("Should not accept a 0 -- input full", token.accept("0"));
	}
	
	public void testGetInput() throws Exception {
		token.initializeEditMask("#", 0);
		assertTrue("Should accept a #", token.accept("0"));
		assertEquals(token.getInput(), "0");
	}
	
	public void testClear_withNonLiteral() throws Exception {
		token.initializeEditMask("#", 0);
		assertTrue("Should accept a 0", token.accept("0"));
		assertNotNull("Input should not be null", token.getInput());
		token.clear();
		assertNull("Input should be null after clear", token.getInput());
	}
	
	public void testClear_withLiteral() throws Exception {
		token.initializeEditMask("(", 0);
		assertNotNull("Input should not be null", token.getInput());
		token.clear();
		assertNotNull("Input should still not be null after clear of read-only literal", token.getInput());
	}
	
	public void testIsComplete_withNonLiteral() throws Exception {
		token.initializeEditMask("#", 0);
		assertFalse("should not be complete", token.isComplete());
		token.accept("1");
		assertTrue("should be complete", token.isComplete());
		token.clear();
		assertFalse("should not be complete", token.isComplete());
	}

}
