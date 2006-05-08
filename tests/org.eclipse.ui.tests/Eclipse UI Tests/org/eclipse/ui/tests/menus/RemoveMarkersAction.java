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
package org.eclipse.ui.tests.menus;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.tests.TestPlugin;

/**
 * @since 3.1
 */
public class RemoveMarkersAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow window;


    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        try {
	        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	        
	        IMarker[] markers = root.findMarkers(AddMarkersAction.CATEGORY_TEST_MARKER, false, IResource.DEPTH_ZERO);
	        
	        for (int i = 0; i < markers.length; i++) {
	            String message = (String) markers[i].getAttribute(IMarker.MESSAGE);

	            if (message != null && message.startsWith("this is a test")) {
	                markers[i].delete();
	            }
	        }
        } catch (CoreException e) {
            openError(e);
        }
    }

    private void openError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            msg = e.getClass().getName();
        }

        e.printStackTrace();

        IStatus status = new Status(IStatus.ERROR, TestPlugin.getDefault()
                .getDescriptor().getUniqueIdentifier(), 0, msg, e);

        TestPlugin.getDefault().getLog().log(status);

        ErrorDialog.openError(window.getShell(), "Error", msg, status);
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }

}
