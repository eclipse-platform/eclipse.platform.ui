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
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MarkerTransfer;

/**
 * Pastes one or more bookmark(s) from the clipboard into the bookmark navigator.
 */
public class ActionPasteMarker extends MarkerSelectionProviderAction {

    private IWorkbenchPart part;

    private Clipboard clipboard;

    private String[] pastableTypes;

    /**
     * Creates the action.
     * @param part
     * @param provider
     */
    public ActionPasteMarker(IWorkbenchPart part, ISelectionProvider provider) {
        super(provider, MarkerMessages.pasteAction_title);
        this.part = part;
        this.pastableTypes = new String[0];
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
        setEnabled(false);
    }

    void setClipboard(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    /**
     * Copies the marker(s) from the clipboard to the bookmark navigator view.
     */
    public void run() {
        // Get the markers from the clipboard
        MarkerTransfer transfer = MarkerTransfer.getInstance();
        IMarker[] markerData = (IMarker[]) clipboard.getContents(transfer);
        paste(markerData);
    }

    void paste(final IMarker[] markers) {
        if (markers == null)
            return;

        final ArrayList newMarkers = new ArrayList();

        try {
            ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    for (int i = 0; i < markers.length; i++) {
                        String type = markers[i].getType();
                        // Paste to the same resource as the original
                        IResource resource = markers[i].getResource();
                        Map attributes = markers[i].getAttributes();
                        IMarker marker = resource.createMarker(type);
                        marker.setAttributes(attributes);
                        newMarkers.add(marker);
                    }
                }
            }, null);
        } catch (CoreException e) {
            ErrorDialog.openError(part.getSite().getShell(), 
            		MarkerMessages.PasteMarker_errorTitle,
                    null, e.getStatus());
            return;
        }

        // Need to do this in an asyncExec, even though we're in the UI thread here,
        // since the marker view updates itself with the addition in an asyncExec,
        // which hasn't been processed yet.
        // Must be done outside IWorkspaceRunnable above since notification for add is
        // sent after IWorkspaceRunnable is run.
        if (getSelectionProvider() != null && newMarkers.size() > 0) {
            part.getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    getSelectionProvider().setSelection(
                            new StructuredSelection(newMarkers));
                }
            });
        }
    }

    void updateEnablement() {
        setEnabled(false);
        if (clipboard == null) {
            return;
        }

        // Paste if clipboard contains pastable markers
        MarkerTransfer transfer = MarkerTransfer.getInstance();
        IMarker[] markerData = (IMarker[]) clipboard.getContents(transfer);
        if (markerData == null || markerData.length < 1
                || pastableTypes == null) {
            return;
        }
        for (int i = 0; i < markerData.length; i++) {
            try {
                IMarker marker = markerData[i];
                if (!marker.exists()) {
                    break;
                }
                boolean pastable = false;
                for (int j = 0; j < pastableTypes.length; j++) {
                    if (marker.isSubtypeOf(pastableTypes[j])) {
                        pastable = true;
                        break;
                    }
                }
                if (!pastable) {
                    return;
                }
                if (!Util.isEditable(marker)) {
                    return;
                }
            } catch (CoreException e) {
                return;
            }
        }
        setEnabled(true);
    }

    /**
     * @param strings
     */
    void setPastableTypes(String[] strings) {
        pastableTypes = strings;
    }
}
