/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * Sub-menu off project menu for showing MRU list of working set builds.
 * 
 * @since 3.0
 */
public class BuildSetMenu extends ContributionItem {
    private IActionBarConfigurer actionBars;

    boolean dirty = true;

    private IMenuListener menuListener = new IMenuListener() {
        public void menuAboutToShow(IMenuManager manager) {
            manager.markDirty();
            dirty = true;
        }
    };

    private IAction selectBuildWorkingSetAction;

    private IWorkbenchWindow window;

    public BuildSetMenu(IWorkbenchWindow window, IActionBarConfigurer actionBars) {
        this.window = window;
        this.actionBars = actionBars;
        selectBuildWorkingSetAction = new SelectBuildWorkingSetAction(window,
                actionBars);
    }

    /**
     * Adds a mnemonic accelerator to actions in the MRU list of
     * recently built working sets
     */
    private void addMnemonic(BuildSetAction action, int index) {
        StringBuffer label = new StringBuffer();
        //add the numerical accelerator
        if (index < 9) {
            label.append('&');
            label.append(index);
            label.append(' ');
        }
        label.append(action.getWorkingSet().getName());
        action.setText(label.toString());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Menu, int)
     */
    public void fill(Menu menu, int index) {
        if (getParent() instanceof MenuManager)
            ((MenuManager) getParent()).addMenuListener(menuListener);
        if (!dirty)
            return;
        fillMenu(menu);
        dirty = false;
    }

    /**
     * Fills the menu with Show View actions.
     */
    private void fillMenu(Menu menu) {
        boolean isAutoBuilding = ResourcesPlugin.getWorkspace()
                .isAutoBuilding();

        //build MRU list of recently built working sets:
        IWorkingSet[] sets = window.getWorkbench().getWorkingSetManager()
                .getRecentWorkingSets();
        BuildSetAction last = BuildSetAction.lastBuilt;
        IWorkingSet lastSet = null;
        //add build action for the last working set that was built
        int accel = 1;
        if (last != null) {
            last.setChecked(true);
            last.setEnabled(!isAutoBuilding);
            last.setActionDefinitionId("org.eclipse.ui.project.buildLast"); //$NON-NLS-1$
            addMnemonic(last, accel++);
            new ActionContributionItem(last).fill(menu, -1);
            lastSet = last.getWorkingSet();
        }
        //add build actions for the most recently used working sets
        for (int i = 0; i < sets.length; i++) {
            if (lastSet != null && lastSet.equals(sets[i]))
                continue;
            BuildSetAction action = new BuildSetAction(sets[i], window,
                    actionBars);
            addMnemonic(action, accel++);
            action.setEnabled(!isAutoBuilding);
            new ActionContributionItem(action).fill(menu, -1);
        }
        //add the action to select a different working set
        new Separator().fill(menu, -1);
        selectBuildWorkingSetAction.setEnabled(!isAutoBuilding);
        new ActionContributionItem(selectBuildWorkingSetAction).fill(menu, -1);
    }

    public boolean isDirty() {
        return dirty;
    }

    /**
     * Overridden to always return true and force dynamic menu building.
     */
    public boolean isDynamic() {
        return true;
    }
}