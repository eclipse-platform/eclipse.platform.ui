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

package org.eclipse.ui.views.bookmarkexplorer;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
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
        if (sel.isEmpty())
            return;
        try {
            getView().getWorkspace().run(new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                    for (Iterator iter = sel.iterator(); iter.hasNext();) {
                        Object o = iter.next();
                        if (o instanceof IMarker) {
                            IMarker marker = (IMarker) o;
                            marker.delete();
                        }
                    }
                }
            }, null);
        } catch (CoreException e) {
            ErrorDialog
                    .openError(
                            getView().getShell(),
                            BookmarkMessages.RemoveBookmark_errorTitle, null, e.getStatus());
        }
    }

    public void selectionChanged(IStructuredSelection sel) {
        setEnabled(!sel.isEmpty());
    }
}
