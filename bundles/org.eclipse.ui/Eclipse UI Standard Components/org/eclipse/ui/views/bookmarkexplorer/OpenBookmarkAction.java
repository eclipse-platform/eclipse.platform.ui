package org.eclipse.ui.views.bookmarkexplorer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import java.util.*;

/**
 * Action to open an editor on the selected bookmarks.
 */
/* package */ class OpenBookmarkAction extends BookmarkAction {
public OpenBookmarkAction(BookmarkNavigator view) {
	super(view, BookmarkMessages.getString("OpenBookmark.text")); //$NON-NLS-1$
	setToolTipText(BookmarkMessages.getString("OpenBookmark.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, new Object[] {IBookmarkHelpContextIds.OPEN_BOOKMARK_ACTION});
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
				BookmarkMessages.getString("OpenBookmark.errorTitle"), //$NON-NLS-1$
				e.getMessage(),
				e);
		}
	}
}
public void selectionChanged(IStructuredSelection sel) {
	setEnabled(!sel.isEmpty());
}
}
