/*******************************************************************************
 * Copyright (c) 2008, 2009 Oakland Software Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Oakland Software Incorporated - initial API and implementation
 *.....IBM Corporation - fixed dead code warning
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

public class ActivityTest extends NavigatorTestBase {

	public ActivityTest() {
		_navigatorInstanceId = ProjectExplorer.VIEW_ID;
	}

	protected static final String ACTIVITY = "org.eclipse.ui.tests.navigator.testActivity";

	private static final boolean DEBUG = false;

	protected boolean verifyMenu(IStructuredSelection sel, String item) {
		MenuManager mm = new MenuManager();
		_actionService.setContext(new ActionContext(sel));
		_actionService.fillContextMenu(mm);

		IContributionItem[] items = mm.getItems();

		MenuManager newMm = (MenuManager) items[1];

		items = newMm.getItems();
		// Get the new Menu
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof ActionContributionItem) {
				ActionContributionItem aci = (ActionContributionItem) items[i];
				if (aci.getAction().getText().startsWith(item))
					return true;
				if (DEBUG)
					System.out.println("action text: " + aci.getAction().getText());
			}
		}

		return false;
	}

	// Bug 217801 make sure category filtering works with common wizards
	public void testCategoryWizard() throws Exception {

		IStructuredSelection sel = new StructuredSelection(_project);
		_viewer.setSelection(sel);

		IWorkbenchActivitySupport actSupport = PlatformUI.getWorkbench().getActivitySupport();

		assertFalse(verifyMenu(sel, "Test CNF"));

		Set ids = new HashSet();
		ids = actSupport.getActivityManager().getEnabledActivityIds();

		//System.out.println("enabled before: " + ids);

		Set newIds = new HashSet();
		newIds.addAll(ids);
		newIds.add(ACTIVITY);
		actSupport.setEnabledActivityIds(newIds);

		ids = actSupport.getActivityManager().getEnabledActivityIds();
		//System.out.println("enabled now: " + ids);

		assertTrue(verifyMenu(sel, "Test CNF"));

	}

}
