package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;

/*
 * The page for setting the default debugger preferences.
 */
public class DebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {

	public DebugPreferencePage() {
		super(GRID);

		IPreferenceStore store= DebugUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(
			parent,
			new Object[] { IDebugHelpContextIds.DEBUG_PREFERENCE_PAGE });
	}
	
	/**
	 * @see FieldEditorPreferencePage#createFieldEditors
	 */
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW, DebugUIMessages.getString("DebugPreferencePage.Show_Debug_Perspective_when_a_program_is_launched_in_de&bug_mode_1"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_SHOW_PROCESS_VIEW, DebugUIMessages.getString("DebugPreferencePage.Show_Debug_Perspective_when_a_program_is_launched_in_&run_mode_2"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
		addField(new BooleanFieldEditor(CONSOLE_OPEN, DebugUIMessages.getString("DebugPreferencePage.Show_&Console_View_when_there_is_program_output_3"), SWT.NONE, getFieldEditorParent())); //$NON-NLS-1$
	}

	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
	
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW, true);
		store.setDefault(IDebugUIConstants.PREF_AUTO_SHOW_PROCESS_VIEW, true);
		store.setDefault(IDebugPreferenceConstants.CONSOLE_OPEN, true);
	}
}

