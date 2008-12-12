package org.eclipse.e4.ui.tests.css.swt.parser;

import junit.framework.TestCase;

import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;

public class StyleRuleTest extends TestCase {

	public void testSimpleStyleRule() throws Exception {
		String css = "Label { background-color: #FF0000 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		CSSRuleList rules = styleSheet.getCssRules();
		CSSRule rule = rules.item(0);
		assertEquals(1, rules.getLength());
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
	}
}
