package org.eclipse.ui.views.bookmarkexplorer;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.part.MarkerTransfer;


/**
 * Copies a task to the clipboard.
 */
/*package*/ class CopyBookmarkAction extends BookmarkAction {
	/**
	 * System clipboard
	 */
	private Clipboard clipboard;

	/**
	 * Creates the action.
	 */
	public CopyBookmarkAction(BookmarkNavigator bookmarkNavigator) {
		super(bookmarkNavigator, BookmarkMessages.getString("CopyBookmark.text")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IBookmarkHelpContextIds.COPY_BOOKMARK_ACTION);
		setEnabled(false);
		clipboard = new Clipboard(Display.getCurrent());
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

		// Place the markers on the clipboard
		StringBuffer buffer = new StringBuffer();
		ILabelProvider provider = (ILabelProvider)bookmarkNavigator.getViewer().getLabelProvider();
		for (int i = 0; i < markers.length; i++) {
			if (i > 0)
				buffer.append(System.getProperty("line.separator")); //$NON-NLS-1$
			buffer.append(provider.getText(markers[i]));
		} 

		setClipboard(markers, buffer.toString());
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
			clipboard.setContents(data, transferTypes);
		} catch (SWTError e){
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
				throw e;
			if (MessageDialog.openQuestion(getView().getShell(), WorkbenchMessages.getString("CopyToClipboardProblemDialog.title"), WorkbenchMessages.getString("CopyToClipboardProblemDialog.message"))) //$NON-NLS-1$ //$NON-NLS-2$
				setClipboard(markers, markerReport);
		}	
	}
}


