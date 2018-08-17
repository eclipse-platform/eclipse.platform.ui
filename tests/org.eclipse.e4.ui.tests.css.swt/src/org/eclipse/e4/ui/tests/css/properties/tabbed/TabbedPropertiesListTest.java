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

import org.eclipse.e4.ui.tests.css.swt.CSSSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.junit.Test;

public class TabbedPropertiesListTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	private TabbedPropertySheetWidgetFactory factory;
	private Shell shell;


	private TabbedPropertyList createTabbedPropertiesList(String stylesheet) {
		String s;
		if (stylesheet == null) {
			s = "TabbedPropertyList { swt-tabAreaBackground-color: #FF0000; swt-tabBackground-color: #FF0000; swt-tabNormalShadow-color: #FF0000; swt-tabDarkShadow-color: #FF0000; color: #FF0000;}";
		} else {
			s = stylesheet;
		}
		engine = createEngine(s, display);

		shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite compositeToTest = new Composite(shell, SWT.NONE);
		compositeToTest.setLayout(new FillLayout());

		factory = new TabbedPropertySheetWidgetFactory();
		TabbedPropertyList list = new TabbedPropertyList(compositeToTest, factory);

		shell.pack();
		return list;
	}

	@Test
	public void colorsAreStyled() {
		TabbedPropertyList list = createTabbedPropertiesList(null);

		engine.applyStyles(shell, true);

		assertEquals(RED, list.getListBackgroundColor().getRGB());
		assertEquals(RED, list.getWidgetBackgroundColor().getRGB());
		assertEquals(RED, list.getWidgetNormalShadowColor().getRGB());
		assertEquals(RED, list.getWidgetDarkShadowColor().getRGB());
		assertEquals(RED, list.getWidgetForegroundColor().getRGB());
	}

	@Test
	public void colorsAreStyledAndReset() {
		TabbedPropertyList list = createTabbedPropertiesList(null);

		RGB colorListBackgroundBeforeStyling = list.getListBackgroundColor().getRGB();
		RGB colorWidgetBackgroundBeforStyling = list.getWidgetBackgroundColor().getRGB();
		RGB colorWidgetNormalShadowBeforStyling = list.getWidgetNormalShadowColor().getRGB();
		RGB colorWidgetDarkShadowBeforStyling = list.getWidgetDarkShadowColor().getRGB();
		RGB colorWidgetForegroundBeforStyling = list.getWidgetForegroundColor().getRGB();

		engine.applyStyles(shell, true);
		engine.reset();
		assertEquals(colorListBackgroundBeforeStyling, list.getListBackgroundColor().getRGB());
		assertEquals(colorWidgetBackgroundBeforStyling, list.getWidgetBackgroundColor().getRGB());
		assertEquals(colorWidgetNormalShadowBeforStyling, list.getWidgetNormalShadowColor().getRGB());
		assertEquals(colorWidgetDarkShadowBeforStyling, list.getWidgetDarkShadowColor().getRGB());
		assertEquals(colorWidgetForegroundBeforStyling, list.getWidgetForegroundColor().getRGB());
	}

	@Test
	public void colorsAreNotChangedWhenNoStyleGivenInCss() {
		TabbedPropertyList list = createTabbedPropertiesList(
				"SomeOtherWidget { listBackground-color: #FF0000; widgetBackground-color: #FF0000; widgetNormalShadow-color: #FF0000; widgetDarkShadow-color: #FF0000; widgetForeground-color: #FF0000;}");

		RGB colorListBackgroundBeforeStyling = list.getListBackgroundColor().getRGB();
		RGB colorWidgetBackgroundBeforStyling = list.getWidgetBackgroundColor().getRGB();
		RGB colorWidgetNormalShadowBeforStyling = list.getWidgetNormalShadowColor().getRGB();
		RGB colorWidgetDarkShadowBeforStyling = list.getWidgetDarkShadowColor().getRGB();
		RGB colorWidgetForegroundBeforStyling = list.getWidgetForegroundColor().getRGB();

		engine.applyStyles(shell, true);
		assertEquals(colorListBackgroundBeforeStyling, list.getListBackgroundColor().getRGB());
		assertEquals(colorWidgetBackgroundBeforStyling, list.getWidgetBackgroundColor().getRGB());
		assertEquals(colorWidgetNormalShadowBeforStyling, list.getWidgetNormalShadowColor().getRGB());
		assertEquals(colorWidgetDarkShadowBeforStyling, list.getWidgetDarkShadowColor().getRGB());
		assertEquals(colorWidgetForegroundBeforStyling, list.getWidgetForegroundColor().getRGB());
	}

}