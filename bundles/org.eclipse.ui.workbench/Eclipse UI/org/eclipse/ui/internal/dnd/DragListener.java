/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dnd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

/**
 * Object that converts SWT events into drag events. To make an SWT
 * object draggable, call attach(...) and all the appropriate listeners will
 * be created. This is intended to be used as a singleton.  
 */
/*package */ class DragListener {
	// Move this many pixels before dragging starts	
	private final static int HYSTERESIS = 10;
	private static final String SOURCE_ID = "org.eclipse.ui.internal.dnd.dragSource"; //$NON-NLS-1$
	
	private Point anchor = new Point(0,0);
	private boolean isDragAllowed = false;
	private Widget dragControl = null;
	private IDragSource dragSource = null;
	private Object draggedItem = null;
	
	private Listener dragListener = new Listener() {
		public void handleEvent(Event event) {
			isDragAllowed = (draggedItem != null);
		}
	};
	
	private Listener moveListener = new Listener() {
		public void handleEvent(Event event) {
			handleMouseMove(event);
		}
	};
	
	private Listener clickListener = new Listener() {
		public void handleEvent(Event e) {
			handleMouseClick(e);
		}
	};

	private Listener mouseDownListener = new Listener() {
		public void handleEvent(Event event) {
			anchor = event.display.getCursorLocation();
			dragControl = event.widget;
			dragSource = (IDragSource)dragControl.getData(SOURCE_ID);
			
			draggedItem = null;
			if (dragSource != null) {
				draggedItem = dragSource.getDraggedItem(anchor);
			}			
		}
	};
	
	// Public interface /////////////////////////////////////
	
	public DragListener() {
		// Insert initialization code here
	}
	
	public void attach(Widget control, IDragSource source) {
		control.addListener(SWT.DragDetect, dragListener);
		control.addListener(SWT.MouseMove, moveListener);
		control.addListener(SWT.MouseUp, clickListener);
		control.addListener(SWT.MouseDoubleClick, clickListener);
		control.addListener(SWT.MouseDown, mouseDownListener);
		control.setData(SOURCE_ID, source);
	}
	
	public void detach(Widget control) {
		control.removeListener(SWT.DragDetect, dragListener);
		control.removeListener(SWT.MouseMove, moveListener);
		control.removeListener(SWT.MouseUp, clickListener);
		control.removeListener(SWT.MouseDoubleClick, clickListener);
		control.removeListener(SWT.MouseDown, mouseDownListener);
		control.setData(SOURCE_ID, null);
	}
	
	// Protected interface /////////////////////////////////////
	protected void handleMouseClick(Event event) {
		isDragAllowed = false;
	}
	
	protected void handleMouseMove(Event e) {
		if (isDragAllowed && hasMovedEnough(e)) {
			openTracker();	
		}
	}
	
	/**
	 * Returns whether the mouse has moved enough to warrant
	 * opening a tracker.
	 */
	protected boolean hasMovedEnough(Event e) {
		int dx = e.x - anchor.x;
		int dy = e.y - anchor.y;
		return (Math.abs(dx) >= HYSTERESIS || Math.abs(dy) >= HYSTERESIS);
	}
	
		
	/**
	 * Open a tracker (a XOR rect on the screen) change
	 * the cursor indicanting where the part will be dropped 
	 * and notify the drag listeners.
	 */
	private void openTracker() {

		dragSource.dragStarted(draggedItem);
		
		// Create a drag rect.
		Rectangle sourceBounds = dragSource.getDragRectangle(draggedItem);

		isDragAllowed = false;
		
		Point location = Display.getDefault().getCursorLocation();
		
		sourceBounds.x += location.x - anchor.x;
		sourceBounds.y += location.y - anchor.y;
		
		if (dragControl instanceof Control) {
			((Control)dragControl).setCapture(true);
		}
		
		IDropTarget target = DragUtil.dragToTarget(draggedItem, sourceBounds);

		if (dragControl instanceof Control) {
			((Control)dragControl).setCapture(false);
		}
		
		dragSource.dragFinished(draggedItem, target != null);
		
		if (target != null) {
			target.drop();
		}
	}
	
}
