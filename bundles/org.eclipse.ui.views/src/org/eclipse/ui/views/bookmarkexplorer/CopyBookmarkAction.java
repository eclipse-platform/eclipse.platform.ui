/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

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
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.part.MarkerTransfer;


/**
 * Copies one or more bookmark(s) to the clipboard.
 */
class CopyBookmarkAction extends BookmarkAction {

	/**
	 * Creates the action.
	 */
	public CopyBookmarkAction(BookmarkNavigator bookmarkNavigator) {
		super(bookmarkNavigator, BookmarkMessages.getString("CopyBookmark.text")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IBookmarkHelpContextIds.COPY_BOOKMARK_ACTION);
		setEnabled(false);
	}
	
	/**
	 * Performs this action.
	 */
	public void run() {
		// Get the selected markers
		BookmarkNavigator bookmarkNavigator = getView();
		StructuredViewer viewer = bookmarkNavigator.getViewer();
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
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
			Object[] data = new Object[] {
				markers,
				markerReport};				
			Transfer[] transferTypes = new Transfer[] {
				MarkerTransfer.getInstance(),
				TextTransfer.getInstance()};
			
			// set the clipboard contents
			getView().getClipboard().setContents(data, transferTypes);
		} catch (SWTError e){
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
				throw e;
			if (MessageDialog.openQuestion(getView().getShell(), WorkbenchMessages.getString("CopyToClipboardProblemDialog.title"), WorkbenchMessages.getString("CopyToClipboardProblemDialog.message"))) //$NON-NLS-1$ //$NON-NLS-2$
				setClipboard(markers, markerReport);
		}	
	}
	
	private String createBookmarkReport(IMarker[] markers) {
		String report = ""; //$NON-NLS-1$
		String[] descriptions = new String[markers.length];
		String[] resources = new String[markers.length];
		String[] folders = new String[markers.length];
		String[] locations = new String[markers.length];
		String descriptionHeader = BookmarkMessages.getString("ColumnDescription.header"); //$NON-NLS-1$
		String resourceHeader = BookmarkMessages.getString("ColumnResource.header"); //$NON-NLS-1$
		String folderHeader = BookmarkMessages.getString("ColumnFolder.header"); //$NON-NLS-1$
		String locationHeader = BookmarkMessages.getString("ColumnLocation.header"); //$NON-NLS-1$
		int maxDescriptionLength = descriptionHeader.length(); 
		int maxResourceLength = resourceHeader.length();
		int maxFolderLength = folderHeader.length();
		
		for (int i = 0; i < markers.length; i++) {
			//get marker description
			descriptions[i] = MarkerUtil.getMessage(markers[i]);
			if (descriptions[i] == null)
				descriptions[i] = "";//$NON-NLS-1$
			if (descriptions[i].length() > maxDescriptionLength)
				maxDescriptionLength = descriptions[i].length();
				
			//get marker resource
			resources[i] = MarkerUtil.getResourceName(markers[i]);
			if (resources[i] == null)
				resources[i] = ""; //$NON-NLS-1$
			if (resources[i].length() > maxResourceLength)
				maxResourceLength = resources[i].length();
				
			//get marker folder names
			folders[i] = MarkerUtil.getContainerName(markers[i]);
			if (folders[i] == null)
				folders[i] = ""; //$NON-NLS-1$
			if (folders[i].length() > maxFolderLength)
				maxFolderLength = folders[i].length();
				
			//get marker location
			int line = MarkerUtil.getLineNumber(markers[i]);
			locations[i] = BookmarkMessages.format("LineIndicator.text", new String[] {String.valueOf(line)});//$NON-NLS-1$
			if (locations[i] == null)
				locations[i] = ""; //$NON-NLS-1$
		}
		
		//add headers
		report += descriptionHeader;
		for (int i = descriptionHeader.length(); i <= maxDescriptionLength; i++)
			report += ' ';
		report += resourceHeader;
		for (int i = resourceHeader.length(); i <= maxResourceLength; i++)
			report += ' ';
		report += folderHeader;
		for (int i = folderHeader.length(); i <= maxFolderLength; i++)
			report += ' ';
		report += locationHeader;
		report += System.getProperty("line.separator"); //$NON-NLS-1$
		
		//add marker info
		for (int i = 0; i < markers.length; i++) {
			report += descriptions[i];
			for (int j = descriptions[i].length(); j <= maxDescriptionLength; j++)
				report += ' ';
			report += resources[i];
			for (int j = resources[i].length(); j <= maxResourceLength; j++)
				report += ' ';
			report += folders[i];
			for (int j = folders[i].length(); j <= maxFolderLength; j++)
				report += ' ';
			report += locations[i];
			report += System.getProperty("line.separator"); //$NON-NLS-1$
		}
		
		return report;
	}
}


