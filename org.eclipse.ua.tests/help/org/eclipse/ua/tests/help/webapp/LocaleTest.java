/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webapp;

import java.util.Locale;

import javax.servlet.http.Cookie;

import junit.framework.TestCase;

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.util.ProductPreferences;
import org.eclipse.help.internal.webapp.data.UrlUtil;

import org.eclipse.core.runtime.Platform;

/**
 * Tests for locale related code in UrlUtil
 */
public class LocaleTest extends TestCase {
	
	private int mode;

	protected void tearDown() throws Exception {
		BaseHelpSystem.setMode(mode);
	}
	
	protected void setUp() throws Exception {
		mode = BaseHelpSystem.getMode();
	}

	public void testFixLocaleNull() {
		assertNull(UrlUtil.cleanLocale(null));
	}
	
	public void testFixLocaleWithIllegalChars() {
		assertEquals("ab-cd______ef", UrlUtil.cleanLocale("ab-cd\n\r_\"\'_ef"));
	}

	@SuppressWarnings("unchecked")
	public void testForced_Locale() {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		MockServletRequest req = new MockServletRequest();
		req.setLocale(new Locale("de"));
		req.getParameterMap().put("lang", new String[] { "es" });
		String locale = UrlUtil.getLocale(req, null);
		assertEquals("es", locale);
	}

	@SuppressWarnings("unchecked")
	public void testForcedLangOverridesCookies() {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		MockServletRequest req = new MockServletRequest();
		req.setLocale(new Locale("de"));
		req.setCookies(new Cookie[] {new Cookie("lang", "it")});
		req.getParameterMap().put("lang", new String[] { "es" });
		String locale = UrlUtil.getLocale(req, null);
		assertEquals("es", locale);
	}
	
	public void testForcedUsingCookies() {
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		MockServletRequest req = new MockServletRequest();
		req.setLocale(new Locale("de"));
		req.setCookies(new Cookie[] {new Cookie("lang", "it")});
		String locale = UrlUtil.getLocale(req, null);
		assertEquals("it", locale);
	}

	public void testGetLocale_De_Standalone() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_STANDALONE);
		req.setLocale(new Locale("de"));
		String locale = UrlUtil.getLocale(req, null);
		assertEquals(Platform.getNL(), locale);
	}
	
	public void testGetLocale_De_Workbench() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_WORKBENCH);
		req.setLocale(new Locale("de"));
		String locale = UrlUtil.getLocale(req, null);
		assertEquals(Platform.getNL(), locale);
	}
	
	public void testGetLocale_De_Infocenter() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		req.setLocale(new Locale("de"));
		String locale = UrlUtil.getLocale(req, null);
		assertEquals("de", locale);
	}

	public void testGetLocale_Pt_Br_Infocenter() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		req.setLocale(new Locale("pt", "br"));
		String locale = UrlUtil.getLocale(req, null);
		assertEquals("pt_br", locale.toLowerCase());
	}
	
	public void testGetLocale_Fr_Ca_To_Infocenter() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		req.setLocale(new Locale("fr", "CA", "to"));
		String locale = UrlUtil.getLocale(req, null);
		assertEquals("fr_CA_to", locale);
	}
	
	public void testIsRTLWorkbench() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_WORKBENCH);
		req.setLocale(new Locale("de"));
		assertEquals(ProductPreferences.isRTL(), UrlUtil.isRTL(req, null));
	}	

	public void testIsRTLInfocenter_ar() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		req.setLocale(new Locale("ar_SA"));
		assertTrue(UrlUtil.isRTL(req, null));
	}

	public void testIsRTLInfocenter_he() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		req.setLocale(new Locale("he"));
		assertTrue(UrlUtil.isRTL(req, null));
	}

	public void testIsRTLInfocenter_de() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		req.setLocale(new Locale("de"));
		assertFalse(UrlUtil.isRTL(req, null));
	}
	
	public void testIsRTLInfocenter_en_us() {
		MockServletRequest req = new MockServletRequest();
		BaseHelpSystem.setMode(BaseHelpSystem.MODE_INFOCENTER);
		req.setLocale(new Locale("en_US"));
		assertFalse(UrlUtil.isRTL(req, null));
	}
	
}
