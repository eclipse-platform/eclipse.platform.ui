/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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
 *     Dina Sayed, dsayed@eg.ibm.com, IBM -  bug 269844
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 502050
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.handlers.ShowInSystemExplorerHandler;
import org.eclipse.ui.internal.ide.registry.SystemEditorOrTextEditorStrategy;

/**
 * The IDEPreferenceInitializer is the preference initializer for the IDE
 * preferences.
 */
public class IDEPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {

		IEclipsePreferences node= DefaultScope.INSTANCE.getNode(IDEWorkbenchPlugin.getDefault().getBundle().getSymbolicName());

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
				IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, false);
		node.put(IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE,
				IDEInternalPreferences.PSPM_PROMPT);
		node.put(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS,
				IDEInternalPreferences.PSPM_PROMPT);
		node.putBoolean(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS, false);

		node.putBoolean(IDEInternalPreferences.WARN_ABOUT_WORKSPACE_INCOMPATIBILITY, true);

		// Turning some Help Menu separators on
		node.putBoolean(getHelpSeparatorKey("group.main"), true); //$NON-NLS-1$
		node.putBoolean(getHelpSeparatorKey("group.assist"), true); //$NON-NLS-1$
		node.putBoolean(getHelpSeparatorKey("group.updates"), true); //$NON-NLS-1$

		// Set up marker limits
		node.putBoolean(IDEInternalPreferences.LIMIT_PROBLEMS, true);
		node.putInt(IDEInternalPreferences.PROBLEMS_LIMIT, 100);

		node.putBoolean(IDEInternalPreferences.USE_MARKER_LIMITS, true);
		node.putInt(IDEInternalPreferences.MARKER_LIMITS_VALUE, 100);

		node.put(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_TYPE, ""); //$NON-NLS-1$
		node.putBoolean(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_RELATIVE, true);

		node.put(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE, IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_PROMPT);
		node.put(IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_VIRTUAL_FOLDER_MODE, IDEInternalPreferences.IMPORT_FILES_AND_FOLDERS_MODE_PROMPT);

		node.put(IDEInternalPreferences.WORKBENCH_SYSTEM_EXPLORER, getShowInSystemExplorerCommand());

		node.put(IDE.UNASSOCIATED_EDITOR_STRATEGY_PREFERENCE_KEY, SystemEditorOrTextEditorStrategy.EXTENSION_ID);

		// IDE window title
		node.putBoolean(IDEInternalPreferences.SHOW_LOCATION, false);
		node.putBoolean(IDEInternalPreferences.SHOW_LOCATION_NAME, true);
		node.putBoolean(IDEInternalPreferences.SHOW_PERSPECTIVE_IN_TITLE, false);
		node.putBoolean(IDEInternalPreferences.SHOW_PRODUCT_IN_TITLE, true);
		node.putBoolean(IDEInternalPreferences.SHOW_ACTIVE_EDITOR_INFO_IN_TITLE, true);

		// by default, don't start hidden problems view to show decoration on
		// it's icon. See bug 513901
		node.putBoolean(IDEInternalPreferences.SHOW_PROBLEMS_VIEW_DECORATIONS_ON_STARTUP, false);

		// by default the problem view should check whether help context is really
		// available.
		node.putBoolean(IDEInternalPreferences.HELP_CONTEXT_AVAILABILITY_CHECK, true);
	}

	/**
	 * The default command for launching the system explorer on this platform.
	 *
	 * @return The default command which launches the system explorer on this system, or an empty
	 *         string if no default exists
	 * @see ShowInSystemExplorerHandler#getDefaultCommand()
	 */
	public static String getShowInSystemExplorerCommand() {
		// See https://bugs.eclipse.org/419940 why it is implemented in here and not in ShowInSystemExplorerHandler#getDefaultCommand()
		if (Util.isGtk()) {
			return "dbus-send --print-reply --dest=org.freedesktop.FileManager1 /org/freedesktop/FileManager1 org.freedesktop.FileManager1.ShowItems array:string:\"${selected_resource_uri}\" string:\"\""; //$NON-NLS-1$
		} else if (Util.isWindows()) {
			return "explorer /E,/select=\"${selected_resource_loc}\""; //$NON-NLS-1$
		} else if (Util.isMac()) {
			return "open -R \"${selected_resource_loc}\""; //$NON-NLS-1$
		}

		// if all else fails, return empty default
		return ""; //$NON-NLS-1$
	}

	private String getHelpSeparatorKey(String groupId) {
		return "useSeparator." + IWorkbenchActionConstants.M_HELP + "." + groupId; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
