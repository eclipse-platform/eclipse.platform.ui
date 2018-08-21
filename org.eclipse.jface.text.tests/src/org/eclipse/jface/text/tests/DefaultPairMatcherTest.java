/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Christian Plesner Hansen (plesner@quenta.org) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import org.junit.Test;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ICharacterPairMatcher;



/**
 * Tests for the default pair matcher.
 *
 * @since 3.3
 */
public class DefaultPairMatcherTest extends AbstractPairMatcherTest {
	
	public DefaultPairMatcherTest() {
		super(false);
	}

	/** Tests that the test case reader works */
	@Test
	public void testTestCaseReader1() {
		performReaderTest("#( )%", 3, 0, "( )");
		performReaderTest("( )%", 3, -1, "( )");
	}

	/**
	 * Close matches.
	 * 
	 * @throws BadLocationException test failure
	 */
	@Test
	public void testCloseMatches1() throws BadLocationException {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}");
		performMatch(matcher, "#()%");
		performMatch(matcher, "(#()%)");
		matcher.dispose();
	}


	/**
	 * Checks of simple situations where no matches should be found.
	 * 
	 * @throws BadLocationException test failure
	 */
	@Test
	public void testIncompleteMatch1() throws BadLocationException {
		final ICharacterPairMatcher matcher= createMatcher("()[]{}");
		performMatch(matcher, "(  %)");
		performMatch(matcher, "%(  )");
		matcher.dispose();
	}

}
