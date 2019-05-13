/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.bookmarkexplorer;

import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the bookmark view.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 *
 */
interface IBookmarkHelpContextIds {
	String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

	// Actions
	String COPY_BOOKMARK_ACTION = PREFIX
			+ "copy_bookmark_action_context"; //$NON-NLS-1$

	String PASTE_BOOKMARK_ACTION = PREFIX
			+ "paste_bookmark_action_context"; //$NON-NLS-1$

	String REMOVE_BOOKMARK_ACTION = PREFIX
			+ "remove_bookmark_action_context"; //$NON-NLS-1$

	String OPEN_BOOKMARK_ACTION = PREFIX
			+ "open_bookmark_action_context"; //$NON-NLS-1$

	String SELECT_ALL_BOOKMARK_ACTION = PREFIX
			+ "select_all_bookmark_action_context"; //$NON-NLS-1$

	String BOOKMARK_PROPERTIES_ACTION = PREFIX
			+ "bookmark_properties_action_context"; //$NON-NLS-1$

	String SORT_ASCENDING_ACTION = PREFIX
			+ "bookmark_sort_ascending_action_context"; //$NON-NLS-1$

	String SORT_DESCENDING_ACTION = PREFIX
			+ "bookmark_sort_descending_action_context"; //$NON-NLS-1$

	String SORT_DESCRIPTION_ACTION = PREFIX
			+ "bookmark_sort_description_action_context"; //$NON-NLS-1$

	String SORT_RESOURCE_ACTION = PREFIX
			+ "bookmark_sort_resource_action_context"; //$NON-NLS-1$

	String SORT_FOLDER_ACTION = PREFIX
			+ "bookmark_sort_folder_action_context"; //$NON-NLS-1$

	String SORT_LOCATION_ACTION = PREFIX
			+ "bookmark_sort_location_action_context"; //$NON-NLS-1$

	String SORT_CREATION_TIME_ACTION = PREFIX
			+ "bookmark_sort_creation_time_action_context"; //$NON-NLS-1$

	// Views
	String BOOKMARK_VIEW = PREFIX + "bookmark_view_context"; //$NON-NLS-1$
}
