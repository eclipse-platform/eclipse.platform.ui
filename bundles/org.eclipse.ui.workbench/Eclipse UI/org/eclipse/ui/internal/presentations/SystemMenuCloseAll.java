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

public class SystemMenuCloseAll extends Action implements ISelfUpdatingAction {

    private IStackPresentationSite presentation;

    public SystemMenuCloseAll(IStackPresentationSite presentation) {
        this.presentation = presentation;
        setText(WorkbenchMessages.PartPane_closeAll);
    }

    public void dispose() {
        presentation = null;
    }

    public void run() {
        presentation.close(presentation.getPartList());
    }

    public void update() {
        IPresentablePart[] parts = presentation.getPartList();
        setEnabled(parts.length != 0);
    }

    public boolean shouldBeVisible() {
        return true;
    }

}
