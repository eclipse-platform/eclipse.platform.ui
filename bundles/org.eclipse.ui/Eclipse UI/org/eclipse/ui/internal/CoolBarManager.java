package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

/**
 * WORK IN PROGRESS FOR COOLBAR SUPPORT
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
	 * Adds a contribution item to the start or end of the group 
	 * with the given id.
	 * 
	 * Not valid for CoolBarManager.  Only CoolBarContributionItems are items
	 * of this manager.
	 */
	private void addToGroup(String itemId, IContributionItem item, boolean append) {
		Assert.isTrue(false);
	}
	/**
	 */
	private boolean coolBarExist() {
		return coolBar != null && !coolBar.isDisposed();
	}
	/**
	 */
	public CoolBar createControl(Composite parent) {
		if (!coolBarExist() && parent != null) {
			// Create the CoolBar and its popup menu.
			coolBar = new CoolBar(parent, style);
			coolBar.setLocked(false);
			coolBar.addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event event) {
					coolBar.getParent().layout();
				}
			});
			coolBar.addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					popupCoolBarMenu(e);
				}
			});
		}
		return coolBar;
	}
	/**
	 * Create the coolbar item for the given contribution item.
	 */
	private CoolItem createCoolItem(CoolBarContributionItem cbItem, ToolBar toolBar) {
		CoolItem coolItem;
		toolBar.setVisible(true);
		int index = -1;
		if (cbItem.isOrderBefore()) {
			index = getInsertBeforeIndex(cbItem);
		} else if (cbItem.isOrderAfter()) {
			index = getInsertAfterIndex(cbItem);
		}
		// Figure out how much space is used on the row on which this
		// cool item will be added.  Calculate this value before we
		// add the cool item.
		int spaceUsed = 0;
		if (index == -1) {
			index = coolBar.getItemCount();
			spaceUsed = getSpaceUsed(index);
			coolItem = new CoolItem(coolBar, SWT.DROP_DOWN);
		} else {
			spaceUsed = getSpaceUsed(index);
			coolItem = new CoolItem(coolBar, SWT.DROP_DOWN, index);
		}
		coolItem.setControl(toolBar);
		coolItem.setData(cbItem);
		setSizeFor(coolItem);
					
		coolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (event.detail == SWT.ARROW) {
					handleChevron(event);
				}
			}
		});
		
		if (coolItem.getPreferredSize().x > (coolBar.getClientArea().width - spaceUsed)) {
			// item will not fit on the row on which it is being added
			// wrap the last item on the row
			int rowEnd = getRowEndIndex(index);		
			redoLayoutStartingAt(rowEnd);
		}
		return coolItem;
	}
	/**
	 */
	public void dispose(CoolBarContributionItem cbItem) {
		CoolItem coolItem = findCoolItem(cbItem);
		if (coolItem != null) {
			dispose(coolItem);
		}
		remove(cbItem);
 		cbItem.getToolBarManager().dispose();
	}
	/**
	 */
	protected void dispose(CoolItem coolItem) {
		if ((coolItem != null) && !coolItem.isDisposed()) {
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
	public void dispose() {
		if (coolBarExist()) {
			IContributionItem[] cbItems = getItems();
			for (int i=0; i<cbItems.length; i++) {
				CoolBarContributionItem cbItem = (CoolBarContributionItem)cbItems[i];
				dispose(cbItem);
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
	CoolItem findCoolItem(CoolBarContributionItem item) {
		CoolItem[] items = coolBar.getItems();
		for (int i = 0; i < items.length; i++) {
			CoolItem coolItem = items[i];
			if (coolItem.getData().equals(item)) return coolItem;
		}
		return null;
	}
	/**
	 */
	CoolBarContributionItem findSubId(String id) {
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
	private boolean fits(int itemIndex) {
		// Does the coolitem fit on the row it is on?  Assume the item is at
		// the end and assume it does not fit if the sum of the preferred 
		// sizes for the items on the row is greater than the width of the 
		// coolbar.  This method is used when determining whether or not to
		// wrap added items when restoring/switching to a perspective.
		int[] wrapIndices = getAdjustedWrapIndices();
		int rowEndIndex = itemIndex;
		int rowStartIndex = 0;
		// We will have at least one wrap index of 0.
		for (int i = 0; i < wrapIndices.length; i++) {
			int wrapIndex = wrapIndices[i];
			if (itemIndex < wrapIndex) {
				break;
			} else {
				rowStartIndex = wrapIndex;
			}
		}
		int width = 0;
		for (int i=rowStartIndex; i<=rowEndIndex; i++) {
			CoolItem item = coolBar.getItem(i);
			width += item.getPreferredSize().x;
		}
		return width <= coolBar.getClientArea().width;
	}

	/** 
	 * Return a consistent set of wrap indices.  The return value
	 * will always include at least one entry and the first entry will 
	 * always be zero.  CoolBar.getWrapIndices() is inconsistent 
	 * in whether or not it returns an index for the first row.
	 */
	private int[] getAdjustedWrapIndices() {
		int[] wrapIndices = coolBar.getWrapIndices();
		int[] adjustedWrapIndices;
		if (wrapIndices.length == 0) {
			adjustedWrapIndices = new int[] { 0 };
		} else {
			if (wrapIndices[0] != 0) {
				adjustedWrapIndices = new int[wrapIndices.length + 1];
				adjustedWrapIndices[0] = 0;
				for (int i = 0; i < wrapIndices.length; i++) {
					adjustedWrapIndices[i + 1] = wrapIndices[i];
				}
			} else {
				adjustedWrapIndices = wrapIndices;
			}
		}
		return adjustedWrapIndices;
	}
	/**
	 */
	private ArrayList getContributionIds() {
		IContributionItem[] items = getItems();
		ArrayList ids = new ArrayList(items.length);
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			ids.add(item.getId());
		}
		return ids;
	}
	/**
	 */
	private ArrayList getCoolItemIds() {
		CoolItem[] coolItems = coolBar.getItems();
		ArrayList ids = new ArrayList(coolItems.length);
		for (int i = 0; i < coolItems.length; i++) {
			CoolBarContributionItem group = (CoolBarContributionItem) coolItems[i].getData();
			ids.add(group.getId());
		}
		return ids;
	}
	/**
	 * Return the SWT control for this manager.
	 */
	public CoolBar getControl() {
		return coolBar;
	}
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
	 *  @return a int array that contains the index of the last item of each row
	 *  (FH: helper method)
	 */
	private int[] getLastItemIndices() {
		int count = coolBar.getItemCount();
		int[] indices = coolBar.getWrapIndices();
		int n = indices.length;
		if (n == 0) {
			return count == 0 ? new int[0] : new int[] { count - 1 };
		}
		else {
			// convert from first item indices to last item indices
			for (int i = 0; i < n - 1; ++i) {
				indices[i] = indices[i+1] - 1;
			}
			indices[n - 1] = count - 1;
		}
		return indices;
	}
	/**
	 */
	public CoolBarLayout getLayout() {
		if (!coolBarExist())
			return null;
	
		CoolBarLayout layout = new CoolBarLayout();
		CoolItem[] coolItems = coolBar.getItems();
		ArrayList newItems = new ArrayList(coolItems.length);
		for (int i = 0; i < coolItems.length; i++) {
			CoolBarContributionItem item = (CoolBarContributionItem) coolItems[i].getData();
			if (item != null) {
				newItems.add(item.getId());
			}
		}
		layout.items = newItems;
		layout.itemSizes = coolBar.getItemSizes();
		layout.itemWrapIndices = coolBar.getWrapIndices();
	
		//FH -> Save the preferred size as actual size for the last item on a row
		int[] lastIndices = getLastItemIndices();
		for (int i = 0; i < lastIndices.length; i++) {
			int lastIndex = lastIndices[i];
			if (lastIndex >= 0 && lastIndex < coolItems.length) {
				CoolItem lastItem = coolItems[lastIndex];
				layout.itemSizes[lastIndex] = lastItem.getPreferredSize();
			}
		}
			
		return layout;
	}
	/**
	 * Get the row end for the given index.
	 */
	private int getRowEndIndex (int index) {		
		int[] wrapIndices = getAdjustedWrapIndices();
		int rowEndIndex = -1;
		// We will have at least one wrap index of 0.
		for (int i = 0; i < wrapIndices.length; i++) {
			int wrapIndex = wrapIndices[i];
			if (index < wrapIndex) {
				rowEndIndex = wrapIndices[i] - 1;
				break;
			} else if (index == wrapIndex) {
				if (i < (wrapIndices.length - 1)) {
					rowEndIndex = wrapIndices[i+1] - 1;
				}
				break;
			}
		}
		if (rowEndIndex == -1) {
			rowEndIndex = coolBar.getItemCount() - 1;
		}
		return rowEndIndex;
	}
	/**
	 * Get the space used by the items on the row that the item added
	 * at index will be on.
	 */
	private int getSpaceUsed (int index) {
		// If no items exist, no space is used.
		if (coolBar.getItemCount() == 0) return 0;
		
		// Get row start and row end.  		
		int[] wrapIndices = getAdjustedWrapIndices();
		int rowEndIndex = -1;
		int rowStartIndex = wrapIndices[0];
		// We will have at least one wrap index of 0.
		for (int i = 0; i < wrapIndices.length; i++) {
			int wrapIndex = wrapIndices[i];
			// want to use the equals here since the item at index 
			// has not been added, for example wrapIndices[0,3] and
			// index = 3 - the item will be added to the end of the 
			// first row, so we want our start/end to be 0/2
			if (index <= wrapIndex) {
				rowEndIndex = wrapIndices[i] - 1;
				break;
			} else {
				rowStartIndex = wrapIndex;
			}
		}
		if (rowEndIndex == -1) {
			rowEndIndex = coolBar.getItemCount() - 1;
		}
		int width = 0;
		for (int i = rowStartIndex; i < rowEndIndex; i++) {
			width += coolBar.getItem(i).getSize().x;
		}
		// For row end item need to calculate size differently since 
		// last item on a row is given a size to account for the 
		// extra space on the row.
		width += coolBar.getItem(rowEndIndex).getPreferredSize().x;
		return width;
	}
	/**
	 */
	public int getStyle() {
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
		Rectangle itemBounds = item.getBounds();
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
	public boolean isLayoutLocked() {
		if (!coolBarExist()) return false;
		return coolBar.getLocked();
	}
	/**
	 */
	public void lockLayout(boolean value) {
		coolBar.setLocked(value);
	}
	/**
	 */
	/* package */ void popupCoolBarMenu(MouseEvent e) {
		if (e.button != 3)
			return;
		Point pt = new Point(e.x, e.y);
		pt = ((Control) e.widget).toDisplay(pt);
		Menu coolBarMenu = coolBarMenuManager.createContextMenu(coolBar);
		coolBarMenu.setLocation(pt.x, pt.y);
		coolBarMenu.setVisible(true);
	}
	/**
	 * Layout out the coolbar items so that each one is completely visible, 
	 * wrapping when necessary.
	 */
	protected void redoLayout() {
		redoLayoutStartingAt(0);
	}
	/**
	 * Layout out the coolbar items so that each one is completely visible, 
	 * wrapping when necessary.
	 */
	protected void redoLayoutStartingAt(int index) {
		// We must reset the wrap indices, otherwise coolItem.setSize(int)
		// may not work as expected.  The coolbar sets the size of the last 
		// item on each row to the size of the item plus any remaining  
		// area to the right of the item and you cannot set this size any 
		// smaller.  For reset/auto-wrapping purposes we only want the last
		// item in the coolbar to have extra space associated to it, so make
		// the appropriate number of rows.
		int[] currentWrapIndices = getAdjustedWrapIndices();
		ArrayList wrapIndices = new ArrayList();
		for (int i=0; i<currentWrapIndices.length; i++) {
			int wrapIndex = currentWrapIndices[i];
			if (wrapIndex < index) {
				wrapIndices.add(new Integer(wrapIndex));
			} else {
				break;
			}
		}
		
		int[] wraps = new int[wrapIndices.size()];
		for (int i=0; i<wrapIndices.size(); i++) {
			wraps[i] = ((Integer)wrapIndices.get(i)).intValue();
		}
		boolean redrawOff = false;
		if (wraps.length < currentWrapIndices.length) {
			// only set this if we have to, doing so will reduce
			// unnecessary flash
			redrawOff = true;
			coolBar.setRedraw(false);
			coolBar.setWrapIndices(wraps);
		}
		
		// Reset the item sizes, necessary to do so before we wrap the items.
		CoolItem[] coolItems = coolBar.getItems();
		for (int i = index; i < coolItems.length; i++) {
			CoolItem coolItem = coolItems[i];
			setSizeFor(coolItem);
		}
		
		// Wrap the items
		int coolBarWidth = coolBar.getClientArea().width;
		int spaceLeft = 0;
		for (int i=index; i<coolItems.length; i++) {
			CoolItem coolItem = coolItems[i];
			int preferredWidth = coolItem.getPreferredSize().x;
			if (preferredWidth <= spaceLeft) {
				// item fits, continue
				spaceLeft -= preferredWidth;
			} else {
				// item doesn't fit, starting a new row
				wrapIndices.add(new Integer(i));
				spaceLeft = coolBarWidth - preferredWidth;
			}
		}

		wraps = new int[wrapIndices.size()];
		for (int i=0; i<wrapIndices.size(); i++) {
			wraps[i] = ((Integer)wrapIndices.get(i)).intValue();
		}
		if (!redrawOff) {
			coolBar.setRedraw(false);
		}
		coolBar.setWrapIndices(wraps);
		relayout();		
		coolBar.setRedraw(true);
	}
	/**
	 */
	protected void resetLayout() {
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
	protected void relayout() {
		coolBar.getParent().layout();
	}
	/**
	 */
	public void setLayout(CoolBarLayout layout) {
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
	protected void setLayoutTo(CoolBarLayout layout) {
		// This method is called after update.  All of the coolbar items have
		// been created, now apply the layout to the coolbar. 

		if (layout == null) {
			coolBar.setRedraw(false);
			CoolItem[] coolItems = coolBar.getItems();
			int[] newItemOrder = new int[coolItems.length];
			// Reset the coolitem order to the creation order.  CoolItems are
			// created in contribution item order.  Only way to reset item 
			// order is to setItemLayout (no setItemOrder API).
			for (int i = 0; i < coolItems.length; i++) {
				newItemOrder[i]=i;
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
		// Handle those items that are on the coolbar, but not in the layout.
		// Just add these items at the end of the coolbar.
		ArrayList addedItems = new ArrayList();
		for (int i=0; i<found.length; i++) {
			if (found[i] == -1) {
				itemOrder[count]=i;
				CoolItem cItem = coolBar.getItem(count);
				itemSizes[count]=cItem.getSize();
				addedItems.add(cItem);
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
		int numItems = itemSizes.length;
		for (int i = 0; i < wrapItems.length; i++) {
			int index = currentCoolItemIds.indexOf(wrapItems[i]);
			if (index != -1) {
				wrapIndices[j] = index;
				j++;
			} else {
				// wrap item no longer exists, wrap on the next visual item 
				int visualIndex = layout.itemWrapIndices[i];
				if ((i+1) < wrapItems.length) {
					// there's another wrap row, set the wrap to the next
					// visual item as long as it isn't on the next row
					int nextWrapIndex = layout.itemWrapIndices[i+1];
					if ((visualIndex < nextWrapIndex) && (visualIndex < itemSizes.length)) {
						wrapIndices[j] = visualIndex;
						j++;
					}
				} else {
					// we're on the last row, set the wrap to the 
					// next visual item
					if (visualIndex < itemSizes.length) {
						wrapIndices[j] = visualIndex;
						j++;
					}
				}
			}
		}
		int[] itemWraps = new int[j];
		System.arraycopy(wrapIndices, 0, itemWraps, 0, j);
		coolBar.setWrapIndices(itemWraps);
		
		if (!addedItems.isEmpty()) {
			// these items have already been added to the coolbar
			// see if the items fit
			int startIndex = coolBar.getItemCount();
			for (int i=0; i<addedItems.size(); i++) {
				CoolItem item = (CoolItem)addedItems.get(i);
				int itemIndex = coolBar.indexOf(item);
				if (!fits(itemIndex)) {
					if (itemIndex < startIndex) {
						startIndex = itemIndex;
					}
				}
			}
			if (startIndex != coolBar.getItemCount()) {
				// redo the layout starting at the first visible item that
				// does not fit
				redoLayoutStartingAt(startIndex);
			}
		}
		coolBar.setRedraw(true);
	}
	private void setSizeFor(CoolItem coolItem) {
		ToolBar toolBar = (ToolBar) coolItem.getControl();
		int minWidth = toolBar.getItems()[0].getWidth();
		Point size = toolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point coolSize = coolItem.computeSize(size.x, size.y);
		// note setMinimumSize must be called before setSize, see PR 15565
		coolItem.setMinimumSize(minWidth, coolSize.y);
		coolItem.setSize(coolSize);
		coolItem.setPreferredSize(coolSize);
	}
	/**
	 */
	public void update(boolean force) {
		if (isDirty() || force) {
			if (coolBarExist()) {
				boolean useRedraw = false;
				
				// remove CoolBarItemContributions that are empty
				IContributionItem[] items = getItems();
				ArrayList cbItemsToRemove = new ArrayList(items.length);
				for (int i = 0; i < items.length; i++) {
					CoolBarContributionItem cbItem = (CoolBarContributionItem) items[i];
					if (cbItem.getItems().length == 0) {
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
						coolItemsToRemove.add(coolItem);
					}
				}
				if ((cbItemsToRemove.size() + coolItemsToRemove.size()) > 2) {
					useRedraw = true;
					coolBar.setRedraw(false);
				}
				
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
				setDirty(false);

				// workaround for 14330
				if(relock) {
					coolBar.setLocked(true);
				}
				if (useRedraw) coolBar.setRedraw(true);
			}
		}
	}
}