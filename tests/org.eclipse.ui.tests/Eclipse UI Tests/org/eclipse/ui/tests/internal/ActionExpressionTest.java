/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.internal;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.api.ListElement;
import org.eclipse.ui.tests.api.ListView;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;

/**
 * This class contains tests for popup menu enablement
 */
public abstract class ActionExpressionTest extends UITestCase {
	protected IWorkbenchWindow fWindow;

	protected IWorkbenchPage fPage;

	protected String VIEW_ID = "org.eclipse.ui.tests.internal.ActionExpressionTest";

	ListElement red = new ListElement("red");

	ListElement blue = new ListElement("blue");

	ListElement redTrue = new ListElement("red", true);

	public ActionExpressionTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		fWindow = openTestWindow();
		fPage = fWindow.getActivePage();
	}

	@Test
	public void testAllAction() throws Throwable {
		// Setup.
		ListView view = showListView();
		MenuManager mgr = getActionMenuManager(view);

		// Test null selection.
		selectAndUpdateMenu(view, null, mgr);
		testAction(mgr, "allAction_v2", false);

		// Test red selection.
		selectAndUpdateMenu(view, red, mgr);
		testAction(mgr, "allAction_v2", true);

		// Test blue selection.
		selectAndUpdateMenu(view, blue, mgr);
		testAction(mgr, "allAction_v2", true);

		// Test red + true selection.
		selectAndUpdateMenu(view, redTrue, mgr);
		testAction(mgr, "allAction_v2", true);
	}

	@Test
	public void testRedAction() throws Throwable {
		// Setup.
		ListView view = showListView();
		MenuManager mgr = getActionMenuManager(view);

		// Test null selection.
		selectAndUpdateMenu(view, null, mgr);
		testAction(mgr, "redAction_v2", false);

		// Test red selection.
		selectAndUpdateMenu(view, red, mgr);
		testAction(mgr, "redAction_v2", true);

		// Test blue selection.
		selectAndUpdateMenu(view, blue, mgr);
		testAction(mgr, "redAction_v2", false);

		// Test red + true selection.
		selectAndUpdateMenu(view, redTrue, mgr);
		testAction(mgr, "redAction_v2", true);
	}

	@Test
	public void testNotRedAction() throws Throwable {
		// Setup.
		ListView view = showListView();
		MenuManager mgr = getActionMenuManager(view);

		// Test null selection.
		selectAndUpdateMenu(view, null, mgr);
		testAction(mgr, "notRedAction_v2", false);

		// Test red selection.
		selectAndUpdateMenu(view, red, mgr);
		testAction(mgr, "notRedAction_v2", false);

		// Test blue selection.
		selectAndUpdateMenu(view, blue, mgr);
		testAction(mgr, "notRedAction_v2", true);

		// Test red + true selection.
		selectAndUpdateMenu(view, redTrue, mgr);
		testAction(mgr, "notRedAction_v2", false);
	}

	@Test
	public void testTrueAction() throws Throwable {
		// Setup.
		ListView view = showListView();
		MenuManager mgr = getActionMenuManager(view);

		// Test null selection.
		selectAndUpdateMenu(view, null, mgr);
		testAction(mgr, "trueAction_v2", false);

		// Test red selection.
		selectAndUpdateMenu(view, red, mgr);
		testAction(mgr, "trueAction_v2", false);

		// Test blue selection.
		selectAndUpdateMenu(view, blue, mgr);
		testAction(mgr, "trueAction_v2", false);

		// Test red + true selection.
		selectAndUpdateMenu(view, redTrue, mgr);
		testAction(mgr, "trueAction_v2", true);
	}

	@Test
	public void testRedOrBlueAction() throws Throwable {
		// Setup.
		ListView view = showListView();
		MenuManager mgr = getActionMenuManager(view);

		// Test null selection.
		selectAndUpdateMenu(view, null, mgr);
		testAction(mgr, "redOrBlueAction_v2", false);

		// Test red selection.
		selectAndUpdateMenu(view, red, mgr);
		testAction(mgr, "redOrBlueAction_v2", true);

		// Test blue selection.
		selectAndUpdateMenu(view, blue, mgr);
		testAction(mgr, "redOrBlueAction_v2", true);

		// Test red + true selection.
		selectAndUpdateMenu(view, redTrue, mgr);
		testAction(mgr, "redOrBlueAction_v2", true);
	}

	@Test
	public void testRedAndTrueAction() throws Throwable {
		// Setup.
		ListView view = showListView();
		MenuManager mgr = getActionMenuManager(view);

		// Test null selection.
		selectAndUpdateMenu(view, null, mgr);
		testAction(mgr, "redAndTrueAction_v2", false);

		// Test red selection.
		selectAndUpdateMenu(view, red, mgr);
		testAction(mgr, "redAndTrueAction_v2", false);

		// Test blue selection.
		selectAndUpdateMenu(view, blue, mgr);
		testAction(mgr, "redAndTrueAction_v2", false);

		// Test red + true selection.
		selectAndUpdateMenu(view, redTrue, mgr);
		testAction(mgr, "redAndTrueAction_v2", true);
	}

	@Test
	public void testPluginStateActions() throws Throwable {
		// Setup.
		ListView view = showListView();
		MenuManager mgr = getActionMenuManager(view);

		// Open the menu and test actions.
		selectAndUpdateMenu(view, red, mgr);
		testAction(mgr, "pluginNotInstalledAction_v2", false);
		testAction(mgr, "pluginInstalledAction_v2", true);
		testAction(mgr, "pluginNotActivatedAction_v2", false);
		testAction(mgr, "pluginActivatedAction_v2", true);
	}

	@Test
	public void testSystemPropertyAction() throws Throwable {
		// Setup.
		ListView view = showListView();
		MenuManager mgr = getActionMenuManager(view);

		// Clear the system property, refresh the menu,
		// and test the action.
		System.setProperty("ActionExpressionVar", "");
		selectAndUpdateMenu(view, red, mgr);
		testAction(mgr, "systemPropertyAction_v2", false);

		// Set the system property, refresh the menu,
		// and test the action.
		System.setProperty("ActionExpressionVar", "bubba");
		selectAndUpdateMenu(view, blue, mgr);
		testAction(mgr, "systemPropertyAction_v2", true);
	}

	/**
	 * Creates the list view.
	 */
	protected ListView showListView() throws Throwable {
		ListView view = (ListView) (fPage.showView(VIEW_ID));
		red = new ListElement("red");
		blue = new ListElement("blue");
		redTrue = new ListElement("red", true);
		view.addElement(red);
		view.addElement(blue);
		view.addElement(redTrue);
		return view;
	}

	/**
	 * Select an object and simulate show.
	 */
	protected void selectAndUpdateMenu(ListView view, ListElement element,
			MenuManager mgr) throws Throwable {
		view.selectElement(element);
		mgr.getMenu().notifyListeners(SWT.Show, new Event());
	}

	/**
	 * Returns the menu manager containing the actions.
	 */
	protected abstract MenuManager getActionMenuManager(ListView view)
			throws Throwable;

	/**
	 * Tests the enablement / visibility of an action.
	 */
	protected abstract void testAction(MenuManager mgr, String action,
			boolean expected) throws Throwable;
}
