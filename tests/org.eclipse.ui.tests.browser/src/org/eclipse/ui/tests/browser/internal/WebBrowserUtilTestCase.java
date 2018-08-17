/*******************************************************************************
 * Copyright (c) 2015, 2018 Tasktop Technologies and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.browser.internal;

import static org.eclipse.ui.internal.browser.IBrowserDescriptor.URL_PARAMETER;
import static org.eclipse.ui.internal.browser.WebBrowserUtil.createParameterArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.ui.internal.browser.WebBrowserUtil;
import org.junit.Test;

@SuppressWarnings("restriction")
public class WebBrowserUtilTestCase {

	private static final String URL = "http://127.0.0.1:3873/help/index.jsp";

	@SuppressWarnings("deprecation")
	@Test
	public void testNullParameters() {
		assertEquals(URL, WebBrowserUtil.createParameterString(null, URL));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testEmptyParameters() {
		assertEquals(URL, WebBrowserUtil.createParameterString("", URL));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testNullURL() {
		assertEquals("", WebBrowserUtil.createParameterString("", null));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testNoSubstitution() {
		assertEquals("-console " + URL, WebBrowserUtil.createParameterString("-console", URL));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSubstitution() {
		assertEquals("-url " + URL + " -console", WebBrowserUtil.createParameterString("-url %URL% -console", URL));
	}

	@Test
	public void testArrayNullParameters() {
		assertArrayEquals(new String[] { URL }, WebBrowserUtil.createParameterArray(null, URL));
	}

	@Test
	public void testArrayEmptyParameters() {
		assertArrayEquals(new String[] { URL }, WebBrowserUtil.createParameterArray("", URL));
	}

	@Test
	public void testArrayNullURL() {
		assertArrayEquals(new String[0], WebBrowserUtil.createParameterArray("", null));
	}

	@Test
	public void testArrayNoSubstitution() {
		assertArrayEquals(new String[] { "-console", URL }, WebBrowserUtil.createParameterArray("-console", URL));
	}

	@Test
	public void testArraySubstitution() {
		assertArrayEquals(new String[] { "-url", URL, "-console" },
				WebBrowserUtil.createParameterArray("-url %URL% -console", URL));
	}

	@Test
	public void testCreateParameterArray() {
		assertArrayEquals(new String[0], createParameterArray(null, null));
		assertArrayEquals(new String[] { "parameters" }, createParameterArray("parameters", null));
		assertArrayEquals(new String[] { "url" }, createParameterArray(null, "url"));
		assertArrayEquals(new String[] { "parameters", "url" }, createParameterArray("parameters ", "url"));
		assertArrayEquals(new String[] { "parameters", "url" }, createParameterArray("parameters", "url"));
		assertArrayEquals(new String[] { "param1", "param2" },
				createParameterArray("param1 " + URL_PARAMETER + " param2", null));
		assertArrayEquals(new String[] { "param1", "url", "param2" },
				createParameterArray("param1 " + URL_PARAMETER + " param2", "url"));
		assertArrayEquals(new String[] { "param1", "url", "param2", "url" },
				createParameterArray("param1 " + URL_PARAMETER + " param2 " + URL_PARAMETER, "url"));
	}

	@Test
	public void testCreateParameterArrayForMozilla() {
		assertArrayEquals(new String[] { "-remote", "openURL(url)" },
				createParameterArray(" -remote openURL(" + URL_PARAMETER + ")", "url"));
		assertArrayEquals(new String[] { "parameters", "-remote", "openURL(url)" },
				createParameterArray("parameters -remote openURL(" + URL_PARAMETER + ")", "url"));
	}
}
