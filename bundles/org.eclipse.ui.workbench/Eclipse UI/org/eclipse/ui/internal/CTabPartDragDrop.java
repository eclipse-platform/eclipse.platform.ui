package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;

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
/** * Verifies that the tab under the mouse pointer is the same 
 * as for this drag operation
 * 
 * @see org.eclipse.ui.internal.PartDragDrop#isDragAllowed(Point) */
protected void isDragAllowed(Point position) {
	CTabFolder tabFolder = getCTabFolder();
	CTabItem tabUnderPointer = tabFolder.getItem(position);
	if (tabUnderPointer != tab)
		return;
	if(tabUnderPointer == null) {
		//Avoid drag from the borders.
		Rectangle clientArea = tabFolder.getClientArea();
		if((tabFolder.getStyle() & SWT.TOP) != 0) {
			if(position.y > clientArea.y)
				return;
		} else {
			if(position.y < clientArea.y + clientArea.height)
				return;
		}
	}

	super.isDragAllowed(position);
}
public void setTab(CTabItem newTab) {
	tab = newTab;
}
}
