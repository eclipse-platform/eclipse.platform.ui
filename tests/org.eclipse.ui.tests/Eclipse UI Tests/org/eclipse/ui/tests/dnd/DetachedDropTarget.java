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
import org.eclipse.swt.widgets.Shell;

public class DetachedDropTarget extends AbstractTestDropTarget {

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "out of the window";
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.dnd.TestDropLocation#getLocation()
     */
    public Point getLocation() {
        Shell shell = getPage().getWorkbenchWindow().getShell();
        Rectangle clientArea = shell.getClientArea();
        Rectangle bounds = (Geometry.toDisplay(shell, clientArea));
        Point centerPoint = Geometry.centerPoint(bounds);
        return new Point(bounds.x + bounds.width + 20, centerPoint.y);
    }

}
