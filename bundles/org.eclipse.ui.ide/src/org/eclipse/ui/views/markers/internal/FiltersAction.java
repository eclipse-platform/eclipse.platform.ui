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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * This action opens a Filters Dialog and notifies the Marker View if the user has
 * modified the filter via the filters dialog.
 */
class FiltersAction extends Action {

    private MarkerView view;

    /**
     * Creates the action
     */
    public FiltersAction(MarkerView view) {
        super(MarkerMessages.filtersAction_title);
        setImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("elcl16/filter_ps.gif")); //$NON-NLS-1$
        setToolTipText(MarkerMessages.filtersAction_tooltip);
        this.view = view;
        setEnabled(true);
    }

    /**
     * Opens the dialog. Notifies the view if the filter has been modified.
     */
    public void run() {
        view.openFiltersDialog();
    }
}
