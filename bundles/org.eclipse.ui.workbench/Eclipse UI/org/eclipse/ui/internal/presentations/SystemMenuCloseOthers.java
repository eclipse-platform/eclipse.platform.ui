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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuCloseOthers extends Action implements
        ISelfUpdatingAction {

    private IStackPresentationSite stackPresentation;
    private IPresentablePart current;

    public SystemMenuCloseOthers(IStackPresentationSite stackPresentation) {
        this.stackPresentation = stackPresentation;
        setText(WorkbenchMessages.PartPane_closeOthers); 
    }

    public void dispose() {
        stackPresentation = null;
    }

    public void run() {
        List others = new LinkedList();
        others.addAll(Arrays.asList(stackPresentation.getPartList()));
        others.remove(current);
        stackPresentation.close((IPresentablePart[]) others
                .toArray(new IPresentablePart[others.size()]));
    }

    public void update() {
        setTarget(stackPresentation.getSelectedPart());
    }

    public boolean shouldBeVisible() {
        return true;
    }

    /**
     * @param currentSelection
     * @since 3.1
     */
    public void setTarget(IPresentablePart current) {
        this.current = current;
        setEnabled(current != null && 
        		stackPresentation.getPartList().length > 1);
    }
}
