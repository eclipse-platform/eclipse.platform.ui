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
package org.eclipse.jface.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;

import org.eclipse.jface.util.Assert;

/**
 * A cool bar manager is a contribution manager which realizes itself and its items
 * in a cool bar control.
 * <p>
 * This class may be instantiated; it may also be subclassed.
 * </p>
 * 
 * @see #ICoolBarManager
 * @since 3.0
 */
public class CoolBarManager extends ContributionManager implements ICoolBarManager {

	/** 
	 * The cool bar items style; <code>SWT.NONE</code> by default.
	 */
	private int itemStyle = SWT.NONE;
	
	/**
	 * A separator created by the end user.
	 */
	private final static String USER_SEPARATOR = "UserSeparator"; //$NON-NLS-1$
	
	/** 
	 * The cool bar control; <code>null</code> before creation
	 * and after disposal.
	 */
	private CoolBar coolBar = null;	
	
	/** 
	 * MenuManager for cool bar pop-up menu, or null if none.
	 */
	private MenuManager contextMenuManager = null;
	
	/**
	 * The original creation order of the contribution items.
	 */
	private ArrayList cbItemsCreationOrder = new ArrayList();
	
	/**
	 * Creates a new cool bar manager with the default style.
	 * Equivalent to <code>CoolBarManager(SWT.NONE)</code>.
	 */
	public CoolBarManager() {
		// do nothing
	}
	
	/**
	 * Creates a cool bar manager with the given SWT style.
	 * Calling <code>createControl</code> will create the cool bar control.
	 *
	 * @param style the cool bar item style; see 
	 * {@link org.eclipse.swt.widgets.CoolBar CoolBar} for for valid style bits
	 */
	public CoolBarManager(int style) {
		itemStyle= style;
	}
	
	/**
	 * Creates a cool bar manager for an existing cool bar control.
	 * This manager becomes responsible for the control, and will
	 * dispose of it when the manager is disposed.
	 *
	 * @param coolBar the cool bar control
	 */
	public CoolBarManager(CoolBar coolBar) {
		this();
		Assert.isNotNull(coolBar);
		this.coolBar = coolBar;
		itemStyle = coolBar.getStyle();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#add(org.eclipse.jface.action.IToolBarManager)
	 */
	public void add(IToolBarManager toolBarManager) {
		Assert.isNotNull(toolBarManager);
		super.add(new ToolBarContributionItem(toolBarManager));
	}
	
	/**
	 * Creates and returns this manager's cool bar control. 
	 * Does not create a new control if one already exists.
	 *
	 * @param parent the parent control
	 * @return the cool bar control
	 */
	public CoolBar createControl(Composite parent) {
		Assert.isNotNull(parent);
		if (!coolBarExist()) {
			coolBar = new CoolBar(parent, itemStyle);
			coolBar.setMenu(getContextMenuControl());
			coolBar.setLocked(false);
			coolBar.addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event event) {
					coolBar.getParent().layout();
				}
			});
			update(false);
		}
		return coolBar;
	}
	
	/**
	 * Subclasses may extend this <code>ContributionManager</code> method, but
	 * must call <code>super.itemAdded</code>.
	 * 
	 * @see org.eclipse.jface.action.ContributionManager#itemAdded(org.eclipse.jface.action.IContributionItem)
	 */
	protected void itemAdded(IContributionItem item) {
		Assert.isNotNull(item);
		super.itemAdded(item);
		int insertedAt = indexOf(item);
		cbItemsCreationOrder.add(Math.min(Math.max(insertedAt, 0), cbItemsCreationOrder.size()),item);
	}
	
	/**
	 * Restores the canonical order of this cool bar manager. The canonical order
	 * is the order in which the contribution items where added.
	 */
	public void resetLayout() {
		for (ListIterator iterator=cbItemsCreationOrder.listIterator();iterator.hasNext();) {
			IContributionItem item = (IContributionItem)iterator.next();
			// if its a user separator then do not include in original order.
			if ( (item.getId() != null) && (item.getId().equals(USER_SEPARATOR)) ) {
				iterator.remove();
			}
		}
		setLayout(cbItemsCreationOrder);
	}
	
	/**
	 * Disposes of this cool bar manager and frees all allocated SWT resources.
	 * Notifies all contribution items of the dispose. Note that this method does
	 * not clean up references between this cool bar manager and its associated
	 * contribution items. Use <code>removeAll</code> for that purpose.
	 */
	public void dispose() {
		if (coolBarExist()) {
			IContributionItem[] items = getItems();
			for (int i=0; i<items.length; i++) {
				// Disposes of the contribution item.
				// If Contribution Item is a toolbar then it will dispose of all the nested
				// contribution items.
				items[i].dispose();
			}
			coolBar.dispose();
			coolBar = null;
		}
		// If a context menu existed then dispose of it.
		if (contextMenuManager != null) {
			contextMenuManager.dispose();
			contextMenuManager = null;
		}
		
	}
	
	/**
	 * Returns whether the cool bar control has been created
	 * and not yet disposed.
	 * 
	 * @return <code>true</code> if the control has been created
	 *	and not yet disposed, <code>false</code> otherwise
	 */
	private boolean coolBarExist() {
		return coolBar != null && !coolBar.isDisposed(); 
	}
	
	/**
	 * Returns the cool bar control for this manager.
	 *
	 * @return the cool bar control, or <code>null</code> if none
	 */
	public CoolBar getControl() {
		return coolBar;
	}
	
	/**
	 * Finds the cool item associated with the given contribution item.
	 * 
	 * @param item the contribution item
	 * @return the associated cool item, or <code>null</code> if not found
	 */
	private CoolItem findCoolItem(IContributionItem item) {
		if (coolBar == null) return null;
		CoolItem[] items = coolBar.getItems();
		for (int i = 0; i < items.length; i++) {
			CoolItem coolItem = items[i];
			IContributionItem data = (IContributionItem)coolItem.getData();
			if (data != null && data.equals(item)) return coolItem;
		}
		return null;
	}
	
	
	/**
	 * Disposes the given cool item.
	 * 
	 * @param item the cool item to dispose
	 */
	private void dispose(CoolItem item) {
		if ((item != null) && !item.isDisposed()) {
			
			item.setData(null);
			Control control = item.getControl();
			// if the control is already disposed, setting the coolitem
			// control to null will cause an SWT exception, workaround
			// for 19630
			if ((control != null) && !control.isDisposed()) {
				item.setControl(null);
			}
			item.dispose();
		}
	}

	/**
	 * Subclasses may extend this <code>ContributionManager</code> method, but
	 * must call <code>super.itemRemoved</code>.
	 * 
	 * @see org.eclipse.jface.action.ContributionManager#itemRemoved(org.eclipse.jface.action.IContributionItem)
	 */
	protected void itemRemoved(IContributionItem item) {
		Assert.isNotNull(item);
		super.itemRemoved(item);
		CoolItem coolItem = findCoolItem(item);
		if (coolItem != null) {
			coolItem.setData(null);
		}
	}
	
	/**
	 * Sets the tab order of the coolbar to the visual order of its items.
	 */
	/* package */ void updateTabOrder() {
		CoolItem[] items = coolBar.getItems();
		Control[] children = new Control[items.length];
		for (int i = 0; i < children.length; i++) {
			if (items[i].getControl() != null) {
				children[i] = items[i].getControl();
			}
		}
		coolBar.setTabList(children);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#getStyle()
	 */
	public int getStyle() {
		return itemStyle;
	}
	
	/**
	 * Return a consistent set of wrap indices.  The return value
	 * will always include at least one entry and the first entry will 
	 * always be zero.  CoolBar.getWrapIndices() is inconsistent 
	 * in whether or not it returns an index for the first row.
	 * 
	 * @param wraps the wrap indicies from the cool bar widget
	 * @return the adjusted wrap indicies.
	 */
	private int[] getAdjustedWrapIndices(int[] wraps) {
		int[] adjustedWrapIndices;
		if (wraps.length == 0) {
			adjustedWrapIndices = new int[] { 0 };
		} else {
			if (wraps[0] != 0) {
				adjustedWrapIndices = new int[wraps.length + 1];
				adjustedWrapIndices[0] = 0;
				for (int i = 0; i < wraps.length; i++) {
					adjustedWrapIndices[i + 1] = wraps[i];
				}
			} else {
				adjustedWrapIndices = wraps;
			}
		}
		return adjustedWrapIndices;
	}
	
	/**
	 * Returns an array list of all the contribution items in the manager. 
	 * @return an array list of contribution items.
	 */
	private ArrayList getItemList() {
		IContributionItem[] cbItems = getItems();
		ArrayList list = new ArrayList(cbItems.length);
		for(int i=0; i < cbItems.length; i++) {
			list.add(cbItems[i]);
		}
		return list;
	}
	
	/**
	 * Positions the list iterator to the starting of the next row. By calling
	 * next on the returned iterator, it will return the first element of the
	 * next row.
	 * @param iterator the list iterator of contribution items
	 */
	private void nextRow(ListIterator iterator, boolean ignoreCurrentItem) {
		
		IContributionItem currentElement = null;
		if (!ignoreCurrentItem && iterator.hasPrevious()) {
			currentElement = (IContributionItem)iterator.previous();
			iterator.next();
		}
		
		if ((currentElement != null) && (currentElement.isSeparator() )) {
			collapseSeparators(iterator);
			return;
		}
		else {
			//Find next separator
			while (iterator.hasNext()) {
				IContributionItem item = (IContributionItem)iterator.next();
				if (item.isSeparator()) {
					// we we find a separator, collapse any consecutive separators
					// and return
					collapseSeparators(iterator);
					return;
				}
			}
		}
	}
	
	/**
	 * Relocates the given contribution item to the specified index.
	 * 
	 * @param cbItem the conribution item to relocate
	 * @param index the index to locate this item
	 * @param contributionList the current list of conrtributions
	 * @param itemLocation
	 */
	private void relocate(IContributionItem cbItem, int index, ArrayList contributionList, HashMap itemLocation) {
		
		if (!(itemLocation.get(cbItem) instanceof Integer)) return;
		int targetRow = ((Integer)itemLocation.get(cbItem)).intValue();
		
		int cbInternalIndex = contributionList.indexOf(cbItem);	

		//	by default add to end of list
		int insertAt = 	contributionList.size();	
		// Find the row to place this item in.
		ListIterator iterator = contributionList.listIterator();
		// bypass any separators at the begining
		collapseSeparators(iterator);
		int currentRow=-1;
		while (iterator.hasNext()) {				
			
			currentRow++;
			if (currentRow == targetRow) {
				// We found the row to insert the item
				int virtualIndex = 0;
				insertAt = iterator.nextIndex();
				// first check the position of the current element (item)
				// then get the next element
				while(iterator.hasNext()) {
					IContributionItem item = (IContributionItem)iterator.next();
					Integer itemRow = (Integer)itemLocation.get(item);
					if (item.isSeparator()) break;
					// if the item has an associate widget
					if ( (itemRow != null) && (itemRow.intValue() == targetRow) ) {
						// if the next element is the index we are looking for
						// then break
						if (virtualIndex >= index) break;
						virtualIndex++;
						
					}
					insertAt++;
				}
				// If we don't need to move it then we return
				if (cbInternalIndex == insertAt) return;
				break;
			}
			nextRow(iterator, true);	
		}
		contributionList.remove(cbItem);
		
		// Adjust insertAt index
		if (cbInternalIndex < insertAt) {
			insertAt--;
		}
		
		// if we didn't find the row then add a new row
		if (currentRow != targetRow) {
			contributionList.add(new Separator(USER_SEPARATOR));
			insertAt = contributionList.size();
		}
		insertAt = Math.min(insertAt, contributionList.size());
		contributionList.add(insertAt,cbItem);
			
	}
	
	/**
	 * Positions the list iterator to the end of all the separators. Calling
	 * <code>next()</code> the iterator should return the immediate object following the last
	 * separator.
	 * @param iterator the list iterator.
	 */
	private void collapseSeparators(ListIterator iterator) {
		
		while (iterator.hasNext()) {
			IContributionItem item = (IContributionItem)iterator.next();
			if (!item.isSeparator()) {
				iterator.previous();
				return;
			}
		}
	}
	
	/**
	 * Colapses consecutive separators and removes a separator from the beginning and end of
	 * the list.
	 * @param contributionList the list of contributions
	 */
	private ArrayList adjustContributionList(ArrayList contributionList) {
		IContributionItem item;
		// Fist remove a separator if it is the first element of the list
		if (contributionList.size() != 0) {
			item = (IContributionItem)contributionList.get(0);
			if (item.isSeparator()) {
				contributionList.remove(0);
			}
		
			ListIterator iterator = contributionList.listIterator();
			// collapse consecutive separators
			while (iterator.hasNext()) {
				item = (IContributionItem)iterator.next();
				if (item.isSeparator()) {
					while (iterator.hasNext()) {
						item = (IContributionItem)iterator.next();
						if (item.isSeparator()) {
							iterator.remove();
						}else {
							break;
						}
					}
		
				}
			}
			// Now check last element to see if there is a separator
			item = (IContributionItem)contributionList.get(contributionList.size()-1);
			if (item.isSeparator()) {
				contributionList.remove(contributionList.size() - 1);
			}
		}
		return contributionList;
		
	}
	
	/**
	 * Synchronizes the visual order of the cool items in the control with 
	 * this manager's internal data structures. This method should be called before 
	 * requesting the order of the contribution items to ensure that the order is
	 * accurate.
	 * <p>
	 * Note that <code>update()</code> and <code>refresh()</code> are converses:
	 * <code>update()</code> changes the visual order to match the internal
	 * structures, and <code>refresh</code> changes the internal structures to
	 * match the visual order.
	 * </p>
	 */
	public void refresh() {
		try {
		// Retreives the list of contribution items as an array list
		ArrayList contributionList = getItemList();
		
		// Check the size of the list
		if (contributionList.size() == 0) return;
		
		// The list of all the cool items in their visual order
		CoolItem[] coolItems = coolBar.getItems();
		// The wrap indicies of the coolbar
		int[] wrapIndicies = getAdjustedWrapIndices(coolBar.getWrapIndices());
		
		int row = 0;
		int lastRow = (wrapIndicies.length - 1);
		int nextRow = row+1;
		int coolItemIndex=0;
		
		// Traverse through all cool items in the coolbar add them to a new data structure
		// in the correct order
		ArrayList displayedItems = new ArrayList(coolBar.getItemCount());
		for (int i=0; i < coolItems.length; i++) {
			CoolItem coolItem = coolItems[i];
			if (coolItem.getData() instanceof IContributionItem) {
				IContributionItem cbItem = (IContributionItem)coolItem.getData();
				displayedItems.add(Math.min(i,displayedItems.size()),cbItem);
			}
		}
		
		// Add separators to the displayed Items data structure
		int offset = 0;
		for(int i=1; i < wrapIndicies.length; i++) {
			int insertAt = wrapIndicies[i] + offset;
			displayedItems.add(insertAt,new Separator(USER_SEPARATOR));
			offset++;
		}
		
		// Determine which rows are invisible
		ArrayList existingVisibleRows = new ArrayList(4);
		ListIterator rowIterator=contributionList.listIterator();
		collapseSeparators(rowIterator);
		int numRow = 0;
		while(rowIterator.hasNext()) {
			// Scan row
			while (rowIterator.hasNext()) {
				IContributionItem cbItem = (IContributionItem)rowIterator.next();
				if (displayedItems.contains(cbItem)) {
					existingVisibleRows.add(new Integer(numRow));
					break;
				}
				if (cbItem.isSeparator()) {
					break;
				}
			}
			nextRow(rowIterator, false);
			numRow++;
		}
		
		Iterator existingRows = existingVisibleRows.iterator();
		// Adjust row number to the first visible
		if (existingRows.hasNext()) {
			row = ((Integer)existingRows.next()).intValue();
		}
		
		HashMap itemLocation = new HashMap();
		for(ListIterator locationIterator=displayedItems.listIterator();locationIterator.hasNext();) {
			IContributionItem item = (IContributionItem)locationIterator.next();
			if (item.isSeparator()) {
				if (existingRows.hasNext()) {
					Integer value = (Integer)existingRows.next();
					row = value.intValue();
				}else {
					row++;
				}
			}else {
				itemLocation.put(item,new Integer(row));
			}
			
		}
		
		// Insert the contribution items in their correct location
		for(ListIterator iterator=displayedItems.listIterator();iterator.hasNext();) {
			IContributionItem cbItem = (IContributionItem)iterator.next();
			if (cbItem.isSeparator()) {
				coolItemIndex=0;
			}else {
				relocate(cbItem, coolItemIndex, contributionList, itemLocation);
				cbItem.saveWidgetState();
				coolItemIndex++;
			}
		}
	
		// Print out the contribution list
		if (contributionList.size() != 0) {
			contributionList = adjustContributionList(contributionList);
			IContributionItem[] array = new IContributionItem[contributionList.size()-1];
			array = (IContributionItem[])contributionList.toArray(array);
			internalSetItems(array);
		}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	/*
	 * Used for debuging. Prints all the items in the internal structures.
	 */
	private void printContributions(ArrayList contributionList) {
		int index = 0;
		System.out.println("----------------------------------\n"); //$NON-NLS-1$
		for (Iterator i = contributionList.iterator();i.hasNext();index++) {
			IContributionItem item = (IContributionItem)i.next();
			if (item.isSeparator()) {
				System.out.println("Separator"); //$NON-NLS-1$
			}else  {
				System.out.println(index + ". Item id: " + item.getId() + " - is Visible: " + item.isVisible());  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
	}
	

	/**
	 * Subclasses may extend this <code>IContributionManager</code> method, but
	 * must call <code>super.update</code>.
	 * 
	 * @see org.eclipse.jface.action.IContributionManager#update(boolean)
	 */
	public void update(boolean force) {
		if (isDirty() || force) {
			if (coolBarExist()) {
				boolean useRedraw = false;
				boolean relock = false;
				boolean changed = false;
				try {
					
					coolBar.setRedraw(false);
					
					//1. Refresh the widget data with the internal data structure
					refresh();

					// 2: Fill in all the contribution items that do not have widgets
					int fillIndex = 0;
					boolean incrementCount = true;
					CoolItem[] coolItems = coolBar.getItems();	
					IContributionItem[] contributionItems = this.getItems();
					for (int i=0; i < contributionItems.length; i++) {
						contributionItems[i].update(ICoolBarManager.VISIBILITY);
						// only valid contribution items a.k.a. no place markers, no group markers or separators and only visible contribution items
						if ((contributionItems[i].isVisible()) && (!contributionItems[i].isGroupMarker()) 
							&& (!contributionItems[i].isSeparator())) {
							incrementCount = true;
							// Find the widget that is associated with this contribution item
							CoolItem item = findCoolItem(contributionItems[i]);
							if (item == null) {
								int prevItemCount = coolBar.getItemCount();
								if (changed = false) {
									if (coolBar.getLocked()) {
										// workaround for 14330
										coolBar.setLocked(false);
										relock = true;
									}
									changed = true;
								}
								// guarantee fillIndex < coolBar.getItemCount()
								fillIndex = Math.min(fillIndex,coolBar.getItemCount());
								contributionItems[i].fill(coolBar,fillIndex);
								
								if (prevItemCount >= coolBar.getItemCount()) {
									incrementCount = false;
								}
							}
							// This index is needed so that we don't count invisible items and items
							// that don't fill anything.
							if (incrementCount) {
								fillIndex++;
							}
						}
					}
					
					// 3: Remove widgets without contribution items
					for (int coolItemIndex=0; coolItemIndex < coolItems.length; coolItemIndex++) {
						CoolItem coolItem = coolItems[coolItemIndex];
						IContributionItem foundItem = (IContributionItem)coolItems[coolItemIndex].getData();
						
						// Dispose of widget if not needed
						if ((foundItem == null) || (!foundItem.isVisible())) {	
							// dispose the widget
							dispose(coolItem);
							changed = true;
						}
						
					}
					
					// 4. Handle wrap indicies
					int numRows = getNumRows(contributionItems)-1;
					int[] wrapIndicies = new int[numRows];
					int j = 0;
					boolean foundSeparator = false;
					for (int i=0; i < contributionItems.length; i++) {
						IContributionItem item = contributionItems[i];
						CoolItem coolItem = findCoolItem(item);
						if (item.isSeparator()) {
							foundSeparator = true;
						}
						if ((!item.isSeparator()) && (!item.isGroupMarker()) && (item.isVisible()) && (coolItem != null) && (foundSeparator)) {
							wrapIndicies[j] = coolBar.indexOf(coolItem);
							j++;
							foundSeparator=false;
						}
					}
					// Set the new wrap indicies
					coolBar.setWrapIndices(wrapIndicies);
					
					
					// 5. update the sizes
					for (int i=0; i < contributionItems.length; i++) {
						IContributionItem item = contributionItems[i];
						item.update(SIZE);
					}
					
					// if the coolBar was previously locked then lock it
					if (relock) coolBar.setLocked(true);
					
					if (changed) {
						updateTabOrder();
					}
					setDirty(false);
				} finally {
					coolBar.setRedraw(true);
				}
				
			} // if (coolBarExist())
		}// if (isDirty() || force) 
	}
	
	/**
	 * Returns the number of rows that should be displayed visually.
	 * 
	 * @param items the array of contributin items
	 * @return the number of rows
	 */
	private int getNumRows(IContributionItem[] items) {
		int numRows = 1;
		boolean separatorFound = false;
		for (int i=0; i < items.length; i++) {
			if (items[i].isSeparator()) {
				separatorFound = true;
			}
			if ((separatorFound) && (items[i].isVisible()) && (!items[i].isGroupMarker()) && (!items[i].isSeparator()) ) {
				numRows++;
				separatorFound = false;
			}
		}
		return numRows;
	}
	
	/**
	 * Returns the control of the Menu Manager. If the menu manager does not have a control
	 * then one is created.
	 * 
	 * @return menu control associated with manager, or null if none
	 */
	private Menu getContextMenuControl() {
		if ((contextMenuManager != null) && (coolBar != null) ) {			
			Menu menuWidget = contextMenuManager.getMenu();
			if ((menuWidget == null) || (menuWidget.isDisposed())) {
				menuWidget = contextMenuManager.createContextMenu(coolBar);
			}
			return menuWidget;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#isLayoutLocked()
	 */
	public IMenuManager getContextMenuManager() {
		return contextMenuManager;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#setContextMenuManager(org.eclipse.jface.action.IMenuManager)
	 */
	public void setContextMenuManager(IMenuManager contextMenuManager) {
		this.contextMenuManager = (MenuManager)contextMenuManager;
		if (coolBar != null) {
			coolBar.setMenu(getContextMenuControl());
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#isLayoutLocked()
	 */
	public boolean getLockLayout() {
		if (!coolBarExist()) {
			return false;
		}
		return coolBar.getLocked();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.action.ICoolBarManager#lockLayout(boolean)
	 */
	public void setLockLayout(boolean value) {
		if (!coolBarExist()) {
			return;
		}
		coolBar.setLocked(value);
	}
	
	/**
	 * Replaces the internal data structure with the given new order. Then force
	 * and update.
	 * @param newLayout a list of new order of contribution items.
	 */
	public void setLayout(ArrayList newLayout) {
		IContributionItem[] newItems = new IContributionItem[0];
		newItems = (IContributionItem[])newLayout.toArray(newItems);
		// dispose of all the cool items on the cool bar manager
		if (coolBar != null) {
			CoolItem[] coolItems = coolBar.getItems();
			for (int i=0; i < coolItems.length; i++) {
				dispose(coolItems[i]);
			}
		}
		// Set the internal structure to this order
		internalSetItems(newItems);
		// Force and update
		update(true);
	}
}
