/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.menus;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.tests.api.ListView;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.eclipse.ui.tests.harness.util.UITestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests that opening a context menu fills its dynamic items a fixed number of
 * times, independent of the number of menu contributions (bug 467000).
 */
@ExtendWith(CloseTestWindowsExtension.class)
public class PopupMenuDynamicFillTest {

	private static final String VIEW_ID = "org.eclipse.ui.tests.internal.ActionExpressionTest";

	private static class CountingDynamicItem extends CompoundContributionItem {
		int fills;

		@Override
		protected IContributionItem[] getContributionItems() {
			fills++;
			return new IContributionItem[] { new ActionContributionItem(new Action("dynamic " + fills) {
			}) };
		}
	}

	@Test
	public void testDynamicFillsDoNotGrowWithContributions() throws Exception {
		IWorkbenchWindow window = UITestUtil.openTestWindow();
		assertEquals(fillsPerOpening(window, 1), fillsPerOpening(window, 20));
	}

	private int fillsPerOpening(IWorkbenchWindow window, int contributions) throws PartInitException {
		IWorkbenchPage page = window.getActivePage();
		IMenuService menuService = window.getService(IMenuService.class);
		CountingDynamicItem dynamic = new CountingDynamicItem();
		List<AbstractContributionFactory> factories = new ArrayList<>();
		factories.add(factory(dynamic));
		for (int i = 0; i < contributions; i++) {
			factories.add(factory(new ActionContributionItem(new Action("static " + i) {
			})));
		}
		factories.forEach(menuService::addContributionFactory);
		try {
			ListView view = (ListView) page.showView(VIEW_ID);
			processEvents();
			Menu menu = view.getMenuManager().getMenu();
			int before = dynamic.fills;
			menu.notifyListeners(SWT.Show, new Event());
			processEvents();
			int fills = dynamic.fills - before;
			menu.notifyListeners(SWT.Hide, new Event());
			processEvents();
			page.hideView(view);
			return fills;
		} finally {
			factories.forEach(menuService::removeContributionFactory);
		}
	}

	private static AbstractContributionFactory factory(IContributionItem item) {
		return new AbstractContributionFactory("popup:" + VIEW_ID, "org.eclipse.ui.tests") {
			@Override
			public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
				additions.addContributionItem(item, null);
			}
		};
	}
}
