package org.eclipse.update.internal.ui.wizards;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.model.*;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class NewFolderWizardPage extends BaseNewWizardPage {
	/**
	 * Constructor for NewFolderWizardPage.
	 * @param folder
	 */
	public NewFolderWizardPage(BookmarkFolder folder) {
		super(folder);
		setTitle("Create New Folder");
		setDescription("Create a new folder to organize your bookmarks. You can choose an existing folder as a container or create a new root folder.");
	}

	/**
	 * @see BaseNewWizardPage#createClientControl(Composite, int)
	 */
	protected void createClientControl(Composite parent, int span) {
	}

	public boolean finish() {
		BookmarkFolder newFolder = new BookmarkFolder(getName());
		addToModel(newFolder);
		return true;
	}
}
