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

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.tests.api.workbenchpart.MenuContributionHarness;

/**
 * @since 3.3
 * 
 */
public class MenuPopulationTest extends MenuTestCase {

	/**
	 * @param testName
	 */
	public MenuPopulationTest(String testName) {
		super(testName);
	}

	public void testViewPopulation() throws Exception {
		MenuManager manager = new MenuManager(null, TEST_CONTRIBUTIONS_CACHE_ID);
		menuService.populateContributionManager(manager, "menu:"
				+ TEST_CONTRIBUTIONS_CACHE_ID);
		IContributionItem[] items = manager.getItems();
		IContributionItem itemX1 = null;
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			if ("MenuTest.ItemX1".equals(item.getId())) {
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
}
