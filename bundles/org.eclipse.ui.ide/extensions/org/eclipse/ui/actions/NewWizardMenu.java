/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;

/**
 * A <code>NewWizardMenu</code> augments <code>BaseNewWizardMenu</code> with IDE-specific
 * actions: New Project... (always shown) and New Example... (shown only if there are example wizards installed).
 */
public class NewWizardMenu extends BaseNewWizardMenu {

    private final IAction newProjectAction;

    private final IAction newExampleAction;

    /**
     * Creates a new wizard shortcut menu for the IDE.
     * 
     * @param window
     *            the window containing the menu
     */
    public NewWizardMenu(IWorkbenchWindow window) {
        this(window, null);
        
    }
    
    /**
     * Creates a new wizard shortcut menu for the IDE.
     * 
     * @param window
     *            the window containing the menu
     * @param id
     *            the identifier for this contribution item 
     */
    public NewWizardMenu(IWorkbenchWindow window, String id) {
        super(window, id);
        newProjectAction = new NewProjectAction(window);
        newExampleAction = new NewExampleAction(window);
    }

    /**
     * Create a new wizard shortcut menu.  
     * <p>
     * If the menu will appear on a semi-permanent basis, for instance within
     * a toolbar or menubar, the value passed for <code>register</code> should be true.
     * If set, the menu will listen to perspective activation and update itself
     * to suit.  In this case clients are expected to call <code>deregister</code> 
     * when the menu is no longer needed.  This will unhook any perspective
     * listeners.
     * </p>
     *
     * @param innerMgr the location for the shortcut menu contents
     * @param window the window containing the menu
     * @param register if <code>true</code> the menu listens to perspective changes in
     *      the window
     * @deprecated use NewWizardMenu(IWorkbenchWindow) instead
     */
    public NewWizardMenu(IMenuManager innerMgr, IWorkbenchWindow window,
            boolean register) {
        this(window, null);
        fillMenu(innerMgr);
        // Must be done after constructor to ensure field initialization.
    }
    
    /* (non-Javadoc)
     * Fills the menu with New Wizards.
     */
    private void fillMenu(IContributionManager innerMgr) {
        // Remove all.
        innerMgr.removeAll();

        IContributionItem[] items = getContributionItems();
        for (int i = 0; i < items.length; i++) {
            innerMgr.add(items[i]);
        }
    }

    /**
     * Removes all listeners from the containing workbench window.
     * <p>
     * This method should only be called if the shortcut menu is created with
     * <code>register = true</code>.
     * </p>
     * 
     * @deprecated has no effect
     */
    public void deregisterListeners() {
        // do nothing
    }

    /**
     * Return whether or not any examples are in the current install.
     * 
     * @return boolean
     */
    private boolean hasExamples() {
        return registryHasCategory(NewWizardsRegistryReader.FULL_EXAMPLES_WIZARD_CATEGORY);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.BaseNewWizardMenu#addItems(org.eclipse.jface.action.IContributionManager)
     */
    protected void addItems(List list) {
        list.add(new ActionContributionItem(newProjectAction)); 
        list.add(new Separator());
        addShortcuts(list);
        list.add(new Separator());
        if (hasExamples()) {
            list.add(new ActionContributionItem(newExampleAction));
            list.add(new Separator());
        }
        list.add(new ActionContributionItem(getShowDialogAction()));
    }
    
}