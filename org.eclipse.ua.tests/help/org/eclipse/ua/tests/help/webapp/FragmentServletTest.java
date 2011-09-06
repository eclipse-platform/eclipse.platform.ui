/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
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

import org.eclipse.help.internal.webapp.servlet.TocFragmentServlet;

/**
 * Tests for the class TocFragmentServlet
 */
public class FragmentServletTest extends TestCase {
	
	private static final String PATH = "1_2";
	private static final String DOC_HTML = "org.eclipse.ua.tests/doc.html";

	public void testFixHrefNormal() {
		String href = TocFragmentServlet.fixupHref(DOC_HTML, PATH);
		assertEquals(DOC_HTML + "?cp=1_2", href);
	}
	
	public void testFixHrefWithParameter() {
		String href = TocFragmentServlet.fixupHref(DOC_HTML + "?a=b", PATH);
		assertEquals(DOC_HTML + "?a=b&cp=1_2", href);
	}

	public void testFixHrefNull() {
		String href = TocFragmentServlet.fixupHref(null, PATH);
		assertEquals("/../nav/1_2",  href);
	}

	public void testFixHrefWithAnchor() {
		String href = TocFragmentServlet.fixupHref(DOC_HTML + "#A", PATH);
		assertEquals(DOC_HTML + "?cp=1_2#A" , href);
	}
	
	public void testFixHrefWithAnchorAndParams() {
		String href = TocFragmentServlet.fixupHref(DOC_HTML + "?a=b#A", PATH);
		assertEquals(DOC_HTML + "?a=b&cp=1_2#A" , href);
	}
	
}
