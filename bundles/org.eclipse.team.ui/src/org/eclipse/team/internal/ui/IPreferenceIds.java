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
}
