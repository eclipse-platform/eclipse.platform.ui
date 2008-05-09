/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.ide;

/**
 * Identifiers for IDE menus, toolbars and groups.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 * Note: want to move IDE-specific stuff out of IWorkbenchActionConstants.
 *   There's still some cleanup to be done here (and there).
 * 
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IIDEActionConstants {

    /**
     * Name of standard File menu (value <code>"file"</code>).
     */
    public static final String M_FILE = "file"; //$NON-NLS-1$

    /**
     * Name of standard Edit menu (value <code>"edit"</code>).
     */
    public static final String M_EDIT = "edit"; //$NON-NLS-1$

    /**
     * Name of standard Navigate menu (value <code>"navigate"</code>).
     */
    public static final String M_NAVIGATE = "navigate"; //$NON-NLS-1$

    /**
     * Name of standard Project menu (value <code>"project"</code>).
     */
    public static final String M_PROJECT = "project"; //$NON-NLS-1$

    /**
     * Name of standard Window menu (value <code>"window"</code>).
     */
    public static final String M_WINDOW = "window"; //$NON-NLS-1$

    /**
     * Name of standard Help menu (value <code>"help"</code>).
     */
    public static final String M_HELP = "help"; //$NON-NLS-1$

    /**
     * File menu: name of group for start of menu (value <code>"fileStart"</code>).
     */
    public static final String FILE_START = "fileStart"; //$NON-NLS-1$

    /**
     * File menu: name of group for end of menu (value <code>"fileEnd"</code>).
     */
    public static final String FILE_END = "fileEnd"; //$NON-NLS-1$

    /**
     * File menu: name of group for extra New-like actions (value <code>"new.ext"</code>).
     */
    public static final String NEW_EXT = "new.ext"; //$NON-NLS-1$

    /**
     * File menu: name of group for extra Close-like actions (value <code>"close.ext"</code>).
     */
    public static final String CLOSE_EXT = "close.ext"; //$NON-NLS-1$

    /**
     * File menu: name of group for extra Save-like actions (value <code>"save.ext"</code>).
     */
    public static final String SAVE_EXT = "save.ext"; //$NON-NLS-1$

    /**
     * File menu: name of group for extra Print-like actions (value <code>"print.ext"</code>).
     */
    public static final String PRINT_EXT = "print.ext"; //$NON-NLS-1$

    /**
     * File menu: name of group for extra Import-like actions (value <code>"import.ext"</code>).
     */
    public static final String IMPORT_EXT = "import.ext"; //$NON-NLS-1$

    /**
     * File menu: name of "Most Recently Used File" group.
     * (value <code>"mru"</code>).
     */
    public static final String MRU = "mru"; //$NON-NLS-1$

    /**
     * Edit menu: name of group for start of menu (value <code>"editStart"</code>).
     */
    public static final String EDIT_START = "editStart"; //$NON-NLS-1$

    /**
     * Edit menu: name of group for end of menu (value <code>"editEnd"</code>).
     */
    public static final String EDIT_END = "editEnd"; //$NON-NLS-1$

    /**
     * Edit menu: name of group for extra Undo-like actions (value <code>"undo.ext"</code>).
     */
    public static final String UNDO_EXT = "undo.ext"; //$NON-NLS-1$

    /**
     * Edit menu: name of group for extra Cut-like actions (value <code>"cut.ext"</code>).
     */
    public static final String CUT_EXT = "cut.ext"; //$NON-NLS-1$

    /**
     * Edit menu: name of group for extra Find-like actions (value <code>"find.ext"</code>).
     * <p>Note: The value of this constant has changed in 3.3 to match the specification;
     * before 3.3, its value was incorrect (<code>"cut.ext"</code>).  See bug 155856 for details.</p>
     */
    public static final String FIND_EXT = "find.ext"; //$NON-NLS-1$

    /**
     * Edit menu: name of group for extra Add-like actions (value <code>"add.ext"</code>).
     */
    public static final String ADD_EXT = "add.ext"; //$NON-NLS-1$

    /**
     * Workbench menu: name of group for extra Build-like actions
     * (value <code>"build.ext"</code>).
     */
    public static final String BUILD_EXT = "build.ext"; //$NON-NLS-1$

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
     * Group id for pin toolbar group.
     * 
     * @since 2.1
     */
    public static final String PIN_GROUP = "pin.group"; //$NON-NLS-1$

    /**
     * Group ids for history toolbar group.
     * 
     * @since 2.1
     */
    public static final String HISTORY_GROUP = "history.group"; //$NON-NLS-1$

    /**
     * Group ids for new toolbar group.
     * 
     * @since 2.1
     */
    public static final String NEW_GROUP = "new.group"; //$NON-NLS-1$

    /**
     * Group ids for save toolbar group.
     * 
     * @since 2.1
     */
    public static final String SAVE_GROUP = "save.group"; //$NON-NLS-1$

    /**
     * Group ids for build toolbar group.
     * 
     * @since 2.1
     */
    public static final String BUILD_GROUP = "build.group"; //$NON-NLS-1$

    // Pop-up menu groups:
    /**
     * Pop-up menu: name of group for Add actions (value <code>"group.add"</code>).
     */
    public static final String GROUP_ADD = "group.add"; //$NON-NLS-1$

    /**
     * Pop-up menu and cool bar: name of group for File actions (value <code>"group.file"</code>).
     */
    public static final String GROUP_FILE = "group.file"; //$NON-NLS-1$

    /**
     * Coolbar: name of group for Navigate actions (value <code>"group.nav"</code>).
     */
    public static final String GROUP_NAV = "group.nav"; //$NON-NLS-1$

    /**
     * Pop-up menu: name of group for Show In actions (value <code>"group.showIn"</code>).
     * 
     * @since 2.1
     */
    public static final String GROUP_SHOW_IN = "group.showIn"; //$NON-NLS-1$

    /**
     * Navigate menu: name of group for start of menu
     * (value <code>"navStart"</code>).
     */
    public static final String NAV_START = "navStart"; //$NON-NLS-1$

    /**
     * Navigate menu: name of group for end of menu
     * (value <code>"navEnd"</code>).
     */
    public static final String NAV_END = "navEnd"; //$NON-NLS-1$

    /**
     * Navigate menu: name of group for extra Open actions
     * (value <code>"open.ext"</code>).
     */
    public static final String OPEN_EXT = "open.ext"; //$NON-NLS-1$

    /**
     * Navigate menu: name of group for extra Show actions
     * (value <code>"show.ext"</code>).
     */
    public static final String SHOW_EXT = "show.ext"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Go Into global action
     * (value <code>"goInto"</code>).
     */
    public static final String GO_INTO = "goInto"; // Global action. //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Go To submenu
     * (value <code>"goTo"</code>).
     */
    public static final String GO_TO = "goTo"; //$NON-NLS-1$

    /**
     * Navigate menu: name of standard Go To Resource global action
     * (value <code>"goToResource"</code>).
     * 
     * Note:should be in an action factory
     */
    public static final String GO_TO_RESOURCE = "goToResource"; // Global action. //$NON-NLS-1$

    /**
     * Project menu: name of group for start of menu
     * (value <code>"projStart"</code>).
     */
    public static final String PROJ_START = "projStart"; //$NON-NLS-1$

    /**
     * Project menu: name of group for start of menu
     * (value <code>"projEnd"</code>).
     */
    public static final String PROJ_END = "projEnd"; //$NON-NLS-1$

    /**
     * Help menu: name of group for start of menu
     * (value <code>"helpStart"</code>).
     */
    public static final String HELP_START = "helpStart"; //$NON-NLS-1$

    /**
     * Help menu: name of group for end of menu
     * (value <code>"helpEnd"</code>).
     */
    public static final String HELP_END = "helpEnd"; //$NON-NLS-1$

}

