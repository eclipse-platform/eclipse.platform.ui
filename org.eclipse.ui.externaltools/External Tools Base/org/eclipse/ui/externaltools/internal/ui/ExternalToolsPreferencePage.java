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
package org.eclipse.ui.externaltools.internal.ui;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Preference page that allows the user to customize external tools
 */
public class ExternalToolsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button promptForToolMigrationButton;
	private Button promptForProjectMigrationButton;
	
	public ExternalToolsPreferencePage() {
		setPreferenceStore(ExternalToolsPlugin.getDefault().getPreferenceStore());
		setDescription(ExternalToolsUIMessages.getString("ExternalToolsPreferencePage.External_tool_project_builders_migration_2")); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IExternalToolsHelpContextIds.EXTERNAL_TOOLS_PREFERENCE_PAGE);
		//The main composite
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight=0;
		layout.marginWidth=0;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());
				
		promptForToolMigrationButton= createCheckButton(composite, ExternalToolsUIMessages.getString("ExternalToolsPreferencePage.Prompt_before_migrating_3"), IPreferenceConstants.PROMPT_FOR_TOOL_MIGRATION); //$NON-NLS-1$
		promptForProjectMigrationButton= createCheckButton(composite, ExternalToolsUIMessages.getString("ExternalToolsPreferencePage.1"), IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION); //$NON-NLS-1$
		
		applyDialogFont(composite);
		
		return composite;
	}
	
	/**
	 * Returns a new check button with the given label for the given preference.
	 */
	private Button createCheckButton(Composite parent, String label, String preferenceKey) {
		Button button= new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		button.setFont(parent.getFont());
		button.setText(label);
		button.setSelection(getPreferenceStore().getBoolean(preferenceKey));
		return button;
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		getPreferenceStore().setValue(IPreferenceConstants.PROMPT_FOR_TOOL_MIGRATION, promptForToolMigrationButton.getSelection());
		getPreferenceStore().setValue(IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION, promptForProjectMigrationButton.getSelection());
		return super.performOk();
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		promptForToolMigrationButton.setSelection(getPreferenceStore().getDefaultBoolean(IPreferenceConstants.PROMPT_FOR_TOOL_MIGRATION));
		promptForToolMigrationButton.setSelection(getPreferenceStore().getDefaultBoolean(IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION));
		super.performDefaults();
	}
}
