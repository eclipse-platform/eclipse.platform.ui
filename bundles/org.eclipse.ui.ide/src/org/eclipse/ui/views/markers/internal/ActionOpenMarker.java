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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Action to open an editor on the selected bookmarks.
 */
public class ActionOpenMarker extends MarkerSelectionProviderAction {

    private final String IMAGE_PATH = "elcl16/gotoobj_tsk.gif"; //$NON-NLS-1$

    private final String DISABLED_IMAGE_PATH = "dlcl16/gotoobj_tsk.gif"; //$NON-NLS-1$

    protected IWorkbenchPart part;

    /**
     * Create a new instance of the receiver.
     * @param part
     * @param provider
     */
    public ActionOpenMarker(IWorkbenchPart part, ISelectionProvider provider) {
        super(provider, MarkerMessages.openAction_title);
        this.part = part;
        setImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor(IMAGE_PATH));
        setDisabledImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor(DISABLED_IMAGE_PATH));
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        IMarker marker = getSelectedMarker();
        //optimization: if the active editor has the same input as the selected marker then 
        //RevealMarkerAction would have been run and we only need to activate the editor
        IEditorPart editor = part.getSite().getPage().getActiveEditor();
        if (editor != null) {
            IEditorInput input = editor.getEditorInput();
            IFile file = ResourceUtil.getFile(input);
            if (file != null) {
                if (marker.getResource().equals(file)) {
                    part.getSite().getPage().activate(editor);
                }
            }
        }

        if (marker.getResource() instanceof IFile) {
            try {
                IDE.openEditor(part.getSite().getPage(), marker, OpenStrategy
                        .activateOnOpen());
            } catch (PartInitException e) {
                // Open an error style dialog for PartInitException by
                // including any extra information from the nested
                // CoreException if present.

                // Check for a nested CoreException
                CoreException nestedException = null;
                IStatus status = e.getStatus();
                if (status != null
                        && status.getException() instanceof CoreException)
                    nestedException = (CoreException) status.getException();

                if (nestedException != null) {
                    // Open an error dialog and include the extra
                    // status information from the nested CoreException
                    ErrorDialog.openError(part.getSite().getShell(), 
                    		MarkerMessages.OpenMarker_errorTitle,
                            e.getMessage(), nestedException.getStatus());
                } else {
                    // Open a regular error dialog since there is no
                    // extra information to display
                    MessageDialog.openError(part.getSite().getShell(), 
                    		MarkerMessages.OpenMarker_errorTitle,
                            e.getMessage());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(Util.isSingleConcreteSelection(selection));
    }
}
