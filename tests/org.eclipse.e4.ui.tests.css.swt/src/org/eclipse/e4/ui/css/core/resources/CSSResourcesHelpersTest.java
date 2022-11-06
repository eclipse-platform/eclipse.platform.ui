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
package org.eclipse.e4.ui.css.core.resources;

import static org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpers.getCSSFontPropertiesKey;
import static org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpers.getCSSValueKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTHelperTestCase;
import org.junit.jupiter.api.Test;
import org.w3c.dom.css.CSSPrimitiveValue;

public class CSSResourcesHelpersTest extends CSSSWTHelperTestCase {
	@Test
	void testGetCSSValueKeyWhenFont() {
		CSS2FontProperties fontProperties = fontProperties("Arial", 10, null, null);
		String result = getCSSValueKey(fontProperties);
		assertNotNull(result);
		assertEquals(getCSSFontPropertiesKey(fontProperties), result);
	}

	@Test
	void testGetCSSValueKeyWhenDefinitionAsFontFamily() {
		CSS2FontProperties fontProperties = fontProperties(addFontDefinitionMarker("symbolicName"), 10, null, null);
		String result = getCSSValueKey(fontProperties);
		assertNotNull(result);
		assertEquals(getCSSFontPropertiesKey(fontProperties), result);
	}

	@Test
	void testGetCSSValueKeyWhenRgbAsColorValue() {
		CSSPrimitiveValue colorValue = colorValue("rgb(255,0,0)");
		String result = getCSSValueKey(colorValue);
		assertNotNull(result);
		assertEquals("rgb(255,0,0)", result);
	}

	@Test
	void testGetCSSValueKeyWhenDefinitionAsColorValue() {
		CSSPrimitiveValue colorValue = colorValue(addColorDefinitionMarker("symbolicName"));

		String result = getCSSValueKey(colorValue);

		assertNotNull(result);
		assertEquals("#symbolicName", result);
	}
}
