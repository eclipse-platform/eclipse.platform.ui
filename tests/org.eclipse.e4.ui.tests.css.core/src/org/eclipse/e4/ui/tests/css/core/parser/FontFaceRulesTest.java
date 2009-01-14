package org.eclipse.e4.ui.tests.css.core.parser;

import junit.framework.TestCase;

import org.w3c.dom.css.CSSStyleSheet;

/**
 * Assert that <code>@font-face</code> rules are ignored.
 */
public class FontFaceRulesTest extends TestCase {

	public void testEmptyFontFaceRule() throws Exception {
		String css = "@font-face {}\n"
			+ "Label { background-color: #FF0000 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		assertEquals(1, styleSheet.getCssRules().getLength());
	}

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
