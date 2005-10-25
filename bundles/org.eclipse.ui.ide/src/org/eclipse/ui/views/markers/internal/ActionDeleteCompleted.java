/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.osgi.util.NLS;

/**
 * ActionDeleteCompleted is the action for deleting completed
 * markers.
 *
 */
public class ActionDeleteCompleted extends MarkerSelectionProviderAction {

    private TaskView part;

    /**
     * Constructs an ActionDeleteCompleted instance
     * 
     * @param part
     * @param provider
     */
    public ActionDeleteCompleted(TaskView part, ISelectionProvider provider) {
        super(provider, MarkerMessages.deleteCompletedAction_title);
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
            MessageDialog.openInformation(part.getSite().getShell(), 
            		MarkerMessages.deleteCompletedTasks_dialogTitle,
                    MarkerMessages.deleteCompletedTasks_noneCompleted);
            return;
        }
        String message;
        if (completed.size() == 1) {
            message = MarkerMessages
                    .deleteCompletedTasks_permanentSingular;
        } else {
            message = NLS.bind(
            		MarkerMessages.deleteCompletedTasks_permanentPlural,
            		String.valueOf(completed.size()));
        }
        // Verify.
        if (!MessageDialog.openConfirm(part.getSite().getShell(), MarkerMessages
                .deleteCompletedTasks_dialogTitle,
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
                            MarkerMessages
                                    .deleteCompletedTasks_errorMessage, null, e.getStatus()); 
        }
    }

    private List getCompletedTasks() {
        List completed = new ArrayList();

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
