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
package org.eclipse.team.internal.ui.jobs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.widgets.Display;

/**
 * Top level category in the job view
 */
class JobStateCategory implements IJobChangeListener {
	AbstractTreeViewer tree;
	List children = new ArrayList();
	public String name;
	public int state;
	public Display display;
	
	public boolean equals(Object other) {
		if(this == other) return true;
		if(other instanceof JobStateCategory) {
			return ((JobStateCategory)other).name.equals(name);
		}
		return false;
	}

	JobStateCategory(AbstractTreeViewer tree, String name, int state) {
		this.tree = tree;
		this.name = name;
		this.state = state;
		this.display = tree.getControl().getShell().getDisplay();
		Platform.getJobManager().addJobChangeListener(this);
	}
	
	public void add(final Object child) {
		display.asyncExec(new Runnable() {
			public void run() {
				if(! children.contains(child)) {
					children.add(child);
					tree.add(JobStateCategory.this, child);
					tree.expandAll();
				}
			}
		});
	}
	
	public void remove(final Object child) {
		display.asyncExec(new Runnable() {
			public void run() {		
				if(children.contains(child)) {
					children.remove(child);
					tree.remove(child);
					tree.expandAll();
				}
			}
		});
	}
	
	public Object[] getChildren() {
		return (Object[]) children.toArray(new Object[children.size()]);
	}
	
	public void dispose() {
		Platform.getJobManager().removeJobChangeListener(this);
	}
	
	public void aboutToRun(IJobChangeEvent event) {
		handleJobChange(event.getJob());
	}
	
	public void awake(IJobChangeEvent event) {
		handleJobChange(event.getJob());
	}
	
	public void done(IJobChangeEvent event) {
		handleJobChange(new JobDoneElement(event.getJob(), event.getResult()));
	}
	
	public void running(IJobChangeEvent event) {
		handleJobChange(event.getJob());
	}
	
	public void scheduled(IJobChangeEvent event) {
		handleJobChange(event.getJob());
	}
	
	public void sleeping(IJobChangeEvent event) {
		handleJobChange(event.getJob());
	}
	
	synchronized protected void handleJobChange(Object object) {
		int s = Job.NONE;
		Job job = null;
		if(object instanceof Job) {
			s = ((Job)object).getState();
			job = ((Job)object);
		} else if(object instanceof JobDoneElement) {
			job = ((JobDoneElement)object).job;
		}
		
		if(s == state) {
			add(object);
		} else {
			remove(job);
		}
	}
	
	public void removeAll() {
		tree.remove(getChildren());
		children.clear();
	}
}