/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Dina Sayed, dsayed@eg.ibm.com, IBM -  bug 269844
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.views.markers.MarkerSupportInternalUtilities;

/**
 * The IDEPreferenceInitializer is the preference initializer for the IDE
 * preferences.
 */
public class IDEPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {

		IEclipsePreferences node = new DefaultScope()
				.getNode(IDEWorkbenchPlugin.getDefault().getBundle()
						.getSymbolicName());

		// API preferences

		node.put(IDE.Preferences.PROJECT_OPEN_NEW_PERSPECTIVE,
				IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);

		// Set the workspace selection dialog to open by default
		node.putBoolean(IDE.Preferences.SHOW_WORKSPACE_SELECTION_DIALOG, true);

		// Internal preferences

		node.putBoolean(IDEInternalPreferences.SAVE_ALL_BEFORE_BUILD, false);
		node.putInt(IDEInternalPreferences.SAVE_INTERVAL, 5); // 5 minutes
		node.putBoolean(IDEInternalPreferences.WELCOME_DIALOG, true);
		node.putBoolean(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP,
				false);
		node.putBoolean(
				IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, true);
		node.put(IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE,
				IDEInternalPreferences.PSPM_PROMPT);
		node.put(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS,
				IDEInternalPreferences.PSPM_PROMPT);
		node.putBoolean(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS, false);

		// Turning some Help Menu separators on
		node.putBoolean(getHelpSeparatorKey("group.main"), true); //$NON-NLS-1$
		node.putBoolean(getHelpSeparatorKey("group.assist"), true); //$NON-NLS-1$
		node.putBoolean(getHelpSeparatorKey("group.updates"), true); //$NON-NLS-1$

		// Set up marker limits
		node.putBoolean(IDEInternalPreferences.LIMIT_PROBLEMS, true);
		node.putInt(IDEInternalPreferences.PROBLEMS_LIMIT, 100);
		
		node.putBoolean(IDEInternalPreferences.LIMIT_BOOKMARKS, true);
		node.putInt(IDEInternalPreferences.BOOKMARKS_LIMIT, 100);

		node.putBoolean(IDEInternalPreferences.LIMIT_TASKS, true);
		node.putInt(IDEInternalPreferences.TASKS_LIMIT, 100);
		
		node.putBoolean(IDEInternalPreferences.USE_MARKER_LIMITS, true);
		node.putInt(IDEInternalPreferences.MARKER_LIMITS_VALUE, 100);
		
		node.put(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_TYPE, ""); //$NON-NLS-1$
		node.putBoolean(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_RELATIVE, true);

		//Filter migration
		node.putBoolean(MarkerSupportInternalUtilities.MIGRATE_BOOKMARK_FILTERS, false);
		node.putBoolean(MarkerSupportInternalUtilities.MIGRATE_TASK_FILTERS, false);
		node.putBoolean(MarkerSupportInternalUtilities.MIGRATE_PROBLEM_FILTERS, false);
		
		node.put(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE, IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_PROMPT);
		node.put(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_VIRTUAL_FOLDER_MODE, IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_PROMPT);
	}

	private String getHelpSeparatorKey(String groupId) {
		return "useSeparator." + IWorkbenchActionConstants.M_HELP + "." + groupId; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
