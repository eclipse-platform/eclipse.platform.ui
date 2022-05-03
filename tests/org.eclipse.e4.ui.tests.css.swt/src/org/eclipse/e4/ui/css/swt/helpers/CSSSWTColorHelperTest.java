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

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper.getSWTColor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.css.CSSValue;

public class CSSSWTColorHelperTest extends CSSSWTHelperTestCase {
	private Display display;

	@Before
	public void setUp() {
		display = Display.getDefault();
	}

	@Test
	public void testGetSWTColor() {
		Color result = getSWTColor(colorValue("red"), display);
		assertNotNull(result);
		assertEquals(255, result.getRed());
		assertEquals(0, result.getBlue());
		assertEquals(0, result.getGreen());
	}

	@Test
	public void testGetSWTColorWhenNotSupportedColorType() {
		Color result = getSWTColor(colorValue("123213", CSSValue.CSS_CUSTOM),
				display);

		assertNull(result);
	}

	@Test
	public void testGetSWTColorWhenInvalidColorValue() {
		Color result = getSWTColor(colorValue("asdsad12"), display);

		assertNotNull(result);
		assertEquals(0, result.getRed());
		assertEquals(0, result.getBlue());
		assertEquals(0, result.getGreen());
	}

	@Test
	public void testGetSWTColorWhenColorFromDefinition() {
		registerColorProviderWith("org.eclipse.jdt.debug.ui.InDeadlockColor", new RGB(0, 255, 0));

		Color result = getSWTColor(
				colorValue(addColorDefinitionMarker("org-eclipse-jdt-debug-ui-InDeadlockColor")),
				display);

		assertNotNull(result);
		assertEquals(0, result.getRed());
		assertEquals(0, result.getBlue());
		assertEquals(255, result.getGreen());
	}
}
