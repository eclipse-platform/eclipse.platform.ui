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
	
	// Preference Pages
	public static final String PREF_PRUNE = PREFIX + "prune_empty_directories_pref"; //$NON-NLS-1$
	public static final String PREF_QUIET = PREFIX + "quietness_level_pref"; //$NON-NLS-1$
	public static final String PREF_COMPRESSION = PREFIX + "compression_level_pref"; //$NON-NLS-1$
	public static final String PREF_KEYWORDMODE = PREFIX + "default_keywordmode_pref"; //$NON-NLS-1$
	public static final String PREF_COMMS_TIMEOUT = PREFIX + "comms_timeout_pref"; //$NON-NLS-1$
	public static final String PREF_CONSIDER_CONTENT = PREFIX + "consider_content_pref"; //$NON-NLS-1$
	public static final String PREF_MARKERS_ENABLED = PREFIX + "markers_enabled_pref"; //$NON-NLS-1$
	public static final String PREF_REPLACE_DELETE_UNMANAGED = PREFIX + "replace_deletion_of_unmanaged_pref"; //$NON-NLS-1$
}
