/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
package org.eclipse.jface.tests.examples.databinding.mask.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.examples.databinding.mask.internal.EditMaskLexerAndToken;
import org.junit.Before;
import org.junit.Test;

public class EditMaskLexerAndTokenTest {

	private EditMaskLexerAndToken token;

	@Before
	public void setUp() throws Exception {
		token = new EditMaskLexerAndToken();
	}

	@Test
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

	@Test
	public void testInitWithLiteral() throws Exception {
		token.initializeEditMask("(", 0);
		assertEquals("Literals automatically set their input", "(", token.getInput());
		assertFalse("Literals don't accept anything", token.accept("("));
		assertTrue("literals are read-only", token.isReadOnly());
		assertTrue("Literals are complete", token.isComplete());
		assertFalse("Literals cannot accept characters", token.canAcceptMoreCharacters());
	}

	@Test
	public void testInitWithBackslashLiteral() throws Exception {
		token.initializeEditMask("\\#", 0);
		assertEquals("Should get backslash literal", "#", token.getInput());
	}

	@Test
	public void testAcceptWithValidInputAndEmpty() throws Exception {
		token.initializeEditMask("#", 0);
		assertTrue("Should accept a 0", token.accept("0"));
	}

	@Test
	public void testAcceptWhenParserCannotAcceptMoreCharacters() throws Exception {
		token.initializeEditMask("#", 0);
		assertTrue("Should accept a 0", token.accept("0"));
		assertFalse("Should not accept a 0 -- input full", token.accept("0"));
	}

	@Test
	public void testGetInput() throws Exception {
		token.initializeEditMask("#", 0);
		assertTrue("Should accept a #", token.accept("0"));
		assertEquals(token.getInput(), "0");
	}

	@Test
	public void testClear_withNonLiteral() throws Exception {
		token.initializeEditMask("#", 0);
		assertTrue("Should accept a 0", token.accept("0"));
		assertNotNull("Input should not be null", token.getInput());
		token.clear();
		assertNull("Input should be null after clear", token.getInput());
	}

	@Test
	public void testClear_withLiteral() throws Exception {
		token.initializeEditMask("(", 0);
		assertNotNull("Input should not be null", token.getInput());
		token.clear();
		assertNotNull("Input should still not be null after clear of read-only literal", token.getInput());
	}

	@Test
	public void testIsComplete_withNonLiteral() throws Exception {
		token.initializeEditMask("#", 0);
		assertFalse("should not be complete", token.isComplete());
		token.accept("1");
		assertTrue("should be complete", token.isComplete());
		token.clear();
		assertFalse("should not be complete", token.isComplete());
	}

}
