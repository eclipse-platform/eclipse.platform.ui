/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

public class ActionDeleteCompleted extends SelectionProviderAction {

    private TaskView part;

    /**
     * Constructs an ActionDeleteCompleted instance
     * 
     * @param part
     * @param provider
     */
    public ActionDeleteCompleted(TaskView part, ISelectionProvider provider) {
        super(provider, Messages.getString("deleteCompletedAction.title")); //$NON-NLS-1$
        this.part = part;
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        final List completed = getCompletedTasks();
        // Check if there is anything to do
        if (completed.size() == 0) {
            MessageDialog.openInformation(part.getSite().getShell(), Messages
                    .getString("deleteCompletedTasks.dialogTitle"), //$NON-NLS-1$
                    Messages.getString("deleteCompletedTasks.noneCompleted")); //$NON-NLS-1$
            return;
        }
        String message;
        if (completed.size() == 1) {
            message = Messages
                    .getString("deleteCompletedTasks.permanentSingular"); //$NON-NLS-1$
        } else {
            message = Messages.format("deleteCompletedTasks.permanentPlural", //$NON-NLS-1$
                    new Object[] { new Integer(completed.size()) });
        }
        // Verify.
        if (!MessageDialog.openConfirm(part.getSite().getShell(), Messages
                .getString("deleteCompletedTasks.dialogTitle"), //$NON-NLS-1$
                message)) {
            return;
        }
        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) {
                    for (int i = 0; i < completed.size(); i++) {
                        IMarker marker = (IMarker) completed.get(i);
                        try {
                            marker.delete();
                        } catch (CoreException e) {
                        }
                    }
                }
            }, null);
        } catch (CoreException e) {
            ErrorDialog
                    .openError(
                            part.getSite().getShell(),
                            Messages
                                    .getString("deleteCompletedTasks.errorMessage"), null, e.getStatus()); //$NON-NLS-1$
        }
    }

    private List getCompletedTasks() {
        List completed = new ArrayList();

        ISelectionProvider sel = getSelectionProvider();

        MarkerList markerList = part.getVisibleMarkers();

        ConcreteMarker[] markers = markerList.toArray();

        for (int i = 0; i < markers.length; i++) {
            ConcreteMarker marker = markers[i];
            if (marker instanceof TaskMarker) {
                TaskMarker taskMarker = (TaskMarker) marker;

                if (taskMarker.getDone() == 1) {
                    completed.add(taskMarker.getMarker());
                }
            }
        }

        return completed;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(!selection.isEmpty());
    }

}