/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.progress.FinishedJobs.KeptJobsListener;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The ProgressViewerContentProvider is the content provider progress viewers.
 */
public class ProgressViewerContentProvider extends ProgressContentProvider {
	protected AbstractProgressViewer progressViewer;

	private KeptJobsListener keptJobListener;

	private Set keptJobs = new HashSet();

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param structured
	 *            The Viewer we are providing content for
	 * @param debug
	 *           If true debug information will be shown
     * 			 if the debug flag in the ProgressManager is true.
	 * @param showFinished
	 *            A boolean that indicates whether or not the finished jobs
	 *            should be shown.
	 */
	public ProgressViewerContentProvider(AbstractProgressViewer structured,
			boolean debug, boolean showFinished) {
		super(debug);
		progressViewer = structured;
		if (showFinished) {
			FinishedJobs.getInstance().addListener(getKeptJobListener());
		}
	}

	/**
	 * Return a listener for kept jobs.
	 * 
	 * @return KeptJobsListener
	 */
	private KeptJobsListener getKeptJobListener() {
		keptJobListener = new KeptJobsListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.progress.FinishedJobs.KeptJobsListener#finished(org.eclipse.ui.internal.progress.JobTreeElement)
			 */
			public void finished(JobTreeElement jte) {
				keptJobs.add(jte);
				final JobTreeElement element = jte;
				Job updateJob = new WorkbenchJob("Refresh finished") {//$NON-NLS-1$
					/* (non-Javadoc)
					 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
					 */
					public IStatus runInUIThread(IProgressMonitor monitor) {
						refresh(new Object[] { element });
						return Status.OK_STATUS;
					}
				};
				updateJob.setSystem(true);
				updateJob.schedule();

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.progress.FinishedJobs.KeptJobsListener#removed(org.eclipse.ui.internal.progress.JobTreeElement)
			 */
			public void removed(JobTreeElement jte) {
				//null indicates they are all removed
				if(jte == null) {
					keptJobs.clear();
				} else {
					keptJobs.remove(jte);
				}
				final JobTreeElement element = jte;
				Job updateJob = new WorkbenchJob("Remove finished") {//$NON-NLS-1$
					/* (non-Javadoc)
					 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
					 */
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if(element == null) {
							refresh();
						} else {
							ProgressViewerContentProvider.this.remove(new Object[] { element });
						}
						return Status.OK_STATUS;
					}
				};
				updateJob.setSystem(true);
				updateJob.schedule();

			}

		};
		return keptJobListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh()
	 */
	public void refresh() {
		progressViewer.refresh(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IProgressUpdateCollector#refresh(org.eclipse.ui.internal.progress.JobTreeElement[])
	 */
	public void refresh(Object[] elements) {
		Object[] refreshes = getRoots(elements, true);
		for (int i = 0; i < refreshes.length; i++) {
			progressViewer.refresh(refreshes[i], true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		Object[] elements = super.getElements(inputElement);

		if (keptJobs.size() == 0) {
			return elements;
		}
		if (elements.length == 0) {
			return keptJobs.toArray();
		}

		Set all = new HashSet();
		
		for (int i = 0; i < elements.length; i++) {
			Object element = elements[i];
			all.add(element);
			if(keptJobs.contains(element)) {
				keptJobs.remove(element);
			}

		}
		
		Iterator keptIterator = keptJobs.iterator();
		while(keptIterator.hasNext()){
			JobInfo next = (JobInfo) keptIterator.next();
			GroupInfo group = next.getGroupInfo();
			if(group == null) 
				all.add(next);
			else 
				all.add(group);
		}
		return all.toArray();
	}

	/**
	 * Get the root elements of the passed elements as we only show roots.
	 * Replace the element with its parent if subWithParent is true
	 * 
	 * @param elements
	 *            the array of elements.
	 * @param subWithParent
	 *            sub with parent flag.
	 * @return Object[]
	 */
	private Object[] getRoots(Object[] elements, boolean subWithParent) {
		if (elements.length == 0) {
			return elements;
		}
		HashSet roots = new HashSet();
		for (int i = 0; i < elements.length; i++) {
			JobTreeElement element = (JobTreeElement) elements[i];
			if (element.isJobInfo()) {
				GroupInfo group = ((JobInfo) element).getGroupInfo();
				if (group == null) {
					roots.add(element);
				} else {
					if (subWithParent) {
						roots.add(group);
					}
				}
			} else {
				roots.add(element);
			}
		}
		return roots.toArray();
	}

	public void add(Object[] elements) {
		progressViewer.add(elements);

	}

	public void remove(Object[] elements) {
		progressViewer.remove(elements);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (keptJobListener != null) {
			FinishedJobs.getInstance().removeListener(keptJobListener);
		}
	}
}
