package org.eclipse.ui.views.bookmarkexplorer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;

/**
 * Action to select all bookmarks.
 */
/* package */ class SelectAllAction extends BookmarkAction {
public SelectAllAction(BookmarkNavigator view) {
	super(view, "Select A&ll");
	setToolTipText("Select the entire contents");
	setEnabled(true);
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
