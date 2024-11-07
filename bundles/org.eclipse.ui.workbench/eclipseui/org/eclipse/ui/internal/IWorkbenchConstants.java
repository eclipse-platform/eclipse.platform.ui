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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440136
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.ui.PlatformUI;

/**
 * General constants used by the workbench.
 */
public interface IWorkbenchConstants {

	/**
	 * @deprecated
	 */
	@Deprecated
	String ACCELERATOR_CONFIGURATION_ID = "acceleratorConfigurationId"; //$NON-NLS-1$

	/**
	 * @deprecated
	 */
	@Deprecated
	String DEFAULT_ACCELERATOR_CONFIGURATION_ID = "org.eclipse.ui.defaultAcceleratorConfiguration"; //$NON-NLS-1$

	/**
	 * @deprecated
	 */
	@Deprecated
	String DEFAULT_ACCELERATOR_SCOPE_ID = "org.eclipse.ui.globalScope"; //$NON-NLS-1$

	// ID of the MPerspectiveStack in the IDE e4 model
	String PERSPECTIVE_STACK_ID = "org.eclipse.ui.ide.perspectivestack"; //$NON-NLS-1$

	// ID of the main MTrimBar in the IDE e4 model
	String MAIN_TOOLBAR_ID = "org.eclipse.ui.main.toolbar"; //$NON-NLS-1$

	// ID of the main MMenu in the IDE e4 model
	String MAIN_MENU_ID = "org.eclipse.ui.main.menu"; //$NON-NLS-1$

	// mappings for type/extension to an editor - backward compatibility only.
	String EDITOR_FILE_NAME = "editors.xml"; //$NON-NLS-1$

	String RESOURCE_TYPE_FILE_NAME = "resourcetypes.xml"; //$NON-NLS-1$

	// Filename containing the workbench's preferences
	String PREFERENCE_BUNDLE_FILE_NAME = "workbench.ini"; //$NON-NLS-1$

	// Identifier for visible view parts.
	String WORKBENCH_VISIBLE_VIEW_ID = "Workbench.visibleViewID"; //$NON-NLS-1$

	// Identifier of workbench info properties page
	String WORKBENCH_PROPERTIES_PAGE_INFO = PlatformUI.PLUGIN_ID + ".propertypages.info.file"; //$NON-NLS-1$

	// Various editor.
	String OLE_EDITOR_ID = PlatformUI.PLUGIN_ID + ".OleEditor"; //$NON-NLS-1$

	// Default view category.
	String DEFAULT_CATEGORY_ID = PlatformUI.PLUGIN_ID;

	String TAG_ID = "id"; //$NON-NLS-1$

	String TAG_FOCUS = "focus"; //$NON-NLS-1$

	String TAG_EDITOR = "editor"; //$NON-NLS-1$

	String TAG_DEFAULT_EDITOR = "defaultEditor"; //$NON-NLS-1$

	String TAG_DELETED_EDITOR = "deletedEditor"; //$NON-NLS-1$

	String TAG_EDITORS = "editors"; //$NON-NLS-1$

	String TAG_WORKBOOK = "workbook"; //$NON-NLS-1$

	String TAG_AREA = "editorArea"; //$NON-NLS-1$

	String TAG_AREA_VISIBLE = "editorAreaVisible"; //$NON-NLS-1$

	String TAG_AREA_TRIM_STATE = "editorAreaTrimState"; //$NON-NLS-1$

	String TAG_INPUT = "input"; //$NON-NLS-1$

	String TAG_FACTORY_ID = "factoryID"; //$NON-NLS-1$

	String TAG_EDITOR_STATE = "editorState"; //$NON-NLS-1$

	String TAG_TITLE = "title"; //$NON-NLS-1$

	String TAG_X = "x"; //$NON-NLS-1$

	String TAG_Y = "y"; //$NON-NLS-1$

	String TAG_WIDTH = "width"; //$NON-NLS-1$

	String TAG_HEIGHT = "height"; //$NON-NLS-1$

	String TAG_MINIMIZED = "minimized"; //$NON-NLS-1$

	String TAG_MAXIMIZED = "maximized"; //$NON-NLS-1$

	String TAG_FOLDER = "folder"; //$NON-NLS-1$

	String TAG_INFO = "info"; //$NON-NLS-1$

	String TAG_PART = "part"; //$NON-NLS-1$

	String TAG_PART_NAME = "partName"; //$NON-NLS-1$

	String TAG_PROPERTIES = "properties"; //$NON-NLS-1$

	String TAG_PROPERTY = "property"; //$NON-NLS-1$

	String TAG_RELATIVE = "relative"; //$NON-NLS-1$

	String TAG_RELATIONSHIP = "relationship"; //$NON-NLS-1$

	String TAG_RATIO = "ratio"; //$NON-NLS-1$

	String TAG_RATIO_LEFT = "ratioLeft"; //$NON-NLS-1$

	String TAG_RATIO_RIGHT = "ratioRight"; //$NON-NLS-1$

	String TAG_ACTIVE_PAGE_ID = "activePageID"; //$NON-NLS-1$

	String TAG_EXPANDED = "expanded"; //$NON-NLS-1$

	String TAG_PAGE = "page"; //$NON-NLS-1$

	String TAG_INTRO = "intro"; //$NON-NLS-1$

	String TAG_LABEL = "label"; //$NON-NLS-1$

	String TAG_CONTENT = "content"; //$NON-NLS-1$

	String TAG_CLASS = "class"; //$NON-NLS-1$

	String TAG_USE_DEPENDENCY_INJECTION = "inject"; //$NON-NLS-1$

	String TAG_FILE = "file"; //$NON-NLS-1$

	String TAG_DESCRIPTOR = "descriptor"; //$NON-NLS-1$

	String TAG_MAIN_WINDOW = "mainWindow"; //$NON-NLS-1$

	String TAG_DETACHED_WINDOW = "detachedWindow"; //$NON-NLS-1$

	String TAG_HIDDEN_WINDOW = "hiddenWindow"; //$NON-NLS-1$

	String TAG_WORKBENCH = "workbench"; //$NON-NLS-1$

	String TAG_WINDOW = "window"; //$NON-NLS-1$

	String TAG_VERSION = "version"; //$NON-NLS-1$

	String TAG_PROGRESS_COUNT = "progressCount"; //$NON-NLS-1$

	String TAG_PERSPECTIVES = "perspectives"; //$NON-NLS-1$

	String TAG_PERSPECTIVE = "perspective"; //$NON-NLS-1$

	String TAG_ACTIVE_PERSPECTIVE = "activePerspective"; //$NON-NLS-1$

	String TAG_ACTIVE_PART = "activePart"; //$NON-NLS-1$

	String TAG_ACTION_SET = "actionSet"; //$NON-NLS-1$

	String TAG_ALWAYS_ON_ACTION_SET = "alwaysOnActionSet"; //$NON-NLS-1$

	String TAG_SHOW_VIEW_ACTION = "show_view_action"; //$NON-NLS-1$

	String TAG_SHOW_IN_TIME = "show_in_time"; //$NON-NLS-1$

	String TAG_TIME = "time"; //$NON-NLS-1$

	String TAG_NEW_WIZARD_ACTION = "new_wizard_action"; //$NON-NLS-1$

	String TAG_PERSPECTIVE_ACTION = "perspective_action"; //$NON-NLS-1$

	String TAG_HIDE_MENU = "hide_menu_item_id"; //$NON-NLS-1$

	String TAG_HIDE_TOOLBAR = "hide_toolbar_item_id"; //$NON-NLS-1$

	String TAG_VIEW = "view"; //$NON-NLS-1$

	String TAG_LAYOUT = "layout"; //$NON-NLS-1$

	String TAG_EXTENSION = "extension"; //$NON-NLS-1$

	String TAG_CONTENT_TYPE = "contentType"; //$NON-NLS-1$

	String TAG_NAME = "name"; //$NON-NLS-1$

	String TAG_IMAGE = "image"; //$NON-NLS-1$

	String TAG_LAUNCHER = "launcher"; //$NON-NLS-1$

	String TAG_PLUGIN = "plugin"; //$NON-NLS-1$

	/** deprecated - use TAG_OPEN_MODE */
	String TAG_INTERNAL = "internal"; //$NON-NLS-1$

	/** deprecated - use TAG_OPEN_MODE */
	String TAG_OPEN_IN_PLACE = "open_in_place"; //$NON-NLS-1$

	String TAG_PROGRAM_NAME = "program_name"; //$NON-NLS-1$

	String TAG_FAST_VIEWS = "fastViews"; //$NON-NLS-1$

	String TAG_FAST_VIEW_BAR = "fastViewBar"; //$NON-NLS-1$

	String TAG_FAST_VIEW_BARS = "fastViewBars"; //$NON-NLS-1$

	String TAG_CLOSEABLE = "closeable";//$NON-NLS-1$


	String TAG_PRESENTATION = "presentation"; //$NON-NLS-1$

	String TAG_STANDALONE = "standalone";//$NON-NLS-1$

	String TAG_SHOW_TITLE = "showTitle";//$NON-NLS-1$

	String TAG_VIEW_STATE = "viewState"; //$NON-NLS-1$

	String TAG_PERSISTABLE = "persistable"; //$NON-NLS-1$

	String TAG_MRU_LIST = "mruList"; //$NON-NLS-1$

	String TAG_WORKING_SET_MANAGER = "workingSetManager"; //$NON-NLS-1$

	String TAG_WORKING_SETS = "workingSets"; //$NON-NLS-1$

	String TAG_WORKING_SET = "workingSet"; //$NON-NLS-1$

	String TAG_ITEM = "item"; //$NON-NLS-1$

	String TAG_EDIT_PAGE_ID = "editPageId"; //$NON-NLS-1$


	String TAG_INDEX = "index"; //$NON-NLS-1$

	String TAG_PINNED = "pinned"; //$NON-NLS-1$

	String TAG_PATH = "path";//$NON-NLS-1$

	String TAG_TOOLTIP = "tooltip";//$NON-NLS-1$

	String TAG_VIEWS = "views";//$NON-NLS-1$

	String TAG_POSITION = "position";//$NON-NLS-1$

	String TAG_NAVIGATION_HISTORY = "navigationHistory";//$NON-NLS-1$

	String TAG_STICKY_STATE = "stickyState"; //$NON-NLS-1$

	String TAG_ACTIVE = "active";//$NON-NLS-1$

	String TAG_REMOVED = "removed";//$NON-NLS-1$

	String TAG_HISTORY_LABEL = "historyLabel";//$NON-NLS-1$

	String TAG_LOCKED = "locked";//$NON-NLS-1$

	String TAG_OPEN_MODE = "openMode"; //$NON-NLS-1$

	String TAG_STARTUP = "startup"; //$NON-NLS-1$

	String TAG_FAST_VIEW_SIDE = "fastViewLocation"; //$NON-NLS-1$

	String TAG_THEME = "theme";//$NON-NLS-1$

	String TAG_VIEW_LAYOUT_REC = "viewLayoutRec"; //$NON-NLS-1$

	String TAG_PERSPECTIVE_BAR = "perspectiveBar"; //$NON-NLS-1$

	String TAG_TRIM = "trimLayout"; //$NON-NLS-1$

	String TAG_TRIM_AREA = "trimArea"; //$NON-NLS-1$

	String TAG_TRIM_ITEM = "trimItem"; //$NON-NLS-1$

	// Fonts
	String SMALL_FONT = "org.eclipse.ui.smallFont"; //$NON-NLS-1$

	// Colors
	String COLOR_HIGHLIGHT = "org.eclipse.ui.highlight"; //$NON-NLS-1$

}
