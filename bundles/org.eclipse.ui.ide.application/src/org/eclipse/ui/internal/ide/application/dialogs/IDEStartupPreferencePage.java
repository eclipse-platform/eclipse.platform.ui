/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Eric Rizzo - removed "prompt for workspace on startup" checkbox
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application.dialogs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.dialogs.StartupPreferencePage;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Extends the Startup and Shutdown preference page with IDE-specific settings.
 * 
 * Note: want IDE settings to appear in main Workbench preference page (via subclassing),
 *   however the superclass, StartupPreferencePage, is internal
 * @since 3.0
 */
public class IDEStartupPreferencePage extends StartupPreferencePage implements
        IWorkbenchPreferencePage {

    private Button refreshButton;

    private Button exitPromptButton;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage
     */
    protected Control createContents(Composite parent) {

    	PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.STARTUP_PREFERENCE_PAGE);

        Composite composite = createComposite(parent);

        createRefreshWorkspaceOnStartupPref(composite);
        createExitPromptPref(composite);

        Label space = new Label(composite,SWT.NONE);
		space.setLayoutData(new GridData());
        
        createEarlyStartupSelection(composite);

        return composite;
    }

    /**
     * The default button has been pressed.
     */
    protected void performDefaults() {
        IPreferenceStore store = getIDEPreferenceStore();

        refreshButton
                .setSelection(store
                        .getDefaultBoolean(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP));
        exitPromptButton
                .setSelection(store
                        .getDefaultBoolean(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW));

        super.performDefaults();
    }

    /**
     * The user has pressed Ok. Store/apply this page's values appropriately.
     */
    public boolean performOk() {
        IPreferenceStore store = getIDEPreferenceStore();

        // store the refresh workspace on startup setting
        store.setValue(IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP,
                refreshButton.getSelection());

        // store the exit prompt on last window close setting
        store.setValue(IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW,
                exitPromptButton.getSelection());

        IDEWorkbenchPlugin.getDefault().savePluginPreferences();

        return super.performOk();
    }

    protected void createRefreshWorkspaceOnStartupPref(Composite composite) {
        refreshButton = new Button(composite, SWT.CHECK);
        refreshButton.setText(IDEWorkbenchMessages.StartupPreferencePage_refreshButton);
        refreshButton.setFont(composite.getFont());
        refreshButton.setSelection(getIDEPreferenceStore().getBoolean(
                IDEInternalPreferences.REFRESH_WORKSPACE_ON_STARTUP));
    }

    protected void createExitPromptPref(Composite composite) {
        exitPromptButton = new Button(composite, SWT.CHECK);
        exitPromptButton.setText(IDEWorkbenchMessages.StartupPreferencePage_exitPromptButton);
        exitPromptButton.setFont(composite.getFont());
        exitPromptButton.setSelection(getIDEPreferenceStore().getBoolean(
                IDEInternalPreferences.EXIT_PROMPT_ON_CLOSE_LAST_WINDOW));
    }

    /**
     * Returns the IDE preference store.
     */
    protected IPreferenceStore getIDEPreferenceStore() {
        return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
    }
}
