/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.bookmarkexplorer;

import org.eclipse.osgi.util.NLS;

public class BookmarkMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.bookmarkexplorer.messages";//$NON-NLS-1$
	//
	// Copyright (c) 2000, 2003 IBM Corporation and others.
	// All rights reserved. This program and the accompanying materials
	// are made available under the terms of the Eclipse Public License v1.0
	// which accompanies this distribution, and is available at
	// http://www.eclipse.org/legal/epl-v10.html
	//
	// Contributors:
	//     IBM Corporation - initial API and implementation
	//

	// package: org.eclipse.ui.views.bookmarkexplorer


	public static String CopyBookmark_text;

	public static String PasteBookmark_text;
	public static String PasteBookmark_undoText;
	public static String PasteBookmark_errorTitle;

	public static String OpenBookmark_text;
	public static String OpenBookmark_toolTip;
	public static String OpenBookmark_errorTitle;

	public static String RemoveBookmark_text;
	public static String RemoveBookmark_undoText;
	public static String RemoveBookmark_toolTip;
	public static String RemoveBookmark_errorTitle;

	public static String SelectAll_text;
	public static String SelectAll_toolTip;

	public static String Properties_text;

	public static String ColumnDescription_text;
	public static String ColumnResource_text;
	public static String ColumnFolder_text;
	public static String ColumnLocation_text;
	public static String ColumnCreationTime_text;
	
	public static String Error_text;

	public static String LineIndicator_text;

	public static String ColumnIcon_header;
	public static String ColumnDescription_header;
	public static String ColumnResource_header;
	public static String ColumnFolder_header;
	public static String ColumnLocation_header;

	public static String ColumnDescription_dialogText;
	public static String ColumnResource_dialogText;
	public static String ColumnFolder_dialogText;
	public static String ColumnLocation_dialogText;

	public static String SortMenuGroup_text;
	public static String SortDirectionAscending_text;
	public static String SortDirectionDescending_text;

	public static String PropertiesDialogTitle_text;
	public static String MarkerCreationTime_text;

	public static String CopyToClipboardProblemDialog_title;
	public static String CopyToClipboardProblemDialog_message;

	public static String CreateBookmark_undoText;
	public static String ModifyBookmark_undoText;	

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, BookmarkMessages.class);
	}
}
