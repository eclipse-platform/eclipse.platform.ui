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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Command handler to open a particular perspective
 * 
 * @author Brock Janiczak (brockj_eclipse@ihug.com.au)
 * @since 3.1
 */
public class OpenPerspectiveHandler extends AbstractHandler {
    /**
     * The identifier of the perspective that should be opened when this handler
     * is executed.
     */
    private String perspectiveId;

    /**
     * Constructs a new <code>OpenPerspectiveHandler</code>.
     * 
     * @param perspectiveId
     *            The identifier of the perspective to open
     */
    public OpenPerspectiveHandler(String perspectiveId) {
        this.perspectiveId = perspectiveId;
    }

    /**
     * Opens the perspective with <code>perspectiveId</code> as its id.
     * 
     * @param parameterValuesByName
     *            Ignored.
     * @return <code>null</code>.
     */
    public Object execute(Map parameterValuesByName) {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow activeWorkbenchWindow = workbench
                .getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null)
            return null;

        IAdaptable input = null;

        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        if (activePage != null)
            input = activePage.getInput();

        try {
            workbench.showPerspective(perspectiveId, activeWorkbenchWindow,
                    input); //$NON-NLS-1$
        } catch (WorkbenchException e) {
            ErrorDialog.openError(activeWorkbenchWindow.getShell(),
                    WorkbenchMessages
                            .getString("ChangeToPerspectiveMenu.errorTitle"), //$NON-NLS-1$
                    e.getMessage(), e.getStatus());
        }
        
        return null;
    }
}
