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
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * A <code>BaseNewWizardMenu</code> is used to populate a menu manager with
 * New Wizard actions for the current perspective's new wizard shortcuts,
 * including an Other... action to open the new wizard dialog.
 * 
 * @since 3.1
 */
public class BaseNewWizardMenu extends CompoundContributionItem {
    /*
     * @issue Should be possible to implement this class entirely using public
     * API. Cases to be fixed: WorkbenchWindow, NewWizardShortcutAction
     *  Suggestions:
     * - add the ability to update the submenus of a window
     */

    private final Map actions = new HashMap(21);

    private final IExtensionChangeHandler configListener = new IExtensionChangeHandler() {

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
         */
        public void removeExtension(IExtension source, Object[] objects) {
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof NewWizardShortcutAction) {
                    actions.values().remove(objects[i]);
                }
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
         */
        public void addExtension(IExtensionTracker tracker, IExtension extension) {
            // Do nothing
        }
    };

    /**
     * TODO: should this be done with an addition listener?
     */
    private final IRegistryChangeListener registryListener = new IRegistryChangeListener() {

        public void registryChanged(IRegistryChangeEvent event) {
            // reset the reader.
            // TODO This is expensive.  Can we be more selective?
            if (getParent() != null) {
                getParent().markDirty();
            }
        }

    };

    private final IAction showDlgAction;

    private IWorkbenchWindow workbenchWindow;

    /**
     * Creates a new wizard shortcut menu for the IDE.
     * 
     * @param window
     *            the window containing the menu
     * @param id
     *            the contribution item identifier, or <code>null</code>
     */
    public BaseNewWizardMenu(IWorkbenchWindow window, String id) {
        super(id);
        Assert.isNotNull(window);
        this.workbenchWindow = window;
        showDlgAction = ActionFactory.NEW.create(window);
        registerListeners();
        // indicate that a new wizards submenu has been created
        ((WorkbenchWindow) window)
                .addSubmenu(WorkbenchWindow.NEW_WIZARD_SUBMENU);
    }

    /**
     * Adds the items to show to the given list.
     * 
     * @param list the list to add items to
     */
    protected void addItems(List list) {
        if (addShortcuts(list)) {
            list.add(new Separator());
        }
        list.add(new ActionContributionItem(getShowDialogAction()));
    }

    /**
     * Adds the new wizard shortcuts for the current perspective to the given list.
     * 
     * @param list the list to add items to
     * @return <code>true</code> if any items were added, <code>false</code> if none were added
     */
    protected boolean addShortcuts(List list) {
        boolean added = false;
        IWorkbenchPage page = workbenchWindow.getActivePage();
        if (page != null) {
            String[] wizardIds = page.getNewWizardShortcuts();
            for (int i = 0; i < wizardIds.length; i++) {
                IAction action = getAction(wizardIds[i]);
                if (action != null) {
                    if (!WorkbenchActivityHelper.filterItem(action)) {
                        list.add(new ActionContributionItem(action));
                        added = true;
                    }
                }
            }
        }
        return added;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#dispose()
     */
    public void dispose() {
        if (workbenchWindow != null) {
            super.dispose();
            unregisterListeners();
            workbenchWindow = null;
        }
    }

    /*
     * (non-Javadoc) Returns the action for the given wizard id, or null if not
     * found.
     */
    private IAction getAction(String id) {
        // Keep a cache, rather than creating a new action each time,
        // so that image caching in ActionContributionItem works.
        IAction action = (IAction) actions.get(id);
        if (action == null) {
            IWizardDescriptor wizardDesc = WorkbenchPlugin.getDefault()
					.getNewWizardRegistry().findWizard(id);
            if (wizardDesc != null) {
                action = new NewWizardShortcutAction(workbenchWindow,
						wizardDesc);
				actions.put(id, action);
				IConfigurationElement element = (IConfigurationElement) Util
						.getAdapter(wizardDesc, IConfigurationElement.class);
				if (element != null) {
					workbenchWindow.getExtensionTracker().registerObject(
							element.getDeclaringExtension(), action,
							IExtensionTracker.REF_WEAK);
				}
            }
        }
        return action;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
     */
    protected IContributionItem[] getContributionItems() {
        ArrayList list = new ArrayList();
        if (workbenchWindow != null && workbenchWindow.getActivePage() != null
                && workbenchWindow.getActivePage().getPerspective() != null) {
            addItems(list);
        } else {
            String text = WorkbenchMessages.Workbench_noApplicableItems;
            Action dummyAction = new Action(text) {
                // dummy inner class; no methods
            };
            dummyAction.setEnabled(false);
            list.add(new ActionContributionItem(dummyAction));
        }
        return (IContributionItem[]) list.toArray(new IContributionItem[list.size()]);
    }

    /**
     * Returns the "Other..." action, used to show the new wizards dialog.
     * 
     * @return the action used to show the new wizards dialog
     */
    protected IAction getShowDialogAction() {
        return showDlgAction;
    }

    /**
     * Returns the window in which this menu appears.
     * 
     * @return the window in which this menu appears
     */
    protected IWorkbenchWindow getWindow() {
        return workbenchWindow;
    }

    /**
     * Registers listeners.
     * 
     * @since 3.1
     */
    private void registerListeners() {
        Platform.getExtensionRegistry().addRegistryChangeListener(
                registryListener);
        workbenchWindow.getExtensionTracker().registerHandler(
				configListener,  null);
    }

    /**
     * Returns whether the new wizards registry has a non-empty category with
     * the given identifier.
     * 
     * @param categoryId
     *            the identifier for the category
     * @return <code>true</code> if there is a non-empty category with the
     *         given identifier, <code>false</code> otherwise
     */
    protected boolean registryHasCategory(String categoryId) {
    	return WorkbenchPlugin.getDefault().getNewWizardRegistry()
				.findCategory(categoryId) != null;
    }

    /**
     * Unregisters listeners.
     * 
     * @since 3.1
     */
    private void unregisterListeners() {
        Platform.getExtensionRegistry().removeRegistryChangeListener(
                registryListener);
        workbenchWindow.getExtensionTracker().unregisterHandler(configListener);
    }
}
