/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.team.internal.ui;

import org.eclipse.osgi.util.NLS;

public class TeamUIMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.team.internal.ui.messages";//$NON-NLS-1$

    public static String information;
    
	public static String CatchupReleaseViewer_open;
	public static String CatchupReleaseViewer_expand;
	public static String CatchupReleaseViewer_ignoreWhiteSpace;
	public static String CatchupReleaseViewer_refreshAction;
	public static String CatchupReleaseViewer_showIncomingAction;
	public static String CatchupReleaseViewer_showInNavigator;
	public static String CatchupReleaseViewer_showOnlyConflictsAction;
	public static String CatchupReleaseViewer_showOutgoingAction;
	public static String CatchupReleaseViewer_removeFromView;
	public static String CatchupReleaseViewer_copyAllRightToLeft;
	public static String CatchupReleaseViewer_Compare_File_Contents_1;
	public static String CatchupReleaseViewer_Copying_right_contents_into_workspace_2;
	public static String CatchupReleaseViewer_MakingLocalLikeRemote;

	public static String ConfigureProjectAction_configureProject;
	public static String ConfigureProjectWizard_configureProject;
	public static String ConfigureSynchronizeScheduleComposite_0;
	public static String ConfigureSynchronizeScheduleComposite_1;
	public static String ConfigureProjectWizard_description;
	public static String ConfigureProjectWizard_title;
	public static String ConfigureProjectWizardMainPage_selectRepository;

	public static String ConfigurationWizard_exceptionCreatingWizard;

	public static String IgnorePreferencePage_description;
	public static String IgnorePreferencePage_add;
	public static String IgnorePreferencePage_enterPatternLong;
	public static String IgnorePreferencePage_enterPatternShort;
	public static String IgnorePreferencePage_ignorePatterns;
	public static String IgnorePreferencePage_patternExistsLong;
	public static String IgnorePreferencePage_patternExistsShort;
	public static String IgnorePreferencePage_remove;

	public static String MergeResource_commonFile;
	public static String MergeResource_noCommonFile;
	public static String MergeResource_noRepositoryFile;
	public static String MergeResource_repositoryFile;
	public static String MergeResource_workspaceFile;

	public static String nothingToSynchronize;
	public static String simpleInternal;
	public static String exception;

	public static String SyncCompareInput_nothingText;
	public static String SyncCompareInput_refresh;
	public static String SyncCompareInput_synchronize;
	public static String SyncCompareInput_taskTitle;

	public static String SyncView_freeModeAction;
	public static String SyncView_freeModeTitle;
	public static String SyncView_freeModeToolTip;
	public static String SyncView_incomingModeAction;
	public static String SyncView_incomingModeTitle;
	public static String SyncView_incomingModeToolTip;
	public static String SyncView_outgoingModeAction;
	public static String SyncView_outgoingModeTitle;
	public static String SyncView_outgoingModeToolTip;
	public static String SyncView_same;
	public static String SyncView_text;
	public static String SyncView_unableSynchronize;
	public static String SyncView_saveTitle;
	public static String SyncView_saveMessage;
	public static String SyncView_errorSaving;
	public static String SyncView_cantSaveError;
	public static String SyncView_dirtyIndicatorInTitle;

	public static String LiveSyncView_titleTooltip;
	public static String LiveSyncView_title;
	public static String LiveSyncView_titleSubscriber;

	public static String SyncInfoCompareInput_localLabel;
	public static String SyncInfoCompareInput_localLabelExists;
	public static String SyncInfoCompareInput_remoteLabel;
	public static String SyncInfoCompareInput_baseLabel;
	public static String SyncInfoCompareInput_remoteLabelExists;
	public static String SyncInfoCompareInput_baseLabelExists;
	public static String SyncInfoCompareInput_title;
	public static String SyncInfoCompareInput_0;
	public static String SyncInfoCompareInput_1;
	public static String SyncInfoCompareInput_2;
	public static String SyncInfoCompareInput_3;
	public static String SyncInfoCompareInput_tooltip;

	public static String TeamAction_internal;
	public static String TeamFile_saveChanges;
	public static String TeamFile_modified;

	public static String TextPreferencePage_add;
	public static String TextPreferencePage_0;
	public static String TextPreferencePage_2;
	public static String TextPreferencePage_3;
	public static String TextPreferencePage_5;
	public static String TextPreferencePage_6;
	public static String TextPreferencePage_7;
	public static String TextPreferencePage_1;
	public static String TextPreferencePage_binary;
	public static String TextPreferencePage_change;
	public static String TextPreferencePage_contents;
	public static String TextPreferencePage_description;
	public static String TextPreferencePage_enterExtensionLong;
	public static String TextPreferencePage_enterExtensionShort;
	public static String TextPreferencePage_extension;
	public static String TextPreferencePage_extensionExistsLong;
	public static String TextPreferencePage_extensionExistsShort;
	public static String TextPreferencePage_remove;
	public static String TextPreferencePage_text;

	public static String TextPreferencePage_columnExtension;
	public static String TextPreferencePage_columnContents;

	public static String SynchronizationViewPreference_defaultPerspectiveNone;
	public static String SynchronizationViewPreference_defaultPerspectiveDescription;
	public static String SynchronizationViewPreference_defaultPerspectiveLabel;

	public static String ScheduledSyncViewRefresh_taskName;
	public static String SyncViewRefresh_taskName;

	public static String ExportProjectSetMainPage_Select_the_projects_to_include_in_the_project_set__2;
	public static String ExportProjectSetMainPage_Project_Set_File_Name__3;
	public static String ExportProjectSetMainPage_Browse_4;
	public static String ExportProjectSetMainPage_You_have_specified_a_folder_5;

	public static String ImportProjectSetMainPage_Project_Set_File_Name__2;
	public static String ImportProjectSetMainPage_Browse_3;
	public static String ImportProjectSetMainPage_The_specified_file_does_not_exist_4;
	public static String ImportProjectSetMainPage_You_have_specified_a_folder_5;
	public static String ImportProjectSetMainPage_workingSetNameEmpty;
	public static String ImportProjectSetMainPage_workingSetNameExists;
	public static String ImportProjectSetMainPage_createWorkingSetLabel;
	public static String ImportProjectSetMainPage_workingSetLabel;

	public static String ProjectSetContentHandler_Element_provider_must_be_contained_in_element_psf_4;
	public static String ProjectSetContentHandler_Element_project_must_be_contained_in_element_provider_7;

	public static String ProjectSetExportWizard_Project_Set_1;
	public static String ProjectSetExportWizard_Export_a_Project_Set_3;
	public static String ProjectSetExportWizard_Question_4;
	public static String ProjectSetExportWizard_Target_directory_does_not_exist__Would_you_like_to_create_it__5;
	public static String ProjectSetExportWizard_Export_Problems_6;
	public static String ProjectSetExportWizard_An_error_occurred_creating_the_target_directory_7;
	public static String ProjectSetExportWizard_Question_8;
	public static String ProjectSetExportWizard_Target_already_exists__Would_you_like_to_overwrite_it__9;
	public static String ProjectSetImportWizard_Project_Set_1;
	public static String ProjectSetImportWizard_0;
	public static String ProjectSetImportWizard_2;
	public static String ProjectSetImportWizard_1;
	public static String ProjectSetImportWizard_3;
	public static String ProjectSetImportWizard_Import_a_Project_Set_3;
	public static String ProjectSetImportWizard_workingSetExistsTitle;
	public static String ProjectSetImportWizard_workingSetExistsMessage;

	public static String ExportProjectSetMainPage_Project_Set_Files_3;
	public static String ExportProjectSetMainPage_default;
	public static String ImportProjectSetMainPage_allFiles;
	public static String ImportProjectSetMainPage_Project_Set_Files_2;
	public static String ExportProjectSetMainPage__File_name__1;
	public static String TeamPreferencePage_General_settings_for_Team_support_1;
	public static String TeamPreferencePage__Use_Incoming_Outgoing_mode_when_synchronizing_2;

	public static String SynchronizeView_noSubscribersMessage;

	public static String SyncViewerDirectionFilters_incomingTitle;
	public static String SyncViewerDirectionFilters_incomingToolTip;
	public static String SyncViewerDirectionFilters_outgoingTitle;

	public static String SyncViewerDirectionFilters_outgoingToolTip;
	public static String SyncViewerDirectionFilters_conflictingTitle;
	public static String SyncViewerDirectionFilters_conflictingToolTip;

	public static String SyncViewPreferencePage_lastRefreshRun;
	public static String SyncViewPreferencePage_lastRefreshRunNever;

	//
	// Misc
	//

	public static String SynchronizeView_12;
	public static String SynchronizeView_13;
	public static String SynchronizeView_14;
	public static String SynchronizeView_16;

	public static String StatisticsPanel_outgoing;
	public static String StatisticsPanel_incoming;
	public static String StatisticsPanel_conflicting;
	public static String StatisticsPanel_changeNumbers;
	public static String StatisticsPanel_noWorkingSet;
	public static String StatisticsPanel_workingSetTooltip;
	public static String StatisticsPanel_numbersTooltip;
	public static String StatisticsPanel_numberTotalSingular;
	public static String StatisticsPanel_numberTotalPlural;

	public static String SyncViewerPreferencePage_0;
	public static String SyncViewerPreferencePage_1;
	public static String SyncViewerPreferencePage_2;
	public static String SyncViewerPreferencePage_3;
	public static String SyncViewerPreferencePage_6;
	public static String SyncViewerPreferencePage_7;
	public static String SyncViewerPreferencePage_8;
	public static String SyncViewerPreferencePage_10;
	public static String SyncViewerPreferencePage_11;
	public static String SyncViewerPreferencePage_12;
	public static String SyncViewerPreferencePage_13;
	public static String SyncViewerPreferencePage_14;
	public static String SyncViewerPreferencePage_15;
	public static String SyncViewerPreferencePage_16;
	public static String SyncViewerPreferencePage_19;
	public static String SyncViewerPreferencePage_31;
	public static String SyncViewerPreferencePage_42;

	public static String PreferencePageContainerDialog_6;

	public static String RefreshSubscriberInputJob_1;
	public static String RefreshSubscriberJob_1;
	public static String RefreshSubscriberJob_0;
	public static String RefreshSubscriberJob_2a;
	public static String RefreshSubscriberJob_2b;
	public static String RefreshSubscriberJob_2;
	public static String RefreshSubscriberJob_3;

	public static String CopyAction_title;
	public static String CopyAction_toolTip;
	public static String CopyAction_errorTitle;
	public static String CopyAction_errorMessage;
	public static String PasteAction_title;
	public static String PasteAction_toolTip;
	public static String RefactorActionGroup_0;
	public static String SynchronizeManager_7;
	public static String SynchronizeManager_8;
	public static String SynchronizeManager_9;
	public static String SynchronizeManager_10;
	public static String SynchronizeView_1;
	public static String TeamSubscriberParticipantPage_7;
	public static String TeamSubscriberParticipantPage_8;
	public static String TeamSubscriberSyncPage_labelWithSyncKind;
	public static String AbstractSynchronizeParticipant_4;

	public static String SynchronizeManager_11;
	public static String AbstractSynchronizeParticipant_5;
	public static String SynchronizeManager_13;

	public static String ChangesSection_filterHides;
	public static String ChangesSection_filterHidesSingular;
	public static String ChangesSection_filterHidesPlural;
	public static String ChangesSection_filterChange;
	public static String ChangesSection_noChanges;
	public static String Utils_22;
	public static String Utils_23;
	public static String Utils_24;
	public static String Utils_25;
	public static String Utils_26;
	public static String RefreshCompleteDialog_4;
	public static String RefreshCompleteDialog_4a;
	public static String RefreshCompleteDialog_changesSingular;
	public static String RefreshCompleteDialog_changesPlural;
	public static String RefreshCompleteDialog_newChangesSingular;
	public static String RefreshCompleteDialog_newChangesPlural;
	public static String RefreshCompleteDialog_6;
	public static String RefreshCompleteDialog_17;
	public static String RefreshCompleteDialog_18;
	public static String RefreshUserNotificationPolicy_0;
	public static String ConfigureRefreshScheduleDialog_0;
	public static String ConfigureRefreshScheduleDialog_1;
	public static String ConfigureRefreshScheduleDialog_1a;
	public static String ConfigureRefreshScheduleDialog_2;
	public static String ConfigureRefreshScheduleDialog_3;
	public static String ConfigureRefreshScheduleDialog_4;
	public static String ConfigureRefreshScheduleDialog_5;
	public static String ConfigureRefreshScheduleDialog_6;
	public static String ConfigureRefreshScheduleDialog_7;
	public static String ConfigureRefreshScheduleDialog_8;
	public static String RefreshSchedule_changesSingular;
	public static String RefreshSchedule_changesPlural;
	public static String RefreshSchedule_7;
	public static String RefreshSchedule_8;
	public static String RefreshSchedule_9;
	public static String RefreshSchedule_10;
	public static String RefreshSchedule_11;
	public static String RefreshSchedule_12;
	public static String RefreshSchedule_13;
	public static String RefreshSchedule_14;
	public static String RefreshSchedule_15;
	public static String DiffNodeControllerHierarchical_0;
	public static String ChangesSection_8;
	public static String ChangesSection_9;
	public static String ChangesSection_10;
	public static String ChangesSection_11;
	public static String ChangesSection_12;

	public static String OpenComparedDialog_diffViewTitleMany;
	public static String OpenComparedDialog_diffViewTitleOne;
	public static String OpenComparedDialog_noChangeTitle;
	public static String OpenComparedDialog_noChangesMessage;
	public static String GlobalRefreshAction_4;
	public static String GlobalRefreshAction_5;
	public static String SubscriberRefreshWizard_0;
	public static String ParticipantCompareDialog_1;
	public static String ParticipantCompareDialog_2;
	public static String ParticipantCompareDialog_3;
	public static String RefreshCompleteDialog_21;
	public static String RefreshCompleteDialog_22;
	public static String SynchronizeManager_27;
	public static String SynchronizeManager_31;
	public static String SynchronizeManager_30;
	public static String RefreshCompleteDialog_9;
	public static String Participant_comparing;
	public static String ParticipantPagePane_0;
	public static String Participant_merging;
	public static String Participant_synchronizing;
	public static String Participant_comparingDetail;
	public static String ParticipantPageSaveablePart_0;
	public static String Participant_mergingDetail;
	public static String Participant_synchronizingDetails;
	public static String Participant_synchronizingMoreDetails;
	public static String Participant_synchronizingResources;
	public static String GlobalRefreshResourceSelectionPage_1;
	public static String GlobalRefreshResourceSelectionPage_2;
	public static String GlobalRefreshResourceSelectionPage_3;
	public static String GlobalRefreshResourceSelectionPage_4;
	public static String GlobalRefreshResourceSelectionPage_5;
	public static String GlobalRefreshResourceSelectionPage_6;
	public static String GlobalRefreshResourceSelectionPage_7;
	public static String GlobalRefreshResourceSelectionPage_8;
	public static String GlobalRefreshResourceSelectionPage_9;
	public static String GlobalRefreshResourceSelectionPage_10;
	public static String GlobalRefreshResourceSelectionPage_11;
	public static String GlobalRefreshResourceSelectionPage_12;
	public static String GlobalRefreshResourceSelectionPage_13;
	public static String GlobalRefreshParticipantSelectionPage_0;
	public static String GlobalRefreshParticipantSelectionPage_1;
	public static String GlobalRefreshParticipantSelectionPage_2;
	public static String GlobalRefreshParticipantSelectionPage_3;

	public static String GlobalRefreshSubscriberPage_0;
	public static String GlobalRefreshSubscriberPage_1;
	public static String GlobalRefreshSubscriberPage_2;

	public static String GlobalRefreshSchedulePage_0;
	public static String GlobalRefreshSchedulePage_1;
	public static String GlobalRefreshSchedulePage_2;
	public static String SynchronizeManager_18;
	public static String GlobalSynchronizeWizard_11;
	public static String SynchronizeManager_19;
	public static String SynchronizeManager_20;
	public static String SynchronizeModelProvider_0;
	public static String SynchronizeModelUpdateHandler_0;
	public static String WorkspaceScope_0;
	public static String WorkingSetScope_0;
	public static String SubscriberParticipant_namePattern;
	public static String SubscriberParticipantWizard_0;
	public static String SubscriberParticipantWizard_1;

	public static String RemoveFromView_warningTitle;
	public static String RemoveFromView_warningMessage;
	public static String RemoveFromView_warningDontShow;
	public static String ResourceMappingSelectionArea_0;
	public static String ResourceMappingSelectionArea_1;

	public static String CompressedFoldersModelProvider_0;
	public static String HierarchicalModelProvider_0;
	public static String UIProjectSetSerializationContext_0;
	public static String UIProjectSetSerializationContext_1;
	public static String RemoveSynchronizeParticipantAction_0;
	public static String RemoveSynchronizeParticipantAction_1;

	public static String ConfigureProjectWizard_showAll;
	public static String ImportProjectSetMainPage_description;
	public static String ExportProjectSetMainPage_description;

	public static String DefaultUIFileModificationValidator_0;
	public static String DefaultUIFileModificationValidator_1;
	public static String DefaultUIFileModificationValidator_2;
	public static String DefaultUIFileModificationValidator_3;
	public static String DefaultUIFileModificationValidator_4;


	public static String CopyToClipboardAction_1;
	public static String CopyToClipboardAction_2;
	public static String CopyToClipboardAction_3;
	public static String CopyToClipboardAction_4;

	public static String FlatModelProvider_6;
	public static String FlatModelProvider_7;
	public static String FlatModelProvider_8;
	public static String FlatModelProvider_9;
	public static String FlatModelProvider_0;

	public static String ChangeLogModelProvider_0a;
	public static String ChangeLogModelProvider_1a;
	public static String ChangeLogModelProvider_2a;
	public static String ChangeLogModelProvider_3a;
	public static String ChangeLogModelProvider_0;
	public static String ChangeLogModelProvider_10;
	public static String ChangeLogModelProvider_11;
	public static String ChangeLogModelProvider_12;
	public static String ChangeLogModelProvider_4a;
	public static String ChangeLogModelProvider_5;
	public static String ChangeLogModelProvider_5a;
	public static String ChangeLogModelProvider_6;
	public static String ChangeLogModelProvider_9;
	public static String ChangeLogModelManager_0;
	public static String ChangeSetActionGroup_0;
	public static String ChangeSetActionGroup_1;
	public static String ChangeSetActionGroup_2;
	public static String CommitSetDiffNode_0;
	public static String FileTypeTable_0;
	public static String FileTypeTable_1;
	public static String FileTypeTable_2;
	public static String FileTypeTable_3;
	public static String FileTypeTable_4;
	public static String FileTypeTable_5;
	public static String FileTypeTable_6;
	public static String OpenWithActionGroup_0;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, TeamUIMessages.class);
	}
}