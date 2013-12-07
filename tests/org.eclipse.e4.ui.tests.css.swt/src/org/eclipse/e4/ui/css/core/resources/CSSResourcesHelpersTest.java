/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.resources;

import static org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpers.getCSSValueKey;
import static org.eclipse.e4.ui.css.core.resources.CSSResourcesHelpers.getCSSFontPropertiesKey;

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTHelperTestCase;
import org.w3c.dom.css.CSSPrimitiveValue;

@SuppressWarnings("restriction")
public class CSSResourcesHelpersTest extends CSSSWTHelperTestCase {	
	public void testGetCSSValueKeyWhenFont() throws Exception {
		CSS2FontProperties fontProperties = fontProperties("Arial", 10, null);
		
		String result = getCSSValueKey(fontProperties);
		
		assertNotNull(result);
		assertEquals(getCSSFontPropertiesKey(fontProperties), result);
	}
	
	public void testGetCSSValueKeyWhenDefinitionAsFontFamily() throws Exception {
		CSS2FontProperties fontProperties = fontProperties(addFontDefinitionMarker("symbolicName"), 10, null);
		
		String result = getCSSValueKey(fontProperties);
		
		assertNotNull(result);
		assertEquals(getCSSFontPropertiesKey(fontProperties), result);
	}
	
	public void testGetCSSValueKeyWhenRgbAsColorValue() throws Exception {
		CSSPrimitiveValue colorValue = colorValue("rgb(255,0,0)");
		
		String result = getCSSValueKey(colorValue);
		
		assertNotNull(result);
		assertEquals("rgb(255,0,0)", result);
	}
	
	public void testGetCSSValueKeyWhenDefinitionAsColorValue() throws Exception {
		CSSPrimitiveValue colorValue = colorValue(addColorDefinitionMarker("symbolicName"));
		
		String result = getCSSValueKey(colorValue);
		
		assertNotNull(result);
		assertEquals("#symbolicName", result);
	}
}
