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
import org.eclipse.ui.IEditorPart;

/**
 * @since 3.0
 */
public class EditorDropTarget extends AbstractTestDropTarget {

    int editorIdx;

    int side;

    public EditorDropTarget(int editorIdx, int side) {
        this.editorIdx = editorIdx;
        this.side = side;
    }

    IEditorPart getPart() {
        return getPage().getEditors()[editorIdx];
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDropTarget#getName()
     */
    public String toString() {
        return DragOperations.nameForConstant(side) + " of editor " + editorIdx;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDropTarget#getLocation()
     */
    public Point getLocation() {
        return DragOperations.getLocation(DragOperations.getPane(getPart()),
                side);
    }
}