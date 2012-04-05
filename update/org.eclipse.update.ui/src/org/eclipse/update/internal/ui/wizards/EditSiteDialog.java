/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.SiteBookmark;


public class EditSiteDialog extends NewUpdateSiteDialog {
	SiteBookmark bookmark;
	private int ignoreBookmark = -1;

	public EditSiteDialog(Shell parentShell, SiteBookmark bookmark, SiteBookmark[] siteBookmarks) {
		super(parentShell, siteBookmarks);
		this.bookmark = bookmark;
		ignoreBookmark(siteBookmarks);
	}

	public EditSiteDialog(Shell parentShell, SiteBookmark bookmark, SiteBookmark[] siteBookmarks, boolean enableOK) {
		super(parentShell, enableOK, siteBookmarks);
		this.bookmark = bookmark;
		ignoreBookmark(siteBookmarks);
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
	
	private void ignoreBookmark( SiteBookmark[] siteBookmarks) {
		
		for( int i = 0; i < siteBookmarks.length; i++) {

			if (siteBookmarks[i].getLabel().equals(bookmark.getLabel().trim()) &&
				siteBookmarks[i].getURL().equals(bookmark.getURL())) {	
				ignoreBookmark = i;	
				return;
			}
			
		}
	}
	
	protected boolean isCurrentlyEditedSiteBookmark( int index) {
		
		return index == ignoreBookmark;
	}
}
