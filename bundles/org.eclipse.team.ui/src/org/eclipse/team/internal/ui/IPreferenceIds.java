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

	/*
	 * Default model provider layout to use with SubscriberSynchronizePage. The user can configure but this
	 * is used to select the initial layout.
	 */
	public static final String SYNCVIEW_COMPRESS_FOLDERS = PREFIX + "compress_folders"; //$NON-NLS-1$
	
	/*
	 * Preference to enable displaying synchronization state in the elements label. This
	 * preference is used automatically with {@link StructuredViewerAdvisor}.
	 */
	public static final String SYNCVIEW_VIEW_SYNCINFO_IN_LABEL = PREFIX + "view_syncinfo_in_label"; //$NON-NLS-1$
	
	/*
	 * Preference to manage the perspective used to synchronize.
	 */
	public static final String SYNCVIEW_DEFAULT_PERSPECTIVE = PREFIX + "syncview_default_perspective"; //$NON-NLS-1$
	public static final String SYNCVIEW_DEFAULT_PERSPECTIVE_NONE = PREFIX + "sync_view_perspective_none"; //$NON-NLS-1$
	public static final String SYNCHRONIZING_COMPLETE_PERSPECTIVE = PREFIX + "sychronizing_default_perspective_to_show"; //$NON-NLS-1$
	public static final String SYNCHRONIZING_COMPLETE_PERSPECTIVE_PROMPT = PREFIX + "sychronizing_default_perspective_to_show_prompt"; //$NON-NLS-1$
	public static final String SYNCHRONIZING_COMPLETE_PERSPECTIVE_ALWAYS = PREFIX + "sychronizing_default_perspective_to_show_always"; //$NON-NLS-1$
	public static final String SYNCHRONIZING_COMPLETE_PERSPECTIVE_NEVER = PREFIX + "sychronizing_default_perspective_to_show_never"; //$NON-NLS-1$
	
	/*
	 * Preference to save the last participant selected via the global synchronize action.
	 */
	public static final String SYNCHRONIZING_DEFAULT_PARTICIPANT = PREFIX + "sychronizing_default_participant"; //$NON-NLS-1$
}