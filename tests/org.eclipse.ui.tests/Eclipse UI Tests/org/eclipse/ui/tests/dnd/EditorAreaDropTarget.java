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
package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;

/**
 * @since 3.0
 */
public class EditorAreaDropTarget extends AbstractTestDropTarget {
    int side;

    /**
     * @param window
     * @param side
     */
    public EditorAreaDropTarget(int side) {
        super();
        this.side = side;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDropTarget#getName()
     */
    public String toString() {
        return DragOperations.nameForConstant(side) + " of editor area";
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDropTarget#getLocation()
     */
    public Point getLocation() {
        return DragOperations.getPointInEditorArea(getPage(), side);
    }

}