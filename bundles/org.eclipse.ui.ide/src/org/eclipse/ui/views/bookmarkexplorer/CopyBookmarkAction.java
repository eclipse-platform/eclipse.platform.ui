/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@Override
	public void run() {
		// Get the selected markers
		BookmarkNavigator bookmarkNavigator = getView();
		StructuredViewer viewer = bookmarkNavigator.getViewer();
		IStructuredSelection selection = viewer.getStructuredSelection();
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
	@Override
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
		StringBuilder report = new StringBuilder();
		// write header
		report.append(BookmarkMessages.ColumnDescription_header).append('\t');
		report.append(BookmarkMessages.ColumnResource_header).append('\t');
		report.append(BookmarkMessages.ColumnFolder_header).append('\t');
		report.append(BookmarkMessages.ColumnLocation_header);
		report.append(System.getProperty("line.separator")); //$NON-NLS-1$

		// write markers
		for (IMarker marker : markers) {
			report.append(MarkerUtil.getMessage(marker)).append('\t');
			report.append(MarkerUtil.getResourceName(marker)).append('\t');
			report.append(MarkerUtil.getContainerName(marker)).append('\t');
			int line = MarkerUtil.getLineNumber(marker);
			report.append(NLS.bind(BookmarkMessages.LineIndicator_text, String.valueOf(line)));
			report.append(System.getProperty("line.separator")); //$NON-NLS-1$
		}

		return report.toString();
	}
}

