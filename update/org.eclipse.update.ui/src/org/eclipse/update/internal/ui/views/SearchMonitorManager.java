/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.views;

import java.util.*;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.model.UpdateModel;
import org.eclipse.update.internal.ui.search.SearchObject;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SearchMonitorManager {
	Hashtable monitors;

	class SearchMonitor implements IProgressMonitor {
		private SearchObject sobj;
		private int totalWork;
		private double worked;
		private boolean active;

		public SearchMonitor(SearchObject sobj) {
			this.sobj = sobj;
			sobj.attachProgressMonitor(this);
		}

		public void dispose() {
			sobj.detachProgressMonitor(this);
		}
		/**
		 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
		 */
		public void beginTask(String name, int totalWork) {
			this.totalWork = totalWork;
			worked = 0;
			active = true;
			update();
		}

		/**
		 * @see org.eclipse.core.runtime.IProgressMonitor#done()
		 */
		public void done() {
			active = false;
			update();
		}

		private void update() {
			UpdateUI.getDefault().getUpdateModel().fireObjectChanged(
				sobj,
				NamedModelObject.P_NAME);
		}

		public String getLabel() {
			if (active) {
				//int perc = (worked * 100) / totalWork;
				int perc = (int)(worked * 100)/totalWork;
				return sobj.getName() + " - " + perc + "%";
			} else {
				return sobj.getName();
			}
		}

		/**
		 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
		 */
		public void internalWorked(double work) {
			worked += work;
			update();
		}

		/**
		 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
		 */
		public boolean isCanceled() {
			return false;
		}

		/**
		 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
		 */
		public void setCanceled(boolean value) {
		}

		/**
		 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
		 */
		public void setTaskName(String name) {
		}

		/**
		 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
		 */
		public void subTask(String name) {
		}

		/**
		 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
		 */
		public void worked(int work) {
			internalWorked(work);
		}

	}
	/**
	 * Constructor for SearchMonitorManager.
	 */
	public SearchMonitorManager() {
		monitors = new Hashtable();
		initialize();
	}
	
	public void shutdown() {
		for (Enumeration enum=monitors.elements(); enum.hasMoreElements();) {
			SearchMonitor monitor = (SearchMonitor)enum.nextElement();
			monitor.dispose();
		}
		monitors.clear();
	}
	
	private void initialize() {
		UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		NamedModelObject [] bookmarks = model.getBookmarks();
		for (int i=0; i<bookmarks.length; i++) {
			processBookmark(bookmarks[i]);
		}
	}
	
	private void processFolder(BookmarkFolder folder) {
		Object [] children = folder.getChildren(null);
		for (int i=0; i<children.length; i++) {
			processBookmark((NamedModelObject)children[i]);
		}
	}
	
	private void processBookmark(NamedModelObject obj) {
		if (obj instanceof SearchObject) {
			register((SearchObject)obj);
		}
		else if (obj instanceof BookmarkFolder) {
			processFolder((BookmarkFolder)obj);
		}
	}
		

	public void register(SearchObject obj) {
		if (monitors.get(obj) == null)
			monitors.put(obj, new SearchMonitor(obj));
	}

	public void unregister(SearchObject obj) {
		SearchMonitor monitor = (SearchMonitor) monitors.get(obj);
		if (monitor != null) {
			monitor.dispose();
			monitors.remove(obj);
		}
	}

	public String getLabel(SearchObject obj) {
		SearchMonitor monitor = (SearchMonitor) monitors.get(obj);
		if (monitor == null)
			return obj.getName();
		else
			return monitor.getLabel();
	}
}