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

package org.eclipse.ui.internal;

import org.eclipse.ui.PlatformUI;

/**
 * General constants used by the workbench.
 */
public interface IWorkbenchConstants {

	// Workbench Extension Point Names
	public static final String PL_ACTION_SETS = "actionSets"; //$NON-NLS-1$
	public static final String PL_VIEW_ACTIONS = "viewActions"; //$NON-NLS-1$
	public static final String PL_EDITOR_ACTIONS = "editorActions"; //$NON-NLS-1$
	public static final String PL_PERSPECTIVES ="perspectives"; //$NON-NLS-1$
	public static final String PL_PERSPECTIVE_EXTENSIONS ="perspectiveExtensions"; //$NON-NLS-1$
	public static final String PL_ACTION_SET_PART_ASSOCIATIONS ="actionSetPartAssociations"; //$NON-NLS-1$
	public static final String PL_PREFERENCES ="preferencePages"; //$NON-NLS-1$
	public static final String PL_PROPERTY_PAGES ="propertyPages"; //$NON-NLS-1$
	public static final String PL_EDITOR ="editors"; //$NON-NLS-1$
	public static final String PL_VIEWS ="views"; //$NON-NLS-1$
	public static final String PL_POPUP_MENU ="popupMenus"; //$NON-NLS-1$
	public static final String PL_IMPORT ="importWizards"; //$NON-NLS-1$
	public static final String PL_EXPORT ="exportWizards"; //$NON-NLS-1$
	public static final String PL_NEW ="newWizards"; //$NON-NLS-1$
	public static final String PL_ELEMENT_FACTORY ="elementFactories"; //$NON-NLS-1$
	public static final String PL_DROP_ACTIONS ="dropActions"; //$NON-NLS-1$
	public static final String PL_MARKER_IMAGE_PROVIDER ="markerImageProviders"; //$NON-NLS-1$
	public static final String PL_MARKER_HELP ="markerHelp"; //$NON-NLS-1$
	public static final String PL_MARKER_RESOLUTION ="markerResolution"; //$NON-NLS-1$
	public static final String PL_ACCELERATOR_CONFIGURATIONS ="acceleratorConfigurations"; //$NON-NLS-1$
	public static final String PL_ACCELERATOR_SCOPES ="acceleratorScopes"; //$NON-NLS-1$
	public static final String PL_ACCELERATOR_SETS ="acceleratorSets"; //$NON-NLS-1$
	public static final String PL_ACTION_DEFINITIONS ="actionDefinitions"; //$NON-NLS-1$
	public static final String PL_CAPABILITIES = "capabilities"; //$NON-NLS-1$
	public static final String PL_WORKINGSETS = "workingSets"; //$NON-NLS-1$	
	
	/**
	 * @deprecated Extension point no longer applicable with new project capability
	 */
	public static final String PL_PROJECT_NATURE_IMAGES ="projectNatureImages"; //$NON-NLS-1$
		
	// Key binding service defaults
	// TODO: review the need for these constants
	public static final String ACCELERATOR_CONFIGURATION_ID = "acceleratorConfigurationId"; //$NON-NLS-1$
	public static final String DEFAULT_ACCELERATOR_CONFIGURATION_ID = "org.eclipse.ui.defaultAcceleratorConfiguration"; //$NON-NLS-1$
	public static final String DEFAULT_ACCELERATOR_SCOPE_ID = "org.eclipse.ui.globalScope"; //$NON-NLS-1$
	
	//mappings for type/extension to an editor - backward compatibility only.
	public final static String EDITOR_FILE_NAME = "editors.xml"; //$NON-NLS-1$
	public final static String RESOURCE_TYPE_FILE_NAME = "resourcetypes.xml"; //$NON-NLS-1$

	// Filename containing the workbench's preferences 
	public static final String PREFERENCE_BUNDLE_FILE_NAME = "workbench.ini"; //$NON-NLS-1$

	// Identifier for visible view parts. 
	public static final String WORKBENCH_VISIBLE_VIEW_ID = "Workbench.visibleViewID";  //$NON-NLS-1$

	// String to show in preference dialog as root node of workbench preferences
	public static final String WORKBENCH_PREFERENCE_CATEGORY_ID = PlatformUI.PLUGIN_ID + ".preferencePages.Workbench"; //$NON-NLS-1$

	// Identifier of workbench info properties page
	public static final String WORKBENCH_PROPERTIES_PAGE_INFO = PlatformUI.PLUGIN_ID + ".propertypages.info.file"; //$NON-NLS-1$
	
	// Default layout.
	public static final String DEFAULT_LAYOUT_ID = PlatformUI.PLUGIN_ID + ".resourcePerspective";       //$NON-NLS-1$

	// Various editor.
	public static final String DEFAULT_EDITOR_ID = PlatformUI.PLUGIN_ID + ".DefaultTextEditor"; //$NON-NLS-1$
	public static final String OLE_EDITOR_ID = PlatformUI.PLUGIN_ID + ".OleEditor"; //$NON-NLS-1$
	public static final String SYSTEM_EDITOR_ID = PlatformUI.PLUGIN_ID + ".SystemEditor"; //$NON-NLS-1$

	// Default view category.
	public static final String DEFAULT_CATEGORY_ID = PlatformUI.PLUGIN_ID;

	// Persistance tags.
	public static final String TAG_ID = "id"; //$NON-NLS-1$
	public static final String TAG_FOCUS = "focus"; //$NON-NLS-1$
	public static final String TAG_EDITOR = "editor"; //$NON-NLS-1$
	public static final String TAG_DELETED_EDITOR = "deletedEditor"; //$NON-NLS-1$
	public static final String TAG_EDITORS = "editors"; //$NON-NLS-1$
	public static final String TAG_WORKBOOK = "workbook"; //$NON-NLS-1$
	public static final String TAG_ACTIVE_WORKBOOK = "activeWorkbook"; //$NON-NLS-1$
	public static final String TAG_AREA = "editorArea"; //$NON-NLS-1$
	public static final String TAG_AREA_VISIBLE = "editorAreaVisible"; //$NON-NLS-1$
	public static final String TAG_INPUT = "input"; //$NON-NLS-1$
	public static final String TAG_FACTORY_ID = "factoryID"; //$NON-NLS-1$
	public static final String TAG_TITLE = "title"; //$NON-NLS-1$
	public static final String TAG_X = "x"; //$NON-NLS-1$
	public static final String TAG_Y = "y"; //$NON-NLS-1$
	public static final String TAG_WIDTH = "width"; //$NON-NLS-1$
	public static final String TAG_HEIGHT = "height"; //$NON-NLS-1$
	public static final String TAG_FOLDER = "folder"; //$NON-NLS-1$
	public static final String TAG_INFO = "info"; //$NON-NLS-1$
	public static final String TAG_PART = "part"; //$NON-NLS-1$
	public static final String TAG_RELATIVE = "relative"; //$NON-NLS-1$
	public static final String TAG_RELATIONSHIP = "relationship"; //$NON-NLS-1$
	public static final String TAG_RATIO = "ratio"; //$NON-NLS-1$
	public static final String TAG_ACTIVE_PAGE_ID = "activePageID"; //$NON-NLS-1$
	public static final String TAG_PAGE = "page"; //$NON-NLS-1$
	public static final String TAG_LABEL = "label"; //$NON-NLS-1$
	public static final String TAG_CONTENT = "content"; //$NON-NLS-1$
	public static final String TAG_CLASS = "class"; //$NON-NLS-1$
	public static final String TAG_FILE = "file"; //$NON-NLS-1$
	public static final String TAG_DESCRIPTOR = "descriptor"; //$NON-NLS-1$
	public static final String TAG_MAIN_WINDOW = "mainWindow"; //$NON-NLS-1$
	public static final String TAG_DETACHED_WINDOW = "detachedWindow"; //$NON-NLS-1$
	public static final String TAG_HIDDEN_WINDOW = "hiddenWindow"; //$NON-NLS-1$
	public static final String TAG_WORKBENCH = "workbench"; //$NON-NLS-1$
	public static final String TAG_WINDOW = "window"; //$NON-NLS-1$
	public static final String TAG_VERSION = "version"; //$NON-NLS-1$
	public static final String TAG_PERSPECTIVES = "perspectives"; //$NON-NLS-1$
	public static final String TAG_PERSPECTIVE = "perspective"; //$NON-NLS-1$
	public static final String TAG_ACTIVE_PERSPECTIVE = "activePerspective"; //$NON-NLS-1$
	public static final String TAG_ACTIVE_PART = "activePart"; //$NON-NLS-1$
	public static final String TAG_ACTION_SET = "actionSet"; //$NON-NLS-1$
	public static final String TAG_ALWAYS_ON_ACTION_SET = "alwaysOnActionSet"; //$NON-NLS-1$
	public static final String TAG_ALWAYS_OFF_ACTION_SET = "alwaysOffActionSet"; //$NON-NLS-1$
	public static final String TAG_SHOW_VIEW_ACTION = "show_view_action"; //$NON-NLS-1$
	public static final String TAG_SHOW_IN_TIME = "show_in_time"; //$NON-NLS-1$
	public static final String TAG_TIME = "time"; //$NON-NLS-1$
	public static final String TAG_NEW_WIZARD_ACTION = "new_wizard_action"; //$NON-NLS-1$
	public static final String TAG_PERSPECTIVE_ACTION = "perspective_action"; //$NON-NLS-1$
	public static final String TAG_VIEW = "view"; //$NON-NLS-1$
	public static final String TAG_LAYOUT = "layout"; //$NON-NLS-1$
	public static final String TAG_EXTENSION = "extension"; //$NON-NLS-1$
	public static final String TAG_NAME = "name"; //$NON-NLS-1$
	public static final String TAG_IMAGE = "image"; //$NON-NLS-1$
	public static final String TAG_LAUNCHER = "launcher"; //$NON-NLS-1$
	public static final String TAG_PLUGING = "plugin"; //$NON-NLS-1$
	public static final String TAG_INTERNAL = "internal"; //$NON-NLS-1$
	public static final String TAG_OPEN_IN_PLACE = "open_in_place"; //$NON-NLS-1$
	public static final String TAG_PROGRAM_NAME = "program_name"; //$NON-NLS-1$
	public static final String TAG_FAST_VIEWS = "fastViews"; //$NON-NLS-1$
	public static final String TAG_VIEW_STATE = "viewState"; //$NON-NLS-1$
	public static final String TAG_SINGLETON="singleton"; //$NON-NLS-1$
	public static final String TAG_EDITOR_REUSE_THRESHOLD="editorReuseThreshold"; //$NON-NLS-1$
	public static final String TAG_PERSISTABLE = "persistable";	//$NON-NLS-1$
	public static final String TAG_MRU_LIST = "mruList";	//$NON-NLS-1$
	public static final String TAG_PERSPECTIVE_HISTORY = "perspHistory";	//$NON-NLS-1$	
	public static final String TAG_WORKING_SET_MANAGER = "workingSetManager"; //$NON-NLS-1$		
	public static final String TAG_WORKING_SETS = "workingSets";	//$NON-NLS-1$	
	public static final String TAG_WORKING_SET = "workingSet";	//$NON-NLS-1$		
	public static final String TAG_ITEM = "item";	//$NON-NLS-1$			
	public static final String TAG_EDIT_PAGE_ID = "editPageId";	//$NON-NLS-1$
	public static final String TAG_TOOLBAR_LAYOUT = "toolbarLayout";	//$NON-NLS-1$
	public static final String TAG_ITEM_SIZE = "itemSize";	//$NON-NLS-1$
	public static final String TAG_ITEM_WRAP_INDEX = "wrapIndex";	//$NON-NLS-1$
	public static final String TAG_INDEX = "index";	//$NON-NLS-1$
	public static final String TAG_PINNED = "pinned";	//$NON-NLS-1$
	public static final String TAG_PATH = "path";//$NON-NLS-1$
	public static final String TAG_TOOLTIP = "tooltip";//$NON-NLS-1$
	public static final String TAG_VIEWS = "views";//$NON-NLS-1$
	public static final String TAG_ADDED = "added";//$NON-NLS-1$
	public static final String TAG_POSITION = "position";//$NON-NLS-1$
	public static final String TAG_NAVIGATION_HISTORY = "navigationHistory";//$NON-NLS-1$
	public static final String TAG_ACTIVE = "active";//$NON-NLS-1$
	public static final String TAG_REMOVED = "removed";//$NON-NLS-1$
	public static final String TAG_HISTORY_LABEL = "historyLabel";//$NON-NLS-1$
}
