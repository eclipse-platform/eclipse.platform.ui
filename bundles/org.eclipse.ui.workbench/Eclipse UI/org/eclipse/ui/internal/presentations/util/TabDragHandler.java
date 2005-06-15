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
package org.eclipse.ui.internal.presentations.util;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.presentations.StackDropResult;

/**
 * @since 3.0
 */
public abstract class TabDragHandler {

    /**
     * Returns the StackDropResult for the location being dragged over.
     * 
     * @param currentControl control being dragged over
     * @param location mouse position (display coordinates)
     * @param initialTab the index of the tab in this stack being dragged, 
     * 			or -1 if dragging a tab from another stack. 
     * @return the StackDropResult for this drag location
     */
    public abstract StackDropResult dragOver(Control currentControl,
            Point location, int initialTab);

    public abstract int getInsertionPosition(Object cookie);
}
