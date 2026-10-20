/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Lars Vogel <Lars.Vogel@gmail.com> - Bug 430468
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleSheetImpl;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.jupiter.api.Test;

/**
 * Assert that <code>@font-face</code> rules are ignored.
 */
public class FontFaceRulesTest {

	@Test
	void testEmptyFontFaceRule() throws Exception {
		String css = "@font-face {}\n"
				+ "Label { background-color: #FF0000 }";
		CSSStyleSheetImpl styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		assertEquals(1, styleSheet.getRules().size());
	}

	@Test
	void testFontFaceRuleWithProperties() throws Exception {
		String css = """
			@font-face {
			  font-family: "Robson Celtic";
			  src: url("http://site/fonts/rob-celt")
			}
			Label { background-color: #FF0000 }""";
		CSSStyleSheetImpl styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		assertEquals(1, styleSheet.getRules().size());
	}
}
