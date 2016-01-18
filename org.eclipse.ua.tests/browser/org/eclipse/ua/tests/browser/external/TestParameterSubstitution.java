/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.browser.external;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.ui.internal.browser.WebBrowserUtil;
import org.junit.Test;

public class TestParameterSubstitution {

	private static final String URL = "http://127.0.0.1:3873/help/index.jsp";

	@Test
	public void testNullParameters() {
		assertEquals(URL, WebBrowserUtil.createParameterString(null, URL));
	}

	@Test
	public void testEmptyParameters() {
		assertEquals(URL, WebBrowserUtil.createParameterString("", URL));
	}

	@Test
	public void testNullURL() {
		assertEquals("", WebBrowserUtil.createParameterString("", null));
	}

	@Test
	public void testNoSubstitution() {
		assertEquals("-console " + URL, WebBrowserUtil.createParameterString("-console", URL));
	}

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
		assertArrayEquals(new String[] { "-url", URL, "-console"}, WebBrowserUtil.createParameterArray("-url %URL% -console", URL));
	}

}
