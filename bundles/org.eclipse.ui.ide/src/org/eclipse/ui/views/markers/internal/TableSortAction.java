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
import org.eclipse.jface.window.Window;

public class TableSortAction extends Action {

    private TableView view;

    private TableSortDialog dialog;

    public TableSortAction(TableView view, TableSortDialog dialog) {
        super(MarkerMessages.sortAction_title);
        this.view = view;
        this.dialog = dialog;
        setEnabled(true);
    }

    public void run() {
        if (dialog.open() == Window.OK && dialog.isDirty()) {
            view.setComparator(dialog.getSorter());
        }
    }
}
