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
package org.eclipse.ui;


/**
 * Action ids for standard actions, groups in the workbench menu bar, and
 * global actions.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * <h3>Standard menus</h3>
 * <ul>
 *   <li>File menu (<code>M_FILE</code>)</li>
 *   <li>Edit menu (<code>M_EDIT</code>)</li>
 *   <li>View menu (<code>M_VIEW</code>)</li>
 *   <li>Workbench menu (<code>M_WORKBENCH</code>)</li>
 *   <li>Window menu (<code>M_WINDOW</code>)</li>
 *   <li>Help menu (<code>M_HELP</code>)</li>
 * </ul>
 * <h3>Standard group for adding top level menus</h3>
 * <ul>
 *   <li>Extra top level menu group (<code>MB_ADDITIONS</code>)</li>
 * </ul>
 * <h3>Global actions</h3>
 * <ul>
 *   <li>Undo (<code>UNDO</code>)</li>
 *   <li>Redo (<code>REDO</code>)</li>
 *   <li>Cut (<code>CUT</code>)</li>
 *   <li>Copy (<code>COPY</code>)</li>
 *   <li>Paste (<code>PASTE</code>)</li>
 *   <li>Delete (<code>DELETE</code>)</li>
 *   <li>Find (<code>FIND</code>)</li>
 *   <li>Select All (<code>SELECT_ALL</code>)</li>
 *   <li>Add Bookmark (<code>BOOKMARK</code>)</li>
 * </ul>
 * <h3>Standard File menu actions</h3>
 * <ul>
 *   <li>Start group (<code>FILE_START</code>)</li>
 *   <li>End group (<code>FILE_END</code>)</li>
 *   <li>New action (<code>NEW</code>)</li>
 *   <li>Extra New-like action group (<code>NEW_EXT</code>)</li>
 *   <li>Close action (<code>CLOSE</code>)</li>
 *   <li>Close All action (<code>CLOSE_ALL</code>)</li>
 *   <li>Extra Close-like action group (<code>CLOSE_EXT</code>)</li>
 *   <li>Save action (<code>SAVE</code>)</li>
 *   <li>Save As action (<code>SAVE_AS</code>)</li>
 *   <li>Save All action (<code>SAVE_ALL</code>)</li>
 *   <li>Extra Save-like action group (<code>SAVE_EXT</code>)</li>
 *   <li>Import action (<code>IMPORT</code>)</li>
 *   <li>Export action (<code>EXPORT</code>)</li>
 *   <li>Extra Import-like action group (<code>IMPORT_EXT</code>)</li>
 *   <li>Quit action (<code>QUIT</code>)</li>
 * </ul>
 * <h3>Standard Edit menu actions</h3>
 * <ul>
 *   <li>Start group (<code>EDIT_START</code>)</li>
 *   <li>End group (<code>EDIT_END</code>)</li>
 *   <li>Undo global action (<code>UNDO</code>)</li>
 *   <li>Redo global action (<code>REDO</code>)</li>
 *   <li>Extra Undo-like action group (<code>UNDO_EXT</code>)</li>
 *   <li>Cut global action (<code>CUT</code>)</li>
 *   <li>Copy global action (<code>COPY</code>)</li>
 *   <li>Paste global action (<code>PASTE</code>)</li>
 *   <li>Extra Cut-like action group (<code>CUT_EXT</code>)</li>
 *   <li>Delete global action (<code>DELETE</code>)</li>
 *   <li>Find global action (<code>FIND</code>)</li>
 *   <li>Select All global action (<code>SELECT_ALL</code>)</li>
 *   <li>Bookmark global action (<code>BOOKMARK</code>)</li>
 * </ul>
 * <h3>Standard Perspective menu actions</h3>
 * <ul>
 *   <li>Extra Perspective-like action group (<code>VIEW_EXT</code>)</li>
 * </ul>
 * <h3>Standard Workbench menu actions</h3>
 * <ul>
 *   <li>Start group (<code>WB_START</code>)</li>
 *   <li>End group (<code>WB_END</code>)</li>
 *   <li>Extra Build-like action group (<code>BUILD_EXT</code>)</li>
 *   <li>Build action (<code>BUILD</code>)</li>
 *   <li>Rebuild All action (<code>REBUILD_ALL</code>)</li>
 * </ul>
 * <h3>Standard Window menu actions</h3>
 * <ul>
 *   <li>Extra Window-like action group (<code>WINDOW_EXT</code>)</li>
 * </ul>
 * <h3>Standard Help menu actions</h3>
 * <ul>
 *   <li>Start group (<code>HELP_START</code>)</li>
 *   <li>End group (<code>HELP_END</code>)</li>
 *   <li>About action (<code>ABOUT</code>)</li>
 * </ul>
 * <h3>Standard pop-up menu groups</h3>
 * <ul>
 *   <li>Managing group (<code>GROUP_MANAGING</code>)</li>
 *   <li>Reorganize group (<code>GROUP_REORGANIZE</code>)</li>
 *   <li>Add group (<code>GROUP_ADD</code>)</li>
 *   <li>File group (<code>GROUP_FILE</code>)</li>
 * </ul>
 */
public interface IWorkbenchActionConstants {

// Standard menus:
	/**
	 * <p>
	 * [Issue: MENU_PREFIX is "". It is used to prefix some of the other
	 * constants. There doesn't seem to be much point for this.
	 * Recommend deleting it.
	 * ]
	 * </p>
	 */
	public static final String MENU_PREFIX = ""; //$NON-NLS-1$

	/**
	 * <p>
	 * [Issue: SEP is "/". It is not used anywhere. Recommend deleting it.]
	 * </p>
	 */
	public static final String SEP = "/"; //$NON-NLS-1$

	/**
	 * Name of standard File menu (value <code>"file"</code>).
	 */
	public static final String M_FILE = MENU_PREFIX+"file"; //$NON-NLS-1$

	/**
	 * Name of standard Edit menu (value <code>"edit"</code>).
	 */
	public static final String M_EDIT = MENU_PREFIX+"edit"; //$NON-NLS-1$

	/**
	 * Name of standard Perspective menu (value <code>"view"</code>).
	 */
	public static final String M_VIEW = MENU_PREFIX+"view"; //$NON-NLS-1$

	/**
	 * Name of standard Workbench menu (value <code>"workbench"</code>).
	 */
	public static final String M_WORKBENCH = MENU_PREFIX+"workbench"; //$NON-NLS-1$

// menu reorg

	/**
	 * Name of standard Navigate menu (value <code>"navigate"</code>).
	 */
	public static final String M_NAVIGATE = MENU_PREFIX+"navigate"; //$NON-NLS-1$

	/**
	 * Name of standard Project menu (value <code>"project"</code>).
	 */
	public static final String M_PROJECT = MENU_PREFIX+"project"; //$NON-NLS-1$

// end menu reorg

	/**
	 * Name of standard Window menu (value <code>"window"</code>).
	 */
	public static final String M_WINDOW = MENU_PREFIX+"window"; //$NON-NLS-1$

	/**
	 * Name of Launch window menu (value <code>"launch"</code>).
	 */
	public static final String M_LAUNCH = MENU_PREFIX + "launch"; //$NON-NLS-1$

	/**
	 * Name of standard Help menu (value <code>"help"</code>).
	 */
	public static final String M_HELP = MENU_PREFIX+"help"; //$NON-NLS-1$
		
// Standard area for adding top level menus:
	/**
	 * Name of group for adding new top-level menus (value <code>"additions"</code>).
	 */
	public static final String MB_ADDITIONS = "additions";	// Group. //$NON-NLS-1$
	
// Standard file actions:
	/**
	 * File menu: name of group for start of menu (value <code>"fileStart"</code>).
	 */
	public static final String FILE_START = "fileStart";	// Group. //$NON-NLS-1$
	
	/**
	 * File menu: name of group for end of menu (value <code>"fileEnd"</code>).
	 */
	public static final String FILE_END = "fileEnd";		// Group. //$NON-NLS-1$
	
	/**
	 * File menu: name of standard New action (value <code>"new"</code>).
	 */
	public static final String NEW = "new"; //$NON-NLS-1$
	
	/**
	 * File menu: name of group for extra New-like actions (value <code>"new.ext"</code>).
	 */
	public static final String NEW_EXT = "new.ext";			// Group. //$NON-NLS-1$
	
	/**
	 * File menu: name of standard Close action (value <code>"close"</code>).
	 */
	public static final String CLOSE = "close"; //$NON-NLS-1$
	
	/**
	 * File menu: name of standard Close All action (value <code>"closeAll"</code>).
	 */
	public static final String CLOSE_ALL = "closeAll"; //$NON-NLS-1$
	
	/**
	 * File menu: name of group for extra Close-like actions (value <code>"close.ext"</code>).
	 */
	public static final String CLOSE_EXT = "close.ext";		// Group. //$NON-NLS-1$
	
	/**
	 * File menu: name of standard Save action (value <code>"save"</code>).
	 */
	public static final String SAVE = "save"; //$NON-NLS-1$
	
	/**
	 * File menu: name of standard Save As action (value <code>"saveAs"</code>).
	 */
	public static final String SAVE_AS = "saveAs"; 	 //$NON-NLS-1$
	
	/**
	 * File menu: name of standard Save All action (value <code>"saveAll"</code>).
	 */
	public static final String SAVE_ALL = "saveAll"; //$NON-NLS-1$
	
	/**
	 * File menu: name of group for extra Save-like actions (value <code>"save.ext"</code>).
	 */
	public static final String SAVE_EXT = "save.ext";		// Group. //$NON-NLS-1$

	/**
	 * File menu: name of standard Print global action 
	 * (value <code>"print"</code>).
	 */
	public static final String PRINT = "print"; 			// Global action. //$NON-NLS-1$
	
	/**
	 * File menu: name of standard Import action (value <code>"import"</code>).
	 */
	public static final String IMPORT = "import"; //$NON-NLS-1$
	
	/**
	 * File menu: name of standard Export action (value <code>"export"</code>).
	 */
	public static final String EXPORT = "export"; //$NON-NLS-1$
	
	/**
	 * File menu: name of group for extra Import-like actions (value <code>"import.ext"</code>).
	 */
	public static final String IMPORT_EXT = "import.ext";	// Group. //$NON-NLS-1$
	
	/**
	 * File menu: name of "Most Recently Used File" group.
	 * (value <code>"mru"</code>).
	 */
	public static final String MRU = "mru"; //$NON-NLS-1$
	
	/**
	 * File menu: name of standard Quit action (value <code>"quit"</code>).
	 */
	public static final String QUIT = "quit"; //$NON-NLS-1$

// Standard edit actions:
	/**
	 * Edit menu: name of group for start of menu (value <code>"editStart"</code>).
	 */
	public static final String EDIT_START = "editStart";	// Group. //$NON-NLS-1$
		
	/**
	 * Edit menu: name of group for end of menu (value <code>"editEnd"</code>).
	 */
	public static final String EDIT_END = "editEnd";		// Group. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Undo global action 
	 * (value <code>"undo"</code>).
	 */
	public static final String UNDO = "undo"; 				// Global action. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Redo global action 
	 * (value <code>"redo"</code>).
	 */
	public static final String REDO = "redo"; 				// Global action. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of group for extra Undo-like actions (value <code>"undo.ext"</code>).
	 */
	public static final String UNDO_EXT = "undo.ext";		// Group. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Cut global action 
	 * (value <code>"cut"</code>).
	 */
	public static final String CUT = "cut"; 				// Global action. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Copy global action
	 * (value <code>"copy"</code>).
	 */
	public static final String COPY = "copy"; 				// Global action. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Paste global action 
	 * (value <code>"paste"</code>).
	 */
	public static final String PASTE = "paste"; 			// Global action. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of group for extra Cut-like actions (value <code>"cut.ext"</code>).
	 */
	public static final String CUT_EXT = "cut.ext";			// Group. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Delete global action 
	 * (value <code>"delete"</code>).
	 */
	public static final String DELETE = "delete"; 			// Global action. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Find global action
	 * (value <code>"find"</code>).
	 */
	public static final String FIND = "find"; 				// Global action. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of group for extra Find-like actions (value <code>"find.ext"</code>).
	 */
	public static final String FIND_EXT = "cut.ext";			// Group. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Select All global action
	 * (value <code>"selectAll"</code>).
	 */
	public static final String SELECT_ALL = "selectAll";		// Global action. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Add Bookmark global action
	 * (value <code>"bookmark"</code>).
	 */
	public static final String BOOKMARK = "bookmark"; 		// Global action. //$NON-NLS-1$

	/**
	 * Edit menu: name of standard Add Task global action
	 * (value <code>"addTask"</code>).
	 */
	public static final String ADD_TASK = "addTask"; 		// Global action. //$NON-NLS-1$
		
	/**
	 * Edit menu: name of group for extra Add-like actions (value <code>"add.ext"</code>).
	 */
	public static final String ADD_EXT = "add.ext";			// Group. //$NON-NLS-1$

// Standard workbench actions:
	/**
	 * Workbench menu: name of group for start of menu
	 * (value <code>"wbStart"</code>).
	 */
	public static final String WB_START = "wbStart";		// Group. //$NON-NLS-1$
	
	/**
	 * Workbench menu: name of group for end of menu
	 * (value <code>"wbEnd"</code>).
	 */
	public static final String WB_END = "wbEnd";			// Group. //$NON-NLS-1$
	
	/**
	 * Workbench menu: name of group for extra Build-like actions
	 * (value <code>"build.ext"</code>).
	 */
	public static final String BUILD_EXT = "build.ext";		// Group. //$NON-NLS-1$
	
	/**
	 * Workbench menu: name of standard Build action 
	 * (value <code>"build"</code>).
	 */
	public static final String BUILD = "build"; //$NON-NLS-1$
	
	/**
	 * Workbench menu: name of standard Rebuild All action 
	 * (value <code>"rebuildAll"</code>).
	 */
	public static final String REBUILD_ALL = "rebuildAll"; //$NON-NLS-1$


// Workbench toolbar ids:
	/**
	 * Workbench toolbar id for file toolbar group.
	 * 
	 * @since 2.1
	 */
	public static final String TOOLBAR_FILE = "org.eclipse.ui.workbench.file"; //$NON-NLS-1$

	/**
	 * Workbench toolbar id for navigate toolbar group.
	 * 
	 * @since 2.1
	 */
	public static final String TOOLBAR_NAVIGATE = "org.eclipse.ui.workbench.navigate"; //$NON-NLS-1$

// Workbench toolbar group ids.  To add an item at the beginning of the group, 
// use the GROUP id.  To add an item at the end of the group, use the EXT id.
	/**
	 * Group ids for navigate toolbar groups.
	 * 
	 * @since 2.1
	 */
	public static final String PIN_GROUP = "pin.group"; //$NON-NLS-1$
	public static final String HISTORY_GROUP = "history.group"; //$NON-NLS-1$
	/**
	 * Group ids for file toolbar groups
	 * 
	 * @since 2.1
	 */
	public static final String NEW_GROUP = "new.group"; //$NON-NLS-1$
	public static final String SAVE_GROUP = "save.group"; //$NON-NLS-1$
	public static final String BUILD_GROUP = "build.group"; //$NON-NLS-1$

// Pop-up menu groups:
	/**
	 * Pop-up menu: name of group for Managing actions (value <code>"group.managing"</code>).
	 */
	public static final String GROUP_MANAGING = "group.managing"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for Reorganize actions (value <code>"group.reorganize"</code>).
	 */
	public static final String GROUP_REORGANIZE = "group.reorganize"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for Add actions (value <code>"group.add"</code>).
	 */
	public static final String GROUP_ADD = "group.add"; //$NON-NLS-1$
	
	/**
	 * Pop-up menu: name of group for File actions (value <code>"group.file"</code>).
	 */
	public static final String GROUP_FILE = "group.file"; //$NON-NLS-1$

	/**
	 * Pop-up menu: name of group for Show In actions (value <code>"group.showIn"</code>).
	 * 
	 * @since 2.1
	 */
	public static final String GROUP_SHOW_IN = "group.showIn"; //$NON-NLS-1$

	/**
	 * Toolbar: name of group for editor action bars.
	 */
	public static final String GROUP_EDITOR = "group.editor"; //$NON-NLS-1$
	
// Standard view actions:
	/**
	 * View menu: name of group for additional view-like items.
	 * (value <code>"additions"</code>).
	 */
	public static final String VIEW_EXT = MB_ADDITIONS;	// Group.
	
// Standard window actions:
	/**
	 * Window menu: name of group for additional window-like items.
	 * (value <code>"additions"</code>).
	 */
	public static final String WINDOW_EXT = MB_ADDITIONS;	// Group.

	/**
	 * Launch menu: name of group for launching additional windows.
	 * (value <code>"additions"</code>).
	 */
	public static final String LAUNCH_EXT = MB_ADDITIONS;	// Group.
	
// menu reorg
	/**
	 * File menu: name of standard Revert global action
	 * (value <code>"revert"</code>).
	 */
	public static final String REVERT = "revert";	// Global action. //$NON-NLS-1$

	/**
	 * File menu: name of standard Refresh global action
	 * (value <code>"refresh"</code>).
	 */
	public static final String REFRESH = "refresh";	// Global action. //$NON-NLS-1$

	/**
	 * File menu: name of standard Properties global action
	 * (value <code>"properties"</code>).
	 */
	public static final String PROPERTIES = "properties";	// Global action. //$NON-NLS-1$

	/**
	 * Edit menu: name of standard Move global action
	 * (value <code>"move"</code>).
	 */
	public static final String MOVE = "move";	// Global action. //$NON-NLS-1$

	/**
	 * Edit menu: name of standard Rename global action
	 * (value <code>"rename"</code>).
	 */
	public static final String RENAME = "rename";	// Global action. //$NON-NLS-1$

	/**
	 * Edit menu: name of standard Add Task global action
	 * (value <code>"addTask"</code>).
	 */
//	public static final String ADD_TASK = "addTask";	// Global action. //$NON-NLS-1$

	/**
	 * Navigate menu: name of group for start of menu
	 * (value <code>"navStart"</code>).
	 */
	public static final String NAV_START = "navStart";	// Group. //$NON-NLS-1$

	/**
	 * Navigate menu: name of group for end of menu
	 * (value <code>"navEnd"</code>).
	 */
	public static final String NAV_END = "navEnd";		// Group. //$NON-NLS-1$

	/**
	 * Navigate menu: name of group for extra Open actions
	 * (value <code>"open.ext"</code>).
	 */
	public static final String OPEN_EXT = "open.ext";		// Group. //$NON-NLS-1$

	/**
	 * Navigate menu: name of group for extra Show actions
	 * (value <code>"show.ext"</code>).
	 */
	public static final String SHOW_EXT = "show.ext";		// Group. //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Go Into global action
	 * (value <code>"goInto"</code>).
	 */
	public static final String GO_INTO = "goInto";	// Global action. //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Go To submenu
	 * (value <code>"goTo"</code>).
	 */
	public static final String GO_TO = "goTo";	//$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Go To Resource global action
	 * (value <code>"goToResource"</code>).
	 */
	public static final String GO_TO_RESOURCE = "goToResource";	// Global action. //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Sync With Editor global action (value
	 * <code>"syncEditor"</code>).
	 * 
	 * @deprecated this action will be removed soon; use SHOW_IN instead 
	 */
	public static final String SYNC_EDITOR = "syncEditor";	// Global action. //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Show In... action 
	 * (value <code>"showIn"</code>).
	 * 
	 * @see org.eclipse.ui.internal.ShowInAction
	 * @since 2.1
	 * 
	 * @deprecated
	 */
	public static final String SHOW_IN = "showIn";	//$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Back global action
	 * (value <code>"back"</code>).
	 */
	public static final String BACK = "back";	// Global action. //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Forward global action
	 * (value <code>"forward"</code>).
	 */
	public static final String FORWARD = "forward";	// Global action. //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Up global action
	 * (value <code>"up"</code>).
	 */
	public static final String UP = "up";	// Global action. //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Next global action
	 * (value <code>"next"</code>).
	 */
	public static final String NEXT = "next";	// Global action. //$NON-NLS-1$

	/**
	 * Navigate menu: name of standard Up global action
	 * (value <code>"previous"</code>).
	 */
	public static final String PREVIOUS = "previous";	// Global action. //$NON-NLS-1$

	/**
	 * Project menu: name of group for start of menu
	 * (value <code>"projStart"</code>).
	 */
	public static final String PROJ_START = "projStart";	// Group. //$NON-NLS-1$

	/**
	 * Project menu: name of group for start of menu
	 * (value <code>"projEnd"</code>).
	 */
	public static final String PROJ_END = "projEnd";	// Group. //$NON-NLS-1$

	/**
	 * Project menu: name of standard Build Project global action
	 * (value <code>"buildProject"</code>).
	 */
	public static final String BUILD_PROJECT = "buildProject";	// Global action. //$NON-NLS-1$

	/**
	 * Project menu: name of standard Rebuild Project global action
	 * (value <code>"rebuildProject"</code>).
	 */
	public static final String REBUILD_PROJECT = "rebuildProject";	// Global action. //$NON-NLS-1$

	/**
	 * Project menu: name of standard Open Project global action
	 * (value <code>"openProject"</code>).
	 */
	public static final String OPEN_PROJECT = "openProject";	// Global action. //$NON-NLS-1$

	/**
	 * Project menu: name of standard Close Project global action
	 * (value <code>"closeProject"</code>).
	 */
	public static final String CLOSE_PROJECT = "closeProject";	// Global action. //$NON-NLS-1$
// end menu reorg


// Standard help actions:
	/**
	 * Help menu: name of group for start of menu
	 * (value <code>"helpStart"</code>).
	 */
	public static final String HELP_START = "helpStart";	// Group. //$NON-NLS-1$
	
	/**
	 * Help menu: name of group for end of menu
	 * (value <code>"helpEnd"</code>).
	 */
	public static final String HELP_END = "helpEnd";		// Group. //$NON-NLS-1$
	
	/**
	 * Help menu: name of standard About action 
	 * (value <code>"about"</code>).
	 */
	public static final String ABOUT = "about"; //$NON-NLS-1$

	/**
	 * Standard global actions in a workbench window.
	 */
	public static final String [] GLOBAL_ACTIONS = {
		UNDO,
		REDO,
		CUT,
		COPY,
		PASTE,
		PRINT,
		DELETE,
		FIND,
		SELECT_ALL,
		BOOKMARK
	};
}
