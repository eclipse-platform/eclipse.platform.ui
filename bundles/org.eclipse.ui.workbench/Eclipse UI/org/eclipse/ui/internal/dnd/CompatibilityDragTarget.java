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

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * Compatibility layer for the old-style drag-and-drop. Adapts an old-style
 * IPartDropListener into an IDragTarget.
 * 
 */
public class CompatibilityDragTarget {

    // Define width of part's "hot" border	
    private final static int MARGIN = 30;

    /**
     * Returns the relative position of the given point (in display coordinates)
     * with respect to the given control. Returns one of SWT.LEFT, SWT.RIGHT, SWT.CENTER, SWT.TOP, 
     * or SWT.BOTTOM if the point is on the control or SWT.DEFAULT if the point is not on the control. 
     * 
     * @param control control to perform hit detection on
     * @param toTest point to test, in display coordinates
     * @return
     */
    public static int getRelativePosition(Control c, Point toTest) {
        Point p = c.toControl(toTest);
        Point e = c.getSize();

        if (p.x > e.x || p.y > e.y || p.x < 0 || p.y < 0) {
            return SWT.DEFAULT;
        }

        // first determine whether mouse position is in center of part
        int hmargin = Math.min(e.x / 3, MARGIN);
        int vmargin = Math.min(e.y / 3, MARGIN);

        Rectangle inner = new Rectangle(hmargin, vmargin, e.x - (hmargin * 2),
                e.y - (vmargin * 2));
        if (inner.contains(p)) {
            return SWT.CENTER;
        } else {
            return Geometry.getClosestSide(inner, p);
        }
    }

}
