/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.themes.ColorDefinition;
import org.junit.Test;

@SuppressWarnings("restriction")
public class ColorDefinitionTest extends CSSSWTTestCase {


	@Test
	public void testColorDefinition() {
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
	public void testColorDefinitionWhenNameCategoryIdAndDescriptionOverridden() {
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
	public void testColorDefinitionWhenDefinitionStylesheetNotFound() {
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
	public void testWidgetWithColorDefinitionAsBackgroundColor() {
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

	private ColorDefinition colorDefinition(String uniqueId, String name,
			String categoryId, String description) {
		return new ColorDefinition(name, uniqueId, "defaultsTo", "black",
				categoryId, true, description, "pluginId");
	}

	private void registerColorProviderWith(final String symbolicName, final RGB rgb) {
		try {
			new CSSActivator() {
				@Override
				public IColorAndFontProvider getColorAndFontProvider() {
					IColorAndFontProvider provider = mock(IColorAndFontProvider.class);
					doReturn(rgb).when(provider).getColor(symbolicName);
					return provider;
				};
			}.start(null);
		} catch (Exception e) {
			fail("Register color provider should not fail");
		}
	}
}
