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
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;

/**
 * This listener receives notifications when the user clicks on one of
 * the buttons (minimize, maximize, or restore) on a pane folder.  
 * 
 * @since 3.0
 */
public abstract class PaneFolderButtonListener {

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
    public void closeButtonPressed(CTabItem item) {
    }

    /**
     * 
     * @since 3.0
     */
    public void showList(CTabFolderEvent event) {
    }
}
