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

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.MarkerTransfer;

/**
 * Copies one or more bookmark(s) to the clipboard.
 */
class CopyBookmarkAction extends BookmarkAction {

    /**
     * Creates the action.
     */
    public CopyBookmarkAction(BookmarkNavigator bookmarkNavigator) {
        super(bookmarkNavigator, BookmarkMessages
                .getString("CopyBookmark.text")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this,
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
            if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
                throw e;
            if (MessageDialog
                    .openQuestion(
                            getView().getShell(),
                            BookmarkMessages
                                    .getString("CopyToClipboardProblemDialog.title"), BookmarkMessages.getString("CopyToClipboardProblemDialog.message"))) //$NON-NLS-1$ //$NON-NLS-2$
                setClipboard(markers, markerReport);
        }
    }

    private String createBookmarkReport(IMarker[] markers) {
        String report = ""; //$NON-NLS-1$

        //write header
        report += BookmarkMessages.getString("ColumnDescription.header") + '\t'; //$NON-NLS-1$
        report += BookmarkMessages.getString("ColumnResource.header") + '\t'; //$NON-NLS-1$
        report += BookmarkMessages.getString("ColumnFolder.header") + '\t'; //$NON-NLS-1$
        report += BookmarkMessages.getString("ColumnLocation.header"); //$NON-NLS-1$
        report += System.getProperty("line.separator"); //$NON-NLS-1$

        //write markers
        for (int i = 0; i < markers.length; i++) {
            report += MarkerUtil.getMessage(markers[i]) + '\t';
            report += MarkerUtil.getResourceName(markers[i]) + '\t';
            report += MarkerUtil.getContainerName(markers[i]) + '\t';
            int line = MarkerUtil.getLineNumber(markers[i]);
            report += BookmarkMessages
                    .format(
                            "LineIndicator.text", new String[] { String.valueOf(line) });//$NON-NLS-1$
            report += System.getProperty("line.separator"); //$NON-NLS-1$
        }

        return report;
    }
}

