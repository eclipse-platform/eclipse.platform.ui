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

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTHelperTestCase;
import org.eclipse.e4.ui.css.swt.resources.ResourceByDefinitionKey;
import org.eclipse.e4.ui.css.swt.resources.SWTResourceRegistryKeyFactory;
import org.eclipse.swt.SWT;
import org.w3c.dom.css.CSSPrimitiveValue;

@SuppressWarnings("restriction")
public class SWTResourceRegistryKeyFactoryTest extends CSSSWTHelperTestCase {
	private SWTResourceRegistryKeyFactory factory = new SWTResourceRegistryKeyFactory();

	public void testCreateKeyWhenFontProperty() throws Exception {
		CSS2FontProperties fontProperties = fontProperties("Arial", 12,
				SWT.ITALIC);

		Object result = factory.createKey(fontProperties);

		assertEquals(String.class, result.getClass());
		assertEquals(CSSResourcesHelpers.getCSSValueKey(fontProperties), result);
	}

	public void testCreateKeyWhenColorValue() throws Exception {
		CSSPrimitiveValue colorValue = colorValue("red");

		Object result = factory.createKey(colorValue);

		assertEquals(String.class, result.getClass());
		assertEquals(CSSResourcesHelpers.getCSSValueKey(colorValue), result);
	}

	public void testCreateKeyWhenFontByDefinition() throws Exception {
		CSS2FontProperties fontProperties = fontProperties(
				"#font-by-definition", 12,
				SWT.ITALIC);

		Object result = factory.createKey(fontProperties);

		assertEquals(ResourceByDefinitionKey.class, result.getClass());
		assertEquals(CSSResourcesHelpers.getCSSValueKey(fontProperties).toString(), result.toString());
	}

	public void testCreateKeyWhenColorByDefinition() throws Exception {
		CSSPrimitiveValue colorValue = colorValue("#color-by-definition");

		Object result = factory.createKey(colorValue);

		assertEquals(ResourceByDefinitionKey.class, result.getClass());
		assertEquals(CSSResourcesHelpers.getCSSValueKey(colorValue).toString(),
				result.toString());
	}
}
