package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.CoolItem;

public class CoolBarLayout {
	public boolean locked = false;
	public Point[] itemSizes = new Point[0];
	public int[] itemWrapIndices = new int[0];
	
	// the coolbar contribution items in order of display
	// store the item instead of indexes because the coolbar
	// items may change between saving/restoring
	ArrayList items = new ArrayList(0);
	
	public CoolBarLayout() {
	}
	public CoolBarLayout(ArrayList items, int[] itemWrapIndices, Point[] itemSizes) {
		this.items = items;
		this.itemWrapIndices = itemWrapIndices;
		this.itemSizes = itemSizes;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer(20);
		buffer.append("items "); //$NON-NLS-1$
		for (int i=0; i<items.size(); i++) {
			CoolBarContributionItem item = (CoolBarContributionItem)items.get(i);
			buffer.append(item.getId() + " ");
		}
		buffer.append('\n');
		buffer.append("item wrap indices "); //$NON-NLS-1$
		for (int i=0; i<itemWrapIndices.length; i++) {
			buffer.append(itemWrapIndices[i] + " "); //$NON-NLS-1$
		}
		buffer.append('\n');
		buffer.append("item sizes "); //$NON-NLS-1$
		for (int i=0; i<itemSizes.length; i++) {
			buffer.append(itemSizes[i] + " "); //$NON-NLS-1$
		}
		buffer.append('\n');
		return buffer.toString();
	}		
}

    
