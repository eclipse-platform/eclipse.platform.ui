/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.themes.FontDefinition;

@SuppressWarnings("restriction")
public class FontDefinitionTest extends CSSSWTTestCase {
	private Display display;

	@Override
	protected void setUp() throws Exception {
		display = Display.getDefault();
	}

	public void testFontDefinition() throws Exception {
		//given
		CSSEngine engine = createEngine("FontDefinition#org-eclipse-jface-bannerfont {font-family: 'Times';font-size: 12;font-style: italic;}", display);
		FontDefinition definition = fontDefinition("org.eclipse.jface.bannerfont");

		assertNull(definition.getValue());
		assertFalse(definition.isOverridden());

		//when
		engine.applyStyles(definition, true);

		//then
		assertNotNull(definition.getValue());
		assertEquals("Times", definition.getValue()[0].getName());
		assertEquals(12, definition.getValue()[0].getHeight());
		assertEquals(SWT.ITALIC, definition.getValue()[0].getStyle());
		assertTrue(definition.isOverridden());
	}

	public void testFontDefinitionWhenDefinitionStylesheetNotFound() throws Exception{
		//given
		CSSEngine engine = createEngine("FontDefinition#org-eclipse-jface-bannerfont {font-family: 'Times';font-size: 12;font-style: italic;}", display);
		FontDefinition definition = fontDefinition("font definition uniqueId without matching stylesheet");

		assertNull(definition.getValue());
		assertFalse(definition.isOverridden());

		//when
		engine.applyStyles(definition, true);

		//then
		assertNull(definition.getValue());
		assertFalse(definition.isOverridden());
	}

	public void testWidgetWithFontDefinitionAsFontFamily() throws Exception {
		//given
		registerFontProviderWith("org.eclipse.jface.bannerfont", new FontData("Times", 12, SWT.ITALIC));

		CSSEngine engine = createEngine(
				"Label {font-family: '#org-eclipse-jface-bannerfont'}",
				display);

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

	private FontDefinition fontDefinition(String uniqueId) {
		return new FontDefinition(new FontDefinition("fontName", uniqueId, "defaultsId",
				"value", "categoryId", true, "fontDescription"),
				new FontData[] {new FontData("Arial", 10, SWT.NORMAL)});
	}

	private void registerFontProviderWith(final String expectedSymbolicName, final FontData fontData) throws Exception {
		new CSSActivator() {
			@Override
			public IColorAndFontProvider getColorAndFontProvider() {
				return new IColorAndFontProvider() {
					public FontData[] getFont(String symbolicName) {
						if (expectedSymbolicName.equals(symbolicName)) {
							return new FontData[]{fontData};
						}
						return null;
					}
					public RGB getColor(String symbolicName) {
						return null;
					}
				};
			};
		}.start(null);
	}
}
