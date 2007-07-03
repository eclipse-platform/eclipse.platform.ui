/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.other;

import org.eclipse.help.internal.base.util.LinkUtil;
import org.eclipse.ui.internal.cheatsheets.views.ViewUtilities;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LinkUtilTest extends TestCase {

	public static Test suite() {
		return new TestSuite(LinkUtilTest.class);
	}

	public void testStripParamNull() {
		assertEquals(null, LinkUtil.stripParams(null));
	}
	
	public void testStripParamEmpty() {
		assertEquals("", ViewUtilities.escapeForLabel(""));
	}

	public void testStripParamWithoutParam() {
		assertEquals("http://www.eclipse.org", LinkUtil.stripParams("http://www.eclipse.org"));
	}

	public void testStripParamWithParam() {
		assertEquals("http://www.mysite.com", LinkUtil.stripParams("http://www.mysite.com?param1=foo&param2=bar"));
	}

	public void testStripParamWithAnchor() {
		assertEquals("http://www.mysite.com#anchor1", LinkUtil.stripParams("http://www.mysite.com#anchor1"));
	}

	public void testStripParamWithAnchorBeforeParam() {
		assertEquals("http://www.mysite.com#anchor1", LinkUtil.stripParams("http://www.mysite.com#anchor1?param=foobar"));
	}

	public void testStripParamWithAnchorAfterParam() {
		assertEquals("http://www.mysite.com#anchor1", LinkUtil.stripParams("http://www.mysite.com?param=foobar#anchor1"));
	}
}
