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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuFastView extends Action implements ISelfUpdatingAction {

    private ViewPane viewPane;

    private IStackPresentationSite site;

    public SystemMenuFastView(IStackPresentationSite site) {
        this.site = site;
        setText(WorkbenchMessages.getString("ViewPane.fastView")); //$NON-NLS-1$
        update();
    }

    public void setPane(ViewPane newPane) {
        viewPane = newPane;
        update();
    }

    public void update() {
        if (viewPane == null
                || !site.isPartMoveable(viewPane.getPresentablePart())) {
            setEnabled(false);
        } else {
            setEnabled(true);
            setChecked(viewPane.getPage().getActivePerspective().isFastView(
                    viewPane.getViewReference()));
        }
    }

    public boolean shouldBeVisible() {
        if (viewPane == null || viewPane.getPage() == null) {
            return false;
        }

        WorkbenchWindow workbenchWindow = (WorkbenchWindow) viewPane.getPage()
                .getWorkbenchWindow();

        return workbenchWindow.getShowFastViewBars() && viewPane != null
                && site.isPartMoveable(viewPane.getPresentablePart());
    }

    public void dispose() {
        viewPane = null;
    }

    public void run() {
        if (!isChecked()) {
            viewPane.doMakeFast();
        } else {
            viewPane.doRemoveFast();
        }
    }
}