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

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dnd.DragUtil;

/**
 * @since 3.0
 */
public class FastViewBarDropTarget extends AbstractTestDropTarget {

    /**
     * @param window
     */
    public FastViewBarDropTarget() {
        super();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "fast view bar";
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDropTarget#getLocation()
     */
    public Point getLocation() {
        WorkbenchWindow window = (WorkbenchWindow) getPage()
                .getWorkbenchWindow();

        Control control = window.getFastViewBar().getControl();
        Rectangle region = DragUtil.getDisplayBounds(control);
        Point result = Geometry.centerPoint(region);

        return result;
    }

}