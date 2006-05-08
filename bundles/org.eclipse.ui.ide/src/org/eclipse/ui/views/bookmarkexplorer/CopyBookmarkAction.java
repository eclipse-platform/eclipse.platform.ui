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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.bookmarkexplorer.BookmarkMessages;
import org.eclipse.ui.part.MarkerTransfer;

/**
 * Copies one or more bookmark(s) to the clipboard.
 */
class CopyBookmarkAction extends BookmarkAction {

    /**
     * Creates the action.
     * 
     * @param bookmarkNavigator the view
     */
    public CopyBookmarkAction(BookmarkNavigator bookmarkNavigator) {
        super(bookmarkNavigator, BookmarkMessages.CopyBookmark_text);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                IBookmarkHelpContextIds.COPY_BOOKMARK_ACTION);
        setEnabled(false);
    }

    /**
     * Performs this action.
     */
    public void run() {
        // Get the selected markers
        BookmarkNavigator bookmarkNavigator = getView();
        StructuredViewer viewer = bookmarkNavigator.getViewer();
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        if (selection.isEmpty()) {
            return;
        }
        List list = selection.toList();
        IMarker[] markers = new IMarker[list.size()];
        list.toArray(markers);

        setClipboard(markers, createBookmarkReport(markers));
    }

    /** 
     * Updates enablement based on the current selection
     */
    public void selectionChanged(IStructuredSelection sel) {
        setEnabled(!sel.isEmpty());
    }

    private void setClipboard(IMarker[] markers, String markerReport) {
        try {
            // Place the markers on the clipboard
            Object[] data = new Object[] { markers, markerReport };
            Transfer[] transferTypes = new Transfer[] {
                    MarkerTransfer.getInstance(), TextTransfer.getInstance() };

            // set the clipboard contents
            getView().getClipboard().setContents(data, transferTypes);
        } catch (SWTError e) {
            if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
            if (MessageDialog
                    .openQuestion(
                            getView().getShell(),
                            BookmarkMessages.CopyToClipboardProblemDialog_title, BookmarkMessages.CopyToClipboardProblemDialog_message)) {
				setClipboard(markers, markerReport);
			}
        }
    }

    private String createBookmarkReport(IMarker[] markers) {
        String report = ""; //$NON-NLS-1$

        //write header
        report += BookmarkMessages.ColumnDescription_header + '\t';
        report += BookmarkMessages.ColumnResource_header + '\t';
        report += BookmarkMessages.ColumnFolder_header + '\t';
        report += BookmarkMessages.ColumnLocation_header;
        report += System.getProperty("line.separator"); //$NON-NLS-1$

        //write markers
        for (int i = 0; i < markers.length; i++) {
            report += MarkerUtil.getMessage(markers[i]) + '\t';
            report += MarkerUtil.getResourceName(markers[i]) + '\t';
            report += MarkerUtil.getContainerName(markers[i]) + '\t';
            int line = MarkerUtil.getLineNumber(markers[i]);
            report += NLS.bind(BookmarkMessages.LineIndicator_text, String.valueOf(line));
            report += System.getProperty("line.separator"); //$NON-NLS-1$
        }

        return report;
    }
}

