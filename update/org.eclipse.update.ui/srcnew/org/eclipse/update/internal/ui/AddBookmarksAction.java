/*
 * Created on Jun 10, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.update.internal.ui.wizards.NewWebSiteDialog;

/**
 * @author wassimm
 */
public class AddBookmarksAction extends Action {

	public AddBookmarksAction() {
		setText("Add Bookmark...");
	}

	public void run() {
		NewWebSiteDialog dialog = new NewWebSiteDialog(UpdateUI.getActiveWorkbenchShell());
		dialog.create();
		dialog.getShell().setText("New Bookmark");
		if (dialog.open() == NewWebSiteDialog.OK) {
			UpdateUI.getDefault().getUpdateModel().saveBookmarks();
		}
	}
}
