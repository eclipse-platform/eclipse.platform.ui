package org.eclipse.ui.views.bookmarkexplorer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.*;
import java.util.*;

/**
 * Action to remove the selected bookmarks.
 */
/* package */ class RemoveBookmarkAction extends BookmarkAction {
public RemoveBookmarkAction(BookmarkNavigator view) {
	super(view, "&Delete");
	setToolTipText("Delete");
	setEnabled(false);
}
/**
 * Delete the marker selection.
 */
public void run() {
	final IStructuredSelection sel = getStructuredSelection();
	if (sel.isEmpty())
		return;
	try {
		getView().getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (Iterator iter = sel.iterator(); iter.hasNext();) {
					Object o = iter.next();
					if (o instanceof IMarker) {
						IMarker marker = (IMarker) o;
						marker.delete();
					}
				}
			}
		}, null);
	} catch (CoreException e) {
		ErrorDialog.openError(getView().getShell(), "Error deleting bookmarks", null, e.getStatus());
	}
}
public void selectionChanged(IStructuredSelection sel) {
	setEnabled(!sel.isEmpty());
}
}
