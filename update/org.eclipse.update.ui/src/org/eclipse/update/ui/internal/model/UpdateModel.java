package org.eclipse.update.ui.internal.model;

import java.util.*;
import org.eclipse.update.core.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.update.internal.ui.*;
import java.net.*;

public class UpdateModel {
	private Vector jobs = new Vector();
	private Vector bookmarks = new Vector();
	private Vector listeners = new Vector();
	private IDialogSettings settings;
	private static final String KEY_BOOKMARK_NAMES = "bookmark.names"; 
	private static final String KEY_BOOKMARK_URLS = "bookmark.urls";
	
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
		settings.put(KEY_BOOKMARK_NAMES, names);
		settings.put(KEY_BOOKMARK_URLS, urls);
	}

	public ChecklistJob [] getJobs() {
		return (ChecklistJob[])
			jobs.toArray(new ChecklistJob[jobs.size()]);
	}
	
	public ChecklistJob [] getJobs(int type) {
		Vector v = new Vector();
		for (int i=0; i<jobs.size(); i++) {
			ChecklistJob job = (ChecklistJob)jobs.elementAt(i);
			if (job.getJobType() == type)
			   v.add(job);
		}
		return (ChecklistJob[])
			v.toArray(new ChecklistJob[v.size()]);
	}
	
	public ChecklistJob findJob(IFeature feature) {
		for (int i=0; i<jobs.size(); i++) {
			ChecklistJob job = (ChecklistJob)jobs.elementAt(i);
			if (job.getFeature().equals(feature))
			   return job;
		}
		return null;
	}
			
	
	public boolean checklistContains(IFeature feature) {
		return findJob(feature)!=null;
	}
	
	public void addJob(ChecklistJob job) {
		jobs.add(job);
		job.setModel(this);
		fireObjectAdded(null, job);
	}
	
	public void removeJob(ChecklistJob job) {
		jobs.remove(job);
		job.setModel(null);
		fireObjectRemoved(null, job);
	}
	
	public void removeJob(IFeature scheduledFeature) {
		ChecklistJob job = findJob(scheduledFeature);
		if (job!=null)
		   removeJob(job);
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
	
	private void fireObjectAdded(Object parent, Object child) {
		for (Iterator iter=listeners.iterator();
				iter.hasNext();) {
			IUpdateModelChangedListener listener = (IUpdateModelChangedListener)iter.next();
			listener.objectAdded(parent, child);
		}
	}

	private void fireObjectRemoved(Object parent, Object child) {
		for (Iterator iter=listeners.iterator();
				iter.hasNext();) {
			IUpdateModelChangedListener listener = (IUpdateModelChangedListener)iter.next();
			listener.objectRemoved(parent, child);
		}
	}
	
	void fireObjectChanged(Object object, String property) {
		for (Iterator iter=listeners.iterator();
			iter.hasNext();) {
			IUpdateModelChangedListener listener = (IUpdateModelChangedListener)iter.next();
			listener.objectChanged(object, property);
		}
	}
}