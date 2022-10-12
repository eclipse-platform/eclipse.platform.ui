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
package org.eclipse.e4.ui.progress.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.progress.UIJob;
import org.eclipse.e4.ui.progress.internal.FinishedJobs.KeptJobsListener;

/**
 * The ProgressViewerContentProvider is the content provider progress viewers.
 */
public class ProgressViewerContentProvider extends ProgressContentProvider {
	/** Viewer to show content. */
	protected AbstractProgressViewer progressViewer;

	private KeptJobsListener keptJobListener;

	private boolean showFinished;

	private FinishedJobs finishedJobs;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param structured      The Viewer we are providing content for
	 * @param finishedJobs    The singleton to store finished jobs which should kept
	 * @param viewUpdater     The singleton to perform viewer updates
	 * @param progressManager helper to manage progress
	 * @param debug           If true debug information will be shown if the debug
	 *                        flag in the ProgressManager is true.
	 * @param showFinished    A boolean that indicates whether or not the finished
	 *                        jobs should be shown.
	 */
	public ProgressViewerContentProvider(AbstractProgressViewer structured,
			FinishedJobs finishedJobs, ProgressViewUpdater viewUpdater,
			ProgressManager progressManager, boolean debug, boolean showFinished) {
		super(viewUpdater, progressManager, debug);
		progressViewer = structured;
		this.finishedJobs = finishedJobs;
		this.showFinished = showFinished;
		if (showFinished) {
			finishedJobs.addListener(getKeptJobListener());
		}
	}

	/**
	 * Return a listener for kept jobs.
	 *
	 * @return KeptJobsListener
	 */
	private KeptJobsListener getKeptJobListener() {
		keptJobListener = new KeptJobsListener() {

			@Override
			public void finished(JobTreeElement jte) {
				final JobTreeElement element = jte;
				Job updateJob = new UIJob("Refresh finished") {//$NON-NLS-1$
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						refresh(new Object[] { element });
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
			public void removed(JobTreeElement element) {
				Job updateJob = UIJob.create("Remove finished", monitor -> {
					if (element == null) {
						refresh();
					} else {
						ProgressViewerContentProvider.this.remove(new Object[] { element });
					}
				});
				updateJob.setSystem(true);
				updateJob.schedule();
			}

		};
		return keptJobListener;
	}

	@Override
	public void refresh() {
		progressViewer.refresh(true);
	}

	@Override
	public void refresh(Object[] elements) {
		for (Object refresh : getRoots(elements, true)) {
			progressViewer.refresh(refresh, true);
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		Object[] elements = super.getElements(inputElement);

		if (!showFinished)
			return elements;

		JobTreeElement[] kept = finishedJobs.getKeptElements();

		if (kept.length == 0)
			return elements;

		Set<Object> all = new HashSet<>();

		all.addAll(Arrays.asList(elements));
		for (JobTreeElement next : kept) {
			if (next.getParent() != null && all.contains(next.getParent()))
				continue;
			all.add(next);

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
		HashSet<Object> roots = new HashSet<>();
		for (Object element : elements) {
			JobTreeElement jobTreeElement = (JobTreeElement) element;
			if (jobTreeElement.isJobInfo()) {
				GroupInfo group = ((JobInfo) jobTreeElement).getGroupInfo();
				if (group == null) {
					roots.add(jobTreeElement);
				} else if (subWithParent) {
					roots.add(group);
				}
			} else {
				roots.add(jobTreeElement);
			}
		}
		return roots.toArray();
	}

	@Override
	public void add(Object[] elements) {
		progressViewer.add(elements);

	}

	@Override
	public void remove(Object[] elements) {
		progressViewer.remove(elements);

	}

	@Override
	public void dispose() {
		super.dispose();
		if (keptJobListener != null) {
			finishedJobs.removeListener(keptJobListener);
		}
	}
}
