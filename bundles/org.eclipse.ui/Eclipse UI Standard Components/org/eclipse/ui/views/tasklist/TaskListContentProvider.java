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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Control;
import java.text.MessageFormat;
import java.util.*;

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
	private String summary;
	private String titleSummary;
/**
 * The constructor.
 */
public TaskListContentProvider(TaskList taskList) {
	this.taskList = taskList;
	this.viewer = taskList.getTableViewer();
}
/**
 * Clears any cached summary info.
 */
void clearSummary() {
	summary = null;
	titleSummary = null;
}
/**
 * Computes a one-line summary of the number of tasks and problems being displayed.
 */
void computeSummary() {
	try {
		TasksFilter filter = (TasksFilter) taskList.getFilter();
		IMarker[] markers = getMarkers();
		String summaryFmt = TaskListMessages.getString("TaskList.summaryFmt"); //$NON-NLS-1$
		String unfilteredTitleFmt1 = TaskListMessages.getString("TaskList.unfilteredTitleFmt1"); //$NON-NLS-1$
		String unfilteredTitleFmtN = TaskListMessages.getString("TaskList.unfilteredTitleFmtN"); //$NON-NLS-1$
		String filteredTitleFmt = TaskListMessages.getString("TaskList.filteredTitleFmt"); //$NON-NLS-1$
		if (filter.showAll()) {
			summary = MessageFormat.format(summaryFmt, new Object[] {
				new Integer(markers.length),
				getSummary(Arrays.asList(markers))
			});
			String titleFmt = markers.length == 1 ? unfilteredTitleFmt1 : unfilteredTitleFmtN;
			titleSummary = MessageFormat.format(titleFmt, new Object[] {
				new Integer(markers.length)
			});
		}
		else {
			Object[] filtered = filter.filter(null, null, markers);
			summary = MessageFormat.format(summaryFmt, new Object[] {
				new Integer(filtered.length),
				getSummary(Arrays.asList(filtered))
			});
			titleSummary = MessageFormat.format(filteredTitleFmt, new Object[] {
				new Integer(filtered.length),
				new Integer(getTotalMarkerCount())
			});
		}
	}
	catch (CoreException e) {
		summary = titleSummary = ""; //$NON-NLS-1$
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
/**
 * Returns all the markers that should be shown for
 * the current settings.
 */

public Object[] getElements(Object parent) {
	try {
		clearSummary();
		return getMarkers();
	} catch (CoreException e) {
		return new IMarker[0];
	}
}
/**
 * Recursively walks over the resource delta and gathers all marker deltas.  Marker
 * deltas are placed into one of the three given vectors depending on
 * the type of delta (add, remove, or change).
 */
protected void getMarkerDeltas(IResourceDelta delta, List additions, List removals, List changes) {
	IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
	for (int i = 0; i < markerDeltas.length; i++) {
		IMarkerDelta markerDelta = markerDeltas[i];
		IMarker marker = markerDelta.getMarker();
		switch (markerDelta.getKind()) {
			case IResourceDelta.ADDED:
				if (taskList.isAffectedBy(markerDelta))
					additions.add(marker);
				break;
			case IResourceDelta.REMOVED:
				if (taskList.isAffectedBy(markerDelta))
					removals.add(marker);
				break;
			case IResourceDelta.CHANGED:
				if (taskList.isAffectedBy(markerDelta))
					changes.add(marker);
				break;
		}
	}

	//recurse on child deltas
	IResourceDelta[] children = delta.getAffectedChildren();
	for (int i = 0; i < children.length; i++) {
		getMarkerDeltas(children[i], additions, removals, changes);
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
	String[] types = taskList.getMarkerTypes();
	ArrayList list = new ArrayList();
	for (int i = 0; i < types.length; ++i) {
		IMarker[] markers = res.findMarkers(types[i], true, depth);
		list.addAll(Arrays.asList(markers));
	}
	IMarker[] result = new IMarker[list.size()];
	list.toArray(result);
	return result;
}
/**
 * Returns a one-line string containing a summary of the number
 * of tasks and problems being displayed, for display in the status line.
 */
public String getSummary() {
	// cache summary message since computing it takes time
	if (summary == null)
		computeSummary();
	return summary;
}
/**
 * Returns a one-line string containing a summary of the number
 * of tasks and problems in the given array of markers.
 */
String getSummary(List markers) throws CoreException {
	int numTasks = 0;
	int numErrors = 0, numWarnings = 0, numInfos = 0;
	for (Iterator i = markers.iterator(); i.hasNext();) {
		IMarker marker = (IMarker) i.next();
		if (marker.isSubtypeOf(IMarker.TASK)) {
			++numTasks;
		}
		else if (marker.isSubtypeOf(IMarker.PROBLEM)) {
			switch (MarkerUtil.getSeverity(marker)) {
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
	}
	String fmt = TaskListMessages.getString("TaskList.summaryCountFmt"); //$NON-NLS-1$
	Object[] args = new Object[] {
		new Integer(numTasks),
		new Integer(numErrors), 
		new Integer(numWarnings), 
		new Integer(numInfos)};
	return MessageFormat.format(fmt, args);
}
/**
 * Returns a one-line string containing a summary of the number items,
 * for display in the title bar.
 */
public String getTitleSummary() {
	// cache summary message since computing it takes time
	if (titleSummary == null)
		computeSummary();
	return titleSummary;
}
/**
 * Returns the count of all markers in the workspace which can be shown in the task list.
 */
int getTotalMarkerCount() throws CoreException {
	IResource root = taskList.getWorkspace().getRoot();
	String[] types = TasksFilter.ROOT_TYPES;
	// code below assumes root types, and their subtypes, are non-overlapping
	int count = 0;
	for (int i = 0; i < types.length; ++i) {
		IMarker[] markers = root.findMarkers(types[i], true, IResource.DEPTH_INFINITE);
		count += markers.length;
	}
	return count;
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
	clearSummary();
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
	final List additions = new ArrayList();
	final List removals = new ArrayList();
	final List changes = new ArrayList();
	getMarkerDeltas(event.getDelta(), additions, removals, changes);

	if (additions.size() + removals.size() + changes.size() == 0) {
		// no changes to markers that we care about
		return;
	}

	clearSummary();
	
	// do the required viewer updates in the UI thread
	// need to use syncExec; see 1G95PU8: ITPUI:WIN2000 - Changing task description flashes old description
	viewer.getControl().getDisplay().syncExec(new Runnable() {
		public void run() {
			updateViewer(additions, removals, changes);
		}
	});
	
}
/**
 * Updates the viewer given the lists of added, removed, and changes markers.
 */
void updateViewer(List additions, List removals, List changes) {

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
	// XXX: Do this inside the asyncExec, since we're talking to status line widget.
	taskList.markersChanged();

}
}
