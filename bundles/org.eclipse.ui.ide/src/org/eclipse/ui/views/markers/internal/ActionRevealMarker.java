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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;

public class ActionRevealMarker extends SelectionProviderAction {

    protected IWorkbenchPart part;

    public ActionRevealMarker(IWorkbenchPart part, ISelectionProvider provider) {
        super(provider, ""); //$NON-NLS-1$
        this.part = part;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        IStructuredSelection selection = getStructuredSelection();
        Object obj = selection.getFirstElement();
        if (obj == null || !(obj instanceof IMarker))
            return;

        IMarker marker = (IMarker) obj;
        IEditorPart editor = part.getSite().getPage().getActiveEditor();
        if (editor == null)
            return;
        IFile file = ResourceUtil.getFile(editor.getEditorInput());
        if (file != null) {
            if (marker.getResource().equals(file)) {
                try {
                    IDE.openEditor(part.getSite().getPage(), marker, false);
                } catch (CoreException e) {
                }
            }
        }
    }

    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(selection != null && selection.size() == 1);
        if (isEnabled()) {
            run();
        }
    }
}
