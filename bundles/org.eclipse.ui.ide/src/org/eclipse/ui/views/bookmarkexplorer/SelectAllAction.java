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

package org.eclipse.ui.views.bookmarkexplorer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action to select all bookmarks.
 */
class SelectAllAction extends BookmarkAction {

    public SelectAllAction(BookmarkNavigator view) {
        super(view, BookmarkMessages.getString("SelectAll.text")); //$NON-NLS-1$
        setToolTipText(BookmarkMessages.getString("SelectAll.toolTip")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this,
                IBookmarkHelpContextIds.SELECT_ALL_BOOKMARK_ACTION);
        setEnabled(true);
    }

    public void run() {
        Viewer viewer = getView().getViewer();
        Control control = viewer.getControl();
        if (control instanceof Table) {
            ((Table) control).selectAll();
            viewer.setSelection(viewer.getSelection(), false);
        }
    }
}