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
package org.eclipse.help.internal.webapp.data;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.base.util.*;

/**
 * This class manages bookmarks.
 */
public class BookmarksData extends RequestData {
	public final static int NONE = 0;
	public final static int ADD = 1;
	public final static int REMOVE = 2;
	public final static int REMOVE_ALL = 3;

	public BookmarksData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);

		switch (getOperation()) {
			case ADD :
				addBookmark();
				break;
			case REMOVE :
				removeBookmark();
				break;
			case REMOVE_ALL :
				removeAllBookmarks();
				break;
			default :
				break;
		}
	}

	public void addBookmark() {
		String bookmarkURL = request.getParameter("bookmark"); //$NON-NLS-1$
		if (bookmarkURL != null && bookmarkURL.length() > 0
				&& !bookmarkURL.equals("about:blank")) { //$NON-NLS-1$
			String title = request.getParameter("title"); //$NON-NLS-1$
			if (title == null) {
				return;
			}
			Preferences prefs = HelpBasePlugin.getDefault()
					.getPluginPreferences();
			String bookmarks = prefs.getString(BaseHelpSystem.BOOKMARKS);

			// separate the url and title by vertical bar

			// check for duplicates
			if (bookmarks.indexOf("," + encode(bookmarkURL) + "|") != -1) //$NON-NLS-1$ //$NON-NLS-2$
				return;
			bookmarks = bookmarks
					+ "," + encode(bookmarkURL) + "|" + encode(title); //$NON-NLS-1$ //$NON-NLS-2$
			prefs.setValue(BaseHelpSystem.BOOKMARKS, bookmarks);
			HelpBasePlugin.getDefault().savePluginPreferences();
		}
	}

	public void removeBookmark() {
		String bookmarkURL = request.getParameter("bookmark"); //$NON-NLS-1$
		if (bookmarkURL != null && bookmarkURL.length() > 0
				&& !bookmarkURL.equals("about:blank")) { //$NON-NLS-1$
			String title = request.getParameter("title"); //$NON-NLS-1$
			if (title == null) {
				return;
			}
			Preferences prefs = HelpBasePlugin.getDefault()
					.getPluginPreferences();
			String bookmarks = prefs.getString(BaseHelpSystem.BOOKMARKS);
			String removeString = "," + encode(bookmarkURL) + "|" + encode(title); //$NON-NLS-1$ //$NON-NLS-2$
			int i = bookmarks.indexOf(removeString);
			if (i == -1)
				return;
			bookmarks = bookmarks.substring(0, i)
					+ bookmarks.substring(i + removeString.length());
			prefs.setValue(BaseHelpSystem.BOOKMARKS, bookmarks);
			HelpBasePlugin.getDefault().savePluginPreferences();
		}
	}

	public void removeAllBookmarks() {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		prefs.setValue(BaseHelpSystem.BOOKMARKS, ""); //$NON-NLS-1$
		HelpBasePlugin.getDefault().savePluginPreferences();
	}

	public Topic[] getBookmarks() {
		// sanity test for infocenter, but this could not work anyway...
		if (BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER) {
			// this is workbench
			Preferences prefs = HelpBasePlugin.getDefault()
					.getPluginPreferences();
			String bookmarks = prefs.getString(BaseHelpSystem.BOOKMARKS);
			StringTokenizer tokenizer = new StringTokenizer(bookmarks, ","); //$NON-NLS-1$
			Topic[] topics = new Topic[tokenizer.countTokens()];
			for (int i = 0; tokenizer.hasMoreTokens(); i++) {
				String bookmark = tokenizer.nextToken();
				// url and title are separated by vertical bar
				int separator = bookmark.indexOf('|');

				String label = decode(bookmark.substring(separator + 1));
				String href = separator < 0 ? "" //$NON-NLS-1$
						: decode(bookmark.substring(0, separator));
				topics[i] = new Topic(label, href);
			}
			return topics;
		}
		return new Topic[0];
	}

	private int getOperation() {
		String op = request.getParameter("operation"); //$NON-NLS-1$
		if ("add".equals(op)) //$NON-NLS-1$
			return ADD;
		else if ("remove".equals(op)) //$NON-NLS-1$
			return REMOVE;
		else if ("removeAll".equals(op)) //$NON-NLS-1$
			return REMOVE_ALL;
		else
			return NONE;
	}
	/**
	 * Ensures that string does not contains ',' or '|' characters.
	 * 
	 * @param s
	 * @return String
	 */
	private static String encode(String s) {
		s = TString.change(s, "\\", "\\escape"); //$NON-NLS-1$ //$NON-NLS-2$
		s = TString.change(s, ",", "\\comma"); //$NON-NLS-1$ //$NON-NLS-2$
		return TString.change(s, "|", "\\pipe"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	private static String decode(String s) {
		s = TString.change(s, "\\pipe", "|"); //$NON-NLS-1$ //$NON-NLS-2$
		s = TString.change(s, "\\comma", ","); //$NON-NLS-1$ //$NON-NLS-2$
		return TString.change(s, "\\escape", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
