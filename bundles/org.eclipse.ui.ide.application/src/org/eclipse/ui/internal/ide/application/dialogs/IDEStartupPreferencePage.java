/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
 *     Eric Rizzo - removed "prompt for workspace on startup" checkbox
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application.dialogs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.dialogs.StartupPreferencePage;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Extends the Startup and Shutdown preference page with IDE-specific settings.
 *
 * Note: want IDE settings to appear in main Workbench preference page (via
 * subclassing), however the superclass, StartupPreferencePage, is internal
 *
 * @since 3.0
 */
public class IDEStartupPreferencePage extends StartupPreferencePage implements IWorkbenchPreferencePage {

	private Button refreshButton;
	private Button showProblemsButton;
	private Button exitPromptButton;

	@Override
	protected void createExtraContent(Composite composite) {
		refreshButton = createCheckBox(IDEWorkbenchMessages.StartupPreferencePage_refreshButton,
				IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP, composite);
		showProblemsButton = createCheckBox(IDEWorkbenchMessages.StartupPreferencePage_showProblemsButton,
				IDEInternalPreferences.SHOW_PROBLEMS_VIEW_DECORATIONS_ON_STARTUP, composite);
		exitPromptButton = createCheckBox(IDEWorkbenchMessages.StartupPreferencePage_exitPromptButton,
				IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, composite);
		super.createExtraContent(composite);
	}

	/**
	 * The default button has been pressed.
	 */
	@Override
	protected void performDefaults() {
		IPreferenceStore store = getIDEPreferenceStore();

		refreshButton.setSelection(store.getDefaultBoolean(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP));

		showProblemsButton.setSelection(
				store.getDefaultBoolean(IDEInternalPreferences.SHOW_PROBLEMS_VIEW_DECORATIONS_ON_STARTUP));

		exitPromptButton.setSelection(store.getDefaultBoolean(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW));

		super.performDefaults();
	}

	/**
	 * The user has pressed Ok. Store/apply this page's values appropriately.
	 */
	@Override
	public boolean performOk() {
		IPreferenceStore store = getIDEPreferenceStore();

		// store the refresh workspace on startup setting
		store.setValue(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP, refreshButton.getSelection());

		store.setValue(IDEInternalPreferences.SHOW_PROBLEMS_VIEW_DECORATIONS_ON_STARTUP,
				showProblemsButton.getSelection());

		// store the exit prompt on last window close setting
		store.setValue(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW, exitPromptButton.getSelection());

		IDEWorkbenchPlugin.getDefault().savePluginPreferences();

		return super.performOk();
	}

	private Button createCheckBox(String text, String preferenceKey, Composite composite) {
		return createCheckBox(text, getIDEPreferenceStore().getBoolean(preferenceKey), composite);
	}

	/**
	 * Returns the IDE preference store.
	 */
	private static IPreferenceStore getIDEPreferenceStore() {
		return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
	}
}
