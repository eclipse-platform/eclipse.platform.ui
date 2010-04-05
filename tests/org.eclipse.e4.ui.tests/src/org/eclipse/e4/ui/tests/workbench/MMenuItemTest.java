/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.ui.model.application.ItemType;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class MMenuItemTest extends TestCase {
	protected IEclipseContext appContext;
	protected E4Workbench wb;

	@Override
	protected void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		appContext.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);
	}

	@Override
	protected void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}

		if (appContext instanceof IDisposable) {
			((IDisposable) appContext).dispose();
		}
	}

	protected Control[] getPresentationControls(Shell shell) {
		TrimmedPartLayout tpl = (TrimmedPartLayout) shell.getLayout();
		return tpl.clientArea.getChildren();
	}

	private void testMMenuItem_Text(String before, String beforeExpected,
			String after, String afterExpected) {
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		MMenuItem menuItem = MApplicationFactory.eINSTANCE.createMenuItem();

		menuItem.setLabel(before);

		window.setMainMenu(menu);
		menu.getChildren().add(menuItem);

		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		Object widget = menuItem.getWidget();
		assertNotNull(widget);
		assertTrue(widget instanceof MenuItem);

		MenuItem menuItemWidget = (MenuItem) widget;

		assertEquals(beforeExpected, menuItemWidget.getText());

		menuItem.setLabel(after);

		assertEquals(afterExpected, menuItemWidget.getText());
	}

	public void testMMenuItem_Text_NullNull() {
		testMMenuItem_Text(null, "", null, "");
	}

	public void testMMenuItem_Text_NullEmpty() {
		testMMenuItem_Text(null, "", "", "");
	}

	public void testMMenuItem_Text_NullString() {
		testMMenuItem_Text(null, "", "label", "label");
	}

	public void testMMenuItem_Text_EmptyNull() {
		testMMenuItem_Text("", "", null, "");
	}

	public void testMMenuItem_Text_EmptyEmpty() {
		testMMenuItem_Text("", "", "", "");
	}

	public void testMMenuItem_Text_EmptyString() {
		testMMenuItem_Text("", "", "label", "label");
	}

	public void testMMenuItem_Text_StringNull() {
		testMMenuItem_Text("label", "label", null, "");
	}

	public void testMMenuItem_Text_StringEmpty() {
		testMMenuItem_Text("label", "label", "", "");
	}

	public void testMMenuItem_Text_StringStringUnchanged() {
		testMMenuItem_Text("label", "label", "label", "label");
	}

	public void testMMenuItem_Text_StringStringChanged() {
		testMMenuItem_Text("label", "label", "label2", "label2");
	}

	public void testMMenuItem_RadioItems() {
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		MMenuItem menuItem1 = MApplicationFactory.eINSTANCE.createMenuItem();
		MMenuItem menuItem2 = MApplicationFactory.eINSTANCE.createMenuItem();

		menuItem1.setType(ItemType.RADIO);
		menuItem2.setType(ItemType.RADIO);

		menu.getChildren().add(menuItem1);
		menu.getChildren().add(menuItem2);
		window.setMainMenu(menu);

		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class.getName(), application);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		Object widget1 = menuItem1.getWidget();
		assertNotNull(widget1);
		assertTrue(widget1 instanceof MenuItem);

		Object widget2 = menuItem2.getWidget();
		assertNotNull(widget2);
		assertTrue(widget2 instanceof MenuItem);

		MenuItem menuItemWidget1 = (MenuItem) widget1;
		MenuItem menuItemWidget2 = (MenuItem) widget2;

		// test that 'clicking' on the item updates the model
		menuItemWidget1.setSelection(false);
		menuItemWidget2.setSelection(true);
		menuItemWidget1.notifyListeners(SWT.Selection, new Event());
		menuItemWidget2.notifyListeners(SWT.Selection, new Event());

		assertFalse(menuItem1.isSelected());
		assertTrue(menuItem2.isSelected());

		menuItemWidget2.setSelection(false);
		menuItemWidget1.setSelection(true);
		menuItemWidget2.notifyListeners(SWT.Selection, new Event());
		menuItemWidget1.notifyListeners(SWT.Selection, new Event());

		assertTrue(menuItem1.isSelected());
		assertFalse(menuItem2.isSelected());

		// Check that model changes are reflected in the items
		menuItem1.setSelected(false);
		assertFalse(menuItemWidget1.getSelection());
		menuItem2.setSelected(true);
		assertTrue(menuItemWidget2.getSelection());
	}
}
