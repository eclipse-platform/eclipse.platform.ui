/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

/**
 * A contribution item that is intended to hold the place of a tool bar
 * contribution item that has been disposed. This is to ensure that tool bar
 * contribution items are disposed (freeing their resources), but that layout
 * information about the item is not lost.
 * 
 * @since 3.0
 */
final class PlaceholderContributionItem implements IContributionItem {

    /**
     * The identifier for the replaced contribution item.
     */
    private final String id;

    /**
     * The height of the SWT widget corresponding to the replaced contribution
     * item.
     */
    private final int storedHeight;

    /**
     * The minimum number of items to display on the replaced contribution
     * item.
     */
    private final int storedMinimumItems;

    /**
     * Whether the replaced contribution item would display chevrons.
     */
    private final boolean storedUseChevron;

    /**
     * The width of the SWT widget corresponding to the replaced contribution
     * item.
     */
    private final int storedWidth;

    /**
     * Constructs a new instance of <code>PlaceholderContributionItem</code>
     * from the item it is intended to replace.
     * 
     * @param item
     *            The item to be replaced; must not be <code>null</code>.
     */
    PlaceholderContributionItem(final ToolBarContributionItem item) {
        item.saveWidgetState();
        id = item.getId();
        storedHeight = item.getCurrentHeight();
        storedWidth = item.getCurrentWidth();
        storedMinimumItems = item.getMinimumItemsToShow();
        storedUseChevron = item.getUseChevron();
    }

    /**
     * Creates a new tool bar contribution item on the given manager -- using
     * the stored data to initialize some of its properties.
     * 
     * @param manager
     *            The manager for which the contribution item should be
     *            created; must not be <code>null</code>
     * @return A new tool bar contribution item equivalent to the contribution
     *         item this placeholder was intended to replace; never <code>null</code>.
     */
    ToolBarContributionItem createToolBarContributionItem(
            final ToolBarManager manager) {
        ToolBarContributionItem toolBarContributionItem = new ToolBarContributionItem(
                manager, id);
        toolBarContributionItem.setCurrentHeight(storedHeight);
        toolBarContributionItem.setCurrentWidth(storedWidth);
        toolBarContributionItem.setMinimumItemsToShow(storedMinimumItems);
        toolBarContributionItem.setUseChevron(storedUseChevron);
        return toolBarContributionItem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#dispose()
     */
    public void dispose() {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Composite)
     */
    public void fill(Composite parent) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.CoolBar,
     *      int)
     */
    public void fill(CoolBar parent, int index) {
        throw new UnsupportedOperationException();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Menu,
     *      int)
     */
    public void fill(Menu parent, int index) {
        throw new UnsupportedOperationException();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.ToolBar,
     *      int)
     */
    public void fill(ToolBar parent, int index) {
        throw new UnsupportedOperationException();

    }

    /**
     * The height of the replaced contribution item.
     * 
     * @return The height.
     */
    int getHeight() {
        return storedHeight;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#getId()
     */
    public String getId() {
        return id;
    }

    /**
     * The width of the replaced contribution item.
     * 
     * @return The width.
     */
    int getWidth() {
        return storedWidth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#isDirty()
     */
    public boolean isDirty() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#isDynamic()
     */
    public boolean isDynamic() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#isEnabled()
     */
    public boolean isEnabled() {
        // XXX Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#isGroupMarker()
     */
    public boolean isGroupMarker() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#isSeparator()
     */
    public boolean isSeparator() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#isVisible()
     */
    public boolean isVisible() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#saveWidgetState()
     */
    public void saveWidgetState() {
        // Do nothing.

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#setParent(org.eclipse.jface.action.IContributionManager)
     */
    public void setParent(IContributionManager parent) {
        // Do nothing

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        // Do nothing.
    }

    /**
     * Displays a string representation of this contribution item, which is
     * really just a function of its identifier.
     */
    public String toString() {
        return "PlaceholderContributionItem(" + id + ")"; //$NON-NLS-1$//$NON-NLS-2$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#update()
     */
    public void update() {
        update(null);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IContributionItem#update(java.lang.String)
     */
    public void update(String identifier) {
        // Do nothing
    }
}
