/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.themes.FontDefinition;
import org.junit.Test;

public class FontDefinitionTest extends CSSSWTTestCase {



	@Test
	public void testFontDefinition() {
		//given
		engine = createEngine(
				"FontDefinition#org-eclipse-jface-bannerfont {font-family: 'Times';font-size: 12;font-style: italic;}",
				display);
		FontDefinition definition = fontDefinition("org.eclipse.jface.bannerfont", "name", "categoryId","description");

		assertNull(definition.getValue());
		assertFalse(definition.isOverridden());

		//when
		engine.applyStyles(definition, true);

		//then
		assertNotNull(definition.getValue());
		assertEquals("Times", definition.getValue()[0].getName());
		assertEquals(12, definition.getValue()[0].getHeight());
		assertEquals(SWT.ITALIC, definition.getValue()[0].getStyle());
		assertEquals("categoryId", definition.getCategoryId());
		assertEquals("name", definition.getName());
		assertTrue(definition.getDescription().startsWith("description"));
		assertTrue(definition.isOverridden());
	}

	@Test
	public void testFontDefinitionWhenNameCategoryIdAndDescriptionOverridden() {
		// given
		engine = createEngine(
				"FontDefinition#org-eclipse-jface-bannerfont {font-family: 'Times';font-size: 12;font-style: italic;"
						+
						" label:'nameOverridden'; category: '#categoryIdOverridden'; description: 'descriptionOverridden'}", display);
		FontDefinition definition = fontDefinition("org.eclipse.jface.bannerfont", "name", "categoryId", "description");

		assertNull(definition.getValue());
		assertFalse(definition.isOverridden());

		// when
		engine.applyStyles(definition, true);

		// then
		assertNotNull(definition.getValue());
		assertEquals("Times", definition.getValue()[0].getName());
		assertEquals(12, definition.getValue()[0].getHeight());
		assertEquals(SWT.ITALIC, definition.getValue()[0].getStyle());
		assertEquals("categoryIdOverridden", definition.getCategoryId());
		assertEquals("nameOverridden", definition.getName());
		assertTrue(definition.getDescription().startsWith("descriptionOverridden"));
		assertTrue(definition.isOverridden());
	}

	@Test
	public void testFontDefinitionWhenDefinitionStylesheetNotFound() {
		//given
		engine = createEngine(
				"FontDefinition#org-eclipse-jface-bannerfont {font-family: 'Times';font-size: 12;font-style: italic;}",
				display);
		FontDefinition definition = fontDefinition("font definition uniqueId without matching stylesheet", "name", "categoryId", "description");

		assertNull(definition.getValue());
		assertFalse(definition.isOverridden());

		//when
		engine.applyStyles(definition, true);

		//then
		assertNull(definition.getValue());
		assertFalse(definition.isOverridden());
	}

	@Test
	public void testWidgetWithFontDefinitionAsFontFamily() {
		//given
		registerFontProviderWith("org.eclipse.jface.bannerfont", new FontData("Times", 12, SWT.ITALIC));

		engine = createEngine("Label {font-family: '#org-eclipse-jface-bannerfont'}", display);

		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		Label label = new Label(shell, SWT.NONE);
		Font font = new Font(display, "Arial", 9, SWT.NORMAL);
		label.setFont(font);
		label.setText("Some label text");


		//when
		engine.applyStyles(label, true);


		//then
		assertEquals("Times", label.getFont().getFontData()[0].getName());
		assertEquals(12, label.getFont().getFontData()[0].getHeight());
		assertEquals(SWT.ITALIC, label.getFont().getFontData()[0].getStyle());

		shell.dispose();
		font.dispose();
	}

	private FontDefinition fontDefinition(String uniqueId, String name,
			String categoryId, String description) {
		return new FontDefinition(new FontDefinition(name, uniqueId,
				"defaultsId", "value", categoryId, true, description),
				new FontData[] {new FontData("Arial", 10, SWT.NORMAL)});
	}

	private void registerFontProviderWith(final String symbolicName, final FontData fontData) {
		try {
			new CSSActivator() {
				@Override
				public IColorAndFontProvider getColorAndFontProvider() {
					IColorAndFontProvider provider = mock(IColorAndFontProvider.class);
					doReturn(new FontData[] { fontData }).when(provider).getFont(symbolicName);
					return provider;
				};
			}.start(null);
		} catch (Exception e) {
			fail("CssActivator start failed");
		}
	}
}
