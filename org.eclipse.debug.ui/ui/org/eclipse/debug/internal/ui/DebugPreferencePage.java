package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.ui.IDebugUIConstants;import org.eclipse.jface.preference.*;import org.eclipse.swt.SWT;import org.eclipse.swt.widgets.Composite;import org.eclipse.ui.IWorkbench;import org.eclipse.ui.IWorkbenchPreferencePage;import org.eclipse.ui.help.WorkbenchHelp;

/*
 * The page for setting the default debugger preferences.
 */
public class DebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, IDebugPreferenceConstants {

	private static final String PREFIX= "debug_preferences.";
	private static final String DEBUG= PREFIX + "debug";
	private static final String RUN= PREFIX + "run";
	private static final String C_OPEN= PREFIX + "console_open";

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
		BooleanFieldEditor debug= new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW, DebugUIUtils.getResourceString(DEBUG), SWT.NONE, getFieldEditorParent());
		BooleanFieldEditor run= new BooleanFieldEditor(IDebugUIConstants.PREF_AUTO_SHOW_PROCESS_VIEW, DebugUIUtils.getResourceString(RUN), SWT.NONE, getFieldEditorParent());
		BooleanFieldEditor consoleOpen= new BooleanFieldEditor(CONSOLE_OPEN, DebugUIUtils.getResourceString(C_OPEN), SWT.NONE, getFieldEditorParent());

		addField(debug);
		addField(run);
		addField(consoleOpen);
	}

	/**
	 * @see IWorkbenchPreferencePage#init
	 */
	public void init(IWorkbench workbench) {
	}
}

