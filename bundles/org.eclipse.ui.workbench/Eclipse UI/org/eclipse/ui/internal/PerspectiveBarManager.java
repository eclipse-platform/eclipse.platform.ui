/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.internal.layout.LayoutUtil;

public class PerspectiveBarManager extends ToolBarManager {

    /**
     * The symbolic font name for the small font (value <code>"org.eclipse.jface.smallfont"</code>).
     */
    public static final String SMALL_FONT = "org.eclipse.ui.smallFont"; //$NON-NLS-1$
    private List mruList = new ArrayList();
    private List itemSequenceList = new ArrayList();
    
    public PerspectiveBarManager(int style) {
        super(style);
    }

    public ToolBar createControl(Composite parent) {
        ToolBar control = super.createControl(parent);

        if (control != null && !control.isDisposed())
                control.setFont(getFont());

        return control;
    }

    public PerspectiveBarManager(ToolBar toolbar) {
        super(toolbar);

        if (toolbar != null && !toolbar.isDisposed())
                toolbar.setFont(getFont());
    }

	// TODO begin refactor this out? it is not good that we know we are inside a
	// CoolBar
	private CoolBar coolBar;
    private Menu popup;
    
	public void handleChevron(SelectionEvent event) {
		/*
		 * If the popup menu is already there, then pop it down. This doesn't
		 * work... still need to figure this out.
		 */
		if (popup != null) {
			popup.dispose();
			popup = null;
			return;
		}
		CoolItem item = (CoolItem) event.widget;
		//ToolBar toolbar = (ToolBar)getControl();
		Control control = getControl();
		if (!(control instanceof ToolBar))
			return; // currently we only deal with toolbar items
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
			if (!intersection.equals(toolBounds))
				break;
			i++;
		}
		/* Create a pop-up menu with items for each of the hidden buttons. */
		popup = new Menu(coolBar);
		
		for (int j = i; j < toolCount; j++) {
			ToolItem tool = tools[j];
			MenuItem menuItem = new MenuItem(popup, SWT.NONE);
			menuItem.setText(tool.getText());
			menuItem.setImage(tool.getImage());
			
			menuItem.setData("IContributionItem", tool.getData()); //$NON-NLS-1$

			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					//rotate the selected item in and the other items right
					// don't touch the "Open" item
					MenuItem menuItem = (MenuItem)e.widget;
					Object item = menuItem.getData("IContributionItem"); //$NON-NLS-1$
					if (item instanceof PerspectiveBarContributionItem) {
						PerspectiveBarContributionItem contribItem = (PerspectiveBarContributionItem)item;
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
		popup.setLocation(pt.x, pt.y);
		popup.setVisible(true);
		Display display = coolBar.getDisplay();
		while (popup != null && popup.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		if (popup != null) {
			popup.dispose();
			popup = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ToolBarManager#relayout(org.eclipse.swt.widgets.ToolBar, int, int)
	 */
	protected void relayout(ToolBar toolBar, int oldCount, int newCount) {
		super.relayout(toolBar, oldCount, newCount);
		
		if (getControl() != null)
    		LayoutUtil.resize(getControl());
	}
	
    void setParent(CoolBar cool) {
        this.coolBar = cool;
    }

    // TODO end refactor this out?

    private Font getFont() {
        return JFaceResources.getFont(SMALL_FONT);
    }
    
    /**
     * Method to select a PerspectiveBarContributionItem and ensure
     * that it is visible. It updates the MRU list.
     * @param contribItem the PerspectiveBarContributionItem to select
     */
    void select(PerspectiveBarContributionItem contribItem) {
    		if (contribItem.getToolItem() == null)
    			return;
			// check if not visible and ensure visible
			Rectangle barBounds = getControl().getBounds();
			PerspectiveBarContributionItem newItem = null;
			if (getControl().isVisible() && !barBounds.intersects(contribItem.getToolItem().getBounds())) {
				// remove and add as the first non-visible
					// save the sequence index before
				int sequenceIndex = itemSequenceList.indexOf(contribItem);
				int index = Math.max(1, getItemInsertionIndex());
				newItem = new PerspectiveBarContributionItem(contribItem.getPerspective(), contribItem.getPage());
				
				removeItem(contribItem);
				contribItem.dispose();
				contribItem = null;
				
				insert(index, newItem);
				mruList.add(0, newItem);
				itemSequenceList.add(sequenceIndex, newItem);
				
				update(false);
				// ensure visible
				ensureVisible(index);
			}
			
			if (contribItem != null/* && getControl().isVisible()*/) {
				mruList.remove(contribItem);
			    mruList.add(0, contribItem);
			}
    }
    
    /**
	 * Method to get the insertion position of a new item
	 * @return the index at which a new item should be inserted
	 */
	private int getItemInsertionIndex() {
		if (!getControl().isVisible())
			return getControl().getItemCount();
		
		ToolItem[] items = getControl().getItems();
		if (items.length > 1) { 
			Rectangle barBounds = getControl().getBounds();
			// find the first non-visible item
			for (int i = 1; i < items.length; i++) {
				ToolItem toolItem = items[i];
				if (!barBounds.intersects(toolItem.getBounds()))
					return i;
			}
			// they are all visible
			return items.length;
		}
		// insert at the beginning
		return 1;
	}

	/**
	 * Method that adds a PerspectiveBarContributionItem and 
	 * ensures it is visible
	 * @param item the PerspectiveBarContributionItem to be added
	 */
	public void addItem(PerspectiveBarContributionItem item) {
		int index = Math.max(1, getItemInsertionIndex());
		insert(index, item);
		update(false);
		ensureVisible(index);
		mruList.add(0, item);
		itemSequenceList.add(item);
	}
	
	/**
	 * Method to remove a PerspectiveBarContributionItem from the toolbar
	 * and from the MRU and sequence lists when necessary
	 * @param item the PerspectiveBarContributionItem to be removed
	 */
	public void removeItem(PerspectiveBarContributionItem item) {
		mruList.remove(item);
		itemSequenceList.remove(item);
		remove(item);
	}

	/**
	 * Method to insure that an item at a given position is visible
	 * @param index the index of the item to ensure visible
	 * @param page the workbench page containing the perspective
	 */
	private void ensureVisible(int index) {
		if (index == 1 || !getControl().isVisible())
			return;
		
		ToolItem current = getControl().getItems()[index];
		Rectangle barBounds = getControl().getBounds();
		while (!barBounds.intersects(current.getBounds()) && index > 1) {
			index = relocateLRU(index);
			if (index == -1)
				break;
			// relocateLRU can affect the items so we need to update the 
			// variable here
			current = getControl().getItems()[index];
		}
	}

	/**
	 * Method that removes the most recently used and visible 
	 * perspective button and places it at a given index
	 * @param index the position at which the removed button should
	 * be relocated
     * @return index for convenience return the index that if needed can be used to 
     * relocate the next least recently used item
	 */
	private int relocateLRU(int index) {
		// if the MRU list does not contain entries, do nothing
		if (mruList.size() < 1)
			return -1;
		
		// get the perspective bar bounds
		Rectangle barBounds = getControl().getBounds();
		boolean isVis = false;
		IContributionItem item = null;
		// loop through the MRU list and find the visible LRU item  
		for (int MRUindex = mruList.size() - 1; !isVis && MRUindex >= 0; MRUindex--) {
			PerspectiveBarContributionItem pItem = (PerspectiveBarContributionItem)mruList.get(MRUindex);
			isVis = barBounds.intersects(pItem.getToolItem().getBounds());
			item = pItem;
		}
		// remove and add the found item at the given index in parameter
		if (item != null) {
			PerspectiveBarContributionItem contribItem = (PerspectiveBarContributionItem)item;
			relocateItem(contribItem, index);
		}
		else
			return -1;
		
		// the item to be removed next should be placed at
		// the given index -1 since we already removed an item 
		// prior to the given index
		return --index;
	}

	/**
	 * Method that relocates a PerspectiveBarContributionItem to another
	 * position, note that the item is disposed and a new one is created
	 * @param contribItem the PerspectiveBarContributionItem to relocate
	 * @param index the index to relocate at
	 */
	private void relocateItem(PerspectiveBarContributionItem contribItem, int index) {
		if (index < 1)
			return;
		PerspectiveBarContributionItem newItem = new PerspectiveBarContributionItem(contribItem.getPerspective(), contribItem.getPage());
		int removedIndex = mruList.indexOf(contribItem);
		int sequenceIndex = itemSequenceList.indexOf(contribItem);

		removeItem(contribItem);
		contribItem.dispose();
		insert(index, newItem);
		
		mruList.add(removedIndex, newItem);
		itemSequenceList.add(sequenceIndex, newItem);
		update(false);
	}
	
	/**
	 * Method to insert a PerspectiveBarContributionItem at a given index
	 * or prior to that index if it encounters an item that was added to
	 * the toolbar after the given item has been added.  (i.e. maintain the order
     * in which items were added/created while finding the location to insert)
	 * @param contribItem the PerspectiveBarContributionItem to insert
	 * @param index the index to insert at
	 */
	private void traverseAndInsert(PerspectiveBarContributionItem contribItem, int index) {
		if (index < 1)
			return;
		PerspectiveBarContributionItem newItem = new PerspectiveBarContributionItem(contribItem.getPerspective(), contribItem.getPage());
		int removedIndex = mruList.indexOf(contribItem);
		int sequenceIndex = itemSequenceList.indexOf(contribItem);
		
		int insertAt = index;
		for (int i = 1; i < index; i++) {
			// An item should be inserted in the order that it was added
			// If an item located prior to the given index in parameter
			// was added after the item we wish to insert at the given
			// index, we should stop at that position and insert our item
			ToolItem toolItem = getControl().getItem(i);
			// find the PerspectiveBarContributionItem since the sequence list
			// contains elements of that type
			PerspectiveBarContributionItem pbcItem = null;
			int idx = -1;
			for (int j = 0; j < itemSequenceList.size(); j++) {
				pbcItem = ((PerspectiveBarContributionItem)itemSequenceList.get(j));
				if (pbcItem.getToolItem().equals(toolItem)) {
					idx = itemSequenceList.indexOf(pbcItem);
					break;
				}
			}
			if (idx > sequenceIndex) {
				insertAt = i;
				break;
			}
		}
		
		removeItem(contribItem);
		contribItem.dispose();
		
		insert(insertAt, newItem);
		
		mruList.add(removedIndex, newItem);
		itemSequenceList.add(sequenceIndex, newItem);
		update(false);
	}

	/**
	 * Method to ensure that the selected button in the toolbar
	 * is showing
	 */
	public void arrangeToolBar() {
		// check if the tool bar is visible
		if (!getControl().isVisible())
			return;
		
		// the tool bar should contain at least the new perspective button 
		// and 2 other buttons
		if (getControl().getItemCount() < 3)
			return;
		
		Rectangle barBounds = getControl().getBounds();
		
		ToolItem[] items = getControl().getItems();
		for (int i = 2; i < items.length; i++) {
			if (items[i].getSelection()) {
				if (!barBounds.intersects(items[i].getBounds()))
					ensureVisible(i);
				break;
			}
			
		}
	}
	
	/**
	 * Method to re-arrange the items in the toolbar in the order
	 * they were added to the toolbar
	 */
	public void rebuildToolBar() {
		// check if the tool bar is visible
		if (!getControl().isVisible())
			return;
		
		// the tool bar should contain at least the new perspective button 
		// and 2 other buttons
		if (getControl().getItemCount() < 3)
			return;
		
		for (int i = 0; i < mruList.size(); i++) {
			PerspectiveBarContributionItem item = (PerspectiveBarContributionItem)mruList.get(i);
			int insertAt = itemSequenceList.indexOf(item)+1;
			if (insertAt != -1)
				if (insertAt == indexOf(item))
					continue;
			traverseAndInsert(item, insertAt);
		}
	}
	
	/**
	 * @return The number of visible items not including the
	 * new perspective button
	 */
	public int getVisibleItemCount() {
		int result = 0;
		Rectangle barBounds = getControl().getBounds();
		ToolItem[] items = getControl().getItems();
		for (int i = 1; i < items.length; i++) {
			if (barBounds.intersects(items[i].getBounds()))
				result++;
			else
				break;
		}
		
		return result;
	}
	
	/**
	 * @return the index of a PerspectiveBarContributionItem based
	 * on the index of its ToolItem in the toolbar 
	 */
	private int indexOf(PerspectiveBarContributionItem contribItem) {
		int result = -1;
		ToolItem tItem = contribItem.getToolItem();
		if (tItem == null || tItem.isDisposed())
			return result;
		
		ToolItem[] items = getControl().getItems();
		if (items.length < 2)
			return result;
		for (int i = 1; i < items.length; i++) {
			if (tItem.equals(items[i])) {
				result = i;
				break;
			}
		}
		return result;
	}
	
	/**
	 * Method to update the content of the MRU and sequence list
	 * when the perspective bar is moved from top right to top left
	 * or left
	 */
	public void updateLists() {
		IContributionItem[] newItems = this.getItems();
		
		for (int i = 1; i < newItems.length; i++) {
			PerspectiveBarContributionItem contribItem = (PerspectiveBarContributionItem)newItems[i];
			if (contribItem.getToolItem().getSelection())
				mruList.add(0, contribItem);
			else
				mruList.add(contribItem);
			itemSequenceList.add(contribItem);
		}
	}
}
