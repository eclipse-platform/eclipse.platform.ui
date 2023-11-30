/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.internal.provisional.action.IToolBarManager2;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Menu;

/**
 * A cool bar manager is a contribution manager which realizes itself and its
 * items in a cool bar control.
 * <p>
 * This class may be instantiated; it may also be subclassed.
 * </p>
 *
 * @since 3.0
 */
public class CoolBarManager extends ContributionManager implements ICoolBarManager {

	/**
	 * A separator created by the end user.
	 */
	public static final String USER_SEPARATOR = "UserSeparator"; //$NON-NLS-1$

	/**
	 * The original creation order of the contribution items.
	 */
	private ArrayList<IContributionItem> cbItemsCreationOrder = new ArrayList<>();

	/**
	 * MenuManager for cool bar pop-up menu, or null if none.
	 */
	private MenuManager contextMenuManager = null;

	/**
	 * The cool bar control; <code>null</code> before creation and after disposal.
	 */
	private CoolBar coolBar = null;

	/**
	 * The cool bar items style; <code>SWT.NONE</code> by default.
	 */
	private int itemStyle = SWT.NONE;

	/**
	 * Creates a new cool bar manager with the default style. Equivalent to
	 * <code>CoolBarManager(SWT.NONE)</code>.
	 */
	public CoolBarManager() {
		// do nothing
	}

	/**
	 * Creates a cool bar manager for an existing cool bar control. This manager
	 * becomes responsible for the control, and will dispose of it when the manager
	 * is disposed.
	 *
	 * @param coolBar the cool bar control
	 */
	public CoolBarManager(CoolBar coolBar) {
		this();
		Assert.isNotNull(coolBar);
		this.coolBar = coolBar;
		itemStyle = coolBar.getStyle();
	}

	/**
	 * Creates a cool bar manager with the given SWT style. Calling
	 * <code>createControl</code> will create the cool bar control.
	 *
	 * @param style the cool bar item style; see
	 *              {@link org.eclipse.swt.widgets.CoolBar CoolBar}for for valid
	 *              style bits
	 */
	public CoolBarManager(int style) {
		itemStyle = style;
	}

	@Override
	public void add(IToolBarManager toolBarManager) {
		Assert.isNotNull(toolBarManager);
		super.add(new ToolBarContributionItem(toolBarManager));
	}

	/**
	 * Collapses consecutive separators and removes a separator from the beginning
	 * and end of the list.
	 *
	 * @param contributionList the list of contributions; must not be
	 *                         <code>null</code>.
	 * @return The contribution list provided with extraneous separators removed;
	 *         this value is never <code>null</code>, but may be empty.
	 */
	private ArrayList<IContributionItem> adjustContributionList(ArrayList<IContributionItem> contributionList) {
		IContributionItem item;
		// Fist remove a separator if it is the first element of the list
		if (!contributionList.isEmpty()) {
			item = contributionList.get(0);
			if (item.isSeparator()) {
				contributionList.remove(0);
			}

			ListIterator<IContributionItem> iterator = contributionList.listIterator();
			// collapse consecutive separators
			while (iterator.hasNext()) {
				item = iterator.next();
				if (item.isSeparator()) {
					while (iterator.hasNext()) {
						item = iterator.next();
						if (item.isSeparator()) {
							iterator.remove();
						} else {
							break;
						}
					}

				}
			}
			if (!contributionList.isEmpty()) {
				// Now check last element to see if there is a separator
				item = contributionList.get(contributionList.size() - 1);
				if (item.isSeparator()) {
					contributionList.remove(contributionList.size() - 1);
				}
			}
		}
		return contributionList;

	}

	@Override
	protected boolean allowItem(IContributionItem itemToAdd) {
		/*
		 * We will allow as many null entries as they like, though there should be none.
		 */
		if (itemToAdd == null) {
			return true;
		}

		/*
		 * Null identifiers can be expected in generic contribution items.
		 */
		String firstId = itemToAdd.getId();
		if (firstId == null) {
			return true;
		}

		// Cycle through the current list looking for duplicates.
		IContributionItem[] currentItems = getItems();
		for (IContributionItem currentItem : currentItems) {
			// We ignore null entries.
			if (currentItem == null) {
				continue;
			}

			String secondId = currentItem.getId();
			if (firstId.equals(secondId)) {
				if (Policy.TRACE_TOOLBAR) {
					System.out.println("Trying to add a duplicate item."); //$NON-NLS-1$
					new Exception().printStackTrace(System.out);
					System.out.println("DONE --------------------------"); //$NON-NLS-1$
				}
				return false;
			}
		}

		return true;
	}

	/**
	 * Positions the list iterator to the end of all the separators. Calling
	 * <code>next()</code> the iterator should return the immediate object following
	 * the last separator.
	 *
	 * @param iterator the list iterator.
	 */
	private void collapseSeparators(ListIterator<IContributionItem> iterator) {

		while (iterator.hasNext()) {
			IContributionItem item = iterator.next();
			if (!item.isSeparator()) {
				iterator.previous();
				return;
			}
		}
	}

	/**
	 * Returns whether the cool bar control has been created and not yet disposed.
	 *
	 * @return <code>true</code> if the control has been created and not yet
	 *         disposed, <code>false</code> otherwise
	 */
	private boolean coolBarExist() {
		return coolBar != null && !coolBar.isDisposed();
	}

	/**
	 * Creates and returns this manager's cool bar control. Does not create a new
	 * control if one already exists.
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
			update(false);
		}
		return coolBar;
	}

	/**
	 * Disposes of this cool bar manager and frees all allocated SWT resources.
	 * Notifies all contribution items of the dispose. Note that this method does
	 * not clean up references between this cool bar manager and its associated
	 * contribution items. Use <code>removeAll</code> for that purpose.
	 */
	public void dispose() {
		if (coolBarExist()) {
			coolBar.dispose();
			coolBar = null;
		}
		IContributionItem[] items = getItems();
		for (IContributionItem item : items) {
			// Disposes of the contribution item.
			// If Contribution Item is a toolbar then it will dispose of
			// all the nested
			// contribution items.
			item.dispose();
		}
		// If a context menu existed then dispose of it.
		if (contextMenuManager != null) {
			contextMenuManager.dispose();
			contextMenuManager = null;
		}

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
				// we created it, we dispose it, see bug 293433
				control.dispose();
			}
			item.dispose();
		}
	}

	/**
	 * Finds the cool item associated with the given contribution item.
	 *
	 * @param item the contribution item
	 * @return the associated cool item, or <code>null</code> if not found
	 */
	private CoolItem findCoolItem(IContributionItem item) {
		if (!coolBarExist()) {
			return null;
		}
		CoolItem[] coolItems = coolBar.getItems();
		return findCoolItem(coolItems, item);
	}

	private CoolItem findCoolItem(CoolItem[] items, IContributionItem item) {
		if (items == null) {
			return null;
		}

		for (CoolItem coolItem : items) {
			IContributionItem data = (IContributionItem) coolItem.getData();
			if (data != null && data.equals(item)) {
				return coolItem;
			}
		}
		return null;
	}

	/**
	 * Return a consistent set of wrap indices. The return value will always include
	 * at least one entry and the first entry will always be zero.
	 * CoolBar.getWrapIndices() is inconsistent in whether or not it returns an
	 * index for the first row.
	 *
	 * @param wraps the wrap indicies from the cool bar widget
	 * @return the adjusted wrap indicies.
	 */
	private int[] getAdjustedWrapIndices(int[] wraps) {
		int[] adjustedWrapIndices;
		if (wraps.length == 0) {
			adjustedWrapIndices = new int[] { 0 };
		} else if (wraps[0] != 0) {
			adjustedWrapIndices = new int[wraps.length + 1];
			adjustedWrapIndices[0] = 0;
			System.arraycopy(wraps, 0, adjustedWrapIndices, 1, wraps.length);
		} else {
			adjustedWrapIndices = wraps;
		}
		return adjustedWrapIndices;
	}

	/**
	 * Returns the control of the Menu Manager. If the menu manager does not have a
	 * control then one is created.
	 *
	 * @return menu control associated with manager, or null if none
	 */
	private Menu getContextMenuControl() {
		if ((contextMenuManager != null) && (coolBar != null)) {
			Menu menuWidget = contextMenuManager.getMenu();
			if ((menuWidget == null) || (menuWidget.isDisposed())) {
				menuWidget = contextMenuManager.createContextMenu(coolBar);
			}
			return menuWidget;
		}
		return null;
	}

	@Override
	public IMenuManager getContextMenuManager() {
		return contextMenuManager;
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
	 * Returns an array list of all the contribution items in the manager.
	 *
	 * @return an array list of contribution items.
	 */
	private ArrayList<IContributionItem> getItemList() {
		IContributionItem[] cbItems = getItems();
		ArrayList<IContributionItem> list = new ArrayList<>(cbItems.length);
		list.addAll(Arrays.asList(cbItems));
		return list;
	}

	@Override
	public boolean getLockLayout() {
		if (!coolBarExist()) {
			return false;
		}
		return coolBar.getLocked();
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
		for (IContributionItem item : items) {
			if (item.isSeparator()) {
				separatorFound = true;
			}
			if ((separatorFound) && (isChildVisible(item)) && (!item.isGroupMarker()) && (!item.isSeparator())) {
				numRows++;
				separatorFound = false;
			}
		}
		return numRows;
	}

	@Override
	public int getStyle() {
		return itemStyle;
	}

	/**
	 * Subclasses may extend this <code>ContributionManager</code> method, but must
	 * call <code>super.itemAdded</code>.
	 *
	 * @see org.eclipse.jface.action.ContributionManager#itemAdded(org.eclipse.jface.action.IContributionItem)
	 */
	@Override
	protected void itemAdded(IContributionItem item) {
		Assert.isNotNull(item);
		super.itemAdded(item);
		int insertedAt = indexOf(item);
		boolean replaced = false;
		final int size = cbItemsCreationOrder.size();
		for (int i = 0; i < size; i++) {
			IContributionItem created = cbItemsCreationOrder.get(i);
			if (created.getId() != null && created.getId().equals(item.getId())) {
				cbItemsCreationOrder.set(i, item);
				replaced = true;
				break;
			}
		}

		if (!replaced) {
			cbItemsCreationOrder.add(Math.min(Math.max(insertedAt, 0), cbItemsCreationOrder.size()), item);
		}
	}

	/**
	 * Subclasses may extend this <code>ContributionManager</code> method, but must
	 * call <code>super.itemRemoved</code>.
	 *
	 * @see org.eclipse.jface.action.ContributionManager#itemRemoved(org.eclipse.jface.action.IContributionItem)
	 */
	@Override
	protected void itemRemoved(IContributionItem item) {
		Assert.isNotNull(item);
		super.itemRemoved(item);
		CoolItem coolItem = findCoolItem(item);
		if (coolItem != null) {
			coolItem.setData(null);
		}
	}

	/**
	 * Positions the list iterator to the starting of the next row. By calling next
	 * on the returned iterator, it will return the first element of the next row.
	 *
	 * @param iterator          the list iterator of contribution items
	 * @param ignoreCurrentItem Whether the current item in the iterator should be
	 *                          considered (as well as subsequent items).
	 */
	private void nextRow(ListIterator<IContributionItem> iterator, boolean ignoreCurrentItem) {

		IContributionItem currentElement = null;
		if (!ignoreCurrentItem && iterator.hasPrevious()) {
			currentElement = iterator.previous();
			iterator.next();
		}

		if ((currentElement != null) && (currentElement.isSeparator())) {
			collapseSeparators(iterator);
			return;
		}

		// Find next separator
		while (iterator.hasNext()) {
			IContributionItem item = iterator.next();
			if (item.isSeparator()) {
				// we we find a separator, collapse any consecutive
				// separators
				// and return
				collapseSeparators(iterator);
				return;
			}
		}
	}

	/*
	 * Used for debuging. Prints all the items in the internal structures.
	 */
	// private void printContributions(ArrayList contributionList) {
	// int index = 0;
	// System.out.println("----------------------------------\n"); //$NON-NLS-1$
	// for (Iterator i = contributionList.iterator(); i.hasNext(); index++) {
	// IContributionItem item = (IContributionItem) i.next();
	// if (item.isSeparator()) {
	// System.out.println("Separator"); //$NON-NLS-1$
	// } else {
	// System.out.println(index + ". Item id: " + item.getId() //$NON-NLS-1$
	// + " - is Visible: " //$NON-NLS-1$
	// + item.isVisible());
	// }
	// }
	// }
	/**
	 * Synchronizes the visual order of the cool items in the control with this
	 * manager's internal data structures. This method should be called before
	 * requesting the order of the contribution items to ensure that the order is
	 * accurate.
	 * <p>
	 * Note that <code>update()</code> and <code>refresh()</code> are converses:
	 * <code>update()</code> changes the visual order to match the internal
	 * structures, and <code>refresh</code> changes the internal structures to match
	 * the visual order.
	 * </p>
	 */
	public void refresh() {
		if (!coolBarExist()) {
			return;
		}

		// Retreives the list of contribution items as an array list
		ArrayList<IContributionItem> contributionList = getItemList();

		// Check the size of the list
		if (contributionList.isEmpty()) {
			return;
		}

		// The list of all the cool items in their visual order
		CoolItem[] coolItems = coolBar.getItems();
		// The wrap indicies of the coolbar
		int[] wrapIndicies = getAdjustedWrapIndices(coolBar.getWrapIndices());

		int row = 0;
		int coolItemIndex = 0;

		// Traverse through all cool items in the coolbar add them to a new
		// data structure
		// in the correct order
		ArrayList<IContributionItem> displayedItems = new ArrayList<>(coolBar.getItemCount());
		for (int i = 0; i < coolItems.length; i++) {
			CoolItem coolItem = coolItems[i];
			if (coolItem.getData() instanceof IContributionItem) {
				IContributionItem cbItem = (IContributionItem) coolItem.getData();
				displayedItems.add(Math.min(i, displayedItems.size()), cbItem);
			}
		}

		// Add separators to the displayed Items data structure
		int offset = 0;
		for (int i = 1; i < wrapIndicies.length; i++) {
			int insertAt = wrapIndicies[i] + offset;
			displayedItems.add(insertAt, new Separator(USER_SEPARATOR));
			offset++;
		}

		// Determine which rows are invisible
		ArrayList<Integer> existingVisibleRows = new ArrayList<>(4);
		ListIterator<IContributionItem> rowIterator = contributionList.listIterator();
		collapseSeparators(rowIterator);
		int numRow = 0;
		while (rowIterator.hasNext()) {
			// Scan row
			while (rowIterator.hasNext()) {
				IContributionItem cbItem = rowIterator.next();
				if (displayedItems.contains(cbItem)) {
					existingVisibleRows.add(Integer.valueOf(numRow));
					break;
				}
				if (cbItem.isSeparator()) {
					break;
				}
			}
			nextRow(rowIterator, false);
			numRow++;
		}

		Iterator<Integer> existingRows = existingVisibleRows.iterator();
		// Adjust row number to the first visible
		if (existingRows.hasNext()) {
			row = existingRows.next().intValue();
		}

		HashMap<IContributionItem, Integer> itemLocation = new HashMap<>();
		for (ListIterator<IContributionItem> locationIterator = displayedItems.listIterator(); locationIterator
				.hasNext();) {
			IContributionItem item = locationIterator.next();
			if (item.isSeparator()) {
				if (existingRows.hasNext()) {
					Integer value = existingRows.next();
					row = value.intValue();
				} else {
					row++;
				}
			} else {
				itemLocation.put(item, Integer.valueOf(row));
			}

		}

		// Insert the contribution items in their correct location
		for (ListIterator<IContributionItem> iterator = displayedItems.listIterator(); iterator.hasNext();) {
			IContributionItem cbItem = iterator.next();
			if (cbItem.isSeparator()) {
				coolItemIndex = 0;
			} else {
				relocate(cbItem, coolItemIndex, contributionList, itemLocation);
				cbItem.saveWidgetState();
				coolItemIndex++;
			}
		}

		contributionList = adjustContributionList(contributionList);
		if (!contributionList.isEmpty()) {
			IContributionItem[] array = new IContributionItem[contributionList.size() - 1];
			array = contributionList.toArray(array);
			internalSetItems(array);
		}

	}

	/**
	 * Relocates the given contribution item to the specified index.
	 *
	 * @param cbItem           the conribution item to relocate
	 * @param index            the index to locate this item
	 * @param contributionList the current list of conrtributions
	 */
	private void relocate(IContributionItem cbItem, int index, ArrayList<IContributionItem> contributionList,
			HashMap<IContributionItem, Integer> itemLocation) {

		if ((itemLocation.get(cbItem) == null)) {
			return;
		}
		int targetRow = itemLocation.get(cbItem).intValue();

		int cbInternalIndex = contributionList.indexOf(cbItem);

		// by default add to end of list
		int insertAt = contributionList.size();
		// Find the row to place this item in.
		ListIterator<IContributionItem> iterator = contributionList.listIterator();
		// bypass any separators at the begining
		collapseSeparators(iterator);
		int currentRow = -1;
		while (iterator.hasNext()) {

			currentRow++;
			if (currentRow == targetRow) {
				// We found the row to insert the item
				int virtualIndex = 0;
				insertAt = iterator.nextIndex();
				// first check the position of the current element (item)
				// then get the next element
				while (iterator.hasNext()) {
					IContributionItem item = iterator.next();
					Integer itemRow = itemLocation.get(item);
					if (item.isSeparator()) {
						break;
					}
					// if the item has an associate widget
					if ((itemRow != null) && (itemRow.intValue() == targetRow)) {
						// if the next element is the index we are looking for
						// then break
						if (virtualIndex >= index) {
							break;
						}
						virtualIndex++;

					}
					insertAt++;
				}
				// If we don't need to move it then we return
				if (cbInternalIndex == insertAt) {
					return;
				}
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
		contributionList.add(insertAt, cbItem);

	}

	/**
	 * Restores the canonical order of this cool bar manager. The canonical order is
	 * the order in which the contribution items where added.
	 */
	public void resetItemOrder() {
		for (ListIterator<IContributionItem> iterator = cbItemsCreationOrder.listIterator(); iterator.hasNext();) {
			IContributionItem item = iterator.next();
			// if its a user separator then do not include in original order.
			if ((item.getId() != null) && (item.getId().equals(USER_SEPARATOR))) {
				iterator.remove();
			}
		}
		IContributionItem[] itemsToSet = new IContributionItem[cbItemsCreationOrder.size()];
		cbItemsCreationOrder.toArray(itemsToSet);
		setItems(itemsToSet);
	}

	@Override
	public void setContextMenuManager(IMenuManager contextMenuManager) {
		this.contextMenuManager = (MenuManager) contextMenuManager;
		if (coolBar != null) {
			coolBar.setMenu(getContextMenuControl());
		}
	}

	/**
	 * Replaces the current items with the given items. Forces an update.
	 *
	 * @param newItems the items with which to replace the current items
	 */
	public void setItems(IContributionItem[] newItems) {
		// dispose of all the cool items on the cool bar manager
		if (coolBar != null) {
			CoolItem[] coolItems = coolBar.getItems();
			for (CoolItem coolItem : coolItems) {
				dispose(coolItem);
			}
		}
		// Set the internal structure to this order
		internalSetItems(newItems);
		// Force and update
		update(true);
	}

	@Override
	public void setLockLayout(boolean value) {
		if (!coolBarExist()) {
			return;
		}
		coolBar.setLocked(value);
	}

	/**
	 * Subclasses may extend this <code>IContributionManager</code> method, but must
	 * call <code>super.update</code>.
	 *
	 * @see org.eclipse.jface.action.IContributionManager#update(boolean)
	 */
	@Override
	public void update(boolean force) {
		if ((!isDirty() && !force) || (!coolBarExist())) {
			return;
		}

		boolean relock = false;
		boolean changed = false;

		try {
			coolBar.setRedraw(false);

			// Refresh the widget data with the internal data structure.
			refresh();

			if (coolBar.getLocked()) {
				coolBar.setLocked(false);
				relock = true;
			}

			/*
			 * Make a list of items including only those items that are visible. Separators
			 * should stay because they mark line breaks in a cool bar.
			 */
			final IContributionItem[] items = getItems();
			final List<IContributionItem> visibleItems = new ArrayList<>(items.length);
			for (final IContributionItem item : items) {
				if (isChildVisible(item)) {
					visibleItems.add(item);
				}
			}

			/*
			 * Make a list of CoolItem widgets in the cool bar for which there is no current
			 * visible contribution item. These are the widgets to be disposed. Dynamic
			 * items are also removed.
			 */
			CoolItem[] coolItems = coolBar.getItems();
			final ArrayList<CoolItem> coolItemsToRemove = new ArrayList<>(coolItems.length);
			for (CoolItem coolItem : coolItems) {
				final Object data = coolItem.getData();
				if ((data == null) || (!visibleItems.contains(data))
						|| ((data instanceof IContributionItem) && ((IContributionItem) data).isDynamic())) {
					coolItemsToRemove.add(coolItem);
				}
			}

			// Dispose of any items in the list to be removed.
			for (int i = coolItemsToRemove.size() - 1; i >= 0; i--) {
				CoolItem coolItem = coolItemsToRemove.get(i);
				if (!coolItem.isDisposed()) {
					Control control = coolItem.getControl();
					if (control != null) {
						coolItem.setControl(null);
						control.dispose();
					}
					coolItem.dispose();
				}
			}

			// Add any new items by telling them to fill.
			coolItems = coolBar.getItems();
			IContributionItem sourceItem;
			IContributionItem destinationItem;
			int sourceIndex = 0;
			int destinationIndex = 0;
			final Iterator<IContributionItem> visibleItemItr = visibleItems.iterator();
			while (visibleItemItr.hasNext()) {
				sourceItem = visibleItemItr.next();

				// Retrieve the corresponding contribution item from SWT's
				// data.
				if (sourceIndex < coolItems.length) {
					destinationItem = (IContributionItem) coolItems[sourceIndex].getData();
				} else {
					destinationItem = null;
				}

				// The items match is they are equal or both separators.
				if (destinationItem != null) {
					if (sourceItem.equals(destinationItem)) {
						sourceIndex++;
						destinationIndex++;
						sourceItem.update();
						continue;

					} else if ((destinationItem.isSeparator()) && (sourceItem.isSeparator())) {
						coolItems[sourceIndex].setData(sourceItem);
						sourceIndex++;
						destinationIndex++;
						sourceItem.update();
						continue;

					}
				}

				// Otherwise, a new item has to be added.
				final int start = coolBar.getItemCount();
				if (sourceItem instanceof ToolBarContributionItem) {
					IToolBarManager manager = ((ToolBarContributionItem) sourceItem).getToolBarManager();
					if (manager instanceof IToolBarManager2) {
						((IToolBarManager2) manager).setOverrides(getOverrides());
					}
				}
				sourceItem.fill(coolBar, destinationIndex);
				final int newItems = coolBar.getItemCount() - start;
				for (int i = 0; i < newItems; i++) {
					coolBar.getItem(destinationIndex++).setData(sourceItem);
				}
				changed = true;
			}

			// Remove any old widgets not accounted for.
			for (int i = coolItems.length - 1; i >= sourceIndex; i--) {
				final CoolItem item = coolItems[i];
				if (!item.isDisposed()) {
					Control control = item.getControl();
					if (control != null) {
						item.setControl(null);
						control.dispose();
					}
					item.dispose();
					changed = true;
				}
			}

			// Update wrap indices.
			updateWrapIndices();

			// Update the sizes.
			for (IContributionItem item : items) {
				item.update(SIZE);
			}

			// if the coolBar was previously locked then lock it
			if (relock) {
				coolBar.setLocked(true);
			}

			if (changed) {
				updateTabOrder();
			}

			// We are no longer dirty.
			setDirty(false);
		} finally {
			coolBar.setRedraw(true);
		}
	}

	/**
	 * Sets the tab order of the coolbar to the visual order of its items.
	 */
	/* package */void updateTabOrder() {
		if (coolBar != null) {
			CoolItem[] items = coolBar.getItems();
			if (items != null) {
				ArrayList<Control> children = new ArrayList<>(items.length);
				for (CoolItem item : items) {
					if ((item.getControl() != null) && (!item.getControl().isDisposed())) {
						children.add(item.getControl());
					}
				}
				// Convert array
				Control[] childrenArray = new Control[0];
				childrenArray = children.toArray(childrenArray);

				if (childrenArray != null) {
					coolBar.setTabList(childrenArray);
				}

			}
		}
	}

	/**
	 * Updates the indices at which the cool bar should wrap.
	 */
	private void updateWrapIndices() {
		final IContributionItem[] items = getItems();
		final int numRows = getNumRows(items) - 1;

		// Generate the list of wrap indices.
		final int[] wrapIndices = new int[numRows];
		boolean foundSeparator = false;
		int j = 0;
		CoolItem[] coolItems = (coolBar == null) ? null : coolBar.getItems();

		for (IContributionItem item : items) {
			CoolItem coolItem = findCoolItem(coolItems, item);
			if (item.isSeparator()) {
				foundSeparator = true;
			}
			if ((!item.isSeparator()) && (!item.isGroupMarker()) && (isChildVisible(item)) && (coolItem != null)
					&& (foundSeparator)) {
				wrapIndices[j] = coolBar.indexOf(coolItem);
				j++;
				foundSeparator = false;
			}
		}

		/*
		 * Check to see if these new wrap indices are different than the old ones.
		 */
		final int[] oldIndices = coolBar.getWrapIndices();
		boolean shouldUpdate = false;
		if (oldIndices.length == wrapIndices.length) {
			for (int i = 0; i < oldIndices.length; i++) {
				if (oldIndices[i] != wrapIndices[i]) {
					shouldUpdate = true;
					break;
				}
			}
		} else {
			shouldUpdate = true;
		}

		if (shouldUpdate) {
			coolBar.setWrapIndices(wrapIndices);
		}
	}

	private boolean isChildVisible(IContributionItem item) {
		Boolean v;

		IContributionManagerOverrides overrides = getOverrides();
		if (overrides == null) {
			v = null;
		} else {
			v = getOverrides().getVisible(item);
		}

		if (v != null) {
			return v.booleanValue();
		}
		return item.isVisible();
	}
}
