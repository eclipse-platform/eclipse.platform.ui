/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
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
	 * Flag to track whether or not to remember coolbar item positions when an item
	 * is deleted.
	 */
	private boolean rememberPositions = false;
	/** 
	 * Stack for remembering positions of coolitems that have been removed.
	 */
	private ArrayList rememberedPositions = new ArrayList();
	/** 
	 * Table for remembering coolitem layouts.
	 */
	private Hashtable toolBarLayouts = new Hashtable();
	
	private class RestoreItemData {
		CoolItemPosition savedPosition;
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
		CoolItemPosition position = getRememberedPosition(cbItem.getId());
		if (position != null) {
			coolItem = createRememberedCoolItem(cbItem, toolBar, position);
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
		if (cbItem.isOrderBefore()) {
			index = getInsertBeforeIndex(cbItem);
		} else if (cbItem.isOrderAfter()) {
			index = getInsertAfterIndex(cbItem);
		}
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
	private CoolItem createRememberedCoolItem(CoolBarContributionItem cbItem, ToolBar toolBar, CoolItemPosition position) {		
		RestoreItemData data = getRestoreData(cbItem, position);		
		int savedAfterRow = -1;
		int currentAfterRow = -1;
		int savedBeforeRow = -1;
		int currentBeforeRow = -1;
		int savedItemRow = -1;
		CoolBarLayout currentLayout = getLayout();
		if (data.savedPosition != null) {
			savedItemRow = data.savedPosition.getRowOf(cbItem.getId());
			if (data.afterItemId != null) {
				savedAfterRow = data.savedPosition.getRowOf(data.afterItemId);
				currentAfterRow = currentLayout.getRowOfIndex(data.afterIndex);
			}
			if (data.beforeItemId != null) {
				savedBeforeRow = data.savedPosition.getRowOf(data.beforeItemId);
				currentBeforeRow = currentLayout.getRowOfIndex(data.beforeIndex);
			}
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
			if ((savedItemRow == savedAfterRow) && (savedItemRow == savedBeforeRow)) {
				// in the saved layout the item was on the same row as both the beforeItem
				// and the afterItem, compare this to the current coolbar layout
				if (currentAfterRow == currentBeforeRow) {
					// in the current layout, both the before and after item are on the
					// same row, so add the item to this row
					createIndex = data.beforeIndex + 1;
					row = currentBeforeRow;
				} else {
					// in the current layout, both the before and after item are not on
					// the same row
					if (currentBeforeRow == savedBeforeRow) {
						// the beforeItem is on the same row as in the saved layout
						createIndex = data.beforeIndex + 1;
						row = currentBeforeRow;
					} else if (currentAfterRow == savedAfterRow) {
						// the afterItem is on the same row as in the saved layout
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
			} else if (savedItemRow == savedBeforeRow) {
				// in the saved layout, the item was on the same row as the beforeItem,
				// add the item to the before row
				createIndex = data.beforeIndex + 1;
				row = currentBeforeRow;
			} else if (savedItemRow == savedAfterRow) {
				// in the saved layout, the item was on the same row as the afterItem
				// add the item to the currentAfterRow
				createIndex = data.afterIndex;
				row = currentAfterRow;
				newWraps = currentLayout.wrapsForNewItem(row, createIndex);
			} else {
				// in the saved layout, the item was on a row by itself
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
			if (savedItemRow == savedAfterRow) {
				// in the saved layout, the item was on the same row as the afterItem, 
				// put the item on the currentAfterRow 
				row = currentAfterRow;
				newWraps = currentLayout.wrapsForNewItem(row, createIndex);
			} else {
				// in the saved layout, the item was not on the same row as the
				//  afterItem, create a new row before currentAfterRow
				row = currentAfterRow;
				createIndex = currentLayout.getStartIndexOfRow(row);
 				newWraps = currentLayout.wrapsForNewRow(row, createIndex);
			}
		} else if (data.beforeIndex != -1) {
			// only a beforeItem was found in the current coolbar layout
			// for the item to be added
			createIndex = data.beforeIndex + 1;
			if (savedItemRow == savedBeforeRow) {
				// in the saved layout, the item was on the same row as the beforeItem, 
				// put the item on currentBeforeRow
				row = currentBeforeRow;
				newWraps = currentLayout.wrapsForNewItem(row, createIndex);
			} else {
				// in the saved layout, the item was not on the same row as the 
				// beforeItem, create a new row with the item after currentBeforeRow
				row = currentBeforeRow + 1;
 				createIndex = currentLayout.getStartIndexOfRow(row);
 				if (createIndex == -1) {
 					// row does not exist
 					createIndex = coolBar.getItemCount();
 				}
				newWraps = currentLayout.wrapsForNewRow(row, createIndex);
			}
		} else {
			// neither a before or after item was found in the current coolbar
			// layout, just add the item to the end of the coolbar
			createIndex = coolBar.getItemCount();
		}

		// create the item
		if (newWraps != null) {
			// item will be added and then the wraps will set, so use 
			// since the position of the item will change
			coolBar.setRedraw(false);
		}
		CoolItem coolItem = new CoolItem(coolBar, SWT.DROP_DOWN, createIndex);
		coolItem.setControl(toolBar);
		coolItem.setData(cbItem);
//		setSizeFor(coolItem, position.getSizeOf(cbItem.getId()).x);
		setSizeFor(coolItem);
		if (newWraps != null)	{
			coolBar.setWrapIndices(newWraps);
			coolBar.setRedraw(true);
		}
		
		positionAdded(position);
		
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
			if (cbItem != null && rememberPositions) rememberPositionFor(cbItem.getId(), getLayout());
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
	/**
	 */
	private int getInsertAfterIndex(CoolBarContributionItem coolBarItem) {
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
	private int getInsertBeforeIndex(CoolBarContributionItem coolBarItem) {
		IContributionItem[] items = getItems();
		int index = -1;
		CoolBarContributionItem beforeItem = null;
		// find out which item should be before this item
		for (int i=0; i<items.length; i++) {
			if (items[i].equals(coolBarItem)) {
				if (i < items.length - 1) {
					while (i < items.length - 1) {
						beforeItem = (CoolBarContributionItem)items[i+1];
						if (beforeItem.isVisible()) break;
						i++;
					}
				} else {
					// item is not before anything
					index = coolBar.getItems().length;
				}
				break;
			}
		}
		// get the coolbar location of the before item
		if (beforeItem != null) {
			CoolItem beforeCoolItem = findCoolItem(beforeItem);
			if (beforeCoolItem != null) {
				index = coolBar.indexOf(beforeCoolItem);
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
		layout.rememberedPositions = new ArrayList();
		layout.rememberedPositions.addAll(rememberedPositions);
		return layout;
	}
	private RestoreItemData getRestoreData(CoolBarContributionItem cbItem, CoolItemPosition position) {
		RestoreItemData bestMatch = new RestoreItemData();		
		ArrayList coolBarItems = getLayout().items;
		
		// Look at the remembered layouts starting at the layout for the item and heading 
		// down the stack looking for the best saved layout to use for restoring the 
		// item position.  Stop processing when an exact match is found or the position of
		// the item has changed in the remembered layouts.
		int startIndex = rememberedPositions.indexOf(position);
		CoolBarLayout previousLayout = null;
		for (int i=startIndex; i < rememberedPositions.size(); i++) {			
			CoolItemPosition savedPosition = (CoolItemPosition)rememberedPositions.get(i);
			ArrayList savedItems = savedPosition.getItems();
			
			// the item we are trying to restore is not in the saved layout, so stop
			int savedItemIndex = savedItems.indexOf(cbItem.getId());
			if (savedItemIndex == -1) break;
				
			// if the previousLayout is not similar to the next layout, stop
			if (previousLayout != null) {
				if (!previousLayout.isDerivativeOf(savedPosition.layout)) break;
			}
			
			boolean afterBeforeItemFound = bestMatch.afterIndex != -1 && bestMatch.beforeIndex != -1;
			String afterId = null;
			String beforeId = null;	
			int beforeIndex = -1;
			int afterIndex = -1;	
			
			// look at the saved items after savedItemIndex, see if any of these items
			// is in the current coolbar layout
			for (int j = savedItemIndex + 1; j < savedItems.size(); j++) {
				//
				afterId = (String)savedItems.get(j);
				afterIndex = coolBarItems.indexOf(afterId);
				if (afterIndex != -1) {
					break;
				}
			}
			// look at the saved items before savedItemIndex, see if any of these items
			// is in the current coolbar layout
			for (int j = savedItemIndex - 1; j >= 0; j--) {
				//
				beforeId = (String)savedItems.get(j);
				beforeIndex = coolBarItems.indexOf(beforeId);
				if (beforeIndex != -1) {
					break;
				}
			}
			if (beforeIndex != -1 && afterIndex != -1) {
				// test whether or not we've found an exact position, 
				// if we have use this savedPosition
				bestMatch.savedPosition = savedPosition;
				bestMatch.afterItemId = afterId;
				bestMatch.afterIndex = afterIndex;
				bestMatch.beforeItemId = beforeId;
				bestMatch.beforeIndex = beforeIndex;
				if (beforeIndex + 1 == afterIndex) {
					break;
				} 
			} else if (beforeIndex != -1) {
				// If we found both items previously, that is the best match.
				// This condition can happen if items were added between this 
				// savedPosition and the previous savedPosition.
				if (afterBeforeItemFound) break;
				bestMatch.savedPosition = savedPosition;
				bestMatch.beforeItemId = beforeId;
				bestMatch.beforeIndex = beforeIndex;
				bestMatch.afterItemId = null;
				bestMatch.afterIndex = -1;
				// there are no after before index on the coolbar
				if (beforeIndex == coolBarItems.size() - 1) break;
			} else if (afterIndex != -1) {
				// If we found both items previously, that is the best match. 
				// This condition can happen if items were added between this 
				// savedPosition and the previous savedPosition.
				if (afterBeforeItemFound) break;
				bestMatch.savedPosition = savedPosition;
				bestMatch.afterItemId = afterId;
				bestMatch.afterIndex = afterIndex;
				bestMatch.beforeItemId = null;
				bestMatch.beforeIndex = -1;
				// there are no items before afterIndex on the coolbar
				if (afterIndex == 0) break;
			} else {
				// nothing found
				break;
			}
			previousLayout = savedPosition.layout;
		}
		return bestMatch;
	}
	/**
	 * Return the remembered position for the given CoolBarContributionItem.  If a position
	 * was not remembered, return null.
	 */
	private CoolItemPosition getRememberedPosition (String cbItemId) {
		for (int i=0; i<rememberedPositions.size(); i++) {
			CoolItemPosition position = (CoolItemPosition)rememberedPositions.get(i);
			if (position.id.equals(cbItemId) && !position.added) return position;
		}
		return null;
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
		((CoolBarContributionItem)item).setOrderAfter(true);
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
		((CoolBarContributionItem)item).setOrderBefore(true);
	}
	/**
	 */
	/* package */ boolean isLayoutLocked() {
		if (!coolBarExist()) return false;
		return coolBar.getLocked();
	}
	/* package */ boolean isValidCoolItemId(String id) {
		ActionSetRegistry registry = WorkbenchPlugin.getDefault().getActionSetRegistry();
		if (registry.findActionSet(id) != null) return true;
		WorkbenchWindow window = (WorkbenchWindow)((Workbench)PlatformUI.getWorkbench()).getActiveWorkbenchWindow();
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
	private void positionAdded(CoolItemPosition position) {
		// mark the position as added back
		position.added = true;
	
		// remembered positions on the top of the stack that have been added can be removed
		boolean done =  rememberedPositions.size() == 0;
		while (!done) {
			CoolItemPosition topLayout = (CoolItemPosition)rememberedPositions.get(0);
			if (topLayout.added) {
				rememberedPositions.remove(0);
				done = rememberedPositions.size() == 0;
			} else {
				done = true;
			}			
		}
		// all except the least recently added position in a set of contiguously added positions
		// can be removed from the stack within each g
		int testIndex = 1;
		done =  testIndex + 1 > rememberedPositions.size() - 1;
		while (!done) {
			CoolItemPosition pos = (CoolItemPosition)rememberedPositions.get(testIndex);
			if (pos.added) {
				CoolItemPosition nextPos = (CoolItemPosition)rememberedPositions.get(testIndex + 1);
				CoolItemPosition prevPos = (CoolItemPosition)rememberedPositions.get(testIndex - 1);
				boolean nextSameGroup = pos.layout.isDerivativeOf(nextPos.layout);
				boolean prevSameGroup = prevPos.layout.isDerivativeOf(pos.layout);
				if (!nextSameGroup && !prevSameGroup) {
					// only item in this group
					rememberedPositions.remove(testIndex);
				} else if (nextPos.added && nextSameGroup && !prevSameGroup) {
					rememberedPositions.remove(testIndex);
				} else {
					testIndex++;
				}
			} else {
				testIndex++;
			}
			done = testIndex + 1 > rememberedPositions.size() - 1; 
		}	
	}
	/**
	 * Layout out the coolbar items so that each one is sized to its preferred size.
	 */
	private void redoLayout() {
		// Reset the wrap indices and the cool item sizes.  The coolbar will automatically
		// wrap the items to the next row that do not fit.
		coolBar.setWrapIndices(new int[0]);		
		CoolItem[] coolItems = coolBar.getItems();
		for (int i = 0; i < coolItems.length; i++) {
			CoolItem coolItem = coolItems[i];
			setSizeFor(coolItem);
		}
		// clear any remembered data
		rememberedPositions = new ArrayList();
		relayout();		
	}
	/**
	 */
	private void resetLayout() {
		coolBar.setRedraw(false);
		CoolItem[] coolItems = coolBar.getItems();
		for (int i = 0; i < coolItems.length; i++) {
			CoolItem coolItem = coolItems[i];
			dispose(coolItem);
		}
		coolBar.setWrapIndices(new int[] {});
		update(true);
		coolBar.setRedraw(true);
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
	 */
	private void relayout() {
		coolBar.getParent().layout();
	}
	/**
	 * Save the position of the given CoolItem for restoring the item later.
	 */
	private CoolItemPosition rememberPositionFor(String cbItemId, CoolBarLayout layout) {
		// create a CoolItemPosition for the given cbItem, no need to save 
		// remembered positions as part of the layout
		layout.rememberedPositions = new ArrayList();
		CoolItemPosition position = new CoolItemPosition(cbItemId, layout);
		
		// remove the previously remembered position for the item if
		// it exists
		int index = -1;
		for (int i=0; i<rememberedPositions.size(); i++) {
			CoolItemPosition item = (CoolItemPosition)rememberedPositions.get(i);
			if (position.id.equals(item.id)) index = i;
		}
		if (index != -1) rememberedPositions.remove(index);
		
		// add the position to the beginning of the stack of remembered
		// positions
		rememberedPositions.add(0, position);
		return position;
	}
	void saveLayoutFor(Perspective perspective) {
		perspective.setToolBarLayout(getLayout());
		rememberPositions = false;
		rememberedPositions = new ArrayList();
	}
	void saveToolBarLayout(CoolBarContributionItem cbItem) {
		toolBarLayouts.put(cbItem.getId(), cbItem.getToolBarManager().getItems());
	}
	void setLayoutFor(Perspective perspective) {
		rememberedPositions = new ArrayList();
		setLayout(perspective.getToolBarLayout());
		updateTabOrder();
		rememberPositions = true;
	}
	/**
	 */
	private void setLayout(CoolBarLayout layout) {
		try {
			setLayoutTo(layout);
		} catch (Exception e) {
			// A lot can go wrong if the layout is out of sync with the coolbar state.
			// Try to recover by resetting the layout.
			WorkbenchPlugin.log("An error has occurred restoring the coolbar layout. " + e.toString()); //$NON-NLS-1$
			resetLayout();
		}
	}
	/**
	 */
	private void setLayoutTo(CoolBarLayout layout) {
		// This method is called after update.  All of the coolbar items have
		// been created, now apply the layout to the coolbar. 

		if (layout == null) {
			coolBar.setRedraw(false);
			CoolItem[] coolItems = coolBar.getItems();
			int[] newItemOrder = new int[coolItems.length];
			// Reset the coolitem order to their original order.  This order is
			// based on the order of the ContributionItems.  Note that the only
			// way to reset item order is to setItemLayout (no setItemOrder API).
			IContributionItem[] items = getItems();
			int j = 0;
			int[] oldItemOrder = coolBar.getItemOrder();
			for (int i = 0; i < items.length; i++) {
				CoolBarContributionItem item = (CoolBarContributionItem)items[i];
				CoolItem coolItem = findCoolItem(item);
				if (coolItem != null) {
					int visualIndex = coolBar.indexOf(coolItem);
					int creationIndex = oldItemOrder[visualIndex];
					newItemOrder[j] = creationIndex;
					j++;
				}
			}
			coolBar.setItemLayout(newItemOrder, coolBar.getWrapIndices(), coolBar.getItemSizes());
			redoLayout();
			coolBar.setRedraw(true);
			return;
		}

		int maxItemCount = coolBar.getItemCount();
		int[] itemOrder = new int[maxItemCount];
		Point[] itemSizes = new Point[maxItemCount];

		// Used to keep track of what cool items have been accounted for in
		// layout.  New items that were added after the layout was saved, will
		// not be accounted for.
		int[] found = new int[maxItemCount];
		for (int i = 0; i < found.length; i++) {
			found[i] = -1;
		}
		int[] currentItemOrder = coolBar.getItemOrder();
		Vector foundItemOrder = new Vector();
		Vector foundItemSizes = new Vector();
		for (int i=0; i<layout.items.size(); i++) {
			CoolItem coolItem = findCoolItem((CoolBarContributionItem)find((String)layout.items.get(i)));
			if (coolItem != null) {
				int index = currentItemOrder[coolBar.indexOf(coolItem)];
				foundItemOrder.add(new Integer(index));
				foundItemSizes.add(layout.itemSizes[i]);
				// the cool item at the given index has been accounted for,
				// so set the found value for that index to 0
				found[index]=0;
			} 
		}
		int count=0;
		for (count=0; count<foundItemOrder.size(); count++) {
			itemOrder[count]=((Integer)foundItemOrder.elementAt(count)).intValue();
			itemSizes[count]=(Point)foundItemSizes.elementAt(count);
		}
		
		rememberedPositions = layout.rememberedPositions;
		// Handle those items that are on the coolbar, but not in the layout.
		// Just add these items at the end of the coolbar for now.
		ArrayList rememberedAddedItems = new ArrayList();
		for (int i=0; i<found.length; i++) {
			if (found[i] == -1) {
				itemOrder[count]=i;
				CoolItem cItem = coolBar.getItem(i);
				itemSizes[count]=cItem.getSize();
				CoolBarContributionItem cbItem = (CoolBarContributionItem)cItem.getData();
				CoolItemPosition position = getRememberedPosition(cbItem.getId());
				if (position != null) {
					rememberedAddedItems.add(cItem);
				}
				count++;
			}
		}

		coolBar.setRedraw(false);
		coolBar.setItemLayout(itemOrder, new int[0], itemSizes);

		// restore the wrap indices after the new item order is restored, wrap on the same items that 
		// were specified in the layout
		String[] wrapItems = new String[layout.itemWrapIndices.length];
		for (int i = 0; i < layout.itemWrapIndices.length; i++) {
			wrapItems[i] = (String) layout.items.get(layout.itemWrapIndices[i]);
		}
		int[] wrapIndices = new int[wrapItems.length];
		ArrayList currentCoolItemIds = getCoolItemIds();
		int j = 0;
		int k = 0;
		while (k < wrapItems.length) {
			int index = currentCoolItemIds.indexOf(wrapItems[k]);
			if (index != -1) {
				wrapIndices[j] = index;
				j++;
				k++;
			} else {
				// wrap item no longer exists, try wrapping on the next item in the
				// saved layout, if we hit a new row, stop
				int visualIndex = layout.items.indexOf(wrapItems[k]);
				int row = layout.getRowOfIndex(visualIndex);
				int nextIndex = visualIndex + 1;
				int nextRow = layout.getRowOfIndex(nextIndex);
				if (nextIndex < layout.items.size() && nextRow == row) {
					String nextItem = (String)layout.items.get(nextIndex);
					wrapItems[k]=nextItem;
				} else {
					k++;
				}
			}
		}
		int[] itemWraps = new int[j];
		System.arraycopy(wrapIndices, 0, itemWraps, 0, j);
		coolBar.setWrapIndices(itemWraps);

		// restore the position of items whose position was remembered
		ArrayList cbItems = new ArrayList();
		ArrayList toolbars = new ArrayList();
		// dispose of the coolitems, then add back one at a time - necessary to restore the
		// original layout
		for (int i=0; i<rememberedAddedItems.size(); i++) {
			CoolItem item = (CoolItem)rememberedAddedItems.get(i);
			CoolBarContributionItem cbItem = (CoolBarContributionItem)item.getData();
			cbItems.add(cbItem);
			ToolBar tBar = (ToolBar) item.getControl();
			toolbars.add(tBar);
			dispose(item);
		}
		for (int i=0; i<cbItems.size(); i++) {		
			CoolBarContributionItem cbItem = (CoolBarContributionItem)cbItems.get(i);
			CoolItemPosition position = getRememberedPosition(cbItem.getId());
			// restore the item's saved position
			ToolBar tBar = (ToolBar)toolbars.get(i);
			createRememberedCoolItem(cbItem, tBar, position);
		}

		// need to remember position for any item that was in the layout but not 
		// currently on the coolbar
		ArrayList currentIds = getCoolItemIds();
		for (int i=0; i<layout.items.size(); i++) {
			String id = (String)layout.items.get(i);
			if (!currentIds.contains(id)) {
				rememberPositionFor(id, layout);
			}
		}
		coolBar.setRedraw(true);
	}
	private void setSizeFor(CoolItem coolItem) {
		setSizeFor(coolItem, -1);
	}
	/**
	 */
	private void setSizeFor(CoolItem coolItem, int coolWidth) {
		ToolBar toolBar = (ToolBar) coolItem.getControl();
		Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point coolSize = coolItem.computeSize(size.x, size.y);
		// note setMinimumSize must be called before setSize, see PR 15565
		coolItem.setMinimumSize(size.x, size.y);
		coolItem.setPreferredSize(coolSize);
		if (coolWidth == -1) {
			coolItem.setSize(coolSize);
		} else {
			coolItem.setSize(new Point(coolWidth, coolSize.y));
		} 
	}
	void setToolBarLayout(CoolBarContributionItem cbItem) {
		IContributionItem[] items = (IContributionItem[])toolBarLayouts.get(cbItem.getId());
		if (items != null) {
			CoolItemToolBarManager tBarMgr = cbItem.getToolBarManager();
			for (int i=0; i<items.length; i++) {
				IContributionItem item = items[i];
				tBarMgr.add(item);
			}	
			toolBarLayouts.remove(cbItem.getId());
		}
	}
	/**
	 */
	public void update(boolean force) {
		if (isDirty() || force) {
			if (coolBarExist()) {
				boolean useRedraw = false;
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
				setDirty(false);

				// workaround for 14330
				if(relock) {
					coolBar.setLocked(true);
				}
				if (useRedraw) coolBar.setRedraw(true);
			}
		}
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
	/* package */ void updateTabOrder() {
        CoolItem[] items = coolBar.getItems();
        Control[] children = new Control[items.length];
        for (int i = 0; i < children.length; i++) {
        	children[i] = items[i].getControl();
		}
		coolBar.setTabList(children);
	}
}