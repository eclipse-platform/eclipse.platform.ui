/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;

public class TableSortAction extends Action {

    private TableView view;

    private TableSortDialog dialog;

    public TableSortAction(TableView view, TableSortDialog dialog) {
        super(Messages.getString("sortAction.title")); //$NON-NLS-1$
        this.view = view;
        this.dialog = dialog;
        setEnabled(true);
    }

    public void run() {
        if (dialog.open() == Window.OK && dialog.isDirty()) {
            view.setSorter(dialog.getSorter());
        }
    }
}