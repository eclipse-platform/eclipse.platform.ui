/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.jface.util.Assert;

/**
 * Describes the width of a table column in pixels, and
 * whether the column is resizable.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class ColumnPixelData extends ColumnLayoutData {

    /**
     * The column's width in pixels.
     */
    public int width;

    /**
     * Creates a resizable column width of the given number of pixels.
     *
     * @param widthInPixels the width of column in pixels
     */
    public ColumnPixelData(int widthInPixels) {
        this(widthInPixels, true);
    }

    /**
     * Creates a column width of the given number of pixels.
     *
     * @param widthInPixels the width of column in pixels
     * @param resizable <code>true</code> if the column is resizable,
     *   and <code>false</code> if size of the column is fixed
     */
    public ColumnPixelData(int widthInPixels, boolean resizable) {
        super(resizable);
        Assert.isTrue(widthInPixels >= 0);
        this.width = widthInPixels;
    }
}