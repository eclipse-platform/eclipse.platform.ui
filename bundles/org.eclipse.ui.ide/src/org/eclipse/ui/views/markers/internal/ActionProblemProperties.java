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

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * ActionProblemProperties is the action for opening the properties
 * in the problems view.
 *
 */
public class ActionProblemProperties extends MarkerSelectionProviderAction {

    private IWorkbenchPart part;

    /**
     * Create a new instance of the receiver.
     * @param part
     * @param provider
     */
    public ActionProblemProperties(IWorkbenchPart part,
            ISelectionProvider provider) {
        super(provider, MarkerMessages.propertiesAction_title);
        setEnabled(false);
        this.part = part;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
 
    	IMarker marker = getSelectedMarker();
        DialogMarkerProperties dialog = new DialogProblemProperties(part
                .getSite().getShell());
        dialog.setMarker(marker);
        dialog.open();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.SelectionProviderAction#selectionChanged(org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(Util.isSingleConcreteSelection(selection));
    }
}
