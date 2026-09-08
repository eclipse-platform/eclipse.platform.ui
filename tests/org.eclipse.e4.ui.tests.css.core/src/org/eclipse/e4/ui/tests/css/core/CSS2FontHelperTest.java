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
package org.eclipse.e4.ui.tests.css.core;

import static org.eclipse.e4.ui.css.core.css2.CSS2FontHelper.FONT_WEIGHT_BOLD;
import static org.eclipse.e4.ui.css.core.css2.CSS2FontHelper.FONT_WEIGHT_NORMAL;
import static org.eclipse.e4.ui.css.core.css2.CSS2FontHelper.getCSSFontPropertyName;
import static org.eclipse.e4.ui.css.core.css2.CSS2FontHelper.getFontWeight;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;

import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssDimension;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumber;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssUnit;
import org.junit.jupiter.api.Test;

public class CSS2FontHelperTest {

	private static CssPrimitive keyword(String value) {
		return new CssText(CssText.Kind.IDENT, value);
	}

	private static CssPrimitive number(double value) {
		return new CssNumber(value, true);
	}

	@Test
	void testKeywordWeights() {
		assertEquals(OptionalInt.of(FONT_WEIGHT_NORMAL), getFontWeight(keyword("normal"), FONT_WEIGHT_NORMAL));
		assertEquals(OptionalInt.of(FONT_WEIGHT_BOLD), getFontWeight(keyword("bold"), FONT_WEIGHT_NORMAL));
		assertEquals(OptionalInt.of(FONT_WEIGHT_BOLD), getFontWeight(keyword("BOLD"), FONT_WEIGHT_NORMAL));
	}

	@Test
	void testNumericWeights() {
		assertEquals(OptionalInt.of(100), getFontWeight(number(100), FONT_WEIGHT_NORMAL));
		assertEquals(OptionalInt.of(600), getFontWeight(number(600), FONT_WEIGHT_NORMAL));
		assertEquals(OptionalInt.of(900), getFontWeight(number(900), FONT_WEIGHT_NORMAL));
	}

	@Test
	void testWeightsOutsideTheAllowedRange() {
		assertFalse(getFontWeight(number(0), FONT_WEIGHT_NORMAL).isPresent());
		assertFalse(getFontWeight(number(1001), FONT_WEIGHT_NORMAL).isPresent());
	}

	@Test
	void testUnknownWeightIsNotAWeight() {
		assertFalse(getFontWeight(keyword("semibold"), FONT_WEIGHT_NORMAL).isPresent());
		assertFalse(getFontWeight(new CssDimension(600, CssUnit.PT), FONT_WEIGHT_NORMAL).isPresent());
	}

	@Test
	void testBolderAndLighterStepFromTheInheritedWeight() {
		assertEquals(OptionalInt.of(FONT_WEIGHT_BOLD), getFontWeight(keyword("bolder"), FONT_WEIGHT_NORMAL));
		assertEquals(OptionalInt.of(900), getFontWeight(keyword("bolder"), FONT_WEIGHT_BOLD));
		assertEquals(OptionalInt.of(900), getFontWeight(keyword("bolder"), 900));

		assertEquals(OptionalInt.of(100), getFontWeight(keyword("lighter"), FONT_WEIGHT_NORMAL));
		assertEquals(OptionalInt.of(FONT_WEIGHT_NORMAL), getFontWeight(keyword("lighter"), FONT_WEIGHT_BOLD));
		assertEquals(OptionalInt.of(100), getFontWeight(keyword("lighter"), 100));
	}

	@Test
	void testShorthandRoutesWeightKeywords() {
		for (String keyword : new String[] { "normal", "bold", "bolder", "lighter" }) {
			assertEquals("font-weight", getCSSFontPropertyName(keyword(keyword)), keyword);
		}
	}

	@Test
	void testShorthandRoutesBareWeightSteps() {
		assertEquals("font-weight", getCSSFontPropertyName(number(600)));
		assertEquals("font-weight", getCSSFontPropertyName(number(900)));
	}

	@Test
	void testShorthandRoutesOtherBareNumbersToSize() {
		assertEquals("font-size", getCSSFontPropertyName(number(12)));
		assertEquals("font-size", getCSSFontPropertyName(number(650)));
		assertEquals("font-size", getCSSFontPropertyName(new CssDimension(600, CssUnit.PT)));
	}

	@Test
	void testShorthandRejectsValuesThatAreNoFontProperty() {
		assertNull(getCSSFontPropertyName(new CssDimension(1, CssUnit.CM)));
	}

	@Test
	void testShorthandRoutesRemainingIdentifiersToFamily() {
		assertEquals("font-family", getCSSFontPropertyName(keyword("Terminal")));
		assertTrue(getFontWeight(keyword("Terminal"), FONT_WEIGHT_NORMAL).isEmpty());
	}
}
