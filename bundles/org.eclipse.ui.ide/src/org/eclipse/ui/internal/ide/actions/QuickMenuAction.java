/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.QuickMenuCreator;
import org.eclipse.ui.keys.IBindingService;

/**
 * A quick menu actions provides support to assign short cuts
 * to sub menus.
 * 
 * @since 3.0
 */
public abstract class QuickMenuAction extends Action {

	private QuickMenuCreator creator = new QuickMenuCreator() {
		protected void fillMenu(IMenuManager menu) {
			QuickMenuAction.this.fillMenu(menu);
		}
	};

    /**
     * Creates a new quick menu action with the given command id.
     * 
     * @param commandId the command id of the short cut used to open
     *  the sub menu 
     */
    public QuickMenuAction(String commandId) {
        setId(commandId);
        setActionDefinitionId(commandId);
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
    	creator.createMenu();
    }
    
    /**
     * Dispose of this menu action.
     */
    public void dispose() {
        if (creator != null) {
            creator.dispose();
            creator = null;
        }
    }

    /**
     * Hook to fill a menu manager with the items of the sub menu.
     * 
     * @param menu the sub menu to fill
     */
    protected abstract void fillMenu(IMenuManager menu);
    
    /**
     * Returns the short cut assigned to the sub menu or <code>null</code> if
     * no short cut is assigned.
     * 
     * @return the short cut as a human readable string or <code>null</code>
     */
    public String getShortCutString() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IBindingService bindingService = (IBindingService) workbench
				.getAdapter(IBindingService.class);
		final TriggerSequence[] activeBindings = bindingService
				.getActiveBindingsFor(getActionDefinitionId());
		if (activeBindings.length > 0) {
			return activeBindings[0].format();
		}

		return null;
    }
}
