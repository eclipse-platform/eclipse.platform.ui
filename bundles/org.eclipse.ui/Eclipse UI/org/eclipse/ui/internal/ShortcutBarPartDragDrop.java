package org.eclipse.ui.internal;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
**********************************************************************/
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IViewPart;

public class ShortcutBarPartDragDrop extends PartDragDrop implements MouseListener, MouseMoveListener{
	// The item that is being dragged by this PartDragDrop.
	private ToolItem draggedItem;

	public ShortcutBarPartDragDrop(LayoutPart itemPart, Control shortcutBar) {
		super(itemPart, shortcutBar);
	}
	
	public void mouseDown(MouseEvent e) {
		if (e.button != 1) return;
		ToolBar toolBar = getToolBar();
		draggedItem = toolBar.getItem(new Point(e.x, e.y));
		if (draggedItem == null) {
			// Avoid drag from the borders.
			return;
		}
		if (draggedItem.getData(ShowFastViewContribution.FAST_VIEW) == null) {
			// Avoid drag from non-fast view icons.
			return;
		}
		super.mouseDown(e);
	}
	
	// Returns the shortcut bar.
	private ToolBar getToolBar() {
		return (ToolBar) getDragControl();	
	}
	/*package*/ ToolItem getDraggedItem() {
		return draggedItem;
	}
	
	/*
	 * Returns the bounds of the fast view of the view
	 * represented by the icon being dragged.
	 */
	protected Rectangle getSourceBounds() {
		IViewPart part = (IViewPart) draggedItem.getData(ShowFastViewContribution.FAST_VIEW);
		WorkbenchPage page = (WorkbenchPage) part.getSite().getPage();
		Perspective persp = page.getActivePerspective();
		Rectangle rect =  persp.getFastViewBounds(part);
		return rect;
	}
	/*
	 * The cursor has moved enough to open a tracker only if it is
	 * NOT still over the icon being dragged. A tracker consumes
	 * events from the queue. If a tracker is opened while we are still
	 * over the pushed ToolItem, it will never be "unpushed", since the
	 * event that unpushes it will be consumed by the tracker.
	 */
	protected boolean hasMovedEnough(MouseEvent e) {
		ToolItem currentItem = getToolBar().getItem(new Point(e.x, e.y));
		if (super.hasMovedEnough(e) && currentItem != draggedItem)
			return true;
		return false;
	}
}
