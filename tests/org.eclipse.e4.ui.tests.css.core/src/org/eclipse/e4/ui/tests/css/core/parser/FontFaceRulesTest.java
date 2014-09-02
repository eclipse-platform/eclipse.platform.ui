/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.Test;
import org.w3c.dom.css.CSSStyleSheet;

/**
 * Assert that <code>@font-face</code> rules are ignored.
 */
public class FontFaceRulesTest {

	@Test
	public void testEmptyFontFaceRule() throws Exception {
		String css = "@font-face {}\n"
				+ "Label { background-color: #FF0000 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		assertEquals(1, styleSheet.getCssRules().getLength());
	}

	@Test
	public void testFontFaceRuleWithProperties() throws Exception {
		String css = "@font-face {\n"
				+ "  font-family: \"Robson Celtic\";\n"
				+ "  src: url(\"http://site/fonts/rob-celt\")\n" + "}\n"
				+ "Label { background-color: #FF0000 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		assertEquals(1, styleSheet.getCssRules().getLength());
	}
}
