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
package org.eclipse.debug.internal.ui.preferences;


import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The page for setting debugger preferences.  Built on the 'field editor' infrastructure.
 */
public class DebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {

	private RadioGroupFieldEditor fSaveRadioFieldEditor;
	
	private static final String PERSPECTIVE_NONE_NAME = LaunchConfigurationsMessages.getString("CommonTab.None");; //$NON-NLS-1$
	
	public DebugPreferencePage() {
		super(GRID);

		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(DebugPreferencesMessages.getString("DebugPreferencePage.General_Settings_for_Debugging_1")); //$NON-NLS-1$
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.DEBUG_PREFERENCE_PAGE);
	}
	
	/**
	 * @see FieldEditorPreferencePage#createFieldEditors
	 */
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, DebugPreferencesMessages.getString("DebugPreferencePage.auto_build_before_launch"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$		
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, DebugPreferencesMessages.getString("DebugPreferencePage.Remove_terminated_launches_when_a_new_launch_is_created_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_REUSE_EDITOR, DebugPreferencesMessages.getString("DebugPreferencePage.Reuse_editor_when_displa&ying_source_code_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_ACTIVATE_WORKBENCH, DebugPreferencesMessages.getString("DebugPreferencePage.Activate_the_&workbench_when_a_breakpoint_is_hit_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		
		createSpacer(getFieldEditorParent(), 1);
		
		createSaveBeforeLaunchEditors(getFieldEditorParent());
		
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	private void createSaveBeforeLaunchEditors(Composite parent) {
		fSaveRadioFieldEditor = new RadioGroupFieldEditor(IDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH_RADIO, DebugPreferencesMessages.getString("DebugPreferencePage.Save_dirty_editors_before_launching_4"), 3,  //$NON-NLS-1$
										new String[][] {{DebugPreferencesMessages.getString("DebugPreferencePage.&Never_5"), IDebugUIConstants.PREF_NEVER_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH}, //$NON-NLS-1$
														{DebugPreferencesMessages.getString("DebugPreferencePage.&Prompt_6"), IDebugUIConstants.PREF_PROMPT_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH}, //$NON-NLS-1$
														{DebugPreferencesMessages.getString("DebugPreferencePage.Auto-sav&e_7"), IDebugUIConstants.PREF_AUTOSAVE_DIRTY_EDITORS_BEFORE_LAUNCH}}, //$NON-NLS-1$
										parent, true);
		addField(fSaveRadioFieldEditor);			
	}	
		
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	
	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok= super.performOk();
		DebugUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}			
}

