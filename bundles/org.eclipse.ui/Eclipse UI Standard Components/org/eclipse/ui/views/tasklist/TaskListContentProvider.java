package org.eclipse.ui.views.tasklist;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
  Cagatay Kavukcuoglu <cagatayk@acm.org> - Filter for markers in same project
**********************************************************************/

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.*;

/**
 * Task list content provider returns elements that should be
 * in the task list depending on the selection mode.
 * It goes directly to the marker manager and retreives
 * tasks and problems.
 */
class TaskListContentProvider implements IStructuredContentProvider, 
		IResourceChangeListener {
	
	private static final int TASKS = 0;
	private static final int ERRORS = 1;
	private static final int WARNINGS = 2;
	private static final int INFOS = 3;

	private TaskList taskList;
	private TableViewer viewer;
	private IResource input;
	
	/* cached counts of tasks, errors, warnings and infos for the visible 
	 * markers, maintained incrementally */
	private int[] visibleMarkerCounts = null;

	/* cached count of all markers in workspace matching supported root types 
	 * (tasks & problems), maintained incrementally */
	private int totalMarkerCount = -1;

	/**
	 * The constructor.
	 */
	public TaskListContentProvider(TaskList taskList) {
		this.taskList = taskList;
		this.viewer = taskList.getTableViewer();
	}

	private boolean getFilterOnMarkerLimit() {
		return taskList.getFilter().getFilterOnMarkerLimit();		
	}

	private int getMarkerLimit() {
		return taskList.getFilter().getMarkerLimit();		
	}

	private boolean isMarkerLimitExceeded() {
		return taskList.isMarkerLimitExceeded();
	}

	private void setMarkerLimitExceeded(boolean markerLimitExceeded) {
		taskList.setMarkerLimitExceeded(markerLimitExceeded);

		viewer.getControl().getDisplay().syncExec(new Runnable() {		
			public void run() {
				viewer.refresh();
			}
		});
	}

	/**
	 * Returns a one-line string containing a summary of the number
	 * of visible tasks and problems.
	 */
	public String getStatusSummaryVisible() {
		if (visibleMarkerCounts == null) {
			return ""; //$NON-NLS-1$
		}
	
		return TaskListMessages.format(
			"TaskList.statusSummaryVisible", //$NON-NLS-1$
			new Object[] {
				new Integer(sum(visibleMarkerCounts)),
				getStatusSummaryBreakdown(visibleMarkerCounts)});
	}
	
	/**
	 * Returns a one-line string containing a summary of the number
	 * of selected tasks and problems.
	 * 
	 * @param selection the current selection
	 */
	public String getStatusSummarySelected(IStructuredSelection selection) {
		int[] selectedMarkerCounts = getMarkerCounts(selection.toList());
		return TaskListMessages.format(
			"TaskList.statusSummarySelected", //$NON-NLS-1$
			new Object[] {
				new Integer(sum(selectedMarkerCounts)),
				getStatusSummaryBreakdown(selectedMarkerCounts)});
	}

	/**
	 * Returns a one-line string containing a summary of the number of 
	 * given tasks, errors, warnings, and infos.
	 */
	private String getStatusSummaryBreakdown(int[] counts) {
		return TaskListMessages.format(
			"TaskList.statusSummaryBreakdown", //$NON-NLS-1$
			new Object[] {
				new Integer(counts[TASKS]),
				new Integer(counts[ERRORS]),
				new Integer(counts[WARNINGS]),
				new Integer(counts[INFOS])});
	}

	/**
	 * Returns a one-line string containing a summary of the number items
	 * being shown by the filter, for display in the title bar.
	 */
	public String getTitleSummary() {
		if (visibleMarkerCounts == null) {
			return ""; //$NON-NLS-1$
		}

		int visibleMarkerCount = sum(visibleMarkerCounts);
		TasksFilter filter = (TasksFilter) taskList.getFilter();

		if (filter.isShowingAll()) {
			return TaskListMessages.format(
				"TaskList.titleSummaryUnfiltered", //$NON-NLS-1$
				new Object[] { new Integer(visibleMarkerCount)});
		}
		else {
			return TaskListMessages.format(
				"TaskList.titleSummaryFiltered", //$NON-NLS-1$
				new Object[] {
					new Integer(visibleMarkerCount),
					new Integer(getTotalMarkerCount())});
		}
	}

	/**
	 * Returns the sum of the given counts.
	 */
	private int sum(int[] counts) {
		int sum = 0;

		for (int i = 0, l = counts.length; i < l; ++i) {
			sum += counts[i];
		}

		return sum;
	}
	
	/**
	 * Returns the count of all markers in the workspace which can be shown in 
	 * the task list. This is computed once, then maintained incrementally by 
	 * the delta processing.
	 */
	private int getTotalMarkerCount() {
		if (totalMarkerCount == -1) {
			totalMarkerCount = 0;

			try {
				IResource root = taskList.getWorkspace().getRoot();
				IMarker[] markers = root.findMarkers(null, true, 
						IResource.DEPTH_INFINITE);

				for (int i = 0; i < markers.length; ++i) {
					if (isRootType(markers[i])) {
						++totalMarkerCount;
					}
				}
			} catch (CoreException e) {
				// shouldn't happen
			}
		}

		return totalMarkerCount;
	}

	/**
	 * Returns whether the given marker is a subtype of one of the root types.
	 */
	private boolean isRootType(IMarker marker) {
		String[] rootTypes = TasksFilter.ROOT_TYPES;
		
		for (int i = 0, l = rootTypes.length; i < l; ++i) {
			if (MarkerUtil.isMarkerType(marker, rootTypes[i])) {
				return true;
			}
		}
		
		return false;
	}	

	/**
	 * Returns the markers to show in the task list.
	 */
	private IMarker[] getMarkers() throws CoreException {			
		IResource resource = taskList.getResource();
		
		if (!resource.exists()) {
			return new IMarker[0];
		}
		
		// added by cagatayk@acm.org
		if (taskList.showOwnerProject()) {
			IResource project = resource.getProject();
			
			// leave it alone if it's workspace root
			if (project != null) {
				resource = project;
			}
		}
		
		int depth = taskList.getResourceDepth();
		IMarker[] markers = resource.findMarkers(null, true, depth);
		
		// Do our own filtering below rather than using a viewer filter
		// because this simplifies maintaining the marker counts.
		TasksFilter filter = taskList.getFilter();		
		ArrayList list = new ArrayList(markers.length);

		for (int i = 0; i < markers.length; ++i) {
			IMarker marker = markers[i];
		
			if (filter.select(marker)) {
				list.add(marker);
			}
		}
	
		IMarker[] result = new IMarker[list.size()];
		list.toArray(result);	
		return result;
	}

	/**
	 * Returns the number of tasks, errors, warnings, infos
	 * in the given markers.
	 */
	private int[] getMarkerCounts(List markers) {
		int[] markerCounts = new int[4];
		Iterator iterator = markers.iterator();
		
		while (iterator.hasNext()) {
			IMarker marker = (IMarker) iterator.next();

			if (MarkerUtil.isMarkerType(marker, IMarker.PROBLEM)) {
				switch (MarkerUtil.getSeverity(marker)) {
				case IMarker.SEVERITY_ERROR:
					++markerCounts[ERRORS];
					break;
				case IMarker.SEVERITY_WARNING:
					++markerCounts[WARNINGS];
					break;
				case IMarker.SEVERITY_INFO:
					++markerCounts[INFOS];
					break;
				}
			}
			else if (MarkerUtil.isMarkerType(marker, IMarker.TASK)) {
				++markerCounts[TASKS];
			}
		}

		return markerCounts;
	}

	/**
	 * Updates the marker counts for the given delta.
	 * Assumptions:
	 *   - the delta is either an addition or a removal
	 *   - problem severities don't change
	 */
	private void updateMarkerCounts(IMarkerDelta markerDelta, int diff) {
		if (visibleMarkerCounts == null) {
			return;
		}

		if (markerDelta.isSubtypeOf(IMarker.PROBLEM)) {
			int severity = markerDelta.getAttribute(IMarker.SEVERITY, 
					IMarker.SEVERITY_WARNING);

			switch (severity) {
			case IMarker.SEVERITY_ERROR:
				visibleMarkerCounts[ERRORS] += diff;
				break;
			case IMarker.SEVERITY_WARNING:
				visibleMarkerCounts[WARNINGS] += diff;
				break;
			case IMarker.SEVERITY_INFO:
				visibleMarkerCounts[INFOS] += diff;
				break;
			}
		}
		else if (markerDelta.isSubtypeOf(IMarker.TASK)) {
			visibleMarkerCounts[TASKS] += diff;
		}
	}

	/**
	 * Updates the viewer given the lists of added, removed, and changes 
	 * markers. This is called inside an syncExec.
	 */
	private void updateViewer(List additions, List removals, List changes) {
			
		// The widget may have been destroyed by the time this is run.  
		// Check for this and do nothing if so.
		Control ctrl = viewer.getControl();

		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}
	
		//update the viewer based on the marker changes.
		//process removals before additions, to avoid multiple equal elements in 
		//the viewer
		if (removals.size() > 0) {

			// Cancel any open cell editor.  We assume that the one being edited 
			// is the one being removed.
			viewer.cancelEditing();
			viewer.remove(removals.toArray());
		}

		if (additions.size() > 0) {
			viewer.add(additions.toArray());
		}

		if (changes.size() > 0) {
			viewer.update(changes.toArray(), null);
		}
	}

	/**
	 * The visual part that is using this content provider is about
	 * to be disposed. Deallocate all allocated SWT resources.
	 */
	public void dispose() {
		if (input != null) {
			input.getWorkspace().removeResourceChangeListener(this);
			input = null;
		}
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.input != null) {
			this.input.getWorkspace().removeResourceChangeListener(this);
		}

		this.input = (IResource) newInput;

		if (this.input != null) {
			this.input.getWorkspace().addResourceChangeListener(this, 
					IResourceChangeEvent.POST_CHANGE);
		}

		this.viewer = (TableViewer) viewer;
	}

	/**
	 * Returns all the markers that should be shown for
	 * the current settings.
	 */
	public Object[] getElements(Object parent) {
		try {
			IMarker[] markers = getMarkers();
			this.visibleMarkerCounts = getMarkerCounts(Arrays.asList(markers));

			if (getFilterOnMarkerLimit() && markers.length > getMarkerLimit()) {
				if (!isMarkerLimitExceeded()) {
					setMarkerLimitExceeded(true);
				}
				
				return new IMarker[0];
			}
			else {
				if (isMarkerLimitExceeded()) {
					setMarkerLimitExceeded(false);
				}			

				return markers;
			}
		} catch (CoreException e) {
			return new IMarker[0];
		}
	}

	/**
	 * The workbench has changed.  Process the delta and issue updates to the 
	 * viewer, inside the UI thread.
	 *
	 * @see IResourceChangeListener#resourceChanged
	 */
	public void resourceChanged(final IResourceChangeEvent event) {	

		/*
		 * gather all marker changes from the delta. be sure to do this in the 
		 * calling thread, as the delta is destroyed when this method returns
		 */
		IMarkerDelta[] markerDeltas = event.findMarkerDeltas(null, true);
		int oldTotal = totalMarkerCount;
		final List additions = new ArrayList();
		final List removals = new ArrayList();
		final List changes = new ArrayList();

		for (int i = 0; i < markerDeltas.length; i++) {
			IMarkerDelta markerDelta = markerDeltas[i];
			int iKind = markerDelta.getKind();

			for (int j = 0; j < TasksFilter.ROOT_TYPES.length; ++j) {
				if (markerDelta.isSubtypeOf(TasksFilter.ROOT_TYPES[j])) {

					/* 
					 * Updates the total count of markers given the applicable 
					 * marker deltas. 
					 */
					if (totalMarkerCount != -1) {
						switch (iKind) {
						case IResourceDelta.ADDED:
							++totalMarkerCount;
							break;
						case IResourceDelta.REMOVED:
							--totalMarkerCount;
							break;
						}
					}

					/*
					 * Partition the marker deltas into one of the three given 
					 * lists depending on
					 * the type of delta (add, remove, or change).
					 * The resulting lists contain the corresponding markers, 
					 * not the deltas.
					 * Deltas which are not under the current focus resource are 
					 * discarded.
					 * This also updates the marker counts.
					 */
					if (taskList.isAffectedBy(markerDelta)) {
						IMarker marker = markerDelta.getMarker();
						
						switch (iKind) {
						case IResourceDelta.ADDED:
							additions.add(marker);														
							updateMarkerCounts(markerDelta, +1);
							break;
						case IResourceDelta.REMOVED:
							removals.add(marker);
							updateMarkerCounts(markerDelta, -1);
							break;
						case IResourceDelta.CHANGED:
							changes.add(marker);

							/* 
							 * Assume attribute changes don't affect marker 
							 * counts. This is only true if problem severities 
							 * can't change. 
							 * */
							break;
						}
					}				

					break;
				}
			}
		}
	
		if (oldTotal == totalMarkerCount && additions.size() + removals.size() 
				+ changes.size() == 0) {
			// no changes to markers that we care about
			return;
		}

		/*
		 * do the required viewer updates in the UI thread need to use syncExec; 
		 * see 1G95PU8: ITPUI:WIN2000 - Changing task description flashes old 
		 * description
		 */				
		viewer.getControl().getDisplay().syncExec(new Runnable() {		
			public void run() {
					
				if (getFilterOnMarkerLimit() && sum(visibleMarkerCounts) > getMarkerLimit()) {
					if (!isMarkerLimitExceeded()) {			
						setMarkerLimitExceeded(true);
					}
				}
				else if (taskList.isMarkerLimitExceeded()) {
					setMarkerLimitExceeded(false);
				}
				else {
					updateViewer(additions, removals, changes);
				}

				/* Update the task list's status message.
				 * XXX: Quick and dirty solution here.  
				 * Would be better to have a separate model for the tasks and
				 * have both the content provider and the task list register for 
				 * updates. XXX: Do this inside the syncExec, since we're 
				 * talking to status line widget.
				 */
				taskList.markersChanged();	
			}
		});		
	}
}
