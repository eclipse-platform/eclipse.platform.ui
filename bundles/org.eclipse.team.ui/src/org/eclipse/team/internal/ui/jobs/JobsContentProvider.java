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

import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for jobs
 */
class JobsContentProvider implements ITreeContentProvider {		

	private final JobsView view;
	private AbstractTreeViewer viewer;
	
	private JobStateCategory Sleeping; 
	private JobStateCategory Running;
	private JobStateCategory Waiting;
	private JobStateCategory Done;

	JobsContentProvider(JobsView view) {
		this.view = view;
	}

	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof IJobManager) {
			return new Object[] {Waiting, Sleeping, Running, Done}; 
		}
		if(inputElement instanceof JobStateCategory) {
			return ((JobStateCategory)inputElement).getChildren(); 
		}
		return new Object[0];
	}

	public void dispose() {
		Sleeping.dispose();
		Running.dispose();
		Waiting.dispose();
		Done.dispose();
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (AbstractTreeViewer)viewer;
		IJobManager oldManager = null;
		IJobManager newManager = null;
		if (oldInput instanceof IJobManager) {
			oldManager = (IJobManager) oldInput;
		}
		if (newInput instanceof IJobManager) {
			newManager = (IJobManager) newInput;
		}
		if (oldManager != newManager) {
			if (oldManager != null) {
			}
			if (newManager != null) {
				Sleeping = new JobStateCategory((AbstractTreeViewer)viewer, "Sleeping", Job.SLEEPING); 
				Running = new JobStateCategory((AbstractTreeViewer)viewer, "Running", Job.RUNNING);
				Waiting = new JobStateCategory((AbstractTreeViewer)viewer, "Waiting", Job.WAITING);
				Done = new JobStateCategory((AbstractTreeViewer)viewer, "Done", Job.NONE);				
			}
		}			
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}
}