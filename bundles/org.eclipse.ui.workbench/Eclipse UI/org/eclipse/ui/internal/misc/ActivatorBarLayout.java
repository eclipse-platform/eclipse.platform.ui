package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.SWT;

public class ActivatorBarLayout extends Layout {
	private int preferredWidth;
	private int lastClientWidth;
public ActivatorBarLayout() {
}
/**
 * Computes the size of the client area
 **/
protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
	Control [] children = composite.getChildren ();
	int count = children.length;
	if (count == 0)
		return new Point(5, 22);
	int width = 0, height = 22;
	for (int i=0; i<count; i++) {
		Control child = children [i];
		Point pt = child.computeSize (SWT.DEFAULT, SWT.DEFAULT, flushCache);
		width += pt.x;
		height = Math.max(height, pt.y);
	}
	preferredWidth = width;
	return new Point(width, height);
}
/**
 * Lays out children of a composite in a horizontal fashion.  
 * If the preferred size of these children extend beyond the 
 * horizontal space available they are scaled accordingly.
 */
protected void layout (Composite composite, boolean flushCache) 
{
	// Get the children.
	Control [] children = composite.getChildren ();
	int count = children.length;
	if (count == 0) return;

	// Get the basic proportions for layout.
	Rectangle client = composite.getClientArea ();
	int x = client.x;
	int y = client.y;
	int height = client.height;

	// Scale everything if the client area is too small.
	float scale = 1.0f;
	if (flushCache || (client.width != lastClientWidth)) {
		preferredWidth = computeSize(composite, 0, 0, true).x;
		lastClientWidth = client.width;
	}
	// System.out.println("Pref " + preferredWidth);
	// System.out.println("Avail " + client.width);
	if (preferredWidth > client.width)
		scale = (float)client.width / (float)preferredWidth;
		
	// Layout children.
	for (int i=0; i<count; i++) {
		Control child = children [i];
		Point pt = child.computeSize (SWT.DEFAULT, SWT.DEFAULT, false);
		int width = (int)((float)pt.x * scale);
		if ((scale < 1.0f) && (i == (count - 1)))
			width = client.width - x;
		child.setBounds (x, y, width, height);
		x += width;
	}
}
}
