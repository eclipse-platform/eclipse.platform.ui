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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tracker;

import org.eclipse.ui.internal.DragCursors;
import org.eclipse.ui.internal.Geometry;

/**
 * Provides the methods for attaching drag-and-drop listeners to SWT controls. 
 */
public class DragUtil {
	private static final String DROP_TARGET_ID = "org.eclipse.ui.internal.dnd.dropTarget"; //$NON-NLS-1$
	
	/**
	 * Singleton drag listener
	 */
	private static DragListener listener = new DragListener();
	
	/**
	 * List of IDragOverListener
	 */
	private static List defaultTargets = new ArrayList();
	
	/**
	 * Sets the drop target for the given control. It is possible to add one or more 
	 * targets for a "null" control. This becomes a default target that is used if no
	 * other targets are found (for example, when dragging objects off the application
	 * window). 
	 * 
	 * @param control the control that should be treated as a drag target, or null
	 * to indicate the default target
	 * @param target the drag target to handle the given control
	 */
	public static void addDragTarget(Control control, IDragOverListener target) {
		if (control == null) {
			defaultTargets.add(target);
		} else {
			control.setData(DROP_TARGET_ID, target);
		}
	}
	
	/**
	 * Removes a drop target from the given control.
	 * 
	 * @param control
	 * @param target
	 */
	public static void removeDragTarget(Control control, IDragOverListener target) {
		if (control == null) {
			defaultTargets.remove(target);
		} else {
			control.setData(DROP_TARGET_ID, null);
		}
	}
	
	/**
	 * Shorthand method. Returns the bounding rectangle for the given control, in
	 * display coordinates. 
	 * 
	 * @param draggedItem
	 * @param boundsControl
	 * @return
	 */
	public static Rectangle getDisplayBounds(Control boundsControl) {
		Control parent = boundsControl.getParent();
		if (parent == null) {
			return boundsControl.getBounds();
		}
		
		return Geometry.toDisplay(parent, boundsControl.getBounds());
	}
	
    public static boolean performDrag(final Object draggedItem, Rectangle sourceBounds) {
		IDropTarget target = dragToTarget(draggedItem, sourceBounds);
		
		if (target == null) {
			return false;
		}
		
		target.drop();
		
		return true;
	}
	
	/**
	 * Drags the given item, given an initial bounding rectangle in display coordinates.
	 * 
	 * @param draggedItem
	 * @param sourceBounds
	 * @return
	 */
    /* package */ static IDropTarget dragToTarget(final Object draggedItem, Rectangle sourceBounds) {
		final Display display = Display.getDefault();
		// Create a tracker.  This is just an XOR rect on the screen.
		// As it moves we notify the drag listeners.
		final Tracker tracker = new Tracker(display, SWT.NULL);
		tracker.addListener(SWT.Move, new Listener() {
			public void handleEvent(Event event) {
				Point location = display.getCursorLocation();
				Control targetControl = display.getCursorControl();
				
				IDropTarget target = getDropTarget(targetControl, draggedItem, location, tracker.getRectangles()[0]); 

				if (target != null) {
					tracker.setCursor(target.getCursor());
				} else {
					tracker.setCursor(DragCursors.getInvalidCursor());
				}				
			}
		});
		
		if (sourceBounds != null) {
			tracker.setRectangles(new Rectangle[] { sourceBounds });
		}

		// Run tracker until mouse up occurs or escape key pressed.
		boolean trackingOk = tracker.open();

		Point location = display.getCursorLocation();
		
		IDropTarget dropTarget = null;
		if (trackingOk) {
			Control targetControl = display.getCursorControl();
			
			dropTarget = getDropTarget(targetControl, draggedItem, location, tracker.getRectangles()[0]);			
		}
				
		// Cleanup.
		tracker.dispose();
		
		return dropTarget;
	}
	
	/**
	 * Flags the given control as draggable
	 * 
	 * @param control
	 */
	public static void addDragSource(Control control, IDragSource source) {
		listener.attach(control, source);
	}
	
	public static void removeDragSource(Control control) {
		listener.detach(control);
	} 
	
	/**
	 * Returns the drag target for the given control or null if none. 
	 * 
	 * @param toSearch
	 * @param e
	 * @return
	 */
	public static IDropTarget getDropTarget(Control toSearch, Object draggedObject, Point position, Rectangle dragRectangle) {		
		for (Control current = toSearch; current != null; current = current.getParent()) {
			IDragOverListener target = (IDragOverListener)current.getData(DROP_TARGET_ID);
						
			if (target == null) {
				continue;
			}
			
			IDropTarget dropTarget = target.drag(toSearch, draggedObject, position, dragRectangle);
			
			if (dropTarget != null) {
				return dropTarget;
			}			
		}
		
		// No controls could handle this event -- check for default targets
		Iterator iter = defaultTargets.iterator();
		while (iter.hasNext()) {
			IDragOverListener next = (IDragOverListener)iter.next();

			IDropTarget dropTarget = next.drag(toSearch, draggedObject, position, dragRectangle);
			
			if (dropTarget != null) {
				return dropTarget;
			}			
		}
		
		// No default targets found either.
		
		return null;
	}
	
}
