package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IMemento;

public class CoolBarLayout {
	public Point[] itemSizes = new Point[0];
	public int[] itemWrapIndices = new int[0];

	// the coolbar contribution items in order of display
	// store the item id instead of indexes because the coolbar
	// items may change between saving/restoring
	ArrayList items = new ArrayList(0);

	public CoolBarLayout() {
	}
	public CoolBarLayout(ArrayList items, int[] itemWrapIndices, Point[] itemSizes) {
		this.items = items;
		this.itemWrapIndices = itemWrapIndices;
		this.itemSizes = itemSizes;
	}
	/**
	 * Restores the object state in the given memento. 
	 * 
	 * @param memento the memento to save the object state in
	 */
	public void restoreState(IMemento memento) {
		IMemento [] sizes = memento.getChildren(IWorkbenchConstants.TAG_ITEM_SIZE);
		itemSizes = new Point[sizes.length];
		for (int i = 0; i < sizes.length; i++) {
			IMemento sizeMem = sizes[i];
			Integer x = sizeMem.getInteger(IWorkbenchConstants.TAG_X);
			Integer y = sizeMem.getInteger(IWorkbenchConstants.TAG_Y);
			itemSizes[i] = new Point(x.intValue(), y.intValue());
		}
		IMemento [] wraps = memento.getChildren(IWorkbenchConstants.TAG_ITEM_WRAP_INDEX);
		itemWrapIndices = new int[wraps.length];
		for (int i = 0; i < wraps.length; i++) {
			IMemento wrapMem = wraps[i];
			Integer index = wrapMem.getInteger(IWorkbenchConstants.TAG_INDEX);
			itemWrapIndices[i] = index.intValue();
		}
		IMemento [] savedItems = memento.getChildren(IWorkbenchConstants.TAG_ITEM);
		items = new ArrayList(savedItems.length);
		for (int i = 0; i < savedItems.length; i++) {
			IMemento savedMem = savedItems[i];
			String id = savedMem.getString(IWorkbenchConstants.TAG_ID);
			items.add(id);
		}	
	}
	/**
	 * Saves the object state in the given memento. 
	 * 
	 * @param memento the memento to save the object state in
	 */
	public void saveState(IMemento memento) {
		for (int i = 0; i < itemSizes.length; i++) {
			IMemento child = memento.createChild(IWorkbenchConstants.TAG_ITEM_SIZE);
			Point pt = itemSizes[i];
			child.putInteger(IWorkbenchConstants.TAG_X, pt.x);
			child.putInteger(IWorkbenchConstants.TAG_Y, pt.y);
		}
		for (int i = 0; i < itemWrapIndices.length; i++) {
			IMemento child = memento.createChild(IWorkbenchConstants.TAG_ITEM_WRAP_INDEX);
			Point pt = itemSizes[i];
			child.putInteger(IWorkbenchConstants.TAG_INDEX, itemWrapIndices[i]);
		}
		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			IMemento child = memento.createChild(IWorkbenchConstants.TAG_ITEM);
			String item = (String)iter.next();
			child.putString(IWorkbenchConstants.TAG_ID, item);
		}
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer(20);
		buffer.append("items "); //$NON-NLS-1$
		for (int i = 0; i < items.size(); i++) {
			String item = (String)items.get(i);
			buffer.append(item + " ");
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