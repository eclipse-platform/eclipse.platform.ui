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
package org.eclipse.ui.internal.presentations;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IPresentablePart;

public class SystemMenuCloseOthers extends Action implements
        ISelfUpdatingAction {

    private DefaultPartPresentation stackPresentation;

    public SystemMenuCloseOthers(DefaultPartPresentation stackPresentation) {
        this.stackPresentation = stackPresentation;
        setText(WorkbenchMessages.getString("PartPane.closeOthers")); //$NON-NLS-1$
    }

    public void dispose() {
        stackPresentation = null;
    }

    public void run() {
        IPresentablePart current = stackPresentation.getCurrent();
        List others = stackPresentation.getPresentableParts();
        others.remove(current);
        stackPresentation.close((IPresentablePart[]) others
                .toArray(new IPresentablePart[others.size()]));
    }

    public void update() {
        IPresentablePart current = stackPresentation.getCurrent();

        setEnabled(current != null);
    }

    public boolean shouldBeVisible() {
        return true;
    }
}