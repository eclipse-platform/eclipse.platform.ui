package org.eclipse.e4.ui.tests.css.core.parser;

import junit.framework.TestCase;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;


public class StyleRuleTest extends TestCase {

	public void testSimpleStyleRule() throws Exception {
		String css = "Label { color: #FF0000 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		CSSRuleList rules = styleSheet.getCssRules();
		assertEquals(1, rules.getLength());
		CSSRule rule = rules.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
	}

	public void testHexColor() throws Exception {
		String css = "Label { color: #FF0220 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		CSSRuleList rules = styleSheet.getCssRules();
		CSSRule rule = rules.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleDeclaration style = ((CSSStyleRule) rule).getStyle();
		CSSValue value = style.getPropertyCSSValue("color");
		assertTrue(value instanceof CSSPrimitiveValue);
		RGBColor colorValue = ((CSSPrimitiveValue) value).getRGBColorValue();
		assertEquals(255.0f, colorValue.getRed().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER), 0f);
		assertEquals(2.0f, colorValue.getGreen().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER), 0f);
		assertEquals(32.0f, colorValue.getBlue().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER), 0f);
	}

	public void testNamedColor() throws Exception {
		String css = "Label { color: green }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		CSSRuleList rules = styleSheet.getCssRules();
		CSSRule rule = rules.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleDeclaration style = ((CSSStyleRule) rule).getStyle();
		CSSValue value = style.getPropertyCSSValue("color");
		assertTrue(value instanceof CSSPrimitiveValue);
		String colorString = ((CSSPrimitiveValue) value).getStringValue();
		assertEquals("green", colorString);
	}

	public void testFont() throws Exception {
		String css = "Label { font: Verdana }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		CSSRuleList rules = styleSheet.getCssRules();
		CSSRule rule = rules.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleDeclaration style = ((CSSStyleRule) rule).getStyle();
		CSSValue value = style.getPropertyCSSValue("font");
		assertTrue(value instanceof CSSPrimitiveValue);
		String colorString = ((CSSPrimitiveValue) value).getStringValue();
		assertEquals("Verdana", colorString);
	}
}
