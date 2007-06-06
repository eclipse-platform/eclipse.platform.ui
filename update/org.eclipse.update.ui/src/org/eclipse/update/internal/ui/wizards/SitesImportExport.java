/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import java.io.*;
import java.util.Vector;

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
			UpdateUIMessages.LocalSiteSelector_dialogMessageImport); 

		dialog.setFilterExtensions(new String[] { "*.xml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$

		SiteBookmark[] siteBookmarks = null;

		String bookmarksFile = dialog.open();
		while (bookmarksFile != null && siteBookmarks == null) {
			File file = new File(bookmarksFile);
			siteBookmarks = createImportedBookmarks(file);
			if (siteBookmarks == null || siteBookmarks.length == 0) {
				MessageDialog.openInformation(
					parent,
					UpdateUIMessages.LocalSiteSelector_importInfoTitle, 
					UpdateUIMessages.LocalSiteSelector_importInfoMessage); 
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
			UpdateUIMessages.LocalSiteSelector_dialogMessageExport); 

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
