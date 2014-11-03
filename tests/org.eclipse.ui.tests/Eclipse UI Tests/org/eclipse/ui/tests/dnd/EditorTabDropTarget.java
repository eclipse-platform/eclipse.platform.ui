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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

/**
 * @since 3.0
 */
public class EditorTabDropTarget extends WorkbenchWindowDropTarget {

    int editorIdx;

    public EditorTabDropTarget(IWorkbenchWindowProvider provider, int editorIdx) {
        super(provider);
        this.editorIdx = editorIdx;
    }

    IEditorPart getPart() {
        return getPage().getEditors()[editorIdx];
    }

    @Override
	public String toString() {
        return "editor " + editorIdx + " tab area";
    }

    @Override
	public Shell getShell() {
    	return getPart().getSite().getShell();
    }

    @Override
	public Point getLocation() {
        Rectangle bounds = DragOperations.getDisplayBounds(DragOperations
                .getPane(getPart()));

        return new Point(bounds.x + 8, bounds.y + 8);
    }
}
