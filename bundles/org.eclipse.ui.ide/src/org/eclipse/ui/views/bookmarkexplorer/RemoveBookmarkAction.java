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

package org.eclipse.ui.views.bookmarkexplorer;

import java.util.List;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.DeleteMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.views.bookmarkexplorer.BookmarkMessages;

/**
 * Action to remove the selected bookmarks.
 */
class RemoveBookmarkAction extends BookmarkAction {

    /**
     * Create a new instance of this class.
     * 
     * @param view the view
     */
    public RemoveBookmarkAction(BookmarkNavigator view) {
        super(view, BookmarkMessages.RemoveBookmark_text);
        setToolTipText(BookmarkMessages.RemoveBookmark_toolTip);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                IBookmarkHelpContextIds.REMOVE_BOOKMARK_ACTION);
        setEnabled(false);
    }

    /**
     * Delete the marker selection.
     */
    public void run() {
        final IStructuredSelection sel = getStructuredSelection();
        if (sel.isEmpty()) {
			return;
		}
        List list = sel.toList();
        IMarker[] markers = new IMarker[list.size()];
        list.toArray(markers);
     	IUndoableOperation op = new DeleteMarkersOperation(markers, BookmarkMessages.RemoveBookmark_undoText);
   		execute(op, BookmarkMessages.RemoveBookmark_errorTitle, null,
   				WorkspaceUndoUtil.getUIInfoAdapter(getView().getShell()));
    }

    public void selectionChanged(IStructuredSelection sel) {
        setEnabled(!sel.isEmpty());
    }
}
