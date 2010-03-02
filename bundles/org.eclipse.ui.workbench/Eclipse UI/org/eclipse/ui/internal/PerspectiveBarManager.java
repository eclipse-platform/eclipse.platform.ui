/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
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
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.layout.LayoutUtil;
import org.eclipse.ui.internal.util.PrefUtil;

public class PerspectiveBarManager extends ToolBarManager {

    public PerspectiveBarManager(int style) {
        super(style);
    }

    public ToolBar createControl(Composite parent) {
        ToolBar control = super.createControl(parent);

        return control;
    }

    // TODO begin refactor this out? it is not good that we know we are inside a
    // CoolBar
    private CoolBar coolBar;
    private Menu chevronMenu = null;
    
    public void handleChevron(SelectionEvent event) {
        CoolItem item = (CoolItem) event.widget;
        //ToolBar toolbar = (ToolBar)getControl();
        Control control = getControl();
        if (!(control instanceof ToolBar)) {
			return; // currently we only deal with toolbar items
		}
        /* Retrieve the current bounding rectangle for the selected cool item. */
        Rectangle itemBounds = item.getBounds();
        /* Convert to display coordinates (i.e. was relative to CoolBar). */
        Point pt = coolBar.toDisplay(new Point(itemBounds.x, itemBounds.y));
        itemBounds.x = pt.x;
        itemBounds.y = pt.y;
        /* Retrieve the total number of buttons in the toolbar. */
        ToolBar toolBar = (ToolBar) control;
        ToolItem[] tools = toolBar.getItems();
        int toolCount = tools.length;
        int i = 0;
        while (i < toolCount) {
            /*
             * Starting from the leftmost tool, retrieve the tool's bounding
             * rectangle.
             */
            Rectangle toolBounds = tools[i].getBounds();
            /* Convert to display coordinates (i.e. was relative to ToolBar). */
            pt = toolBar.toDisplay(new Point(toolBounds.x, toolBounds.y));
            toolBounds.x = pt.x;
            toolBounds.y = pt.y;
            /*
             * Figure out the visible portion of the tool by looking at the
             * intersection of the tool bounds with the cool item bounds.
             */
            Rectangle intersection = itemBounds.intersection(toolBounds);
            /*
             * If the tool is not completely within the cool item bounds, then
             * the tool is at least partially hidden, and all remaining tools
             * are completely hidden.
             */
            if (!intersection.equals(toolBounds)) {
				break;
			}
            i++;
        }
        
        /* Create a pop-up menu with items for each of the hidden buttons. */
        // Out with the old...
        if (chevronMenu != null && !chevronMenu.isDisposed())
        	chevronMenu.dispose();
        
        // ...In with the new
        chevronMenu = new Menu(coolBar);

        for (int j = i; j < toolCount; j++) {
            ToolItem tool = tools[j];
            MenuItem menuItem = new MenuItem(chevronMenu, SWT.NONE);
            if (tool.getSelection()) {
				menuItem.setEnabled(false);
			}
            
            // Show the full text of the perspective name in the chevron menu.
			if (tool.getData() instanceof PerspectiveBarContributionItem) {
				menuItem.setText(((PerspectiveBarContributionItem) tool
						.getData()).getPerspective().getLabel());
			} else {
				menuItem.setText(tool.getText());
			}
            menuItem.setImage(tool.getImage());

            menuItem.setData("IContributionItem", tool.getData()); //$NON-NLS-1$

            menuItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    //rotate the selected item in and the other items right
                    // don't touch the "Open" item
                    MenuItem menuItem = (MenuItem) e.widget;
                    Object item = menuItem.getData("IContributionItem"); //$NON-NLS-1$
                    if (item instanceof PerspectiveBarContributionItem) {
                        PerspectiveBarContributionItem contribItem = (PerspectiveBarContributionItem) item;
                        update(false);
                        contribItem.select();
                    }
                }
            });
        }
        /*
         * Display the pop-up menu immediately below the chevron, with the left
         * edges aligned. Need to convert the given point to display
         * coordinates in order to pass them to Menu.setLocation (i.e. was
         * relative to CoolBar).
         */
        pt = coolBar.toDisplay(new Point(event.x, event.y));
        chevronMenu.setLocation(pt.x, pt.y);
        chevronMenu.setVisible(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.ToolBarManager#relayout(org.eclipse.swt.widgets.ToolBar, int, int)
     */
    protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
        super.relayout(toolBar, oldCount, newCount);

        if (getControl() != null) {
			LayoutUtil.resize(getControl());
		}
    }

    void setParent(CoolBar cool) {
        this.coolBar = cool;
    }

    /**
     * Method to select a PerspectiveBarContributionItem and ensure
     * that it is visible. It updates the MRU list.
     * @param contribItem the PerspectiveBarContributionItem to select
     */
    void select(PerspectiveBarContributionItem contribItem) {
        if (contribItem.getToolItem() == null) {
			return;
		}
        // check if not visible and ensure visible
        if (getControl().isVisible()
                && !isItemVisible(contribItem.getToolItem())) {
            ensureVisible(contribItem);
        }
    }

    /**
     * Method that adds a PerspectiveBarContributionItem and 
     * ensures it is visible
     * @param item the PerspectiveBarContributionItem to be added
     */
    public void addItem(PerspectiveBarContributionItem item) {
    	// Put it after the 'open' button (if any)
    	IPreferenceStore apiPreferenceStore = PrefUtil.getAPIPreferenceStore();
    	if (apiPreferenceStore.getBoolean(IWorkbenchPreferenceConstants.SHOW_OPEN_ON_PERSPECTIVE_BAR))
    		insert(1, item);
    	else
    		insert(0, item);

    	update(false);
    }

    /**
     * Method to remove a PerspectiveBarContributionItem from the toolbar
     * and from the MRU and sequence lists when necessary
     * @param item the PerspectiveBarContributionItem to be removed
     */
    public void removeItem(PerspectiveBarContributionItem item) {
        remove(item);
    }

    /**
     * Re-insert the item at the beginning of the perspective bar,
     * ensuring that it is visible to the user.
     * 
     * @param contribItem
     */
    private void ensureVisible(PerspectiveBarContributionItem contribItem) {
    	relocate(contribItem, 1);
    }
    
    void relocate(PerspectiveBarContributionItem contribItem, int index) {
        PerspectiveBarContributionItem newItem = new PerspectiveBarContributionItem(
                contribItem.getPerspective(), contribItem.getPage());

        removeItem(contribItem);
        contribItem.dispose();
        contribItem = null;

        insert(index, newItem);
        update(false);
    }

    /**
     * @return true if the toolItem is visible on the screen, false otherwise.
     */
    private boolean isItemVisible(ToolItem toolItem) {
        Rectangle barBounds = getControl().getBounds();
        Rectangle itemBounds = toolItem.getBounds();
        return (barBounds.intersection(itemBounds).equals(itemBounds));
    }

    /**
     * This method ensures that the selected item in the toolbar is visible.
     */
    public void arrangeToolbar() {
        // check if the tool bar is visible
        if (!getControl().isVisible()) {
			return;
		}

        // the tool bar should contain at least the new perspective button 
        // and 2 other buttons
        if (getControl().getItemCount() < 3) {
			return;
		}

        // Find the selected item and make sure it is visible.
        IContributionItem[] items = getItems();
        for (int i = 2; i < items.length; i++) {
            PerspectiveBarContributionItem contribItem = (PerspectiveBarContributionItem) items[i];
            if (contribItem.getToolItem().getSelection()) {
                if (!isItemVisible(contribItem.getToolItem())) {
                    ensureVisible(contribItem);
                    break;
                }
            }
        }
    }
}
