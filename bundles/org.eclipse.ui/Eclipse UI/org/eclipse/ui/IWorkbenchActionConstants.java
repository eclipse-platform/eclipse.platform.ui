package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
 *   <li>Workspace menu (<code>M_WORKBENCH</code>)</li>
 *   <li>View menu (<code>M_VIEW</code>)</li>
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
 * <h3>Standard Workbench menu actions</h3>
 * <ul>
 *   <li>Start group (<code>WB_START</code>)</li>
 *   <li>End group (<code>WB_END</code>)</li>
 *   <li>Extra Build-like action group (<code>BUILD_EXT</code>)</li>
 *   <li>Build action (<code>BUILD</code>)</li>
 *   <li>Rebuild All action (<code>REBUILD_ALL</code>)</li>
 * </ul>
 * <h3>Standard View menu actions</h3>
 * <ul>
 *   <li>Extra View-like action group (<code>VIEW_EXT</code>)</li>
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
	 * Name of standard Workspace menu (value <code>"workbench"</code>).
	 */
	public static final String M_WORKBENCH = MENU_PREFIX+"workbench"; //$NON-NLS-1$

	/**
	 * Name of standard View menu (value <code>"view"</code>).
	 */
	public static final String M_VIEW = MENU_PREFIX+"view"; //$NON-NLS-1$

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
	 * Edit menu: name of standard Select All global action
	 * (value <code>"selectAll"</code>).
	 */
	public static final String SELECT_ALL = "selectAll";	// Global action. //$NON-NLS-1$
	
	/**
	 * Edit menu: name of standard Bookmark global action
	 * (value <code>"bookmark"</code>).
	 */
	public static final String BOOKMARK = "bookmark"; 		// Global action. //$NON-NLS-1$

// Standard workspace actions:
	/**
	 * Workspace menu: name of group for start of menu
	 * (value <code>"wbstart"</code>).
	 */
	public static final String WB_START = "wbStart";		// Group. //$NON-NLS-1$
	
	/**
	 * Workspace menu: name of group for end of menu
	 * (value <code>"wbEnd"</code>).
	 */
	public static final String WB_END = "wbEnd";			// Group. //$NON-NLS-1$
	
	/**
	 * Workspace menu: name of group for extra Build-like actions
	 * (value <code>"build.ext"</code>).
	 */
	public static final String BUILD_EXT = "build.ext";		// Group. //$NON-NLS-1$
	
	/**
	 * Workspace menu: name of standard Build action 
	 * (value <code>"build"</code>).
	 */
	public static final String BUILD = "build"; //$NON-NLS-1$
	
	/**
	 * Workspace menu: name of standard Rebuild All action 
	 * (value <code>"rebuildAll"</code>).
	 */
	public static final String REBUILD_ALL = "rebuildAll"; //$NON-NLS-1$

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
