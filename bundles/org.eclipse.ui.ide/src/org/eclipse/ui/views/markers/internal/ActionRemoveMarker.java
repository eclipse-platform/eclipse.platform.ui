/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;

/**
 * Action to remove the selected bookmarks.
 */
public class ActionRemoveMarker extends SelectionProviderAction {

    private IWorkbenchPart part;

    /**
     * Creates the action.
     */
    public ActionRemoveMarker(IWorkbenchPart part, ISelectionProvider provider) {
        super(provider, MarkerMessages.deleteAction_title);
        this.part = part;
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
        setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
        setToolTipText(MarkerMessages.deleteAction_tooltip);
        setEnabled(false);
    }

    /**
     * Delete the marker selection.
     */
    public void run() {
        if (!isEnabled()) {
            return;
        }
        final IStructuredSelection selection = getStructuredSelection();
        if (selection.isEmpty()) {
            return;
        }
        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    for (Iterator iter = selection.iterator(); iter.hasNext();) {
                        Object o = iter.next();
                        if (o instanceof IMarker) {
                            IMarker marker = (IMarker) o;
                            marker.delete();
                        }
                    }
                }
            }, null);
        } catch (CoreException e) {
            ErrorDialog.openError(part.getSite().getShell(), 
            		MarkerMessages.RemoveMarker_errorTitle, 
            		null, e.getStatus()); 
        }
    }

    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(false);
        if (selection == null || selection.isEmpty()) {
            return;
        }
        for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();
            if (!(obj instanceof IMarker)) {
                return;
            }
            IMarker marker = (IMarker) obj;
            if (!Util.isEditable(marker)) {
                return;
            }
        }
        setEnabled(true);
    }
}
