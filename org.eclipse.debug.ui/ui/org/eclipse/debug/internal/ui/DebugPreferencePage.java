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
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW, "Show Debug Perspective when a program is launched in de&bug mode", SWT.NONE, getFieldEditorParent()));
		addField(new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_SHOW_PROCESS_VIEW, "Show Debug Perspective when a program is launched in &run mode", SWT.NONE, getFieldEditorParent()));
		addField(new BooleanFieldEditor(CONSOLE_OPEN, "Show &Console View when there is program output", SWT.NONE, getFieldEditorParent()));
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

