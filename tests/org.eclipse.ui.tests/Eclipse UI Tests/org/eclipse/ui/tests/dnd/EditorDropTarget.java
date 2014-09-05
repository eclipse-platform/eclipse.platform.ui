/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

/**
 * @since 3.0
 */
public class EditorDropTarget extends WorkbenchWindowDropTarget {

    int editorIdx;

    int side;

    public EditorDropTarget(IWorkbenchWindowProvider provider, int editorIdx, int side) {
        super(provider);
        this.editorIdx = editorIdx;
        this.side = side;
    }

    IEditorPart getPart() {
        return getPage().getEditors()[editorIdx];
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDropTarget#getName()
     */
    @Override
	public String toString() {
        return DragOperations.nameForConstant(side) + " of editor " + editorIdx;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDropTarget#getLocation()
     */
    @Override
	public Point getLocation() {
        return DragOperations.getLocation(DragOperations.getPane(getPart()),
                side);
    }
    
    @Override
	public Shell getShell() {
    	return getPart().getSite().getShell();
    }
}
