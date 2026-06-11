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
import org.eclipse.e4.ui.css.core.impl.dom.CSSImportRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleSheetImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CssRule;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
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
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("");

		assertNotNull(sheet);
		assertEquals(0, sheet.getRules().size());
	}

	@Test
	void testSingleRule() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("Button { color: red; }");

		assertNotNull(sheet);
		assertEquals(1, sheet.getRules().size());

		CssRule rule = sheet.getRules().get(0);
		assertInstanceOf(CSSStyleRuleImpl.class, rule);

		CSSStyleRuleImpl styleRule = (CSSStyleRuleImpl) rule;
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
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss(css);

		List<CssRule> rules = sheet.getRules();
		assertEquals(3, rules.size());
		assertEquals("Button", ((CSSStyleRuleImpl) rules.get(0)).getSelectorText());
		assertEquals("Label", ((CSSStyleRuleImpl) rules.get(1)).getSelectorText());
		assertEquals("Composite", ((CSSStyleRuleImpl) rules.get(2)).getSelectorText());
	}

	@Test
	void testMultipleSelectorsInOneRule() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("Button, Label { color: red; }");

		assertEquals(1, sheet.getRules().size());
		CSSStyleRuleImpl rule = (CSSStyleRuleImpl) sheet.getRules().get(0);

		String selectorText = rule.getSelectorText();
		assertTrue(selectorText.contains("Button"), () -> "expected Button in " + selectorText);
		assertTrue(selectorText.contains("Label"), () -> "expected Label in " + selectorText);
	}

	@Test
	void testMultipleDeclarations() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil
				.parseCss("Button { color: red; background-color: blue; font-style: italic; }");

		CSSStyleRuleImpl rule = (CSSStyleRuleImpl) sheet.getRules().get(0);
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
		CSSStyleSheetImpl compact = ParserTestUtil.parseCss("Button{color:red;background-color:blue;}");
		CSSStyleSheetImpl padded = ParserTestUtil.parseCss("""

				Button   {
				    color:           red;
				    background-color:\tblue;
				}

				""");

		assertEquals(compact.getRules().size(), padded.getRules().size());

		CSSStyleRuleImpl compactRule = (CSSStyleRuleImpl) compact.getRules().get(0);
		CSSStyleRuleImpl paddedRule = (CSSStyleRuleImpl) padded.getRules().get(0);

		assertEquals(compactRule.getSelectorText(), paddedRule.getSelectorText());
		assertEquals(compactRule.getStyle().getLength(), paddedRule.getStyle().getLength());
		assertEquals(compactRule.getStyle().getPropertyCSSValue("color").getCssText(),
				paddedRule.getStyle().getPropertyCSSValue("color").getCssText());
		assertEquals(compactRule.getStyle().getPropertyCSSValue("background-color").getCssText(),
				paddedRule.getStyle().getPropertyCSSValue("background-color").getCssText());
	}

	@Test
	void testCommentsIgnored() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("""
				/* leading comment */
				Button {
					/* inside */
					color: red; /* trailing */
				}
				/* between rules */
				Label { color: blue; }
				""");

		assertEquals(2, sheet.getRules().size());
		CSSStyleRuleImpl first = (CSSStyleRuleImpl) sheet.getRules().get(0);
		assertEquals("Button", first.getSelectorText());
		assertEquals("red", first.getStyle().getPropertyCSSValue("color").getCssText());
	}

	@Test
	void testImportRuleType() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCssWithoutImports("@import url('other.css');");

		assertEquals(1, sheet.getRules().size());
		CssRule rule = sheet.getRules().get(0);
		assertInstanceOf(CSSImportRuleImpl.class, rule);
	}

	@Test
	void testImportHrefExposed() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil
				.parseCssWithoutImports("@import url('platform:/plugin/x/style.css');");

		CSSImportRuleImpl rule = (CSSImportRuleImpl) sheet.getRules().get(0);
		assertEquals("platform:/plugin/x/style.css", rule.getHref());
	}

	@Test
	void testStringValues() throws Exception {
		CSSStyleSheetImpl quoted = ParserTestUtil.parseCss("Button[style~='SWT.CHECK'] { color: red; }");
		CSSStyleSheetImpl unquoted = ParserTestUtil.parseCss("Button[style~=SWT_CHECK] { color: red; }");

		assertEquals(1, quoted.getRules().size());
		assertEquals(1, unquoted.getRules().size());

		assertTrue(((CSSStyleRuleImpl) quoted.getRules().get(0)).getSelectorText().contains("SWT.CHECK"));
		assertTrue(((CSSStyleRuleImpl) unquoted.getRules().get(0)).getSelectorText().contains("SWT_CHECK"));
	}

	@Test
	void testColorValueForms() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("""
				A { color: #fff; }
				B { color: #ffffff; }
				C { color: rgb(255, 255, 255); }
				D { color: white; }
				""");

		assertEquals(4, sheet.getRules().size());
		for (int i = 0; i < 4; i++) {
			CSSStyleRuleImpl rule = (CSSStyleRuleImpl) sheet.getRules().get(i);
			CSSValue value = rule.getStyle().getPropertyCSSValue("color");
			assertNotNull(value, () -> "rule " + rule.getSelectorText() + " has no color value");
		}

		CSSValue hex3 = ((CSSStyleRuleImpl) sheet.getRules().get(0)).getStyle().getPropertyCSSValue("color");
		CSSValue hex6 = ((CSSStyleRuleImpl) sheet.getRules().get(1)).getStyle().getPropertyCSSValue("color");
		CSSValue rgb = ((CSSStyleRuleImpl) sheet.getRules().get(2)).getStyle().getPropertyCSSValue("color");

		// All three numeric forms are CSS_RGBCOLOR primitive values resolving to white.
		assertWhiteRgb((CSSPrimitiveValue) hex3);
		assertWhiteRgb((CSSPrimitiveValue) hex6);
		assertWhiteRgb((CSSPrimitiveValue) rgb);

		CSSValue named = ((CSSStyleRuleImpl) sheet.getRules().get(3)).getStyle().getPropertyCSSValue("color");
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

	@Test
	void testImportantPriorityPreserved() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("Button { color: red !important; }");

		CSSStyleDeclaration style = ((CSSStyleRuleImpl) sheet.getRules().get(0)).getStyle();
		assertEquals("red", style.getPropertyCSSValue("color").getCssText());
		assertEquals("important", style.getPropertyPriority("color"));
	}

	@Test
	void testImportantOnlyMarksItsOwnDeclaration() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil
				.parseCss("Button { color: red !important; background-color: blue; }");

		CSSStyleDeclaration style = ((CSSStyleRuleImpl) sheet.getRules().get(0)).getStyle();
		assertEquals("important", style.getPropertyPriority("color"));
		assertEquals("", style.getPropertyPriority("background-color"));
	}

	@Test
	void testTrailingSemicolonOptional() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("Button { color: red }");

		CSSStyleDeclaration style = ((CSSStyleRuleImpl) sheet.getRules().get(0)).getStyle();
		assertEquals(1, style.getLength());
		assertEquals("red", style.getPropertyCSSValue("color").getCssText());
		assertEquals("", style.getPropertyPriority("color"));
	}

	@Test
	void testStraySemicolonsTolerated() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("Button { ; color: red;; }");

		CSSStyleDeclaration style = ((CSSStyleRuleImpl) sheet.getRules().get(0)).getStyle();
		assertEquals(1, style.getLength());
		assertEquals("red", style.getPropertyCSSValue("color").getCssText());
	}

	@Test
	void testMultiValuePropertyIsValueList() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("Button { margin: 1px 2px 3px 4px; }");

		CSSValue value = ((CSSStyleRuleImpl) sheet.getRules().get(0)).getStyle().getPropertyCSSValue("margin");
		assertEquals(CSSValue.CSS_VALUE_LIST, value.getCssValueType());

		CSSValueList list = (CSSValueList) value;
		assertEquals(4, list.getLength());
		for (int i = 0; i < list.getLength(); i++) {
			CSSPrimitiveValue item = (CSSPrimitiveValue) list.item(i);
			assertEquals(CSSPrimitiveValue.CSS_PX, item.getPrimitiveType());
			assertEquals(i + 1.0f, item.getFloatValue(CSSPrimitiveValue.CSS_PX));
		}
	}

	@Test
	void testLengthAndPercentageUnits() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("""
				A { width: 10px; }
				B { width: 50%; }
				""");

		CSSPrimitiveValue px = (CSSPrimitiveValue) ((CSSStyleRuleImpl) sheet.getRules().get(0)).getStyle()
				.getPropertyCSSValue("width");
		assertEquals(CSSPrimitiveValue.CSS_PX, px.getPrimitiveType());
		assertEquals(10.0f, px.getFloatValue(CSSPrimitiveValue.CSS_PX));

		CSSPrimitiveValue percent = (CSSPrimitiveValue) ((CSSStyleRuleImpl) sheet.getRules().get(1)).getStyle()
				.getPropertyCSSValue("width");
		assertEquals(CSSPrimitiveValue.CSS_PERCENTAGE, percent.getPrimitiveType());
		assertEquals(50.0f, percent.getFloatValue(CSSPrimitiveValue.CSS_PERCENTAGE));
	}

	@Test
	void testUnquotedImportUrlHref() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCssWithoutImports("@import url(other.css);");

		CSSImportRuleImpl rule = (CSSImportRuleImpl) sheet.getRules().get(0);
		assertEquals("other.css", rule.getHref());
	}

	@Test
	void testFontFaceRuleDiscarded() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("""
				@font-face { font-family: x; }
				Label { color: blue; }
				""");

		// The engine parses but does not retain @font-face; the regular rule that
		// follows it must still be present and intact.
		assertEquals(1, sheet.getRules().size());
		CSSStyleRuleImpl label = (CSSStyleRuleImpl) sheet.getRules().get(0);
		assertEquals("Label", label.getSelectorText());
		assertEquals("blue", label.getStyle().getPropertyCSSValue("color").getCssText());
	}

	@Test
	void testMediaRuleToleratedAndFollowingRuleParsed() throws Exception {
		CSSStyleSheetImpl sheet = ParserTestUtil.parseCss("""
				@media screen { Button { color: red; } }
				Label { color: blue; }
				""");

		// @media is accepted without applying its block; the top-level rule after
		// it must remain reachable with its declarations intact.
		boolean labelFound = false;
		List<CssRule> rules = sheet.getRules();
		for (int i = 0; i < rules.size(); i++) {
			if (rules.get(i) instanceof CSSStyleRuleImpl rule && "Label".equals(rule.getSelectorText())) {
				assertEquals("blue", rule.getStyle().getPropertyCSSValue("color").getCssText());
				labelFound = true;
			}
		}
		assertTrue(labelFound, "expected the Label rule following the @media block to be parsed");
	}

	private static void assertWhiteRgb(CSSPrimitiveValue value) {
		assertEquals(CSSPrimitiveValue.CSS_RGBCOLOR, value.getPrimitiveType());
		RGBColor rgb = value.getRGBColorValue();
		assertEquals(255.0f, rgb.getRed().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
		assertEquals(255.0f, rgb.getGreen().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
		assertEquals(255.0f, rgb.getBlue().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
	}

}
