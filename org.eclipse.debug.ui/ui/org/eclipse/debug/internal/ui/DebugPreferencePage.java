package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;

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

