/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.widgets.Control;

/**
 * Interface for trim controls that can be docked to the edge of a Workbench window using
 * drag-and-drop.
 */
public interface IWindowTrim {
    /**
     * Returns the control representing this trim widget, or null if it has not yet
     * been created.
     * 
     * @return the control for the trim widget.
     */
    Control getControl();

    /**
     * Returns the set of sides that this trim can be docked onto.
     * 
     * @return bitwise or of one or more of SWT.TOP, SWT.BOTTOM, SWT.LEFT, and SWT.RIGHT
     */
    int getValidSides();

    /**
     * Called to notify the trim object that it has been docked on the given side of the layout
     * 
     * @param dropSide
     */
    void dock(int dropSide);
    
    /**
     * Each piece of window trim must have a unique ID to participate
     * fully as trim.
     * 
     * @return The unique id
     * @since 3.2
     */
    public String getId();
}
