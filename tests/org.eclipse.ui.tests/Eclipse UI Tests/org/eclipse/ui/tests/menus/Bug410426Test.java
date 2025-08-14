/*******************************************************************************
 * Copyright (c) 2014, 2017 Obeo and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.menus;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.equinox.log.LogFilter;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

/**
 * @author Maxime Porhel
 */
public class Bug410426Test {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	@Test
	public void testToolbarContributionFromFactoryVisibility() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IMenuService menus = window.getService(IMenuService.class);
		ToolBarManager manager = new ToolBarManager();

		try {
			// populate contribution
			populateTestToolbar(menus, manager);

			// check the contributions and their visibility
			IContributionItem[] items = manager.getItems();
			assertEquals(6, items.length);
			checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_ITEM_WITHOUT_VISIBLE_WHEN, items, true);
			checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_ITEM_WITH_ALWAYS_TRUE_VISIBLE_WHEN, items, true);
			checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_ITEM_WITH_ALWAYS_FALSE_VISIBLE_WHEN, items, false);

			checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_MENU_MANAGER_WITHOUT_VISIBLE_WHEN, items, true);
			checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_MENU_MANAGER_WITH_ALWAYS_TRUE_VISIBLE_WHEN, items, true);
			checkItem(DeclaredProgrammaticFactoryForToolbarVisibilityTest.TEST_ITEM_WITH_ALWAYS_FALSE_VISIBLE_WHEN, items, false);

			// now get the tool items
			ToolBar toolBar = manager.createControl(window.getShell());
			manager.update(true);
			ToolItem[] toolItems = toolBar.getItems();
			assertEquals("Only four tool items should be created as there are four visible contributions on the six contributions:", 4, toolItems.length);
		} finally {
			menus.releaseContributions(manager);
		}
	}

	private void populateTestToolbar(IMenuService menus, ToolBarManager manager) {
		menus.populateContributionManager(manager, "toolbar:org.eclipse.ui.tests.toolbarContributionFromFactoryVisibilityTest");
	}

	private void checkItem(String id, IContributionItem[] items, boolean expectedVisibility) {
		IContributionItem item = getItemWithId(id, items);

		assertNotNull(item);
		assertEquals("The contribution item with id '" + id + "' has not the expected vibility:", expectedVisibility, item.isVisible());
	}

	private IContributionItem getItemWithId(String id, IContributionItem[] items) {
		for (IContributionItem item : items) {
			if (id.equals(item.getId())) {
				return item;
			}
		}
		return null;
	}

	@Test
	public void testNoClassCastExceptionForMenuManagerToolbarContribution() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		IMenuService menus = window.getService(IMenuService.class);
		ToolBarManager manager = new ToolBarManager();

		//Add a log listener to detect the corrected ClassCastException in bug 410426.
		final List<ClassCastException> cces = new ArrayList<>();
		ExtendedLogReaderService log = window
				.getService(ExtendedLogReaderService.class);
		LogListener logListener = entry -> {
			if (entry.getLevel() == LogService.LOG_ERROR && entry.getException() instanceof ClassCastException cce
					&& entry.getException().getMessage()
							.contains("MenuManager cannot be cast to org.eclipse.jface.action.ContributionItem")) { // $NON-NLS-N$
				cces.add(cce);
			}
		};
		LogFilter logFilter = (bundle, loggerName, logLevel) -> logLevel == LogService.LOG_ERROR && loggerName == null
				&& "org.eclipse.equinox.event".equals(bundle.getSymbolicName());
		log.addLogListener(logListener, logFilter);

		try {
			populateTestToolbar(menus, manager);

			assertTrue("We should not get these 'MenuManager cannot be cast to org.eclipse.jface.action.ContributionItem' ClassCastException.", cces.isEmpty());

			// check the contributions count.
			IContributionItem[] items = manager.getItems();
			assertEquals(6, items.length);
		} finally {
			menus.releaseContributions(manager);
			log.removeLogListener(logListener);
			cces.clear();
		}
	}
}
