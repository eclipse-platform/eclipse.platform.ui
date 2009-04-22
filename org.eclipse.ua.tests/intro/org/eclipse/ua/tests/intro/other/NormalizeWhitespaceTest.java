/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.intro.other;

import org.eclipse.ui.internal.intro.impl.util.StringUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * Tests whitespace normalization used by SWT presentation.
 */
public class NormalizeWhitespaceTest extends TestCase {
	
	/*
	 * Returns an instance of this Test.
	 */
	public static Test suite() {
		return new TestSuite(NormalizeWhitespaceTest.class);
	}

	public void testNullString() {
		assertNull(StringUtil.normalizeWhiteSpace(null));
	}

	public void testEmptyString() {
		String result = StringUtil.normalizeWhiteSpace("");
		assertEquals("", result);
	}

	public void testSimpleString() {
		String result = StringUtil.normalizeWhiteSpace("Hello World");
		assertEquals("Hello World", result);
	}

	public void testRepeatedSpace() {
		String result = StringUtil.normalizeWhiteSpace("Hello   World");
		assertEquals("Hello World", result);
	}
	
	public void testOtherWhitespace() {
		String result = StringUtil.normalizeWhiteSpace("Hello\n\r\t World");
		assertEquals("Hello World", result);
	}

	public void testLeadingSpace() {
		String result = StringUtil.normalizeWhiteSpace(" Hello World");
		assertEquals("Hello World", result);
	}
	
	public void testTrailingSpace() {
		String result = StringUtil.normalizeWhiteSpace("Hello World ");
		assertEquals("Hello World", result);
	}
	
}
