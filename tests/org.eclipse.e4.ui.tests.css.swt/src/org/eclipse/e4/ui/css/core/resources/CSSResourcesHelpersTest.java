/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.resources;

import static org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpers.getCSSFontPropertiesKey;
import static org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpers.getCSSValueKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTHelperTestCase;
import org.junit.Test;
import org.w3c.dom.css.CSSPrimitiveValue;

public class CSSResourcesHelpersTest extends CSSSWTHelperTestCase {
	@Test
	public void testGetCSSValueKeyWhenFont() {
		CSS2FontProperties fontProperties = null;
		fontProperties = fontProperties("Arial", 10, null, null);
		String result = getCSSValueKey(fontProperties);
		assertNotNull(result);
		assertEquals(getCSSFontPropertiesKey(fontProperties), result);
	}

	@Test
	public void testGetCSSValueKeyWhenDefinitionAsFontFamily() {
		CSS2FontProperties fontProperties = null;
		fontProperties = fontProperties(addFontDefinitionMarker("symbolicName"), 10, null, null);
		String result = getCSSValueKey(fontProperties);
		assertNotNull(result);
		assertEquals(getCSSFontPropertiesKey(fontProperties), result);
	}

	@Test
	public void testGetCSSValueKeyWhenRgbAsColorValue() {
		CSSPrimitiveValue colorValue = colorValue("rgb(255,0,0)");
		String result = getCSSValueKey(colorValue);
		assertNotNull(result);
		assertEquals("rgb(255,0,0)", result);
	}

	@Test
	public void testGetCSSValueKeyWhenDefinitionAsColorValue() {
		CSSPrimitiveValue colorValue = colorValue(addColorDefinitionMarker("symbolicName"));

		String result = getCSSValueKey(colorValue);

		assertNotNull(result);
		assertEquals("#symbolicName", result);
	}
}
