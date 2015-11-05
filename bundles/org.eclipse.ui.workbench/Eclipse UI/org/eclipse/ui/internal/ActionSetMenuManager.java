/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.SubContributionItem;
import org.eclipse.jface.action.SubMenuManager;

/**
 * An <code>EditorMenuManager</code> is used to sort the contributions
 * made by an editor so that they always appear after the action sets.
 */
public class ActionSetMenuManager extends SubMenuManager {
    private String actionSetId;

    /**
     * Constructs a new editor manager.
     */
    public ActionSetMenuManager(IMenuManager mgr, String actionSetId) {
        super(mgr);
        this.actionSetId = actionSetId;
    }

	/**
	 * Returns the item passed to us, not the wrapper. In the case of menu's not
	 * added by this manager, ensure that we return a wrapper for the menu.
	 */
    @Override
	public IContributionItem find(String id) {
        IContributionItem item = getParentMenuManager().find(id);
        if (item instanceof SubContributionItem) {
			// Return the item passed to us, not the wrapper.
            item = unwrap(item);
		}

        if (item instanceof IMenuManager) {
            // if it is a menu manager wrap it before returning
            IMenuManager menu = (IMenuManager) item;
            if (menu instanceof SubMenuManager) {
				// it it is already wrapped then remover the wrapper and
                // rewrap. We have a table of wrappers so we reuse wrappers
                // we create.
                menu = (IMenuManager) ((SubMenuManager) menu).getParent();
			}
            item = getWrapper(menu);
        }

        return item;
    }

	/**
	 * @return Returns the actionSetId.
	 */
	public String getActionSetId() {
		return actionSetId;
	}

    @Override
	public IContributionItem[] getItems() {
        return getParentMenuManager().getItems();
    }

    @Override
	protected SubContributionItem wrap(IContributionItem item) {
        return new ActionSetContributionItem(item, actionSetId);
    }

    @Override
	protected SubMenuManager wrapMenu(IMenuManager menu) {
        return new ActionSetMenuManager(menu, actionSetId);
    }

	@Override
	public String toString() {
		return "ActionSetMenuManager [id=" + actionSetId + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
