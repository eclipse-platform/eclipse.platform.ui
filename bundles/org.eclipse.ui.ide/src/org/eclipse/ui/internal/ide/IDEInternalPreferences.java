/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dina Sayed, dsayed@eg.ibm.com, IBM -  bug 269844
 *     Markus Schorn (Wind River Systems) -  bug 284447
 *******************************************************************************/

package org.eclipse.ui.internal.ide;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;

/**
 * The IDEInternalPreferences are the internal constants used by the Workbench.
 */
public interface IDEInternalPreferences {
    // (boolean) Save all dirty editors before running a full or incremental build 
    public static final String SAVE_ALL_BEFORE_BUILD = "SAVE_ALL_BEFORE_BUILD"; //$NON-NLS-1$

    // (boolean) Refresh workspace on startup 
    public static final String REFRESH_WORKSPACE_ON_STARTUP = "REFRESH_WORKSPACE_ON_STARTUP"; //$NON-NLS-1$

    // (int) Workspace save interval in minutes
    // @issue we should drop this and have clients refer to the core preference instead. its not even kept up-to-date if client uses core api directly
    public final static String SAVE_INTERVAL = "saveInterval"; //$NON-NLS-1$

    public static final int MAX_SAVE_INTERVAL = 9999;

    // (boolean) Show Problems view to users when build contains errors
    //public static final String SHOW_TASKS_ON_BUILD = "SHOW_TASKS_ON_BUILD"; //$NON-NLS-1$

    // (boolean) Prompt for exit confirmation when last window closed.
    public static final String EXIT_PROMPT_ON_CLOSE_LAST_WINDOW = "EXIT_PROMPT_ON_CLOSE_LAST_WINDOW"; //$NON-NLS-1$

    // (String) Whether to open the preferred perspective when creating a new project
    public static final String PROJECT_SWITCH_PERSP_MODE = "SWITCH_PERSPECTIVE_ON_PROJECT_CREATION"; //$NON-NLS-1$

    /**
     * (String) Whether to open required projects when opening a project.
     */ 
    public static final String OPEN_REQUIRED_PROJECTS = "OPEN_REQUIRED_PROJECTS"; //$NON-NLS-1$
    
    /**
     * (String) Whether to confirm closing unrelated projects.
     */ 
    public static final String CLOSE_UNRELATED_PROJECTS = "CLOSE_UNRELATED_PROJECTS"; //$NON-NLS-1$

    public static final String PSPM_PROMPT = MessageDialogWithToggle.PROMPT;

    public static final String PSPM_ALWAYS = MessageDialogWithToggle.ALWAYS;

    public static final String PSPM_NEVER = MessageDialogWithToggle.NEVER;

    // (boolean) Whether or not to display the Welcome dialog on startup.
    public static final String WELCOME_DIALOG = "WELCOME_DIALOG"; //$NON-NLS-1$
    
    //Whether or not to limit problems
    public static final String LIMIT_PROBLEMS = "LIMIT_PROBLEMS"; //$NON-NLS-1$
    
    //The list of defined problems filters
    public static final String PROBLEMS_FILTERS = "PROBLEMS_FILTERS"; //$NON-NLS-1$
    
    //problem limits
    public static final String PROBLEMS_LIMIT = "PROBLEMS_LIMIT"; //$NON-NLS-1$
    
    //Whether or not to limit tasks
    public static final String LIMIT_TASKS = "LIMIT_TASKS"; //$NON-NLS-1$
    
    //tasks limits
    public static final String TASKS_LIMIT = "TASKS_LIMIT"; //$NON-NLS-1$
    
    //The list of defined tasks filters
    public static final String TASKS_FILTERS = "TASKS_FILTERS"; //$NON-NLS-1$
    
    //Whether or not to limit bookmarks
    public static final String LIMIT_BOOKMARKS = "LIMIT_BOOKMARKS"; //$NON-NLS-1$
    
    //bookmark limits
    public static final String BOOKMARKS_LIMIT = "BOOKMARKS_LIMIT"; //$NON-NLS-1$
    
//  The list of defined tasks filters
    public static final String BOOKMARKS_FILTERS = "BOOKMARKS_FILTERS"; //$NON-NLS-1$
    
    //Enablement of marker limits
    public static final String USE_MARKER_LIMITS = "USE_MARKER_LIMITS"; //$NON-NLS-1$
    	
   	//Value of marker limits
    public static final String MARKER_LIMITS_VALUE = "MARKER_LIMITS_VALUE"; //$NON-NLS-1$

    // Type of import
    public static final String IMPORT_FILES_AND_FOLDERS_TYPE = "IMPORT_FILES_AND_FOLDERS_TYPE"; //$NON-NLS-1$
    
    // (boolean) Using variable relative paths for the import file and folder dialog
    public static final String IMPORT_FILES_AND_FOLDERS_RELATIVE = "IMPORT_FILES_AND_FOLDERS_RELATIVE"; //$NON-NLS-1$

    // (string) Save all dirty editors before running a full or incremental build 
    public static final String IMPORT_FILES_AND_FOLDERS_MODE = "IMPORT_FILES_AND_FOLDERS_MODE"; //$NON-NLS-1$

    // (string) Save all dirty editors before running a full or incremental build 
    public static final String IMPORT_FILES_AND_FOLDERS_VIRTUAL_FOLDER_MODE = "IMPORT_FILES_AND_FOLDERS_VIRTUAL_FOLDER_MODE"; //$NON-NLS-1$

    public static final String IMPORT_FILES_AND_FOLDERS_MODE_PROMPT = MessageDialogWithToggle.PROMPT;

    public static final String IMPORT_FILES_AND_FOLDERS_MODE_MOVE_COPY = "MOVE_COPY"; //$NON-NLS-1$

    public static final String IMPORT_FILES_AND_FOLDERS_MODE_LINK = "LINK"; //$NON-NLS-1$
    
    public static final String IMPORT_FILES_AND_FOLDERS_MODE_LINK_AND_VIRTUAL_FOLDER = "LINK_AND_VIRTUAL_FOLDER"; //$NON-NLS-1$

    // Always show this import window
    public static final String IMPORT_FILES_AND_FOLDERS_SHOW_DIALOG = "IMPORT_FILES_AND_FOLDERS_SHOW_DIALOG"; //$NON-NLS-1$

    /**
     * Workspace name, will be displayed in the window title.
     */
	public static final String WORKSPACE_NAME = "WORKSPACE_NAME"; //$NON-NLS-1$
}
