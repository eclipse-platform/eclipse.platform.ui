/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

public interface IHelpContextIds {
	public static final String PREFIX = TeamUIPlugin.ID + "."; //$NON-NLS-1$

    // Dialogs
    public static final String SYNC_VIEW_PREFERENCE_DIALOG = PREFIX + "sync_view_preference_dialog_context"; //$NON-NLS-1$
    
	// Preference Pages
	public static final String IGNORE_PREFERENCE_PAGE = PREFIX + "ignore_preference_page_context"; //$NON-NLS-1$
	public static final String FILE_TYPE_PREFERENCE_PAGE = PREFIX + "file_type_preference_page_context"; //$NON-NLS-1$
    public static final String SYNC_PREFERENCE_PAGE = PREFIX + "sync_preference_page_context"; //$NON-NLS-1$

	// Wizard Pages
	public static final String SHARE_PROJECT_PAGE = PREFIX + "share_project_page_context"; //$NON-NLS-1$
	public static final String IMPORT_PROJECT_SET_PAGE = PREFIX + "import_project_set_page_context"; //$NON-NLS-1$
	public static final String EXPORT_PROJECT_SET_PAGE = PREFIX + "export_project_set_page_context"; //$NON-NLS-1$
	public static final String SYNC_RESOURCE_SELECTION_PAGE = PREFIX + "sync_resource_selection_page_context"; //$NON-NLS-1$
    public static final String REFRESH_WIZARD_SELECTION_PAGE = PREFIX + "refresh_wizard_selection_page_context"; //$NON-NLS-1$

	// Catchup Release Viewers
	public static final String TARGET_CATCHUP_RELEASE_VIEWER = PREFIX + "target_catchup_release_viewer_context"; //$NON-NLS-1$

	// Target Actions
	public static final String SYNC_GET_ACTION = PREFIX + "sync_get_action_context"; //$NON-NLS-1$
	public static final String SYNC_PUT_ACTION = PREFIX + "sync_put_action_context"; //$NON-NLS-1$

	// Views
	public static final String SITE_EXPLORER_VIEW = PREFIX + "site_explorer_view_context"; //$NON-NLS-1$
	public static final String SYNC_VIEW = PREFIX + "sync_view_context"; //$NON-NLS-1$

	// Site Explorer View Actions
	public static final String ADD_SITE_ACTION = PREFIX + "add_site_action_context"; //$NON-NLS-1$
	public static final String NEW_FOLDER_ACTION = PREFIX + "new_folder_action_context"; //$NON-NLS-1$

	// Sync View Actions
	public static final String OPEN_ACTION = PREFIX + "open_action_context"; //$NON-NLS-1$
	public static final String EXPANDALL_ACTION = PREFIX + "expandall_action_context"; //$NON-NLS-1$
	public static final String REMOVE_ACTION = PREFIX + "remove_action_context"; //$NON-NLS-1$
	public static final String NAVIGATOR_SHOW_ACTION = PREFIX + "navigator_show_action_context"; //$NON-NLS-1$

}
