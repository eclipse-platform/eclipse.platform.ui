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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The ProgressViewerContentProvider is the content provider progress viewers.
 */
public class ProgressViewerContentProvider extends ProgressContentProvider {
	/** Viewer to show content. */
	protected AbstractProgressViewer progressViewer;

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
		startListening();
	}

	/**
	 * Stop listening on progress item changes.
	 */
	public void stopListening() {
		ProgressViewUpdater.getSingleton().removeCollector(this);
		refreshNeeded = true;
	}

	/**
	 * Start listening on progress item changes.
	 */
	public void startListening() {
		ProgressViewUpdater.getSingleton().addCollector(this, showFinished);
		if (refreshNeeded) {
			refreshNeeded = false;
			refresh();
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

		JobTreeElement[] kept = FinishedJobs.getInstance().getKeptElements();

		if (kept.length == 0) {
			return elements;
		}

		Set<Object> all = new LinkedHashSet<>();

		all.addAll(Arrays.asList(elements));
		for (JobTreeElement next : kept) {
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
				} else if (subWithParent) {
					roots.add(group);
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
