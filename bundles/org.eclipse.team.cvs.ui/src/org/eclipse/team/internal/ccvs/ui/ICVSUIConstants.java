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
	public final String ICON_PATH_FULL = "icons/full/";
	public final String ICON_PATH_BASIC = "icons/basic/";
	
	// images
	
	// overlays
	public final String IMG_MERGEABLE_CONFLICT = "ovr16/confauto_ov.gif";
	public final String IMG_QUESTIONABLE = "ovr16/question_ov.gif";
	public final String IMG_MERGED = "ovr16/merged_ov.gif";
	
	// objects
	public final String IMG_REPOSITORY = "obj16/repository_rep.gif";
	public final String IMG_TAG = "obj16/tag.gif";
	public final String IMG_BRANCHES_CATEGORY = "obj16/branches_rep.gif";
	public final String IMG_VERSIONS_CATEGORY = "obj16/versions_rep.gif";
	public final String IMG_PROJECT_VERSION = "obj16/prjversions_rep.gif";
	
	// toolbar
	public final String IMG_REFRESH = "clcl16/refresh.gif";
	public final String IMG_CLEAR = "clcl16/clear_co.gif";
	
	// wizards
	public final String IMG_NEWLOCATION = "wizards/newlocation_wiz.gif";
	
	// preferences
	public final String PREF_SHOW_COMMENTS = "pref_show_comments";
	public final String PREF_SHOW_TAGS = "pref_show_tags";
	public final String PREF_PRUNE_EMPTY_DIRECTORIES = "pref_prune_empty_directories";	
	public final String PREF_TIMEOUT = "pref_timeout";	
	public final String PREF_QUIETNESS = "pref_quietness";
	public final String PREF_SHOW_MODULES = "pref_show_modules";
	public final String PREF_HISTORY_TRACKS_SELECTION = "pref_history_tracks_selection";
	public final String PREF_CVS_RSH = "pref_cvs_rsh";
	public final String PREF_CVS_SERVER = "pref_cvs_server";
	public final String PREF_CONSIDER_CONTENTS = "pref_consider_contents";
	public final String PREF_PROMPT_ON_FILE_DELETE = "pref_prompt_on_file_delete";
	public final String PREF_PROMPT_ON_FOLDER_DELETE = "pref_prompt_on_folder_delete";
	public final String PREF_SHOW_MARKERS = "pref_show_markers";

	// console preferences
	public final String PREF_CONSOLE_COMMAND_COLOR = "pref_console_command_color";
	public final String PREF_CONSOLE_MESSAGE_COLOR = "pref_console_message_color";
	public final String PREF_CONSOLE_ERROR_COLOR = "pref_console_error_color";
	public final String PREF_CONSOLE_FONT = "pref_console_font";
	public final String PREF_CONSOLE_AUTO_OPEN = "pref_console_auto_open";
		
	// decorator preferences
	public final String PREF_FILETEXT_DECORATION = "pref_filetext_decoration";
	public final String PREF_FOLDERTEXT_DECORATION = "pref_foldertext_decoration";
	public final String PREF_PROJECTTEXT_DECORATION = "pref_projecttext_decoration";
	
	public final String PREF_SHOW_DIRTY_DECORATION = "pref_show_overlaydirty";
	public final String PREF_SHOW_ADDED_DECORATION = "pref_show_added";
	public final String PREF_SHOW_HASREMOTE_DECORATION = "pref_show_hasremote";
	
	public final String PREF_DIRTY_FLAG = "pref_dirty_flag";
	public final String PREF_ADDED_FLAG = "pref_added_flag";
	
	public final String PREF_CALCULATE_DIRTY = "pref_calculate_dirty";	

	// Wizard banners
	public final String IMG_WIZBAN_SHARE = "wizban/newconnect_wizban.gif";	
	public final String IMG_WIZBAN_BRANCH = "wizban/newstream_wizban.gif";	
	public final String IMG_WIZBAN_MERGE = "wizban/mergestream_wizban.gif";	

	// Properties
	public final String PROP_NAME = "cvs.name";
	public final String PROP_REVISION = "cvs.revision";
	public final String PROP_AUTHOR = "cvs.author";
	public final String PROP_COMMENT = "cvs.comment";
	public final String PROP_DATE = "cvs.date";
	public final String PROP_DIRTY = "cvs.dirty";
	public final String PROP_MODIFIED = "cvs.modified";
	public final String PROP_KEYWORD = "cvs.date";
	public final String PROP_TAG = "cvs.tag";
	public final String PROP_PERMISSIONS = "cvs.permissions";
	public final String PROP_HOST = "cvs.host";
	public final String PROP_USER = "cvs.user";
	public final String PROP_METHOD = "cvs.method";
	public final String PROP_PORT = "cvs.port";
	public final String PROP_ROOT = "cvs.root";
}

