package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Point;

public class CoolBarLayout {
	public Point[] itemSizes = new Point[0];
	public int[] itemWrapIndices = new int[0];
	
	// the coolbar contribution items in order of display
	// store the item instead of indexes because the coolbar
	// items may change between saving/restoring
	ArrayList items = new ArrayList();
	
	public CoolBarLayout() {
	}
	public CoolBarLayout(ArrayList items, int[] itemWrapIndices, Point[] itemSizes) {
		this.items = items;
		this.itemWrapIndices = itemWrapIndices;
		this.itemSizes = itemSizes;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer(20);
		buffer.append("item wrap "); //$NON-NLS-1$
		for (int i=0; i<itemWrapIndices.length; i++) {
			buffer.append(itemWrapIndices[i] + " "); //$NON-NLS-1$
		}
		buffer.append('\n');
		buffer.append("item size "); //$NON-NLS-1$
		for (int i=0; i<itemSizes.length; i++) {
			buffer.append(itemSizes[i] + " "); //$NON-NLS-1$
		}
		buffer.append('\n');
		return buffer.toString();
	}		
}

    
