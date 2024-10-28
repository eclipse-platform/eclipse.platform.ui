/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 444070
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.junit.Assert;

/**
 * <code>ActionUtil</code> contains methods to run actions in the workbench.
 */
public class ActionUtil {

	/**
	 * Runs an action contribution.
	 *
	 * @param item an action contribution item
	 */
	public static void runAction(IContributionItem item) {
		Assert.assertTrue(item instanceof ActionContributionItem);
		((ActionContributionItem) item).getAction().run();
	}

	/**
	 * Runs the first action found in a menu manager with a
	 * particular label.
	 *
	 * @param mgr the containing menu manager
	 * @param label the action label
	 */
	public static void runActionWithLabel(IMenuManager mgr, String label) {
		IContributionItem[] items = mgr.getItems();
		for (IContributionItem item : items) {
			if (item instanceof SubContributionItem subItem)
				item = subItem.getInnerItem();
			if (item instanceof ActionContributionItem actionContribItem) {
				IAction action = actionContribItem.getAction();
				if (label.equals(action.getText())) {
					action.run();
					return;
				}
			}
		}
		Assert.fail("Unable to find action: " + label);
	}

	/**
	 * Runs the first action found in a window with a
	 * particular label.
	 *
	 * @param win the containing window
	 * @param label the action label
	 */
	public static void runActionWithLabel(IWorkbenchWindow win, String label) {
		WorkbenchWindow realWin = (WorkbenchWindow) win;
		IMenuManager mgr = realWin.getMenuBarManager();
		runActionWithLabel(mgr, label);
	}

	/**
	 * Runs an action identified by an id path in a
	 * menu manager.
	 *
	 * @param mgr the containing menu manager
	 */
	public static void runActionUsingPath(IMenuManager mgr, String idPath) {
		IContributionItem item = mgr.findUsingPath(idPath);
		Assert.assertNotNull(item);
		runAction(item);
	}

	/**
	 * Runs an action identified by an id path in a
	 * window.
	 *
	 * @param win the containing window
	 */
	public static void runActionUsingPath(IWorkbenchWindow win, String idPath) {
		WorkbenchWindow realWin = (WorkbenchWindow) win;
		IMenuManager mgr = realWin.getMenuBarManager();
		runActionUsingPath(mgr, idPath);
	}

	/**
	 * Returns the first action found in a menu manager with a
	 * particular label.
	 *
	 * @param mgr the containing menu manager
	 * @param label the action label
	 * @return the first action with the label, or <code>null</code>
	 * 		if it is not found.
	 */
	public static IAction getActionWithLabel(IMenuManager mgr, String label) {
		IContributionItem[] items = mgr.getItems();
		for (IContributionItem item : items) {
			if (item instanceof SubContributionItem subItem)
				item = subItem.getInnerItem();
			if (item instanceof ActionContributionItem actionContribItem) {
				IAction action = actionContribItem.getAction();
				if (label.equals(action.getText())) {
					return action;
				}
			}
		}
		return null;
	}

}

