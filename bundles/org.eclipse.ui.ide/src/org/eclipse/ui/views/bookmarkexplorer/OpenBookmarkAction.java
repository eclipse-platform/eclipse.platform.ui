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

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.ide.IDE;

/**
 * Action to open an editor on the selected bookmarks.
 */
class OpenBookmarkAction extends BookmarkAction {

    public OpenBookmarkAction(BookmarkNavigator view) {
        super(view, BookmarkMessages.getString("OpenBookmark.text")); //$NON-NLS-1$
        setToolTipText(BookmarkMessages.getString("OpenBookmark.toolTip")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this,
                IBookmarkHelpContextIds.OPEN_BOOKMARK_ACTION);
        setEnabled(false);
    }

    public void run() {
        IWorkbenchPage page = getView().getSite().getPage();
        for (Iterator i = getStructuredSelection().iterator(); i.hasNext();) {
            IMarker marker = (IMarker) i.next();
            try {
                IDE.openEditor(page, marker, OpenStrategy.activateOnOpen());
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
                    ErrorDialog.openError(getView().getShell(),
                            BookmarkMessages
                                    .getString("OpenBookmark.errorTitle"), //$NON-NLS-1$
                            e.getMessage(), nestedException.getStatus());
                } else {
                    // Open a regular error dialog since there is no
                    // extra information to display
                    MessageDialog.openError(getView().getShell(),
                            BookmarkMessages
                                    .getString("OpenBookmark.errorTitle"), //$NON-NLS-1$
                            e.getMessage());
                }
            }
        }
    }

    public void selectionChanged(IStructuredSelection sel) {
        setEnabled(!sel.isEmpty());
    }
}