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
import static org.junit.Assert.assertTrue;

import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;


public class StyleRuleTest {

	@Test
	public void testSimpleStyleRule() throws Exception {
		String css = "Label { color: #FF0000 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		assertNotNull(styleSheet);
		CSSRuleList rules = styleSheet.getCssRules();
		assertEquals(1, rules.getLength());
		CSSRule rule = rules.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
	}

	@Test
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

	@Test
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

	@Test
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

	@Test
	public void testTestFontItalic() throws Exception {
		String css = "Label { font: Arial 12px; font-style: italic }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		CSSRuleList rules = styleSheet.getCssRules();
		CSSRule rule = rules.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleDeclaration style = ((CSSStyleRule) rule).getStyle();
		CSSValue value = style.getPropertyCSSValue("font-style");
		assertTrue(value instanceof CSSPrimitiveValue);
		String colorString = ((CSSPrimitiveValue) value).getStringValue();
		assertEquals("italic", colorString);
	}

	@Test
	public void testTestFontBold() throws Exception{
		String css = "Label { font: Arial 12px; font-style: bold }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		CSSRuleList rules = styleSheet.getCssRules();
		CSSRule rule = rules.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleDeclaration style = ((CSSStyleRule) rule).getStyle();
		CSSValue value = style.getPropertyCSSValue("font-style");
		assertTrue(value instanceof CSSPrimitiveValue);
		String colorString = ((CSSPrimitiveValue) value).getStringValue();
		assertEquals("bold", colorString);
	}

	@Test
	public void testBackgroundNameColor() throws Exception{
		String css = "Label { background-color: green }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		CSSRuleList rules = styleSheet.getCssRules();
		CSSRule rule = rules.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleDeclaration style = ((CSSStyleRule) rule).getStyle();
		CSSValue value = style.getPropertyCSSValue("background-color");
		assertTrue(value instanceof CSSPrimitiveValue);
		String colorString = ((CSSPrimitiveValue) value).getStringValue();
		assertEquals("green", colorString);
	}

	@Test
	public void testBackgroundHexColor() throws Exception {
		String css = "Label { background-color: #FF0220 }";
		CSSStyleSheet styleSheet = ParserTestUtil.parseCss(css);
		CSSRuleList rules = styleSheet.getCssRules();
		CSSRule rule = rules.item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());
		CSSStyleDeclaration style = ((CSSStyleRule) rule).getStyle();
		CSSValue value = style.getPropertyCSSValue("background-color");
		assertTrue(value instanceof CSSPrimitiveValue);
		RGBColor colorValue = ((CSSPrimitiveValue) value).getRGBColorValue();
		assertEquals(255.0f, colorValue.getRed().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER), 0f);
		assertEquals(2.0f, colorValue.getGreen().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER), 0f);
		assertEquals(32.0f, colorValue.getBlue().getFloatValue(
				CSSPrimitiveValue.CSS_NUMBER), 0f);
	}
}
