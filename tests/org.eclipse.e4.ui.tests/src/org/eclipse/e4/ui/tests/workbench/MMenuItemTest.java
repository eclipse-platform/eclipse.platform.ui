/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MMenuItem;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.workbench.swt.internal.E4Application;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.renderers.swt.TrimmedPartLayout;
import org.eclipse.swt.widgets.Control;
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

	private void testMToolItem_Text(String before, String beforeExpected,
			String after, String afterExpected) {
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		MMenuItem menuItem = MApplicationFactory.eINSTANCE.createMenuItem();

		menuItem.setLabel(before);

		window.setMainMenu(menu);
		menu.getChildren().add(menuItem);

		wb = new E4Workbench(window, appContext);
		wb.createAndRunUI(window);

		Object widget = menuItem.getWidget();
		assertNotNull(widget);
		assertTrue(widget instanceof MenuItem);

		MenuItem toolItemWidget = (MenuItem) widget;

		assertEquals(beforeExpected, toolItemWidget.getText());

		menuItem.setLabel(after);

		assertEquals(afterExpected, toolItemWidget.getText());
	}

	public void testMToolItem_Text_NullNull() {
		testMToolItem_Text(null, "", null, "");
	}

	public void testMToolItem_Text_NullEmpty() {
		testMToolItem_Text(null, "", "", "");
	}

	public void testMToolItem_Text_NullString() {
		testMToolItem_Text(null, "", "label", "label");
	}

	public void testMToolItem_Text_EmptyNull() {
		testMToolItem_Text("", "", null, "");
	}

	public void testMToolItem_Text_EmptyEmpty() {
		testMToolItem_Text("", "", "", "");
	}

	public void testMToolItem_Text_EmptyString() {
		testMToolItem_Text("", "", "label", "label");
	}

	public void testMToolItem_Text_StringNull() {
		testMToolItem_Text("label", "label", null, "");
	}

	public void testMToolItem_Text_StringEmpty() {
		testMToolItem_Text("label", "label", "", "");
	}

	public void testMToolItem_Text_StringStringUnchanged() {
		testMToolItem_Text("label", "label", "label", "label");
	}

	public void testMToolItem_Text_StringStringChanged() {
		testMToolItem_Text("label", "label", "label2", "label2");
	}
}
