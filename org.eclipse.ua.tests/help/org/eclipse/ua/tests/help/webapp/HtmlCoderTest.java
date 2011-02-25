/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import junit.framework.TestCase;

import org.eclipse.help.internal.webapp.data.UrlUtil;

public class HtmlCoderTest extends TestCase {
	
	public void testEncodeEmpty() {
		String encoded = UrlUtil.htmlEncode(null);
		assertNull(encoded);
	}	

	/**
	 * Verify that alpha characters are not encoded
	 */
	public void testEncodeAlpha() {
		final String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String encoded = UrlUtil.htmlEncode(letters);
		assertEquals(letters, encoded);
	}
	
	/**
	 * Verify that alpha characters are not encoded
	 */
	public void testEncodeNumeric() {
		final String numbers = "1234567890";
		String encoded = UrlUtil.htmlEncode(numbers);
		assertEquals(numbers, encoded);
	}
	
	/**
	 * Verify that space is not encoded
	 */
	public void testEncodeSpace() {
		final String spaces = "  ";
		String encoded = UrlUtil.htmlEncode(spaces);
		assertEquals(spaces, encoded);
	}

	/**
	 * Verify that quote is encoded
	 */
	public void testEncodeQuote() {
		final String source = "\'";
		String encoded = UrlUtil.htmlEncode(source);
		assertNotSame(source, encoded);
	}
	
	/**
	 * Verify that less than is encoded
	 */
	public void testEncodeLt() {
		final String source = "<";
		String encoded = UrlUtil.htmlEncode(source);
		assertNotSame(source, encoded);
	}	

	/**
	 * Verify that greater than is encoded
	 */
	public void testEncodeGt() {
		final String source = ">";
		String encoded = UrlUtil.htmlEncode(source);
		assertNotSame(source, encoded);
	}

	/**
	 * Verify that ampersand is encoded
	 */
	public void testEncodeAmp() {
		final String source = "&";
		String encoded = UrlUtil.htmlEncode(source);
		assertNotSame(source, encoded);
	}

	/**
	 * Verify that ampersand is encoded
	 */
	public void testEncodeBackslash() {
		final String source = "\\";
		String encoded = UrlUtil.htmlEncode(source);
		assertNotSame(source, encoded);
	}

	/**
	 * Verify that newline is encoded
	 */
	public void testEncodeNewline() {
		final String source = "\n";
		String encoded = UrlUtil.htmlEncode(source);
		assertNotSame(source, encoded);
	}

	/**
	 * Verify that CR is encoded
	 */
	public void testEncodeCarriageReturn() {
		final String source = "\r";
		String encoded = UrlUtil.htmlEncode(source);
		assertNotSame(source, encoded);
	}

	/**
	 * Verify that accented character is not encoded
	 */
	public void testNoEncodeAccented() {
		final String source = "\u00c1";
		String encoded = UrlUtil.htmlEncode(source);
		assertEquals(source, encoded);
	}
	
	/**
	 * Verify that Chinese character is not encoded
	 */
	public void testNoEncodeChinese() {
		final String source = "\u4e01";
		String encoded = UrlUtil.htmlEncode(source);
		assertEquals(source, encoded);
	}

}
