package org.eclipse.update.internal.ui.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;
import org.eclipse.update.core.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.update.internal.ui.*;
import java.net.*;

public class UpdateModel {
	private Vector changes = new Vector();
	private Vector bookmarks = new Vector();
	private Vector listeners = new Vector();
	private IDialogSettings settings;
	private static final String KEY_BOOKMARK_NAMES = "bookmark.names"; 
	private static final String KEY_BOOKMARK_URLS = "bookmark.urls";
	private AvailableUpdates availableUpdates;
	
	public UpdateModel() {
		settings = UpdateUIPlugin.getDefault().getDialogSettings();
	}
	
	public void startup() {
		// load bookmarks
		String [] names = settings.getArray(KEY_BOOKMARK_NAMES);
		String [] urls = settings.getArray(KEY_BOOKMARK_URLS);
		if (names==null || urls == null) return;
		for (int i=0; i<names.length; i++) {
			String name = names[i];
			String urlName = urls[i];
			try {
				URL url = new URL(urlName);
				SiteBookmark bookmark = new SiteBookmark(name, url);
				bookmark.setModel(this);
				bookmarks.add(bookmark);
			}
			catch (MalformedURLException e) {
			}
		}
	}
	
	public void shutdown() {
		// save bookmarks
		String [] names = null;
		String [] urls = null;
		
		if (bookmarks.size()>0) {
			names = new String [bookmarks.size()];
			urls = new String [bookmarks.size()];
			for (int i=0; i<bookmarks.size(); i++) {
				SiteBookmark bookmark = (SiteBookmark)bookmarks.get(i);
				names[i] = bookmark.getName();
				urls[i] = bookmark.getURL().toString();
			}
		}
		else {
			names = new String [0];
			urls = new String [0];
		}
		settings.put(KEY_BOOKMARK_NAMES, names);
		settings.put(KEY_BOOKMARK_URLS, urls);
	}


	public PendingChange [] getPendingChanges() {
		return (PendingChange[])
			changes.toArray(new PendingChange[changes.size()]);
	}
	
	public PendingChange [] getPendingChanges(int type) {
		Vector v = new Vector();
		for (int i=0; i<changes.size(); i++) {
			PendingChange job = (PendingChange)changes.elementAt(i);
			if (job.getJobType() == type)
			   v.add(job);
		}
		return (PendingChange[])
			v.toArray(new PendingChange[v.size()]);
	}
	
	public PendingChange findPendingChange(IFeature feature) {
		for (int i=0; i<changes.size(); i++) {
			PendingChange job = (PendingChange)changes.elementAt(i);
			if (job.getFeature().equals(feature))
			   return job;
		}
		return null;
	}
			
	
	public boolean isPending(IFeature feature) {
		return findPendingChange(feature)!=null;
	}
	
	public void addPendingChange(PendingChange change) {
		changes.add(change);
		change.setModel(this);
		fireObjectAdded(this, change);
	}
	
	public void removePendingChange(PendingChange change) {
		changes.remove(change);
		change.setModel(null);
		fireObjectRemoved(this, change);
	}
	
	public void removePendingChange(IFeature scheduledFeature) {
		PendingChange change = findPendingChange(scheduledFeature);
		if (change!=null)
		   removePendingChange(change);
	}	

	public void addBookmark(SiteBookmark bookmark) {
		bookmarks.add(bookmark);
		bookmark.setModel(this);
		fireObjectAdded(null, bookmark);
	}
	
	public void removeBookmark(SiteBookmark bookmark) {
		bookmarks.remove(bookmark);
		bookmark.setModel(null);
		fireObjectRemoved(null, bookmark);
	}
	
	public SiteBookmark [] getBookmarks() {
		return (SiteBookmark[])bookmarks.toArray(new SiteBookmark[bookmarks.size()]);
	}
	
	public void addUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (!listeners.contains(listener)) 
		   listeners.add(listener);
	}


	public void removeUpdateModelChangedListener(IUpdateModelChangedListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}
	
	void fireObjectAdded(Object parent, Object child) {
		for (Iterator iter=listeners.iterator();
				iter.hasNext();) {
			IUpdateModelChangedListener listener = (IUpdateModelChangedListener)iter.next();
			listener.objectAdded(parent, child);
		}
	}


	void fireObjectRemoved(Object parent, Object child) {
		for (Iterator iter=listeners.iterator();
				iter.hasNext();) {
			IUpdateModelChangedListener listener = (IUpdateModelChangedListener)iter.next();
			listener.objectRemoved(parent, child);
		}
	}
	
	public void fireObjectChanged(Object object, String property) {
		for (Iterator iter=listeners.iterator();
			iter.hasNext();) {
			IUpdateModelChangedListener listener = (IUpdateModelChangedListener)iter.next();
			listener.objectChanged(object, property);
		}
	}
	
	public AvailableUpdates getUpdates() {
		if (availableUpdates==null) {
		   availableUpdates = new AvailableUpdates();
		   availableUpdates.setModel(this);
		}
		return availableUpdates;
	}
}