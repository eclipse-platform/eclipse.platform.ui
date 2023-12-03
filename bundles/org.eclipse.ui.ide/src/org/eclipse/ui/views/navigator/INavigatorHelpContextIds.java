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
 *   IBM Corporation - initial API and implementation
 *   Sebastian Davids <sdavids@gmx.de> - Collapse all action (25826)
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the resource navigator view.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @noreference This class is not intended to be referenced by clients.
 *
 *              Planned to be deleted, please see Bug
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=549953
 */
/* package */interface INavigatorHelpContextIds {
	public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

	// Actions
	public static final String FILTER_SELECTION_ACTION = PREFIX + "filter_selection_action_context"; //$NON-NLS-1$

	public static final String GOTO_RESOURCE_ACTION = PREFIX + "goto_resource_action_context"; //$NON-NLS-1$

	public static final String RESOURCE_NAVIGATOR_MOVE_ACTION = PREFIX + "resource_navigator_move_action_context"; //$NON-NLS-1$

	public static final String RESOURCE_NAVIGATOR_RENAME_ACTION = PREFIX + "resource_navigator_rename_action_context"; //$NON-NLS-1$

	public static final String SHOW_IN_NAVIGATOR_ACTION = PREFIX + "show_in_navigator_action_context"; //$NON-NLS-1$

	public static final String SORT_VIEW_ACTION = PREFIX + "sort_view_action_context"; //$NON-NLS-1$

	public static final String COPY_ACTION = PREFIX + "resource_navigator_copy_action_context"; //$NON-NLS-1$

	public static final String PASTE_ACTION = PREFIX + "resource_navigator_paste_action_context"; //$NON-NLS-1$

	public static final String COLLAPSE_ALL_ACTION = PREFIX + "collapse_all_action_context"; //$NON-NLS-1$

	// Dialogs
	public static final String GOTO_RESOURCE_DIALOG = PREFIX + "goto_resource_dialog_context"; //$NON-NLS-1$

	// Views
	public static final String RESOURCE_VIEW = PREFIX + "resource_view_context"; //$NON-NLS-1$
}
