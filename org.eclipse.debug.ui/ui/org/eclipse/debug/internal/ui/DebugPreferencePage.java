package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/*
 * The page for setting the default debugger preferences.
 */
public class DebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {

	private RadioGroupFieldEditor fSaveRadioFieldEditor;

	public DebugPreferencePage() {
		super(GRID);

		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(DebugUIMessages.getString("DebugPreferencePage.General_Settings_for_Debugging_1")); //$NON-NLS-1$
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(
			parent,
			IDebugHelpContextIds.DEBUG_PREFERENCE_PAGE);
	}
	
	/**
	 * @see FieldEditorPreferencePage#createFieldEditors
	 */
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_SINGLE_CLICK_LAUNCHING, DebugUIMessages.getString("DebugPreferencePage.&Single-click_launching_2"), SWT.NONE, getFieldEditorParent()));  //$NON-NLS-1$
		createSaveBeforeLaunchEditors(getFieldEditorParent());
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, DebugUIMessages.getString("DebugPreferencePage.auto_build_before_launch"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$		
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW, DebugUIMessages.getString("DebugPreferencePage.Show_Debug_Perspective_when_a_program_is_launched_in_de&bug_mode_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_SHOW_PROCESS_VIEW, DebugUIMessages.getString("DebugPreferencePage.Show_Debug_Perspective_when_a_program_is_launched_in_&run_mode_2"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, DebugUIMessages.getString("DebugPreferencePage.Remove_terminated_launches_when_a_new_launch_is_created_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new RadioGroupFieldEditor(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION,
											DebugUIMessages.getString("DebugPreferencePage.Orientation_of_detail_pane_in_variables_view_1"), //$NON-NLS-1$
											1,
											new String[][] {
												{DebugUIMessages.getString("DebugPreferencePage.To_the_right_of_variables_tree_pane_2"), IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_RIGHT}, //$NON-NLS-1$
												{DebugUIMessages.getString("DebugPreferencePage.Underneath_the_variables_tree_pane_3"), IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH} //$NON-NLS-1$
											},
											getFieldEditorParent()));
											
		addField(new ColorFieldEditor(CHANGED_VARIABLE_RGB, DebugUIMessages.getString("DebugPreferencePage.&Changed_variable_value_color__3"), getFieldEditorParent())); //$NON-NLS-1$
	}

	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	protected static void initDefaults(IPreferenceStore store) {
		store.setDefault(IDebugUIConstants.PREF_SINGLE_CLICK_LAUNCHING, false);
		
		
		store.setDefault(IDebugUIConstants.PREF_BUILD_BEFORE_LAUNCH, true);	
		store.setDefault(IDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH_RADIO, IDebugUIConstants.PREF_PROMPT_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH);
		store.setDefault(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW, true);
		store.setDefault(IDebugUIConstants.PREF_AUTO_SHOW_PROCESS_VIEW, true);
		store.setDefault(IDebugUIConstants.PREF_AUTO_REMOVE_OLD_LAUNCHES, false);
		store.setDefault(IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_ORIENTATION, IDebugPreferenceConstants.VARIABLES_DETAIL_PANE_UNDERNEATH);
		PreferenceConverter.setDefault(store, CHANGED_VARIABLE_RGB, new RGB(255, 0, 0));
	}
	
	private void createSaveBeforeLaunchEditors(Composite parent) {
		fSaveRadioFieldEditor = new RadioGroupFieldEditor(IDebugUIConstants.PREF_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH_RADIO, DebugUIMessages.getString("DebugPreferencePage.Save_dirty_editors_before_launching_4"), 1,  //$NON-NLS-1$
										new String[][] {{DebugUIMessages.getString("DebugPreferencePage.&Never_5"), IDebugUIConstants.PREF_NEVER_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH}, //$NON-NLS-1$
														{DebugUIMessages.getString("DebugPreferencePage.&Prompt_6"), IDebugUIConstants.PREF_PROMPT_SAVE_DIRTY_EDITORS_BEFORE_LAUNCH}, //$NON-NLS-1$
														{DebugUIMessages.getString("DebugPreferencePage.Auto-sav&e_7"), IDebugUIConstants.PREF_AUTOSAVE_DIRTY_EDITORS_BEFORE_LAUNCH}}, //$NON-NLS-1$
										parent);
		addField(fSaveRadioFieldEditor);
		
			
	}						
}

