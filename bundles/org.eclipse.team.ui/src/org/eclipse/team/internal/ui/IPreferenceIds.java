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
	public static final String SYNCVIEW_BACKGROUND_SYNC = PREFIX + "background_sync"; //$NON-NLS-1$ 
	public static final String SYNCVIEW_SCHEDULED_SYNC = PREFIX + "scheduled_sync"; //$NON-NLS-1$	
	public static final String SYNCVIEW_DELAY = PREFIX + "scheduled_sync_delay"; //$NON-NLS-1$
	public static final String SYNCVIEW_COMPRESS_FOLDERS = PREFIX + "compress_folders"; //$NON-NLS-1$
	public static final String SYNCVIEW_USEBOTHMODE = PREFIX + "syncview_use_bothmode"; //$NON-NLS-1$
	
	public static final String SYNCVIEW_DEFAULT_PERSPECTIVE = PREFIX + "syncview_default_perspective"; //$NON-NLS-1$
	public static final String SYNCVIEW_DEFAULT_PERSPECTIVE_NONE = PREFIX + "sync_view_perspective_none"; //$NON-NLS-1$
	
	public static final String SYNCVIEW_VIEW_TYPE = PREFIX + "view_type"; //$NON-NLS-1$
	public static final String SYNCVIEW_VIEW_TABLESORT = PREFIX + "table_column_sort"; //$NON-NLS-1$
	public static final String SYNCVIEW_VIEW_TABLESORT_REVERSED = PREFIX + "table_column_sort_reversed"; //$NON-NLS-1$
}
