/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import org.eclipse.help.internal.webapp.data.UrlUtil;

import junit.framework.TestCase;

/**
 * Tests for the code which identifies the browser kind and version based upon the 
 * User-Agent attribute of the HTTP request header. Each test uses a string derived
 * from a specific browser.
 */
public class BrowserIdentificationTest extends TestCase {
	
	public void testIE_6() {
		final String agent = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)";
		assertTrue(UrlUtil.isIE(agent));
		assertFalse(UrlUtil.isMozilla(agent));
		assertFalse(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertFalse(UrlUtil.isGecko(agent));
		assertEquals("6.0", UrlUtil.getIEVersion(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	}

	public void testWindowsFirefox_1_8() {
		final String agent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.8) Gecko/20061025 (CK-IBM) Firefox/1.5.0.8";
		assertFalse(UrlUtil.isIE(agent));
		assertTrue(UrlUtil.isMozilla(agent));
		assertFalse(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertTrue(UrlUtil.isGecko(agent));
		assertEquals("1.8.0.8", UrlUtil.getMozillaVersion(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	}

	public void testGTKFirefox_1_4() {
		final String agent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.4) Gecko/20030922";
		assertFalse(UrlUtil.isIE(agent));
		assertTrue(UrlUtil.isMozilla(agent));
		assertFalse(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertTrue(UrlUtil.isGecko(agent));
		assertEquals("1.4", UrlUtil.getMozillaVersion(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	}
	
	public void testGTKKonqueror_3_1() {
		final String agent = "Mozilla/5.0 (compatible; Konqueror/3.1; Linux)";
		assertFalse(UrlUtil.isIE(agent));
		assertTrue(UrlUtil.isMozilla(agent));
		assertFalse(UrlUtil.isOpera(agent));
		assertTrue(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertFalse(UrlUtil.isGecko(agent));
		assertFalse(UrlUtil.isAdvanced(agent));
	}
	
	public void testMacMozilla1_7_3 () { 
		final String agent = "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.7.3) Gecko/20040910";
		assertFalse(UrlUtil.isIE(agent));
		assertTrue(UrlUtil.isMozilla(agent));
		assertFalse(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertTrue(UrlUtil.isGecko(agent));
		assertEquals("1.7.3", UrlUtil.getMozillaVersion(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	} 	
	
	public void testSafari_417_8 () { 
		final String agent = "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/417.9 (KHTML, like Gecko) Safari/417.8";
		assertFalse(UrlUtil.isIE(agent));
		assertTrue(UrlUtil.isMozilla(agent));
		assertFalse(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertTrue(UrlUtil.isSafari(agent));
		assertFalse(UrlUtil.isGecko(agent));
		assertEquals("417", UrlUtil.getSafariVersion(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	} 
	
	public void testOpera_9() {
		final String agent = "Opera/9.02 (Windows NT 5.1; U; en)";
		assertFalse(UrlUtil.isIE(agent));
		assertFalse(UrlUtil.isMozilla(agent));
		assertTrue(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertFalse(UrlUtil.isGecko(agent));
		assertEquals("9.02", UrlUtil.getOperaVersion(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	}

	public void testOpera_9_IEMode () { 
		final String agent = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; en) Opera 9.02";
		assertTrue(UrlUtil.isIE(agent));
		assertFalse(UrlUtil.isMozilla(agent));
		assertTrue(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertFalse(UrlUtil.isGecko(agent));
		assertEquals("6.0", UrlUtil.getIEVersion(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	} 

	public void testXulRunnerOnUbuntu() {
		final String agent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9) Gecko";
		assertFalse(UrlUtil.isIE(agent));
		assertTrue(UrlUtil.isMozilla(agent));
		assertFalse(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertTrue(UrlUtil.isGecko(agent));
		assertEquals("1.9", UrlUtil.getMozillaVersion(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	}
	
	public void testXulRunnerTruncated() {
		final String agent = "Mozilla/5.0 (X11; U; Linux i686;";
		assertFalse(UrlUtil.isIE(agent));
		assertTrue(UrlUtil.isMozilla(agent));
		assertFalse(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertFalse(UrlUtil.isGecko(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	}
	
	public void testFirefox3() {
		final String agent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.13) Gecko/2008031";
		assertFalse(UrlUtil.isIE(agent));
		assertTrue(UrlUtil.isMozilla(agent));
		assertFalse(UrlUtil.isOpera(agent));
		assertFalse(UrlUtil.isKonqueror(agent));
		assertFalse(UrlUtil.isSafari(agent));
		assertTrue(UrlUtil.isGecko(agent));
		assertEquals("1.8.1.13", UrlUtil.getMozillaVersion(agent));
		assertTrue(UrlUtil.isAdvanced(agent));
	}
	
	
	
}
