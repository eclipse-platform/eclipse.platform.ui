/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dnd;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;

/**
 * This interface is used to drop objects. It knows how to drop a particular object
 * in a particular location. IDropTargets are typically created by IDragOverListeners, and
 * it is the job of the IDragOverListener to supply the drop target with information about
 * the object currently being dragged.
 *
 * @see org.eclipse.ui.internal.dnd.IDragOverListener
 */
public interface IDropTarget {

    /**
     * Drops the object in this position
     */
    void drop();

    /**
     * Returns a cursor describing this drop operation
     *
     * @return a cursor describing this drop operation
     */
    Cursor getCursor();

    /**
     * Returns a rectangle (screen coordinates) describing the target location
     * for this drop operation.
     *
     * @return a snap rectangle or null if this drop target does not have a specific snap
     * location.
     */
    Rectangle getSnapRectangle();
}
