/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
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

    private IMenuListener menuListener = new IMenuListener() {
        public void menuAboutToShow(IMenuManager manager) {
            manager.markDirty();
        }
    };
    
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
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu, int)
     */
    public void fill(Menu menu, int index) {
        if (index == -1) {
			index = menu.getItemCount();
		}
        
        IContributionItem[] items = getContributionItemsToFill();
		if (index > menu.getItemCount()) {
			index = menu.getItemCount();
		}
        for (int i = 0; i < items.length; i++) {
            IContributionItem item = items[i];
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
        if (oldItems != null) {
            for (int i = 0; i < oldItems.length; i++) {
                IContributionItem oldItem = oldItems[i];
                oldItem.dispose();
            }
            oldItems = null;
        }
        oldItems = getContributionItems();
        return oldItems;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.ContributionItem#isDirty()
     */
    public boolean isDirty() {
		return true;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.ContributionItem#isDynamic()
     */
    public boolean isDynamic() {
        return true;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.ContributionItem#setParent(org.eclipse.jface.action.IContributionManager)
     */
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
}
