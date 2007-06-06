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
package org.eclipse.update.internal.ui.model;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configurator.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.operations.*;

public class UpdateModel implements IAdaptable {
	private Vector bookmarks = new Vector();
	private Vector listeners = new Vector();
	private static final String BOOKMARK_FILE = "bookmarks.xml"; //$NON-NLS-1$
	
	public UpdateModel() {
		reset();
	}
	
	public void reset() {
		// load bookmarks
		bookmarks.clear();
		BookmarkUtil.parse(getBookmarksFileName(), bookmarks);
	}
	
	private String getBookmarksFileName() {
		URL platformXML = ConfiguratorUtils.getCurrentPlatformConfiguration().getConfigurationLocation();
		if (!"file".equals(platformXML.getProtocol())) { //$NON-NLS-1$
			IPath path = UpdateUI.getDefault().getStateLocation();
			path = path.append(BOOKMARK_FILE);
			return path.toOSString();
		} else {
			File f = new File(platformXML.getFile());
			f = new File(f.getParentFile(), BOOKMARK_FILE);
			return f.getAbsolutePath();
		}
	}
	
	public void shutdown() {
		saveBookmarks();
	}
	
	public void saveBookmarks() {
		BookmarkUtil.store(getBookmarksFileName(), bookmarks);
	}
	
	
	public void addBookmark(NamedModelObject bookmark) {
		bookmarks.add(bookmark);
		bookmark.setModel(this);
		fireObjectsAdded(null, new Object []{bookmark});
	}
	
	public void removeBookmark(NamedModelObject bookmark) {
		bookmarks.remove(bookmark);
		bookmark.setModel(null);
		fireObjectsRemoved(null, new Object []{bookmark});
	}
	
	public NamedModelObject [] getBookmarks() {
		return (NamedModelObject[])bookmarks.toArray(new NamedModelObject[bookmarks.size()]);
	}
	
	public SiteBookmark [] getBookmarkLeafs() {
		return BookmarkUtil.getBookmarks(bookmarks);
	}
	
	public BookmarkFolder getFolder(IPath path) {
		return BookmarkUtil.getFolder(bookmarks, path);
	}
	
	public void addUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (!listeners.contains(listener)) 
		   listeners.add(listener);
	}


	public void removeUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
	
	public void fireObjectsAdded(Object parent, Object [] children) {
		for (Iterator iter=listeners.iterator();
				iter.hasNext();) {
			IUpdateModelChangedListener listener = (IUpdateModelChangedListener)iter.next();
			listener.objectsAdded(parent, children);
		}
	}


	public void fireObjectsRemoved(Object parent, Object [] children) {
		for (Iterator iter=listeners.iterator();
				iter.hasNext();) {
			IUpdateModelChangedListener listener = (IUpdateModelChangedListener)iter.next();
			listener.objectsRemoved(parent, children);
		}
	}
	
	public void fireObjectChanged(Object object, String property) {
		for (Iterator iter=listeners.iterator();
			iter.hasNext();) {
			IUpdateModelChangedListener listener = (IUpdateModelChangedListener)iter.next();
			listener.objectChanged(object, property);
		}
	}
	public Object getAdapter(Class key) {
		return null;
	}
}
