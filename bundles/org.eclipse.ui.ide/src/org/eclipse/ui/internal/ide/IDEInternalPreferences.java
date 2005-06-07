/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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

    public static final String PSPM_PROMPT = MessageDialogWithToggle.PROMPT; //$NON-NLS-1$

    public static final String PSPM_ALWAYS = MessageDialogWithToggle.ALWAYS; //$NON-NLS-1$

    public static final String PSPM_NEVER = MessageDialogWithToggle.NEVER; //$NON-NLS-1$

    // (boolean) Whether or not to display the Welcome dialog on startup.
    public static final String WELCOME_DIALOG = "WELCOME_DIALOG"; //$NON-NLS-1$
}
