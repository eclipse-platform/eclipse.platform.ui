/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

public interface ICVSUIConstants {
	// image paths
	public final String ICON_PATH_FULL = "icons/full/"; //$NON-NLS-1$
	public final String ICON_PATH_BASIC = "icons/basic/"; //$NON-NLS-1$
	
	// images
	
	// overlays
	public final String IMG_MERGEABLE_CONFLICT = "ovr16/confauto_ov.gif"; //$NON-NLS-1$
	public final String IMG_QUESTIONABLE = "ovr16/question_ov.gif"; //$NON-NLS-1$
	public final String IMG_MERGED = "ovr16/merged_ov.gif"; //$NON-NLS-1$
	
	// objects
	public final String IMG_REPOSITORY = "obj16/repository_rep.gif"; //$NON-NLS-1$
	public final String IMG_TAG = "obj16/tag.gif"; //$NON-NLS-1$
	public final String IMG_BRANCHES_CATEGORY = "obj16/branches_rep.gif"; //$NON-NLS-1$
	public final String IMG_VERSIONS_CATEGORY = "obj16/versions_rep.gif"; //$NON-NLS-1$
	public final String IMG_PROJECT_VERSION = "obj16/prjversions_rep.gif"; //$NON-NLS-1$
	
	// toolbar
	public final String IMG_REFRESH = "clcl16/refresh.gif"; //$NON-NLS-1$
	public final String IMG_CLEAR = "clcl16/clear_co.gif"; //$NON-NLS-1$
	
	// wizards
	public final String IMG_NEWLOCATION = "wizards/newlocation_wiz.gif"; //$NON-NLS-1$
	
	// preferences
	public final String PREF_SHOW_COMMENTS = "pref_show_comments"; //$NON-NLS-1$
	public final String PREF_SHOW_TAGS = "pref_show_tags"; //$NON-NLS-1$
	public final String PREF_PRUNE_EMPTY_DIRECTORIES = "pref_prune_empty_directories";	 //$NON-NLS-1$
	public final String PREF_TIMEOUT = "pref_timeout";	 //$NON-NLS-1$
	public final String PREF_QUIETNESS = "pref_quietness"; //$NON-NLS-1$
	public final String PREF_SHOW_MODULES = "pref_show_modules"; //$NON-NLS-1$
	public final String PREF_HISTORY_TRACKS_SELECTION = "pref_history_tracks_selection"; //$NON-NLS-1$
	public final String PREF_CVS_RSH = "pref_cvs_rsh"; //$NON-NLS-1$
	public final String PREF_CVS_SERVER = "pref_cvs_server"; //$NON-NLS-1$
	public final String PREF_CONSIDER_CONTENTS = "pref_consider_contents"; //$NON-NLS-1$
	public final String PREF_PROMPT_ON_FILE_DELETE = "pref_prompt_on_file_delete"; //$NON-NLS-1$
	public final String PREF_PROMPT_ON_FOLDER_DELETE = "pref_prompt_on_folder_delete"; //$NON-NLS-1$
	public final String PREF_SHOW_MARKERS = "pref_show_markers"; //$NON-NLS-1$
	public final String PREF_REPLACE_UNMANAGED = "pref_replace_unmanaged"; //$NON-NLS-1$
	public final String PREF_COMPRESSION_LEVEL = "pref_compression_level"; //$NON-NLS-1$
	public final String PREF_TEXT_KSUBST = "pref_text_ksubst"; //$NON-NLS-1$
	public final String PREF_PROMPT_ON_MIXED_TAGS = "pref_prompt_on_mixed_tags"; //$NON-NLS-1$

	// console preferences
	public final String PREF_CONSOLE_COMMAND_COLOR = "pref_console_command_color"; //$NON-NLS-1$
	public final String PREF_CONSOLE_MESSAGE_COLOR = "pref_console_message_color"; //$NON-NLS-1$
	public final String PREF_CONSOLE_ERROR_COLOR = "pref_console_error_color"; //$NON-NLS-1$
	public final String PREF_CONSOLE_FONT = "pref_console_font"; //$NON-NLS-1$
	public final String PREF_CONSOLE_AUTO_OPEN = "pref_console_auto_open"; //$NON-NLS-1$
		
	// decorator preferences
	public final String PREF_FILETEXT_DECORATION = "pref_filetext_decoration"; //$NON-NLS-1$
	public final String PREF_FOLDERTEXT_DECORATION = "pref_foldertext_decoration"; //$NON-NLS-1$
	public final String PREF_PROJECTTEXT_DECORATION = "pref_projecttext_decoration"; //$NON-NLS-1$
	
	public final String PREF_SHOW_DIRTY_DECORATION = "pref_show_overlaydirty"; //$NON-NLS-1$
	public final String PREF_SHOW_ADDED_DECORATION = "pref_show_added"; //$NON-NLS-1$
	public final String PREF_SHOW_HASREMOTE_DECORATION = "pref_show_hasremote"; //$NON-NLS-1$
	
	public final String PREF_DIRTY_FLAG = "pref_dirty_flag"; //$NON-NLS-1$
	public final String PREF_ADDED_FLAG = "pref_added_flag"; //$NON-NLS-1$
	
	public final String PREF_CALCULATE_DIRTY = "pref_calculate_dirty";	 //$NON-NLS-1$

	// Wizard banners
	public final String IMG_WIZBAN_SHARE = "wizban/newconnect_wizban.gif";	 //$NON-NLS-1$
	public final String IMG_WIZBAN_BRANCH = "wizban/newstream_wizban.gif";	 //$NON-NLS-1$
	public final String IMG_WIZBAN_MERGE = "wizban/mergestream_wizban.gif";	 //$NON-NLS-1$

	// Properties
	public final String PROP_NAME = "cvs.name"; //$NON-NLS-1$
	public final String PROP_REVISION = "cvs.revision"; //$NON-NLS-1$
	public final String PROP_AUTHOR = "cvs.author"; //$NON-NLS-1$
	public final String PROP_COMMENT = "cvs.comment"; //$NON-NLS-1$
	public final String PROP_DATE = "cvs.date"; //$NON-NLS-1$
	public final String PROP_DIRTY = "cvs.dirty"; //$NON-NLS-1$
	public final String PROP_MODIFIED = "cvs.modified"; //$NON-NLS-1$
	public final String PROP_KEYWORD = "cvs.date"; //$NON-NLS-1$
	public final String PROP_TAG = "cvs.tag"; //$NON-NLS-1$
	public final String PROP_PERMISSIONS = "cvs.permissions"; //$NON-NLS-1$
	public final String PROP_HOST = "cvs.host"; //$NON-NLS-1$
	public final String PROP_USER = "cvs.user"; //$NON-NLS-1$
	public final String PROP_METHOD = "cvs.method"; //$NON-NLS-1$
	public final String PROP_PORT = "cvs.port"; //$NON-NLS-1$
	public final String PROP_ROOT = "cvs.root"; //$NON-NLS-1$
}

