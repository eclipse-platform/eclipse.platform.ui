/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * Implementers of this interface will receive notifications when objects are dragged over
 * a particular SWT control. 
 */
public interface IDragOverListener {

    /**
     * Notifies the receiver that the given object has been dragged over
     * the given position. Returns a drop target if the object may be
     * dropped in this position. Returns null otherwise.
     * 
     * @param draggedObject object being dragged over this location
     * @param position location of the cursor
     * @param dragRectangle current drag rectangle (may be an empty rectangle if none)
     * @return a valid drop target or null if none
     */
    IDropTarget drag(Control currentControl, Object draggedObject,
            Point position, Rectangle dragRectangle);
}
