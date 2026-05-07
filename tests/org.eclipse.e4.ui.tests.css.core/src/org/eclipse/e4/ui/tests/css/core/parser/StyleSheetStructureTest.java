/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

/**
 * Phase 2 of the test plan in css-testing.md. Locks in the structural shape
 * the parser produces, using only the high-level DOM-CSS interfaces. The new
 * parser introduced during the CSS engine rework must produce an AST these
 * tests still accept.
 */
public class StyleSheetStructureTest {

	@Test
	void testEmptyStyleSheet() throws Exception {
		CSSStyleSheet sheet = ParserTestUtil.parseCss("");

		assertNotNull(sheet);
		assertEquals(0, sheet.getCssRules().getLength());
	}

	@Test
	void testSingleRule() throws Exception {
		CSSStyleSheet sheet = ParserTestUtil.parseCss("Button { color: red; }");

		assertNotNull(sheet);
		assertEquals(1, sheet.getCssRules().getLength());

		CSSRule rule = sheet.getCssRules().item(0);
		assertEquals(CSSRule.STYLE_RULE, rule.getType());

		CSSStyleRule styleRule = (CSSStyleRule) rule;
		assertEquals("Button", styleRule.getSelectorText());

		CSSStyleDeclaration style = styleRule.getStyle();
		assertEquals(1, style.getLength());
		assertEquals("color", style.item(0));

		CSSValue colorValue = style.getPropertyCSSValue("color");
		assertNotNull(colorValue);
		assertEquals("red", colorValue.getCssText());
	}

	@Test
	void testMultipleRulesPreserveOrder() throws Exception {
		String css = """
				Button { color: red; }
				Label { color: green; }
				Composite { color: blue; }
				""";
		CSSStyleSheet sheet = ParserTestUtil.parseCss(css);

		CSSRuleList rules = sheet.getCssRules();
		assertEquals(3, rules.getLength());
		assertEquals("Button", ((CSSStyleRule) rules.item(0)).getSelectorText());
		assertEquals("Label", ((CSSStyleRule) rules.item(1)).getSelectorText());
		assertEquals("Composite", ((CSSStyleRule) rules.item(2)).getSelectorText());
	}

	@Test
	void testMultipleSelectorsInOneRule() throws Exception {
		CSSStyleSheet sheet = ParserTestUtil.parseCss("Button, Label { color: red; }");

		assertEquals(1, sheet.getCssRules().getLength());
		CSSStyleRule rule = (CSSStyleRule) sheet.getCssRules().item(0);

		String selectorText = rule.getSelectorText();
		assertTrue(selectorText.contains("Button"), () -> "expected Button in " + selectorText);
		assertTrue(selectorText.contains("Label"), () -> "expected Label in " + selectorText);
	}

	@Test
	void testMultipleDeclarations() throws Exception {
		CSSStyleSheet sheet = ParserTestUtil
				.parseCss("Button { color: red; background-color: blue; font-style: italic; }");

		CSSStyleRule rule = (CSSStyleRule) sheet.getCssRules().item(0);
		CSSStyleDeclaration style = rule.getStyle();

		assertEquals(3, style.getLength());
		assertEquals("color", style.item(0));
		assertEquals("background-color", style.item(1));
		assertEquals("font-style", style.item(2));

		assertEquals("red", style.getPropertyCSSValue("color").getCssText());
		assertEquals("blue", style.getPropertyCSSValue("background-color").getCssText());
		assertEquals("italic", style.getPropertyCSSValue("font-style").getCssText());
	}

	@Test
	void testWhitespaceTolerance() throws Exception {
		CSSStyleSheet compact = ParserTestUtil.parseCss("Button{color:red;background-color:blue;}");
		CSSStyleSheet padded = ParserTestUtil.parseCss("""

				Button   {
				    color:           red;
				    background-color:\tblue;
				}

				""");

		assertEquals(compact.getCssRules().getLength(), padded.getCssRules().getLength());

		CSSStyleRule compactRule = (CSSStyleRule) compact.getCssRules().item(0);
		CSSStyleRule paddedRule = (CSSStyleRule) padded.getCssRules().item(0);

		assertEquals(compactRule.getSelectorText(), paddedRule.getSelectorText());
		assertEquals(compactRule.getStyle().getLength(), paddedRule.getStyle().getLength());
		assertEquals(compactRule.getStyle().getPropertyCSSValue("color").getCssText(),
				paddedRule.getStyle().getPropertyCSSValue("color").getCssText());
		assertEquals(compactRule.getStyle().getPropertyCSSValue("background-color").getCssText(),
				paddedRule.getStyle().getPropertyCSSValue("background-color").getCssText());
	}

	@Test
	void testCommentsIgnored() throws Exception {
		CSSStyleSheet sheet = ParserTestUtil.parseCss("""
				/* leading comment */
				Button {
					/* inside */
					color: red; /* trailing */
				}
				/* between rules */
				Label { color: blue; }
				""");

		assertEquals(2, sheet.getCssRules().getLength());
		CSSStyleRule first = (CSSStyleRule) sheet.getCssRules().item(0);
		assertEquals("Button", first.getSelectorText());
		assertEquals("red", first.getStyle().getPropertyCSSValue("color").getCssText());
	}

	@Test
	void testImportRuleType() throws Exception {
		CSSStyleSheet sheet = ParserTestUtil.parseCssWithoutImports("@import url('other.css');");

		assertEquals(1, sheet.getCssRules().getLength());
		CSSRule rule = sheet.getCssRules().item(0);
		assertEquals(CSSRule.IMPORT_RULE, rule.getType());
		assertInstanceOf(CSSImportRule.class, rule);
	}

	@Test
	void testImportHrefExposed() throws Exception {
		CSSStyleSheet sheet = ParserTestUtil
				.parseCssWithoutImports("@import url('platform:/plugin/x/style.css');");

		CSSImportRule rule = (CSSImportRule) sheet.getCssRules().item(0);
		assertEquals("platform:/plugin/x/style.css", rule.getHref());
	}

	@Test
	void testStringValues() throws Exception {
		CSSStyleSheet quoted = ParserTestUtil.parseCss("Button[style~='SWT.CHECK'] { color: red; }");
		CSSStyleSheet unquoted = ParserTestUtil.parseCss("Button[style~=SWT_CHECK] { color: red; }");

		assertEquals(1, quoted.getCssRules().getLength());
		assertEquals(1, unquoted.getCssRules().getLength());

		assertTrue(((CSSStyleRule) quoted.getCssRules().item(0)).getSelectorText().contains("SWT.CHECK"));
		assertTrue(((CSSStyleRule) unquoted.getCssRules().item(0)).getSelectorText().contains("SWT_CHECK"));
	}

	@Test
	void testColorValueForms() throws Exception {
		CSSStyleSheet sheet = ParserTestUtil.parseCss("""
				A { color: #fff; }
				B { color: #ffffff; }
				C { color: rgb(255, 255, 255); }
				D { color: white; }
				""");

		assertEquals(4, sheet.getCssRules().getLength());
		for (int i = 0; i < 4; i++) {
			CSSStyleRule rule = (CSSStyleRule) sheet.getCssRules().item(i);
			CSSValue value = rule.getStyle().getPropertyCSSValue("color");
			assertNotNull(value, () -> "rule " + rule.getSelectorText() + " has no color value");
		}

		CSSValue hex3 = ((CSSStyleRule) sheet.getCssRules().item(0)).getStyle().getPropertyCSSValue("color");
		CSSValue hex6 = ((CSSStyleRule) sheet.getCssRules().item(1)).getStyle().getPropertyCSSValue("color");
		CSSValue rgb = ((CSSStyleRule) sheet.getCssRules().item(2)).getStyle().getPropertyCSSValue("color");

		// All three numeric forms are CSS_RGBCOLOR primitive values resolving to white.
		assertWhiteRgb((CSSPrimitiveValue) hex3);
		assertWhiteRgb((CSSPrimitiveValue) hex6);
		assertWhiteRgb((CSSPrimitiveValue) rgb);

		CSSValue named = ((CSSStyleRule) sheet.getCssRules().item(3)).getStyle().getPropertyCSSValue("color");
		assertEquals("white", named.getCssText());
	}

	@Test
	void testInvalidInputErrorReported() throws Exception {
		List<Exception> reported = new ArrayList<>();
		CSSErrorHandler recorder = reported::add;

		CSSEngine engine = new CSSSWTEngineImpl(Display.getDefault());
		engine.setErrorHandler(recorder);

		boolean threw = false;
		try {
			engine.parseStyleSheet(new StringReader("@@@ not valid css {"));
		} catch (Exception e) {
			threw = true;
		}

		// The contract is "garbage does not silently pass": either the parser
		// throws, or the error handler is invoked. Both is also fine. A clean
		// return with no error reported is the regression we want to catch.
		assertTrue(threw || !reported.isEmpty(),
				"expected the parser to throw or invoke the error handler for invalid input");
	}

	private static void assertWhiteRgb(CSSPrimitiveValue value) {
		assertEquals(CSSPrimitiveValue.CSS_RGBCOLOR, value.getPrimitiveType());
		RGBColor rgb = value.getRGBColorValue();
		assertEquals(255.0f, rgb.getRed().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
		assertEquals(255.0f, rgb.getGreen().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
		assertEquals(255.0f, rgb.getBlue().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
	}

}
