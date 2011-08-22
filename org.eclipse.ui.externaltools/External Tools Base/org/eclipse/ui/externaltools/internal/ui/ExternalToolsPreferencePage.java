/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Frederic Gurr - Fixed restore default behavior (bug 346082)
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ui;


import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;

/**
 * Preference page that allows the user to customize external tools
 */
public class ExternalToolsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button promptForToolMigrationButton;
	private Button promptForProjectMigrationButton;
	
	public ExternalToolsPreferencePage() {
		setPreferenceStore(ExternalToolsPlugin.getDefault().getPreferenceStore());
		setDescription(ExternalToolsUIMessages.ExternalToolsPreferencePage_External_tool_project_builders_migration_2);
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IExternalToolsHelpContextIds.EXTERNAL_TOOLS_PREFERENCE_PAGE);
		//The main composite
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight=0;
		layout.marginWidth=0;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());
				
		promptForToolMigrationButton= createCheckButton(composite, ExternalToolsUIMessages.ExternalToolsPreferencePage_Prompt_before_migrating_3, IPreferenceConstants.PROMPT_FOR_TOOL_MIGRATION);
		promptForProjectMigrationButton= createCheckButton(composite, ExternalToolsUIMessages.ExternalToolsPreferencePage_1, IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION);
		
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
		promptForProjectMigrationButton.setSelection(getPreferenceStore().getDefaultBoolean(IPreferenceConstants.PROMPT_FOR_PROJECT_MIGRATION));
		super.performDefaults();
	}
}
