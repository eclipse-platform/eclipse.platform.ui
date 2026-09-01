/*******************************************************************************
 * Copyright (c) 2009, 2026 EclipseSource and others.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.e4.ui.css.core.impl.dom.CSSMediaRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleRuleImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CSSStyleSheetImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CssRule;
import org.eclipse.e4.ui.css.core.impl.dom.Media;
import org.eclipse.e4.ui.tests.css.core.util.ParserTestUtil;
import org.junit.jupiter.api.Test;

/**
 * Verifies {@code @media} parsing and the platform features the engine
 * evaluates its queries against.
 */
public class MediaRulesTest {

	private static final Media.Context LINUX = new Media.Context("linux", "gtk", "6.14.0-33-generic");
	private static final Media.Context WINDOWS = new Media.Context("win32", "win32", "10.0");
	private static final Media.Context MACOS = new Media.Context("macosx", "cocoa", "10.15.7");

	private static CSSMediaRuleImpl parseMediaRule(String css) {
		CSSStyleSheetImpl styleSheet = ParserTestUtil.parseCssWithoutImports(css);
		assertTrue(styleSheet.getProblems().isEmpty(), "unexpected parse problems: " + styleSheet.getProblems());
		CssRule rule = styleSheet.getRules().get(0);
		assertTrue(rule instanceof CSSMediaRuleImpl, "expected a media rule, got " + rule);
		return (CSSMediaRuleImpl) rule;
	}

	@Test
	void testMediaBlockKeepsItsRules() {
		String css = """
				@media screen, print {
				BODY { line-height: 1.2 }
				Label { color: #FF0000 }
				}
				Label { background-color: #FF0000 }""";
		CSSStyleSheetImpl styleSheet = ParserTestUtil.parseCssWithoutImports(css);

		assertEquals(2, styleSheet.getRules().size());
		CSSMediaRuleImpl media = (CSSMediaRuleImpl) styleSheet.getRules().get(0);
		assertEquals(2, media.getRules().size());
		assertTrue(media.getRules().get(0).getCssText().contains("line-height"));
		assertTrue(((CSSStyleRuleImpl) styleSheet.getRules().get(1)).getCssText().contains("background-color"));
	}

	@Test
	void testScreenAndAllMatchOtherMediaTypesDoNot() {
		assertTrue(parseMediaRule("@media screen { Label { color: red } }").matches(LINUX));
		assertTrue(parseMediaRule("@media all { Label { color: red } }").matches(LINUX));
		assertFalse(parseMediaRule("@media print { Label { color: red } }").matches(LINUX));
		assertFalse(parseMediaRule("@media speech { Label { color: red } }").matches(LINUX));
	}

	@Test
	void testOperatingSystemFeature() {
		CSSMediaRuleImpl rule = parseMediaRule("@media (-eclipse-os: linux) { Label { color: red } }");
		assertTrue(rule.matches(LINUX));
		assertFalse(rule.matches(WINDOWS));
	}

	@Test
	void testWindowingSystemFeature() {
		CSSMediaRuleImpl rule = parseMediaRule("@media (-eclipse-ws: gtk) { Label { color: red } }");
		assertTrue(rule.matches(LINUX));
		assertFalse(rule.matches(WINDOWS));
	}

	@Test
	void testFeatureValueIsCaseInsensitive() {
		assertTrue(parseMediaRule("@media (-ECLIPSE-OS: LINUX) { Label { color: red } }").matches(LINUX));
	}

	@Test
	void testBooleanFormIsTrueWheneverTheFeatureHasAValue() {
		CSSMediaRuleImpl rule = parseMediaRule("@media (-eclipse-os) { Label { color: red } }");
		assertTrue(rule.matches(LINUX));
		assertFalse(rule.matches(new Media.Context(null, "gtk")));
	}

	@Test
	void testAndRequiresEveryFeature() {
		CSSMediaRuleImpl rule = parseMediaRule(
				"@media screen and (-eclipse-os: linux) and (-eclipse-ws: gtk) { Label { color: red } }");
		assertTrue(rule.matches(LINUX));
		assertFalse(rule.matches(new Media.Context("linux", "cocoa")));
	}

	@Test
	void testCommaIsOr() {
		CSSMediaRuleImpl rule = parseMediaRule(
				"@media (-eclipse-os: linux), (-eclipse-os: win32) { Label { color: red } }");
		assertTrue(rule.matches(LINUX));
		assertTrue(rule.matches(WINDOWS));
		assertFalse(rule.matches(new Media.Context("macosx", "cocoa")));
	}

	@Test
	void testNotInverts() {
		assertFalse(parseMediaRule("@media not (-eclipse-os: linux) { Label { color: red } }").matches(LINUX));
		assertTrue(parseMediaRule("@media not (-eclipse-os: linux) { Label { color: red } }").matches(WINDOWS));
		assertFalse(parseMediaRule("@media not screen { Label { color: red } }").matches(LINUX));
	}

	@Test
	void testOnlyIsIgnored() {
		assertTrue(parseMediaRule("@media only screen { Label { color: red } }").matches(LINUX));
	}

	@Test
	void testOsVersionMatchesAtThePrecisionOfTheValue() {
		CSSMediaRuleImpl rule = parseMediaRule("@media (-eclipse-os-version: \"10.15\") { Label { color: red } }");
		assertTrue(rule.matches(MACOS));
		assertFalse(rule.matches(new Media.Context("macosx", "cocoa", "10.14.6")));
		assertFalse(rule.matches(new Media.Context("macosx", "cocoa", "11.0")));
	}

	@Test
	void testMinOsVersionIsInclusive() {
		CSSMediaRuleImpl rule = parseMediaRule("@media (-eclipse-min-os-version: \"10.15\") { Label { color: red } }");
		assertTrue(rule.matches(MACOS));
		assertTrue(rule.matches(new Media.Context("macosx", "cocoa", "11.0")));
		assertFalse(rule.matches(new Media.Context("macosx", "cocoa", "10.14.6")));
	}

	@Test
	void testMaxOsVersionCoversTheWholeLineItNames() {
		CSSMediaRuleImpl rule = parseMediaRule("@media (-eclipse-max-os-version: \"10.15\") { Label { color: red } }");
		assertTrue(rule.matches(MACOS));
		assertTrue(rule.matches(new Media.Context("macosx", "cocoa", "10.14.6")));
		assertFalse(rule.matches(new Media.Context("macosx", "cocoa", "11.0")));
	}

	@Test
	void testOsVersionIgnoresANonNumericTail() {
		assertTrue(parseMediaRule("@media (-eclipse-min-os-version: \"6.14\") { Label { color: red } }")
				.matches(LINUX));
	}

	@Test
	void testOsVersionCombinesWithTheOperatingSystem() {
		CSSMediaRuleImpl rule = parseMediaRule(
				"@media (-eclipse-os: macosx) and (-eclipse-min-os-version: \"10.15\") { Label { color: red } }");
		assertTrue(rule.matches(MACOS));
		assertFalse(rule.matches(LINUX));
	}

	@Test
	void testOsVersionNeverMatchesWhenTheVersionIsUnknown() {
		assertFalse(parseMediaRule("@media (-eclipse-min-os-version: \"10.15\") { Label { color: red } }")
				.matches(new Media.Context("macosx", "cocoa")));
	}

	@Test
	void testUnquotedTwoSegmentVersionIsAccepted() {
		assertTrue(parseMediaRule("@media (-eclipse-min-os-version: 10.15) { Label { color: red } }").matches(MACOS));
	}

	@Test
	void testUnknownFeatureNeverMatches() {
		assertFalse(parseMediaRule("@media (-eclipse-unknown: linux) { Label { color: red } }").matches(LINUX));
	}

	@Test
	void testEmptyQueryListMatches() {
		assertTrue(parseMediaRule("@media { Label { color: red } }").matches(LINUX));
	}

	@Test
	void testUnsupportedButWellFormedQueryIsNotAParseError() {
		CSSStyleSheetImpl styleSheet = ParserTestUtil
				.parseCssWithoutImports("@media (min-width: 100px) { Label { color: red } }");

		assertTrue(styleSheet.getProblems().isEmpty(), "unexpected parse problems: " + styleSheet.getProblems());
		assertFalse(((CSSMediaRuleImpl) styleSheet.getRules().get(0)).matches(LINUX));
	}

	@Test
	void testMalformedQueryBecomesNotAllAndSparesItsNeighbours() {
		CSSStyleSheetImpl styleSheet = ParserTestUtil
				.parseCssWithoutImports("@media 42, (-eclipse-os: linux) { Label { color: red } }");
		CSSMediaRuleImpl rule = (CSSMediaRuleImpl) styleSheet.getRules().get(0);

		assertFalse(styleSheet.getProblems().isEmpty(), "the malformed query should be reported");
		assertEquals(List.of(Media.Query.NEVER,
				new Media.Query(false, "all", List.of(new Media.Feature(Media.FEATURE_OS, "linux")))),
				rule.getQueries());
		assertTrue(rule.matches(LINUX));
		assertFalse(rule.matches(WINDOWS));
	}

	@Test
	void testMalformedRuleInsideBlockDoesNotSwallowTheBlock() {
		CSSStyleSheetImpl styleSheet = ParserTestUtil.parseCssWithoutImports("""
				@media screen {
				[ { color: red }
				Label { color: blue }
				}
				Button { color: green }""");

		assertEquals(2, styleSheet.getRules().size());
		CSSMediaRuleImpl media = (CSSMediaRuleImpl) styleSheet.getRules().get(0);
		assertEquals(1, media.getRules().size());
		assertEquals("Label", media.getRules().get(0).getSelectorText());
		assertEquals("Button", ((CSSStyleRuleImpl) styleSheet.getRules().get(1)).getSelectorText());
	}

	@Test
	void testMediaTextRoundTrip() {
		CSSMediaRuleImpl rule = parseMediaRule(
				"@media not screen and (-eclipse-os: linux), (-eclipse-ws) { Label { color: red } }");
		assertEquals("not screen and (-eclipse-os: linux), all and (-eclipse-ws)", rule.getMediaText());
	}
}
