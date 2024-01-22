/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.ui.internal;

/**
 * This class defines constants for looking up resources that are available only
 * within the Eclipse UI and Eclipse UI Standard Components projects.
 */
public interface IWorkbenchGraphicConstants {

	String IMG_ETOOL_IMPORT_WIZ = "IMG_ETOOL_IMPORT_WIZ"; //$NON-NLS-1$

	String IMG_ETOOL_EXPORT_WIZ = "IMG_ETOOL_EXPORT_WIZ"; //$NON-NLS-1$

	String IMG_ETOOL_NEW_PAGE = "IMG_ETOOL_NEW_PAGE"; //$NON-NLS-1$

	String IMG_ETOOL_PIN_EDITOR = "IMG_ETOOL_PIN_EDITOR"; //$NON-NLS-1$

	String IMG_ETOOL_PIN_EDITOR_DISABLED = "IMG_ETOOL_PIN_EDITOR_DISABLED"; //$NON-NLS-1$

	String IMG_ETOOL_HELP_CONTENTS = "IMG_ETOOL_HELP_CONTENTS"; //$NON-NLS-1$

	String IMG_ETOOL_HELP_SEARCH = "IMG_ETOOL_HELP_SEARCH"; //$NON-NLS-1$

	String IMG_ETOOL_TIPS_AND_TRICKS = "IMG_ETOOL_TIPS_AND_TRICKS"; //$NON-NLS-1$

	// Fast view enabled and disabled icons
	String IMG_ETOOL_NEW_FASTVIEW = "IMG_ETOOL_NEW_FASTVIEW"; //$NON-NLS-1$
	String IMG_DTOOL_NEW_FASTVIEW = "IMG_DTOOL_NEW_FASTVIEW"; //$NON-NLS-1$

	// TrimStack buttons
	String IMG_ETOOL_RESTORE_TRIMPART = "IMG_ETOOL_RESTORE_TRIMPART"; //$NON-NLS-1$
	String IMG_ETOOL_EDITOR_TRIMPART = "IMG_ETOOL_EDITOR_TRIMPART"; //$NON-NLS-1$

	// local toolbars

	String IMG_LCL_CLOSE_VIEW = "IMG_LCL_CLOSE_VIEW"; //$NON-NLS-1$

	String IMG_LCL_PIN_VIEW = "IMG_LCL_PIN_VIEW"; //$NON-NLS-1$

	String IMG_LCL_MIN_VIEW = "IMG_LCL_MIN_VIEW"; //$NON-NLS-1$

	String IMG_LCL_RENDERED_VIEW_MENU = "IMG_LCL_RENDERED_VIEW_MENU"; //$NON-NLS-1$

	String IMG_LCL_VIEW_MENU = "IMG_LCL_VIEW_MENU"; //$NON-NLS-1$

	String IMG_LCL_BUTTON_MENU = "IMG_LCL_BUTTON_MENU"; //$NON-NLS-1$

	String IMG_LCL_SELECTED_MODE = "IMG_LCL_SELECTED_MODE"; //$NON-NLS-1$

	String IMG_LCL_SHOWCHILD_MODE = "IMG_LCL_SHOWCHILD_MODE"; //$NON-NLS-1$

	String IMG_LCL_SHOWSYNC_RN = "IMG_LCL_SHOWSYNC_RN"; //$NON-NLS-1$

	String IMG_LCL_CLOSE_VIEW_THIN = "IMG_LCL_CLOSE_VIEW_THIN"; //$NON-NLS-1$

	String IMG_LCL_MIN_VIEW_THIN = "IMG_LCL_MIN_VIEW_THIN"; //$NON-NLS-1$

	String IMG_LCL_MAX_VIEW_THIN = "IMG_LCL_MAX_VIEW_THIN"; //$NON-NLS-1$

	String IMG_LCL_RESTORE_VIEW_THIN = "IMG_LCL_RESTORE_VIEW_THIN"; //$NON-NLS-1$

	String IMG_LCL_SHOW_TOOLBAR_THIN = "IMG_LCL_SHOW_TOOLBAR_THIN"; //$NON-NLS-1$

	String IMG_LCL_HIDE_TOOLBAR_THIN = "IMG_LCL_HIDE_TOOLBAR_THIN"; //$NON-NLS-1$

	String IMG_LCL_VIEW_MENU_THIN = "IMG_LCL_VIEW_MENU_THIN"; //$NON-NLS-1$

	// wizard images
	String IMG_WIZBAN_NEW_WIZ = "IMG_WIZBAN_NEW_WIZ"; //$NON-NLS-1$

	String IMG_WIZBAN_EXPORT_WIZ = "IMG_WIZBAN_EXPORT_WIZ"; //$NON-NLS-1$

	String IMG_WIZBAN_IMPORT_WIZ = "IMG_WIZBAN_IMPORT_WIZ"; //$NON-NLS-1$

	String IMG_WIZBAN_EXPORT_PREF_WIZ = "IMG_WIZBAN_EXPORT_PREF_WIZ"; //$NON-NLS-1$

	String IMG_WIZBAN_IMPORT_PREF_WIZ = "IMG_WIZBAN_IMPORT_PREF_WIZ"; //$NON-NLS-1$

	String IMG_WIZBAN_WORKINGSET_WIZ = "IMG_WIZBAN_WORKINGSET_WIZ"; //$NON-NLS-1$

	String IMG_VIEW_DEFAULTVIEW_MISC = "IMG_VIEW_DEFAULTVIEW_MISC"; //$NON-NLS-1$

	/**
	 * Identifies an activity category.
	 *
	 * @since 3.0
	 */
	String IMG_OBJ_ACTIVITY_CATEGORY = "IMG_OBJ_ACTIVITY_CATEGORY"; //$NON-NLS-1$

	/**
	 * Identifies an activity.
	 *
	 * @since 3.0
	 */
	String IMG_OBJ_ACTIVITY = "IMG_OBJ_ACTIVITY"; //$NON-NLS-1$

	/**
	 * Identifies a font.
	 *
	 * @since 3.0
	 */
	String IMG_OBJ_FONT = "IMG_OBJ_FONT"; //$NON-NLS-1$

	/**
	 * Identifies a theme category.
	 *
	 * @since 3.0
	 */
	String IMG_OBJ_THEME_CATEGORY = "IMG_OBJ_THEME_CATEGORY"; //$NON-NLS-1$

	/**
	 * Generic working set icon.
	 *
	 * @since 3.2
	 */
	String IMG_OBJ_WORKING_SETS = "IMG_OBJ_WORKING_SETS"; //$NON-NLS-1$

	/**
	 * Separator icon for selection dialogs.
	 */
	String IMG_OBJ_SEPARATOR = "IMG_OBJ_SEPARATOR"; //$NON-NLS-1$

	/**
	 * Default icon for Quick Access nodes.
	 */
	String IMG_OBJ_NODE = "IMG_OBJ_NODE"; //$NON-NLS-1$

	/**
	 * Default icon for Quick Access elements.
	 */
	String IMG_OBJ_ELEMENT = "IMG_OBJ_ELEMENT"; //$NON-NLS-1$

	/**
	 * Icon for signed objects (such as bundles).
	 *
	 * @since 3.3
	 */
	String IMG_OBJ_SIGNED_YES = "IMG_OBJ_SIGNED_YES"; //$NON-NLS-1$

	/**
	 * Icon for unsigned objects (such as bundles).
	 *
	 * @since 3.3
	 */
	String IMG_OBJ_SIGNED_NO = "IMG_OBJ_SIGNED_NO"; //$NON-NLS-1$

	/**
	 * Icon for objects whos signing state is not known (such as bundles).
	 *
	 * @since 3.3
	 */
	String IMG_OBJ_SIGNED_UNKNOWN = "IMG_OBJ_SIGNED_UNKNOWN"; //$NON-NLS-1$

	String IMG_PREF_IMPORT = "IMG_PREF_IMPORT"; //$NON-NLS-1$
	String IMG_PREF_EXPORT = "IMG_PREF_EXPORT"; //$NON-NLS-1$
}
