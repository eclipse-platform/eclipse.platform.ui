package org.eclipse.ui.views.bookmarkexplorer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.help.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;

/**
 * Action to select all bookmarks.
 */
/* package */ class SelectAllAction extends BookmarkAction {
public SelectAllAction(BookmarkNavigator view) {
	super(view, BookmarkMessages.getString("SelectAll.text")); //$NON-NLS-1$
	setToolTipText(BookmarkMessages.getString("SelectAll.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, new Object[] {IBookmarkHelpContextIds.SELECT_ALL_BOOKMARK_ACTION});
	setEnabled(true);
	setAccelerator(SWT.CTRL |'a');
}
public void run() {
	Viewer viewer = getView().getViewer();
	Control control = viewer.getControl();
	if (control instanceof Tree) {
		((Tree) control).selectAll();
		viewer.setSelection(viewer.getSelection(), false);
	}
}
}
