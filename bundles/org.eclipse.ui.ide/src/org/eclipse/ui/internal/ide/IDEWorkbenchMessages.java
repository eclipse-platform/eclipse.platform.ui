/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Benjamin Muskalla - bug 29633
 * Oakland Software Incorporated (Francis Upton) <francisu@ieee.org>
 *		- Bug 224997 [Workbench] Impossible to copy project
 * Dina Sayed, dsayed@eg.ibm.com, IBM -  bug 269844
 * Serge Beauchamp (Freescale Semiconductor) - [252996] Resource filters
 *     Markus Schorn (Wind River Systems) -  bug 284447
 *     James Blackburn (Broadcom Corp.)   -  bug 340978
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.osgi.util.NLS;

public class IDEWorkbenchMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.ide.messages";//$NON-NLS-1$
	// package: org.eclipse.ui.ide

	public static String IDEWorkbenchAdvisor_noPerspective;
	public static String IDEWorkbenchAdvisor_cancelHistoryPruning;
	public static String IDEWorkbenchAdvisor_preHistoryCompaction;
	public static String IDEWorkbenchAdvisor_postHistoryCompaction;

	public static String IDE_noFileEditorFound;
	public static String IDE_coreExceptionFileStore;

	public static String OpenWithMenu_Other;
	public static String OpenWithMenu_OtherDialogDescription;

	public static String QuickStartAction_errorDialogTitle;
	public static String QuickStartAction_infoReadError;
	
	public static String ConfigurationLogUpdateSection_installConfiguration;
	public static String ConfigurationLogUpdateSection_lastChangedOn;
	public static String ConfigurationLogUpdateSection_location;
	public static String ConfigurationLogUpdateSection_IU;
	public static String ConfigurationLogUpdateSection_IUHeader;
	public static String ConfigurationLogUpdateSection_bundle;
	public static String ConfigurationLogUpdateSection_bundleHeader;
	public static String ConfigurationLogUpdateSection_timestamp;

	public static String ErrorClosing;
	public static String ErrorOnSaveAll;

	public static String ResourceInfoPage_noResource;

	public static String ResourceFilterPage_title;
	public static String ResourceFilterPage_noResource;
	public static String ResourceFilterPage_addButtonLabel;
	public static String ResourceFilterPage_addGroupButtonLabel;
	public static String ResourceFilterPage_editButtonLabel;
	public static String ResourceFilterPage_removeButtonLabel;
	public static String ResourceFilterPage_columnFilterMode;
	public static String ResourceFilterPage_columnFilterDescription;
	public static String ResourceFilterPage_columnFilterTarget;
	public static String ResourceFilterPage_columnFilterPattern;
	public static String ResourceFilterPage_applyRecursivelyToFolderStructure;
	public static String ResourceFilterPage_recursive;
	public static String ResourceFilterPage_details;
	public static String ResourceFilterPage_caseSensitive;
	public static String ResourceFilterPage_regularExpression;
	public static String ResourceFilterPage_multiMatcher_Matcher;
	public static String ResourceFilterPage_multiMatcher_FileLength;
	public static String ResourceFilterPage_multiMatcher_TimeInterval;
	public static String ResourceFilterPage_multiMatcher_InvalidFileLength;
	public static String ResourceFilterPage_multiMatcher_InvalidTimeInterval;
	public static String ResourceFilterPage_includeOnly;
	public static String ResourceFilterPage_excludeAll;
	public static String ResourceFilterPage_includeOnlyColumn;
	public static String ResourceFilterPage_excludeAllColumn;
	public static String ResourceFilterPage_filesAndFolders;
	public static String ResourceFilterPage_files;
	public static String ResourceFilterPage_folders;
	public static String ResourceFilterPage_editFilterDialogTitle;
	public static String ResourceFilterPage_newFilterDialogTitleFolder;
	public static String ResourceFilterPage_newFilterDialogTitleProject;
	public static String ResourceFilterPage_addSubFilterActionLabel;
	public static String ResourceFilterPage_addSubFilterGroupActionLabel;
	public static String ResourceFilterPage_removeFilterActionLabel;
	public static String ResourceFilterPage_editFilterActionLabel;
	public static String ResourceFilterPage_multiKeyName;
	public static String ResourceFilterPage_multiKeyProjectRelativePath;
	public static String ResourceFilterPage_multiKeyLocation;
	public static String ResourceFilterPage_multiKeyLastModified;
	public static String ResourceFilterPage_multiKeyCreated;
	public static String ResourceFilterPage_multiKeyLength;
	public static String ResourceFilterPage_multiKeyReadOnly;
	public static String ResourceFilterPage_multiKeySymLink;
	public static String ResourceFilterPage_multiEquals;
	public static String ResourceFilterPage_multiMatches;
	public static String ResourceFilterPage_multiLargerThan;
	public static String ResourceFilterPage_multiSmallerThan;
	public static String ResourceFilterPage_multiBefore;
	public static String ResourceFilterPage_multiAfter;
	public static String ResourceFilterPage_multiWithin;
	public static String ResourceFilterPage_true;
	public static String ResourceFilterPage_false;
	
	//
	//
	// Copies from org.eclipse.ui.workbench
	//
	public static String showAdvanced;
	public static String hideAdvanced;
	public static String editfilters;
	public static String useDefaultLocation;
	public static String createLinkedFolder;
	public static String createVirtualFolder;
	
	// ==============================================================================
	// Workbench Actions
	// ==============================================================================

	// --- File Menu ---
	public static String Workbench_file;
	public static String Workbench_new;
	public static String OpenWorkspaceAction_text;
	public static String OpenWorkspaceAction_toolTip;
	public static String OpenWorkspaceAction_errorTitle;
	public static String OpenWorkspaceAction_errorMessage;
	public static String OpenWorkspaceAction_other;
	public static String NewProjectAction_text;
	public static String NewProjectAction_toolTip;
	public static String NewExampleAction_text;
	public static String NewExampleAction_toolTip;
	public static String SaveAsDialog_title;
	public static String SaveAsDialog_message;
	public static String SaveAsDialog_text;
	public static String SaveAsDialog_fileLabel;
	public static String SaveAsDialog_file;
	public static String SaveAsDialog_overwriteQuestion;
	public static String SaveAsDialog_closedProjectMessage;
	public static String Workbench_projectProperties;
	public static String Workbench_projectPropertiesToolTip;


	// --- Edit Menu ---
	public static String Workbench_edit;
	public static String Workbench_addBookmark;
	public static String Workbench_addBookmarkToolTip;
	public static String Workbench_addTask;
	public static String Workbench_addTaskToolTip;


	// --- Navigate Menu ---
	public static String Workbench_navigate;
	public static String Workbench_goTo;

	public static String Workbench_showIn;

	// --- Project Menu ---
	public static String Workbench_project;

	public static String Workbench_buildProject;
	public static String Workbench_buildProjectToolTip;
	public static String Workbench_rebuildProject;
	public static String Workbench_rebuildProjectToolTip;
	public static String Workbench_buildClean;
	public static String Workbench_buildSet;
	public static String Workbench_buildAutomatically;

	public static String GlobalBuildAction_text;
	public static String GlobalBuildAction_toolTip;
	public static String GlobalBuildAction_rebuildText;
	public static String GlobalBuildAction_rebuildToolTip;
	public static String GlobalBuildAction_buildProblems;
	public static String GlobalBuildAction_internalError;
	public static String GlobalBuildAction_buildOperationTitle;
	public static String GlobalBuildAction_rebuildAllOperationTitle;
	public static String GlobalBuildAction_jobTitle;

	public static String BuildSetAction_noBuildTitle;
	public static String BuildSetAction_noProjects;

	
	// --- Window Menu ---
	public static String Workbench_window;
	public static String Workbench_openPerspective;
	public static String Workbench_showView;

	public static String PromptOnExitDialog_shellTitle;
	public static String PromptOnExitDialog_message0;
	public static String PromptOnExitDialog_message1;
	public static String PromptOnExitDialog_choice;

	public static String Workbench_shortcuts;
	public static String Workbench_openNewWindow;


	// --- Help Menu ---
	public static String Workbench_help;
	public static String QuickStart_text;
	public static String QuickStart_toolTip;
	public static String QuickStartMessageDialog_title;
	public static String QuickStartMessageDialog_message;
	public static String WelcomePageSelectionDialog_title;
	public static String WelcomePageSelectionDialog_message;
	public static String TipsAndTricks_text;
	public static String TipsAndTricks_toolTip;
	public static String TipsAndTricksMessageDialog_title;
	public static String TipsAndTricksMessageDialog_message;
	public static String TipsAndTricksPageSelectionDialog_title;
	public static String TipsAndTricksPageSelectionDialog_message;
	public static String TipsAndTricksErrorDialog_title;
	public static String TipsAndTricksErrorDialog_noHref;
	public static String TipsAndTricksErrorDialog_noFeatures;

	// ==============================================================================
	// Navigator Actions
	// ==============================================================================
	public static String OpenWithMenu_dialogTitle;

	public static String CopyProjectAction_title;
	public static String CopyProjectAction_toolTip;
	public static String CopyProjectAction_copyTitle;
	public static String CopyProjectAction_copyNameOneArg;
	public static String CopyProjectAction_copyNameTwoArgs;
	public static String CopyProjectAction_alreadyExists;
	public static String CopyProjectAction_copyFailedTitle;
	public static String CopyProjectAction_internalError;

	public static String CopyResourceAction_title;
	public static String CopyResourceAction_toolTip;
	public static String CopyResourceAction_selectDestination;

	public static String MoveProjectAction_text;
	public static String MoveProjectAction_toolTip;
	public static String MoveProjectAction_moveTitle;
	public static String MoveProjectAction_dialogTitle;
	public static String MoveProjectAction_internalError;

	public static String MoveResourceAction_text;
	public static String MoveResourceAction_toolTip;
	public static String MoveResourceAction_title;
	public static String MoveResourceAction_checkMoveMessage;

	public static String ReadOnlyCheck_problems;

	public static String RenameResourceAction_text;
	public static String RenameResourceAction_toolTip;
	public static String RenameResourceAction_operationTitle;
	public static String RenameResourceAction_inputDialogTitle;
	public static String RenameResourceAction_inputDialogMessage;
	public static String RenameResourceAction_checkTitle;
	public static String RenameResourceAction_readOnlyCheck;
	public static String RenameResourceAction_resourceExists;
	public static String RenameResourceAction_projectExists;
	public static String RenameResourceAction_nameExists;
	public static String RenameResourceAction_overwriteQuestion;
	public static String RenameResourceAction_overwriteProjectQuestion;
	public static String RenameResourceAction_problemTitle;
	public static String RenameResourceAction_progress;
	public static String RenameResourceAction_nameMustBeDifferent;
	public static String RenameResourceAction_problemMessage;

	public static String DeleteResourceAction_text;
	public static String DeleteResourceAction_toolTip;
	public static String DeleteResourceAction_title1;
	public static String DeleteResourceAction_titleN;
	public static String DeleteResourceAction_confirm1;
	public static String DeleteResourceAction_confirmN;
	public static String DeleteResourceAction_titleProject1;
	public static String DeleteResourceAction_titleProjectN;
	public static String DeleteResourceAction_confirmProject1;
	public static String DeleteResourceAction_confirmProjectN;
	public static String DeleteResourceAction_deleteContents1;
	public static String DeleteResourceAction_deleteContentsN;
	public static String DeleteResourceAction_deleteContentsDetails;
	public static String DeleteResourceAction_doNotDeleteContents;
	public static String DeleteResourceAction_confirmLinkedResource1;
	public static String DeleteResourceAction_confirmLinkedResourceN;
	public static String DeleteResourceAction_readOnlyQuestion;
	public static String DeleteResourceAction_jobName;
	public static String DeleteResourceAction_checkJobName;
	public static String DeleteResourceAction_operationLabel;

	public static String AddBookmarkLabel;
	public static String AddBookmarkToolTip;
	public static String AddBookmarkDialog_title;
	public static String AddBookmarkDialog_message;

	public static String AddTaskLabel;
	public static String AddTaskToolTip;

	public static String OpenFileAction_text;
	public static String OpenFileAction_toolTip;
	public static String OpenFileAction_openFileShellTitle;

	public static String OpenLocalFileAction_title;
	public static String OpenLocalFileAction_message_fileNotFound;
	public static String OpenLocalFileAction_message_filesNotFound;
	public static String OpenLocalFileAction_message_errorOnOpen;
	public static String OpenLocalFileAction_title_selectWorkspaceFile;
	public static String OpenLocalFileAction_message_fileLinkedToMultiple;
	
	public static String OpenResourceAction_text;
	public static String OpenResourceAction_toolTip;
	public static String OpenResourceAction_dialogTitle;
	public static String OpenResourceAction_problemMessage;
	public static String OpenResourceAction_operationMessage;
	public static String OpenResourceAction_openRequiredProjects;
	
	public static String CloseResourceAction_text;
	public static String CloseResourceAction_warningForOne;
	public static String CloseResourceAction_warningForMultiple;
	public static String CloseResourceAction_confirm;
	public static String CloseResourceAction_toolTip;
	public static String CloseResourceAction_title;
	public static String CloseResourceAction_problemMessage;
	public static String CloseResourceAction_operationMessage;

	public static String CloseUnrelatedProjectsAction_text;
	public static String CloseUnrelatedProjectsAction_toolTip;
	public static String CloseUnrelatedProjectsAction_confirmMsg1;
	public static String CloseUnrelatedProjectsAction_confirmMsgN;
	public static String CloseUnrelatedProjectsAction_AlwaysClose;
	public static String CloseUnrelatedProjectsAction_AlwaysCloseWithoutPrompt;

	public static String BuildAction_text;
	public static String BuildAction_toolTip;
	public static String BuildAction_problemMessage;
	public static String BuildAction_problemTitle;
	public static String BuildAction_operationMessage;

	public static String RebuildAction_text;
	public static String RebuildAction_tooltip;

	public static String RefreshAction_text;
	public static String RefreshAction_toolTip;
	public static String RefreshAction_progressMessage;
	public static String RefreshAction_problemTitle;
	public static String RefreshAction_problemMessage;
	public static String RefreshAction_locationDeletedMessage;
	public static String RefreshAction_dialogTitle;

	public static String SelectWorkingSetAction_text;

	// --- Operations ---
	public static String CopyProjectOperation_progressTitle;
	public static String CopyProjectOperation_copyFailedMessage;
	public static String CopyProjectOperation_copyFailedTitle;
	public static String CopyProjectOperation_internalError;
	public static String CopyProjectOperation_copyProject;

	public static String CopyFilesAndFoldersOperation_copyFailedTitle;
	public static String CopyFilesAndFoldersOperation_problemMessage;
	public static String CopyFilesAndFoldersOperation_operationTitle;
	public static String CopyFilesAndFoldersOperation_nameCollision;
	public static String CopyFilesAndFoldersOperation_internalError;
	public static String CopyFilesAndFoldersOperation_resourceExists;
	public static String CopyFilesAndFoldersOperation_overwriteQuestion;
	public static String CopyFilesAndFoldersOperation_overwriteWithDetailsQuestion;
	public static String CopyFilesAndFoldersOperation_overwriteMergeQuestion;
	public static String CopyFilesAndFoldersOperation_overwriteNoMergeLinkQuestion;
	public static String CopyFilesAndFoldersOperation_overwriteNoMergeNoLinkQuestion;
	public static String CopyFilesAndFoldersOperation_deepCopyQuestion;
	public static String CopyFilesAndFoldersOperation_deepMoveQuestion;
	public static String CopyFilesAndFoldersOperation_copyNameTwoArgs;
	public static String CopyFilesAndFoldersOperation_copyNameOneArg;
	public static String CopyFilesAndFoldersOperation_destinationAccessError;
	public static String CopyFilesAndFoldersOperation_destinationDescendentError;
	public static String CopyFilesAndFoldersOperation_overwriteProblem;
	public static String CopyFilesAndFoldersOperation_question;
	public static String CopyFilesAndFoldersOperation_inputDialogTitle;
	public static String CopyFilesAndFoldersOperation_inputDialogMessage;
	public static String CopyFilesAndFoldersOperation_nameExists;
	public static String CopyFilesAndFoldersOperation_nameMustBeDifferent;
	public static String CopyFilesAndFoldersOperation_sameSourceAndDest;
	public static String CopyFilesAndFoldersOperation_importSameSourceAndDest;
	public static String CopyFilesAndFoldersOperation_resourceDeleted;
	public static String CopyFilesAndFoldersOperation_missingPathVariable;
	public static String CopyFilesAndFoldersOperation_missingLinkTarget;
	public static String CopyFilesAndFoldersOperation_CopyResourcesTask;
	public static String CopyFilesAndFoldersOperation_parentNotEqual;
	public static String CopyFilesAndFoldersOperation_infoNotFound;
	public static String CopyFilesAndFoldersOperation_sourceCannotBeCopiedIntoAVirtualFolder;
	public static String CopyFilesAndFoldersOperation_copyTitle;
	public static String CopyFilesAndFoldersOperation_moveTitle;

	public static String MoveFilesAndFoldersOperation_sameSourceAndDest;
	public static String MoveFilesAndFoldersOperation_moveFailedTitle;
	public static String MoveFilesAndFoldersOperation_problemMessage;
	public static String MoveFilesAndFoldersOperation_operationTitle;

	public static String WizardDataTransfer_existsQuestion;
	public static String WizardDataTransfer_overwriteNameAndPathQuestion;
	public static String WizardDataTransfer_exceptionMessage;
	public static String WizardTransferPage_selectTypes;
	public static String WizardTransferPage_selectAll;
	public static String WizardTransferPage_deselectAll;

	// --- Import ---
	public static String WizardImportPage_specifyFolder;
	public static String WizardImportPage_specifyProject;
	public static String WizardImportPage_folderMustExist;
	public static String WizardImportPage_errorDialogTitle;
	public static String WizardImportPage_folder;
	public static String WizardImportPage_browseLabel;
	public static String WizardImportPage_browse2;
	public static String WizardImportPage_selectFolderLabel;
	public static String WizardImportPage_selectFolderTitle;
	public static String WizardImportPage_destinationLabel;
	public static String WizardImportPage_options;
	public static String WizardImportPage_projectNotExist;
	public static String WizardImportPage_importOnReceiver;
	public static String WizardImportPage_noOpenProjects;
	public static String WizardImportPage_undefinedPathVariable;
	public static String WizardImportPage_containerNotExist;

	// --- Export ---
	public static String WizardExportPage_errorDialogTitle;
	public static String WizardExportPage_mustExistMessage;
	public static String WizardExportPage_mustBeAccessibleMessage;
	public static String WizardExportPage_detailsMessage;
	public static String WizardExportPage_whatLabel;
	public static String WizardExportPage_whereLabel;
	public static String WizardExportPage_options;
	public static String WizardExportPage_selectionDialogMessage;
	public static String WizardExportPage_resourceTypeDialog;
	public static String WizardExportPage_folder;
	public static String WizardExportPage_browse;
	public static String WizardExportPage_allTypes;
	public static String WizardExportPage_specificTypes;
	public static String WizardExportPage_edit;
	public static String WizardExportPage_details;
	public static String WizardExportPage_selectResourcesTitle;
	public static String WizardExportPage_oneResourceSelected;
	public static String WizardExportPage_selectResourcesToExport;
	public static String WizardExportPage_internalErrorTitle;
	public static String WizardExportPage_resourceCountMessage;

	// --- New Example ---
	public static String NewExample_title;

	public static String WizardNewProjectCreationPage_projectNameEmpty;
	public static String WizardNewProjectCreationPage_projectLocationEmpty;
	public static String WizardNewProjectCreationPage_projectExistsMessage;
	public static String WizardNewProjectCreationPage_nameLabel;
	public static String WizardNewProjectReferences_title;

	// --- New Folder ---
	public static String WizardNewFolderMainPage_folderName;
	public static String WizardNewFolderMainPage_folderLabel;
	public static String WizardNewFolderMainPage_description;
	public static String WizardNewFolderCreationPage_progress;
	public static String WizardNewFolderCreationPage_errorTitle;
	public static String WizardNewFolderCreationPage_internalErrorTitle;
	public static String WizardNewFolderCreationPage_resourceWillBeFilteredWarning;
	public static String WizardNewFolderCreationPage_title;
	public static String WizardNewFolder_internalError;
	public static String WizardNewFolderCreationPage_createLinkLocationTitle;
	public static String WizardNewFolderCreationPage_createLinkLocationQuestion;

	// --- New File ---
	public static String WizardNewFileCreationPage_progress;
	public static String WizardNewFileCreationPage_errorTitle;
	public static String WizardNewFileCreationPage_fileLabel;
	public static String WizardNewFileCreationPage_file;
	public static String WizardNewFileCreationPage_internalErrorTitle;
	public static String WizardNewFileCreationPage_internalErrorMessage;
	public static String WizardNewFileCreationPage_title;
	public static String WizardNewFileCreationPage_resourceWillBeFilteredWarning;
	public static String WizardNewFileCreationPage_createLinkLocationTitle;
	public static String WizardNewFileCreationPage_createLinkLocationQuestion;


	// --- Linked Resource ---
	public static String WizardNewLinkPage_linkFileButton;
	public static String WizardNewLinkPage_linkFolderButton;
	public static String WizardNewLinkPage_browseButton;
	public static String WizardNewLinkPage_variablesButton;
	public static String WizardNewLinkPage_targetSelectionLabel;
	public static String WizardNewLinkPage_linkTargetEmpty;
	public static String WizardNewLinkPage_linkTargetInvalid;
	public static String WizardNewLinkPage_linkTargetLocationInvalid;
	public static String WizardNewLinkPage_linkTargetNonExistent;
	public static String WizardNewLinkPage_linkTargetNotFile;
	public static String WizardNewLinkPage_linkTargetNotFolder;

	// ==============================================================================
	// Preference Pages
	// ==============================================================================
	public static String Preference_note;

	// --- Workbench ---
	public static String WorkbenchPreference_encoding;
	public static String WorkbenchPreference_defaultEncoding;
	public static String WorkbenchPreference_otherEncoding;
	public static String WorkbenchPreference_unsupportedEncoding;
	public static String WorkbenchPreference_encoding_encodingMessage;
	
	// ---workspace ---
	public static String IDEWorkspacePreference_autobuild;
	public static String IDEWorkspacePreference_autobuildToolTip;
	public static String IDEWorkspacePreference_savePriorToBuilding;
	public static String IDEWorkspacePreference_savePriorToBuildingToolTip;
	public static String IDEWorkspacePreference_RefreshButtonText;
	public static String IDEWorkspacePreference_RefreshButtonToolTip;
	public static String IDEWorkspacePreference_RefreshLightweightButtonText;
	public static String IDEWorkspacePreference_RefreshLightweightButtonToolTip;
	public static String IDEWorkspacePreference_fileLineDelimiter;
	public static String IDEWorkspacePreference_defaultLineDelim;
	public static String IDEWorkspacePreference_defaultLineDelimProj;
	public static String IDEWorkspacePreference_otherLineDelim;
	public static String IDEWorkspacePreference_relatedLink;
	public static String IDEWorkspacePreference_openReferencedProjects;
	public static String IDEWorkspacePreference_closeUnrelatedProjectsToolTip;
	public static String IDEWorkspacePreference_workspaceName;

	// --- Linked Resources ---
	public static String LinkedResourcesPreference_explanation;
	public static String LinkedResourcesPreference_enableLinkedResources;
	public static String LinkedResourcesPreference_linkedResourcesWarningTitle;
	public static String LinkedResourcesPreference_linkedResourcesWarningMessage;
	public static String LinkedResourcesPreference_dragAndDropHandlingMessage;
	public static String LinkedResourcesPreference_dragAndDropVirtualFolderHandlingMessage;
	public static String LinkedResourcesPreference_link;
	public static String linkedResourcesPreference_copy;
	public static String LinkedResourcesPreference_promptVirtual;
	public static String LinkedResourcesPreference_linkAndVirtualFolder;
	public static String LinkedResourcesPreference_linkVirtual;
	public static String linkedResourcesPreference_copyVirtual;
	public static String LinkedResourcesPreference_linkAndVirtualFolderVirtual;

	// The following six keys are marked as unused by the NLS search, but they are indirectly used
	// and should be removed.
	public static String PathVariableDialog_shellTitle_newVariable;
	public static String PathVariableDialog_shellTitle_existingVariable;
	public static String PathVariableDialog_shellTitle_editLocation;
	public static String PathVariableDialog_dialogTitle_newVariable;
	public static String PathVariableDialog_dialogTitle_existingVariable;
	public static String PathVariableDialog_dialogTitle_editLinkLocation;
	public static String PathVariableDialog_message_newVariable;
	public static String PathVariableDialog_message_existingVariable;
	public static String PathVariableDialog_message_editLocation;

	public static String PathVariableDialog_variableName;
	public static String PathVariableDialog_variableValue;
	public static String PathVariableDialog_variableResolvedValue;
	public static String PathVariableDialog_variableNameEmptyMessage;
	public static String PathVariableDialog_variableValueEmptyMessage;
	public static String PathVariableDialog_variableValueInvalidMessage;
	public static String PathVariableDialog_file;
	public static String PathVariableDialog_folder;
	public static String PathVariableDialog_variable;
	public static String PathVariableDialog_selectFileTitle;
	public static String PathVariableDialog_selectFolderTitle;
	public static String PathVariableDialog_selectFolderMessage;
	public static String PathVariableDialog_variableAlreadyExistsMessage;
	public static String PathVariableDialog_pathIsRelativeMessage;
	public static String PathVariableDialog_pathDoesNotExistMessage;
	public static String PathVariableDialog_variableValueIsWrongTypeFolder;
	public static String PathVariableDialog_variableValueIsWrongTypeFile;

	// --- Local History ---
	public static String FileHistory_longevity;
	public static String FileHistory_entries;
	public static String FileHistory_diskSpace;
	public static String FileHistory_applyPolicy;
	public static String FileHistory_mustBePositive;
	public static String FileHistory_invalid;
	public static String FileHistory_exceptionSaving;
	public static String FileHistory_aboveMaxEntries;
	public static String FileHistory_aboveMaxFileSize;
	public static String FileHistory_restartNote;

	// --- Perspectives ---
	public static String ProjectSwitchPerspectiveMode_optionsTitle;
	public static String ProjectSwitchPerspectiveMode_always;
	public static String ProjectSwitchPerspectiveMode_never;
	public static String ProjectSwitchPerspectiveMode_prompt;

	// --- Build Order ---
	public static String BuildOrderPreference_up;
	public static String BuildOrderPreference_down;
	public static String BuildOrderPreference_add;
	public static String BuildOrderPreference_remove;
	public static String BuildOrderPreference_selectOtherProjects;
	public static String BuildOrderPreference_useDefaults;
	public static String BuildOrderPreference_projectBuildOrder;
	public static String BuildOrderPreference_removeNote;
	public static String BuildOrderPreference_maxIterationsLabel;

	// --- Startup preferences ---
	public static String StartupPreferencePage_refreshButton;
	public static String StartupPreferencePage_launchPromptButton;
	public static String StartupPreferencePage_exitPromptButton;

	// --- Startup -> Workspaces preferences ---
	public static String RecentWorkspacesPreferencePage_NumberOfWorkspaces_label;
	public static String RecentWorkspacesPreferencePage_PromptAtStartup_label;
	public static String RecentWorkspacesPreferencePage_RecentWorkspacesList_label;
	public static String RecentWorkspacesPreferencePage_RemoveButton_label;


	// --- Info ---
	public static String ResourceInfo_readOnly;
	public static String ResourceInfo_executable;
	public static String ResourceInfo_locked;
	public static String ResourceInfo_archive;
	public static String ResourceInfo_derived;
	public static String ResourceInfo_derivedHasDerivedAncestor;
	public static String ResourceInfo_type;
	public static String ResourceInfo_location;
	public static String ResourceInfo_resolvedLocation;
	public static String ResourceInfo_size;
	public static String ResourceInfo_bytes;
	public static String ResourceInfo_file;
	public static String ResourceInfo_fileTypeFormat;
	public static String ResourceInfo_folder;
	public static String ResourceInfo_project;
	public static String ResourceInfo_linkedFile;
	public static String ResourceInfo_linkedFolder;
	public static String ResourceInfo_virtualFolder;
	public static String ResourceInfo_unknown;
	public static String ResourceInfo_notLocal;
	public static String ResourceInfo_undefinedPathVariable;
	public static String ResourceInfo_notExist;
	public static String ResourceInfo_fileNotExist;
	public static String ResourceInfo_path;
	public static String ResourceInfo_lastModified;
	public static String ResourceInfo_fileEncodingTitle;
	public static String ResourceInfo_fileContentEncodingFormat;
	public static String ResourceInfo_fileContentTypeEncodingFormat;
	public static String ResourceInfo_fileContainerEncodingFormat;
	public static String ResourceInfo_containerEncodingFormat;
	public static String ResourceInfo_exWarning;
	public static String ResourceInfo_isVirtualFolder;
	public static String ResourceInfo_edit;
	public static String ResourceInfo_attributes;
	public static String ResourceInfo_permissions;
	public static String ResourceInfo_owner;
	public static String ResourceInfo_group;
	public static String ResourceInfo_other;
	public static String ResourceInfo_read;
	public static String ResourceInfo_write;
	public static String ResourceInfo_execute;
	public static String ResourceInfo_recursiveChangesTitle;
	public static String ResourceInfo_recursiveChangesSummary;
	public static String ResourceInfo_recursiveChangesSet;
	public static String ResourceInfo_recursiveChangesUnset;
	public static String ResourceInfo_recursiveChangesQuestion;
	public static String ResourceInfo_recursiveChangesJobName;
	public static String ResourceInfo_recursiveChangesSubTaskName;
	public static String ResourceInfo_recursiveChangesError;

	// --- Project References ---
	public static String ProjectReferencesPage_label;

	// --- Project Linked Resources References ---
	public static String ProjectLinkedResourcePage_description;
	public static String ProjectLinkedResourcePage_pathVariableTabTitle;
	public static String ProjectLinkedResourcePage_linkedResourcesTabTitle;

	// --- Linked Resource Editor ---
	public static String LinkedResourceEditor_editLinkedLocation;
	public static String LinkedResourceEditor_convertToVariableLocation;
	public static String LinkedResourceEditor_remove;
	public static String LinkedResourceEditor_resourceName;
	public static String LinkedResourceEditor_path;
	public static String LinkedResourceEditor_location;
	public static String LinkedResourceEditor_fixed;
	public static String LinkedResourceEditor_broken;
	public static String LinkedResourceEditor_absolute;
	public static String LinkedResourceEditor_changedTo;
	public static String LinkedResourceEditor_unableToSetLinkLocationForResource;
	public static String LinkedResourceEditor_convertRelativePathLocations;
	public static String LinkedResourceEditor_convertionResults;
	public static String linkedResourceEditor_OK;
	public static String LinkedResourceEditor_unableToCreateVariable;
	public static String LinkedResourceEditor_unableToFindCommonPathSegments;
	public static String LinkedResourceEditor_convertAbsolutePathLocations;	
	public static String LinkedResourceEditor_descriptionBlock;
	public static String LinkedResourceEditor_convertTitle;
	public static String LinkedResourceEditor_convertMessage;
	public static String LinkedResourceEditor_removeTitle;
	public static String LinkedResourceEditor_removeMessage;
	public static String LinkedResourceEditor_removingMessage;
	
	// ==============================================================================
	// Editors
	// ==============================================================================
	public static String DefaultEditorDescription_name;

	public static String WelcomeEditor_accessException;
	public static String WelcomeEditor_readFileError;
	public static String WelcomeEditor_title;
	public static String WelcomeEditor_toolTip;
	public static String WelcomeEditor_copy_text;

	public static String WelcomeItem_unableToLoadClass;
	public static String WelcomeParser_parseError;
	public static String WelcomeParser_parseException;
	public static String Workbench_openEditorErrorDialogTitle;
	public static String Workbench_openEditorErrorDialogMessage;
	public static String QuickStartAction_openEditorException;

	// ==============================================================================
	// Dialogs
	// ==============================================================================
	public static String Question;
	public static String Always;
	public static String Never;
	public static String Prompt;

	public static String ContainerSelectionDialog_title;
	public static String ContainerSelectionDialog_message;

	public static String ContainerGroup_message;
	public static String ContainerGroup_selectFolder;

	public static String ContainerGenerator_progressMessage;
	public static String ContainerGenerator_pathOccupied;

	public static String ResourceGroup_resource;
	public static String ResourceGroup_nameExists;
	public static String ResourceGroup_folderEmpty;
	public static String ResourceGroup_noProject;
	public static String ResourceGroup_emptyName;
	public static String ResourceGroup_invalidFilename;
	public static String ResourceGroup_pathOccupied;

	public static String FileSelectionDialog_title;
	public static String FileSelectionDialog_message;

	public static String ProjectLocationSelectionDialog_nameLabel;
	public static String ProjectLocationSelectionDialog_locationLabel;
	public static String ProjectLocationSelectionDialog_browseLabel;
	public static String ProjectLocationSelectionDialog_directoryLabel;
	public static String ProjectLocationSelectionDialog_locationError;
	public static String ProjectLocationSelectionDialog_locationIsSelf;
	public static String ProjectLocationSelectionDialog_selectionTitle;
	public static String ProjectLocationSelectionDialog_useDefaultLabel;

	public static String ResourceSelectionDialog_title;
	public static String ResourceSelectionDialog_message;

	public static String MarkerResolutionSelectionDialog_title;
	public static String MarkerResolutionSelectionDialog_messageLabel;
	public static String MarkerDeleteHandler_JobTitle;
	public static String MarkerDeleteHandler_JobMessageLabel;
	
	public static String FilteredResourcesSelectionDialog_showDerivedResourcesAction;
	
	public static String ResourceSelectionDialog_label;
	public static String ResourceSelectionDialog_matching;
	public static String ResourceSelectionDialog_folders;
	public static String ResourceSelectionDialog_showDerived;

	public static String OpenResourceDialog_title;
	public static String OpenResourceDialog_openWithMenu_label;
	public static String OpenResourceDialog_openButton_text;
	public static String OpenResourceDialog_openWithButton_toolTip;

	public static String OpenResourceDialog_showInMenu_label;

	public static String NewFolderDialog_title;
	public static String NewFolderDialog_nameLabel;
	public static String NewFolderDialog_alreadyExists;
	public static String NewFolderDialog_folderNameEmpty;
	public static String NewFolderDialog_progress;
	public static String NewFolderDialog_errorTitle;
	public static String NewFolderDialog_internalError;

	public static String CreateLinkedResourceGroup_linkFileButton;
	public static String CreateLinkedResourceGroup_linkFolderButton;
	public static String CreateLinkedResourceGroup_browseButton;
	public static String CreateLinkedResourceGroup_variablesButton;
	public static String CreateLinkedResourceGroup_resolvedPathLabel;
	public static String CreateLinkedResourceGroup_targetSelectionLabel;
	public static String CreateLinkedResourceGroup_targetSelectionTitle;	
	public static String CreateLinkedResourceGroup_linkTargetNotFile;
	public static String CreateLinkedResourceGroup_linkTargetNotFolder;
	public static String CreateLinkedResourceGroup_linkTargetNonExistent;
	public static String CreateLinkedResourceGroup_unableToValidateLinkTarget;
	public static String CreateLinkedResourceGroup_linkRequiredUnderAGroup;

	public static String PathVariablesBlock_variablesLabel;
	public static String PathVariablesBlock_variablesLabelForResource;
	public static String PathVariablesBlock_addVariableButton;
	public static String PathVariablesBlock_editVariableButton;
	public static String PathVariablesBlock_removeVariableButton;
	public static String PathVariablesBlock_nameColumn;
	public static String PathVariablesBlock_valueColumn;

	public static String ResourceFilterEditDialog_title;

	public static String PathVariableSelectionDialog_title;
	public static String PathVariableSelectionDialog_extendButton;
	public static String PathVariableSelectionDialog_ExtensionDialog_title;
	public static String PathVariableSelectionDialog_ExtensionDialog_description;

	public static String ImportTypeDialog_title;
	public static String ImportTypeDialog_titleFilesOnly;
	public static String ImportTypeDialog_titleFilesLinking;
	public static String ImportTypeDialog_question;
	public static String ImportTypeDialog_questionFilesOnly;
	public static String ImportTypeDialog_moveFilesAndDirectories;
	public static String ImportTypeDialog_copyFilesAndDirectories;
	public static String ImportTypeDialog_moveFiles;
	public static String ImportTypeDialog_copyFiles;
	public static String ImportTypeDialog_recreateFilesAndDirectories;
	public static String ImportTypeDialog_createLinks;
	public static String ImportTypeDialog_linkFiles;
	public static String ImportTypeDialog_importElementsAs;
	public static String ImportTypeDialog_importElementsAsTooltip;
	public static String ImportTypeDialog_importElementsAsTooltipSet;
	public static String ImportTypeDialog_editVariables;
	public static String ImportTypeDialog_alwaysPerformThisOperation;
	public static String ImportTypeDialog_configureSettings;

	// ==============================================================================
	// Editor Framework
	// ==============================================================================
	public static String EditorManager_saveResourcesMessage;
	public static String EditorManager_saveResourcesTitle;

	public static String OpenSystemEditorAction_dialogTitle;
	public static String OpenSystemEditorAction_text;
	public static String OpenSystemEditorAction_toolTip;

	// ==============================================================================
	// Workspace
	// ==============================================================================
	public static String WorkspaceAction_problemsTitle;
	public static String WorkspaceAction_logTitle;
	public static String WorkbenchAction_problemsMessage;
	public static String WorkbenchAction_internalError;
	public static String Workspace;


	// ==============================================================================
	// Workbench
	// ==============================================================================
	public static String WorkbenchWindow_shellTitle;

	public static String Internal_error;
	public static String InternalError;
	public static String InternalErrorNoArg;
	public static String InternalErrorOneArg;

	public static String FatalError_RecursiveError;
	public static String FatalError_OutOfMemoryError;
	public static String FatalError_StackOverflowError;
	public static String FatalError_VirtualMachineError;
	public static String FatalError_SWTError;
	public static String FatalError;

	public static String ProblemSavingWorkbench;
	public static String ProblemsSavingWorkspace;

	public static String Problems_Opening_Page;

	public static String Workspace_refreshing;

	public static String IDEExceptionHandler_ExceptionHandledMessage;
	// ==============================================================================
	// Keys with references but don't show in the UI
	// ==============================================================================
	public static String CreateFileAction_text;
	public static String CreateFileAction_toolTip;
	public static String CreateFileAction_title;

	public static String CreateFolderAction_text;
	public static String CreateFolderAction_toolTip;
	public static String CreateFolderAction_title;

	public static String ScrubLocalAction_problemsMessage;
	public static String ScrubLocalAction_text;
	public static String ScrubLocalAction_toolTip;
	public static String ScrubLocalAction_problemsTitle;
	public static String ScrubLocalAction_progress;

	public static String TextAction_selectAll;
	public static String Cut;
	public static String Copy;
	public static String Paste;
	public static String Delete;

	// ==============================================================================
	// Keys used in the reuse editor which is released as experimental.
	// ==============================================================================
	public static String WorkbenchPreference_saveInterval;
	public static String WorkbenchPreference_saveIntervalError;

	// ==============================================================================
	// Working Set Framework.
	// ==============================================================================
	public static String ResourceWorkingSetPage_title;
	public static String ResourceWorkingSetPage_description;
	public static String ResourceWorkingSetPage_message;
	public static String ResourceWorkingSetPage_label_tree;
	public static String ResourceWorkingSetPage_warning_nameMustNotBeEmpty;
	public static String ResourceWorkingSetPage_warning_nameWhitespace;
	public static String ResourceWorkingSetPage_warning_workingSetExists;
	public static String ResourceWorkingSetPage_warning_resourceMustBeChecked;
	public static String ResourceWorkingSetPage_error;
	public static String ResourceWorkingSetPage_error_updateCheckedState;
	public static String ResourceWorkingSetPage_selectAll_label;
	public static String ResourceWorkingSetPage_selectAll_toolTip;
	public static String ResourceWorkingSetPage_deselectAll_label;
	public static String ResourceWorkingSetPage_deselectAll_toolTip;
	
	public static String ResourceEncodingFieldEditor_ErrorLoadingMessage;
	public static String ResourceEncodingFieldEditor_ErrorStoringMessage;
	public static String ResourceEncodingFieldEditor_EncodingConflictTitle;
	public static String ResourceEncodingFieldEditor_EncodingConflictMessage;
	public static String ResourceEncodingFieldEditor_SeparateDerivedEncodingsLabel;

	public static String ChooseWorkspaceDialog_dialogName;
	public static String ChooseWorkspaceDialog_dialogTitle;
	public static String ChooseWorkspaceDialog_dialogMessage;
	public static String ChooseWorkspaceDialog_defaultProductName;
	public static String ChooseWorkspaceDialog_workspaceEntryLabel;
	public static String ChooseWorkspaceDialog_browseLabel;
	public static String ChooseWorkspaceDialog_directoryBrowserTitle;
	public static String ChooseWorkspaceDialog_directoryBrowserMessage;
	public static String ChooseWorkspaceDialog_useDefaultMessage;
	
	public static String ChooseWorkspaceWithSettingsDialog_SettingsGroupName;
	public static String ChooseWorkspaceWithSettingsDialog_ProblemsTransferTitle;
	public static String ChooseWorkspaceWithSettingsDialog_TransferFailedMessage;
	public static String ChooseWorkspaceWithSettingsDialog_SaveSettingsFailed;
	public static String ChooseWorkspaceWithSettingsDialog_ClassCreationFailed;
	
	public static String IDEApplication_workspaceMandatoryTitle;
	public static String IDEApplication_workspaceMandatoryMessage;
	public static String IDEApplication_workspaceInUseTitle;
	public static String IDEApplication_workspaceInUseMessage;
	public static String IDEApplication_workspaceEmptyTitle;
	public static String IDEApplication_workspaceEmptyMessage;
	public static String IDEApplication_workspaceInvalidTitle;
	public static String IDEApplication_workspaceInvalidMessage;
	public static String IDEApplication_workspaceCannotBeSetTitle;
	public static String IDEApplication_workspaceCannotBeSetMessage;
	public static String IDEApplication_workspaceCannotLockTitle;
	public static String IDEApplication_workspaceCannotLockMessage;
	public static String IDEApplication_versionTitle;
	public static String IDEApplication_versionMessage;
	public static String GlobalBuildAction_BuildRunningTitle;
	public static String GlobalBuildAction_BuildRunningMessage;
	public static String CleanDialog_buildCleanAuto;
	public static String CleanDialog_buildCleanManual;
	public static String CleanDialog_title;
	public static String CleanDialog_cleanAllButton;
	public static String CleanDialog_cleanSelectedButton;
	public static String CleanDialog_buildNowButton;
	public static String CleanDialog_globalBuildButton;
	public static String CleanDialog_buildSelectedProjectsButton;
	public static String CleanDialog_cleanSelectedTaskName;
	public static String CleanDialog_cleanAllTaskName;
	public static String IDEEncoding_EncodingJob;
	public static String IDEEditorsPreferencePage_WorkbenchPreference_viewsRelatedLink;
	public static String IDEEditorsPreferencePage_WorkbenchPreference_FileEditorsRelatedLink;
    public static String IDEEditorsPreferencePage_WorkbenchPreference_contentTypesRelatedLink;
	public static String WorkbenchEncoding_invalidCharset;
	public static String CopyProjectAction_confirm;
	public static String CopyProjectAction_warning;
	public static String DeleteResourceAction_confirm;
	public static String DeleteResourceAction_warning;
	public static String CopyFilesAndFoldersOperation_confirmMove;
	public static String CopyFilesAndFoldersOperation_warningMove;
	public static String CopyFilesAndFoldersOperation_confirmCopy;
	public static String CopyFilesAndFoldersOperation_warningCopy;
	public static String RenameResourceAction_confirm;
	public static String RenameResourceAction_warning;

	public static String IDE_sideEffectWarning;

	public static String IDE_areYouSure;
	
	public static String IDEIdleHelper_backgroundGC;
	
	public static String SystemSettingsChange_title;
	public static String SystemSettingsChange_message;
	public static String SystemSettingsChange_yes;
	public static String SystemSettingsChange_no;
	
	public static String UnsupportedVM_message;
	
	public static String IDEWorkbenchActivityHelper_jobName;
	
	public static String OpenDelayedFileAction_title;
	public static String OpenDelayedFileAction_message_errorOnOpen;
	public static String OpenDelayedFileAction_message_fileNotFound;
	public static String OpenDelayedFileAction_message_noWindow;

	public static String editorAssociationOverride_error_couldNotCreate_message;
	public static String editorAssociationOverride_error_invalidElementName_message;
	public static String editorAssociationOverride_error_invalidExtension_message;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, IDEWorkbenchMessages.class);
	}

}
