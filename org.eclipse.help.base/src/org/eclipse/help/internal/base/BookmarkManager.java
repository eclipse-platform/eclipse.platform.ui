/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.base;

import java.util.*;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.base.util.TString;

/**
 * Code for bookmark management has been moved here so that it can be shared
 * between the web app and the help view. The manager implements Observable so
 * that views can be notified on bookmark changes. The webapp does not use this
 * feature.
 * 
 * @since 3.1
 */
public class BookmarkManager extends Observable implements
		Preferences.IPropertyChangeListener {
	// all bookmarks removed
	public static final int REMOVE_ALL = 1;

	// bookmark added
	public static final int ADD = 2;

	// bookmark removed
	public static final int REMOVE = 3;

	// bookmark changed
	public static final int CHANGE = 4;

	// everything changed (by the webapp)
	public static final int WORLD_CHANGED = 5;

	private boolean ignoreNotification;
	
	private ArrayList bookmarks;

	public static class Bookmark implements IHelpResource {
		private String label;

		private String href;

		public Bookmark(String label, String href) {
			this.label = label;
			this.href = href;
		}

		public String getHref() {
			return href;
		}

		public String getLabel() {
			return label;
		}

		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (object == this)
				return true;
			if (object instanceof Bookmark) {
				Bookmark b = (Bookmark) object;
				return b.href.equals(href) && b.label.equals(label);
			}
			return false;
		}
	}

	public static class BookmarkEvent {
		private int type;

		private Bookmark bookmark;

		public BookmarkEvent(int type, Bookmark bookmark) {
			this.type = type;
			this.bookmark = bookmark;
		}

		public int getType() {
			return type;
		}

		public Bookmark getBookmark() {
			return bookmark;
		}
	}

	public BookmarkManager() {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		prefs.addPropertyChangeListener(this);
	}

	public void close() {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		prefs.removePropertyChangeListener(this);
	}

	public void addBookmark(String bookmarkURL, String title) {
		if (bookmarkURL != null && bookmarkURL.length() > 0
				&& !bookmarkURL.equals("about:blank")) { //$NON-NLS-1$
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
			ignoreNotification = true;
			prefs.setValue(BaseHelpSystem.BOOKMARKS, bookmarks);
			HelpBasePlugin.getDefault().savePluginPreferences();
			Bookmark bookmark = new Bookmark(title, bookmarkURL);
			if (this.bookmarks!=null)
				this.bookmarks.add(bookmark);
			setChanged();
			notifyObservers(new BookmarkEvent(ADD, bookmark));
			ignoreNotification = false;
		}
	}

	public void removeBookmark(String bookmarkURL, String title) {
		removeBookmark(new Bookmark(title, bookmarkURL));
	}

	public void removeBookmark(Bookmark bookmark) {
		String bookmarkURL = bookmark.getHref();
		String title = bookmark.getLabel();
		if (bookmarkURL != null && bookmarkURL.length() > 0
				&& !bookmarkURL.equals("about:blank")) { //$NON-NLS-1$
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
			ignoreNotification = true;
			prefs.setValue(BaseHelpSystem.BOOKMARKS, bookmarks);
			HelpBasePlugin.getDefault().savePluginPreferences();
			if (this.bookmarks!=null)
				this.bookmarks.remove(bookmark);
			setChanged();
			notifyObservers(new BookmarkEvent(REMOVE, bookmark));
			ignoreNotification = false;
		}
	}

	public void removeAllBookmarks() {
		Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
		ignoreNotification = true;
		prefs.setValue(BaseHelpSystem.BOOKMARKS, ""); //$NON-NLS-1$
		HelpBasePlugin.getDefault().savePluginPreferences();
		if (bookmarks!=null)
			bookmarks.clear();
		setChanged();
		notifyObservers(new BookmarkEvent(REMOVE_ALL, null));
		ignoreNotification = false;
	}

	public IHelpResource[] getBookmarks() {
		if (bookmarks==null) {
			Preferences prefs = HelpBasePlugin.getDefault().getPluginPreferences();
			String value = prefs.getString(BaseHelpSystem.BOOKMARKS);
			StringTokenizer tokenizer = new StringTokenizer(value, ","); //$NON-NLS-1$
			bookmarks = new ArrayList();
			for (int i = 0; tokenizer.hasMoreTokens(); i++) {
				String bookmark = tokenizer.nextToken();
				// url and title are separated by vertical bar
				int separator = bookmark.indexOf('|');
				String label = decode(bookmark.substring(separator + 1));
				String href = separator < 0 ? "" //$NON-NLS-1$
					: decode(bookmark.substring(0, separator));
				bookmarks.add(new Bookmark(label, href));
			}
		}
		return (IHelpResource[])bookmarks.toArray(new IHelpResource[bookmarks.size()]);
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

	public void propertyChange(PropertyChangeEvent event) {
		if (ignoreNotification)
			return;
		if (event.getProperty().equals(BaseHelpSystem.BOOKMARKS)) {
			// notify observers about the changes in the preferences
			// made elsewhere (by the webapp)
			// Also clear the cache.
			bookmarks=null;
			setChanged();
			notifyObservers(new BookmarkEvent(WORLD_CHANGED, null));
		}
	}
}
