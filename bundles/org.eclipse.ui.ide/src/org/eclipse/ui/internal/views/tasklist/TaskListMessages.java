/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.tasklist;

import org.eclipse.osgi.util.NLS;

/**
 * TaskListMessages are the messages used in the TaskList.
 *
 */
public class TaskListMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.views.tasklist.messages";//$NON-NLS-1$

	// ==============================================================================
	// TaskList
	// ==============================================================================

	public static String TaskList_line;
	public static String TaskList_lineAndLocation;

	public static String TaskList_statusSummaryVisible;
	public static String TaskList_statusSummarySelected;
	public static String TaskList_statusSummaryBreakdown;
	public static String TaskList_titleSummaryUnfiltered;
	public static String TaskList_titleSummaryFiltered;
	public static String TaskList_headerIcon;
	public static String TaskList_headerCompleted;
	public static String TaskList_headerPriority;
	public static String TaskList_headerDescription;
	public static String TaskList_headerResource;
	public static String TaskList_headerFolder;
	public static String TaskList_headerLocation;

	public static String TaskList_high;
	public static String TaskList_low;
	public static String TaskList_normal;

	public static String TaskList_errorModifyingTask;

	public static String TaskList_reportKind;
	public static String TaskList_reportStatus;
	public static String TaskList_reportPriority;

	public static String TaskList_task;
	public static String TaskList_error;
	public static String TaskList_warning;
	public static String TaskList_info;

	public static String TaskList_completed;
	public static String TaskList_notCompleted;

	public static String TaskList_markerLimitExceeded;

	// --- Actions ---
	public static String NewTask_text;
	public static String NewTask_tooltip;
	public static String NewTask_notShownTitle;
	public static String NewTask_notShownMsg;

	public static String CopyTask_text;
	public static String CopyTask_tooltip;

	public static String PasteTask_text;
	public static String PasteTask_tooltip;
	public static String PasteTask_errorMessage;

	public static String RemoveTask_text;
	public static String RemoveTask_tooltip;
	public static String RemoveTask_undoText;
	public static String RemoveTask_errorMessage;

	public static String Filters_text;
	public static String Filters_tooltip;

	public static String SortByMenu_text;
	public static String SortByCategory_text;
	public static String SortByCategory_tooltip;
	public static String SortByCompleted_text;
	public static String SortByCompleted_tooltip;
	public static String SortByPriority_text;
	public static String SortByPriority_tooltip;
	public static String SortByDescription_text;
	public static String SortByDescription_tooltip;
	public static String SortByResource_text;
	public static String SortByResource_tooltip;
	public static String SortByContainer_text;
	public static String SortByContainer_tooltip;
	public static String SortByLocation_text;
	public static String SortByLocation_tooltip;
	public static String SortByCreationTime_text;
	public static String SortByCreationTime_tooltip;
	public static String SortAscending_text;
	public static String SortAscending_tooltip;
	public static String SortDescending_text;
	public static String SortDescending_tooltip;

	public static String GotoTask_text;
	public static String GotoTask_tooltip;
	public static String GotoTask_errorMessage;

	public static String PurgeCompleted_text;
	public static String PurgeCompleted_tooltip;
	public static String PurgeCompleted_title;
	public static String PurgeCompleted_noneCompleted;
	public static String PurgeCompleted_permanent;
	public static String PurgeCompleted_errorMessage;

	public static String MarkCompleted_text;
	public static String MarkCompleted_tooltip;

	public static String SelectAll_text;
	public static String SelectAll_tooltip;

	public static String Resolve_text;
	public static String Resolve_tooltip;
	public static String Resolve_title;
	public static String Resolve_noResolutionsLabel;

	public static String Properties_text;
	public static String Properties_tooltip;

	// --- Filter Dialog ---
	public static String TaskList_filter;
	public static String TaskList_showItemsOfType;

	public static String TaskList_anyResource;
	public static String TaskList_anyResourceInSameProject;
	public static String TaskList_selectedResource;
	public static String TaskList_selectedAndChildren;
	public static String TaskList_workingSet;
	public static String TaskList_workingSetSelect;
	public static String TaskList_noWorkingSet;

	public static String TaskList_whereDescription;
	public static String TaskList_contains;
	public static String TaskList_doesNotContain;

	public static String TaskList_severity_label;
	public static String TaskList_severity_error;
	public static String TaskList_severity_warning;
	public static String TaskList_severity_info;

	public static String TaskList_priority_label;
	public static String TaskList_priority_high;
	public static String TaskList_priority_low;
	public static String TaskList_priority_normal;

	public static String TaskList_status_label;
	public static String TaskList_status_completed;
	public static String TaskList_status_notCompleted;

	public static String TaskList_resetText;

	public static String TaskList_limitVisibleTasksTo;
	public static String TaskList_titleMarkerLimitInvalid;
	public static String TaskList_messageMarkerLimitInvalid;
	public static String TaskPropertiesDialog_WorkingOnMarker;
	public static String TaskPropertiesDialog_CreatingMarker;

	// --- Properties Dialog ---
	public static String TaskProp_newTaskTitle;
	public static String TaskProp_propertiesTitle;
	//TaskProp.titleFmt = {0} - {1}
	public static String TaskProp_description;
	public static String TaskProp_creationTime;
	public static String TaskProp_priority;
	public static String TaskProp_completed;
	public static String TaskProp_severity;
	public static String TaskProp_onResource;
	public static String TaskProp_inFolder;
	public static String TaskProp_location;
	public static String TaskProp_errorMessage;

	public static String CopyToClipboardProblemDialog_title;
	public static String CopyToClipboardProblemDialog_message;

	public static String TaskPropertiesDialog_UpdatingAttributes;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, TaskListMessages.class);
	}
}
