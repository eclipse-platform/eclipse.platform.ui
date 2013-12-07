/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.getFontData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

@SuppressWarnings("restriction")
public class CSSSWTFontHelperTest extends CSSSWTHelperTestCase {
	public void testGetFontData() throws Exception {
		FontData result = getFontData(fontProperties("Times", 11, SWT.NORMAL),
				new FontData());

		assertEquals("Times", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.NORMAL, result.getStyle());
	}

	public void testGetFontDataWhenMissingFamilyInCss() throws Exception {
		FontData result = getFontData(fontProperties(null, 11, SWT.NORMAL),
				new FontData("Courier", 5, SWT.ITALIC));

		assertEquals("Courier", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.NORMAL, result.getStyle());
	}

	public void testGetFontDataWhenMissingSizeInCss() throws Exception {
		FontData result = getFontData(fontProperties("Arial", null, SWT.NORMAL),
				new FontData("Courier", 5, SWT.ITALIC));

		assertEquals("Arial", result.getName());
		assertEquals(5, result.getHeight());
		assertEquals(SWT.NORMAL, result.getStyle());
	}

	public void testGetFontDataWhenMissingStyleInCss() throws Exception {
		FontData result = getFontData(fontProperties("Times", 11, null),
				new FontData("Courier", 5, SWT.ITALIC));

		assertEquals("Times", result.getName());
		assertEquals(11, result.getHeight());
		assertEquals(SWT.ITALIC, result.getStyle());
	}

	public void testGetFontDataWhenFontFamilyFromDefinition() throws Exception {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);

		FontData result = getFontData(
				fontProperties(
						addFontDefinitionMarker("org-eclipse-jface-bannerfont"),
						10, null),
						new FontData());

		assertEquals("Arial", result.getName());
		assertEquals(10, result.getHeight());
		assertEquals(SWT.ITALIC, result.getStyle());
	}

	public void testGetFontDataWhenFontFamilyAndSizeFromDefinition() throws Exception {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);

		FontData result = getFontData(
				fontProperties(
						addFontDefinitionMarker("org-eclipse-jface-bannerfont"),
						null, SWT.NORMAL),
						new FontData());

		assertEquals("Arial", result.getName());
		assertEquals(15, result.getHeight());
		assertEquals(SWT.ITALIC, result.getStyle());
	}

	public void testGetFontDataWhenFontFamilySizeAndStyleFromDefinition() throws Exception {
		registerFontProviderWith("org.eclipse.jface.bannerfont", "Arial", 15, SWT.ITALIC);

		FontData result = getFontData(
				fontProperties(addFontDefinitionMarker("org-eclipse-jface-bannerfont")),
				new FontData());

		assertEquals("Arial", result.getName());
		assertEquals(15, result.getHeight());
		assertEquals(SWT.ITALIC, result.getStyle());
	}
}
