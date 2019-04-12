/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.Iterator;
import java.util.LinkedHashSet;
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

	private boolean showFinished;

	/** flag if we need a full refresh */
	private boolean refreshNeeded;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param structured   The Viewer we are providing content for
	 * @param debug        If true debug information will be shown if the debug flag
	 *                     in the ProgressManager is true.
	 * @param showFinished A boolean that indicates whether or not the finished jobs
	 *                     should be shown.
	 */
	public ProgressViewerContentProvider(AbstractProgressViewer structured, boolean debug, boolean showFinished) {
		super(debug);
		progressViewer = structured;
		this.showFinished = showFinished;
		if (showFinished) {
			keptJobListener = new FinishedJobsListener();
		}
		startListening();
	}

	public void stopListening() {
		ProgressViewUpdater.getSingleton().removeCollector(this);
		if (keptJobListener != null) {
			FinishedJobs.getInstance().removeListener(keptJobListener);
		}
		refreshNeeded = true;
	}

	public void startListening() {
		ProgressViewUpdater.getSingleton().addCollector(this);
		if (keptJobListener != null) {
			FinishedJobs.getInstance().addListener(keptJobListener);
		}
		if (refreshNeeded) {
			refreshNeeded = false;
			refresh();
		}
	}

	class FinishedJobsListener implements KeptJobsListener {

		@Override
		public void finished(JobTreeElement jte) {
			final JobTreeElement element = jte;
			Job updateJob = new WorkbenchJob("Refresh finished") {//$NON-NLS-1$
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					refresh(element);
					return Status.OK_STATUS;
				}

				@Override
				public boolean shouldSchedule() {
					return !progressViewer.getControl().isDisposed();
				}

				@Override
				public boolean shouldRun() {
					return !progressViewer.getControl().isDisposed();
				}
			};
			updateJob.setSystem(true);
			updateJob.schedule();
		}

		@Override
		public void removed(JobTreeElement jte) {
			final JobTreeElement element = jte;
			Job updateJob = new WorkbenchJob("Remove finished") {//$NON-NLS-1$
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (element == null) {
						refresh();
					} else {
						remove(element);
					}
					return Status.OK_STATUS;
				}
			};
			updateJob.setSystem(true);
			updateJob.schedule();
		}
	}

	@Override
	public void refresh() {
		progressViewer.refresh(true);
	}

	@Override
	public void refresh(JobTreeElement... elements) {
		for (Object refresh : getRoots(elements, true)) {
			progressViewer.refresh(refresh, true);
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		Object[] elements = super.getElements(inputElement);

		if (!showFinished) {
			return elements;
		}

		Set<JobTreeElement> kept = FinishedJobs.getInstance().getKeptAsSet();

		if (kept.isEmpty()) {
			return elements;
		}

		Set<Object> all = new LinkedHashSet<>();

		for (Object element : elements) {
			all.add(element);
		}
		Iterator<JobTreeElement> keptIterator = kept.iterator();
		while (keptIterator.hasNext()) {
			JobTreeElement next = keptIterator.next();
			if (next.getParent() != null && all.contains(next.getParent())) {
				continue;
			}
			all.add(next);
		}
		return all.toArray();
	}

	/**
	 * Get the root elements of the passed elements as we only show roots. Replace
	 * the element with its parent if subWithParent is true
	 *
	 * @param elements      the array of elements.
	 * @param subWithParent sub with parent flag.
	 * @return JobTreeElement[]
	 */
	private JobTreeElement[] getRoots(JobTreeElement[] elements, boolean subWithParent) {
		if (elements.length == 0) {
			return elements;
		}
		Set<JobTreeElement> roots = new LinkedHashSet<>();
		for (Object element : elements) {
			JobTreeElement jobTreeElement = (JobTreeElement) element;
			if (jobTreeElement.isJobInfo()) {
				GroupInfo group = ((JobInfo) jobTreeElement).getGroupInfo();
				if (group == null) {
					roots.add(jobTreeElement);
				} else {
					if (subWithParent) {
						roots.add(group);
					}
				}
			} else {
				roots.add(jobTreeElement);
			}
		}
		return roots.toArray(new JobTreeElement[0]);
	}

	@Override
	public void add(JobTreeElement... elements) {
		progressViewer.add(elements);
	}

	@Override
	public void remove(JobTreeElement... elements) {
		progressViewer.remove(elements);
	}

	@Override
	public void dispose() {
		stopListening();
		super.dispose();
	}
}
