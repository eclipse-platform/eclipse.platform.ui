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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A drag source provides objects that can be dragged. An object that implements
 * this interface can be attached to an SWT control using the method 
 * DragUtil.addDragSource.
 * 
 * <p>Clients should not implement this interface directly. They should subclass
 * DragSource instead.</p>
 */
public interface IDragSource {
	/**
	 * Returns the item that will be dragged from the given position
	 * in the drag source, or null if the given position is not
	 * draggable. 
	 * 
	 * @param position the cursor position, in display coordinates
	 * @return the item that will be dragged
	 */
	Object getDraggedItem(Point position);
	
	/**
	 * Notifies the receiver that the user has started dragging the given
	 * object.
	 * 
	 * @param draggedItem, must not be <code>null</code>.
	 */
	void dragStarted(Object draggedItem);
	
	/**
	 * This method is called to notify a drag source that an item
	 * has been dragged from it. Note that the source is always notified
	 * about the drag before the destination.
	 *  
	 * @param draggedItem
	 * @param success true iff the item was dropped on a valid drag target
	 * @param target the drag target where the item was dropped
	 */
	void dragFinished(Object draggedItem, boolean success);
	
	/**
	 * Returns a rectangle that can be used to create a tracker for dragging the
	 * given item. 
	 *  
	 * @param draggedItem
	 * @return a rectangle that can be used for tracking the given item (display coordinates)
	 */
	Rectangle getDragRectangle(Object draggedItem);
}
