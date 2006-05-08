/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import java.lang.reflect.Method;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * <code>ActionUtil</code> contains methods to run actions
 * in the workbench.
 */
public class ActionUtil {

    /**
     * Runs an action contribution.
     * 
     * @param test the current test case
     * @param item an action contribution item
     */
    public static void runAction(TestCase test, IContributionItem item) {
        Assert.assertTrue(item instanceof ActionContributionItem);
        ((ActionContributionItem) item).getAction().run();
    }

    /**
     * Runs the first action found in a menu manager with a
     * particular label. 
     *
     * @param test the current test case
     * @param mgr the containing menu manager
     * @param label the action label
     */
    public static void runActionWithLabel(TestCase test, IMenuManager mgr,
            String label) {
        IContributionItem[] items = mgr.getItems();
        for (int nX = 0; nX < items.length; nX++) {
            IContributionItem item = items[nX];
            if (item instanceof SubContributionItem)
                item = ((SubContributionItem) item).getInnerItem();
            if (item instanceof ActionContributionItem) {
                IAction action = ((ActionContributionItem) item).getAction();
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
     * @param test the current test case
     * @param win the containing window
     * @param label the action label
     */
    public static void runActionWithLabel(TestCase test, IWorkbenchWindow win,
            String label) {
        WorkbenchWindow realWin = (WorkbenchWindow) win;
        IMenuManager mgr = realWin.getMenuManager();
        runActionWithLabel(test, mgr, label);
    }

    /**
     * Runs an action identified by an id path in a 
     * menu manager.
     * 
     * @param test the current test case
     * @param mgr the containing menu manager
     * @param label the action label
     */
    public static void runActionUsingPath(TestCase test, IMenuManager mgr,
            String idPath) {
        IContributionItem item = mgr.findUsingPath(idPath);
        Assert.assertNotNull(item);
        runAction(test, item);
    }

    /**
     * Runs an action identified by an id path in a 
     * window.
     * 
     * @param test the current test case
     * @param win the containing window
     * @param label the action label
     */
    public static void runActionUsingPath(TestCase test, IWorkbenchWindow win,
            String idPath) {
        WorkbenchWindow realWin = (WorkbenchWindow) win;
        IMenuManager mgr = realWin.getMenuManager();
        runActionUsingPath(test, mgr, idPath);
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
        for (int nX = 0; nX < items.length; nX++) {
            IContributionItem item = items[nX];
            if (item instanceof SubContributionItem)
                item = ((SubContributionItem) item).getInnerItem();
            if (item instanceof ActionContributionItem) {
                IAction action = ((ActionContributionItem) item).getAction();
                if (label.equals(action.getText())) {
                    return action;
                }
            }
        }
        return null;
    }

    /**
     * Fire the "handleAboutToShow" method in a menu manager.
     * This triggers the same behavior as when a user opens a menu.
     * The menu to be populated with actions and those 
     * actions to be enacted in SWT widgets.
     * 
     * @param mgr the menu manager to open
     */
    public static void fireAboutToShow(MenuManager mgr) throws Throwable {
        Class clazz = mgr.getClass();
        Method method = clazz.getDeclaredMethod("handleAboutToShow",
                new Class[0]);
        method.setAccessible(true);
        method.invoke(mgr, new Object[0]);
    }
}

