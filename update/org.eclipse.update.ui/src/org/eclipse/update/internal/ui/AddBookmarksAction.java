/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui;

import org.eclipse.jface.action.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * Adds bookmarks to update sites
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
