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

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.swt.widgets.Control;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.viewers.*;

/**
 * Task list content provider returns elements that should be
 * in the task list depending on the selection mode.
 * It goes directly to the marker manager and retreives
 * tasks and problems.
 */
/* package */ class TaskListContentProvider implements IStructuredContentProvider, IResourceChangeListener {
	
	private TaskList taskList;
	private TableViewer viewer;
	private IResource input;
	
	// cached number of tasks, errors, warnings and infos, for the shown markers
	private int[] markerCounts = null;

	// total count of all applicable markers in workspace, maintained incrementally
	private int totalMarkerCount = -1;
			
/**
 * The constructor.
 */
public TaskListContentProvider(TaskList taskList) {
	this.taskList = taskList;
	this.viewer = taskList.getTableViewer();
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
/**
 * Returns all the markers that should be shown for
 * the current settings.
 */
public Object[] getElements(Object parent) {
	try {
		IMarker[] markers = getMarkers();
		this.markerCounts = getMarkerCounts(Arrays.asList(markers));
		return markers;
	} catch (CoreException e) {
		return new IMarker[0];
	}
}

/**
 * Recursively walks over the resource delta and gathers all
 * root type marker deltas.
 */
private ArrayList getMarkerDeltas(IResourceChangeEvent event) {
	IMarkerDelta[] markerDeltas = event.findMarkerDeltas(null, true);
	ArrayList list = new ArrayList(markerDeltas.length);
	for (int i = 0; i < markerDeltas.length; i++) {
		IMarkerDelta markerDelta = markerDeltas[i];
		if (taskList.isRootType(markerDelta)) {
			list.add(markerDelta);
		}
	}
	return list;
}

/**
 * Partition the marker deltas into one of the three given lists depending on
 * the type of delta (add, remove, or change).
 * The resulting lists contain the corresponding markers, not the deltas.
 * Deltas which are not under the current focus resource are discarded.
 * This also updates the marker counts.
 */
private void partitionMarkerDeltas(ArrayList markerDeltas, ArrayList additions, ArrayList removals, ArrayList changes) {
	for (int i = 0, size = markerDeltas.size(); i < size; ++i) {
		IMarkerDelta markerDelta = (IMarkerDelta) markerDeltas.get(i);
		if (taskList.isAffectedBy(markerDelta)) {
			IMarker marker = markerDelta.getMarker();
			switch (markerDelta.getKind()) {
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
					// Assume attribute changes don't affect marker counts.
					// This is only true if problem severities can't change.
					break;
			}
		}
	}
}

/**
 * Returns the markers to show in the task list.
 */
IMarker[] getMarkers() throws CoreException {
		
	IResource res = taskList.getResource();
	if (!res.exists()) {
		return new IMarker[0];
	}
	
	// added by cagatayk@acm.org
	if (taskList.showOwnerProject()) {
		IResource project = res.getProject();
		
		// leave it alone if it's workspace root
		if (project != null)
			res = project;
	}
	
	int depth = taskList.getResourceDepth();
	IMarker[] markers = res.findMarkers(null, true, depth);
	
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
 * Returns a one-line string containing a summary of the number
 * of tasks and problems being displayed, for display in the status line.
 */
public String getStatusSummary() {
	if (markerCounts == null) {
		return ""; //$NON-NLS-1$
	}
	String summaryFmt = TaskListMessages.getString("TaskList.summaryFmt"); //$NON-NLS-1$
	Object[] args = new Object[] {
		new Integer(sum(markerCounts)),
		getCountSummary(markerCounts)
	};
	return MessageFormat.format(summaryFmt, args);
}

/**
 * Returns a one-line string containing a summary of the number
 * of tasks and problems that are currently selected, for display in the status line.
 * 
 * @param selection the current selection
 */
public String getStatusSummary(IStructuredSelection selection) {
	int[] counts = getMarkerCounts(selection.toList());
	String fmt = TaskListMessages.getString("TaskList.selectedFmt"); //$NON-NLS-1$
	Object[] args = new Object[] {
		new Integer(sum(counts)),
		getCountSummary(counts)
	};
	return MessageFormat.format(fmt, args);
}

/**
 * Returns a string summarizing the given marker counts.
 */
private String getCountSummary(int[] counts) {
	int numTasks = counts[0];
	int numErrors = counts[1];
	int numWarnings = counts[2];
	int numInfos = counts[3];
	String fmt = TaskListMessages.getString("TaskList.summaryCountFmt"); //$NON-NLS-1$
	Object[] args = new Object[] {
		new Integer(numTasks),
		new Integer(numErrors), 
		new Integer(numWarnings), 
		new Integer(numInfos)};
	return MessageFormat.format(fmt, args);
}

/**
 * Returns the number of tasks, errors, warnings, infos
 * in the given markers.
 */
int[] getMarkerCounts(List markers) {
	int numTasks = 0;
	int numErrors = 0, numWarnings = 0, numInfos = 0;
	for (Iterator i = markers.iterator(); i.hasNext();) {
		IMarker marker = (IMarker) i.next();
		if (MarkerUtil.isMarkerType(marker, IMarker.PROBLEM)) {
			int sev = MarkerUtil.getSeverity(marker);
			switch (sev) {
				case IMarker.SEVERITY_ERROR:
					++numErrors;
					break;
				case IMarker.SEVERITY_WARNING:
					++numWarnings;
					break;
				case IMarker.SEVERITY_INFO:
					++numInfos;
					break;
			}
		}
		else if (MarkerUtil.isMarkerType(marker, IMarker.TASK)) {
			++numTasks;
		}
	}
	return new int[] { numTasks, numErrors, numWarnings, numInfos };
}

/**
 * Returns the sum of the given counts.
 */
private int sum(int[] counts) {
	int sum = 0;
	for (int i = 0; i < counts.length; ++i)
		sum += counts[i];
	return sum;
}

/**
 * Returns a one-line string containing a summary of the number items
 * being shown by the filter, for display in the title bar.
 */
public String getFilterSummary() {
	if (markerCounts == null) {
		return ""; //$NON-NLS-1$
	}
	int numShowing = sum(markerCounts);
	TasksFilter filter = (TasksFilter) taskList.getFilter();
	if (filter.isShowingAll()) {
		String titleFmt = 
			numShowing == 1 
				? TaskListMessages.getString("TaskList.unfilteredFmt1")  //$NON-NLS-1$
				: TaskListMessages.getString("TaskList.unfilteredFmtN"); //$NON-NLS-1$
		return MessageFormat.format(titleFmt, new Object[] {
			new Integer(numShowing)
		});
	}
	else {
		String filteredTitleFmt = TaskListMessages.getString("TaskList.filteredFmt"); //$NON-NLS-1$
		return MessageFormat.format(filteredTitleFmt, new Object[] {
			new Integer(numShowing),
			new Integer(getTotalMarkerCount())
		});
	}
}
/**
 * Returns the count of all markers in the workspace which can be shown in the task list.
 * This is computed once, then maintained incrementally by the delta processing.
 */
int getTotalMarkerCount() {
	if (totalMarkerCount == -1) {
		int count = 0;
		try {
			IResource root = taskList.getWorkspace().getRoot();
			IMarker[] markers = root.findMarkers(null, true, IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; ++i) {
				if (taskList.isRootType(markers[i])) {
					++count;
				}
			}
		} catch (CoreException e) {
			// shouldn't happen
		}
		totalMarkerCount = count;
	}
	return totalMarkerCount;
}

public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	if (this.input != null) {
		this.input.getWorkspace().removeResourceChangeListener(this);
	}
	this.input = (IResource) newInput;
	if (this.input != null) {
		this.input.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}
	this.viewer = (TableViewer) viewer;
}

/**
 * The workbench has changed.  Process the delta and issue updates to the viewer,
 * inside the UI thread.
 *
 * @see IResourceChangeListener#resourceChanged
 */
public void resourceChanged(final IResourceChangeEvent event) {

	// gather all marker changes from the delta.
	// be sure to do this in the calling thread, 
	// as the delta is destroyed when this method returns
	ArrayList markerDeltas = getMarkerDeltas(event);
	
	int oldTotal = totalMarkerCount;
	updateTotalMarkerCount(markerDeltas);
	
	final ArrayList additions = new ArrayList();
	final ArrayList removals = new ArrayList();
	final ArrayList changes = new ArrayList();
	partitionMarkerDeltas(markerDeltas, additions, removals, changes);

	if (oldTotal == totalMarkerCount && additions.size() + removals.size() + changes.size() == 0) {
		// no changes to markers that we care about
		return;
	}
	
	// do the required viewer updates in the UI thread
	// need to use syncExec; see 1G95PU8: ITPUI:WIN2000 - Changing task description flashes old description
	viewer.getControl().getDisplay().syncExec(new Runnable() {
		public void run() {
			updateViewer(additions, removals, changes);
		}
	});
}

/**
 * Updates the marker counts for the given delta.
 * Assumptions:
 *   - the delta is either an addition or a removal
 *   - problem severities don't change
 */
private void updateMarkerCounts(IMarkerDelta markerDelta, int diff) {
	if (markerCounts == null) {
		return;
	}
	if (markerDelta.isSubtypeOf(IMarker.PROBLEM)) {
		int sev = markerDelta.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		switch (sev) {
			case IMarker.SEVERITY_ERROR:
				markerCounts[1] += diff;
				break;
			case IMarker.SEVERITY_WARNING:
				markerCounts[2] += diff;
				break;
			case IMarker.SEVERITY_INFO:
				markerCounts[3] += diff;
				break;
		}
	}
	else if (markerDelta.isSubtypeOf(IMarker.TASK)) {
		markerCounts[0] += diff;
	}
}

/**
 * Updates the total count of markers given the applicable marker deltas.
 */
private void updateTotalMarkerCount(ArrayList markerDeltas) {
	if (totalMarkerCount == -1) {
		return;
	}
	for (int i = 0, size = markerDeltas.size(); i < size; ++i) {
		IMarkerDelta markerDelta = (IMarkerDelta) markerDeltas.get(i);
		switch (markerDelta.getKind()) {
			case IResourceDelta.ADDED:
				++totalMarkerCount;
				break;
			case IResourceDelta.REMOVED:
				--totalMarkerCount;
				break;
		}
	}
}

/**
 * Updates the viewer given the lists of added, removed, and changes markers.
 * This is called inside an syncExec.
 */
private void updateViewer(ArrayList additions, ArrayList removals, ArrayList changes) {

	// The widget may have been destroyed by the time this is run.  
	// Check for this and do nothing if so.
	Control ctrl = viewer.getControl();
	if (ctrl == null || ctrl.isDisposed())
		return;

	//update the viewer based on the marker changes.
	//process removals before additions, to avoid multiple equal elements in the viewer
	if (removals.size() > 0) {
		// Cancel any open cell editor.  We assume that the one being edited is the one being removed.
		viewer.cancelEditing();
		viewer.remove(removals.toArray());
	}
	if (additions.size() > 0) {
		viewer.add(additions.toArray());
	}
	if (changes.size() > 0) {
		viewer.update(changes.toArray(), null);
	}

	// Update the task list's status message.
	// XXX: Quick and dirty solution here.  
	// Would be better to have a separate model for the tasks and
	// have both the content provider and the task list register for updates.
	// XXX: Do this inside the syncExec, since we're talking to status line widget.
	taskList.markersChanged();

}
}
