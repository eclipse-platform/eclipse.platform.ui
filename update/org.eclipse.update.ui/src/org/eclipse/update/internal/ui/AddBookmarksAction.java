/*
 * Created on Jun 10, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui;

import org.eclipse.jface.action.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @author wassimm
 */
public class AddBookmarksAction extends Action {

	public AddBookmarksAction() {
		setText(UpdateUI.getString("AddBookmarksAction.add")); //$NON-NLS-1$
	}

	public void run() {
		NewWebSiteDialog dialog = new NewWebSiteDialog(UpdateUI.getActiveWorkbenchShell());
		dialog.create();
		dialog.getShell().setText(UpdateUI.getString("AddBookmarksAction.new")); //$NON-NLS-1$
		if (dialog.open() == NewWebSiteDialog.OK) {
			UpdateUI.getDefault().getUpdateModel().saveBookmarks();
		}
	}
}
