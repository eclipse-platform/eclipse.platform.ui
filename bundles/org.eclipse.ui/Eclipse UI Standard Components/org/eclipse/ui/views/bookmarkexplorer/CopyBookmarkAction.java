package org.eclipse.ui.views.bookmarkexplorer;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.help.WorkbenchHelp;
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
		Object[] data = new Object[] {
			markers,
			buffer.toString()};				
		Transfer[] transferTypes = new Transfer[] {
			MarkerTransfer.getInstance(),
			TextTransfer.getInstance()};
		clipboard.setContents(data, transferTypes);
	}

	/** 
	 * Updates enablement based on the current selection
	 */
	public void selectionChanged(IStructuredSelection sel) {
		setEnabled(!sel.isEmpty());
	}
}


