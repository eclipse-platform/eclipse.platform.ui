/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

public interface IPreferenceIds {
	public static final String PREFIX = TeamUIPlugin.ID + "."; //$NON-NLS-1$

	// Sync Viewer
	public static final String SYNCVIEW_COMPRESS_FOLDERS = PREFIX + "compress_folders"; //$NON-NLS-1$
	public static final String SYNCVIEW_SELECTED_MODE = PREFIX + "syncview_selected_mode"; //$NON-NLS-1$
	
	public static final String SYNCVIEW_DEFAULT_PERSPECTIVE = PREFIX + "syncview_default_perspective"; //$NON-NLS-1$
	public static final String SYNCVIEW_DEFAULT_PERSPECTIVE_NONE = PREFIX + "sync_view_perspective_none"; //$NON-NLS-1$
	
	public static final String SYNCVIEW_VIEW_SYNCINFO_IN_LABEL = PREFIX + "view_syncinfo_in_label"; //$NON-NLS-1$
	
	public static final String SYNCVIEW_VIEW_PROMPT_WHEN_NO_CHANGES = PREFIX + "syncview_promptwhennochanges"; //$NON-NLS-1$
	public static final String SYNCVIEW_VIEW_PROMPT_WITH_CHANGES = PREFIX + "syncview_promptwithchanges"; //$NON-NLS-1$
	
	public static final String SYNCVIEW_VIEW_BKG_PROMPT_WHEN_NO_CHANGES = PREFIX + "syncview__bkg_promptwhennochanges"; //$NON-NLS-1$
	public static final String SYNCVIEW_VIEW_BKG_PROMPT_WITH_CHANGES = PREFIX + "syncview_bkg_promptwithchanges"; //$NON-NLS-1$
	
	public static final String SYNCVIEW_VIEW_SMART_MODE_SWITCH = PREFIX + "syncview_smartmode"; //$NON-NLS-1$
}
