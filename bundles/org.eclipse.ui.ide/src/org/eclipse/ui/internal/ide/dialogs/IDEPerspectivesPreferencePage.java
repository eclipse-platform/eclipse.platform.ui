/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.PerspectivesPreferencePage;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Extends the Perspectives preference page with IDE-specific settings.
 * 
 * Note: want IDE settings to appear in main Perspectives preference page (via
 * subclassing), however the superclass, PerspectivesPreferencePage, is
 * internal
 */
public class IDEPerspectivesPreferencePage extends PerspectivesPreferencePage {
    private final String PROJECT_SWITCH_PERSP_MODE_TITLE = IDEWorkbenchMessages.ProjectSwitchPerspectiveMode_optionsTitle;

    private final String PSPM_ALWAYS_TEXT = IDEWorkbenchMessages.ProjectSwitchPerspectiveMode_always;

    private final String PSPM_NEVER_TEXT = IDEWorkbenchMessages.ProjectSwitchPerspectiveMode_never;

    private final String PSPM_PROMPT_TEXT = IDEWorkbenchMessages.ProjectSwitchPerspectiveMode_prompt;

    private RadioGroupFieldEditor projectSwitchField;

    /**
     * Creates the page's UI content.
     */
    protected Control createContents(Composite parent) {
        // @issue if the product subclasses this page, then it should provide
        // the help content
    	PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(
						parent,
						org.eclipse.ui.internal.IWorkbenchHelpContextIds.PERSPECTIVES_PREFERENCE_PAGE);

        Composite composite = createComposite(parent);

        createOpenPerspButtonGroup(composite);
        createOpenViewButtonGroup(composite);
        createProjectPerspectiveGroup(composite);
        createCustomizePerspective(composite);

        return composite;
    }

    /**
     * Creates a composite that contains buttons for selecting the preference
     * opening new project selections.
     */
    private void createProjectPerspectiveGroup(Composite composite) {

        Composite projectComposite = new Composite(composite, SWT.NONE);
        projectComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectComposite.setFont(composite.getFont());

        String[][] namesAndValues = {
                { PSPM_ALWAYS_TEXT, IDEInternalPreferences.PSPM_ALWAYS },
                { PSPM_NEVER_TEXT, IDEInternalPreferences.PSPM_NEVER },
                { PSPM_PROMPT_TEXT, IDEInternalPreferences.PSPM_PROMPT } };
        projectSwitchField = new RadioGroupFieldEditor(
                IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE,
                PROJECT_SWITCH_PERSP_MODE_TITLE, namesAndValues.length,
                namesAndValues, projectComposite, true);
        projectSwitchField.setPreferenceStore(getIDEPreferenceStore());
        projectSwitchField.setPage(this);
        projectSwitchField.load();
    }

    /**
     * Returns the IDE preference store.
     */
    protected IPreferenceStore getIDEPreferenceStore() {
        return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.dialogs.PerspectivesPreferencePage#performDefaults()
     */
    protected void performDefaults() {
        projectSwitchField.loadDefault();
        super.performDefaults();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.dialogs.PerspectivesPreferencePage#performOk()
     */
    public boolean performOk() {
        projectSwitchField.store();
        IDEWorkbenchPlugin.getDefault().savePluginPreferences();
        return super.performOk();
    }

}
