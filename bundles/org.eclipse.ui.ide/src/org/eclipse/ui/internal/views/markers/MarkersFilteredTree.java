/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Raghunandana Murthappa - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.ui.dialogs.filteredtree.BasicUIJob;
import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTree;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A {@link FilteredTree} hosting the {@link MarkersTreeViewer} of a Markers
 * view, so that the shown markers can be narrowed down with the search box
 * displayed above the tree.
 */
class MarkersFilteredTree extends FilteredTree {

	private final ExtendedMarkersView view;

	private MarkersTreeViewer markersTreeViewer;

	MarkersFilteredTree(Composite parentComposite, int treeStyle, ExtendedMarkersView view, PatternFilter filter) {
		super(parentComposite, treeStyle, filter);
		this.view = view;
	}

	@Override
	protected TreeViewer doCreateTreeViewer(Composite treeParent, int style) {
		markersTreeViewer = new MarkersTreeViewer(treeParent, style);
		return markersTreeViewer;
	}

	/**
	 * Returns the {@link MarkersTreeViewer} hosted by this filtered tree.
	 *
	 * @return the markers tree viewer
	 */
	MarkersTreeViewer getMarkersTreeViewer() {
		return markersTreeViewer;
	}

	/**
	 * Piggy-backs on the (already debounced) job that applies the search box
	 * text to the tree, to also refresh the view's description/status line
	 * once the search filter has actually been applied - otherwise it keeps
	 * reporting counts as if the search box text had no effect, since
	 * filtering by the search box happens entirely at the {@link TreeViewer}
	 * level and the view is never notified of it on its own.
	 */
	@Override
	protected BasicUIJob doCreateRefreshJob() {
		BasicUIJob job = super.doCreateRefreshJob();
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				updateViewTitleAndDescription();
			}

		});
		return job;
	}

	private void updateViewTitleAndDescription() {
		if (isDisposed()) {
			return;
		}
		Display display = getDisplay();
		if (Display.getCurrent() == null) {
			display.asyncExec(() -> updateViewTitleAndDescription());
			return;
		}
		view.updateTitle();
		view.updateStatusLine(markersTreeViewer.getStructuredSelection());
	}

	/**
	 * Shows or hides the search box used to filter the shown markers. Hiding the
	 * box also resets the filter, so that no markers stay hidden by a filter the
	 * user can no longer see.
	 *
	 * @param visible whether the search box should be shown
	 */
	void setFilterTextVisible(boolean visible) {
		if (!visible && getFilterControl() != null && !getFilterControl().isDisposed()) {
			clearText();
		}
		setShowFilterControls(visible);
	}
}
