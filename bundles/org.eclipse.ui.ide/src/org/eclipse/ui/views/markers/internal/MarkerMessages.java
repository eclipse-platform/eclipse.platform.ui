/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.osgi.util.NLS;

/**
 * MarkerMessages is the class that handles the messages for the markers.
 * 
 */
public class MarkerMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ui.views.markers.internal.messages"; //$NON-NLS-1$

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, MarkerMessages.class);
	}

	public static String sortAction_title;
	public static String filtersAction_title;
	public static String filtersAction_tooltip;
	public static String filtersSubMenu_title;

	public static String sortDialog_title;
	public static String sortDialog_label;
	public static String sortDialog_columnLabel;

	public static String sortDirectionAscending_text;
	public static String sortDirectionAscending_text2;
	public static String sortDirectionAscending_text3;
	public static String sortDirectionAscending_text4;

	public static String sortDirectionDescending_text;
	public static String sortDirectionDescending_text2;
	public static String sortDirectionDescending_text3;
	public static String sortDirectionDescending_text4;

	public static String restoreDefaults_text;

	public static String Error;
	public static String Unknown;

	public static String description_message;
	public static String description_resource;
	public static String description_folder;
	public static String description_lineNumber;
	public static String description_creationTime;
	public static String description_markerId;
	public static String description_type;

	public static String label_lineNumber;

	public static String openAction_title;
	public static String copyAction_title;
	public static String pasteAction_title;
	public static String deleteAction_title;
	public static String deleteAction_tooltip;
	public static String selectAllAction_title;
	public static String selectAllAction_calculating;
	public static String selectAllAction_applying;
	public static String propertiesAction_title;
	
	public static String deleteActionConfirmTitle;
	public static String deleteActionConfirmMessage;

	public static String filtersDialog_title;
	public static String configureFiltersCommand_title;
	public static String configureFiltersDialog_title;
	
	public static String configEditDialog_name;

	public static String filtersDialog_showItemsOfType;
	public static String filtersDialog_anyResource;
	public static String filtersDialog_anyResourceInSameProject;
	public static String filtersDialogDeselectedFiltersTitle;
	public static String filtersDialogDeselectedFiltersMessage;
	public static String filtersDialog_selectedResource;
	public static String filtersDialog_selectedAndChildren;
	public static String filtersDialog_workingSet;
	public static String filtersDialog_workingSetSelect;
	public static String filtersDialog_noWorkingSet;
	public static String filtersDialog_currentWorkingSet;
	public static String filtersDialog_selectAll;
	public static String filtersDialog_deselectAll;
	public static String filtersDialog_selectAllTypes;
	public static String filtersDialog_deselectAllTypes;
	public static String filtersDialog_descriptionLabel;
	public static String filtersDialog_contains;
	public static String filtersDialog_doesNotContain;
	public static String filtersDialog_entriesTitle;
	public static String filtersDialog_severityLabel;
	public static String filtersDialog_filterOnSeverity;
	public static String filtersDialog_severityError;
	public static String filtersDialog_severityWarning;
	public static String filtersDialog_severityInfo;
	public static String filtersDialog_priorityLabel;
	public static String filtersDialog_priorityHigh;
	public static String filtersDialog_priorityNormal;
	public static String filtersDialog_priorityLow;
	public static String filtersDialog_statusLabel;
	public static String filtersDialog_statusComplete;
	public static String filtersDialog_statusIncomplete;
	public static String filtersDialog_conflictingName;

	public static String filtersDialog_scopeTitle;
	public static String filtersDialog_typesTitle;
	public static String filtersDialog_completionTitle;
	public static String filtersDialog_priorityTitle;

	public static String propertiesDialog_creationTime_text;
	public static String propertiesDialog_description_text;
	public static String propertiesDialog_folder_text;
	public static String propertiesDialog_location_text;
	public static String propertiesDialog_resource_text;
	public static String propertiesDialog_title;
	public static String propertiesDialog_severityLabel;
	public static String propertiesDialog_errorLabel;
	public static String propertiesDialog_warningLabel;
	public static String propertiesDialog_infoLabel;
	public static String propertiesDialog_noseverityLabel;
	public static String propertiesDialog_priority;
	public static String propertiesDialog_priorityHigh;
	public static String propertiesDialog_priorityNormal;
	public static String propertiesDialog_priorityLow;
	public static String propertiesDialog_completed;

	public static String filter_matchedMessage;
	public static String filter_itemsMessage;
	public static String problem_filter_matchedMessage;

	public static String errorModifyingBookmark;
	public static String errorModifyingTask;

	public static String problemSeverity_description;
	public static String problem_statusSummaryBreakdown;
	public static String marker_statusSummarySelected;
	public static String marker_statusSelectedCount;
	public static String errorsAndWarningsSummaryBreakdown;

	public static String deleteMarkers_errorMessage;
	public static String deleteMarker_operationName;
	public static String deleteMarkers_operationName;
	public static String deleteBookmarkMarker_operationName;
	public static String deleteBookmarkMarkers_operationName;
	public static String deleteTaskMarker_operationName;
	public static String deleteTaskMarkers_operationName;
	public static String deleteProblemMarker_operationName;
	public static String deleteProblemMarkers_operationName;


	public static String deleteCompletedAction_title;

	public static String markCompletedAction_title;
	public static String markCompletedHandler_task;

	public static String resolveMarkerAction_title;
	public static String resolveMarkerAction_dialogTitle;
	public static String resolveMarkerAction_computationAction;
	public static String resolveMarkerAction_computationManyAction;

	public static String deleteCompletedTasks_dialogTitle;
	public static String deleteCompletedTasks_noneCompleted;
	public static String deleteCompletedTasks_permanentPlural;
	public static String deleteCompletedTasks_permanentSingular;
	public static String deleteCompletedTasks_errorMessage;

	public static String addGlobalTaskAction_title;
	public static String addGlobalTaskAction_tooltip;

	public static String addGlobalTaskDialog_title;

	public static String completion_description;
	public static String priority_description;
	public static String severity_description;

	public static String priority_high;
	public static String priority_normal;
	public static String priority_low;

	public static String CopyToClipboardProblemDialog_title;
	public static String CopyToClipboardProblemDialog_message;

	public static String MarkerFilter_searching;

	public static String MarkerView_waiting_on_changes;
	public static String MarkerView_searching_for_markers;
	public static String MarkerView_refreshing_counts;
	public static String MarkerView_queueing_updates;
	public static String MarkerView_processUpdates;

	public static String MarkerView_18;
	public static String MarkerView_19;
	public static String SortUtil_finding_first;
	public static String SortUtil_partitioning;
	public static String OpenMarker_errorTitle;
	public static String PasteMarker_errorTitle;
	public static String RemoveMarker_errorTitle;

	public static String MarkerFilter_defaultFilterName;
	public static String MarkerFilter_newFilterName;
	public static String MarkerFilter_filtersTitle;
	public static String MarkerFilter_addFilterName;
	public static String MarkerFilter_cloneFilterName;
	public static String MarkerFilter_editFilterName;
	public static String MarkerFilter_deleteSelectedName;
	public static String MarkerFilter_renameName;
	public static String MarkerFilter_showAllCommand_title;
	public static String MarkerFilter_ConfigureContentsCommand_title;
	
	public static String MarkerFilterDialog_title;
	public static String MarkerFilterDialog_message;
	public static String MarkerFilterDialog_emptyMessage;
	public static String MarkerFilterDialog_YouHaveDisabledMarkerLimit;
	public static String MarkerFilterDialog_errorTitle;
	public static String MarkerFilterDialog_failedFilterMessage;

	public static String MarkerPreferences_DialogTitle;
	public static String MarkerPreferences_MarkerLimits;
	public static String MarkerPreferences_VisibleItems;
	public static String MarkerPreferences_MoveLeft;
	public static String MarkerPreferences_MoveRight;
	public static String MarkerPreferences_ColumnGroupTitle;
	public static String MarkerPreferences_VisibleColumnsTitle;
	public static String MarkerPreferences_HiddenColumnsTitle;
	public static String MarkerPreferences_AtLeastOneVisibleColumn;
	public static String MarkerPreferences_WidthOfShownColumn;
	
	public static String ProblemFilterDialog_System_Filters_Title;
	public static String ProblemFilterDialog_All_Problems;
	public static String ProblemFilterDialog_Selected_Types;
	public static String ProblemFilterDialog_Info_Severity;
	public static String ProblemFilterDialog_Warning_Severity;
	public static String ProblemFilterDialog_Error_Severity;
	public static String ProblemFilterDialog_Contains_Description;
	public static String ProblemFilterDialog_Does_Not_Contain_Description;
	public static String ProblemFilterDialog_any;
	public static String ProblemFilterDialog_sameContainer;
	public static String ProblemFilterDialog_selectedAndChildren;
	public static String ProblemFilterDialog_selected;
	public static String ProblemFilterDialog_workingSet;

	public static String ProblemFilterRegistry_nullType;

	public static String FieldMessage_NullMessage;
	public static String FieldCategory_Uncategorized;
	public static String FieldMessage_WrongType;
	public static String FiltersConfigurationDialog_title;
	public static String FiltersConfigurationDialog_message;
	public static String Category_Label;
	public static String Category_Limit_Label;
	public static String Category_One_Item_Label;
	public static String Category_building;

	public static String MarkerResolutionDialog_Fixing;
	public static String MarkerResolutionDialog_Description;
	public static String MarkerResolutionDialog_Problems_List_Title;
	public static String MarkerResolutionDialog_Problems_List_Resource;
	public static String MarkerResolutionDialog_Problems_List_Location;
	public static String MarkerResolutionDialog_Resolutions_List_Title;
	public static String MarkerResolutionDialog_CannotFixTitle;
	public static String MarkerResolutionDialog_CannotFixMessage;
	public static String MarkerResolutionDialog_NoResolutionsFound;
	public static String MarkerResolutionDialog_NoResolutionsFoundForMultiSelection;

	public static String MarkerResolutionDialog_Title;
	public static String MarkerResolutionDialog_CalculatingTask;
	public static String MarkerResolutionDialog_WorkingSubTask;

	public static String MarkerResolutionDialog_AddOthers;

	public static String ProblemView_GroupByMenu;
	public static String ProblemView_Type;
	public static String ProblemView_None;
	public static String ProblemView_UpdateCategoryJob;

	public static String Util_ProjectRoot;
	public static String Util_WorkspaceRoot;

	public static String DialogMarkerProperties_CreateMarker;
	public static String DialogMarkerProperties_ModifyMarker;
	public static String DialogMarkerProperties_Create;
	public static String DialogMarkerProperties_Modify;

	public static String DialogTaskProperties_CreateTask;

	public static String modifyBookmark_title;
	public static String modifyTask_title;

	public static String qualifiedMarkerCommand_title;
	public static String task_title;
	public static String problem_title;
	public static String bookmark_title;

	public static String PasteHandler_title;

	public static String ALL_Title;
	public static String AND_Title;
	public static String OR_Title;
	public static String MarkerConfigurationsLabel;

	public static String ContentGenerator_NoGrouping;
	public static String newViewTitle;
	public static String NewViewHandler_dialogTitle;
	public static String NewViewHandler_dialogMessage;

}
