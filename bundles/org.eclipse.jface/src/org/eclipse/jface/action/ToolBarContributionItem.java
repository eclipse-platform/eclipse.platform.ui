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

package org.eclipse.jface.action;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * The <code>ToolBarContributionItem</code> class provides a wrapper for tool bar
 * managers when used in cool bar managers. It extends <code>ContributionItem</code>
 * but and provides some additional methods to customize the size of the cool item
 * and to retrieve the underlying tool bar manager.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 3.0
 */
public class ToolBarContributionItem extends ContributionItem {
	
	/**
	 * A constant used by <code>setMinimumItemsToShow</code> and <code>getMinimumItemsToShow</code>
	 * to indicate that all tool items should be shown in the cool item.
	 */
	public static final int SHOW_ALL_ITEMS = -1;
	
	/**
	 * The tool bar manager used to manage the tool items contained in the cool item widget.
	 */
	private ToolBarManager toolBarManager = null;
	
	/**
	 * The pull down menu used to list all hidden tool items if the current size is less than
	 * the preffered size.
	 */
	private MenuManager chevronMenuManager = null;
	
	/**
	 * The context menu of the cool item. If one is not defined the the parents context menu
	 * is used.
	 */
	private MenuManager contextMenuManager = null;
	
	/**
	 * Mininum number of tool items to show in the cool item widget.
	 */
	private int minimumItemsToShow = SHOW_ALL_ITEMS;
	
	/**
	 * Enable/disable chevron support.
	 */
	private boolean useChevron = true;
	
	/**
	 * Current width of cool item. 
	 */
	private int currentWidth = -1;
	
	/**
	 * Current height of cool item
	 */
	private int currentHeight = -1;
	
	/**
	 * The widget created for this item; <code>null</code>
	 * before creation and after disposal.
	 */
	private CoolItem coolItem = null;
	
	/**
	 * Creates a tool bar contribution item.
	 * 
	 * @param toolBarManager the tool bar manager to wrap
	 * @param id the contribution item id, or <code>null</code> if none
	 */
	public ToolBarContributionItem(IToolBarManager toolBarManager, String id) {
		super(id);
		Assert.isTrue(toolBarManager instanceof ToolBarManager);
		this.toolBarManager = (ToolBarManager)toolBarManager;
	}
	
	/**
	 * Convenience method equivalent to 
	 * <code>ToolBarContributionItem(toolBarManager, null)</code>.
	 * 
	 * @param toolBarManager the tool bar manager
	 */
	public ToolBarContributionItem(IToolBarManager toolBarManager) {
		this(toolBarManager,null);
	}
	
	/**
	 * Convenience method equivalent to
	 * <code>ToolBarContributionItem(new ToolBarManager(), null)</code>.
	 */
	public ToolBarContributionItem() {
		this(new ToolBarManager(), null);
	}
	
	/**
	 * Returns the internal tool bar manager of the contribution item.
	 * 
	 * @return the tool bar manager, or <code>null</code> if one is not defined.
	 * @see IToolBarManager
	 */
	public IToolBarManager getToolBarManager() {
		return toolBarManager;
	}

	/** 
	 * Returns a consistent set of wrap indices. The return value
	 * will always include at least one entry and the first entry will 
	 * always be zero.  CoolBar.getWrapIndices() is inconsistent 
	 * in whether or not it returns an index for the first row.
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
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.CoolBar, int)
	 */
	public void fill(CoolBar coolBar, int index) {
	   if (coolItem == null && coolBar != null) {
	   	   ToolBar toolBar = toolBarManager.createControl(coolBar);
		   //toolBarManager.update(true);
		   // Do not create a coolItem if the toolbar is empty
		   if (toolBar.getItemCount() < 1) return;
		   int flags = SWT.DROP_DOWN;
		   if (index >= 0) {
		   		coolItem = new CoolItem(coolBar, flags, index);
		   }else {
		   		coolItem = new CoolItem(coolBar, flags);
		   }
		   // sets the back reference   
		   coolItem.setData(this);
		   // Add the toolbar to the CoolItem widget
		   coolItem.setControl(toolBar);   
		   
		   // Handle Context Menu
		   toolBar.addListener(SWT.MenuDetect, new Listener() {
			   public void handleEvent(Event event) {
			   		// if the toolbar does not have its own context menu then
			   	 	// handle the event
					 if (toolBarManager.getContextMenuManager() == null) {   
					 	handleContextMenu(event);
					 }
			   }
		   });
		   
		   // Handle for chevron clicking
		   if (getUseChevron()) {
			   // Chevron Support
			   coolItem.addSelectionListener(new SelectionAdapter() {
				   	public void widgetSelected(SelectionEvent event) {
				   		if (event.detail == SWT.ARROW) {
				   			handleChevron(event);
				   		}
				   	}
			   });
		   }
		   
		   // Handle for disposal
		   coolItem.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					handleWidgetDispose(event);
				}
		   });
		   
		   // Sets the size of the coolItem
		   updateSize(true);
	   }
	}
	
	/**
	 * Handles the event when the toobar item does not have its own context menu.
	 * @param event the event object
	 */
	private void handleContextMenu(Event event) {
		ToolBar toolBar = toolBarManager.getControl();
		// If parent has a menu then use that one
		Menu parentMenu = toolBar.getParent().getMenu();
		if ((parentMenu != null) && (!parentMenu.isDisposed())) {
			toolBar.setMenu(parentMenu);
			// Hook listener to remove menu once it has disapeared
			parentMenu.addListener(SWT.Hide, new Listener(){
				public void handleEvent(Event event) {
					ToolBar toolBar = toolBarManager.getControl();
					if (toolBar != null) {
						toolBar.setMenu(null);
						Menu parentMenu = toolBar.getParent().getMenu();
						if (parentMenu != null) {
							parentMenu.removeListener(SWT.Hide,this);
						}
					}
				}
			});
		}
	}
	
	/**
	 * Handles the disposal of the widget.
	 * @param event the event object
	 */
	private void handleWidgetDispose(DisposeEvent event) {
		coolItem = null;	
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#saveWidgetState()
	 */
	public void saveWidgetState() {
		if (coolItem == null)  return;
		
		//1. Save current size
		CoolBar coolBar = coolItem.getParent();
		boolean isLastOnRow = false;
		int lastIndex = coolBar.getItemCount() - 1;
		int coolItemIndex = coolBar.indexOf(coolItem);
		int [] wrapIndicies = getAdjustedWrapIndices(coolBar.getWrapIndices());
		// Traverse through all wrap indicies backwards
		for (int row=wrapIndicies.length-1; row >= 0; row--) {
			if (wrapIndicies[row] <= coolItemIndex) {

				int nextRow = row + 1;
				int rowStartIndex = wrapIndicies[row];
				int nextRowStartIndex;
				if (nextRow > (wrapIndicies.length - 1) ) {
					nextRowStartIndex = lastIndex + 1;
				}else {
					nextRowStartIndex = wrapIndicies[nextRow];
				}
				 
				// Check to see if its the last item on the row
				if (coolItemIndex == (nextRowStartIndex -1)) {
					isLastOnRow = true;
				}
				boolean isAloneOnRow = ((rowStartIndex == coolItemIndex) && isLastOnRow);
				break;
			 }
		 }
		
		// Save the preferred size as actual size for the last item on a row
		int nCurrentWidth;
		if (isLastOnRow) {
			nCurrentWidth = coolItem.getPreferredSize().x;
		}else {
			nCurrentWidth = coolItem.getSize().x;
		}
		setCurrentWidth(nCurrentWidth);
		setCurrentHeight(coolItem.getSize().y);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#dispose()
	 */
	public void dispose() {
		
		// Dispose of the ToolBar and all its contributions
		if (toolBarManager != null) {
			toolBarManager.dispose();
		}
		
	}
	
	/**
	 * Updates the cool items' preferred, minimum, and current size. The preferred
	 * size is calculated based on the tool bar size and extra trim.
	 * 
	 * @param changeCurrentSize <code>true</code> if the current size should be changed
	 * to the preferred size, <code>false</code> to not change the current size
	 */
	public void updateSize(boolean changeCurrentSize) {
		// cannot set size if coolItem is null
		if (coolItem == null || coolItem.isDisposed()) return;
		// Calculate size of toolbar
		ToolBar toolBar = (ToolBar)coolItem.getControl();
		
		if(toolBar == null)
			return; 
		// if the toolbar does not contain any items then dispose of coolItem
		if (toolBar.getItemCount() <= 0) {	
			coolItem.setData(null);
			Control control = coolItem.getControl();
			if ((control != null) && !control.isDisposed()) {
				control.dispose();
				coolItem.setControl(null);
			}
			if (!coolItem.isDisposed()) {
				coolItem.dispose();
			}
			return;
		}
		Point toolBarSize = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		// Set the preffered size to the size of the toolbar plus trim
		Point prefferedSize = coolItem.computeSize(toolBarSize.x, toolBarSize.y);
		coolItem.setPreferredSize(prefferedSize);
		// note setMinimumSize must be called before setSize, see PR 15565
		// Set minimum size
		if (getMinimumItemsToShow() != SHOW_ALL_ITEMS) {
			int toolItemWidth = toolBar.getItems()[1].getWidth();
			int minimumWidth = toolItemWidth * getMinimumItemsToShow();
			coolItem.setMinimumSize(minimumWidth, toolBarSize.y);
		}else {
			coolItem.setMinimumSize(toolBarSize.x, toolBarSize.y);
		}
		
		if (changeCurrentSize) {
			// Set current size to preffered size
			coolItem.setSize(prefferedSize);
		}
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
		CoolBar coolBar = item.getParent();
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
	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#update(java.lang.String)
	 */
	public void update(String propertyName) {
		if (coolItem != null) {	
			boolean sizeChanged = (propertyName == null) || propertyName.equals(CoolBarManager.SIZE);
			
			if (sizeChanged) {
				updateSize(true);
			}
			
		}
		// Sets its own visibility depending on whether there exist any items in 
		// tool bar manager that are visible
		if (propertyName.equals(CoolBarManager.VISIBILITY)) {
			// if there is no visible items in our tool bar
			if (toolBarManager != null) {
				IContributionItem[] items = toolBarManager.getItems();
				boolean visibleItem = false;
				for (int i=0; i < items.length; i++) {
					if ((!items[i].isGroupMarker()) && (!items[i].isSeparator()) && (items[i].isVisible())) {
						visibleItem = true;
						break;
					}
				}
				if (!visibleItem) {
					setVisible(false);
				}else {
					setVisible(true);
					if (getParent() != null) {
						getParent().markDirty();
					}
				}
			}
			
		}
	}


	/**
	 * Returns the minimum number of tool items to show in the cool item.
	 * 
	 * @return the minimum number of tool items to show,
	 * or <code>SHOW_ALL_ITEMS</code> if a value was not set
	 * @see #setMinimumItemsToShow(int)
	 */
	public int getMinimumItemsToShow() {
		return minimumItemsToShow;
	}

	/**
	 * Sets the minimum number of tool items to show in the cool item. If this number is less
	 * than the total tool items, a chevron will appear and the hidden tool items appear in a 
	 * drop down menu. By default, all the tool items are shown in the cool item.
	 * 
	 * @param minimumItemsToShow the minimum number of tool items to show. 
	 * @see #getMinimumItemsToShow()
	 * @see #setUseChevron(boolean)
	 */
	public void setMinimumItemsToShow(int minimumItemsToShow) {
		this.minimumItemsToShow = minimumItemsToShow;
	}
	
	/**
	 * Enables or disables chevron support for the cool item. By default, chevron support is 
	 * enabled.
	 * 
	 * @param value <code>true</code> to enable chevron support, <code>false</code> otherwise.
	 */
	public void setUseChevron(boolean value) {
		useChevron = value;	
	}
	
	/**
	 * Returns whether chevron support is enabled.
	 * 
	 * @return <code>true</code> if chevron support is enabled, <code>false</code> otherwise
	 */
	public boolean getUseChevron() {
		return useChevron;
	}

	/**
	 * Returns the current width of the corresponding cool item.
	 * 
	 * @return the current size
	 */
	public int getCurrentWidth() {
		return currentWidth;
	}
	
	/**
	 * Returns the current height of the corresponding cool item.
	 * 
	 * @return the current height
	 */
	public int getCurrentHeight() {
		return currentHeight;
	}
	
	/**
	 * Sets the current height of the cool item. 
	 * Update(SIZE) should be called to adjust the widget.
	 * 
	 * @param currentHeight the current height to set
	 */
	public void setCurrentHeight(int currentHeight) {
		this.currentHeight = currentHeight;
	}
	
	/**
	 * Sets the current width of the cool item. 
	 * Update(SIZE) should be called to adjust the widget.
	 * 
	 * @param currentWidth the current width to set
	 */
	public void setCurrentWidth(int currentWidth) {
		this.currentWidth = currentWidth;
	}

}
