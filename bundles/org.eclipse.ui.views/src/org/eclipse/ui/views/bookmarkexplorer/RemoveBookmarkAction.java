package org.eclipse.ui.views.bookmarkexplorer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.help.*;
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
	super(view, BookmarkMessages.getString("RemoveBookmark.text")); //$NON-NLS-1$
	setToolTipText(BookmarkMessages.getString("RemoveBookmark.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IBookmarkHelpContextIds.REMOVE_BOOKMARK_ACTION);
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
		ErrorDialog.openError(getView().getShell(), BookmarkMessages.getString("RemoveBookmark.errorTitle"), null, e.getStatus()); //$NON-NLS-1$
	}
}
public void selectionChanged(IStructuredSelection sel) {
	setEnabled(!sel.isEmpty());
}
}
