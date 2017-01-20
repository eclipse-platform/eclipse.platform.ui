/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Menu;

/**
 * A compound contribution is a contribution item consisting of a
 * dynamic list of contribution items.
 *
 * @since 3.1
 */
public abstract class CompoundContributionItem extends ContributionItem {

    private IMenuListener menuListener = manager -> manager.markDirty();

    private IContributionItem[] oldItems;

    /**
     * Creates a compound contribution item with a <code>null</code> id.
     */
    protected CompoundContributionItem() {
        super();
    }

    /**
     * Creates a compound contribution item with the given (optional) id.
     *
     * @param id the contribution item identifier, or <code>null</code>
     */
    protected CompoundContributionItem(String id) {
        super(id);
    }

    @Override
	public void fill(Menu menu, int index) {
        if (index == -1) {
			index = menu.getItemCount();
		}

        IContributionItem[] items = getContributionItemsToFill();
		if (index > menu.getItemCount()) {
			index = menu.getItemCount();
		}
        for (IContributionItem item : items) {
            int oldItemCount = menu.getItemCount();
            if (item.isVisible()) {
                item.fill(menu, index);
            }
            int newItemCount = menu.getItemCount();
            int numAdded = newItemCount - oldItemCount;
            index += numAdded;
        }
    }

    /**
	 * Return a list of contributions items that will replace this item in the
	 * parent manager. The list must contain new contribution items every call
	 * since the old ones will be disposed.
	 *
	 * @return an array list of items to display. Must not be <code>null</code>.
	 */
    protected abstract IContributionItem[] getContributionItems();

    private IContributionItem[] getContributionItemsToFill() {
		disposeOldItems();
		oldItems = getContributionItems();
		return oldItems;
	}

	private void disposeOldItems() {
        if (oldItems != null) {
            for (IContributionItem oldItem : oldItems) {
                oldItem.dispose();
            }
            oldItems = null;
        }
    }

    @Override
	public boolean isDirty() {
		return true;
    }

    @Override
	public boolean isDynamic() {
        return true;
    }


    @Override
	public void setParent(IContributionManager parent) {
        if (getParent() instanceof IMenuManager) {
            IMenuManager menuMgr = (IMenuManager) getParent();
            menuMgr.removeMenuListener(menuListener);
        }
        if (parent instanceof IMenuManager) {
            IMenuManager menuMgr = (IMenuManager) parent;
            menuMgr.addMenuListener(menuListener);
        }
        super.setParent(parent);
    }

	@Override
	public void dispose() {
		disposeOldItems();
		super.dispose();
	}
}
