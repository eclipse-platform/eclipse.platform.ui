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

import org.eclipse.ui.IMemento;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import java.util.Arrays;
import java.util.HashSet;

/* package */ class TasksFilter extends ViewerFilter implements Cloneable {

	public static final String[] ROOT_TYPES = new String[] { IMarker.PROBLEM, IMarker.TASK };

	// Filter on resource constants
	static final int ON_ANY_RESOURCE = 0;
	static final int ON_SELECTED_RESOURCE_ONLY = 1;
	static final int ON_SELECTED_RESOURCE_AND_CHILDREN = 2;
	static final int ON_ANY_RESOURCE_OF_SAME_PROJECT = 3; // added by cagatayk@acm.org

	// Description filter kind constants
	static final int FILTER_CONTAINS = 0;
	static final int FILTER_DOES_NOT_CONTAIN = 1;
	
	String[] types;
	int onResource;
	boolean filterOnDescription;
	int descriptionFilterKind;
	String descriptionFilter;
	boolean filterOnSeverity;
	int severityFilter;
	boolean filterOnPriority;
	int priorityFilter;
	boolean filterOnCompletion;
	int completionFilter;

	private static final String TAG_ID = "id"; //$NON-NLS-1$
	private static final String TAG_TYPE = "type";  //$NON-NLS-1$
	private static final String TAG_ON_RESOURCE = "onResource"; //$NON-NLS-1$
	private static final String TAG_FILTER_ON_DESCRIPTION = "filterOnDescription"; //$NON-NLS-1$
	private static final String TAG_DESCRIPTION_FILTER_KIND = "descriptionFilterKind"; //$NON-NLS-1$
	private static final String TAG_DESCRIPTION_FILTER = "descriptionFilter"; //$NON-NLS-1$
	private static final String TAG_FILTER_ON_SEVERITY = "filterOnSeverity"; //$NON-NLS-1$
	private static final String TAG_SEVERITY_FILTER = "severityFilter"; //$NON-NLS-1$
	private static final String TAG_FILTER_ON_PRIORITY = "filterOnPriority"; //$NON-NLS-1$
	private static final String TAG_PRIORITY_FILTER = "priorityFilter"; //$NON-NLS-1$
	private static final String TAG_FILTER_ON_COMPLETION = "filterOnCompletion"; //$NON-NLS-1$
	private static final String TAG_COMPLETION_FILTER = "completionFilter"; //$NON-NLS-1$
	
public TasksFilter() {
	reset();
}
boolean checkDescription(IMarker marker) {
	String msg = MarkerUtil.getMessage(marker);
	boolean contains = containsSubstring(msg, descriptionFilter);
	return descriptionFilterKind == FILTER_CONTAINS ? contains : !contains;
}
public Object clone() {
	try {
		return super.clone();
	}
	catch (CloneNotSupportedException e) {
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
}
/**
 * @see IPersistable
 */
public void restoreState(IMemento memento) {
	IMemento children[] = memento.getChildren(TAG_TYPE);
	types = new String[children.length];
	for (int i = 0; i < children.length; i++){
		types[i] = children[i].getString(TAG_ID);
	}
	Integer ival = memento.getInteger(TAG_ON_RESOURCE);
	onResource = ival == null ? ON_ANY_RESOURCE : ival.intValue();
	ival = memento.getInteger(TAG_FILTER_ON_DESCRIPTION);
	filterOnDescription = ival != null && ival.intValue() == 1;
	ival = memento.getInteger(TAG_DESCRIPTION_FILTER_KIND);
	descriptionFilterKind = ival == null ? FILTER_CONTAINS : ival.intValue();
	descriptionFilter = memento.getString(TAG_DESCRIPTION_FILTER);
	if (descriptionFilter == null)
		descriptionFilter = ""; //$NON-NLS-1$
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
}
/**
 * Saves the object state within a memento.
 *
 * @param memento a memento to receive the object state
 */
public void saveState(IMemento memento) {
	for (int i = 0; i < types.length; i++){
		memento.createChild(TAG_TYPE).putString(TAG_ID,types[i]);
	}
	memento.putInteger(TAG_ON_RESOURCE,onResource);
	memento.putInteger(TAG_FILTER_ON_DESCRIPTION,filterOnDescription?1:0);
	memento.putInteger(TAG_DESCRIPTION_FILTER_KIND,descriptionFilterKind);
	memento.putString(TAG_DESCRIPTION_FILTER,descriptionFilter);
	memento.putInteger(TAG_FILTER_ON_SEVERITY,filterOnSeverity?1:0);
	memento.putInteger(TAG_SEVERITY_FILTER,severityFilter);
	memento.putInteger(TAG_FILTER_ON_PRIORITY,filterOnPriority?1:0);
	memento.putInteger(TAG_PRIORITY_FILTER,priorityFilter);
	memento.putInteger(TAG_FILTER_ON_COMPLETION,filterOnCompletion?1:0);
	memento.putInteger(TAG_COMPLETION_FILTER,completionFilter);
}

public boolean select(Viewer viewer, Object parentElement, Object element) {
	return select((IMarker) element);
}
	
public boolean select(IMarker marker) {

	// types and resource settings are handled by the content provider

	// severity filter applies only to problems
	if (filterOnSeverity && MarkerUtil.isMarkerType(marker, IMarker.PROBLEM)) {
		int bit = 1 << MarkerUtil.getSeverity(marker);
		if ((severityFilter & bit) == 0)
			return false;
	}

	// priority and completion filters apply only to tasks
	// avoid doing type check more than once
	if ((filterOnPriority || filterOnCompletion) && MarkerUtil.isMarkerType(marker, IMarker.TASK)) {
		if (filterOnPriority) {
			int bit = 1 << MarkerUtil.getPriority(marker);
			if ((priorityFilter & bit) == 0)
				return false;
		}
		if (filterOnCompletion) {
			int bit = MarkerUtil.isComplete(marker) ? 2 : 1;
			if ((completionFilter & bit) == 0)
				return false;
		}
	}

	// description applies to all markers
	if (filterOnDescription) {
		if (!checkDescription(marker))
			return false;
	}
	return true;
}
/**
 * Returns whether the filter is including all markers.
 *
 * @return <code>true</code> if the filter includes all markers, <code>false</code> if not
 */
public boolean showAll() {
	if (filterOnDescription || filterOnSeverity || filterOnPriority || filterOnCompletion) {
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
