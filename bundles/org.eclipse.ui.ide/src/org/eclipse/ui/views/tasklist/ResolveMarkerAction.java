/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.MarkerResolutionSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.views.tasklist.TaskListMessages;

/**
 * This action displays a list of resolutions for the selected marker
 * 
 * @since 2.0
 */
class ResolveMarkerAction extends TaskAction {

    /**
     * Creates the action.
     */
    protected ResolveMarkerAction(TaskList tasklist, String id) {
        super(tasklist, id);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                ITaskListHelpContextIds.RESOLVE_MARKER_ACTION);
    }

    /**
     * Returns whether this action should be enabled given the selection.
     * 
     * @param selection the selection
     * @return enablement
     */
    public boolean shouldEnable(IStructuredSelection selection) {
        if (selection.size() != 1) {
			return false;
		}
        IMarker marker = (IMarker) selection.getFirstElement();
        if (marker == null) {
			return false;
		}
        return IDE.getMarkerHelpRegistry().hasResolutions(marker);
    }

    /**
     * Displays a list of resolutions and performs the selection.
     */
    public void run() {
        IMarker marker = getMarker();
        if (marker == null) {
            return;
        }
        getTaskList().cancelEditing();
        IMarkerResolution[] resolutions = getResolutions(marker);
        if (resolutions.length == 0) {
            MessageDialog.openInformation(getShell(), TaskListMessages.Resolve_title, 
                    TaskListMessages.Resolve_noResolutionsLabel); 
            return;
        }
        MarkerResolutionSelectionDialog d = new MarkerResolutionSelectionDialog(
                getShell(), resolutions);
        if (d.open() != Window.OK) {
			return;
		}
        Object[] result = d.getResult();
        if (result != null && result.length > 0) {
			((IMarkerResolution) result[0]).run(marker);
		}
    }

    /**
     * Returns the resolutions for the given marker.
     *
     * @param marker the marker for which to obtain resolutions
     * @return the resolutions for the selected marker	
     */
    private IMarkerResolution[] getResolutions(IMarker marker) {
        return IDE.getMarkerHelpRegistry().getResolutions(marker);
    }

    /**
     * Returns the selected marker (may be <code>null</code>).
     * 
     * @return the selected marker
     */
    private IMarker getMarker() {
        IStructuredSelection selection = (IStructuredSelection) getTaskList()
                .getSelection();
        // only enable for single selection
        if (selection.size() != 1) {
			return null;
		}
        return (IMarker) selection.getFirstElement();
    }
}
