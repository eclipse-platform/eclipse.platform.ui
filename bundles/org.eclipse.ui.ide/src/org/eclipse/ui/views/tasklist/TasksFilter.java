/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 * 		Cagatay Kavukcuoglu <cagatayk@acm.org> - Filter for markers in same project
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IContainmentAdapter;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

class TasksFilter extends ViewerFilter implements Cloneable {

    public static final String[] ROOT_TYPES = new String[] { IMarker.PROBLEM,
            IMarker.TASK };

    // Filter on resource constants
    static final int ON_ANY_RESOURCE = 0;

    static final int ON_SELECTED_RESOURCE_ONLY = 1;

    static final int ON_SELECTED_RESOURCE_AND_CHILDREN = 2;

    static final int ON_ANY_RESOURCE_OF_SAME_PROJECT = 3; // added by cagatayk@acm.org

    static final int ON_WORKING_SET = 4;

    // Description filter kind constants
    static final int FILTER_CONTAINS = 0;

    static final int FILTER_DOES_NOT_CONTAIN = 1;

    //final static int MINIMUM_MARKER_LIMIT = 10;
    final static int DEFAULT_MARKER_LIMIT = 2000;

    //final static int MAXIMUM_MARKER_LIMIT = 20000;

    String[] types;

    int onResource;

    IWorkingSet workingSet;

    boolean filterOnDescription;

    int descriptionFilterKind;

    String descriptionFilter;

    boolean filterOnSeverity;

    int severityFilter;

    boolean filterOnPriority;

    int priorityFilter;

    boolean filterOnCompletion;

    int completionFilter;

    private boolean filterOnMarkerLimit = true;

    private int markerLimit = DEFAULT_MARKER_LIMIT;

    private static final String TAG_ID = "id"; //$NON-NLS-1$

    private static final String TAG_TYPE = "type"; //$NON-NLS-1$

    private static final String TAG_ON_RESOURCE = "onResource"; //$NON-NLS-1$

    private static final String TAG_WORKING_SET = "workingSet"; //$NON-NLS-1$	

    private static final String TAG_FILTER_ON_DESCRIPTION = "filterOnDescription"; //$NON-NLS-1$

    private static final String TAG_DESCRIPTION_FILTER_KIND = "descriptionFilterKind"; //$NON-NLS-1$

    private static final String TAG_DESCRIPTION_FILTER = "descriptionFilter"; //$NON-NLS-1$

    private static final String TAG_FILTER_ON_SEVERITY = "filterOnSeverity"; //$NON-NLS-1$

    private static final String TAG_SEVERITY_FILTER = "severityFilter"; //$NON-NLS-1$

    private static final String TAG_FILTER_ON_PRIORITY = "filterOnPriority"; //$NON-NLS-1$

    private static final String TAG_PRIORITY_FILTER = "priorityFilter"; //$NON-NLS-1$

    private static final String TAG_FILTER_ON_COMPLETION = "filterOnCompletion"; //$NON-NLS-1$

    private static final String TAG_COMPLETION_FILTER = "completionFilter"; //$NON-NLS-1$

    private static final String TAG_FILTER_ON_MARKER_LIMIT = "filterOnMarkerLimit"; //$NON-NLS-1$ 

    private static final String TAG_MARKER_LIMIT = "markerLimit"; //$NON-NLS-1$

    public TasksFilter() {
        reset();
    }

    boolean getFilterOnMarkerLimit() {
        return filterOnMarkerLimit;
    }

    void setFilterOnMarkerLimit(boolean filterOnMarkerLimit) {
        this.filterOnMarkerLimit = filterOnMarkerLimit;
    }

    int getMarkerLimit() {
        return markerLimit;
    }

    void setMarkerLimit(int markerLimit) {
        if (markerLimit < 1) {
            markerLimit = TasksFilter.DEFAULT_MARKER_LIMIT;
        }

        //if (markerLimit < TasksFilter.MINIMUM_MARKER_LIMIT) {
        //	markerLimit = TasksFilter.MINIMUM_MARKER_LIMIT;
        //} else if (markerLimit > TasksFilter.MAXIMUM_MARKER_LIMIT) {
        //	markerLimit = TasksFilter.MAXIMUM_MARKER_LIMIT;
        //} 

        this.markerLimit = markerLimit;
    }

    boolean checkDescription(String desc) {
        if (desc == null) { // be paranoid
            desc = ""; //$NON-NLS-1$
        }
        boolean contains = containsSubstring(desc, descriptionFilter);
        return descriptionFilterKind == FILTER_CONTAINS ? contains : !contains;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(); // shouldn't happen
        }
    }

    boolean containsSubstring(String string, String substring) {
        int strLen = string.length();
        int subLen = substring.length();
        int len = strLen - subLen;
        for (int i = 0; i <= len; ++i) {
            if (string.regionMatches(true, i, substring, 0, subLen)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if the given resource is enclosed by a working set element.
     * The IContainmentAdapter of each working set element is used for the
     * containment test. If there is no IContainmentAdapter for a working 
     * set element, a simple resource based test is used. 
     * 
     * @param element resource to test for enclosure by a working set
     * 	element 
     * @return true if element is enclosed by a working set element and 
     * 	false otherwise. 
     */
    private boolean isEnclosed(IResource element) {
        IPath elementPath = element.getFullPath();
        IAdaptable[] workingSetElements = workingSet.getElements();

        if (elementPath.isEmpty() || elementPath.isRoot()) {
            return false;
        }
        for (int i = 0; i < workingSetElements.length; i++) {
            IAdaptable workingSetElement = workingSetElements[i];
            IContainmentAdapter containmentAdapter = (IContainmentAdapter) workingSetElement
                    .getAdapter(IContainmentAdapter.class);

            // if there is no IContainmentAdapter defined for the working  
            // set element type fall back to using resource based  
            // containment check 
            if (containmentAdapter != null) {
                if (containmentAdapter.contains(workingSetElement, element,
                        IContainmentAdapter.CHECK_CONTEXT
                                | IContainmentAdapter.CHECK_IF_CHILD
                                | IContainmentAdapter.CHECK_IF_DESCENDANT)) {
					return true;
				}
            } else if (isEnclosedResource(element, elementPath,
                    workingSetElement)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns if the given resource is enclosed by a working set element.
     * A resource is enclosed if it is either a parent of a working set 
     * element, a child of a working set element or a working set element
     * itself.
     * Simple path comparison is used. This is only guaranteed to return
     * correct results for resource working set elements. 
     * 
     * @param element resource to test for enclosure by a working set
     * 	element
     * @param elementPath full, absolute path of the element to test 
     * @return true if element is enclosed by a working set element and 
     * 	false otherwise. 
     */
    private boolean isEnclosedResource(IResource element, IPath elementPath,
            IAdaptable workingSetElement) {
        IResource workingSetResource = null;

        if (workingSetElement.equals(element)) {
			return true;
		}
        if (workingSetElement instanceof IResource) {
            workingSetResource = (IResource) workingSetElement;
        } else {
            workingSetResource = (IResource) workingSetElement
                    .getAdapter(IResource.class);
        }
        if (workingSetResource != null) {
            IPath resourcePath = workingSetResource.getFullPath();
            if (resourcePath.isPrefixOf(elementPath)) {
				return true;
			}
        }
        return false;
    }

    public void reset() {
        types = ROOT_TYPES;
        onResource = ON_ANY_RESOURCE;
        filterOnDescription = false;
        descriptionFilter = ""; //$NON-NLS-1$
        filterOnSeverity = false;
        severityFilter = 0;
        filterOnPriority = false;
        priorityFilter = 0;
        filterOnCompletion = false;
        completionFilter = 0;
        filterOnMarkerLimit = true;
        markerLimit = DEFAULT_MARKER_LIMIT;
    }

    /**
     * @see IPersistable
     */
    public void restoreState(IMemento memento) {
        IMemento children[] = memento.getChildren(TAG_TYPE);
        types = new String[children.length];
        for (int i = 0; i < children.length; i++) {
            types[i] = children[i].getString(TAG_ID);
        }
        Integer ival = memento.getInteger(TAG_ON_RESOURCE);
        onResource = ival == null ? ON_ANY_RESOURCE : ival.intValue();
        restoreWorkingSet(memento.getString(TAG_WORKING_SET));
        ival = memento.getInteger(TAG_FILTER_ON_DESCRIPTION);
        filterOnDescription = ival != null && ival.intValue() == 1;
        ival = memento.getInteger(TAG_DESCRIPTION_FILTER_KIND);
        descriptionFilterKind = ival == null ? FILTER_CONTAINS : ival
                .intValue();
        descriptionFilter = memento.getString(TAG_DESCRIPTION_FILTER);
        if (descriptionFilter == null) {
			descriptionFilter = ""; //$NON-NLS-1$
		}
        ival = memento.getInteger(TAG_FILTER_ON_SEVERITY);
        filterOnSeverity = ival != null && ival.intValue() == 1;
        ival = memento.getInteger(TAG_SEVERITY_FILTER);
        severityFilter = ival == null ? 0 : ival.intValue();
        ival = memento.getInteger(TAG_FILTER_ON_PRIORITY);
        filterOnPriority = ival != null && ival.intValue() == 1;
        ival = memento.getInteger(TAG_PRIORITY_FILTER);
        priorityFilter = ival == null ? 0 : ival.intValue();
        ival = memento.getInteger(TAG_FILTER_ON_COMPLETION);
        filterOnCompletion = ival != null && ival.intValue() == 1;
        ival = memento.getInteger(TAG_COMPLETION_FILTER);
        completionFilter = ival == null ? 0 : ival.intValue();
        ival = memento.getInteger(TAG_FILTER_ON_MARKER_LIMIT);
        filterOnMarkerLimit = ival == null || ival.intValue() == 1;
        ival = memento.getInteger(TAG_MARKER_LIMIT);
        markerLimit = ival == null ? DEFAULT_MARKER_LIMIT : ival.intValue();
    }

    /**
     * Restores the saved working set, if any.
     * 
     * @param the saved working set name or null
     */
    private void restoreWorkingSet(String workingSetName) {
        if (workingSetName != null) {
            IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
                    .getWorkingSetManager();
            IWorkingSet workingSet = workingSetManager
                    .getWorkingSet(workingSetName);

            if (workingSet != null) {
                this.workingSet = workingSet;
            }
        }
    }

    /**
     * Saves the object state within a memento.
     *
     * @param memento a memento to receive the object state
     */
    public void saveState(IMemento memento) {
        for (int i = 0; i < types.length; i++) {
            memento.createChild(TAG_TYPE).putString(TAG_ID, types[i]);
        }
        memento.putInteger(TAG_ON_RESOURCE, onResource);
        if (workingSet != null) {
            memento.putString(TAG_WORKING_SET, workingSet.getName());
        }
        memento.putInteger(TAG_FILTER_ON_DESCRIPTION, filterOnDescription ? 1
                : 0);
        memento.putInteger(TAG_DESCRIPTION_FILTER_KIND, descriptionFilterKind);
        memento.putString(TAG_DESCRIPTION_FILTER, descriptionFilter);
        memento.putInteger(TAG_FILTER_ON_SEVERITY, filterOnSeverity ? 1 : 0);
        memento.putInteger(TAG_SEVERITY_FILTER, severityFilter);
        memento.putInteger(TAG_FILTER_ON_PRIORITY, filterOnPriority ? 1 : 0);
        memento.putInteger(TAG_PRIORITY_FILTER, priorityFilter);
        memento
                .putInteger(TAG_FILTER_ON_COMPLETION, filterOnCompletion ? 1
                        : 0);
        memento.putInteger(TAG_COMPLETION_FILTER, completionFilter);
        memento.putInteger(TAG_FILTER_ON_MARKER_LIMIT, filterOnMarkerLimit ? 1
                : 0);
        memento.putInteger(TAG_MARKER_LIMIT, markerLimit);
    }

    public boolean select(Viewer viewer, Object parentElement, Object element) {
        return select((IMarker) element);
    }

    public boolean select(IMarker marker) {
        // resource settings are handled by the content provider
        return selectByType(marker) && selectByAttributes(marker)
                && selectByWorkingSet(marker);
    }

    public boolean select(IMarkerDelta markerDelta) {
        // resource settings are handled by the content provider
        return selectByType(markerDelta) && selectByAttributes(markerDelta)
                && selectByWorkingSet(markerDelta);
    }

    private boolean selectByType(IMarker marker) {
        for (int i = 0; i < types.length; ++i) {
            if (MarkerUtil.isMarkerType(marker, types[i])) {
				return true;
			}
        }
        return false;
    }

    private boolean selectByType(IMarkerDelta markerDelta) {
        for (int i = 0; i < types.length; ++i) {
            if (markerDelta.isSubtypeOf(types[i])) {
				return true;
			}
        }
        return false;
    }

    /**
     * Returns whether the specified marker should be filter out or not.
     * 
     * @param marker the marker to test
     * @return 
     * 	true=the marker should not be filtered out
     * 	false=the marker should be filtered out
     */
    private boolean selectByWorkingSet(IMarker marker) {
        if (workingSet == null || onResource != ON_WORKING_SET) {
            return true;
        }
        IResource resource = marker.getResource();
        if (resource != null) {
            return isEnclosed(resource);
        }
        return false;
    }

    /**
     * Returns whether the specified marker delta should be filter out 
     * or not.
     * 
     * @param markerDelta the marker delta to test
     * @return 
     * 	true=the marker delta should not be filtered out
     * 	false=the marker delta should be filtered out
     */
    private boolean selectByWorkingSet(IMarkerDelta markerDelta) {
        if (workingSet == null || onResource != ON_WORKING_SET) {
            return true;
        }
        IResource resource = markerDelta.getResource();
        if (resource != null) {
            return isEnclosed(resource);
        }
        return false;
    }

    /* 
     * WARNING: selectByAttributes(IMarker) and selectByAttributes(IMarkerDelta) must correspond.
     */

    private boolean selectByAttributes(IMarker marker) {

        // severity filter applies only to problems
        if (filterOnSeverity
                && MarkerUtil.isMarkerType(marker, IMarker.PROBLEM)) {
            int sev = MarkerUtil.getSeverity(marker);
            if ((severityFilter & (1 << sev)) == 0) {
				return false;
			}
        }

        // priority and completion filters apply only to tasks
        // avoid doing type check more than once
        if ((filterOnPriority || filterOnCompletion)
                && MarkerUtil.isMarkerType(marker, IMarker.TASK)) {
            if (filterOnPriority) {
                int pri = MarkerUtil.getPriority(marker);
                if ((priorityFilter & (1 << pri)) == 0) {
					return false;
				}
            }
            if (filterOnCompletion) {
                boolean complete = MarkerUtil.isComplete(marker);
                if ((completionFilter & (complete ? 2 : 1)) == 0) {
					return false;
				}
            }
        }

        // description applies to all markers
        if (filterOnDescription) {
            String desc = MarkerUtil.getMessage(marker);
            if (!checkDescription(desc)) {
				return false;
			}
        }
        return true;
    }

    private boolean selectByAttributes(IMarkerDelta markerDelta) {

        // severity filter applies only to problems
        if (filterOnSeverity && markerDelta.isSubtypeOf(IMarker.PROBLEM)) {
            int sev = markerDelta.getAttribute(IMarker.SEVERITY,
                    IMarker.SEVERITY_WARNING);
            if ((severityFilter & (1 << sev)) == 0) {
				return false;
			}
        }

        // priority and completion filters apply only to tasks
        // avoid doing type check more than once
        if ((filterOnPriority || filterOnCompletion)
                && markerDelta.isSubtypeOf(IMarker.TASK)) {
            if (filterOnPriority) {
                int pri = markerDelta.getAttribute(IMarker.PRIORITY,
                        IMarker.PRIORITY_NORMAL);
                if ((priorityFilter & (1 << pri)) == 0) {
					return false;
				}
            }
            if (filterOnCompletion) {
                boolean complete = markerDelta
                        .getAttribute(IMarker.DONE, false);
                if ((completionFilter & (complete ? 2 : 1)) == 0) {
					return false;
				}
            }
        }

        // description applies to all markers
        if (filterOnDescription) {
            String desc = markerDelta.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
            if (!checkDescription(desc)) {
				return false;
			}
        }
        return true;
    }

    /**
     * Returns whether the filter is including all markers.
     *
     * @return <code>true</code> if the filter includes all markers, <code>false</code> if not
     */
    public boolean isShowingAll() {
        if (filterOnDescription || filterOnSeverity || filterOnPriority
                || filterOnCompletion) {
            return false;
        }
        if (onResource != ON_ANY_RESOURCE) {
            return false;
        }

        HashSet set = new HashSet(Arrays.asList(types));
        if (set.size() != ROOT_TYPES.length) {
            return false;
        }
        for (int i = 0; i < ROOT_TYPES.length; ++i) {
            if (!set.contains(ROOT_TYPES[i])) {
                return false;
            }
        }
        return true;
    }
}
