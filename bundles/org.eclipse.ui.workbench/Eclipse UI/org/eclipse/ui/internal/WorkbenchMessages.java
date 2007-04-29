/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Message class for workbench messages.  These messages are used 
 * throughout the workbench. 
 *
 */
public class WorkbenchMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.messages";//$NON-NLS-1$


	public static String BundleSigningTray_Cant_Find_Service;


	public static String BundleSigningTray_Determine_Signer_For;


	public static String BundleSigningTray_Signing_Certificate;


	public static String BundleSigningTray_Signing_Date;


	public static String BundleSigningTray_Unget_Signing_Service;


	public static String BundleSigningTray_Unknown;


	public static String BundleSigningTray_Unsigned;


	public static String BundleSigningTray_Working;


	public static String NewWorkingSet;


	public static String PlatformUI_NoWorkbench;

	public static String Workbench_CreatingWorkbenchTwice;

    public static String StatusUtil_errorOccurred;
    
	// ==============================================================================
	// Workbench Actions
	// ==============================================================================

	// --- File Menu ---
	public static String NewWizardAction_text;
	public static String NewWizardAction_toolTip;
	public static String CloseAllAction_text;
	public static String CloseAllAction_toolTip;
	public static String CloseAllSavedAction_text;
	public static String CloseAllSavedAction_toolTip;
	public static String CloseEditorAction_text;
	public static String CloseEditorAction_toolTip;
	public static String CloseOthersAction_text;
	public static String CloseOthersAction_toolTip;
	public static String NewEditorAction_text;
	public static String NewEditorAction_tooltip;
	public static String SaveAction_text;
	public static String SaveAction_toolTip;
	public static String SaveAs_text;
	public static String SaveAs_toolTip;
	public static String SaveAll_text;
	public static String SaveAll_toolTip;
	public static String Workbench_revert;
	public static String Workbench_revertToolTip;
	public static String Workbench_move;

	public static String Workbench_moveToolTip;
	public static String Workbench_rename;
	public static String Workbench_renameToolTip;
	public static String Workbench_refresh;
	public static String Workbench_refreshToolTip;
	public static String Workbench_properties;
	public static String Workbench_propertiesToolTip;


	public static String Workbench_print;
	public static String Workbench_printToolTip;
	public static String ExportResourcesAction_text;
	public static String ExportResourcesAction_fileMenuText;
	public static String ExportResourcesAction_toolTip;
	public static String ImportResourcesAction_text;
	public static String ImportResourcesAction_toolTip;
	public static String OpenRecent_errorTitle;
	public static String OpenRecent_unableToOpen;
	public static String Exit_text;
	public static String Exit_toolTip;


	// --- Edit Menu ---
	public static String Workbench_undo;
	public static String Workbench_undoToolTip;
	public static String Workbench_redo;
	public static String Workbench_redoToolTip;
	public static String Workbench_cut;
	public static String Workbench_cutToolTip;
	public static String Workbench_copy;
	public static String Workbench_copyToolTip;
	public static String Workbench_paste;
	public static String Workbench_pasteToolTip;
	public static String Workbench_delete;
	public static String Workbench_deleteToolTip;
	public static String Workbench_selectAll;
	public static String Workbench_selectAllToolTip;
	public static String Workbench_findReplace;
	public static String Workbench_findReplaceToolTip;

	// --- Navigate Menu ---
	public static String Workbench_goInto;
	public static String Workbench_goIntoToolTip;
	public static String Workbench_back;
	public static String Workbench_backToolTip;
	public static String Workbench_forward;
	public static String Workbench_forwardToolTip;
	public static String Workbench_up;
	public static String Workbench_upToolTip;
	public static String Workbench_next;
	public static String Workbench_nextToolTip;
	public static String Workbench_previous;
	public static String Workbench_previousToolTip;

	public static String NavigationHistoryAction_forward_text;
	public static String NavigationHistoryAction_forward_toolTip;
	public static String NavigationHistoryAction_backward_text;
	public static String NavigationHistoryAction_backward_toolTip;
	public static String NavigationHistoryAction_forward_toolTipName;
	public static String NavigationHistoryAction_backward_toolTipName;
	public static String NavigationHistoryAction_locations;

	public static String Workbench_showInNoTargets;
	public static String Workbench_showInNoPerspectives;
	public static String Workbench_noApplicableItems;

	public static String OpenPreferences_text;
	public static String OpenPreferences_toolTip;

	// --- Window Menu ---
	public static String PerspectiveMenu_otherItem;
	public static String SelectPerspective_shellTitle;
	public static String Workbench_showPerspectiveError;
	public static String ChangeToPerspectiveMenu_errorTitle;
    public static String OpenPerspectiveDialogAction_text;
    public static String OpenPerspectiveDialogAction_tooltip;

    public static String ShowView_title;
	public static String ShowView_shellTitle;
	public static String ShowView_errorTitle;

	public static String ToggleEditor_hideEditors;
	public static String ToggleEditor_showEditors;
	public static String ToggleEditor_toolTip;

	public static String LockToolBarAction_text;
	public static String LockToolBarAction_toolTip;

	public static String EditActionSetsAction_text;
	public static String EditActionSetsAction_toolTip;
	public static String ActionSetSelection_customize;
	public static String ActionSetDialogInput_viewCategory;
	public static String ActionSetDialogInput_perspectiveCategory;
	public static String ActionSetDialogInput_wizardCategory;

	public static String ActionSetSelection_menuTab;
	public static String ActionSetSelection_actionSetsTab;
	public static String ActionSetSelection_selectActionSetsLabel;
	public static String ActionSetSelection_selectActionSetsHelp;
	public static String ActionSetSelection_availableActionSets;
	public static String ActionSetSelection_menubarActions;
	public static String ActionSetSelection_toolbarActions;
	public static String ActionSetSelection_selectMenusLabel;
	public static String ActionSetSelection_availableMenus;
	public static String ActionSetSelection_availableCategories;
	public static String ActionSetSelection_menuItems;
	public static String ActionSetSelection_descriptionColumnHeader;
	public static String ActionSetSelection_menuColumnHeader;
	public static String ActionSetSelection_toolbarLocation;
	public static String ActionSetSelection_menubarLocation;
	public static String ActionSetSelection_noDesc;

	public static String SavePerspective_text;
	public static String SavePerspective_toolTip;
	public static String SavePerspective_shellTitle;
	public static String SavePerspectiveDialog_description;
	public static String SavePerspective_name;
	public static String SavePerspective_existing;
	public static String SavePerspective_overwriteTitle;
	public static String SavePerspective_overwriteQuestion;
	public static String SavePerspective_singletonQuestion;
	public static String SavePerspective_errorTitle;
	public static String SavePerspective_errorMessage;

	public static String ResetPerspective_text;
	public static String ResetPerspective_toolTip;
	public static String ResetPerspective_message;
	public static String ResetPerspective_title;
	public static String RevertPerspective_note;

	public static String ClosePerspectiveAction_text;
	public static String ClosePerspectiveAction_toolTip;
	public static String CloseAllPerspectivesAction_text;
	public static String CloseAllPerspectivesAction_toolTip;

	public static String OpenInNewWindowAction_text;
	public static String OpenInNewWindowAction_toolTip;
	public static String OpenInNewWindowAction_errorTitle;
	public static String CycleEditorAction_next_text;
	public static String CycleEditorAction_next_toolTip;
	public static String CycleEditorAction_prev_text;
	public static String CycleEditorAction_prev_toolTip;
	public static String CycleEditorAction_header;
	public static String CyclePartAction_next_text;
	public static String CyclePartAction_next_toolTip;
	public static String CyclePartAction_prev_text;
	public static String CyclePartAction_prev_toolTip;
	public static String CyclePartAction_header;
	public static String CyclePartAction_editor;
	public static String CyclePerspectiveAction_next_text;
	public static String CyclePerspectiveAction_next_toolTip;
	public static String CyclePerspectiveAction_prev_text;
	public static String CyclePerspectiveAction_prev_toolTip;
	public static String CyclePerspectiveAction_header;
	public static String ActivateEditorAction_text;
	public static String ActivateEditorAction_toolTip;
	public static String MaximizePartAction_text;
	public static String MaximizePartAction_toolTip;
	public static String MinimizePartAction_text;
	public static String MinimizePartAction_toolTip;



	// --- Help Menu ---
	public static String AboutAction_text;
	public static String AboutAction_toolTip;
	public static String HelpContentsAction_text;
	public static String HelpContentsAction_toolTip;
	public static String HelpSearchAction_text;
	public static String HelpSearchAction_toolTip;
	public static String DynamicHelpAction_text;
	public static String DynamicHelpAction_toolTip;
	public static String AboutDialog_shellTitle;
	public static String AboutDialog_featureInfo;
	public static String AboutDialog_pluginInfo;
	public static String AboutDialog_systemInfo;
	public static String AboutDialog_defaultProductName;
	public static String ProductInfoDialog_errorTitle;
	public static String ProductInfoDialog_unableToOpenWebBrowser;
	public static String PreferencesExportDialog_ErrorDialogTitle;
	public static String AboutPluginsDialog_shellTitle;
	public static String AboutPluginsDialog_pluginName;
	public static String AboutPluginsDialog_pluginId;
	public static String AboutPluginsDialog_version;
	public static String AboutPluginsDialog_signed;
	public static String AboutPluginsDialog_provider;
	public static String AboutPluginsDialog_state_installed;
	public static String AboutPluginsDialog_state_resolved;
	public static String AboutPluginsDialog_state_starting;
	public static String AboutPluginsDialog_state_stopping;
	public static String AboutPluginsDialog_state_uninstalled;
	public static String AboutPluginsDialog_state_active;
	public static String AboutPluginsDialog_state_unknown;
	public static String AboutPluginsDialog_moreInfo;
	public static String AboutPluginsDialog_signingInfo_show;
	public static String AboutPluginsDialog_signingInfo_hide;
	public static String AboutPluginsDialog_errorTitle;
	public static String AboutPluginsDialog_unableToOpenFile;
	public static String AboutFeaturesDialog_shellTitle;
	public static String AboutFeaturesDialog_featureName;
	public static String AboutFeaturesDialog_featureId;
	public static String AboutFeaturesDialog_version;
	public static String AboutFeaturesDialog_signed;
	public static String AboutFeaturesDialog_provider;
	public static String AboutFeaturesDialog_moreInfo;
	public static String AboutFeaturesDialog_pluginsInfo;
	public static String AboutFeaturesDialog_noInformation;
	public static String AboutFeaturesDialog_pluginInfoTitle;
	public static String AboutFeaturesDialog_pluginInfoMessage;
	public static String AboutFeaturesDialog_noInfoTitle;
	public static String AboutSystemDialog_browseErrorLogName;
	public static String AboutSystemDialog_copyToClipboardName;
	public static String AboutSystemDialog_noLogTitle;
	public static String AboutSystemDialog_noLogMessage;

	// --- Shortcutbar ---
	public static String PerspectiveBarContributionItem_toolTip;
	public static String PerspectiveBarNewContributionItem_toolTip;

	//--- Coolbar ---
	public static String WorkbenchWindow_FileToolbar;
	public static String WorkbenchWindow_NavigateToolbar;
	public static String WorkbenchWindow_searchCombo_toolTip;
	public static String WorkbenchWindow_searchCombo_text;


	public static String WorkbenchWindow_close;
	public static String WorkbenchPage_PerspectiveFormat;
	public static String WorkbenchPage_ErrorCreatingPerspective;
	public static String WorkbenchPage_UndefinedPerspective;

	public static String SelectWorkingSetAction_text;
	public static String SelectWorkingSetAction_toolTip;
	public static String EditWorkingSetAction_text;
	public static String EditWorkingSetAction_toolTip;
	public static String EditWorkingSetAction_error_nowizard_title;
	public static String EditWorkingSetAction_error_nowizard_message;
	public static String ClearWorkingSetAction_text;
	public static String ClearWorkingSetAction_toolTip;
	public static String WindowWorkingSets;
	public static String NoWorkingSet;
	public static String SelectedWorkingSets;
	public static String NoApplicableWorkingSets;

	// ==============================================================================
	// Drill Actions
	// ==============================================================================
	public static String GoHome_text;
	public static String GoHome_toolTip;
	public static String GoBack_text;
	public static String GoBack_toolTip;
	public static String GoInto_text;
	public static String GoInto_toolTip;


	public static String ICategory_other;

	// ==============================================================================
	// Wizards
	// ==============================================================================
	public static String NewWizard_title;
	public static String NewWizardNewPage_description;
	public static String NewWizardNewPage_wizardsLabel;
	public static String NewWizardNewPage_showAll;
	public static String WizardList_description;
	public static String Select;
	public static String NewWizardSelectionPage_description;
	public static String NewWizardShortcutAction_errorTitle;
	public static String NewWizardShortcutAction_errorMessage;

	public static String NewWizardsRegistryReader_otherCategory;
	public static String NewWizardDropDown_text;
	
	public static String WorkbenchWizard_errorMessage;
	public static String WorkbenchWizard_errorTitle;
	public static String WizardTransferPage_selectAll;
	public static String WizardTransferPage_deselectAll;
	public static String TypesFiltering_title;
	public static String TypesFiltering_message;
	public static String TypesFiltering_otherExtensions;
	public static String TypesFiltering_typeDelimiter;

	// --- Import/Export ---
	public static String ImportExportPage_chooseImportSource;
	public static String ImportExportPage_chooseExportDestination;
	
	// --- Import ---
	public static String ImportWizard_title;
	public static String ImportWizard_selectSource;

	// --- Export ---
	public static String ExportWizard_title;
	public static String ExportWizard_selectDestination;
	// --- New Project ---
	public static String NewProject_title;

	// ==============================================================================
	// Preference Pages
	// ==============================================================================
	public static String PreferenceNode_errorTitle;
	public static String PreferenceNode_errorMessage;
    public static String PreferenceNode_NotFound;
	public static String Preference_note;

	// --- Workbench ---
	public static String WorkbenchPreference_showMultipleEditorTabsButton;
	public static String WorkbenchPreference_useIPersistableEditorButton;
	public static String WorkbenchPreference_promptWhenStillOpenButton;
	public static String WorkbenchPreference_showTextOnPerspectiveBar;
	public static String WorkbenchPreference_stickyCycleButton;
	public static String WorkbenchPreference_RunInBackgroundButton;
	public static String WorkbenchPreference_RunInBackgroundToolTip;

	// --- Appearance ---
	public static String ViewsPreference_currentPresentation;
	public static String ViewsPreference_currentPresentationFormat;
	public static String ViewsPreference_presentationConfirm_title;
	public static String ViewsPreference_presentationConfirm_message;
	public static String ViewsPreference_presentationConfirm_yes;
	public static String ViewsPreference_presentationConfirm_no;
	public static String ViewsPreference_editors;
	public static String ViewsPreference_editors_top;
	public static String ViewsPreference_editors_bottom;
	public static String ViewsPreference_views;
	public static String ViewsPreference_views_top;
	public static String ViewsPreference_views_bottom;
	public static String ViewsPreference_perspectiveBar;
	public static String ViewsPreference_perspectiveBar_topRight;
	public static String ViewsPreference_perspectiveBar_topLeft;
	public static String ViewsPreference_perspectiveBar_left;
	public static String ViewsPreference_traditionalTabs;
	public static String ViewsPreference_currentTheme;
	public static String ViewsPreference_currentThemeDescription;
	public static String ViewsPreference_currentThemeFormat;
	public static String ViewsPreference_enableAnimations;
	public static String ViewsPreference_override;
	public static String ViewsPreference_restartRequestJobName;
	
	// --- File Editors ---
	public static String FileEditorPreference_fileTypes;
	public static String FileEditorPreference_add;
	public static String FileEditorPreference_remove;
	public static String FileEditorPreference_associatedEditors;
	public static String FileEditorPreference_addEditor;
	public static String FileEditorPreference_removeEditor;
	public static String FileEditorPreference_default;
	public static String FileEditorPreference_existsTitle;
	public static String FileEditorPreference_existsMessage;
	public static String FileEditorPreference_defaultLabel;
    public static String FileEditorPreference_contentTypesRelatedLink;
    public static String FileEditorPreference_isLocked;

	public static String FileExtension_extensionEmptyMessage;
	public static String FileExtension_fileNameInvalidMessage;
	public static String FilteredPreferenceDialog_PreferenceSaveFailed;
	
	public static String FileExtension_fileTypeMessage;
	public static String FileExtension_fileTypeLabel;
	public static String FileExtension_shellTitle;
	public static String FileExtension_dialogTitle;

	public static String Choose_the_editor_for_file;
	public static String EditorSelection_chooseAnEditor;
	public static String EditorSelection_internal;
	public static String EditorSelection_external;
	public static String EditorSelection_browse;
	public static String EditorSelection_title;

	// --- Perspectives ---
	public static String OpenPerspectiveMode_optionsTitle;
	public static String OpenPerspectiveMode_sameWindow;
	public static String OpenPerspectiveMode_newWindow;

	public static String OpenViewMode_title;
	public static String OpenViewMode_embed;
	public static String OpenViewMode_fast;

	public static String PerspectivesPreference_MakeDefault;
	public static String PerspectivesPreference_MakeDefaultTip;
	public static String PerspectivesPreference_Reset;
	public static String PerspectivesPreference_ResetTip;
	public static String PerspectivesPreference_Delete;
	public static String PerspectivesPreference_DeleteTip;
	public static String PerspectivesPreference_available;
	public static String PerspectivesPreference_defaultLabel;
	public static String PerspectivesPreference_cannotdelete_title;
	public static String PerspectivesPreference_cannotdelete_message;

	public static String PerspectiveLabelProvider_unknown;

	//---- General Preferences----
	public static String PreferencePage_noDescription;
	public static String PreferencePageParameterValues_pageLabelSeparator;

	// --- Workbench -----
	public static String WorkbenchPreference_openMode;
	public static String WorkbenchPreference_doubleClick;
	public static String WorkbenchPreference_singleClick;
	public static String WorkbenchPreference_singleClick_SelectOnHover;
	public static String WorkbenchPreference_singleClick_OpenAfterDelay;
	public static String WorkbenchPreference_noEffectOnAllViews;
	public static String WorkbenchPreference_HeapStatusButton;
	public static String WorkbenchPreference_HeapStatusButtonToolTip;

	// --- Fonts ---
	public static String FontsPreference_useSystemFont;

	// --- Decorators ---
	public static String DecoratorsPreferencePage_description;
	public static String DecoratorsPreferencePage_decoratorsLabel;
	public static String DecoratorsPreferencePage_explanation;

	// --- Startup preferences ---
	public static String StartupPreferencePage_label;

	// ==============================================================================
	// Property Pages
	// ==============================================================================
	public static String PropertyDialog_text;
	public static String PropertyDialog_toolTip;
	public static String PropertyDialog_messageTitle;
	public static String PropertyDialog_noPropertyMessage;
	public static String PropertyDialog_propertyMessage;
	public static String PropertyPageNode_errorTitle;
	public static String PropertyPageNode_errorMessage;

	public static String SystemInPlaceDescription_name;
	public static String SystemEditorDescription_name;

	// ==============================================================================
	// Dialogs
	// ==============================================================================
	public static String Error;
	public static String Information;

	public static String ErrorPreferencePage_errorMessage;

	public static String ListSelection_title;
	public static String ListSelection_message;

	public static String SelectionDialog_selectLabel;
	public static String SelectionDialog_deselectLabel;

	public static String ElementTreeSelectionDialog_nothing_available;

	public static String CheckedTreeSelectionDialog_nothing_available;
	public static String CheckedTreeSelectionDialog_select_all;
	public static String CheckedTreeSelectionDialog_deselect_all;

	// ==============================================================================
	// Editor Framework
	// ==============================================================================
	public static String EditorManager_saveResourcesMessage;
	public static String EditorManager_saveResourcesOptionallyMessage;
	public static String EditorManager_saveResourcesTitle;
	public static String EditorManager_exceptionRestoringEditor;
	public static String EditorManager_unableToCreateEditor;
	public static String EditorManager_systemEditorError;
	public static String EditorManager_invalidDescriptor;
	public static String EditorManager_instantiationError;
    public static String EditorManager_errorInInit;
	public static String EditorManager_siteIncorrect;
	public static String EditorManager_unknownEditorIDMessage;
	public static String EditorManager_errorOpeningExternalEditor;
	public static String EditorManager_unableToOpenExternalEditor;
	public static String EditorManager_operationFailed;
	public static String EditorManager_saveChangesQuestion;
	public static String EditorManager_closeWithoutPromptingOption;
	public static String EditorManager_saveChangesOptionallyQuestion;
    public static String EditorManager_missing_editor_descriptor;
    public static String EditorManager_no_in_place_support;
    public static String EditorManager_invalid_editor_descriptor;
    public static String EditorManager_no_persisted_state;
    public static String EditorManager_no_input_factory_ID;
    public static String EditorManager_bad_element_factory;
    public static String EditorManager_create_element_returned_null;
    public static String EditorManager_wrong_createElement_result;
    public static String EditorManager_backgroundSaveJobName;
    
	public static String EditorPane_pinEditor;

	public static String ExternalEditor_errorMessage;
	public static String Save;
	public static String Save_Resource;
	public static String Save_All;


	// ==============================================================================
	// Perspective Framework
	// ==============================================================================
	public static String OpenNewPageMenu_dialogTitle;
	public static String OpenNewPageMenu_unknownPageInput;

	public static String OpenNewWindowMenu_dialogTitle;
	public static String OpenNewWindowMenu_unknownInput;

	public static String OpenPerspectiveMenu_pageProblemsTitle;
	public static String OpenPerspectiveMenu_errorUnknownInput;

	public static String Perspective_oneError;
	public static String Perspective_multipleErrors;

	public static String Perspective_problemRestoringTitle;
	public static String Perspective_errorReadingState;
	public static String Perspective_problemLoadingTitle;
	public static String Perspective_errorLoadingState;
	public static String WorkbenchPage_problemRestoringTitle;
	public static String WorkbenchPage_errorReadingState;

	public static String Perspective_problemSavingTitle;
	public static String Perspective_problemSavingMessage;

	public static String Perspective_unableToLoad;
	public static String Perspective_couldNotFind;

	// ==============================================================================
	// Views Framework
	// ==============================================================================
	public static String Menu;

	public static String StandardSystemToolbar_Minimize;
	public static String StandardSystemToolbar_Maximize;
	public static String StandardSystemToolbar_Restore;

	public static String EditorArea_Tooltip;
	public static String ViewPane_fastView;
	public static String ViewPane_minimizeView;
	public static String ViewPane_moveView;
	public static String ViewPane_moveFolder;

	public static String EditorPane_moveEditor;

	public static String ViewLabel_unknown;

	public static String ViewFactory_initException;
	public static String ViewFactory_siteException;
	public static String ViewFactory_couldNotCreate;
	public static String ViewFactory_noMultiple;
	public static String ViewFactory_couldNotSave;
	// ==============================================================================
	// Workbench
	// ==============================================================================
	public static String Startup_Loading;
	public static String Startup_Loading_Workbench;
	public static String Startup_Done;

	public static String WorkbenchPage_UnknownLabel;

	public static String WorkbenchPage_editorAlreadyOpenedMsg;

	// These four keys are marked as unused by the NLS search, but they are indirectly used
	// and should be removed.
	public static String PartPane_sizeLeft;
	public static String PartPane_sizeRight;
	public static String PartPane_sizeTop;
	public static String PartPane_sizeBottom;

	public static String PartPane_detach;
	public static String PartPane_restore;
	public static String PartPane_move;
	public static String PartPane_size;
	public static String PartPane_maximize;
	public static String PartPane_close;
	public static String PartPane_closeOthers;
	public static String PartPane_closeAll;
	public static String PartPane_newEditor;
	public static String PluginAction_operationNotAvailableMessage;
	public static String PluginAction_disabledMessage;
	public static String ActionDescriptor_invalidLabel;

	public static String XMLMemento_parserConfigError;
	public static String XMLMemento_ioError;
	public static String XMLMemento_formatError;
	public static String XMLMemento_noElement;

	// --- Workbench Errors/Problems ---
	public static String WorkbenchWindow_exceptionMessage;
	public static String WorkbenchPage_AbnormalWorkbenchCondition;
	public static String WorkbenchPage_IllegalSecondaryId;
	public static String WorkbenchPage_IllegalViewMode;
	public static String WorkbenchPart_AutoTitleFormat;
	public static String EditorPart_AutoTitleFormat;
	public static String Abnormal_Workbench_Conditi;
	public static String WorkbenchPage_ErrorActivatingView;
	public static String DecoratorManager_ErrorActivatingDecorator;

	public static String EditorRegistry_errorTitle;
	public static String EditorRegistry_errorMessage;

	public static String ErrorClosing;
	public static String ErrorClosingNoArg;
	public static String ErrorClosingOneArg;
	public static String ErrorReadingState;

	public static String Invalid_workbench_state_ve;
	public static String Workbench_incompatibleUIState;
	public static String Workbench_incompatibleSavedStateVersion;
	public static String ProblemSavingState;
	public static String SavingProblem;

	public static String Problems_Opening_Page;
	public static String Restoring_Problems;

	public static String Workspace_problemsTitle;

	public static String Workbench_problemsRestoringMsg;
	public static String Workbench_problemsSavingMsg;
	public static String Workbench_problemsRestoring;
	public static String Workbench_problemsSaving;
	public static String WorkbenchWindow_problemsRestoringWindow;
	public static String WorkbenchWindow_problemsSavingWindow;
	public static String EditorManager_problemsRestoringEditors;
	public static String EditorManager_problemsSavingEditors;
	public static String RootLayoutContainer_problemsRestoringPerspective;
	public static String RootLayoutContainer_problemsSavingPerspective;
	public static String ViewFactory_problemsSavingViews;

	public static String EditorManager_unableToSaveEditor;
	public static String Perspective_problemsRestoringPerspective;
	public static String Perspective_problemsSavingPerspective;
	public static String Perspective_problemsRestoringViews;
	public static String WorkbenchWindow_unableToRestorePerspective;
	public static String WorkbenchPage_unableToRestorePerspective;
	public static String WorkbenchPage_unableToSavePerspective;
	public static String Perspective_unableToRestorePerspective;
	public static String PageLayout_missingRefPart;
	public static String PageLayout_duplicateRefPart;
	public static String PartStack_incorrectPartInFolder;


	// ==============================================================================
	// Keys used in the reuse editor which is released as experimental.
	// ==============================================================================
	public static String EditorManager_openNewEditorLabel;
	public static String EditorManager_reuseEditorDialogTitle;
	public static String PinEditorAction_text;
	public static String PinEditorAction_toolTip;
	public static String WorkbenchPreference_reuseEditors;
	public static String WorkbenchPreference_reuseDirtyEditorGroupTitle;
	public static String WorkbenchPreference_promptToReuseEditor;
	public static String WorkbenchPreference_openNewEditor;
	public static String WorkbenchPreference_reuseEditorsThreshold;
	public static String WorkbenchPreference_reuseEditorsThresholdError;
	public static String WorkbenchPreference_recentFiles;
	public static String WorkbenchPreference_recentFilesError;
	public static String WorkbenchEditorsAction_label;
	public static String WorkbookEditorsAction_label;

	public static String WorkbenchEditorsDialog_title;
	public static String WorkbenchEditorsDialog_label;
	public static String WorkbenchEditorsDialog_closeSelected;
	public static String WorkbenchEditorsDialog_saveSelected;
	public static String WorkbenchEditorsDialog_selectClean;
	public static String WorkbenchEditorsDialog_invertSelection;
	public static String WorkbenchEditorsDialog_allSelection;
	public static String WorkbenchEditorsDialog_showAllPersp;
	public static String WorkbenchEditorsDialog_name;
	public static String WorkbenchEditorsDialog_path;
	public static String WorkbenchEditorsDialog_activate;
	public static String WorkbenchEditorsDialog_close;

	public static String ShowPartPaneMenuAction_text;
	public static String ShowPartPaneMenuAction_toolTip;
	public static String ShowViewMenuAction_text;
	public static String ShowViewMenuAction_toolTip;
	public static String QuickAccessAction_text;
	public static String QuickAccessAction_toolTip;

	public static String ToggleCoolbarVisibilityAction_show_text;
	public static String ToggleCoolbarVisibilityAction_hide_text;
	public static String ToggleCoolbarVisibilityAction_toolTip;
	

	// ==============================================================================
	// Working Set Framework.
	// ==============================================================================
	public static String ProblemSavingWorkingSetState_message;
	public static String ProblemSavingWorkingSetState_title;
	public static String ProblemRestoringWorkingSetState_message;

	public static String ProblemRestoringWorkingSetState_title;

	public static String WorkingSetEditWizard_title;
	public static String WorkingSetNewWizard_title;

	public static String WorkingSetTypePage_description;
	public static String WorkingSetTypePage_typesLabel;

	public static String WorkingSetSelectionDialog_title;
	public static String WorkingSetSelectionDialog_title_multiSelect;
	public static String WorkingSetSelectionDialog_message;
	public static String WorkingSetSelectionDialog_message_multiSelect;
	public static String WorkingSetSelectionDialog_detailsButton_label;
	public static String WorkingSetSelectionDialog_newButton_label;
	public static String WorkingSetSelectionDialog_removeButton_label;
	
	public static String WorkbenchPage_workingSet_default_label;
	public static String WorkbenchPage_workingSet_multi_label;

	// =================================================================
	// System Summary
	// =================================================================
	public static String SystemSummary_title;
	public static String SystemSummary_timeStamp;
	public static String SystemSummary_systemProperties;
	public static String SystemSummary_features;
	public static String SystemSummary_pluginRegistry;
	public static String SystemSummary_userPreferences;
	public static String SystemSummary_sectionTitle;
	public static String SystemSummary_sectionError;

	// paramter 0 is the feature name, parameter 1 is the version and parameter 2 is the Id
	public static String SystemSummary_featureVersion;
	public static String SystemMenuMovePane_PaneName;

	public static String SystemSummary_descriptorIdVersionState;

	// =================================================================
	// Editor List
	// =================================================================
	public static String EditorList_saveSelected_text;
	public static String EditorList_saveSelected_toolTip;
	public static String EditorList_closeSelected_text;
	public static String EditorList_closeSelected_toolTip;

	public static String EditorList_selectClean_text;
	public static String EditorList_selectClean_toolTip;
	public static String EditorList_invertSelection_text;
	public static String EditorList_invertSelection_toolTip;
	public static String EditorList_selectAll_text;
	public static String EditorList_selectAll_toolTip;

	public static String EditorList_FullName_text;
	public static String EditorList_FullName_toolTip;

	public static String EditorList_SortBy_text;
	public static String EditorList_SortByName_text;
	public static String EditorList_SortByName_toolTip;
	public static String EditorList_SortByMostRecentlyUsed_text;
	public static String EditorList_SortByMostRecentlyUsed_toolTip;

	public static String EditorList_ApplyTo_text;
	public static String EditorList_DisplayAllWindows_text;
	public static String EditorList_DisplayAllWindows_toolTip;
	public static String EditorList_DisplayAllPage_text;
	public static String EditorList_DisplayAllPage_toolTip;
	public static String EditorList_DisplayTabGroup_text;
	public static String EditorList_DisplayTabGroup_toolTip;
	public static String DecorationScheduler_UpdateJobName;
	public static String DecorationScheduler_CalculationJobName;
	public static String DecorationScheduler_UpdatingTask;
	public static String DecorationScheduler_CalculatingTask;
	public static String DecorationScheduler_ClearResultsJob;
	public static String DecorationScheduler_DecoratingSubtask;

	public static String PerspectiveBar_showText;
	public static String PerspectiveBar_customize;
	public static String PerspectiveBar_saveAs;
	public static String PerspectiveBar_reset;

	public static String PerspectiveSwitcher_dockOn;
	public static String PerspectiveSwitcher_topRight;
	public static String PerspectiveSwitcher_topLeft;
	public static String PerspectiveSwitcher_left;


	public static String FastViewBar_view_orientation;
	public static String FastViewBar_horizontal;
	public static String FastViewBar_vertical;
	public static String FastViewBar_0;

	public static String WorkbenchPlugin_extension;

	public static String EventLoopProgressMonitor_OpenDialogJobName;
	public static String DecorationReference_EmptyReference;
	public static String RectangleAnimation_Animating_Rectangle;
	public static String FilteredList_UpdateJobName;
	public static String FilteredTree_ClearToolTip;
	public static String FilteredTree_FilterMessage;
	public static String FilteredTree_FilteredDialogTitle;
	public static String Workbench_restoreDisabled;
	public static String Workbench_noStateToRestore;
	public static String Workbench_noWindowsRestored;
	public static String Workbench_startingPlugins;
	public static String ScopedPreferenceStore_DefaultAddedError;

	public static String WorkbenchEncoding_invalidCharset;

	public static String Dynamic_resetPerspectiveMessage;
	public static String Dynamic_resetPerspectiveTitle;

	//==============================================================
	// Undo/Redo Support
	
	public static String Operations_undoCommand;
	public static String Operations_redoCommand;
	public static String Operations_undoTooltipCommand;
	public static String Operations_redoTooltipCommand;
	public static String Operations_undoRedoCommandDisabled;
	public static String Operations_undoProblem;
	public static String Operations_redoProblem;
	public static String Operations_executeProblem;
	public static String Operations_undoInfo;
	public static String Operations_redoInfo;
	public static String Operations_executeInfo;
	public static String Operations_undoWarning;
	public static String Operations_redoWarning;
	public static String Operations_executeWarning;
	public static String Operations_linearUndoViolation;
	public static String Operations_linearRedoViolation;
	public static String Operations_nonLocalUndoWarning;
	public static String Operations_nonLocalRedoWarning;
	public static String Operations_discardUndo;
	public static String Operations_discardRedo;
	public static String Operations_proceedWithNonOKExecuteStatus;
	public static String Operations_proceedWithNonOKUndoStatus;
	public static String Operations_proceedWithNonOKRedoStatus;
	public static String Operations_stoppedOnExecuteErrorStatus;
	public static String Operations_stoppedOnUndoErrorStatus;
	public static String Operations_stoppedOnRedoErrorStatus;

	//==============================================================
	// Heap Status

	public static String HeapStatus_status;
	public static String HeapStatus_widthStr;
	public static String HeapStatus_memoryToolTip;
	public static String HeapStatus_meg;
	public static String HeapStatus_maxUnknown;
	public static String HeapStatus_noMark;
	public static String HeapStatus_buttonToolTip;
	public static String SetMarkAction_text;
	public static String ClearMarkAction_text;
	public static String ShowMaxAction_text;
//	public static String ShowKyrsoftViewAction_text;
//	public static String ShowKyrsoftViewAction_KyrsoftNotInstalled;
//	public static String ShowKyrsoftViewAction_OpenPerspectiveFirst;
//	public static String ShowKyrsoftViewAction_ErrorShowingKyrsoftView;


    // ==============================================================================
    // Content Types preference page
    // ==============================================================================
    
    public static String ContentTypes_lockedFormat;
    public static String ContentTypes_characterSetLabel;
    public static String ContentTypes_characterSetUpdateLabel;
    public static String ContentTypes_fileAssociationsLabel;
    public static String ContentTypes_fileAssociationsAddLabel;
    public static String ContentTypes_fileAssociationsRemoveLabel;
    public static String ContentTypes_contentTypesLabel;
    public static String ContentTypes_errorDialogMessage;
    public static String ContentTypes_FileEditorsRelatedLink;
    public static String Edit;

    // =========================================================================
    // Deprecated actions support
    // =========================================================================
    public static String CommandService_AutogeneratedCategoryName;
    public static String CommandService_AutogeneratedCategoryDescription;
    public static String LegacyActionPersistence_AutogeneratedCommandName;
	
    // ==============================================================================
    // Trim Common UI
    // ==============================================================================
    
    // Trim Menu item labels
    public static String TrimCommon_DockOn;
    public static String TrimCommon_Left;
    public static String TrimCommon_Right;
    public static String TrimCommon_Bottom;
    public static String TrimCommon_Top;
    public static String TrimCommon_Close;

    // Trim area Display Names
    public static String TrimCommon_Main_TrimName;
    public static String TrimCommon_PerspectiveSwitcher_TrimName;
    public static String TrimCommon_FastView_TrimName;
    public static String TrimCommon_HeapStatus_TrimName;
    public static String TrimCommon_IntroBar_TrimName;
    public static String TrimCommon_Progress_TrimName;
    public static String TrimCommon_StatusLine_TrimName;
		
    // FilteredItemsSelectionDialog
    public static String FilteredItemsSelectionDialog_cacheSearchJob_taskName;
    public static String FilteredItemsSelectionDialog_menu;
	public static String FilteredItemsSelectionDialog_refreshJob;
	public static String FilteredItemsSelectionDialog_progressRefreshJob;
	public static String FilteredItemsSelectionDialog_cacheRefreshJob;
	public static String FilteredItemsSelectionDialog_cacheRefreshJob_checkDuplicates;
	public static String FilteredItemsSelectionDialog_cacheRefreshJob_getFilteredElements;
	public static String FilteredItemsSelectionDialog_patternLabel;
	public static String FilteredItemsSelectionDialog_listLabel;
	public static String FilteredItemsSelectionDialog_toggleStatusAction;
	public static String FilteredItemsSelectionDialog_removeItemsFromHistoryAction;
	public static String FilteredItemsSelectionDialog_searchJob_taskName;
	public static String FilteredItemsSelectionDialog_separatorLabel;
    public static String FilteredItemsSelectionDialog_storeError;
    public static String FilteredItemsSelectionDialog_restoreError;
	
    // AbstractSearcher
    public static String FilteredItemsSelectionDialog_jobLabel; 
    public static String FilteredItemsSelectionDialog_jobError;
    public static String FilteredItemsSelectionDialog_jobCancel;
    
    // GranualProgressMonitor
    public static String FilteredItemsSelectionDialog_taskProgressMessage;
    public static String FilteredItemsSelectionDialog_subtaskProgressMessage;
    	
    static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, WorkbenchMessages.class);
	}


    public static String FastViewBar_show_view;
    
    // Content assist support
    public static String ContentAssist_Cue_Description_Key;
	
	//Settings transfer
	public static String WorkbenchLayoutSettings_Name;
	public static String WorkbenchSettings_CouldNotCreateDirectories;
	public static String WorkbenchSettings_CouldNotFindLocation;
	public static String WorkingSets_Name;
	public static String WorkingSets_CannotSave;

	// StatusDialog
	public static String StatusDialog_reason;
	public static String StatusDialog_checkDetailsMessage;
}
