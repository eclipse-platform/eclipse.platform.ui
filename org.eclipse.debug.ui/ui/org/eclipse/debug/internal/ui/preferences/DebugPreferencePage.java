package org.eclipse.debug.internal.ui.preferences;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

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
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
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
		
		createSpacer(getFieldEditorParent(), 1);
		
		createSaveBeforeLaunchEditors(getFieldEditorParent());
		
		createSpacer(getFieldEditorParent(), 1);
		
		String[][] perspectiveNamesAndIds = getPerspectiveNamesAndIds();
		addField(new ComboFieldEditor(IDebugUIConstants.PREF_SHOW_DEBUG_PERSPECTIVE_DEFAULT,
									   DebugPreferencesMessages.getString("DebugPreferencePage.Default_perspective_for_Debug_2"), //$NON-NLS-1$
									   perspectiveNamesAndIds,
									   getFieldEditorParent()));
									   
		addField(new ComboFieldEditor(IDebugUIConstants.PREF_SHOW_RUN_PERSPECTIVE_DEFAULT,
									   DebugPreferencesMessages.getString("DebugPreferencePage.Default_perspective_for_Run_3"), //$NON-NLS-1$
									   perspectiveNamesAndIds,
									   getFieldEditorParent()));
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, true);	
		store.setDefault(IDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH_RADIO, IDebugUIConstants.PREF_PROMPT_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
		store.setDefault(IDebugUIConstants.PREF_SHOW_DEBUG_PERSPECTIVE_DEFAULT, IDebugUIConstants.ID_DEBUG_PERSPECTIVE);
		store.setDefault(IDebugUIConstants.PREF_SHOW_RUN_PERSPECTIVE_DEFAULT, IDebugUIConstants.PERSPECTIVE_NONE);
		store.setDefault(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, false);
		
		store.setDefault(IDebugUIConstants.PREF_REUSE_EDITOR, true);
	}
	
	private void createSaveBeforeLaunchEditors(Composite parent) {
		fSaveRadioFieldEditor = new RadioGroupFieldEditor(IDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH_RADIO, DebugPreferencesMessages.getString("DebugPreferencePage.Save_dirty_editors_before_launching_4"), 1,  //$NON-NLS-1$
										new String[][] {{DebugPreferencesMessages.getString("DebugPreferencePage.&Never_5"), IDebugUIConstants.PREF_NEVER_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH}, //$NON-NLS-1$
														{DebugPreferencesMessages.getString("DebugPreferencePage.&Prompt_6"), IDebugUIConstants.PREF_PROMPT_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH}, //$NON-NLS-1$
														{DebugPreferencesMessages.getString("DebugPreferencePage.Auto-sav&e_7"), IDebugUIConstants.PREF_AUTOSAVE_DIRTY_EDITORS_BEFORE_LAUNCH}}, //$NON-NLS-1$
										parent, true);
		addField(fSaveRadioFieldEditor);			
	}	
	
	/**
	 * Return a 2-dimensional array of perspective names and ids arranged as follows:
	 * { {persp1name, persp1id}, {persp2name, persp2id}, ... }
	 */
	protected static String[][] getPerspectiveNamesAndIds() {
		IPerspectiveRegistry reg = PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor[] persps = reg.getPerspectives();
		
		String[][] table = new String[persps.length + 1][2];
		table[0][0] = PERSPECTIVE_NONE_NAME;
		table[0][1] = IDebugUIConstants.PERSPECTIVE_NONE;
		for (int i = 0; i < persps.length; i++) {
			table[i + 1][0] = persps[i].getLabel();
			table[i + 1][1] = persps[i].getId();
		}
		
		return table;
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

