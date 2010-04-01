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

import org.eclipse.help.internal.util.URLCoder;

public class UrlCoderTest extends TestCase {
	
	private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-={}[]:\";'<>,.?/'";
	private static final String ALPHANUMERIC = "Bxz91r";
	private static final String ALPHA = "acgrdft";
	private static final String EMPTY_STRING = "";
	private static final String ACCENTED = "seg\u00FAn cu\u00E1l oto\u00F1o";		
	private static final String CHINESE = "\u4ECA\u5929\u662F\u5929";

	private void encodeDecode(String value) {
		String encoded = URLCoder.encode(value);
		assertEquals(value, URLCoder.decode(encoded));
	}

	private void compactEncodeDecode(String value) {
		String encoded = URLCoder.compactEncode(value);
		assertEquals(value, URLCoder.decode(encoded));
	}
	
	private boolean compactEncodingIsShorter(String value) {
		String compactEncoded = URLCoder.compactEncode(value);
		String encoded = URLCoder.encode(value);
        return compactEncoded.length() < encoded.length();
	}

	public void testEncodeEmpty() {
		encodeDecode(EMPTY_STRING);
	}	

	public void testEncodeAlphabetic() {
		encodeDecode(ALPHA);
	}	
	
	public void testEncodeAlphaNumeric() {
		encodeDecode(ALPHANUMERIC);
	}
	
	public void testEncodeSpecialCharacters() {
		encodeDecode(SPECIAL_CHARACTERS);
	}	
	
	public void testEncodeAccented() {
		encodeDecode(ACCENTED);
	}
	
	public void testEncodeChinese() {
		encodeDecode(CHINESE);
	}
	
	// Compact Encodings

	public void testCompactEncodeEmpty() {
		compactEncodeDecode(EMPTY_STRING);
	}	

	public void testCompactEncodeAlphabetic() {
		compactEncodeDecode(ALPHA);
	}	
	
	public void testCompactEncodeAlphaNumeric() {
		compactEncodeDecode(ALPHANUMERIC);
	}
	
	public void testCompactEncodeSpecialCharacters() {
		compactEncodeDecode(SPECIAL_CHARACTERS);
	}	
	
	public void testCompactEncodeAccented() {
		compactEncodeDecode(ACCENTED);
	}
	
	public void testCompactEncodeChinese() {
		compactEncodeDecode(CHINESE);
	}
	
	// Verify compaction
	

	public void testCompactionEmpty() {
		assertFalse(compactEncodingIsShorter(EMPTY_STRING));
	}	

	public void testCompactionAlphabetic() {
		assertTrue(compactEncodingIsShorter(ALPHA));
	}	
	
	public void testCompactionAlphaNumeric() {
		assertTrue(compactEncodingIsShorter(ALPHANUMERIC));
	}
	
	public void testCompactionSpecialCharacters() {
		assertFalse(compactEncodingIsShorter(SPECIAL_CHARACTERS));
	}	
	
	public void testCompactionAccented() {
		assertTrue(compactEncodingIsShorter(ACCENTED));
	}
	
	public void testCompactionChinese() {
		assertFalse(compactEncodingIsShorter(CHINESE));
	}

}
