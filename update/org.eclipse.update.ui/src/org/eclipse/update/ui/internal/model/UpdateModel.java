package org.eclipse.update.ui.internal.model;

import java.util.*;
import org.eclipse.update.core.*;


public class UpdateModel {
	private Vector jobs = new Vector();
	private Vector bookmarks = new Vector();
	private Vector listeners = new Vector();
	
	public UpdateModel() {
	}
	
	public void startup() {
	}
	
	public void shutdown() {
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