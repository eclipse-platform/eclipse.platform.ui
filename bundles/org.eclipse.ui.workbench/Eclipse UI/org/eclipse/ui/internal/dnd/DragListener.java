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

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

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
	private Control dragControl = null;
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
			dragControl = (Control)event.widget;
			anchor = getEventLoc(event);
			dragSource = (IDragSource)dragControl.getData(SOURCE_ID);
			
			draggedItem = null;
			if (dragSource != null) {
				draggedItem = dragSource.getDraggedItem(anchor);				
			}			
		}
	};

	/**
	 * Returns the location of the given event, in display coordinates
	 * @return
	 */
	private static Point getEventLoc(Event event) {
		Control ctrl = (Control)event.widget;
		return ctrl.toDisplay(new Point(event.x, event.y));		
	}
	
	/**
	 * Returns whether the mouse has moved enough to warrant
	 * opening a tracker.
	 */
	private boolean hasMovedEnough(Event event) {		
		return Geometry.distanceSquared(getEventLoc(event), anchor) 
			>= HYSTERESIS * HYSTERESIS; 		
	}
	
	// Public interface /////////////////////////////////////
	
	public DragListener() {
		// Insert initialization code here
	}
	
	public void attach(Control control, IDragSource source) {
		control.addListener(SWT.DragDetect, dragListener);
		control.addListener(SWT.MouseUp, clickListener);
		control.addListener(SWT.MouseDoubleClick, clickListener);
		control.addListener(SWT.MouseDown, mouseDownListener);
		control.addListener(SWT.MouseMove, moveListener);
		control.setData(SOURCE_ID, source);
	}
	
	public void detach(Control control) {
		control.removeListener(SWT.DragDetect, dragListener);
		control.removeListener(SWT.MouseUp, clickListener);
		control.removeListener(SWT.MouseDoubleClick, clickListener);
		control.removeListener(SWT.MouseDown, mouseDownListener);
		control.removeListener(SWT.MouseMove, moveListener);
		control.setData(SOURCE_ID, null);
	}
	
	// Protected interface /////////////////////////////////////
	protected void handleMouseClick(Event event) {
		isDragAllowed = false;
	}
	
	protected void handleMouseMove(Event e) {
		if (isDragAllowed && hasMovedEnough(e)) {
			openTracker(e);	
		}
	}	
		
	/**
	 * Open a tracker (a XOR rect on the screen) change
	 * the cursor indicanting where the part will be dropped 
	 * and notify the drag listeners.
	 */
	private void openTracker(Event e) {

		dragSource.dragStarted(draggedItem);
		
		// Create a drag rect.
		Rectangle sourceBounds = dragSource.getDragRectangle(draggedItem);

		isDragAllowed = false;
		
		Point location = getEventLoc(e);
		
		sourceBounds.x += location.x - anchor.x;
		sourceBounds.y += location.y - anchor.y;
		
		if (dragControl instanceof Control) {
			((Control)dragControl).setCapture(true);
		}
		
		IDropTarget target = DragUtil.dragToTarget(draggedItem, sourceBounds, 
				location, true);

		if (dragControl instanceof Control) {
			((Control)dragControl).setCapture(false);
		}
		
		dragSource.dragFinished(draggedItem, target != null);
		
		if (target != null) {
			target.drop();
		}
	}
	
}
