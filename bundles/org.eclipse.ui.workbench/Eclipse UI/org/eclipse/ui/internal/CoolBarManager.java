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

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.ActionSetRegistry;

/**
 */
public class CoolBarManager extends ContributionManager implements IToolBarManager {
	/** 
	 * The cool bar style; <code>SWT.NONE</code> by default.
	 */
	private int style = SWT.NONE;

	/** 
	 * The cool bar control; <code>null</code> before creation
	 * and after disposal.
	 */
	private CoolBar coolBar = null;

	/** 
	 * MenuManager for chevron menu when CoolItems not fully displayed.
	 */
	private MenuManager chevronMenuManager;
	
	/** 
	 * MenuManager for coolbar popup menu
	 */
	private MenuManager coolBarMenuManager = new MenuManager();
	private Menu coolBarMenu = null;
	
	/**
	 * CoolBarLayout to use when restoring coolitems.
	 */
	private CoolBarLayout rememberedLayout;
	
	private class RestoreItemData {
		String beforeItemId; // found afterItemId, restore item after this item
		String afterItemId; // found beforeItemId, restore item before this item
		int beforeIndex = -1; // index in current layout of beforeItemId
		int afterIndex = -1; // index in current layout of afterItemId
		
		RestoreItemData() {
		}
	}
	/**
	 */
	public CoolBarManager() {
	}
	/**
	 */
	public CoolBarManager(int style) {
		this.style = style;
	}
	/**
	 * Adds an action as a contribution item to this manager.
	 * Equivalent to <code>add(new ActionContributionItem(action))</code>.
	 * 
	 * Not valid for CoolBarManager.  Only CoolBarContributionItems may be added
	 * to this manager.
	 * 
	 * @param action the action
	 */
	public void add(IAction action) {
		Assert.isTrue(false);
	}
	/**
	 * Adds a CoolBarContributionItem to this manager.
	 * 
	 * @exception AssertionFailedException if the type of item is
	 * not valid
	 */
	public void add(IContributionItem item) {
		Assert.isTrue(item instanceof CoolBarContributionItem);
		super.add(item);
	}
	/**
	 * Adds a contribution item to the coolbar's menu.
	 */
	public void addToMenu(ActionContributionItem item) {
		coolBarMenuManager.add(item.getAction());
	}
	/**
	 */
	private boolean coolBarExist() {
		return coolBar != null && !coolBar.isDisposed();
	}
	/**
	 */
	/* package */ CoolBar createControl(Composite parent) {
		if (!coolBarExist() && parent != null) {
			// Create the CoolBar and its popup menu.
			coolBar = new CoolBar(parent, style);
			coolBar.setLocked(false);
			coolBar.addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event event) {
					coolBar.getParent().layout();
				}
			});
			coolBar.setMenu(getCoolBarMenu());
		}
		return coolBar;
	}
	/**
	 * Create the coolbar item for the given contribution item.
	 */
	private CoolItem createCoolItem(CoolBarContributionItem cbItem, ToolBar toolBar) {
		CoolItem coolItem;
		toolBar.setVisible(true);
		RestoreItemData data = getRestoreData(cbItem);		
		if (data != null) {
			coolItem = createRememberedCoolItem(cbItem, toolBar, data);
		} else {
			coolItem = createNewCoolItem(cbItem, toolBar);
		}
		coolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail == SWT.ARROW) {
					handleChevron(event);
				}
			}
		});	
		return coolItem;
	}
	/**
	 * Create a new coolbar item for the given contribution item.  Put the item in its original 
	 * creation position.
	 */
	private CoolItem createNewCoolItem(CoolBarContributionItem cbItem, ToolBar toolBar) {
		CoolItem coolItem;
		int index = -1;
		// when a coolitem is initially created, we specifically order it based on its 
		// position in the the contributions list.
		index = getInsertIndex(cbItem);
		if (index == -1) {
			index = coolBar.getItemCount();
			coolItem = new CoolItem(coolBar, SWT.DROP_DOWN);
		} else {
			coolItem = new CoolItem(coolBar, SWT.DROP_DOWN, index);
		}
		coolItem.setControl(toolBar);
		coolItem.setData(cbItem);
		setSizeFor(coolItem);
					
		return coolItem;
	}
	/**
	 * Create a new coolbar item for the given contribution item.  Put the item in its rememberedl 
	 * position.
	 */
	private CoolItem createRememberedCoolItem(CoolBarContributionItem cbItem, ToolBar toolBar, RestoreItemData data) {		
		int rememberedItemRow = -1;	// row index of the item in the remembered layout
		int rememberedAfterRow = -1;	// row index of the after item in the remembered layout
		int currentAfterRow = -1;	// row index of the after item in the current layout
		int rememberedBeforeRow = -1;	// row index of the before item in the remembered layout
		int currentBeforeRow = -1;	// row index of the before item in the current layout
		
		CoolBarLayout currentLayout = getLayout();
		rememberedItemRow = rememberedLayout.getRowOf(cbItem.getId());
		if (data.afterItemId != null) {
			rememberedAfterRow = rememberedLayout.getRowOf(data.afterItemId);
			currentAfterRow = currentLayout.getRowOfIndex(data.afterIndex);
		}
		if (data.beforeItemId != null) {
			rememberedBeforeRow = rememberedLayout.getRowOf(data.beforeItemId);
			currentBeforeRow = currentLayout.getRowOfIndex(data.beforeIndex);
		}
		
		int createIndex = -1;
		int row;
		int[] newWraps = null;
		
		// Figure out where to place the item and how to adjust the wrap indices.
		// When adding the item at the afterIndex, wrap indices may need to be 
		// adjusted if the index represents the beginning of a row in the current
		// coolbar layout.
		if (data.afterIndex != -1 && data.beforeIndex != -1) {
			// both a beforeItem and afterItem were found in the current coolbar layout
			// for the item to be added
			if ((rememberedItemRow == rememberedAfterRow) && (rememberedItemRow == rememberedBeforeRow)) {
				// in the remembered layout the item was on the same row as both the beforeItem
				// and the afterItem, compare this to the current coolbar layout
				if (currentAfterRow == currentBeforeRow) {
					// in the current layout, both the before and after item are on the
					// same row, so add the item to this row
					createIndex = data.beforeIndex + 1;
					row = currentBeforeRow;
				} else {
					// in the current layout, both the before and after item are not on
					// the same row
					if (currentBeforeRow == rememberedBeforeRow) {
						// the beforeItem is on the same row as in the remembered layout
						createIndex = data.beforeIndex + 1;
						row = currentBeforeRow;
					} else if (currentAfterRow == rememberedAfterRow) {
						// the afterItem is on the same row as in the remembered layout
						createIndex = data.afterIndex;
						row = currentAfterRow;
						newWraps = currentLayout.wrapsForNewItem(row, createIndex);
					} else {
						// current layout rows are different than when the item 
						// was deleted, just add the item to the currentBeforeRow
						createIndex = data.beforeIndex + 1;
						row = currentBeforeRow;
					}
				}
			} else if (rememberedItemRow == rememberedBeforeRow) {
				// in the remembered layout, the item was on the same row as the beforeItem,
				// add the item to the before row
				createIndex = data.beforeIndex + 1;
				row = currentBeforeRow;
			} else if (rememberedItemRow == rememberedAfterRow) {
				// in the remembered layout, the item was on the same row as the afterItem
				// add the item to the currentAfterRow
				createIndex = data.afterIndex;
				row = currentAfterRow;
				newWraps = currentLayout.wrapsForNewItem(row, createIndex);
			} else {
				// in the remembered layout, the item was on a row by itself
				// put the item on a row by itself after currentBeforeRow
				row = currentBeforeRow + 1;
				createIndex = currentLayout.getStartIndexOfRow(row);
 				if (createIndex == -1) {
 					// row does not exist
 					createIndex = coolBar.getItemCount();
 				}
				newWraps = currentLayout.wrapsForNewRow(row, createIndex);
			}
		} else if (data.afterIndex != -1) {
			// only an afterItem was found in the current coolbar layout
			// for the item to be added
			createIndex = data.afterIndex;
			if (rememberedItemRow == rememberedAfterRow) {
				// in the remembered layout, the item was on the same row as the afterItem, 
				// put the item on the currentAfterRow 
				row = currentAfterRow;
				newWraps = currentLayout.wrapsForNewItem(row, createIndex);
			} else {
				// in the remembered layout, the item was not on the same row as the
				//  afterItem, create a new row before currentAfterRow
				row = currentAfterRow;
				createIndex = currentLayout.getStartIndexOfRow(row);
 				newWraps = currentLayout.wrapsForNewRow(row, createIndex);
			}
		} else if (data.beforeIndex != -1) {
			// only a beforeItem was found in the current coolbar layout
			// for the item to be added
			createIndex = data.beforeIndex + 1;
			if (rememberedItemRow == rememberedBeforeRow) {
				// in the remembered layout, the item was on the same row as the beforeItem, 
				// put the item on currentBeforeRow
				row = currentBeforeRow;
				newWraps = currentLayout.wrapsForNewItem(row, createIndex);
			} else {
				// in the remembered layout, the item was not on the same row as the 
				// beforeItem, create a new row with the item after currentBeforeRow
				row = currentBeforeRow + 1;
 				createIndex = currentLayout.getStartIndexOfRow(row);
 				if (createIndex == -1) {
 					// row does not exist
 					createIndex = coolBar.getItemCount();
					newWraps = currentLayout.wrapsForNewRow(row, createIndex);
 				} else {
 					newWraps = currentLayout.wrapsForNewItem(row, createIndex);
 				}
			}
		} else {
			// neither a before or after item was found in the current coolbar
			// layout, just add the item to the end of the coolbar
			createIndex = coolBar.getItemCount();
		}

		// create the item
		if (newWraps != null) {
			// item will be added and then the wraps will set, so use setRedraw(false)
			// since the position of the item will change
			coolBar.setRedraw(false);
		}
		CoolItem coolItem = new CoolItem(coolBar, SWT.DROP_DOWN, createIndex);
		coolItem.setControl(toolBar);
		coolItem.setData(cbItem);
		if (newWraps != null)	{
			coolBar.setWrapIndices(newWraps);
		}
		// must call setSize after wrap indices are set, otherwise the size may not take affect appropriately
		setSizeFor(coolItem);
		if (newWraps != null)	{
			coolBar.setRedraw(true);
		}
		updateRememberedLayout();
		return coolItem;
	}	
	/**
	 */
	private void dispose(CoolBarContributionItem cbItem) {
		CoolItem coolItem = findCoolItem(cbItem);
		if (coolItem != null) {
			dispose(coolItem);
		}
		remove(cbItem);
 		cbItem.getToolBarManager().dispose();
	}
	/**
	 */
	private void dispose(CoolItem coolItem) {
		if ((coolItem != null) && !coolItem.isDisposed()) {
			CoolBarContributionItem cbItem = (CoolBarContributionItem)coolItem.getData();
			if (cbItem != null ) rememberLayoutFor(cbItem.getId());
			coolItem.setData(null);
			Control control = coolItem.getControl();
			// if the control is already disposed, setting the coolitem
			// control to null will cause an SWT exception, workaround
			// for 19630
			if ((control != null) && !control.isDisposed()) {
				coolItem.setControl(null);
			}
			coolItem.dispose();
		}
	}		
	/**
	 */
	/* package */ void dispose() {
		if (coolBarExist()) {
			IContributionItem[] cbItems = getItems();
			for (int i=0; i<cbItems.length; i++) {
				CoolBarContributionItem cbItem = (CoolBarContributionItem)cbItems[i];
				dispose(cbItem);
				cbItem.dispose();
			}
			coolBar.dispose();
			coolBar = null;
		}
		if (chevronMenuManager != null) {
			chevronMenuManager.dispose();
			chevronMenuManager = null;
		}
		if (coolBarMenuManager != null) {
			coolBarMenuManager.dispose();
			coolBarMenuManager = null;
		}
	}
	/**
	 */
	private CoolItem findCoolItem(CoolBarContributionItem item) {
		if (coolBar == null) return null;
		CoolItem[] items = coolBar.getItems();
		for (int i = 0; i < items.length; i++) {
			CoolItem coolItem = items[i];
			CoolBarContributionItem data = (CoolBarContributionItem)coolItem.getData();
			if (data != null && data.equals(item)) return coolItem;
		}
		return null;
	}
	/**
	 */
	/* package */ CoolBarContributionItem findSubId(String id) {
		IContributionItem[] items = getItems();
		for (int i = 0; i < items.length; i++) {
			CoolBarContributionItem item = (CoolBarContributionItem)items[i];
			IContributionItem subItem = item.getToolBarManager().find(id);
			if (subItem != null) return item;
		}
		return null;
	}
	/**
	 */
	private ArrayList getCoolItemIds() {
		CoolItem[] coolItems = coolBar.getItems();
		ArrayList ids = new ArrayList(coolItems.length);
		for (int i = 0; i < coolItems.length; i++) {
			CoolBarContributionItem group = (CoolBarContributionItem) coolItems[i].getData();
			if (group != null) ids.add(group.getId());
		}
		return ids;
	}
	/**
	 */
	/* package */ Menu getCoolBarMenu() {
		if (coolBarMenu == null) {
			coolBarMenu = coolBarMenuManager.createContextMenu(coolBar);
		}
		return coolBarMenu;
	}
	/**
	 * Return the SWT control for this manager.
	 */
	/* package */ CoolBar getControl() {
		return coolBar;
	}
	private int getInsertIndex(CoolBarContributionItem coolBarItem) {
		IContributionItem[] items = getItems();
		int index = -1;
		CoolBarContributionItem afterItem = null;
		// find out which item should be after this item
		for (int i=0; i<items.length; i++) {
			if (items[i].equals(coolBarItem)) {
				if (i > 0) {
					while (i > 0) {
						afterItem = (CoolBarContributionItem)items[i-1];
						if (afterItem.isVisible()) break;
						i--;
					}
				} else {
					// item is not after anything
					index = 0;
				}
				break;
			}
		}
		// get the coolbar location of the after item
		if (afterItem != null) {
			CoolItem afterCoolItem = findCoolItem(afterItem);
			if (afterCoolItem != null) {
				index = coolBar.indexOf(afterCoolItem);
				index++;
			}
		}
		return index;
	}
	/**
	 */
	private CoolBarLayout getLayout() {
		if (!coolBarExist())
			return null;	
		CoolBarLayout layout = new CoolBarLayout(coolBar);
		return layout;
	}
	private RestoreItemData getRestoreData(CoolBarContributionItem cbItem) {
		if (rememberedLayout == null) return null;
		
		// see if the item is in the remembered layout, if not there is no information to use
		// to restore the item's position
		CoolBarLayout currentLayout = getLayout();
		ArrayList coolBarItems = currentLayout.items;	
		ArrayList rememberedItems = rememberedLayout.items;
		int rememberedItemIndex = rememberedItems.indexOf(cbItem.getId());
		if (rememberedItemIndex == -1) return null;

		RestoreItemData data = new RestoreItemData();
		if (!currentLayout.isDerivativeOf(rememberedLayout)) return data;
		
		// Look for the place to put the item.  Compare the remembered layout with the
		// current layout.
		
		// Look at the remembered items after rememberedItemIndex, see if any of these items
		// is in the current coolbar layout
		for (int j = rememberedItemIndex + 1; j < rememberedItems.size(); j++) {
			//
			String afterId = (String)rememberedItems.get(j);
			int afterIndex = coolBarItems.indexOf(afterId);
			if (afterIndex != -1) {
				data.afterItemId = afterId;
				data.afterIndex = afterIndex;
				break;
			}
		}
		
		// Look at the remembered items before rememberedItemIndex, see if any of these items
		// is in the current coolbar layout
		for (int j = rememberedItemIndex - 1; j >= 0; j--) {
			//
			String beforeId = (String)rememberedItems.get(j);
			int beforeIndex = coolBarItems.indexOf(beforeId);
			if (beforeIndex != -1) {
				data.beforeItemId = beforeId;
				data.beforeIndex = beforeIndex;
				break;
			}
		}
		
		return data;
	}
	/* package */ int getStyle() {
		return style;
	}
	/**
	 * Create and display the chevron menu.
	 */
	private void handleChevron(SelectionEvent event) {
		CoolItem item = (CoolItem) event.widget;
		Control control = item.getControl();
		if ((control instanceof ToolBar) == false) {
			return;
		}
		
		Point chevronPosition = coolBar.toDisplay(new Point(event.x, event.y));
		ToolBar toolBar = (ToolBar) control;
		ToolItem[] tools = toolBar.getItems();
		int toolCount = tools.length;
		int visibleItemCount = 0;
		while (visibleItemCount < toolCount) {
			Rectangle toolBounds = tools[visibleItemCount].getBounds();
			Point point = toolBar.toDisplay(new Point(toolBounds.x, toolBounds.y));
			toolBounds.x = point.x;
			toolBounds.y = point.y;
			// stop if the tool is at least partially hidden by the drop down chevron
			if (chevronPosition.x >= toolBounds.x && chevronPosition.x - toolBounds.x <= toolBounds.width) {
				break;
			}
			visibleItemCount++;
		}

		// Create a pop-up menu with items for each of the hidden buttons.
		if (chevronMenuManager != null) {
			chevronMenuManager.dispose();
		}
		chevronMenuManager = new MenuManager();
		for (int i = visibleItemCount; i < toolCount; i++) {
			IContributionItem data = (IContributionItem) tools[i].getData();
			if (data instanceof ActionContributionItem) {
				ActionContributionItem contribution = new ActionContributionItem(((ActionContributionItem) data).getAction());
				chevronMenuManager.add(contribution);
			} else if (data instanceof SubContributionItem) {
				IContributionItem innerData = ((SubContributionItem)data).getInnerItem();
				if (innerData instanceof ActionContributionItem) {
					ActionContributionItem contribution = new ActionContributionItem(((ActionContributionItem) innerData).getAction());
					chevronMenuManager.add(contribution);
				}
			} else if (data.isSeparator()) {
				chevronMenuManager.add(new Separator());
			}
		}
		Menu popup = chevronMenuManager.createContextMenu(coolBar);
		popup.setLocation(chevronPosition.x, chevronPosition.y);
		popup.setVisible(true);
	}
	/**
	 * Inserts a contribution item for the given action after the item 
	 * with the given id.
	 * Equivalent to
	 * <code>insertAfter(id,new ActionContributionItem(action))</code>.
	 *
	 * Not valid for CoolBarManager.  Only CoolBarContributionItems may be added
	 * to this manager.
	 *
	 * @param id the contribution item id
	 * @param action the action to insert
	 */
	public void insertAfter(String id, IAction action) {
		Assert.isTrue(false);
	}
	/**
	 * Inserts a contribution item after the item with the given id.
	 *
	 * @param id the CoolBarContributionItem 
	 * @param item the CoolBarContributionItem to insert
	 * @exception IllegalArgumentException if there is no item with
	 *   the given id
	 * @exception IllegalArgumentException if the type of item is
	 * 	not valid
	 */
	public void insertAfter(String id, IContributionItem item) {
		Assert.isTrue(item instanceof CoolBarContributionItem);
		super.insertAfter(id, item);
	}
	/**
	 * Inserts a contribution item for the given action before the item 
	 * with the given id.
	 * Equivalent to
	 * <code>insertBefore(id,new ActionContributionItem(action))</code>.
	 *
	 * Not valid for CoolBarManager.  Only CoolBarContributionItems may be added
	 * to this manager.
	 *
	 * @param id the contribution item id
	 * @param action the action to insert
	 */
	public void insertBefore(String id, IAction action) {
		Assert.isTrue(false);
	}
	/**
	 * Inserts a contribution item before the item with the given id.
	 *
	 * @param id the CoolBarContributionItem 
	 * @param item the CoolBarContributionItem to insert
	 * @exception IllegalArgumentException if there is no item with
	 *   the given id
	 * @exception IllegalArgumentException if the type of item is
	 * 	not valid
	 */
	public void insertBefore(String id, IContributionItem item) {
		Assert.isTrue(item instanceof CoolBarContributionItem);
		super.insertBefore(id, item);
	}
	/**
	 */
	/* package */ boolean isLayoutLocked() {
		if (!coolBarExist()) return false;
		return coolBar.getLocked();
	}
	/* package */ boolean isValidCoolItemId(String id, WorkbenchWindow window) {
		ActionSetRegistry registry = WorkbenchPlugin.getDefault().getActionSetRegistry();
		if (registry.findActionSet(id) != null) return true;
		if (window != null) {
			return window.isWorkbenchCoolItemId(id);
		}
		return false;
	}
	/**
	 */
	/* package */ void lockLayout(boolean value) {
		coolBar.setLocked(value);
	}
	/**
	 */
	protected void resetLayout() {
		try {
			coolBar.setRedraw(false);
			coolBar.setWrapIndices(null);
			CoolItem[] coolItems = coolBar.getItems();
			CoolBarContributionItem[] cbItems = new CoolBarContributionItem[coolItems.length];
			for (int i = 0; i < coolItems.length; i++) {
				CoolItem coolItem = coolItems[i];
				cbItems[i] = (CoolBarContributionItem)coolItem.getData();
				dispose(coolItem);
			}
			rememberedLayout = null;
			update(true);
		} finally {
			coolBar.setRedraw(true);
		}
	}
	/**
	 * Removes the given contribution item from the contribution items
	 * known to this manager.
	 *
	 * @param item the contribution item
	 * @return the <code>item</code> parameter if the item was removed,
	 *   and <code>null</code> if it was not found
	 * @exception IllegalArgumentException if the type of item is
	 * 	not valid
	 */
	public IContributionItem remove(IContributionItem item) {
		Assert.isTrue(item instanceof CoolBarContributionItem);
		return super.remove(item);
	}
	/**
	 * Remember the coolbar layout for the current item to help when restoring the
	 * item.
	 */
	private void rememberLayoutFor(String cbItemId) {
		CoolBarLayout currentLayout = getLayout();
		if (rememberedLayout == null) {
			rememberedLayout = currentLayout;
			return;
		}
		// If the current layout resembles the remembered layout, do not change the
		// remembered layout.  We want to remember the layout with the most information.
		if (currentLayout.isDerivativeOf(rememberedLayout)) return;		
		rememberedLayout = currentLayout;
	}
	public boolean restoreState(IMemento memento) {
		Integer locked = memento.getInteger(IWorkbenchConstants.TAG_LOCKED);
		boolean state = (locked != null) && (locked.intValue() == 1);
		lockLayout(state);
		
		CoolBarLayout coolBarLayout = new CoolBarLayout();
		IMemento layoutMemento = memento.getChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
		if (layoutMemento != null) {
			coolBarLayout.restoreState(layoutMemento);
			int itemOrder[] = new int[coolBarLayout.itemSizes.length];
			for (int i=0; i < coolBarLayout.itemSizes.length; i++) {
				CoolBarContributionItem cbItem = (CoolBarContributionItem)find((String)coolBarLayout.items.get(i));
				CoolItem coolItem = findCoolItem(cbItem);
				int index = coolBar.indexOf(coolItem);
				itemOrder[i] = index;
			}
			coolBar.setItemLayout(itemOrder, coolBarLayout.itemWrapIndices, coolBar.getItemSizes());
			updateItemSizes();
		}
		layoutMemento = memento.getChild(IWorkbenchConstants.TAG_LAYOUT);
		if (layoutMemento != null) {
			rememberedLayout = new CoolBarLayout();
			rememberedLayout.restoreState(layoutMemento);
		} 
		return true;
	}
	IStatus saveState(IMemento memento) {
		int state = isLayoutLocked() ? 1 : 0;
		memento.putInteger(IWorkbenchConstants.TAG_LOCKED, state);
		
		CoolBarLayout layout = getLayout();
		IMemento childMem = memento.createChild(IWorkbenchConstants.TAG_TOOLBAR_LAYOUT);
		layout.saveState(childMem);

		if (rememberedLayout != null) {
			childMem = memento.createChild(IWorkbenchConstants.TAG_LAYOUT);
			rememberedLayout.saveState(childMem);
		}
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
	}
	/**
	 */
	private void setSizeFor(CoolItem coolItem) {
		ToolBar toolBar = (ToolBar) coolItem.getControl();
		Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point coolSize = coolItem.computeSize(size.x, size.y);
		// note setMinimumSize must be called before setSize, see PR 15565
		coolItem.setMinimumSize(size.x, size.y);
		coolItem.setPreferredSize(coolSize);
		coolItem.setSize(coolSize);
	}
	/**
	 */
	public void update(boolean force) {
		if (isDirty() || force) {
			if (coolBarExist()) {
				boolean useRedraw = false;
				try {
					CoolBarLayout layout = getLayout();
					// remove CoolBarItemContributions that are empty
					IContributionItem[] items = getItems();
					ArrayList cbItemsToRemove = new ArrayList(items.length);
					for (int i = 0; i < items.length; i++) {
						CoolBarContributionItem cbItem = (CoolBarContributionItem) items[i];
						if (cbItem.isEmpty()) {
							CoolItem coolItem = findCoolItem(cbItem);
							if (!useRedraw && coolItem != null) {
								int visualIndex = coolBar.indexOf(coolItem);
								if (layout.isOnRowAlone(visualIndex)) {
									useRedraw = true;
								}
							}						
							cbItemsToRemove.add(cbItem);
						}
					}
					
					// remove non-visible CoolBarContributionItems
					CoolItem[] coolItems = coolBar.getItems();
					ArrayList coolItemsToRemove = new ArrayList(coolItems.length);
					for (int i = 0; i < coolItems.length; i++) {
						CoolItem coolItem = coolItems[i];
						CoolBarContributionItem cbItem = (CoolBarContributionItem) coolItem.getData();
						if ((cbItem != null) && !cbItem.isVisible() && (!cbItemsToRemove.contains(cbItem))) {
							if (!useRedraw) {
								int visualIndex = coolBar.indexOf(coolItem);
								if (layout.isOnRowAlone(visualIndex)) {
									useRedraw = true;
								}
							}
							coolItemsToRemove.add(coolItem);
						}
					}
					// set redraw off if deleting a sole item from a row to reduce jumpiness in the
					// case that an item gets added back on that row as part of the update
					if (!useRedraw && (cbItemsToRemove.size() + coolItemsToRemove.size() > 2)) {
						useRedraw = true;
					}
					if (useRedraw) coolBar.setRedraw(false);
	
					for (Iterator e = cbItemsToRemove.iterator(); e.hasNext();) {
						CoolBarContributionItem cbItem = (CoolBarContributionItem) e.next();
						dispose(cbItem);
					}
					for (Iterator e = coolItemsToRemove.iterator(); e.hasNext();) {
						CoolItem coolItem = (CoolItem) e.next();
						ToolBar tBar = (ToolBar) coolItem.getControl();
						tBar.setVisible(false);
						dispose(coolItem);
					}
	
					// create a CoolItem for each group of items that does not have a CoolItem 
					ArrayList coolItemIds = getCoolItemIds();
					items = getItems();
					boolean changed = false;
					boolean relock = false;
					for (int i = 0; i < items.length; i++) {
						CoolBarContributionItem cbItem = (CoolBarContributionItem) items[i];
						if (!coolItemIds.contains(cbItem.getId())) {
							if (cbItem.isVisible()) {
								ToolBar toolBar = cbItem.getControl();
								if ((toolBar != null) && (!toolBar.isDisposed()) && (toolBar.getItemCount() > 0) && cbItem.hasDisplayableItems()) {
									if (!changed) {
										// workaround for 14330
										changed = true;
										if (coolBar.getLocked()) {
											coolBar.setLocked(false);
											relock = true;
										}
									}
									createCoolItem(cbItem, toolBar);
								}
							}
						} 				
					}

					updateTabOrder();
					updateItemSizes();
					setDirty(false);
	
					// workaround for 14330
					if(relock) {
						coolBar.setLocked(true);
					}
				} finally {
					if (useRedraw) coolBar.setRedraw(true);
				}
			}
		}
	}
	private void updateRememberedLayout() {
		if (rememberedLayout == null) return;
		ArrayList currentIds = getCoolItemIds();
		ArrayList rememberedIds = rememberedLayout.items;
		if (currentIds.size() != rememberedIds.size()) return;
		for (int i=0; i<rememberedIds.size(); i++) {
			String item = (String)rememberedIds.get(i);
			if (currentIds.indexOf(item) == -1) return;
		}
		// All items in the remembered layout are on the coolbar.  Clear out
		// the remembered layout.
		rememberedLayout = null;
	}		
	/**
	 * Recalculate and set the size for the given CoolBarContributionItem's coolitem.
	 */
	/* package */ void updateSizeFor(CoolBarContributionItem cbItem) {
		CoolItem coolItem = findCoolItem(cbItem);
		if (coolItem != null) setSizeFor(coolItem);
	}
	/**
	 * Sets the tab order of the coolbar to the visual order of its items.
	 */
	/* package */ void updateItemSizes() {
        CoolItem[] items = coolBar.getItems();
		Point[] itemSizes = new Point[coolBar.getItemCount()];
		for (int i=0; i < coolBar.getItemCount(); i++) {
			itemSizes[i]=items[i].getMinimumSize();
		}
		coolBar.setItemLayout(coolBar.getItemOrder(), coolBar.getWrapIndices(), itemSizes);
	}
	/**
	 * Sets the tab order of the coolbar to the visual order of its items.
	 */
	/* package */ void updateTabOrder() {
        CoolItem[] items = coolBar.getItems();
        Control[] children = new Control[items.length];
        for (int i = 0; i < children.length; i++) {
        	children[i] = items[i].getControl();
		}
		coolBar.setTabList(children);
	}
}
