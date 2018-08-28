/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.bookmarkexplorer;

import org.eclipse.osgi.util.NLS;

public class BookmarkMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.bookmarkexplorer.messages";//$NON-NLS-1$


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


	public static String LineIndicator_text;

	public static String ColumnIcon_header;
	public static String ColumnDescription_header;
	public static String ColumnResource_header;
	public static String ColumnFolder_header;
	public static String ColumnLocation_header;

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
