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
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * This convenience class provides a "close" system menu item that closes
 * the currently selected pane in a presentation. Presentations can use
 * this to add a close item to their system menu.
 * 
 * @since 3.0
 */
public final class SystemMenuClose extends Action implements ISelfUpdatingAction {

    private IStackPresentationSite site;
    private IPresentablePart part;

    public SystemMenuClose(IStackPresentationSite site) {
        this.site = site;
        setText(WorkbenchMessages.PartPane_close);
    }

    public void dispose() {
        site = null;
    }

    public void run() {
        if (part != null) {
            site.close(new IPresentablePart[] { part });
        }
    }

    public void setTarget(IPresentablePart presentablePart) {
        this.part = presentablePart;
        setEnabled(presentablePart != null && site.isCloseable(presentablePart));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.ISelfUpdatingAction#update()
     */
    public void update() {
        setTarget(site.getSelectedPart());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.presentations.ISelfUpdatingAction#shouldBeVisible()
     */
    public boolean shouldBeVisible() {
        return true;
    }
}
