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
package org.eclipse.ui.presentations;

import org.eclipse.swt.graphics.Rectangle;

/**
 * This structure describes a drop event that will cause a dragged part
 * to be stacked in a position currently occupied by another part. 
 * 
 * @since 3.0
 */
public final class StackDropResult {

    private Rectangle snapRectangle;

    private Object cookie;

    /**
     * Creates a drop result
     * 
     * @param snapRectangle region that should be highlighted by the tracking
     * rectangle (display coordinates) 
     * @param cookie the presentation may attach an object to this drop result 
     * in order to identify the drop location. This object will be passed back into the
     * presentation's add method.
     */
    public StackDropResult(Rectangle snapRectangle, Object cookie) {
        this.snapRectangle = snapRectangle;
        this.cookie = cookie;
    }

    /**
     * Returns a rectangle (screen coordinates) describing the target location
     * for this drop operation. While dragging, the tracking rectangle will
     * snap to this position.
     * 
     * @return a snap rectangle (not null)
     */
    public Rectangle getSnapRectangle() {
        return snapRectangle;
    }

    /**
     * Returns the cookie for this drop result. This object provided by the presentation,
     * but is remembered by the workbench. It will be given back to the presentation's add
     * method to indicate that a part is being added as a result of a drop operation.
     * 
     * @return the drop cookie for this drop result
     */
    public Object getCookie() {
        return cookie;
    }

}
