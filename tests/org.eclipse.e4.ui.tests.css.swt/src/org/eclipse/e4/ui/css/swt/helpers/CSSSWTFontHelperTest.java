/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.getFontData;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontPropertiesImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssDimension;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssUnit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.junit.jupiter.api.Test;

public class CSSSWTFontHelperTest extends CSSSWTHelperTestCase {

	@Test
	void testGetFontData() {
		FontData result = getFontData(fontProperties("Times", 11, CSS_ITALIC, CSS_BOLD),
				new FontData());

		assertEquals("Times", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataWithoutOldFont() {
		FontData result = getFontData(fontProperties("Times", 11, CSS_ITALIC, CSS_BOLD),
				null);

		assertEquals("Times", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataStyledFont() {
		FontData result = getFontData(fontProperties("Times", 11, "normal", "normal"),
				new FontData("Courier", 11, SWT.ITALIC | SWT.BOLD));

		assertEquals("Times", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.NORMAL, result.getStyle());
	}

	@Test
	void testGetFontDataWhenMissingFamilyInCss() {
		FontData result = getFontData(fontProperties(null, 11, CSS_ITALIC, CSS_BOLD),
				new FontData("Courier", 5, SWT.NORMAL));

		assertEquals("Courier", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataWhenMissingSizeInCss() {
		FontData result = getFontData(fontProperties("Arial", null, CSS_ITALIC, CSS_BOLD),
				new FontData("Courier", 5, SWT.NORMAL));

		assertEquals("Arial", result.getName());
		assertEquals(5, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataWhenMissingStyleInCss() {
		FontData result = getFontData(fontProperties("Times", 11, null, CSS_BOLD),
				new FontData("Courier", 5, SWT.ITALIC));

		assertEquals("Times", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataWhenMissingWeightInCss() {
		FontData result = getFontData(fontProperties("Times", 11, CSS_ITALIC, null),
				new FontData("Courier", 5, SWT.BOLD));

		assertEquals("Times", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataWhenMissingAllInCss() {
		FontData result = getFontData(fontProperties(null, null, null, null),
				new FontData("Courier", 11, SWT.ITALIC | SWT.BOLD));

		assertEquals("Courier", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataWhenFontFamilyFromDefinitionAndOverwritingSize() {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC | SWT.BOLD);

		FontData result = getFontData(
				fontProperties(addFontDefinitionMarker("org-eclipse-jface-bannerfont"), 10, null, null),
				new FontData());

		assertEquals("Arial", result.getName());
		assertEquals(10, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataWhenFontFamilyFromDefinitionAndOverwritingStyle() {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.BOLD);

		FontData result = getFontData(
				fontProperties(addFontDefinitionMarker("org-eclipse-jface-bannerfont"), null, CSS_ITALIC, null),
				new FontData());

		assertEquals("Arial", result.getName());
		assertEquals(15, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataWhenFontFamilyFromDefinitionAndOverwritingWeight() {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);

		FontData result = getFontData(
				fontProperties(addFontDefinitionMarker("org-eclipse-jface-bannerfont"), null, null, CSS_BOLD),
				new FontData());

		assertEquals("Arial", result.getName());
		assertEquals(15, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	@Test
	void testGetFontDataWithSizeInEm() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(1.5, CssUnit.EM)),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(15, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeInEmIsRounded() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(1.2, CssUnit.EM)),
				new FontData("Courier", 9, SWT.NORMAL));

		assertEquals(11, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeInPercent() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(120, CssUnit.PERCENT)),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(12, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeInPercentBelowHundred() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(50, CssUnit.PERCENT)),
				new FontData("Courier", 11, SWT.NORMAL));

		assertEquals(6, result.getHeight());
	}

	@Test
	void testGetFontDataWithRelativeSizeNeverReachesZero() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(1, CssUnit.PERCENT)),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(1, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeLarger() {
		FontData result = getFontData(fontPropertiesWithSize(keyword("larger")),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(12, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeSmaller() {
		FontData result = getFontData(fontPropertiesWithSize(keyword("smaller")),
				new FontData("Courier", 12, SWT.NORMAL));

		assertEquals(10, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeKeywordIsCaseInsensitive() {
		FontData result = getFontData(fontPropertiesWithSize(keyword("LARGER")),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(12, result.getHeight());
	}

	@Test
	void testGetFontDataWithRelativeSizeAndWithoutOldFont() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(1.5, CssUnit.EM)), null);

		assertEquals(new FontData().getHeight(), result.getHeight());
	}

	@Test
	void testGetFontDataWithUnknownSizeKeyword() {
		FontData result = getFontData(fontPropertiesWithSize(keyword("xx-large")),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(10, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeInPixels() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(16, CssUnit.PX)),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(12, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeInPixelsIsRounded() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(11, CssUnit.PX)),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(8, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeInPoints() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(16, CssUnit.PT)),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(16, result.getHeight());
	}

	@Test
	void testGetFontDataWithSizeWithoutUnitIsTakenAsPoints() {
		FontData result = getFontData(fontPropertiesWithSize(new CssDimension(16, CssUnit.NUMBER)),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals(16, result.getHeight());
	}

	@Test
	void testGetFontDataWhenFontFamilyFromDefinitionAndRelativeSize() {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.NORMAL);

		FontData result = getFontData(
				fontPropertiesWithSize(addFontDefinitionMarker("org-eclipse-jface-bannerfont"),
						new CssDimension(200, CssUnit.PERCENT)),
				new FontData("Courier", 10, SWT.NORMAL));

		assertEquals("Arial", result.getName());
		assertEquals(20, result.getHeight());
	}

	@Test
	void testGetFontDataFromFontDefinition() {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC | SWT.BOLD);

		FontData result = getFontData(
				fontProperties(addFontDefinitionMarker("org-eclipse-jface-bannerfont")),
				new FontData());

		assertEquals("Arial", result.getName());
		assertEquals(15, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}

	private static CssPrimitive keyword(String value) {
		return new CssText(CssText.Kind.IDENT, value);
	}

	private static CSS2FontProperties fontPropertiesWithSize(CssPrimitive size) {
		return fontPropertiesWithSize(null, size);
	}

	private static CSS2FontProperties fontPropertiesWithSize(String family, CssPrimitive size) {
		CSS2FontProperties result = new CSS2FontPropertiesImpl();
		if (family != null) {
			result.setFamily(new CssText(CssText.Kind.IDENT, family));
		}
		result.setSize(size);
		result.setSizeFromCSS(true);
		return result;
	}
}
