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
package org.eclipse.ui.internal.presentations.newapi;

import org.eclipse.swt.graphics.Point;

/**
 * @since 3.0
 */
public abstract class AbstractTabFolderListener {

    /**
     * Called when the minimize, maximize, or restore buttons are pressed.
     *   
     * @param buttonId one of the IStackPresentationSite.STATE_* constants
     */
    public void stateButtonPressed(int buttonId) {
    }

    /**
     * Called when a close button is pressed.
     *   
     * @param item the tab whose close button was pressed
     */
    public void closeButtonPressed(AbstractTabItem item) {
    }

    /**
     * Called to show the part list
     * 
     * @since 3.0
     */
    public void showList() {
    }

    /**
     * Called to show the pane menu at the given location (display coordinates)
     */
    public void showPaneMenu(Point location) {
    }

    /**
     * Called to indicate the start of a drag
     * 
     * @param beingDragged tab being dragged (or null if none)
     * @param initialLocation initial mouse location (display coordinates)
     */
    public void dragStart(AbstractTabItem beingDragged, Point initialLocation) {
    }

}