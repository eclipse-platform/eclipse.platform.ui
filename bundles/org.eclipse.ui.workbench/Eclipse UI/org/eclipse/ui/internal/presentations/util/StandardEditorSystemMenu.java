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
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.presentations.SystemMenuCloseAll;
import org.eclipse.ui.internal.presentations.SystemMenuCloseOthers;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * Implements the standard system menu used by the default presentation.
 * Not intended to be subclassed by clients
 * 
 * @since 3.1
 */
public class StandardEditorSystemMenu extends StandardViewSystemMenu {

    private SystemMenuCloseOthers closeOthers;
    private SystemMenuCloseAll closeAll;
    
    /**
     * @param site
     */
    public StandardEditorSystemMenu(IStackPresentationSite site) {
        super(site);
        
        closeOthers = new SystemMenuCloseOthers(site);
        closeAll = new SystemMenuCloseAll(site);
        menuManager.add(closeOthers);
        menuManager.add(closeAll);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.newapi.ISystemMenu#show(org.eclipse.swt.widgets.Control, org.eclipse.swt.graphics.Point, org.eclipse.ui.presentations.IPresentablePart)
     */
    public void show(Control parent, Point displayCoordinates,
            IPresentablePart currentSelection) {
        closeOthers.setTarget(currentSelection);
        closeAll.update();
        super.show(parent, displayCoordinates, currentSelection);
    }
}
