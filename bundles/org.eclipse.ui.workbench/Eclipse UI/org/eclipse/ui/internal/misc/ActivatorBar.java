package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * A Label which supports aligned text and/or an image and different border styles.
 * If there is not enough space a SmartLabel uses the following strategy to fit the information into the
 * available space:
 * <pre>
 * - ignores the indent in left align mode
 * - ignores the image and the gap
 * - shortens the text by replacing the center portion of the label with an ellipsis
 * - shortens the text by removing the center portion of the label
 * </pre>
 */
public class ActivatorBar extends Canvas implements PaintListener {
	private ActivatorItem activeItem = null;
	private ItemListener listener;
	private ArrayList items = new ArrayList(5);
	/**
	 * Create a SmartLabel with the given borderStyle as a child of parent.
	 */
	public ActivatorBar(Composite parent) {
		super(parent, SWT.NONE);
		addPaintListener(this);
		setLayout(new ActivatorBarLayout());
	}
/**
 * Add an item to the bar.
 */
protected void addItem(ActivatorItem item) {
	items.add(item);
	layout();
}
/**
 * Add a selection listener.
 */
public void addItemListener(ItemListener listener) {
	this.listener = listener;
}
/**
 * Gets the item count.
 */
public ActivatorItem getItem(int nX) {
	return (ActivatorItem)items.get(nX);
}
/**
 * Gets the item count.
 */
public int getItemCount() {
	return items.size();
}
/**
 * Gets the active tab.
 */
public ActivatorItem getSelection() {
	return activeItem;
}
/**
 * Gets the active tab.
 */
public int getSelectionIndex() {
	if (activeItem == null)
		return -1;
	else
		return items.indexOf(activeItem);
}
/**
 * A close button has been pressed.  Notify any listeners.
 */
protected void itemClosePressed(ActivatorItem item) {
	if (listener != null)
		listener.itemClosePressed(item);
}
/**
 * A button has been pressed.  Notify any listeners.
 */
protected void itemSelected(ActivatorItem item) {
	if (listener != null)
		listener.itemSelected(item);
}
	//---- painting
	
	/**
 	 * Implements PaintListener.
 	 * @private
 	 */
	public void paintControl(PaintEvent event) {
		Display disp = getDisplay();
		GC gc= event.gc;
		Rectangle r= getClientArea();
		paintGradient(disp, gc, r);
	}
	//---- painting
	static protected void paintGradient(Display disp, GC gc, Rectangle r) 
	{
		Color c1 = disp.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
		Color c2 = disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);

		int y = 0;
		int STEPS = 20;
		int dY = r.height / STEPS + 1;

		// Loop through bands.
		for (int nStep = 0; nStep < STEPS; nStep ++) {
			// Calculate band color.
			int red = (c1.getRed() * (STEPS - nStep) + c2.getRed() * nStep) / STEPS;
			int green = (c1.getGreen() * (STEPS - nStep) + c2.getGreen() * nStep) / STEPS;
			int blue = (c1.getBlue() * (STEPS - nStep) + c2.getBlue() * nStep) / STEPS;

			// Draw the band.
			Color bandColor = new Color(disp, red, green, blue);
			Color oldColor = gc.getBackground();
			gc.setBackground(bandColor);
			gc.fillRectangle(0, y, r.width, dY);
			gc.setBackground(oldColor);
			bandColor.dispose();

			// Next step.
			y += dY;
		}
	}
/**
 * Remove all items from the bar.
 */
public void removeAllItems() {
	List safeCopy = (List)items.clone();
	Iterator enum = safeCopy.iterator();
	while (enum.hasNext()) {
		ActivatorItem item = (ActivatorItem)enum.next();
		item.dispose(); // This affects the item list.
	}
	items.clear();
	layout();
}
/**
 * Remove an item from o the bar.
 */
protected void removeItem(ActivatorItem item) {
	int nIndex = items.indexOf(item);
	if (nIndex >= 0) {
		if (activeItem == item)
			activeItem = null;
		items.remove(nIndex);
	}
	layout();
}
/**
 * Remove a selection listener.
 */
public void removeSelectionListener (ItemListener listener) {
	this.listener = null;
}
/**
 * Sets the active tab.
 */
public void setSelection(ActivatorItem item) {
	if (activeItem == item)
		return;
	if (activeItem != null)
		activeItem.setPressed(false);
	activeItem = item;
	if (activeItem != null)
		activeItem.setPressed(true);
}
/**
 * Sets the active tab.
 */
public void setSelectionIndex(int nX) {
	ActivatorItem item = (ActivatorItem)items.get(nX);
	setSelection(item);
}
}
