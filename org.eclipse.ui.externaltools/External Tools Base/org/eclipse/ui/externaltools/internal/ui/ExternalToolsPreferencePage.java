package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;

/**
 * Preference page that allows the user to customize external tools
 */
public class ExternalToolsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button promptForMigrationButton;
	private Button showConsoleButton;
	
	public ExternalToolsPreferencePage() {
		setDescription("Preferences for external tools:");
		setPreferenceStore(ExternalToolsPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Font font = parent.getFont();
		
		//The main composite
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight=0;
		layout.marginWidth=0;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		
		createProjectBuilderOptions(composite, font);
		
		return composite;
	}
	
	private void createProjectBuilderOptions(Composite parent, Font font) {
		Group group= new Group(parent, SWT.NONE);
		group.setText("Project Builders");
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(data);
		GridLayout layout= new GridLayout();
		group.setLayout(layout);
		group.setFont(font);
		
		promptForMigrationButton= new Button(group, SWT.CHECK | SWT.LEFT);
		promptForMigrationButton.setFont(font);
		promptForMigrationButton.setText("Always &prompt before migrating project builders");
		promptForMigrationButton.setToolTipText("Check this button to be prompted whenever a project builder needs to be migrated to the new format");
		promptForMigrationButton.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.PROMPT_FOR_MIGRATION));
		
		showConsoleButton= new Button(group, SWT.CHECK | SWT.LEFT);
		showConsoleButton.setFont(font);
		showConsoleButton.setText("Show &console when project builders write output");
		showConsoleButton.setToolTipText("Check this button to show the console whenever a project builder generates output");
		showConsoleButton.setSelection(getPreferenceStore().getBoolean(IPreferenceConstants.SHOW_CONSOLE_FOR_BUILDERS));
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
		getPreferenceStore().setValue(IPreferenceConstants.PROMPT_FOR_MIGRATION, promptForMigrationButton.getSelection());
		getPreferenceStore().setValue(IPreferenceConstants.SHOW_CONSOLE_FOR_BUILDERS, showConsoleButton.getSelection());
		return super.performOk();
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		promptForMigrationButton.setSelection(getPreferenceStore().getDefaultBoolean(IPreferenceConstants.PROMPT_FOR_MIGRATION));
		showConsoleButton.setSelection(getPreferenceStore().getDefaultBoolean(IPreferenceConstants.SHOW_CONSOLE_FOR_BUILDERS));
		super.performDefaults();
	}

}
