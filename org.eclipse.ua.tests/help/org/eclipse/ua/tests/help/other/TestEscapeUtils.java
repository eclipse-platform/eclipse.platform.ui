/*******************************************************************************
 * Copyright (c) 2007 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import org.eclipse.help.ui.internal.util.EscapeUtils;
import junit.framework.TestCase;

public class TestEscapeUtils extends TestCase {

	public void testEscapeEmpty() {
		assertEquals("", EscapeUtils.escapeSpecialChars(""));
		assertEquals("", EscapeUtils.escapeSpecialCharsLeavinggBold(""));
	}

	public void testEscapeSimple() {
		assertEquals("abc", EscapeUtils.escapeSpecialChars("abc"));
		assertEquals("abc", EscapeUtils.escapeSpecialCharsLeavinggBold("abc"));
	}
	
	public void testEscapeTabs() {
		assertEquals("a  bc", EscapeUtils.escapeSpecialChars("a\t\tbc"));
		assertEquals("a  bc", EscapeUtils.escapeSpecialCharsLeavinggBold("a\t\tbc"));
	}
	
	public void testEscapeAmpersand() {
		assertEquals("&amp;1&amp;", EscapeUtils.escapeSpecialChars("&1&"));
		assertEquals("&amp;1&amp;", EscapeUtils.escapeSpecialCharsLeavinggBold("&1&"));
		assertEquals("&amp;1&amp;", EscapeUtils.escapeAmpersand("&1&"));
	}

	public void testEscapeQuotes() {
		assertEquals("&quot;&quot;&apos;&apos;", EscapeUtils.escapeSpecialChars("\"\"\'\'"));
		assertEquals("&quot;&quot;&apos;&apos;", EscapeUtils.escapeSpecialCharsLeavinggBold("\"\"\'\'"));
	}

	public void testEscapePTag() {
		assertEquals("&lt;p&gt;", EscapeUtils.escapeSpecialChars("<p>"));
		assertEquals("&lt;p&gt;", EscapeUtils.escapeSpecialCharsLeavinggBold("<p>"));
	}

	public void testEscapeLowerBTag() {
		assertEquals("&lt;b&gt;", EscapeUtils.escapeSpecialChars("<b>"));
		assertEquals("<b>", EscapeUtils.escapeSpecialCharsLeavinggBold("<b>"));
	}
	
	public void testEscapeUpperBTag() {
		assertEquals("&lt;B&gt;", EscapeUtils.escapeSpecialChars("<B>"));
		assertEquals("<B>", EscapeUtils.escapeSpecialCharsLeavinggBold("<B>"));
	}
	
	public void testEscapClosingBTag() {
		assertEquals("&lt;/b&gt;", EscapeUtils.escapeSpecialChars("</b>"));
		assertEquals("</b>", EscapeUtils.escapeSpecialCharsLeavinggBold("</b>"));
		assertEquals("&lt;/B&gt;", EscapeUtils.escapeSpecialChars("</B>"));
		assertEquals("</B>", EscapeUtils.escapeSpecialCharsLeavinggBold("</B>"));
	}

}
