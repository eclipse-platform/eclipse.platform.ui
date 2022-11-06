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
	void testGetFontDataFromFontDefinition() {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC | SWT.BOLD);

		FontData result = getFontData(
				fontProperties(addFontDefinitionMarker("org-eclipse-jface-bannerfont")),
				new FontData());

		assertEquals("Arial", result.getName());
		assertEquals(15, result.getHeight());
		assertEquals(SWT.ITALIC | SWT.BOLD, result.getStyle());
	}
}
