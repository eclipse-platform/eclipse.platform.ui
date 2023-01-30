/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.examples.databinding.mask.EditMaskParseException;
import org.eclipse.jface.examples.databinding.mask.internal.EditMaskParser;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class EditMaskParserTest {

	private EditMaskParser parser;

	@Before
	public void setUp() throws Exception {
		parser = new EditMaskParser("(###) ###-####");
	}

	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.mask.internal.EditMaskParser#EditMaskParser(java.lang.String)}.
	 */
	@Test
	public void testEditMaskParser_validMask() {
		new EditMaskParser("(###) ###-####");
	}

	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.mask.internal.EditMaskParser#EditMaskParser(java.lang.String)}.
	 */
	@Test
	public void testEditMaskParser_invalidMask() {
		assertThrows(EditMaskParseException.class, () -> new EditMaskParser("(###) ###-####\\"));
	}

	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.mask.internal.EditMaskParser#setInput(java.lang.String)}.
	 */
	@Test
	public void testSetInput() {
		parser.setInput("63a0) 5*55-1\\212abc9");
		assertEquals("Unformatted input", "6305551212", parser.getRawResult());
		assertEquals("Formatted input", "(630) 555-1212", parser.getFormattedResult());
	}

	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.mask.internal.EditMaskParser#setInput(java.lang.String)}.
	 */
	@Test
	public void testSetInput_incomplete() {
		parser.setInput("6a0) 5*5-1\\12");
		assertEquals("Unformatted input", "6055112", parser.getRawResult());
		assertEquals("Formatted input", "(605) 511-2   ", parser.getFormattedResult());
	}

	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.mask.internal.EditMaskParser#isComplete()}.
	 */
	@Test
	public void testIsComplete() {
		parser.setInput("63a0) 5*55-1\\212");
		assertTrue("complete", parser.isComplete());
		parser.setInput("6a0) 5*5-1\\12");
		assertFalse("incomplete", parser.isComplete());
	}

	@Test
	public void testSetPlaceholder() throws Exception {
		parser.setInput("6a0) 5*5-1\\12");
		assertEquals("Formatted input", "(605) 511-2   ", parser.getFormattedResult());
		parser.setPlaceholder('_');
		assertEquals("Formatted input", "(605) 511-2___", parser.getFormattedResult());
	}

	/**
	 * Test method for {@link org.eclipse.jface.examples.databinding.mask.internal.EditMaskParser#getNextInputPosition(int)}.
	 */
	@Test
	public void testGetNextInputPosition() {
		assertEquals("Skip leading (", 1, parser.getNextInputPosition(0));
		assertEquals("Position 1 is good", 1, parser.getNextInputPosition(1));
		assertEquals("Skip )<space>", 6, parser.getNextInputPosition(4));
	}

	@Test
	public void testGetFirstIncompleteInputPosition() throws Exception {
		assertEquals("1st position incomplete", 1, parser.getFirstIncompleteInputPosition());
		parser.setInput("6a0) 5*5-1\\12");
		assertEquals("11th position incomplete", 11, parser.getFirstIncompleteInputPosition());
		parser.setInput("63a0) 5*55-1\\212");
		assertEquals("all complete", -1, parser.getFirstIncompleteInputPosition());
	}
}

