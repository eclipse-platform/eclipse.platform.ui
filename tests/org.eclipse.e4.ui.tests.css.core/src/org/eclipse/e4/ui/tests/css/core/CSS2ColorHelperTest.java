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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.e4.ui.css.core.css2.CSS2ColorHelper;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssColor;
import org.junit.jupiter.api.Test;

public class CSS2ColorHelperTest {

	@Test
	void testAliceBlueResolves() {
		assertTrue(CSS2ColorHelper.isColorName("aliceblue"));
		CssColor color = CSS2ColorHelper.getRGBColor("AliceBlue");
		assertNotNull(color);
		assertEquals(0xF0, (int) color.red().value());
		assertEquals(0xF8, (int) color.green().value());
		assertEquals(0xFF, (int) color.blue().value());
		assertEquals("aliceblue", CSS2ColorHelper.getColorNameFromHexaColor("#F0F8FF"));
	}
}
