/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.views.markers.internal.DialogTaskProperties;

/**
 * Standard action for adding a task to the currently selected file
 * resource(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @since 2.1
 */
public class AddTaskAction extends SelectionListenerAction {
    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID + ".AddTaskAction";//$NON-NLS-1$

    /**
     * The shell in which to show any dialogs.
     */
    private Shell shell;

    /**
     * Creates a new instance of the receiver.
     * 
     * @param shell shell to use to show any dialogs
     */
    public AddTaskAction(Shell shell) {
        super(IDEWorkbenchMessages.getString("AddTaskLabel")); //$NON-NLS-1$		
        setId(ID);
        this.shell = shell;
        Assert.isNotNull(shell);
        setToolTipText(IDEWorkbenchMessages.getString("AddTaskToolTip")); //$NON-NLS-1$		
        WorkbenchHelp.setHelp(this, IIDEHelpContextIds.ADD_TASK_ACTION);
    }

    private IResource getElement(IStructuredSelection selection) {
        if (selection.size() != 1)
            return null;

        Object element = selection.getFirstElement();
        IResource resource = null;
        if (element instanceof IResource)
            resource = (IResource) element;
        if (element instanceof IAdaptable)
            resource = (IResource) ((IAdaptable) element)
                    .getAdapter(IResource.class);

        if (resource != null && resource instanceof IProject) {
            IProject project = (IProject) resource;
            if (project.isOpen() == false)
                resource = null;
        }
        return resource;
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        IResource resource = getElement(getStructuredSelection());
        if (resource != null) {
            DialogTaskProperties dialog = new DialogTaskProperties(shell);
            dialog.setResource(resource);
            dialog.open();
        }
    }

    /**
     * The <code>AddTaskAction</code> implementation of this
     * <code>SelectionListenerAction</code> method enables the action only
     * if the selection contains a single resource and the resource is
     * not a closed project.
     * 
     * @param selection the selection to update the enabled state for
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        return super.updateSelection(selection)
                && getElement(selection) != null;
    }
}