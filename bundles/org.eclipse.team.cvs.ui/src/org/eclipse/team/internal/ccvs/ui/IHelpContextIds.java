/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

public interface IHelpContextIds {
	public static final String PREFIX = CVSUIPlugin.ID + "."; //$NON-NLS-1$
	
	// Dialogs
	public static final String TAG_CONFIGURATION_OVERVIEW = PREFIX + "tag_configuration_overview"; //$NON-NLS-1$
	public static final String TAG_CONFIGURATION_REFRESHLIST = PREFIX + "tag_configuration_refreshlist"; //$NON-NLS-1$
	public static final String TAG_CONFIGURATION_REFRESHACTION = PREFIX + "tag_configuration_refreshaction"; //$NON-NLS-1$
	public static final String USER_VALIDATION_DIALOG = PREFIX + "user_validation_dialog_context"; //$NON-NLS-1$
	public static final String RELEASE_COMMENT_DIALOG = PREFIX + "release_comment_dialog_context"; //$NON-NLS-1$
	public static final String BRANCH_DIALOG = PREFIX + "branch_dialog_context"; //$NON-NLS-1$

	// Different uses of the TagSelectionDialog (not done)
	public static final String REPLACE_TAG_SELECTION_DIALOG = PREFIX + "replace_tag_selection_dialog_context"; //$NON-NLS-1$
	public static final String COMPARE_TAG_SELECTION_DIALOG = PREFIX + "compare_tag_selection_dialog_context"; //$NON-NLS-1$
	public static final String TAG_REMOTE_WITH_EXISTING_DIALOG = PREFIX + "tag_remot_with_existing_dialog_context"; //$NON-NLS-1$

	// Different uses of the TagAsVersionDialog (not done)
	public static final String TAG_LOCAL_AS_VERSION_DIALOG = PREFIX + "tag_local_as_version_dialog_context"; //$NON-NLS-1$
	public static final String TAG_REMOTE_AS_VERSION_DIALOG = PREFIX + "tag_remote_as_version_dialog_context"; //$NON-NLS-1$
	
	// Different uses of InputDialog from actions (not done)
	public static final String DEFINE_BRANCH_DIALOG = PREFIX + "define_branch_dialog_context"; //$NON-NLS-1$
	public static final String DEFINE_VERSION_DIALOG = PREFIX + "define_version_dialog_context"; //$NON-NLS-1$
	
	// Wizards 
	// (not done)
	public static final String SHARING_WIZARD = PREFIX + "sharing_wizard_context"; //$NON-NLS-1$
	public static final String KEYWORD_SUBSTITUTION_WIZARD = PREFIX + "keyword_substituton_wizard_context"; //$NON-NLS-1$
	public static final String NEW_LOCATION_WIZARD = PREFIX + "new_location_wizard_context"; //$NON-NLS-1$
	public static final String PATCH_WIZARD = PREFIX + "patch_wizard_context"; //$NON-NLS-1$

	// Wizard Pages (not done)
	public static final String SHARING_AUTOCONNECT_PAGE = PREFIX + "sharing_autoconnect_page_context"; //$NON-NLS-1$
	public static final String SHARING_SELECT_REPOSITORY_PAGE = PREFIX + "sharing_select_repository_page_context"; //$NON-NLS-1$
	public static final String SHARING_NEW_REPOSITORY_PAGE = PREFIX + "sharing_new_repository_page_context"; //$NON-NLS-1$
	public static final String SHARING_MODULE_PAGE = PREFIX + "sharing_module_page_context"; //$NON-NLS-1$
	public static final String SHARING_FINISH_PAGE = PREFIX + "sharing_finish_page_context"; //$NON-NLS-1$
	public static final String PATCH_SELECTION_PAGE = PREFIX + "patch_selection_page_context"; //$NON-NLS-1$
	public static final String PATCH_OPTIONS_PAGE = PREFIX + "patch_options_page_context"; //$NON-NLS-1$
	public static final String KEYWORD_SUBSTITUTION_SELECTION_PAGE = PREFIX + "keyword_substituton_selection_page_context"; //$NON-NLS-1$
	public static final String KEYWORD_SUBSTITUTION_SUMMARY_PAGE = PREFIX + "keyword_substituton_summary_page_context"; //$NON-NLS-1$
	public static final String KEYWORD_SUBSTITUTION_SHARED_PAGE = PREFIX + "keyword_substituton_shared_page_context"; //$NON-NLS-1$
	public static final String KEYWORD_SUBSTITUTION_CHANGED_PAGE = PREFIX + "keyword_substituton_changed_page_context"; //$NON-NLS-1$
	
	// Preference Pages
	public static final String PREF_PRUNE = PREFIX + "prune_empty_directories_pref"; //$NON-NLS-1$
	public static final String PREF_QUIET = PREFIX + "quietness_level_pref"; //$NON-NLS-1$
	public static final String PREF_COMPRESSION = PREFIX + "compression_level_pref"; //$NON-NLS-1$
	public static final String PREF_KEYWORDMODE = PREFIX + "default_keywordmode_pref"; //$NON-NLS-1$
	public static final String PREF_COMMS_TIMEOUT = PREFIX + "comms_timeout_pref"; //$NON-NLS-1$
	public static final String PREF_CONSIDER_CONTENT = PREFIX + "consider_content_pref"; //$NON-NLS-1$
	public static final String PREF_MARKERS_ENABLED = PREFIX + "markers_enabled_pref"; //$NON-NLS-1$
	public static final String PREF_REPLACE_DELETE_UNMANAGED = PREFIX + "replace_deletion_of_unmanaged_pref"; //$NON-NLS-1$
	
	// Views
	public static final String CONSOLE_VIEW = PREFIX + "console_view_context"; //$NON-NLS-1$
	public static final String REPOSITORIES_VIEW = PREFIX + "repositories_view_context"; //$NON-NLS-1$
	public static final String RESOURCE_HISTORY_VIEW = PREFIX + "resource_history_view_context"; //$NON-NLS-1$
	
	// Viewers
	public static final String CATCHUP_RELEASE_VIEWER = PREFIX + "catchup_release_viewer_context"; //$NON-NLS-1$
	
	// Add to .cvsignor dialog
	public static final String ADD_TO_CVSIGNORE = PREFIX + "add_to_cvsignore_context"; //$NON-NLS-1$	
}
