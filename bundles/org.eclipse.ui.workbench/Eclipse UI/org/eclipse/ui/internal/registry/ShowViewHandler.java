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

package org.eclipse.ui.internal.registry;

import java.util.Map;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Command handler to show a particular view.
 *
 * @since 3.0
 */
public class ShowViewHandler extends AbstractHandler {
    private String viewId;

    public ShowViewHandler(String viewId) {
        this.viewId = viewId;
    }

    public Object execute(Map parameterValuesByName) throws ExecutionException {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null)
            return null;
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        if (activePage == null)
            return null;
        try {
            activePage.showView(viewId);
        } catch (PartInitException e) {
            ErrorDialog.openError(activePage.getWorkbenchWindow().getShell(),
                    WorkbenchMessages.getString("ShowView.errorTitle"), //$NON-NLS-1$
                    e.getMessage(), e.getStatus());
        }
        return null;
    }
}