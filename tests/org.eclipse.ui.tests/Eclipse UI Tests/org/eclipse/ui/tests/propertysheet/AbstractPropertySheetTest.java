/*******************************************************************************
 * Copyright (c) 2008, 2009 Versant Corp. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.tests.propertysheet;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.properties.PropertySheet;

/**
 * @since 3.4
 */
public abstract class AbstractPropertySheetTest extends UITestCase {

	private static final String PIN_PROPERTY_SHEET_ACTION_ID_PREFIX = "org.eclipse.ui.views.properties.PinPropertySheetAction";
	protected IWorkbenchPage activePage;
	protected PropertySheet propertySheet;

	public AbstractPropertySheetTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		IWorkbenchWindow workbenchWindow = openTestWindow();
		activePage = workbenchWindow.getActivePage();
	}

	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();
		activePage = null;
		propertySheet = null;
	}

	/**
	 * @return the count of PropertySheets
	 */
	protected int countPropertySheetViews() {
		int count = 0;
		IViewReference[] views = activePage.getViewReferences();
		for (IViewReference ref : views) {
			if (ref.getId().equals(IPageLayout.ID_PROP_SHEET)) {
				count++;
			}
		}
		return count;
	}

	protected IAction getPinPropertySheetAction(PropertySheet propertySheet) {
		IActionBars actionBars = propertySheet.getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();
		IContributionItem[] items = toolBarManager.getItems();
		for (IContributionItem contributionItem : items) {
			if (contributionItem.getId() != null
					&& contributionItem.getId().startsWith(
							PIN_PROPERTY_SHEET_ACTION_ID_PREFIX)) {
				return ((ActionContributionItem) contributionItem)
						.getAction();
			}
		}
		return null;
	}
}
