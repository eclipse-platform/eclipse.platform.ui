package org.eclipse.help.servlet.data;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.*;

/**
 * This class calls eclipse API's directly, so it should only be
 * instantiated in the workbench scenario, not in the infocenter.
 */
public class BookmarksData extends RequestData {

	public BookmarksData(ServletContext context, HttpServletRequest request) {
		super(context, request);

		// see if anything is to be added
		addBookmark();
		// see if anything is to be removd
		removeBookmark();
	}

	public void addBookmark() {
		String bookmarkURL = request.getParameter("add");
		if (bookmarkURL != null && bookmarkURL.length() > 0) {
			String title = request.getParameter("title");
			Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
			String bookmarks = prefs.getString(HelpSystem.BOOKMARKS);
			
			// separate the url and title by vertical bar
			
			// check for duplicates
			if (bookmarks.indexOf(","+bookmarkURL + "|") != -1)
				return;
			bookmarks = bookmarks + "," + bookmarkURL + "|" + title;
			prefs.setValue(HelpSystem.BOOKMARKS, bookmarks);
			HelpPlugin.getDefault().savePluginPreferences();
		}
	}

	public void removeBookmark() {
		String bookmarkURL = request.getParameter("remove");
		if (bookmarkURL != null && bookmarkURL.length() > 0) {
			String title = request.getParameter("title");
			Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
			String bookmarks = prefs.getString(HelpSystem.BOOKMARKS);
			String removeString = "," + bookmarkURL + "|" + title;
			int i = bookmarks.indexOf(removeString);
			if (i == -1)
				return;
			bookmarks =
				bookmarks.substring(0, i)
					+ bookmarks.substring(i + removeString.length());
			prefs.setValue(HelpSystem.BOOKMARKS, bookmarks);
			HelpPlugin.getDefault().savePluginPreferences();
		}
	}

	public Topic[] getBookmarks() {
		// sanity test for infocenter, but this could not work anyway...
		if (HelpSystem.getMode()!=HelpSystem.MODE_INFOCENTER) {
			// this is workbench
			Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
			String bookmarks = prefs.getString(HelpSystem.BOOKMARKS);
			StringTokenizer tokenizer = new StringTokenizer(bookmarks, ",");
			Topic[] topics = new Topic[tokenizer.countTokens()];
			for (int i = 0; tokenizer.hasMoreTokens(); i++) {
				String bookmark = tokenizer.nextToken();
				// url and title are separated by vertical bar
				int separator = bookmark.indexOf('|');

				String label = bookmark.substring(separator + 1);
				String href =
					separator < 0 ? "" : bookmark.substring(0, separator);
				topics[i] = new Topic(label, href);
			}
			return topics;
		}
		return new Topic[0];
	}
}
