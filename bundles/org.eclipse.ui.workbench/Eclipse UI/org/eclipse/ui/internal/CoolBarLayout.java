package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;

public class CoolBarLayout {
	public Point[] itemSizes = new Point[0];
	public int[] itemWrapIndices = new int[0];

	// the coolbar contribution items in order of display
	// store the item id instead of indexes because the coolbar
	// items may change between saving/restoring
	ArrayList items = new ArrayList(0);

	ArrayList rememberedPositions = new ArrayList();

	public CoolBarLayout() {
	}
	public CoolBarLayout(CoolBar coolBar) {
		this();
		initialize(coolBar);
	}
	void initialize(CoolBar coolBar) {
		CoolItem[] coolItems = coolBar.getItems();
		ArrayList newItems = new ArrayList(coolItems.length);
		for (int i = 0; i < coolItems.length; i++) {
			CoolBarContributionItem item = (CoolBarContributionItem) coolItems[i].getData();
			if (item != null) {
				newItems.add(item.getId());
			}
		}
		items = newItems;
		itemSizes = coolBar.getItemSizes();
		itemWrapIndices = getAdjustedWrapIndices(coolBar.getWrapIndices());
	
		// Save the preferred size as actual size for the last item on a row
		int count = coolBar.getItemCount();
		int[] lastIndices = coolBar.getWrapIndices();
		int n = lastIndices.length;
		if (n == 0) {
			if (count == 0) lastIndices = new int[0];
			else lastIndices = new int[] {count - 1};
		} else {
			// convert from first item indices to last item indices
			for (int i = 0; i < n - 1; ++i) {
				lastIndices[i] = lastIndices[i+1] - 1;
			}
			lastIndices[n - 1] = count - 1;
		}
		for (int i = 0; i < lastIndices.length; i++) {
			int lastIndex = lastIndices[i];
			if (lastIndex >= 0 && lastIndex < coolItems.length) {
				CoolItem lastItem = coolItems[lastIndex];
				itemSizes[lastIndex] = lastItem.getPreferredSize();
			}
		}
	}
	public CoolBarLayout(ArrayList items, int[] itemWrapIndices, Point[] itemSizes, ArrayList rememberedPositions) {
		this.items = items;
		this.itemWrapIndices = itemWrapIndices;
		this.itemSizes = itemSizes;
		this.rememberedPositions = rememberedPositions;
	}
	/**
	 * Return the set of wrap indices that would be a result of adding
	 * an item at the given itemIndex on the the given row.
	 */
	int[] wrapsForNewItem(int rowIndex, int itemIndex) {
		int[] newWraps = null;
		int rowStartIndex = getStartIndexOfRow(rowIndex);
		if (itemIndex == rowStartIndex) {
			// if itemIndex is the start of the row, we are adding the item 
			// to the beginning of the row, so adjust the wrap indices
			newWraps = new int[itemWrapIndices.length];
			System.arraycopy(itemWrapIndices, 0, newWraps, 0, itemWrapIndices.length);
			newWraps[rowIndex] = itemIndex;
			for (int i = rowIndex + 1; i < newWraps.length; i++) {
				newWraps[i]++;
			}
		} 
		return newWraps;
	}
	/**
	 * Return the set of wrap indices that would be a result of adding
	 * an item at the given itemIndex on the given new row.
	 */
	int[]  wrapsForNewRow(int rowIndex, int itemIndex) {
		int[] newWraps = new int[itemWrapIndices.length + 1];
		for (int j = 0; j < rowIndex; j++) {
			newWraps[j]=itemWrapIndices[j];
		}
		newWraps[rowIndex] = itemIndex;
		for (int j = rowIndex; j < itemWrapIndices.length; j++) {
			newWraps[j+1]=itemWrapIndices[j]+1;
		}
		return newWraps;
	}
	/** 
	 * Return a consistent set of wrap indices.  The return value
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
	/** 
	 * Return the number of rows in the layout.
	 */
	int getNumberOfRows() {
		return itemWrapIndices.length;
	}
	/**
	 */
	int getRowOfIndex(int index) {
		int row = -1;
		for (row = 0; row < itemWrapIndices.length; row++) {
			if (itemWrapIndices[row] > index) break;
		}
		if (row > 0) row--;
		return row;
	}
	/**
	 * Return the item index of the first item on the given row.
	 */
	int getStartIndexOfRow(int rowIndex) {
		// return the item index of the first item in the given row
		if (rowIndex > itemWrapIndices.length - 1) return -1;  // row doesn't exist
		return itemWrapIndices[rowIndex];
	}
	boolean isOnRowAlone(int itemIndex) {
		int row = getRowOfIndex(itemIndex);
		int rowStart = getStartIndexOfRow(row);
		int nextRowStart = getStartIndexOfRow(row + 1);
		if (nextRowStart == -1) nextRowStart = rowStart;
		return (rowStart == itemIndex) && (nextRowStart - rowStart <= 1);
	}
	/**
	 * Return whether or not the items from this layout that are in
	 * otherLayout are in the same order within the layouts.
	 */
	/* package */ boolean isDerivativeOf(CoolBarLayout otherLayout) {
		// for the items that are in this layout, 
		// get the indexes of these items in otherLayout
		ArrayList indexes = new ArrayList();
		for (int i=0; i<this.items.size(); i++) {
			String itemId = (String)this.items.get(i);
			int index = otherLayout.items.indexOf(itemId);
			if (index != -1) indexes.add(new Integer(index));
		}
		// see if the items that are shared across the two
		// layouts are in the same order, if not return 
		// false
		int previous = -1;
		for (int i=0; i<indexes.size(); i++) {
			int value = ((Integer)indexes.get(i)).intValue();
			if (value > previous) {
				previous = value;
			} else {
				return false;
			}
		}
		return true;
	}
	/**
	 * Restores the object state in the given memento. Returns whether or not the
	 * restoration was successful.
	 * 
	 * @param memento the memento to save the object state in
	 */
	public boolean restoreState(IMemento memento) {
		Integer newLayout = memento.getInteger(IWorkbenchConstants.TAG_ACTION_SET);
		if (newLayout == null) return false;
		
		IMemento [] sizes = memento.getChildren(IWorkbenchConstants.TAG_ITEM_SIZE);
		if (sizes == null) return false;
		itemSizes = new Point[sizes.length];
		for (int i = 0; i < sizes.length; i++) {
			IMemento sizeMem = sizes[i];
			Integer x = sizeMem.getInteger(IWorkbenchConstants.TAG_X);
			if (x == null) return false;
			Integer y = sizeMem.getInteger(IWorkbenchConstants.TAG_Y);
			if (y == null) return false;
			itemSizes[i] = new Point(x.intValue(), y.intValue());
		}
		IMemento [] wraps = memento.getChildren(IWorkbenchConstants.TAG_ITEM_WRAP_INDEX);
		if (wraps == null) return false;
		itemWrapIndices = new int[wraps.length];
		for (int i = 0; i < wraps.length; i++) {
			IMemento wrapMem = wraps[i];
			Integer index = wrapMem.getInteger(IWorkbenchConstants.TAG_INDEX);
			if (index == null) return false;
			itemWrapIndices[i] = index.intValue();
		}
		IMemento [] savedItems = memento.getChildren(IWorkbenchConstants.TAG_ITEM);
		if (savedItems == null) return false;
		items = new ArrayList(savedItems.length);
		for (int i = 0; i < savedItems.length; i++) {
			IMemento savedMem = savedItems[i];
			String id = savedMem.getString(IWorkbenchConstants.TAG_ID);
			if (id == null) return false;
			items.add(id);
		}
		IMemento [] savedPositions = memento.getChildren(IWorkbenchConstants.TAG_POSITION);
		rememberedPositions = new ArrayList(savedPositions.length);
		for (int i=0; i < savedPositions.length; i++) {
			CoolItemPosition position = new CoolItemPosition();
			IMemento savedPos = savedPositions[i];
			position.id = savedPos.getString(IWorkbenchConstants.TAG_ID);
			if (position.id == null) return false;
			Integer pos = savedPos.getInteger(IWorkbenchConstants.TAG_ADDED);
			if (pos == null) return false;
			int added = pos.intValue();
			position.added = added == 1;
			position.layout = new CoolBarLayout();
			IMemento layoutMemento = savedPos.getChild(IWorkbenchConstants.TAG_LAYOUT);
			if (layoutMemento == null) return false;
			position.layout.restoreState(layoutMemento);
			rememberedPositions.add(position);
		}
		return true;	
	}
	/**
	 * Saves the object state in the given memento. 
	 * 
	 * @param memento the memento to save the object state in
	 */
	public IStatus saveState(IMemento memento) {
		// tag used to indicate whether or not adding actions to other
		// coolitems is supported
		memento.putInteger(IWorkbenchConstants.TAG_ACTION_SET, 1);

		for (int i = 0; i < itemSizes.length; i++) {
			IMemento child = memento.createChild(IWorkbenchConstants.TAG_ITEM_SIZE);
			Point pt = itemSizes[i];
			child.putInteger(IWorkbenchConstants.TAG_X, pt.x);
			child.putInteger(IWorkbenchConstants.TAG_Y, pt.y);
		}
		for (int i = 0; i < itemWrapIndices.length; i++) {
			IMemento child = memento.createChild(IWorkbenchConstants.TAG_ITEM_WRAP_INDEX);
			child.putInteger(IWorkbenchConstants.TAG_INDEX, itemWrapIndices[i]);
		}
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			IMemento child = memento.createChild(IWorkbenchConstants.TAG_ITEM);
			String item = (String)iter.next();
			child.putString(IWorkbenchConstants.TAG_ID, item);
		}
		iter = rememberedPositions.iterator();
		while (iter.hasNext()) {
			IMemento child = memento.createChild(IWorkbenchConstants.TAG_POSITION);
			CoolItemPosition position = (CoolItemPosition)iter.next();
			child.putString(IWorkbenchConstants.TAG_ID, position.id);
			int value = 0;
			if (position.added) value = 1;
			child.putInteger(IWorkbenchConstants.TAG_ADDED, value);
			IMemento layout = child.createChild(IWorkbenchConstants.TAG_LAYOUT);
			position.layout.saveState(layout); 
		}
		return new Status(IStatus.OK,PlatformUI.PLUGIN_ID,0,"",null); //$NON-NLS-1$
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer(20);
		buffer.append("items "); //$NON-NLS-1$
		for (int i = 0; i < items.size(); i++) {
			String item = (String)items.get(i);
			buffer.append(item + " "); //$NON-NLS-1$
		}
		buffer.append('\n');
		buffer.append("item wrap indices "); //$NON-NLS-1$
		for (int i = 0; i < itemWrapIndices.length; i++) {
			buffer.append(itemWrapIndices[i] + " "); //$NON-NLS-1$
		}
		buffer.append('\n');
		buffer.append("item sizes "); //$NON-NLS-1$
		for (int i = 0; i < itemSizes.length; i++) {
			buffer.append(itemSizes[i] + " "); //$NON-NLS-1$
		}
		buffer.append('\n');
		return buffer.toString();
	}
}