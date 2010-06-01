/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * The UIUpdateJob runs in the UI thread and is responsible updating the Markers
 * view UI with newly updated markers.
 * 
 * @since 3.6
 * 
 */
class UIUpdateJob extends WorkbenchJob {
	
	private ExtendedMarkersView view;

	private boolean updating;

	private long lastUpdateTime=-1;

	/**
	 * @param view
	 *            the markers view base class
	 */
	public UIUpdateJob(ExtendedMarkersView view) {
		super(view.getSite().getShell().getDisplay(),
				MarkerMessages.MarkerView_19);
		this.view = view;
		updating = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	public IStatus runInUIThread(IProgressMonitor monitor) {
		if(monitor.isCanceled()){
			return Status.CANCEL_STATUS;
		}
		TreeViewer viewer = view.getViewer();
		if (viewer.getControl().isDisposed()) {
			return Status.CANCEL_STATUS;
		}
		
		Markers clone = view.getActiveViewerInputClone();
		try {
			updating = true;
			monitor.beginTask(MarkerMessages.MarkerView_19,
					IProgressMonitor.UNKNOWN);

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			
			//view.indicateUpdating(MarkerMessages.MarkerView_19,
			//		true);

			// If there is only one category and the user has no saved state
			// show it
			if (view.getBuilder().isShowingHierarchy()
					&& view.getCategoriesToExpand().isEmpty()) {
				MarkerCategory[] categories = clone.getCategories();
				if (categories != null && categories.length == 1)
					view.getCategoriesToExpand().add(
							categories[0].getDescription());
			}

			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			/* 
			 * always use a clone for Thread safety. We avoid setting the clone
			 * as new input as we would offset the benefits of optimization in
			 * TreeViewer.
			 */
			clone = view.createViewerInputClone();
			if (clone == null) {
				// do not update yet,we are changing
				return Status.CANCEL_STATUS;
			}
			/*
			 * we prefer not to check for cancellation beyond this since we
			 * have to show correct marker counts on UI, not an updating message.
			 */
			IContentProvider contentProvider = viewer.getContentProvider();
			contentProvider.inputChanged(viewer, view.getViewerInput(), clone);
			viewer.getTree().setRedraw(false);
			viewer.refresh(true);
			if (!monitor.isCanceled()) {
				//do not expand if canceled
				view.reexpandCategories();
			}
			if (view.getBuilder().readChangeFlags()[0]) {
				// indicate changes
			}
			//show new counts
			view.updateTitle();

			lastUpdateTime = System.currentTimeMillis();
		} finally {
			viewer.getTree().setRedraw(true);
			view.updateStatusLine((IStructuredSelection) viewer.getSelection());
			//view.updateCategoryLabels();
			updating = false;
		}
		monitor.done();
		return Status.OK_STATUS;
	}

	/**
	 * @return Returns true if updating.
	 */
	boolean isUpdating() {
		return updating;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
	 */
	public boolean shouldRun() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */
	public boolean belongsTo(Object family) {
		if (family.equals(view.MARKERSVIEW_UPDATE_JOB_FAMILY)) {
			return true;
		}
		return super.belongsTo(family);
	}

	/**
	 * @return Returns the lastUpdateTime.
	 */
	long getLastUpdateTime() {
		return lastUpdateTime;
	}
}