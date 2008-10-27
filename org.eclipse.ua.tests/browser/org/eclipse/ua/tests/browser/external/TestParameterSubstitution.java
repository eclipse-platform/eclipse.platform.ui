/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.browser.external;

import org.eclipse.ui.internal.browser.WebBrowserUtil;

import junit.framework.TestCase;

public class TestParameterSubstitution extends TestCase {
	
	private static final String URL = "http://127.0.0.1:3873/help/index.jsp";

	public void testNullParameters() {
		assertEquals(URL, WebBrowserUtil.createParameterString(null, URL));
	}

	public void testEmptyParameters() {
		assertEquals(URL, WebBrowserUtil.createParameterString("", URL));
	}
	
	public void testNullURL() {
		assertEquals("", WebBrowserUtil.createParameterString("", null));
	}

	public void testNoSubstitution() {
		assertEquals("-console " + URL, WebBrowserUtil.createParameterString("-console", URL));
	}
	
	public void testSubstitution() {
		assertEquals("-url " + URL + " -console", WebBrowserUtil.createParameterString("-url %URL% -console", URL));
	}

}
