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

import java.io.*;
import java.util.*;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;

/**
 */
public class SitesImportExport {

	static String lastLocation = null;
	
	public SitesImportExport() {
		super();
	}
	

	public static SiteBookmark[] getImportedBookmarks(Shell parent) {
		FileDialog dialog = new FileDialog(parent);
		dialog.setText(
			UpdateUI.getString("LocalSiteSelector.dialogMessageImport")); //$NON-NLS-1$

		dialog.setFilterExtensions(new String[] { "*.xml", "*" }); //$NON-NLS-1$

		SiteBookmark[] siteBookmarks = null;

		String bookmarksFile = dialog.open();
		while (bookmarksFile != null && siteBookmarks == null) {
			File file = new File(bookmarksFile);
			siteBookmarks = createImportedBookmarks(file);
			if (siteBookmarks == null || siteBookmarks.length == 0) {
				MessageDialog.openInformation(
					parent,
					UpdateUI.getString("LocalSiteSelector.importInfoTitle"), //$NON-NLS-1$
					UpdateUI.getString("LocalSiteSelector.importInfoMessage")); //$NON-NLS-1$
				bookmarksFile = dialog.open();
			} else {
				break;
			}
		}
		return siteBookmarks;
	}

	public static void exportBookmarks(Shell parent, SiteBookmark[] siteBookmarks) {
		FileDialog dialog = new FileDialog(parent, SWT.SAVE);
		dialog.setText(
			UpdateUI.getString("LocalSiteSelector.dialogMessageExport")); //$NON-NLS-1$

		dialog.setFileName("bookmarks.xml"); //$NON-NLS-1$

		String bookmarksFile = dialog.open();
		if (bookmarksFile == null)
			return;
		
		Vector bookmarks = new Vector(siteBookmarks.length);
		for (int i=0; i<siteBookmarks.length; i++)
			bookmarks.add(siteBookmarks[i]);
		BookmarkUtil.store(bookmarksFile, bookmarks);
	}
	
	/**
	 * Creates bookmarks out from the given file
	 * 
	 * @param file
	 * @return
	 */
	static SiteBookmark[] createImportedBookmarks(File file) {
		Vector bookmarks = new Vector();
		BookmarkUtil.parse(file.getAbsolutePath(), bookmarks);
		return BookmarkUtil.getBookmarks(bookmarks);
	}

}
