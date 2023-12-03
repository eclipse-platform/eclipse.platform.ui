/*******************************************************************************
 * Copyright (c) 2016 Obeo and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Axel Richard <axel.richard@obeo.fr> - Bug 392457
 ******************************************************************************/
package org.eclipse.ui.tests.menus;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.11
 */
@RunWith(JUnit4.class)
public class DynamicToolbarTest extends MenuTestCase {

	public DynamicToolbarTest() {
		super(DynamicToolbarTest.class.getSimpleName());
	}

	@Test
	public void testDynamicMenu() throws Exception {
		ToolBarManager manager = new ToolBarManager();
		try {
			menuService.populateContributionManager(manager, "toolbar:org.eclipse.ui.trim.status");
			IContributionItem[] contributionItems = manager.getItems();
			assertEquals(1, contributionItems.length);
			assertEquals(contributionItems[0].getId(), "org.eclipse.ui.tests.dynamicToolbarContribution");

		} finally {
			menuService.releaseContributions(manager);
		}

	}

	public static class MyStatusBar extends WorkbenchWindowControlContribution {

		protected Composite fTopControl = null;
		private ToolBar toolBar;
		private ToolItem toolItem;

		@Override
		protected Control createControl(Composite parent) {
			GridLayout layout = new GridLayout(1, false);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			fTopControl = new Composite(parent, SWT.NONE);
			fTopControl.setLayout(layout);
			toolBar = new ToolBar(fTopControl, SWT.HORIZONTAL);
			toolItem = new ToolItem(toolBar, SWT.PUSH);
			toolItem.setText("StatusItem");
			toolBar.pack();
			return fTopControl;
		}
	}
}
