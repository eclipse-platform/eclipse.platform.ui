/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.*;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class NewFolderWizardPage extends BaseNewWizardPage {
	private static final String KEY_TITLE = "NewFolderWizardPage.title";
	private static final String KEY_DESC = "NewFolderWizardPage.desc";

	/**
	 * Constructor for NewFolderWizardPage.
	 * @param folder
	 */
	public NewFolderWizardPage(BookmarkFolder folder) {
		super(folder);
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
	}

	/**
	 * @see BaseNewWizardPage#createClientControl(Composite, int)
	 */
	protected void createClientControl(Composite parent, int span) {
		WorkbenchHelp.setHelp(parent, "org.eclipse.update.ui.NewFolderWizardPage");
	}

	public boolean finish() {
		BookmarkFolder newFolder = new BookmarkFolder(getName());
		addToModel(newFolder);
		return true;
	}
}
