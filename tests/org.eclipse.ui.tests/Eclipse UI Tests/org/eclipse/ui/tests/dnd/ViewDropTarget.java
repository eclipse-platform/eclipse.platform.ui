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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IViewDescriptor;

/**
 * @since 3.0
 */
public class ViewDropTarget extends AbstractTestDropTarget {

    String targetPart;

    int side;

    public ViewDropTarget(String part, int side) {
        targetPart = part;
        this.side = side;
    }

    IViewPart getPart() {
        return getPage().findView(targetPart);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDropTarget#getName()
     */
    public String toString() {
        IViewDescriptor desc = WorkbenchPlugin.getDefault().getViewRegistry()
                .find(targetPart);
        String title = desc.getLabel();

        return DragOperations.nameForConstant(side) + " of " + title;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDropTarget#getLocation()
     */
    public Point getLocation() {
        return DragOperations.getLocation(DragOperations.getPane(getPart()),
                side);
    }
}