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

/**
 * The IPreferenceConstants are the internal constants used by the Workbench.
 */
public interface IPreferenceConstants {
	//Do we show tabs up or down for views
	public static final String VIEW_TAB_POSITION = "VIEW_TAB_POSITION"; //$NON-NLS-1$

	//Boolean: true = single click opens editor; false = double click opens it.
	public static final String OPEN_ON_SINGLE_CLICK = "OPEN_ON_SINGLE_CLICK"; //$NON-NLS-1$
	//Boolean: true = select on hover;
	public static final String SELECT_ON_HOVER = "SELECT_ON_HOVER"; //$NON-NLS-1$
	//Boolean: true = open after delay
	public static final String OPEN_AFTER_DELAY = "OPEN_AFTER_DELAY"; //$NON-NLS-1$

	//Do we show color icons in toolbars?
	public static final String COLOR_ICONS = "COLOR_ICONS"; //$NON-NLS-1$
	
	//Do we show tabs up or down for editors
	public static final String EDITOR_TAB_POSITION = "EDITOR_TAB_POSITION"; //$NON-NLS-1$
	
	//mappings for type/extension to an editor
	public final static String EDITORS = "editors"; //$NON-NLS-1$
	public final static String RESOURCES = "resourcetypes"; //$NON-NLS-1$

	//saving perspective layouts
	public final static String PERSPECTIVES = "perspectives"; //$NON-NLS-1$

	// (int) If > 0, an editor will be reused once 'N' editors are opened.
	public static final String REUSE_EDITORS = "REUSE_OPEN_EDITORS"; //$NON-NLS-1$
	//Boolean:	true = replace dirty editor if no other editors to reuse (prompt for save); 
	//			false = open a new editor if no other editors to resuse
	public static final String REUSE_DIRTY_EDITORS = "REUSE_DIRTY_EDITORS"; //$NON-NLS-1$
	//On/Off option for the two preceding options.
	public static final String REUSE_EDITORS_BOOLEAN = "REUSE_OPEN_EDITORS_BOOLEAN"; //$NON-NLS-1$
	
	// (int) N recently viewed files will be listed in the File->Open Recent menu.
	public static final String RECENT_FILES = "RECENT_FILES"; //$NON-NLS-1$
	public static final int MAX_RECENT_FILES_SIZE = 15;

	// (integer) Mode for opening a view.
	public static final String OPEN_VIEW_MODE = "OPEN_VIEW_MODE"; //$NON-NLS-1$
	public static final int OVM_EMBED = 0;
	public static final int OVM_FAST = 1;
	public static final int OVM_FLOAT = 2;

	// (int) Mode for opening a new perspective
	public static final String OPEN_PERSP_MODE = "OPEN_PERSPECTIVE_MODE"; //$NON-NLS-1$
	public static final int OPM_ACTIVE_PAGE = 0;
	//public static final int OPM_NEW_PAGE = 1;
	public static final int OPM_NEW_WINDOW = 2;

	//Identifier for enabled decorators
	public static final String ENABLED_DECORATORS = "ENABLED_DECORATORS"; //$NON-NLS-1$
	
	//List of plugins but that extends "startup" extension point but are overriden by the user.
	//String of plugin unique ids separated by ";"
	public static final String PLUGINS_NOT_ACTIVATED_ON_STARTUP = "PLUGINS_NOT_ACTIVATED_ON_STARTUP"; //$NON-NLS-1$
	
	//Separator for PLUGINS_NOT_ACTIVATED_ON_STARTUP
	public static char SEPARATOR = ';'; //$NON-NLS-1$
	
	//Preference key for default editors
	public final static String DEFAULT_EDITORS = "defaultEditors"; //$NON-NLS-1$

	//Preference key for default editors
	public final static String DEFAULT_EDITORS_CACHE = "defaultEditorsCache"; //$NON-NLS-1$

	//Tab width = tab height * scalar value
	public final static String EDITOR_TAB_WIDTH = "EDITOR_TAB_WIDTH"; //$NON-NLS-1$

	//Boolean: true = show Editors drop down button on CTabFolder 
	public static final String EDITORLIST_PULLDOWN_ACTIVE = "EDITORLIST_PULLDOWN_ACTIVE"; //$NON-NLS-1$

	// Selection scope for EditorList
	public static final String EDITORLIST_SELECTION_SCOPE = "EDITORLIST_SELECTION_SCOPE"; //$NON-NLS-1$
	public static final int EDITORLIST_SET_WINDOW_SCOPE = 0;
	public static final int EDITORLIST_SET_PAGE_SCOPE = 1;
	public static final int EDITORLIST_SET_TAB_GROUP_SCOPE = 2;

	// Sort criteria for EditorList
	public static final String EDITORLIST_SORT_CRITERIA = "EDITORLIST_SORT_CRITERIA"; //$NON-NLS-1$
	public static final int EDITORLIST_NAME_SORT = 0;
	public static final int EDITORLIST_MRU_SORT = 1;
	
	// Boolean; true = EditorList displays full path
	public static final String EDITORLIST_DISPLAY_FULL_NAME = "EDITORLIST_DISPLAY_FULL_NAME"; //$NON-NLS-1$
	
	// Show the shortcut bar in workbench windows
	public static final String SHOW_SHORTCUT_BAR = "SHOW_SHORTCUT_BAR"; //$NON-NLS-1$

	// Show the status line in workbench windows
	public static final String SHOW_STATUS_LINE = "SHOW_STATUS_LINE"; //$NON-NLS-1$

	// Show the toolbar in workbench windows
	public static final String SHOW_TOOL_BAR = "SHOW_TOOL_BAR"; //$NON-NLS-1$
	}
