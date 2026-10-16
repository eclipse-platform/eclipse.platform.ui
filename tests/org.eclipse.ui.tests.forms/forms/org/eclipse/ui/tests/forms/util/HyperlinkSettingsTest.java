/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     vogella GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HyperlinkSettingsTest {

	private static final RGB THEME_LINK = new RGB(1, 2, 3);
	private static final RGB THEME_ACTIVE_LINK = new RGB(4, 5, 6);

	private RGB savedLink;
	private RGB savedActiveLink;

	@BeforeEach
	public void saveRegistry() {
		ColorRegistry registry = JFaceResources.getColorRegistry();
		savedLink = registry.getRGB(JFacePreferences.HYPERLINK_COLOR);
		savedActiveLink = registry.getRGB(JFacePreferences.ACTIVE_HYPERLINK_COLOR);
		assertNotNull(savedLink);
		assertNotNull(savedActiveLink);
	}

	@AfterEach
	public void restoreRegistry() {
		ColorRegistry registry = JFaceResources.getColorRegistry();
		registry.put(JFacePreferences.HYPERLINK_COLOR, savedLink);
		registry.put(JFacePreferences.ACTIVE_HYPERLINK_COLOR, savedActiveLink);
	}

	@Test
	public void testDefaultForegroundsFollowColorRegistry() {
		HyperlinkSettings settings = new HyperlinkSettings(Display.getCurrent());

		JFaceResources.getColorRegistry().put(JFacePreferences.HYPERLINK_COLOR, THEME_LINK);
		JFaceResources.getColorRegistry().put(JFacePreferences.ACTIVE_HYPERLINK_COLOR, THEME_ACTIVE_LINK);

		assertEquals(THEME_LINK, settings.getForeground().getRGB());
		assertEquals(THEME_ACTIVE_LINK, settings.getActiveForeground().getRGB());
	}

	@Test
	public void testExplicitForegroundsIgnoreColorRegistry() {
		HyperlinkSettings settings = new HyperlinkSettings(Display.getCurrent());
		Color fg = new Color(10, 20, 30);
		Color activeFg = new Color(40, 50, 60);
		settings.setForeground(fg);
		settings.setActiveForeground(activeFg);

		JFaceResources.getColorRegistry().put(JFacePreferences.HYPERLINK_COLOR, THEME_LINK);
		JFaceResources.getColorRegistry().put(JFacePreferences.ACTIVE_HYPERLINK_COLOR, THEME_ACTIVE_LINK);

		assertEquals(fg, settings.getForeground());
		assertEquals(activeFg, settings.getActiveForeground());
	}

	@Test
	public void testInitializeDefaultForegroundsFollowsColorRegistryAgain() {
		HyperlinkSettings settings = new HyperlinkSettings(Display.getCurrent());
		settings.setForeground(new Color(10, 20, 30));
		settings.setActiveForeground(new Color(40, 50, 60));
		settings.initializeDefaultForegrounds(Display.getCurrent());

		JFaceResources.getColorRegistry().put(JFacePreferences.HYPERLINK_COLOR, THEME_LINK);
		JFaceResources.getColorRegistry().put(JFacePreferences.ACTIVE_HYPERLINK_COLOR, THEME_ACTIVE_LINK);

		assertEquals(THEME_LINK, settings.getForeground().getRGB());
		assertEquals(THEME_ACTIVE_LINK, settings.getActiveForeground().getRGB());
	}
}
