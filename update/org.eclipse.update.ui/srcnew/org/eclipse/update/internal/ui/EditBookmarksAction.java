/*
 * Created on Jun 4, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui;

import org.eclipse.jface.action.Action;

/**
 * @author Wassim Melhem
 */
public class EditBookmarksAction extends Action {

	public EditBookmarksAction() {
		setText("Edit Bookmarks...");
	}
	
	public void run() {
		WebBookmarksDialog dialog = new WebBookmarksDialog(UpdateUI.getActiveWorkbenchShell());
		dialog.create();
		dialog.getShell().setText("Bookmarks");
		dialog.getShell().setSize(300,300);
		dialog.open();
	}


}
