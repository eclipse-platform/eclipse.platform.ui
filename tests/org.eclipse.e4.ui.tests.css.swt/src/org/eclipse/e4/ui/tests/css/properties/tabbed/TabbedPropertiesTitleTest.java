/*******************************************************************************
 * Copyright (c) 2017 SAP SE and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.properties.tabbed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.e4.ui.tests.css.swt.CSSSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyTitle;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.junit.Test;

public class TabbedPropertiesTitleTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	private TabbedPropertySheetWidgetFactory factory;
	private Shell shell;


	private TabbedPropertyTitle createTabbedPropertiesTitle(String stylesheet) {
		String s;
		if (stylesheet == null) {
			s = "TabbedPropertyTitle { swt-backgroundGradientStart-color: #FF0000; swt-backgroundGradientEnd-color: #FF0000; swt-backgroundBottomKeyline1-color: #FF0000; swt-backgroundBottomKeyline2-color: #FF0000;}";
		} else {
			s = stylesheet;
		}
		engine = createEngine(
				s,
				display);

		shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite compositeToTest = new Composite(shell, SWT.NONE);
		compositeToTest.setLayout(new FillLayout());

		factory = new TabbedPropertySheetWidgetFactory();
		TabbedPropertyTitle title = new TabbedPropertyTitle(compositeToTest, factory);

		shell.pack();
		return title;
	}

	private void assertColor(RGB expected, String actualKey){
		assertNotNull(factory.getColors().getColor(actualKey));
		assertEquals(expected, factory.getColors().getColor(actualKey).getRGB());
	}

	@Test
	public void titleBackgroundColorIsStyled() {
		createTabbedPropertiesTitle(null);

		engine.applyStyles(shell, true);

		assertColor(RED, IFormColors.H_GRADIENT_START);
		assertColor(RED, IFormColors.H_GRADIENT_END);
		assertColor(RED, IFormColors.H_BOTTOM_KEYLINE1);
		assertColor(RED, IFormColors.H_BOTTOM_KEYLINE2);
	}

	@Test
	public void titleBackgroundColorIsStyledAndReset() {
		createTabbedPropertiesTitle(null);

		RGB colorGradStartBeforStyling = factory.getColors().getColor(IFormColors.H_GRADIENT_START).getRGB();
		RGB colorGradEndBeforStyling = factory.getColors().getColor(IFormColors.H_GRADIENT_END).getRGB();
		RGB colorBottomKeylineOneBeforStyling = factory.getColors().getColor(IFormColors.H_BOTTOM_KEYLINE1).getRGB();
		RGB colorBottomKeylineTwoBeforStyling = factory.getColors().getColor(IFormColors.H_BOTTOM_KEYLINE2).getRGB();

		engine.applyStyles(shell, true);
		engine.reset();
		assertColor(colorGradStartBeforStyling, IFormColors.H_GRADIENT_START);
		assertColor(colorGradEndBeforStyling, IFormColors.H_GRADIENT_END);
		assertColor(colorBottomKeylineOneBeforStyling, IFormColors.H_BOTTOM_KEYLINE1);
		assertColor(colorBottomKeylineTwoBeforStyling, IFormColors.H_BOTTOM_KEYLINE2);
	}

	@Test
	public void colorsAreNotChangedWhenNoStyleGivenInCss() {
		createTabbedPropertiesTitle(
				"SomeOtherWidget { h-gradient-start-color: #FF0000; h-gradient-end-color: #FF0000; h-bottom-keyline-1-color: #FF0000; h-bottom-keyline-2-color: #FF0000;}");

		RGB colorGradStartBeforStyling = factory.getColors().getColor(IFormColors.H_GRADIENT_START).getRGB();
		RGB colorGradEndBeforStyling = factory.getColors().getColor(IFormColors.H_GRADIENT_END).getRGB();
		RGB colorBottomKeylineOneBeforStyling = factory.getColors().getColor(IFormColors.H_BOTTOM_KEYLINE1).getRGB();
		RGB colorBottomKeylineTwoBeforStyling = factory.getColors().getColor(IFormColors.H_BOTTOM_KEYLINE2).getRGB();

		engine.applyStyles(shell, true);
		assertColor(colorGradStartBeforStyling, IFormColors.H_GRADIENT_START);
		assertColor(colorGradEndBeforStyling, IFormColors.H_GRADIENT_END);
		assertColor(colorBottomKeylineOneBeforStyling, IFormColors.H_BOTTOM_KEYLINE1);
		assertColor(colorBottomKeylineTwoBeforStyling, IFormColors.H_BOTTOM_KEYLINE2);
	}

}