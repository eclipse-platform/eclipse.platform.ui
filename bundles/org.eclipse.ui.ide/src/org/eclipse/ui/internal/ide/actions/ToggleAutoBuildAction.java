/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Action for toggling autobuild on or off.
 */
public class ToggleAutoBuildAction extends Action implements
        ActionFactory.IWorkbenchAction {
    private IWorkbenchWindow window;

    /**
     * Creates a new ToggleAutoBuildAction
     * @param window The window for parenting dialogs associated with this action
     */
    public ToggleAutoBuildAction(IWorkbenchWindow window) {
        super(IDEWorkbenchMessages.Workbench_buildAutomatically);
        this.window = window;
        setChecked(ResourcesPlugin.getWorkspace().isAutoBuilding());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
     */
    public void dispose() {
        //nothing to dispose
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription description = workspace.getDescription();
        description.setAutoBuilding(!description.isAutoBuilding());
        try {
            workspace.setDescription(description);
        } catch (CoreException e) {
            ErrorDialog.openError(window.getShell(), null, null, e.getStatus());
        }
    }
}
