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


import java.text.MessageFormat;

import org.eclipse.debug.internal.ui.AlwaysNeverDialog;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
	
	public DebugPreferencePage() {
		super(GRID);

		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(DebugPreferencesMessages.getString("DebugPreferencePage.General_Settings_for_Debugging_1")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createControl(Composite)
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
		createWaitForBuildEditor();
		
		createSpacer(getFieldEditorParent(), 2);
		
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE, DebugPreferencesMessages.getString("DebugPreferencePage.25"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, DebugPreferencesMessages.getString("DebugPreferencePage.Remove_terminated_launches_when_a_new_launch_is_created_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_REUSE_EDITOR, DebugPreferencesMessages.getString("DebugPreferencePage.Reuse_editor_when_displa&ying_source_code_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_ACTIVATE_WORKBENCH, DebugPreferencesMessages.getString("DebugPreferencePage.Activate_the_&workbench_when_a_breakpoint_is_hit_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_ACTIVATE_DEBUG_VIEW, DebugPreferencesMessages.getString("DebugPreferencePage.26"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		createSwitchPerspectiveOnSuspendEditor();
		createSwitchPerspectiveEditor();
		
		createSpacer(getFieldEditorParent(), 2);
		
		createSaveBeforeLaunchEditors();
		
		createRelaunchInDebugMode();
		
		createSpacer(getFieldEditorParent(), 2);
		
		final IntegerFieldEditor editor = new IntegerFieldEditor(IDebugUIConstants.PREF_MAX_HISTORY_SIZE, DebugPreferencesMessages.getString("DebugPreferencePage.10"), getFieldEditorParent()); //$NON-NLS-1$
		int historyMax = IDebugPreferenceConstants.MAX_LAUNCH_HISTORY_SIZE;
		editor.setTextLimit(Integer.toString(historyMax).length());
		editor.setErrorMessage(MessageFormat.format(DebugPreferencesMessages.getString("DebugPreferencePage.11"), new Object[] { new Integer(1), new Integer(historyMax)})); //$NON-NLS-1$
		editor.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		editor.setValidRange(1, historyMax);		
		editor.setPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID)) 
					setValid(editor.isValid());
			}
		});
		addField(editor);
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	private void createSaveBeforeLaunchEditors() {
		addField(new RadioGroupFieldEditor(IDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH_RADIO, DebugPreferencesMessages.getString("DebugPreferencePage.Save_dirty_editors_before_launching_4"), 3,  //$NON-NLS-1$
										new String[][] {{DebugPreferencesMessages.getString("DebugPreferencePage.Auto-sav&e_7"), IDebugUIConstants.PREF_AUTOSAVE_DIRTY_EDITORS_BEFORE_LAUNCH}, //$NON-NLS-1$
														{DebugPreferencesMessages.getString("DebugPreferencePage.&Never_5"), IDebugUIConstants.PREF_NEVER_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH}, //$NON-NLS-1$
														{DebugPreferencesMessages.getString("DebugPreferencePage.&Prompt_6"), IDebugUIConstants.PREF_PROMPT_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH}}, //$NON-NLS-1$														
										getFieldEditorParent(),
										true));	
	}	
	
	private void createWaitForBuildEditor() {
		addField(new RadioGroupFieldEditor(IDebugUIConstants.PREF_WAIT_FOR_BUILD, 
						DebugPreferencesMessages.getString("DebugPreferencePage.12"), 3,  //$NON-NLS-1$
						new String[][] {{DebugPreferencesMessages.getString("DebugPreferencePage.15"), AlwaysNeverDialog.ALWAYS}, //$NON-NLS-1$
							{DebugPreferencesMessages.getString("DebugPreferencePage.13"), AlwaysNeverDialog.NEVER}, //$NON-NLS-1$
							{DebugPreferencesMessages.getString("DebugPreferencePage.14"), AlwaysNeverDialog.PROMPT}}, //$NON-NLS-1$
						getFieldEditorParent(),
						true));
	}
	
	private void createSwitchPerspectiveEditor() {
		addField(new RadioGroupFieldEditor(IDebugUIConstants.PREF_SWITCH_TO_PERSPECTIVE,
				DebugPreferencesMessages.getString("DebugPreferencePage.17"), 3, //$NON-NLS-1$
				new String[][] {{DebugPreferencesMessages.getString("DebugPreferencePage.18"), AlwaysNeverDialog.ALWAYS}, //$NON-NLS-1$
								{DebugPreferencesMessages.getString("DebugPreferencePage.19"), AlwaysNeverDialog.NEVER}, //$NON-NLS-1$
								{DebugPreferencesMessages.getString("DebugPreferencePage.20"), AlwaysNeverDialog.PROMPT}}, //$NON-NLS-1$
				getFieldEditorParent(),
				true));
	}
	
	private void createSwitchPerspectiveOnSuspendEditor() {
		addField(new RadioGroupFieldEditor(IDebugUIConstants.PREF_SWITCH_PERSPECTIVE_ON_SUSPEND,
				DebugPreferencesMessages.getString("DebugPreferencePage.21"), 3, //$NON-NLS-1$
				new String[][] {{DebugPreferencesMessages.getString("DebugPreferencePage.22"), AlwaysNeverDialog.ALWAYS}, //$NON-NLS-1$
								{DebugPreferencesMessages.getString("DebugPreferencePage.23"), AlwaysNeverDialog.NEVER}, //$NON-NLS-1$
								{DebugPreferencesMessages.getString("DebugPreferencePage.24"), AlwaysNeverDialog.PROMPT}}, //$NON-NLS-1$
				getFieldEditorParent(),
				true));
	}
	
	private void createRelaunchInDebugMode() {
		String[][] labelAndValues = new String[][] {{DebugPreferencesMessages.getString("DebugPreferencePage.27"), AlwaysNeverDialog.ALWAYS}, {DebugPreferencesMessages.getString("DebugPreferencePage.28"), AlwaysNeverDialog.NEVER}, {DebugPreferencesMessages.getString("DebugPreferencePage.29"), AlwaysNeverDialog.PROMPT}}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		RadioGroupFieldEditor radioGroupFieldEditor = new RadioGroupFieldEditor(IDebugUIConstants.PREF_RELAUNCH_IN_DEBUG_MODE, DebugPreferencesMessages.getString("DebugPreferencePage.30"), 3, labelAndValues, getFieldEditorParent(), true); //$NON-NLS-1$
		addField(radioGroupFieldEditor);
	}	
		
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		boolean ok= super.performOk();
		DebugUIPlugin.getDefault().savePluginPreferences();
		return ok;
	}			
}

