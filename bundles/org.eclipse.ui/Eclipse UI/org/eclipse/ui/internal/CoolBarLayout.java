package org.eclipse.ui.internal;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Point;

public class CoolBarLayout {
	public int[] itemOrder = new int[0];
	public Point[] itemSizes = new Point[0];
	public int[] itemWrapIndices = new int[0];
	
	// the coolbar contribution items in order of display
	ArrayList items = new ArrayList();
	
	public CoolBarLayout() {
	}
	public CoolBarLayout(int[] itemOrder, int[] itemWrapIndices, Point[] itemSizes) {
		this.itemOrder = itemOrder;
		this.itemWrapIndices = itemWrapIndices;
		this.itemSizes = itemSizes;
	}
	public CoolBarLayout(ArrayList items, int[] itemWrapIndices, Point[] itemSizes) {
		this.items = items;
		this.itemWrapIndices = itemWrapIndices;
		this.itemSizes = itemSizes;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer(20);
		buffer.append("item order ");
		for (int i=0; i<itemOrder.length; i++) {
			buffer.append(itemOrder[i] + " ");
		}
		buffer.append('\n');
		buffer.append("item wrap ");
		for (int i=0; i<itemWrapIndices.length; i++) {
			buffer.append(itemWrapIndices[i] + " ");
		}
		buffer.append('\n');
		buffer.append("item size ");
		for (int i=0; i<itemSizes.length; i++) {
			buffer.append(itemSizes[i] + " ");
		}
		buffer.append('\n');
		return buffer.toString();
	}		
}

    
