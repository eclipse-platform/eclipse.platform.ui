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
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.themes.ColorDefinition;

@SuppressWarnings("restriction")
public class ColorDefinitionTest extends CSSSWTTestCase {
	private Display display;
	
	@Override
	protected void setUp() throws Exception {
		display = Display.getDefault();
	}
	
	public void testColorDefinition() throws Exception {
		//given
		CSSEngine engine = createEngine("ColorDefinition#ACTIVE_HYPERLINK_COLOR{color: green}", display);		
		ColorDefinition definition = colorDefinition("ACTIVE_HYPERLINK_COLOR");		
		
		assertEquals(new RGB(0, 0, 0), definition.getValue());
		assertFalse(definition.isOverridden());		
		
		//when
		engine.applyStyles(definition, true);
				
		//then
		assertEquals(new RGB(0, 128, 0), definition.getValue());
		assertTrue(definition.isOverridden());
	}
	
	public void testColorDefinitionWhenDefinitionStylesheetNotFound() throws Exception{
		//given
		CSSEngine engine = createEngine("ColorDefinition#ACTIVE_HYPERLINK_COLOR{color: green}", display);		
		ColorDefinition definition = colorDefinition("color definition uniqueId without matching stylesheet");		
		
		assertEquals(new RGB(0, 0, 0), definition.getValue());
		assertFalse(definition.isOverridden());	
				
		//when
		engine.applyStyles(definition, true);		
		
		//then
		assertEquals(new RGB(0, 0, 0), definition.getValue());
		assertFalse(definition.isOverridden());	
	}
	
	public void testWidgetWithColorDefinitionAsBackgroundColor() throws Exception {
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
		
		shell.dispose();		
	}
	
	private ColorDefinition colorDefinition(String uniqueId) {
		return new ColorDefinition("label", uniqueId, "defaultsTo", "black", "categoryId", true, 
				"description", "pluginId");		
	}
	
	private void registerColorProviderWith(final String expectedSymbolicName, final RGB expectedRgb) throws Exception {
		new CSSActivator() {
			@Override
			public IColorAndFontProvider getColorAndFontProvider() {
				return new IColorAndFontProvider() {			
					public FontData[] getFont(String symbolicName) {							
						return null;
					}					
					public RGB getColor(String symbolicName) {
						if (expectedSymbolicName.equals(symbolicName)) {
							return expectedRgb;
						}
						return null;
					}
				};
			};
		}.start(null);
	}
}
