package org.eclipse.ui.views.bookmarkexplorer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import java.util.*;

/**
 * Action to open an editor on the selected bookmarks.
 */
/* package */ class OpenBookmarkAction extends BookmarkAction {
public OpenBookmarkAction(BookmarkNavigator view) {
	super(view, "&Go to File");
	setToolTipText("Go to File");
	setEnabled(false);
}
public void run() {
	IWorkbenchPage page = getView().getSite().getPage();
	for (Iterator i = getStructuredSelection().iterator(); i.hasNext();) {
		IMarker marker = (IMarker) i.next();
		try {
			page.openEditor(marker);
		} catch (PartInitException e) {
			DialogUtil.openError(
				getView().getShell(),
				"Problems Opening Editor",
				e.getMessage(),
				e);
		}
	}
}
public void selectionChanged(IStructuredSelection sel) {
	setEnabled(!sel.isEmpty());
}
}
