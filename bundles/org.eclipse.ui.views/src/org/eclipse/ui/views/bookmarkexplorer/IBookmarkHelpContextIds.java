/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

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
	public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

	// Actions
	public static final String COPY_BOOKMARK_ACTION = PREFIX + "copy_bookmark_action_context"; //$NON-NLS-1$
	public static final String REMOVE_BOOKMARK_ACTION = PREFIX + "remove_bookmark_action_context"; //$NON-NLS-1$
	public static final String OPEN_BOOKMARK_ACTION = PREFIX + "open_bookmark_action_context"; //$NON-NLS-1$
	public static final String SELECT_ALL_BOOKMARK_ACTION = PREFIX + "select_all_bookmark_action_context"; //$NON-NLS-1$

	// Views
	public static final String BOOKMARK_VIEW = PREFIX + "bookmark_view_context"; //$NON-NLS-1$
}
