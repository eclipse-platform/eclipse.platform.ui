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
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper.getSWTColor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.css.CSSValue;

public class CSSSWTColorHelperTest extends CSSSWTHelperTestCase {
	private Display display;
	private Color result;

	@Before
	public void setUp() {

		display = Display.getDefault();
	}

	@After
	public void tearDown() {
		if (result != null) {
			result.dispose();
		}
		display.dispose();
	}

	@Test
	public void testGetSWTColor() {
		result = getSWTColor(colorValue("red"), display);
		assertNotNull(result);
		assertEquals(255, result.getRed());
		assertEquals(0, result.getBlue());
		assertEquals(0, result.getGreen());
	}

	@Test
	public void testGetSWTColorWhenNotSupportedColorType() {
		result = getSWTColor(colorValue("123213", CSSValue.CSS_CUSTOM),
				display);

		assertNull(result);
	}

	@Test
	public void testGetSWTColorWhenInvalidColorValue() {
		result = getSWTColor(colorValue("asdsad12"), display);

		assertNotNull(result);
		assertEquals(0, result.getRed());
		assertEquals(0, result.getBlue());
		assertEquals(0, result.getGreen());
	}

	@Test
	public void testGetSWTColorWhenColorFromDefinition() {
		registerColorProviderWith("org.eclipse.jdt.debug.ui.InDeadlockColor", new RGB(0, 255, 0));

		result = getSWTColor(
				colorValue(addColorDefinitionMarker("org-eclipse-jdt-debug-ui-InDeadlockColor")),
				display);

		assertNotNull(result);
		assertEquals(0, result.getRed());
		assertEquals(0, result.getBlue());
		assertEquals(255, result.getGreen());
	}
}
