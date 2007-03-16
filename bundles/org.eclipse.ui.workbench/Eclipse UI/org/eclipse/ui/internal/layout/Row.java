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

package org.eclipse.ui.internal.layout;

/**
 * Describes a single row (or column) in a CellLayout
 * 
 * @since 3.0
 */
public class Row {
    /**
     * True iff this row will expand to fill available space
     */
    boolean grows = false;

    /**
     * Size of this row. For growing rows, this is the relative size
     * of the row with respect to other rows (ie: a row of size 100 is twice
     * as wide as a row of size 50). For fixed rows, this is the absolute size
     * of the row, in pixels. 
     */
    int size = 0;

    /**
     * True iff this row should query its child controls for its size.
     */
    boolean largerThanChildren = true;

    /**
     * Creates a fixed-size row with the given width (pixels).
     * The preferred sizes of child controls are ignored.
     * 
     * @param size
     */
    public Row(int size) {
        largerThanChildren = false;
        this.size = size;
        grows = false;
    }

    /**
     * Creates a row that automatically computes its size based on the preferred
     * sizes of its children.
     * 
     * @param growing
     */
    public Row(boolean growing) {
        this.grows = growing;

        if (growing) {
            size = 100;
        }
    }

    /**
     * Creates a growing row.
     * 
     * @param sizeRatio determines the size of this row with respect to other growing rows
     * (for example, a row with size = 3 will be 3x as large as a row with size = 1)
     * @param largerThanChildren true iff the preferred size of this row should take into
     * account the preferred sizes of its children.
     */
    public Row(int size, boolean largerThanChildren) {
        this.grows = true;
        this.size = size;
        this.largerThanChildren = largerThanChildren;
    }

    /**
     * Construct and return a typical growing row.
     * 
     * @return a growing row
     */
    public static Row growing() {
        return new Row(100, true);
    }

    /**
     * Construct and return a growing row with custom properties
     * 
     * @param size relative size of this row with respect to other growing rows
     * @param largerThanChildren true iff the preferred size of this row should
     * be based on the preferred sizes of its children
     * @return a new Row
     */
    public static Row growing(int size, boolean largerThanChildren) {
        return new Row(size, largerThanChildren);
    }

    /**
     * Construct and return a fixed-size row. The row will not grow when the layout
     * is resized, and its size will be computed from the default sizes of its children.
     * 
     * @return a new Row
     */
    public static Row fixed() {
        return new Row(false);
    }

    /**
     * Construct and return a fixed-size row. The row will always have the given
     * width, regardless of the size of the layout or the preferred sizes of its children. 
     * 
     * @param pixels size of the row
     * @return a fixed-size row with the given width (in pixels)
     */
    public static Row fixed(int pixels) {
        return new Row(pixels);
    }
}
