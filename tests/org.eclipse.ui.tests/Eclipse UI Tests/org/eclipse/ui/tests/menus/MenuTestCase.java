/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
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

package org.eclipse.ui.tests.menus;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.e4.ui.workbench.renderers.swt.HandledContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.tests.api.workbenchpart.MenuContributionHarness;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Base class for tests concerning the 'org.eclipse.ui.menus'
 * extension point. Gains access to the various services that
 * are useful in writing the tests and defines the id of the
 * URI that contains a 'known' structure. If the XML describing
 * the structure is changed then the static tables that the
 * tests use to determine 'success' have to be verified and
 * updated if necessary.
 *
 * @since 3.3
 */
public class MenuTestCase extends UITestCase {

	protected static final String TEST_CONTRIBUTIONS_CACHE_ID = "org.eclipse.ui.tests.IfYouChangeMe.FixTheTests";

	public MenuTestCase(String testName) {
		super(testName);
	}

	protected IContextService contextService;
	protected IMenuService menuService;
	protected IWorkbenchWindow window;
	protected IContextActivation activeContext;

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		window = openTestWindow();
		contextService = window
				.getService(IContextService.class);
		Context context1 = contextService
				.getContext(MenuContributionHarness.CONTEXT_TEST1_ID);
		if (!context1.isDefined()) {
			context1.define("Menu Test 1", "Menu test 1",
					IContextService.CONTEXT_ID_DIALOG_AND_WINDOW);
		}

		menuService = window.getService(IMenuService.class);
	}

	@Override
	protected void doTearDown() throws Exception {
		if (activeContext != null) {
			contextService.deactivateContext(activeContext);
			activeContext = null;
		}
		contextService = null;
		menuService = null;
		window = null;

		super.doTearDown();
	}

	protected static int ALL_OK = -1;
	protected static int checkContribIds(IContributionItem[] items, String[] ids) {
		// Test cases should check this independently so they can issue the
		// correct error (i.e. "Not enough items...wanted 6 got 5") but for
		// safety's sake...
		if (items.length != ids.length) {
			return 0;
		}

		for (int i = 0; i < ids.length; i++) {
			// HACK!! Some uds are based on intances
			if (ids[i] == null) {
				continue;
			}

			if (!ids[i].equals(items[i].getId())) {
				return i;
			}
		}
		return ALL_OK;
	}

	protected static int checkContribClasses(IContributionItem[] items, Class<?>[] classes) {
		// Test cases should check this independently so they can issue the
		// correct error (i.e. "Not enough items...wanted 6 got 5") but for
		// safety's sake...
		if (items.length != classes.length) {
			return 0;
		}

		for (int i = 0; i < classes.length; i++) {
			// HACK!! cant find anonyous classes
			if (classes[i] == null) {
				continue;
			}

			// minor upgrade ... if the item is an instanceof the class we're
			// good
			// this handles the case where the item is a subclass of
			// CompoundContributionItem
			if (!classes[i].isInstance(items[i])
					&& !(classes[i] == CommandContributionItem.class && HandledContributionItem.class
							.isInstance(items[i]))) {
				return i;
			}
		}
		return ALL_OK;
	}

	protected static int checkMenuItemLabels(MenuItem[] menuItems,
			String[] expectedLabels) {
		// Test cases should check this independently so they can issue the
		// correct error (i.e. "Not enough items...wanted 6 got 5") but for
		// safety's sake...
		if (menuItems.length != expectedLabels.length) {
			return 0;
		}

		for (int i = 0; i < expectedLabels.length; i++) {
			if (!expectedLabels[i].equals(menuItems[i].getText())) {
				return i;
			}
		}
		return ALL_OK;
	}

	protected static void printIds(IContributionItem[] items) {
		System.out.println("String[] expectedIds = {");
		for (int i = 0; i < items.length; i++) {
			String comma = (i < (items.length-1)) ? "," : "";
			System.out.println("\t\"" + items[i].getId() + "\"" + comma);
		}
		System.out.println("};");
	}

	protected static void printClasses(IContributionItem[] items) {
		System.out.println("Class[] expectedClasses = {");
		for (int i = 0; i < items.length; i++) {
			String comma = (i < (items.length-1)) ? "," : "";
			System.out.println("\t" + items[i].getClass().getName() + ".class" + comma);
		}
		System.out.println("};");
	}

	protected static void printMenuItemLabels(MenuItem[] items) {
		System.out.println("String[] expectedMenuItemLabels = {");
		for (int i = 0; i < items.length; i++) {
			String comma = (i < (items.length-1)) ? "," : "";
			System.out.println("\t\"" + items[i].getText() + "\"" + comma);
		}
		System.out.println("};");
	}
}
