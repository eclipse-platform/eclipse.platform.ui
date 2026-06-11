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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors;
import org.eclipse.e4.ui.css.core.impl.engine.selector.Selectors.Selector;
import org.eclipse.e4.ui.css.core.impl.parser.CssParseException;
import org.eclipse.e4.ui.css.core.impl.parser.CssParser;
import org.junit.jupiter.api.Test;
import java.util.List;

import org.eclipse.e4.ui.css.core.impl.dom.CSSImportRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleSheetImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CssRule;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

/**
 * Unit tests for the hand-written {@link CssParser}. They pin the parser's
 * output directly (selector trees, specificity, value model, at-rule handling)
 * so the cutover that replaces Batik with this parser is gated by behaviour,
 * not by inspection.
 */
public class CssParserTest {

	private static Selector firstSelector(String selector) {
		return CssParser.parseSelectors(selector).item(0);
	}

	private static CSSStyleRuleImpl firstStyleRule(String css) {
		return (CSSStyleRuleImpl) CssParser.parseStyleSheet(css).getRules().get(0);
	}

	// ---------- selectors ----------

	@Test
	void testSimpleSelectorTypesAndSpecificity() {
		assertInstanceOf(Selectors.Universal.class, firstSelector("*"));
		assertEquals(0, firstSelector("*").specificity());
		assertInstanceOf(Selectors.ElementType.class, firstSelector("Button"));
		assertEquals(1, firstSelector("Button").specificity());
		assertInstanceOf(Selectors.ClassSelector.class, firstSelector(".primary"));
		assertEquals(10, firstSelector(".primary").specificity());
		assertInstanceOf(Selectors.IdSelector.class, firstSelector("#go"));
		assertEquals(100, firstSelector("#go").specificity());
	}

	@Test
	void testCompoundSelectorIsAndTree() {
		Selector selector = firstSelector("Button.primary#go");
		assertInstanceOf(Selectors.And.class, selector);
		assertEquals(111, selector.specificity());
		assertEquals("Button.primary#go", selector.text());
	}

	@Test
	void testCombinators() {
		assertInstanceOf(Selectors.Descendant.class, firstSelector("Composite Button"));
		assertInstanceOf(Selectors.Child.class, firstSelector("Composite > Button"));
		assertInstanceOf(Selectors.Adjacent.class, firstSelector("Composite + Button"));
		assertEquals(2, firstSelector("Composite Button").specificity());
		assertEquals("Composite > Button", firstSelector("Composite>Button").text());
	}

	@Test
	void testAttributeAndPseudoSelectors() {
		assertInstanceOf(Selectors.AttributeSelector.class, firstSelector("[style]"));
		assertInstanceOf(Selectors.AttributeIncludes.class, firstSelector("[style~='SWT.CHECK']"));
		assertInstanceOf(Selectors.AttributeBeginHyphen.class, firstSelector("[lang|='en']"));
		assertEquals(11, firstSelector("Button[style]").specificity());
		assertEquals(11, firstSelector("Button:selected").specificity());
	}

	@Test
	void testUniversalElementDroppedBeforeConditions() {
		// '.foo' and '*[a]' carry no element-type contribution.
		assertInstanceOf(Selectors.ClassSelector.class, firstSelector(".foo"));
		assertInstanceOf(Selectors.AttributeSelector.class, firstSelector("*[a]"));
	}

	@Test
	void testSelectorListMaxSpecificity() {
		Selectors.SelectorList list = CssParser.parseSelectors("Button, .primary, #go");
		assertEquals(3, list.getLength());
		assertEquals(100, list.specificity());
	}

	@Test
	void testMalformedSelectorThrows() {
		assertThrows(CssParseException.class, () -> CssParser.parseSelectors("Button["));
	}

	// ---------- declarations and values ----------

	@Test
	void testSingleDeclaration() {
		CSSStyleRuleImpl rule = firstStyleRule("Button { color: red; }");
		assertEquals("Button", rule.getSelectorText());
		CSSStyleDeclaration style = rule.getStyle();
		assertEquals(1, style.getLength());
		assertEquals("color", style.item(0));
		assertEquals("red", style.getPropertyCSSValue("color").getCssText());
	}

	@Test
	void testImportantPriority() {
		CSSStyleDeclaration style = firstStyleRule("Button { color: red !important; background: blue; }").getStyle();
		assertEquals("important", style.getPropertyPriority("color"));
		assertEquals("", style.getPropertyPriority("background"));
	}

	@Test
	void testTrailingSemicolonAndStraySemicolonsTolerated() {
		assertEquals(1, firstStyleRule("Button { color: red }").getStyle().getLength());
		assertEquals(1, firstStyleRule("Button { ; color: red;; }").getStyle().getLength());
	}

	@Test
	void testMultiValuePropertyIsValueList() {
		CSSValue value = firstStyleRule("Button { margin: 1px 2px 3px 4px; }").getStyle()
				.getPropertyCSSValue("margin");
		assertEquals(CSSValue.CSS_VALUE_LIST, value.getCssValueType());
		assertEquals(4, ((CSSValueList) value).getLength());
	}

	@Test
	void testLengthAndPercentageUnits() {
		CSSPrimitiveValue px = (CSSPrimitiveValue) firstStyleRule("A { width: 10px; }").getStyle()
				.getPropertyCSSValue("width");
		assertEquals(CSSPrimitiveValue.CSS_PX, px.getPrimitiveType());
		assertEquals(10.0f, px.getFloatValue(CSSPrimitiveValue.CSS_PX));

		CSSPrimitiveValue percent = (CSSPrimitiveValue) firstStyleRule("B { width: 50%; }").getStyle()
				.getPropertyCSSValue("width");
		assertEquals(CSSPrimitiveValue.CSS_PERCENTAGE, percent.getPrimitiveType());
	}

	@Test
	void testHexAndRgbColoursAreRgbColor() {
		CSSPrimitiveValue hex = (CSSPrimitiveValue) firstStyleRule("A { color: #ffffff; }").getStyle()
				.getPropertyCSSValue("color");
		assertEquals(CSSPrimitiveValue.CSS_RGBCOLOR, hex.getPrimitiveType());
		assertEquals(255.0f, hex.getRGBColorValue().getRed().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));

		CSSPrimitiveValue rgb = (CSSPrimitiveValue) firstStyleRule("B { color: rgb(255, 0, 128); }").getStyle()
				.getPropertyCSSValue("color");
		assertEquals(CSSPrimitiveValue.CSS_RGBCOLOR, rgb.getPrimitiveType());
		assertEquals(128.0f, rgb.getRGBColorValue().getBlue().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
	}

	@Test
	void testShorthandHexExpands() {
		CSSPrimitiveValue hex = (CSSPrimitiveValue) firstStyleRule("A { color: #fff; }").getStyle()
				.getPropertyCSSValue("color");
		assertEquals(255.0f, hex.getRGBColorValue().getGreen().getFloatValue(CSSPrimitiveValue.CSS_NUMBER));
	}

	@Test
	void testCommentsIgnored() {
		CSSStyleSheetImpl sheet = CssParser.parseStyleSheet("""
				/* lead */ Button { /* in */ color: red; } /* tail */
				""");
		assertEquals(1, sheet.getRules().size());
	}

	// ---------- at-rules ----------

	@Test
	void testImportRuleKept() {
		CSSStyleSheetImpl sheet = CssParser.parseStyleSheet("@import url('other.css');");
		assertEquals(1, sheet.getRules().size());
		CssRule rule = sheet.getRules().get(0);
		assertTrue(rule instanceof CSSImportRuleImpl);
		assertEquals("other.css", ((CSSImportRuleImpl) rule).getHref());
	}

	@Test
	void testMediaAndFontFaceDiscardedFollowingRuleKept() {
		CSSStyleSheetImpl sheet = CssParser.parseStyleSheet("""
				@font-face { font-family: x; }
				@media screen { Hidden { color: red; } }
				Label { color: blue; }
				""");
		// Both at-rules are discarded entirely; only the top-level Label remains.
		List<CssRule> rules = sheet.getRules();
		assertEquals(1, rules.size());
		CSSStyleRuleImpl label = (CSSStyleRuleImpl) rules.get(0);
		assertEquals("Label", label.getSelectorText());
		assertEquals("blue", label.getStyle().getPropertyCSSValue("color").getCssText());
	}

	@Test
	void testMultipleRulesPreserveOrder() {
		List<CssRule> rules = CssParser.parseStyleSheet("A { color: red; } B { color: green; } C { color: blue; }")
				.getRules();
		assertEquals(3, rules.size());
		assertEquals("A", ((CSSStyleRuleImpl) rules.get(0)).getSelectorText());
		assertEquals("C", ((CSSStyleRuleImpl) rules.get(2)).getSelectorText());
	}

	@Test
	void testEmptyStyleSheet() {
		assertEquals(0, CssParser.parseStyleSheet("").getRules().size());
		assertTrue(CssParser.parseStyleSheet("   \n  ").getRules().isEmpty());
	}
}
