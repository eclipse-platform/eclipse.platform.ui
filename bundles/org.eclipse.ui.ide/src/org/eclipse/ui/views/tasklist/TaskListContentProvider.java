/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.views.tasklist.TaskListMessages;

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
    }

    /**
     * Returns a one-line string containing a summary of the number
     * of visible tasks and problems.
     */
    public String getStatusSummaryVisible() {
        if (visibleMarkerCounts == null) {
            return ""; //$NON-NLS-1$
        }

        return NLS.bind(TaskListMessages.TaskList_statusSummaryVisible,new Integer(sum(visibleMarkerCounts)),
		getStatusSummaryBreakdown(visibleMarkerCounts));
    }

    /**
     * Returns a one-line string containing a summary of the number
     * of selected tasks and problems.
     * 
     * @param selection the current selection
     */
    public String getStatusSummarySelected(IStructuredSelection selection) {
        int[] selectedMarkerCounts = getMarkerCounts(selection.toList());
        return NLS.bind(TaskListMessages.TaskList_statusSummarySelected, new Integer(sum(selectedMarkerCounts)),
		getStatusSummaryBreakdown(selectedMarkerCounts) );
    }

    /**
     * Returns a one-line string containing a summary of the number of 
     * given tasks, errors, warnings, and infos.
     */
    private String getStatusSummaryBreakdown(int[] counts) {
        return NLS.bind(
				TaskListMessages.TaskList_statusSummaryBreakdown, 
				new Object []{ 
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
        TasksFilter filter = taskList.getFilter();

        if (filter.isShowingAll()) {
            return NLS.bind(TaskListMessages.TaskList_titleSummaryUnfiltered, new Integer(visibleMarkerCount));
        } else {
            return NLS.bind(TaskListMessages.TaskList_titleSummaryFiltered, new Integer(visibleMarkerCount),
			new Integer(getTotalMarkerCount()));
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
        IResource[] resources = taskList.getResources();
        int l = resources.length;
        IResource resource;
        boolean bExists = false;

        for (int i = 0; i < l; i++) {
            resource = resources[i];

            if (resource != null && resource.exists()) {
                bExists = true;
                break;
            }
        }

        if (!bExists) {
            return new IMarker[0];
        }

        if (taskList.showOwnerProject()) {
            IResource[] projectResources = new IResource[l];
            IResource project;

            for (int i = 0; i < l; i++) {
                resource = resources[i];

                if (resource != null) {
                    project = resource.getProject();

                    if (project != null) {
                        projectResources[i] = project;
                    } else {
                        projectResources[i] = resource;
                    }
                }
            }

            resources = projectResources;
        }

        int depth = taskList.getResourceDepth();
        TasksFilter filter = taskList.getFilter();
        Set set = new HashSet();

        for (int i = 0; i < l; i++) {
            resource = resources[i];

            if (resource != null) {
                IMarker[] markers = resource.findMarkers(null, true, depth);

                for (int j = 0; j < markers.length; ++j) {
                    IMarker marker = markers[j];

                    if (filter.select(marker)) {
                        set.add(marker);
                    }
                }
            }
        }

        IMarker[] result = new IMarker[set.size()];
        set.toArray(result);
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
            } else if (MarkerUtil.isMarkerType(marker, IMarker.TASK)) {
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
    private void updateMarkerCounts(IMarkerDelta markerDelta, int difference) {
        if (visibleMarkerCounts == null) {
			return;
		}

        if (markerDelta.isSubtypeOf(IMarker.PROBLEM)) {
            int severity = markerDelta.getAttribute(IMarker.SEVERITY,
                    IMarker.SEVERITY_WARNING);

            switch (severity) {
            case IMarker.SEVERITY_ERROR:
                visibleMarkerCounts[ERRORS] += difference;
                break;
            case IMarker.SEVERITY_WARNING:
                visibleMarkerCounts[WARNINGS] += difference;
                break;
            case IMarker.SEVERITY_INFO:
                visibleMarkerCounts[INFOS] += difference;
                break;
            }
        } else if (markerDelta.isSubtypeOf(IMarker.TASK)) {
			visibleMarkerCounts[TASKS] += difference;
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

                    viewer.getControl().getDisplay().syncExec(new Runnable() {
                        public void run() {
                            viewer.refresh();
                        }
                    });
                }

                return new IMarker[0];
            } else {
                if (isMarkerLimitExceeded()) {
                    setMarkerLimitExceeded(false);

                    viewer.getControl().getDisplay().syncExec(new Runnable() {
                        public void run() {
                            viewer.refresh();
                        }
                    });
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

        if (markerDeltas == null) {
			return;
		}

        int oldTotal = totalMarkerCount;
        final List additions = new ArrayList();
        final List removals = new ArrayList();
        final List changes = new ArrayList();

        for (int i = 0; i < markerDeltas.length; i++) {
            IMarkerDelta markerDelta = markerDeltas[i];

            if (markerDelta == null) {
				continue;
			}

            int iKind = markerDelta.getKind();

            for (int j = 0; j < TasksFilter.ROOT_TYPES.length; j++) {
                if (markerDelta.isSubtypeOf(TasksFilter.ROOT_TYPES[j])) {

                    /* 
                     * Updates the total count of markers given the applicable 
                     * marker deltas. 
                     */
                    if (totalMarkerCount != -1) {
                        switch (iKind) {
                        case IResourceDelta.ADDED:
                            totalMarkerCount++;
                            break;
                        case IResourceDelta.REMOVED:
                            totalMarkerCount--;
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

                    IResource resource = markerDelta.getResource();

                    if (resource == null) {
						continue;
					}

                    boolean affectedBy = taskList.checkResource(resource)
                            && taskList.getFilter().select(markerDelta);

                    if (affectedBy) {
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
                             */
                            break;
                        }
                    }

                    break;
                }
            }
        }

        if (oldTotal == totalMarkerCount
                && additions.size() + removals.size() + changes.size() == 0) {
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
                if (getFilterOnMarkerLimit()
                        && sum(visibleMarkerCounts) > getMarkerLimit()) {
                    if (!isMarkerLimitExceeded()) {
                        setMarkerLimitExceeded(true);
                        viewer.refresh();
                    }
                } else if (taskList.isMarkerLimitExceeded()) {
                    setMarkerLimitExceeded(false);
                    viewer.refresh();
                } else {
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
