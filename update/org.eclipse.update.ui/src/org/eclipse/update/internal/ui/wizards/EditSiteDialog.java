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
package org.eclipse.update.internal.ui.wizards;

import java.net.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;


public class EditSiteDialog extends NewUpdateSiteDialog {
	SiteBookmark bookmark;

	public EditSiteDialog(Shell parentShell, SiteBookmark bookmark) {
		super(parentShell);
		this.bookmark = bookmark;
	}

	protected void initializeFields() {
		name.setText(bookmark.getName());
		url.setText(bookmark.getURL().toString());
		url.setEditable(!bookmark.isLocal());
	}

	protected void update() {
		try {
			bookmark.setName(name.getText());
			bookmark.setURL(new URL(url.getText()));
			UpdateUI.getDefault().getUpdateModel().fireObjectChanged(bookmark, null);
		} catch (MalformedURLException e) {
		}
	}
}
