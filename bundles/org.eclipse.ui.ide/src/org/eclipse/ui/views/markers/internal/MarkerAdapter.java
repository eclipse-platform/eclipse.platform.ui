package org.eclipse.ui.views.markers.internal;
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

/**
 * The MarkerAdapter is the adapter for the deferred update of markers.
 * @since 3.1
 *
 */
public class MarkerAdapter implements IDeferredWorkbenchAdapter {
	
	MarkerView view;

	private MarkerList lastMarkers = new MarkerList();


	/**
	 * Create a new instance of the receiver.
	 * @param markerView
	 */
	MarkerAdapter(MarkerView markerView){
		view = markerView;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object, org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void fetchDeferredChildren(Object object,
			IElementCollector collector, IProgressMonitor monitor) {


		int markerLimit = view.getMarkerLimit();
		monitor.beginTask(MarkerMessages.MarkerView_19, markerLimit == -1 ? 60
				: 100);
		try {
			monitor.subTask(MarkerMessages.MarkerView_waiting_on_changes);

			if (monitor.isCanceled()) {
				return;
			}

			monitor.subTask(MarkerMessages.MarkerView_searching_for_markers);
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
			lastMarkers = MarkerList.compute(view.getEnabledFilters(),
					subMonitor, true);

			if (monitor.isCanceled()) 
				return;
			
			view.refreshMarkerCounts(monitor);
			

		} catch (CoreException e) {
			IDEWorkbenchPlugin.getDefault().getLog().log(e.getStatus());
			lastMarkers = new MarkerList();
			return;
		}

		if (monitor.isCanceled()) {
			return;
		}

		// Exit immediately if the markers have changed in the meantime.

		Collection markers = Arrays.asList(lastMarkers.toArray());

		if (markerLimit != -1) {

			monitor.subTask(MarkerMessages.MarkerView_18);
			SubProgressMonitor mon = new SubProgressMonitor(monitor, 40);

			markers = SortUtil.getFirst(markers, (TableSorter) view.getViewer().getSorter(), markerLimit, mon);
			if (monitor.isCanceled())
				return;
		}

		monitor.subTask(MarkerMessages.MarkerView_queueing_updates);

		SubProgressMonitor sub = new SubProgressMonitor(monitor, 50);
		collector.add(markers.toArray(), sub);
		
		if (monitor.isCanceled())
			return;

		monitor.done();
		view.scheduleCountUpdate();	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
	 */
	public boolean isContainer() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
	 */
	public ISchedulingRule getRule(Object object) {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		if(o.equals(this))
			return lastMarkers.toArray();
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return MarkerMessages.MarkerList_0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		if(o instanceof ConcreteMarker)
			return this;
		return null;
	}

	/**
	 * Get the current list of markers.
	 * @return MarkerList
	 */
	MarkerList getCurrentMarkers(){
		return lastMarkers;
		
	}
}
