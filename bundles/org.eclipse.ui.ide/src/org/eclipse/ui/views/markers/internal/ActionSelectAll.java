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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.actions.SelectionProviderAction;

public class ActionSelectAll extends SelectionProviderAction {

    /**
     * @param provider
     */
    public ActionSelectAll(ISelectionProvider provider) {
        super(provider, MarkerMessages.selectAllAction_title);
        setEnabled(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        TableViewer viewer = (TableViewer) getSelectionProvider();

        Object[] elements = ((IStructuredContentProvider) viewer
                .getContentProvider()).getElements(viewer.getInput());

        StructuredSelection newSelection = new StructuredSelection(elements);
        super.getSelectionProvider().setSelection(newSelection);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(!selection.isEmpty());
    }
}
