package org.eclipse.e4.ui.tests.css.swt.parser;

import junit.framework.TestCase;

import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

/**
 * Assert that <code>@media</code> rules are ignored.
 */
public class MediaRulesTest extends TestCase {

	public void testMediaRule() throws Exception {
		String css = "@media screen, print {\n"
			+ "BODY { line-height: 1.2 }\n"
			+ "}\n" + "Label { background-color: #FF0000 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		CSSRuleList rules = styleSheet.getCssRules();
		assertEquals(1, rules.getLength());
	}
}
