package org.eclipse.ui.views.tasklist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
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
	private Viewer viewer;
	private IResource input;
	private String summary;
/**
 * The constructor.
 */
public TaskListContentProvider(TaskList taskList) {
	this.taskList = taskList;
	this.viewer = taskList.getTableViewer();
}
/**
 * Returns whether we are interested in the given marker delta.
 */
protected boolean check(IMarkerDelta markerDelta) {
	return checkType(markerDelta) && checkResource(markerDelta.getResource());
}
/**
 * Returns whether we are interested in markers on the given resource.
 */
protected boolean checkResource(IResource resource) {
	if (taskList.showSelections() && !taskList.showChildrenHierarchy()) {
		return resource.equals(taskList.getResource());
	}
	else {
		return taskList.getResource().getFullPath().isPrefixOf(resource.getFullPath());
	}
}
/**
 * Returns whether we are interested in the type of the given marker.
 */
protected boolean checkType(IMarkerDelta markerDelta) {
	String[] types = taskList.getMarkerTypes();
	for (int i = 0; i < types.length; ++i) {
		if (markerDelta.isSubtypeOf(types[i])) {
			return true;
		}
	}
	return false;
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
		summary = null;
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
				if (check(markerDelta))
					additions.add(marker);
				break;
			case IResourceDelta.REMOVED:
				if (check(markerDelta))
					removals.add(marker);
				break;
			case IResourceDelta.CHANGED:
				if (check(markerDelta))
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
	int depth = taskList.getResourceDepth();
	String[] types = taskList.getMarkerTypes();
	HashSet set = new HashSet();
	for (int i = 0; i < types.length; ++i) {
		IMarker[] markers = res.findMarkers(types[i], true, depth);
		set.addAll(Arrays.asList(markers));
	}
	IMarker[] result = new IMarker[set.size()];
	set.toArray(result);
	return result;
}
/**
 * Returns a one-line string containing a summary of the number
 * of tasks and problems being displayed.
 */
public String getSummary() {
	// cache summary message since computing it takes time
	if (summary == null)
		summary = getSummary0();
	return summary;
}
/**
 * Returns a one-line string containing a summary of the number
 * of tasks and problems being displayed.
 */
private String getSummary0() {
	try {
		IMarker[] markers = getMarkers();
		Object[] filtered = taskList.getFilter().filter(null, null, markers);
		String fmt = "{0} item(s): {1}";
		Object[] args = new Object[] {
			new Integer(filtered.length),
			taskList.getSummary(Arrays.asList(filtered))
		};
		return MessageFormat.format(fmt, args);
	}
	catch (CoreException e) {
		return "";
	}
}
public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	if (this.input != null) {
		this.input.getWorkspace().removeResourceChangeListener(this);
	}
	this.input = (IResource) newInput;
	if (this.input != null) {
		this.input.getWorkspace().addResourceChangeListener(this);
	}
	this.viewer = viewer;
}
/**
 * The workbench has changed.  Process the delta and issue updates to the viewer,
 * inside the UI thread.
 *
 * @see IResourceChangeListener#resourceChanged
 */
public void resourceChanged(final IResourceChangeEvent event) {
	// we only care about changes that have already happened.
	if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
		return;
	}

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

	// clear cached summary
	summary = null;
	
	// do the required viewer updates in the UI thread
	// need to use syncExec; see 1G95PU8: ITPUI:WIN2000 - Changing task description flashes old description
	viewer.getControl().getDisplay().syncExec(new Runnable() {
		public void run() {
			// This method runs inside an asyncExec.  The widget may have been destroyed
			// by the time this is run.  Check for this and do nothing if so.
			Control ctrl = viewer.getControl();
			if (ctrl == null || ctrl.isDisposed())
				return;
				
			//update the viewer based on the marker changes.
			if (removals.size() > 0) {
				((TableViewer) viewer).remove(removals.toArray());
			}
			if (additions.size() > 0) {
				((TableViewer) viewer).add(additions.toArray());
			}
			if (changes.size() > 0) {
				((TableViewer) viewer).update(changes.toArray(), null);
			}

			// Update the task list's status message.
			// XXX: Quick and dirty solution here.  
			// Would be better to have a separate model for the tasks and
			// have both the content provider and the task list register for updates.
			// XXX: Do this inside the asyncExec, since talking to status line widget.
			taskList.updateStatusMessage();
			
		}
	});
	
}
}
