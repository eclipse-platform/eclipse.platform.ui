/*******************************************************************************
 * Copyright (c) 2013, 2019 IBM Corporation and others.
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
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Hashtable;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.junit.jupiter.api.Test;
import org.osgi.framework.FrameworkUtil;

public class ColorDefinitionTest extends CSSSWTTestCase {


	@Test
	void testColorDefinition() {
		//given
		CSSEngine engine = createEngine("ColorDefinition#ACTIVE_HYPERLINK_COLOR{color: green}", display);
		ColorDefinition definition = colorDefinition("ACTIVE_HYPERLINK_COLOR", "name", "categoryId", "description");

		assertEquals(new RGB(0, 0, 0), definition.getValue());
		assertFalse(definition.isOverridden());

		//when
		engine.applyStyles(definition, true);

		//then
		assertEquals(new RGB(0, 128, 0), definition.getValue());
		assertEquals("name", definition.getName());
		assertEquals("categoryId", definition.getCategoryId());
		assertTrue(definition.getDescription().startsWith("description"));
		assertTrue(definition.isOverridden());
		engine.dispose();
	}

	@Test
	void testColorDefinitionWhenNameCategoryIdAndDescriptionOverridden() {
		// given
		CSSEngine engine = createEngine("ColorDefinition#ACTIVE_HYPERLINK_COLOR{color: green;" +
				"label:'nameOverridden'; category:'#categoryIdOverridden'; description: 'descriptionOverridden'}", display);
		ColorDefinition definition = colorDefinition("ACTIVE_HYPERLINK_COLOR","name", "categoryId", "description");

		assertEquals(new RGB(0, 0, 0), definition.getValue());
		assertFalse(definition.isOverridden());

		// when
		engine.applyStyles(definition, true);

		// then
		assertEquals(new RGB(0, 128, 0), definition.getValue());
		assertEquals("nameOverridden", definition.getName());
		assertEquals("categoryIdOverridden", definition.getCategoryId());
		assertTrue(definition.getDescription().startsWith("descriptionOverridden"));
		assertTrue(definition.isOverridden());
		engine.dispose();
	}

	@Test
	void testColorDefinitionWhenDefinitionStylesheetNotFound() {
		//given
		CSSEngine engine = createEngine("ColorDefinition#ACTIVE_HYPERLINK_COLOR{color: green}", display);
		ColorDefinition definition = colorDefinition("color definition uniqueId without matching stylesheet",
				"name", "categoryId", "description");

		assertEquals(new RGB(0, 0, 0), definition.getValue());
		assertFalse(definition.isOverridden());

		//when
		engine.applyStyles(definition, true);

		//then
		assertEquals(new RGB(0, 0, 0), definition.getValue());
		assertFalse(definition.isOverridden());
		engine.dispose();
	}

	@Test
	void testWidgetWithColorDefinitionAsBackgroundColor() {
		//given
		registerColorProviderWith("ACTIVE_HYPERLINK_COLOR", new RGB(255, 0, 0));

		CSSEngine engine = createEngine("Label {background-color: '#ACTIVE_HYPERLINK_COLOR'}", display);

		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		Label label = new Label(shell, SWT.NONE);
		label.setText("Some label text");

		//when
		engine.applyStyles(label, true);

		//then
		assertEquals(new RGB(255, 0, 0), label.getBackground().getRGB());

		engine.dispose();
		shell.dispose();
	}

	@Test
	void testUnset() {
		CSSEngine engine = createEngine("Button {background-color: unset;}", display);

		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		Button button = new Button(shell, SWT.NONE);
		Color red = display.getSystemColor(SWT.COLOR_RED);
		button.setBackground(red);

		// when
		engine.applyStyles(button, true);

		// then

		/*
		 * button still returns a non-null background (inherited or default background)
		 */
		assertNotEquals(red.getRGB(), button.getBackground().getRGB());

		engine.dispose();
		shell.dispose();
	}

	@Test
	void testSetColorDefinitionWithSystemColor() {
		// given
		CSSEngine engine = createEngine("ColorDefinition#ACTIVE_HYPERLINK_COLOR{color: '#COLOR-LIST-SELECTION'}",
				display);
		ColorDefinition definition = colorDefinition("ACTIVE_HYPERLINK_COLOR", "name", "categoryId", "description");

		assertEquals(new RGB(0, 0, 0), definition.getValue());
		assertFalse(definition.isOverridden());

		// when
		engine.applyStyles(definition, true);

		// then
		assertEquals(display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB(), definition.getValue());
		assertTrue(definition.isOverridden());
		engine.dispose();
	}

	private ColorDefinition colorDefinition(String uniqueId, String name,
			String categoryId, String description) {
		return new ColorDefinition(name, uniqueId, "defaultsTo", "black",
				categoryId, true, description, "pluginId");
	}

	private void registerColorProviderWith(final String symbolicName, final RGB rgb) {
		IColorAndFontProvider provider = mock(IColorAndFontProvider.class);
		doReturn(rgb).when(provider).getColor(symbolicName);
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("service.ranking", "1000");

		FrameworkUtil.getBundle(getClass()).getBundleContext().registerService(IColorAndFontProvider.class, provider,
				properties);
	}
}
