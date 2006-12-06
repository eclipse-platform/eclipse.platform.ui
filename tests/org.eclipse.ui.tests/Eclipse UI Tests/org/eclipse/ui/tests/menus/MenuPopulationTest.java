/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.menus;

import org.eclipse.core.commands.contexts.Context;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.tests.api.workbenchpart.MenuContributionHarness;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 * 
 */
public class MenuPopulationTest extends UITestCase {

	/**
	 * 
	 */
	private static final String MENU_VIEW_ID = "org.eclipse.ui.tests.api.MenuTestHarness";

	/**
	 * @param testName
	 */
	public MenuPopulationTest(String testName) {
		super(testName);
	}

	private IContextService contextService;

	private IMenuService menuService;

	private IWorkbenchWindow window;

	private IContextActivation activeContext;

	public void testViewPopulation() throws Exception {
		MenuManager manager = new MenuManager(null, MENU_VIEW_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ MENU_VIEW_ID);
		IContributionItem[] items = manager.getItems();
		IContributionItem itemX1 = null;
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			if ("org.eclipse.ui.tests.menus.itemX1".equals(item.getId())) {
				itemX1 = item;
			}
		}

		assertFalse(itemX1.isVisible());

		activeContext = contextService
				.activateContext(MenuContributionHarness.CONTEXT_TEST1_ID);

		assertTrue(itemX1.isVisible());

		contextService.deactivateContext(activeContext);

		assertFalse(itemX1.isVisible());

		activeContext = contextService
				.activateContext(MenuContributionHarness.CONTEXT_TEST1_ID);

		assertTrue(itemX1.isVisible());

		menuService.releaseContributions(manager);
		manager.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();

		window = openTestWindow();
		contextService = (IContextService) window
				.getService(IContextService.class);
		Context context1 = contextService
				.getContext(MenuContributionHarness.CONTEXT_TEST1_ID);
		if (!context1.isDefined()) {
			context1.define("Menu Test 1", "Menu test 1",
					IContextService.CONTEXT_ID_DIALOG_AND_WINDOW);
		}

		menuService = (IMenuService) window.getService(IMenuService.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doTearDown()
	 */
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
}
