package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Controls the drag and drop of the part
 * which is contained within the CTabFolder
 * tab.
 */
public class CTabPartDragDrop extends PartDragDrop {
	private CTabItem tab;
public CTabPartDragDrop(LayoutPart dragPart, CTabFolder tabFolder, CTabItem tabItem) {
	super(dragPart, tabFolder);
	this.tab = tabItem;
}
protected CTabFolder getCTabFolder() {
	return (CTabFolder) getDragControl();
}
/**
 * Returns the source's bounds
 */
protected Rectangle getSourceBounds() {
	return PartTabFolder.calculatePageBounds(getCTabFolder());
}
/**
 * @see MouseListener::mouseDown
 */
public void mouseDown(MouseEvent e) {
	if (e.button != 1) return;

	// Verify that the tab under the mouse pointer
	// is the same as for this drag operation
	CTabFolder tabFolder = getCTabFolder();
	CTabItem tabUnderPointer = tabFolder.getItem(new Point(e.x, e.y));
	if (tabUnderPointer != tab)
		return;
	if(tabUnderPointer == null) {
		//Avoid drag from the borders.
		Rectangle clientArea = tabFolder.getClientArea();
		if((tabFolder.getStyle() & SWT.TOP) != 0) {
			if(e.y > clientArea.y)
				return;
		} else {
			if(e.y < clientArea.y + clientArea.height)
				return;
		}
	}

	super.mouseDown(e);
}
public void setTab(CTabItem newTab) {
	tab = newTab;
}
}
